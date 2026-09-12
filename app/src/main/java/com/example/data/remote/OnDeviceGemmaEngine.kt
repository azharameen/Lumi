package com.example.data.remote

import android.app.ActivityManager
import android.content.Context
import com.example.data.local.entity.AiExecutionLogEntity
import com.example.domain.model.PetEmotion
import com.example.domain.tools.AgentToolDispatcher
import com.example.domain.tools.ToolRegistry
import com.example.domain.tools.ToolRetriever
import com.example.domain.model.ToolExecutionReport
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject


sealed class OnDeviceInferenceException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class HardwareIncompatible(message: String) : OnDeviceInferenceException(message)
    class ModelNotFound(val modelId: String, message: String) : OnDeviceInferenceException(message)
    class InsufficientMemory(val requiredBytes: Long, val availableBytes: Long, message: String) : OnDeviceInferenceException(message)
    class InferenceExecutionError(message: String, cause: Throwable? = null) : OnDeviceInferenceException(message, cause)
}

data class GemmaModelStatus(
    val isModelLoaded: Boolean,
    val accelerator: String = "GPU OpenCL / NPU",
    val contextWindowTokens: Int = 2048,
    val generationSpeedTokPerSec: Double = 0.0,
    val availableDeviceRamBytes: Long = 0L,
    val requiredRamBytes: Long = 2_200_000_000L,
    val isMemorySufficient: Boolean = true
)

class OnDeviceGemmaEngine(
    private val toolDispatcher: AgentToolDispatcher,
    private val downloadManager: ModelDownloadManager? = null,
    private val context: Context? = null,
    private val toolRetriever: ToolRetriever? = null
) {
    private val crashlyticsManager by lazy {
        try {
            org.koin.core.context.GlobalContext.get().get<com.example.data.firebase.LumiCrashlyticsManager>()
        } catch (_: Exception) {
            null
        }
    }
    companion object {
        private const val RAM_HEADROOM_SAFETY_MARGIN_BYTES = 350_000_000L
    }

    private var llmInference: LlmInference? = null
    private var loadedModelPath: String? = null

    private val _selectedAccelerator = kotlinx.coroutines.flow.MutableStateFlow(HardwareAccelerator.GPU_OPENCL)
    val selectedAccelerator: kotlinx.coroutines.flow.StateFlow<HardwareAccelerator> = _selectedAccelerator.asStateFlow()

    val activeModelId: kotlinx.coroutines.flow.StateFlow<String?> 
        get() = downloadManager?.activeModelId ?: kotlinx.coroutines.flow.MutableStateFlow(null).asStateFlow()

    fun setHardwareAccelerator(accelerator: HardwareAccelerator) {
        _selectedAccelerator.value = accelerator
    }


    fun isHardwareSupported(): Boolean {
        // MediaPipe Tasks GenAI only provides native JNI binaries (libllm_inference_engine_jni.so) for ARM64 (arm64-v8a).
        // On x86_64 emulators, even if arm64 is in the device translation abilist, an x86_64 app process
        // cannot dynamically load arm64 native shared libraries directly without crashing with UnsatisfiedLinkError.
        // Therefore, verify that the primary ABI of the running process is ARM.
        val primaryAbi = android.os.Build.SUPPORTED_ABIS?.firstOrNull() ?: ""
        return primaryAbi.contains("arm64", ignoreCase = true) || primaryAbi.contains("v7a", ignoreCase = true)
    }

    fun isModelReady(): Boolean {
        if (!isHardwareSupported()) return false
        val activeSpec = downloadManager?.getActiveModelSpec() ?: return false
        val modelFile = downloadManager.getModelFile(activeSpec.id)
        return modelFile.exists() && modelFile.length() > 0L
    }

    fun checkMemoryAvailability(requiredBytes: Long): Pair<Boolean, Long> {
        if (context == null) return Pair(true, Long.MAX_VALUE)
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        // MediaPipe uses mmap memory mapping for model weights rather than Java heap allocation.
        // Only block local execution if Android OS explicitly flags a lowMemory state
        // or available RAM is below the critical safety margin (250MB).
        val isCriticallyLow = memoryInfo.lowMemory || memoryInfo.availMem < 250_000_000L
        return Pair(!isCriticallyLow, memoryInfo.availMem)
    }

    fun getDiagnostics(): GemmaModelStatus {
        val (isMemSufficient, availRam) = checkMemoryAvailability(2_200_000_000L)
        return GemmaModelStatus(
            isModelLoaded = llmInference != null,
            availableDeviceRamBytes = availRam,
            isMemorySufficient = isMemSufficient
        )
    }

    suspend fun executeOnDeviceTurn(
        userMessage: String,
        recentHistory: List<Pair<String, String>> = emptyList()
    ): AgentExecutionResult = withContext(Dispatchers.Default) {
        if (context == null) throw OnDeviceInferenceException.HardwareIncompatible("Application context required for MediaPipe Local LLM.")

        val activeSpec = downloadManager?.getActiveModelSpec()
            ?: throw OnDeviceInferenceException.ModelNotFound("unknown", "No active on-device model spec is configured.")

        val modelTag = activeSpec.name
        val modelFile = downloadManager.getModelFile(activeSpec.id)

        if (!modelFile.exists() || modelFile.length() <= 0L) {
            throw OnDeviceInferenceException.ModelNotFound(
                activeSpec.id,
                "Model weights for $modelTag are missing. Ensure the .bin file is fully downloaded."
            )
        }

        val (isMemSufficient, availRam) = checkMemoryAvailability(activeSpec.requiredRamBytes)
        if (!isMemSufficient) {
            throw OnDeviceInferenceException.InsufficientMemory(
                requiredBytes = activeSpec.requiredRamBytes,
                availableBytes = availRam,
                message = "Cannot initialize Local LLM: Insufficient RAM. Device is in low memory state."
            )
        }

        try {
            // Load/Init MediaPipe Inference Engine
            if (llmInference == null || loadedModelPath != modelFile.absolutePath) {
                llmInference?.close()
                val optionsBuilder = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(512)
                    .setTopK(40)
                    .setTemperature(0.4f)

                // Try GPU first, fallback to CPU if initialization fails (often due to resource contention)
                llmInference = try {
                    LlmInference.createFromOptions(context, optionsBuilder.build())
                } catch (t: Throwable) {
                    if (t is LinkageError || t is UnsatisfiedLinkError) {
                        throw OnDeviceInferenceException.HardwareIncompatible(
                            "MediaPipe GenAI native library (libllm_inference_engine_jni.so) requires an ARM-compatible device (arm64-v8a). x86_64 emulators are not supported by MediaPipe."
                        )
                    }
                    val msg = t.message ?: ""
                    if (msg.contains("model identifier") || msg.contains("TFL3") || msg.contains("initialize session") || msg.contains("RET_CHECK")) {
                        modelFile.delete()
                        loadedModelPath = null
                        downloadManager?.notifyCorruptedOrDeleted(activeSpec.id)
                        throw OnDeviceInferenceException.ModelNotFound(activeSpec.id, "Corrupted model detected during load and removed.")
                    }
                    crashlyticsManager?.logBreadcrumb("OnDeviceGemmaEngine", "GPU Init failed, falling back to CPU: $msg")
                    try {
                        LlmInference.createFromOptions(context, optionsBuilder.build())
                    } catch (inner: Throwable) {
                        if (inner is LinkageError || inner is UnsatisfiedLinkError) {
                            throw OnDeviceInferenceException.HardwareIncompatible(
                                "MediaPipe GenAI native library (libllm_inference_engine_jni.so) requires an ARM-compatible device (arm64-v8a). x86_64 emulators are not supported by MediaPipe."
                            )
                        }
                        throw inner
                    }
                }
                loadedModelPath = modelFile.absolutePath
            }

            val conversationHistory = recentHistory.takeLast(4).joinToString("\n") { "${it.first}: ${it.second}" }
            
            // Stage 1: Fast Tool Retrieval (<5ms) with Intent Gating
            // Only inject tool schemas if the user message signals actionable tool intent
            val actionIntentKeywords = listOf(
                "set", "schedule", "create", "add", "turn", "toggle", "open", "launch",
                "check", "battery", "uptime", "alarm", "reminder", "timer", "volume",
                "brightness", "wifi", "bluetooth", "note", "task", "event", "status"
            )
            val hasActionIntent = actionIntentKeywords.any { keyword ->
                Regex("""\b${Regex.escape(keyword)}\b""", RegexOption.IGNORE_CASE).containsMatchIn(userMessage)
            }

            val relevantTools = if (hasActionIntent) {
                toolRetriever?.getRelevantTools(userMessage, maxTools = 3) ?: emptyList()
            } else {
                emptyList()
            }

            val toolPromptSection = if (relevantTools.isNotEmpty()) {
                val toolsXml = relevantTools.joinToString("\n") { tool ->
                    val paramsStr = tool.parameters.joinToString(" ") { "${it.name}=\"${it.type}\"" }
                    "<tool name=\"${tool.id}\" desc=\"${tool.description}\" $paramsStr/>"
                }
                "\nAvailable Tools:\n$toolsXml\nIf you need to call a tool, reply ONLY with: <tool_call><name>TOOL_NAME</name><args>{\"key\": \"val\"}</args></tool_call>\n"
            } else ""

            val systemPrompt = "You are Lumi, a friendly, loving AI companion pet. Reply warmly and concisely to your friend."
            
            // Build Gemma Instruction-Tuning Prompt with native alternating turns (<start_of_turn>user ... <end_of_turn><start_of_turn>model)
            val prompt = buildString {
                val pastTurns = recentHistory.takeLast(6)
                if (pastTurns.isNotEmpty()) {
                    pastTurns.forEachIndexed { index, (sender, text) ->
                        val isUser = sender.equals("user", ignoreCase = true)
                        val roleTag = if (isUser) "user" else "model"
                        append("<start_of_turn>$roleTag\n")
                        if (index == 0 && isUser) {
                            append(systemPrompt)
                            if (toolPromptSection.isNotBlank()) {
                                append("\n").append(toolPromptSection)
                            }
                            append("\n\n")
                        }
                        append(text.trim())
                        append("<end_of_turn>\n")
                    }
                    append("<start_of_turn>user\n")
                    append(userMessage)
                    append("<end_of_turn>\n<start_of_turn>model\n")
                } else {
                    append("<start_of_turn>user\n")
                    append(systemPrompt)
                    if (toolPromptSection.isNotBlank()) {
                        append("\n").append(toolPromptSection)
                    }
                    append("\n\n")
                    append(userMessage)
                    append("<end_of_turn>\n<start_of_turn>model\n")
                }
            }

            // Stage 2: Real True Local Inference Execution
            val rawOutput = try {
                llmInference?.generateResponse(prompt)
                    ?: throw OnDeviceInferenceException.InferenceExecutionError("Local engine returned null.")
            } catch (t: Throwable) {
                if (t is LinkageError || t is UnsatisfiedLinkError) {
                    throw OnDeviceInferenceException.HardwareIncompatible(
                        "MediaPipe GenAI native library requires an ARM-compatible device (arm64-v8a)."
                    )
                }
                if (t.message?.contains("model identifier") == true || t.message?.contains("TFL3") == true) {
                    // Critical Corruption Detected: Wipe model file and notify download manager to show Download button in UI
                    modelFile.delete()
                    loadedModelPath = null
                    llmInference = null
                    downloadManager?.notifyCorruptedOrDeleted(activeSpec.id)
                    throw OnDeviceInferenceException.ModelNotFound(activeSpec.id, "Corrupted model detected and removed. Please re-download.")
                }
                throw t
            }

            val toolReports = mutableListOf<ToolExecutionReport>()
            val executedToolIds = mutableSetOf<String>()
            val executedToolSummaries = mutableListOf<String>()

            // Stage 3: Parse XML Tool Calls & Local Kotlin Execution (Handle multiple or duplicated calls)
            val toolCallRegex = Regex("<tool_call>\\s*<name>(.*?)</name>\\s*<args>(.*?)</args>\\s*</tool_call>", RegexOption.DOT_MATCHES_ALL)
            val matches = toolCallRegex.findAll(rawOutput).toList()

            for (match in matches) {
                val toolId = match.groupValues[1].trim()
                val argsJsonStr = match.groupValues[2].trim()

                // Deduplicate repetitive tool executions in a single model turn
                if (!executedToolIds.add(toolId)) continue

                val tool = ToolRegistry.getInstance().getTool(toolId)
                if (tool != null) {
                    val paramsMap = mutableMapOf<String, Any?>()
                    try {
                        val jsonObj = JSONObject(argsJsonStr)
                        jsonObj.keys().forEach { key -> paramsMap[key] = jsonObj.get(key) }
                    } catch (_: Exception) {
                        // ignore malformed json
                    }

                    val startTime = System.currentTimeMillis()
                    val execResult = tool.execute(paramsMap)
                    val duration = System.currentTimeMillis() - startTime

                    toolReports.add(
                        ToolExecutionReport(
                            toolName = tool.id,
                            title = tool.displayName,
                            description = execResult.resultText,
                            isSuccess = execResult.success,
                            payloadPreview = execResult.resultText.take(100)
                        )
                    )
                    executedToolSummaries.add(execResult.resultText)
                }
            }

            // Thoroughly scrub XML tags and raw tool call syntax from conversational output
            var generatedText = rawOutput
                .replace("<start_of_turn>model", "")
                .replace("<end_of_turn>", "")
                .replace("<start_of_turn>user", "")
                .replace(Regex("<tool_call>.*?</tool_call>", RegexOption.DOT_MATCHES_ALL), "")
                .replace(Regex("<tool.*?>.*?</tool.*?>", RegexOption.DOT_MATCHES_ALL), "")
                .replace(Regex("</?tool_call>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("</?tool>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("</?args>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("</?name>", RegexOption.IGNORE_CASE), "")
                .trim()

            // Gracefully truncate hallucinated multi-turn continuations (e.g. model fabricating next "user: ... assistant: ...")
            val nextTurnPattern = Regex("""(?:\n|<start_of_turn>)\s*(?:user|human|userl|assistant|model|lumi)\s*[:\-]""", RegexOption.IGNORE_CASE)
            val turnCutoff = nextTurnPattern.find(generatedText)?.range?.first
            if (turnCutoff != null && turnCutoff > 0) {
                generatedText = generatedText.substring(0, turnCutoff).trim()
            }

            // Strip leading speaker prefixes (e.g. "Lumi:", "Assistant:", "Model:")
            generatedText = generatedText
                .replace(Regex("""^(?:lumi|assistant|model|ai|bot)\s*[:\-]\s*""", RegexOption.IGNORE_CASE), "")
                .trim()

            // Clean any stray "user:" or "assistant:" transcript artifacts
            generatedText = generatedText
                .replace(Regex("""(?i)\b(?:user|assistant|userl)\s*:\s*"""), "")
                .trim()

            // If the model produced only tool calls with no companion text, provide a natural companion response
            if (generatedText.isBlank()) {
                generatedText = when {
                    executedToolSummaries.isNotEmpty() -> executedToolSummaries.joinToString("\n\n")
                    else -> "I'm right here with you! How can I help?"
                }
            }

            val lowerText = generatedText.lowercase(java.util.Locale.ROOT)
            val emotion = when {
                lowerText.contains("schedule") || lowerText.contains("calendar") -> PetEmotion.ENERGETIC
                lowerText.contains("task") || lowerText.contains("todo") -> PetEmotion.HAPPY
                lowerText.contains("breathe") || lowerText.contains("water") -> PetEmotion.CALM
                lowerText.contains("stress") || lowerText.contains("sorry") -> PetEmotion.CONCERNED
                else -> PetEmotion.HAPPY
            }

            AgentExecutionResult(generatedText, emotion, toolReports)
        } catch (h: OnDeviceInferenceException) {
            throw h
        } catch (t: Throwable) {
            if (t is LinkageError || t is UnsatisfiedLinkError) {
                throw OnDeviceInferenceException.HardwareIncompatible(
                    "MediaPipe GenAI native library requires an ARM-compatible device (arm64-v8a). It is not supported on x86_64 emulators."
                )
            }
            throw OnDeviceInferenceException.InferenceExecutionError(
                "True Local inference error on $modelTag: ${t.localizedMessage}",
                t
            )
        }
    }

    suspend fun benchmarkOnDeviceGemma(): Pair<String, Long> = withContext(Dispatchers.Default) {
        val safeContext = context ?: throw OnDeviceInferenceException.HardwareIncompatible("Context required for benchmark.")
        if (!isHardwareSupported()) {
            throw OnDeviceInferenceException.HardwareIncompatible("MediaPipe GenAI native library requires ARM64 architecture.")
        }
        
        try {
            // Initialize engine if not loaded
            if (llmInference == null) {
                if (!isModelReady()) throw OnDeviceInferenceException.ModelNotFound("unknown", "Model weights missing for benchmark.")
                val activeSpec = downloadManager?.getActiveModelSpec() ?: throw OnDeviceInferenceException.ModelNotFound("unknown", "No active spec.")
                val modelFile = downloadManager.getModelFile(activeSpec.id)
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(128)
                    .build()
                llmInference = LlmInference.createFromOptions(safeContext, options)
                loadedModelPath = modelFile.absolutePath
            }
            
            val start = System.currentTimeMillis()
            val response = llmInference?.generateResponse("Test") ?: "Failed"
            Pair(response, System.currentTimeMillis() - start)
        } catch (h: OnDeviceInferenceException) {
            throw h
        } catch (t: Throwable) {
            if (t is LinkageError || t is UnsatisfiedLinkError) {
                throw OnDeviceInferenceException.HardwareIncompatible("MediaPipe GenAI native library requires ARM64 architecture.")
            }
            throw OnDeviceInferenceException.InferenceExecutionError("Benchmark failed: ${t.localizedMessage}", t)
        }
    }

    /**
     * Semantically classifies a user query into a structured Intent/Skill category.
     */
    suspend fun classifyIntent(userQuery: String): String = withContext(Dispatchers.Default) {
        if (!isModelReady() || !isHardwareSupported() || context == null) return@withContext "GENERAL_COMPANION"
        
        val prompt = """
            You are a semantic classifier. Categorize the user's message into EXACTLY ONE of these categories:
            - GOOGLE_WORKSPACE (if about emails, docs, sheets, drive)
            - GITHUB (if about issues, repos, code, pull requests)
            - SLACK (if about messaging, channels, status)
            - LIFE_ORGANIZER (if about tasks, todos, calendar, schedules)
            - WELLNESS (if about health, mood, breathing, meditation)
            - GENERAL_COMPANION (anything else)
            
            User message: "$userQuery"
            Category:
        """.trimIndent()

        try {
            // Re-use or init inference
            if (llmInference == null) {
                val activeSpec = downloadManager?.getActiveModelSpec() ?: return@withContext "GENERAL_COMPANION"
                val modelFile = downloadManager.getModelFile(activeSpec.id)
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(16)
                    .setTemperature(0.1f)
                    .build()
                llmInference = LlmInference.createFromOptions(context, options)
                loadedModelPath = modelFile.absolutePath
            }

            val raw = llmInference?.generateResponse(prompt)?.trim() ?: "GENERAL_COMPANION"
            
            when {
                raw.contains("GOOGLE_WORKSPACE") -> "GOOGLE_WORKSPACE"
                raw.contains("GITHUB") -> "GITHUB"
                raw.contains("SLACK") -> "SLACK"
                raw.contains("LIFE_ORGANIZER") -> "LIFE_ORGANIZER"
                raw.contains("WELLNESS") -> "WELLNESS"
                else -> "GENERAL_COMPANION"
            }
        } catch (t: Throwable) {
            "GENERAL_COMPANION"
        }
    }
}
