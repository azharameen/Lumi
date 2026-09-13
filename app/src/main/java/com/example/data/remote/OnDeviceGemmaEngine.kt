package com.example.data.remote

import android.app.ActivityManager
import android.content.Context
import com.example.data.local.entity.AiExecutionLogEntity
import com.example.domain.model.PetEmotion
import com.example.domain.tools.AgentToolDispatcher
import com.example.domain.tools.ToolRegistry
import com.example.domain.tools.ToolRetriever
import com.example.domain.tools.LumiTool
import com.example.domain.model.ToolExecutionReport
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
        const val DEFAULT_CONTEXT_MAX_TOKENS = 1024
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
        recentHistory: List<Pair<String, String>> = emptyList(),
        memoryContext: String = "",
        onStreamToken: suspend (String) -> Unit = {}
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
                    .setMaxTokens(DEFAULT_CONTEXT_MAX_TOKENS)
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

            // Stage 1: Dynamically retrieve candidate tools relevant ONLY to the active userMessage via FTS BM25
            val relevantCandidates = try {
                toolRetriever?.getRelevantTools(userMessage, maxTools = 5) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            val toolPromptSection = if (relevantCandidates.isNotEmpty()) {
                buildString {
                    append("[Tools]\n")
                    for (t in relevantCandidates) {
                        val paramDesc = if (t.parameters.isNotEmpty()) {
                            "(" + t.parameters.joinToString(", ") { "${it.name}: ${it.type.lowercase(java.util.Locale.ROOT)}" } + ")"
                        } else "()"
                        append("- ${t.id}$paramDesc: ${t.description}\n")
                    }
                    append("\n[CRITICAL TOOL CALLING RULE]\n")
                    append("If the user's message is an action or request matching any tool in [Tools], output ONLY the tool call in this exact format and nothing else:\n")
                    append("<tool_call><name>TOOL_ID</name><args>{\"param\": \"val\"}</args></tool_call>\n")
                    append("Do NOT output conversational chatter or explanations when executing a tool.\n")
                }.trim()
            } else ""

            val systemPrompt = "You are Lumi, a loving, helpful AI companion pet and life planner on Android. You control device hardware directly. When asked for a specific action or command, execute only that request directly and concisely. Do not drag forward or resume previous discussion topics unless the user explicitly asks to."
            
            // Build Gemma Instruction-Tuning Prompt ensuring tools & memory context are ALWAYS in the active turn
            val prompt = buildString {
                val prunedHistory = com.example.domain.ai.ContextRelevancePruner.getInstance()
                    .pruneHistory(userMessage, recentHistory)
                val pastTurns = prunedHistory.takeLast(2)
                for ((sender, text) in pastTurns) {
                    val roleTag = if (sender.equals("user", ignoreCase = true)) "user" else "model"
                    val cleanText = text
                        .replace(Regex("<tool_call>.*?</tool_call>", RegexOption.DOT_MATCHES_ALL), "")
                        .trim()
                    if (cleanText.isNotBlank()) {
                        append("<start_of_turn>$roleTag\n").append(cleanText.take(100).trim()).append("<end_of_turn>\n")
                    }
                }
                append("<start_of_turn>user\n")
                append(systemPrompt).append("\n\n")
                if (memoryContext.isNotBlank()) {
                    append("User Context & Memories:\n").append(memoryContext.take(200).trim()).append("\n\n")
                }
                append(toolPromptSection).append("\n\n")
                append("User request: ").append(userMessage.take(200).trim())
                append("<end_of_turn>\n<start_of_turn>model\n")
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

            // Stage 3: Multi-Format Tool Call Parser (XML, JSON, & Function signatures)
            data class ExtractedCall(val rawName: String, val argsJson: String)
            val extractedCalls = mutableListOf<ExtractedCall>()

            // Format 1: XML <tool_call><name>...</name><args>...</args></tool_call>
            val xmlRegex = Regex("<tool_call>\\s*<name>(.*?)</name>\\s*<args>(.*?)</args>\\s*</tool_call>", RegexOption.DOT_MATCHES_ALL)
            for (match in xmlRegex.findAll(rawOutput)) {
                extractedCalls.add(ExtractedCall(match.groupValues[1].trim(), match.groupValues[2].trim()))
            }

            // Format 2: XML <tool_call>{...}</tool_call>
            val xmlJsonRegex = Regex("<tool_call>\\s*(\\{.*?\\})\\s*</tool_call>", RegexOption.DOT_MATCHES_ALL)
            for (match in xmlJsonRegex.findAll(rawOutput)) {
                try {
                    val jsonObj = JSONObject(match.groupValues[1].trim())
                    val name = jsonObj.optString("name").ifBlank { jsonObj.optString("call").ifBlank { jsonObj.optString("tool") } }
                    val args = jsonObj.optJSONObject("args")?.toString() ?: jsonObj.optJSONObject("parameters")?.toString() ?: "{}"
                    if (name.isNotBlank()) extractedCalls.add(ExtractedCall(name, args))
                } catch (_: Exception) {}
            }

            // Format 3: Embedded JSON {"call": "...", "args": {...}}
            val jsonPattern = Regex("""\{[^{}]*?"(?:call|tool|name)"\s*:\s*"([^"]+)"[^{}]*?"(?:args|parameters)"\s*:\s*(\{[^{}]*\})[^{}]*?\}""", RegexOption.DOT_MATCHES_ALL)
            for (match in jsonPattern.findAll(rawOutput)) {
                extractedCalls.add(ExtractedCall(match.groupValues[1].trim(), match.groupValues[2].trim()))
            }

            // Format 4: Function signature syntax e.g. system_toggle_flashlight(true)
            val funcRegex = Regex("""\b((?:system_|communication_|set_)[a-z_]+)\s*\((.*?)\)""", RegexOption.IGNORE_CASE)
            for (match in funcRegex.findAll(rawOutput)) {
                val name = match.groupValues[1].trim()
                val argsContent = match.groupValues[2].trim()
                val argsMap = mutableMapOf<String, Any?>()
                
                // Relying purely on the LLM's generated arguments instead of hardcoded keyword matching on user message
                if (argsContent.contains("true", ignoreCase = true)) {
                    argsMap["state"] = true
                } else if (argsContent.contains("false", ignoreCase = true)) {
                    argsMap["state"] = false
                }
                
                val digitMatch = Regex("""\+?\d[\d\s\-]{4,}\d""").find(argsContent)?.value
                if (digitMatch != null) {
                    argsMap["phoneNumber"] = digitMatch.replace(Regex("""[\s\-]"""), "")
                }
                
                extractedCalls.add(ExtractedCall(name, JSONObject(argsMap as Map<*, *>).toString()))
            }

            for (call in extractedCalls) {
                val tool = resolveTool(call.rawName)
                if (tool != null && executedToolIds.add(tool.id)) {
                    val rawParams = mutableMapOf<String, Any?>()
                    try {
                        val jsonObj = JSONObject(call.argsJson)
                        jsonObj.keys().forEach { key -> rawParams[key] = jsonObj.get(key) }
                    } catch (_: Exception) {}

                    val safeParams = normalizeParams(tool, rawParams)
                    val execResult = tool.execute(safeParams)

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
                .replace(jsonPattern, "")
                .replace(funcRegex, "")
                .replace(Regex("""(?i)\[?(?:tools|available tools|format|instructions|example)\]?:?.*?(?:\n|$)"""), "")
                .replace(Regex("""(?i)when the user asks to control.*?(?:\n|$)"""), "")
                .replace(Regex("""(?i)you must output.*?(?:\n|$)"""), "")
                .replace(Regex("""(?i)^\s*[-*]?\s*(?:system_|communication_|set_)[a-z_]+.*?(?:\n|$)""", RegexOption.MULTILINE), "")
                .replace(Regex("""(?i)\b(?:system_|communication_|set_)[a-z_]+\s*\(.*?\).*?(?:\n|$)""", RegexOption.MULTILINE), "")
                .replace(Regex("""(?i)^\s*(?:this command|you can use this command).*?(?:\n|$)""", RegexOption.MULTILINE), "")
                .replace(Regex("""(?i)user\s*request\s*:?.*?(?:\n|$)"""), "")
                .trim()

            // Gracefully truncate hallucinated multi-turn continuations
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

            // Detect any remaining instruction leak or prompt echo
            val isContaminatedWithInstructions = generatedText.contains("instruction", ignoreCase = true) ||
                    generatedText.contains("tool_call", ignoreCase = true) ||
                    generatedText.contains("output a tool", ignoreCase = true) ||
                    generatedText.contains("device settings", ignoreCase = true) ||
                    generatedText.contains("<name>", ignoreCase = true) ||
                    generatedText.contains("<args>", ignoreCase = true) ||
                    generatedText.contains("system_", ignoreCase = true) ||
                    generatedText.contains("command turns", ignoreCase = true) ||
                    generatedText.contains("use this command", ignoreCase = true)

            // If tools were executed, ALWAYS return the clean companion confirmation and tool status,
            // avoiding any leaked tool call signatures, camera explanations, or method strings
            if (executedToolSummaries.isNotEmpty()) {
                val summaryText = executedToolSummaries.joinToString("\n")
                generatedText = "$summaryText ✨"
            } else if (generatedText.isBlank() || isContaminatedWithInstructions) {
                generatedText = "I'm right here with you! How can I help?"
            }

            val emotion = PetEmotion.HAPPY

            // Stage 4: Real-time token streaming to the UI
            streamTokensGracefully(generatedText, onStreamToken)

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


    private fun resolveTool(toolIdOrName: String): LumiTool? {
        val registry = ToolRegistry.getInstance()
        val trimmed = toolIdOrName.trim()
        val clean = trimmed.lowercase(java.util.Locale.ROOT)
            .removePrefix("system_")
            .removePrefix("tool_")
            .replace(" ", "_")
        
        // 1. Exact ID match
        registry.getTool(trimmed)?.let { return it }
        // 2. Case-insensitive ID match
        registry.getAllTools().find { it.id.equals(trimmed, ignoreCase = true) }?.let { return it }
        // 3. ID without system_ prefix
        registry.getAllTools().find { it.id.removePrefix("system_").equals(clean, ignoreCase = true) }?.let { return it }
        // 4. Common device & assistant aliases
        when (clean) {
            "flashlight", "torch", "light", "toggle_flashlight" -> registry.getTool("system_toggle_flashlight")?.let { return it }
            "bluetooth", "open_bluetooth", "bt" -> registry.getTool("system_open_bluetooth_settings")?.let { return it }
            "wifi", "wi_fi", "open_wifi", "internet_settings" -> registry.getTool("system_open_wifi_settings")?.let { return it }
            "location", "gps", "open_location" -> registry.getTool("system_open_location_settings")?.let { return it }
            "display", "screen", "brightness_settings" -> registry.getTool("system_open_display_settings")?.let { return it }
            "battery", "battery_status", "power" -> registry.getTool("system_battery_status")?.let { return it }
            "volume", "media_volume", "sound" -> registry.getTool("system_set_media_volume")?.let { return it }
            "timer", "set_timer", "countdown" -> registry.getTool("set_timer")?.let { return it }
            "alarm", "set_alarm", "alarm_clock" -> registry.getTool("set_alarm")?.let { return it }
            "app", "open_app", "launch_app" -> registry.getTool("system_open_app")?.let { return it }
            "storage", "storage_info", "disk" -> registry.getTool("system_storage_info")?.let { return it }
            "uptime", "device_uptime" -> registry.getTool("system_device_uptime")?.let { return it }
            "ram", "memory", "ram_usage" -> registry.getTool("system_ram_usage")?.let { return it }
            "network", "network_status", "connectivity" -> registry.getTool("system_network_status")?.let { return it }
        }
        // 5. Contains match on id or display name
        registry.getAllTools().find { 
            it.id.contains(clean, ignoreCase = true) || 
            it.displayName.contains(clean, ignoreCase = true) 
        }?.let { return it }
        
        return null
    }

    private fun normalizeParams(tool: LumiTool, rawArgs: Map<String, Any?>): Map<String, Any?> {
        val normalized = rawArgs.toMutableMap()
        when (tool.id) {
            "system_toggle_flashlight" -> {
                val stateRaw = rawArgs["state"] ?: rawArgs["enabled"] ?: rawArgs["on"] ?: rawArgs["status"] ?: rawArgs["action"]
                val boolState = when (stateRaw?.toString()?.lowercase(java.util.Locale.ROOT)) {
                    "off", "false", "0", "disable", "deactivate", "stop" -> false
                    else -> true
                }
                normalized["state"] = boolState
            }
            "system_open_app" -> {
                val appVal = rawArgs["appName"] ?: rawArgs["app"] ?: rawArgs["name"] ?: rawArgs["application"]
                if (appVal != null) normalized["appName"] = appVal.toString()
            }
            "system_set_media_volume", "system_set_ringer_volume", "system_set_alarm_volume" -> {
                val lvl = rawArgs["level"] ?: rawArgs["volume"] ?: rawArgs["percentage"] ?: rawArgs["val"] ?: 50
                normalized["level"] = lvl
            }
            "set_timer" -> {
                val sec = rawArgs["seconds"] ?: rawArgs["duration"] ?: rawArgs["time"] ?: 60
                normalized["seconds"] = sec
            }
        }
        return normalized
    }


    private suspend fun streamTokensGracefully(text: String, onStreamToken: suspend (String) -> Unit) {
        if (text.isBlank()) return
        val words = text.split(" ")
        val sb = StringBuilder()
        for (i in words.indices) {
            if (i > 0) sb.append(" ")
            sb.append(words[i])
            onStreamToken(sb.toString())
            delay(16) // Smooth natural streaming cadence
        }
        onStreamToken(text)
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
                    .setMaxTokens(DEFAULT_CONTEXT_MAX_TOKENS)
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
            - COMMUNICATION (if about dialing numbers, phone calls, texting, drafting SMS)
            - DEVICE_CONTROLS (if about flashlight, bluetooth, wifi, volume, alarms, timers, apps, battery, device settings)
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
                    .setMaxTokens(DEFAULT_CONTEXT_MAX_TOKENS)
                    .setTemperature(0.1f)
                    .build()
                llmInference = LlmInference.createFromOptions(context, options)
                loadedModelPath = modelFile.absolutePath
            }

            val raw = llmInference?.generateResponse(prompt)?.trim() ?: "GENERAL_COMPANION"
            
            when {
                raw.contains("COMMUNICATION") -> "COMMUNICATION"
                raw.contains("DEVICE_CONTROLS") -> "DEVICE_CONTROLS"
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

    /**
     * Intelligently generates context-aware follow-up suggestion pills from recent dialogue turns
     * using the on-device Gemma LLM. Zero keyword matching.
     */
    suspend fun generateFollowUpSuggestions(
        recentHistory: List<Pair<String, String>>,
        maxSuggestions: Int = 4
    ): List<String> = withContext(Dispatchers.Default) {
        if (!isModelReady() || !isHardwareSupported() || context == null || recentHistory.isEmpty()) {
            return@withContext emptyList()
        }

        val dialogueContext = recentHistory.takeLast(3).joinToString("\n") { (sender, text) ->
            val role = if (sender.equals("user", ignoreCase = true)) "User" else "Lumi"
            "$role: ${text.take(120).trim()}"
        }

        val prompt = """
            <start_of_turn>user
            You are Lumi's suggestion engine. Given this recent conversation between a user and their AI companion Lumi:
            $dialogueContext

            Generate $maxSuggestions short, helpful follow-up actions or questions the user might want to say or do next.
            Strict rules:
            - Output each suggestion on its own line.
            - Start each suggestion with an emoji.
            - Keep each suggestion under 6 words.
            - Do NOT include numbering, bullet points, asterisks, or explanations.<end_of_turn>
            <start_of_turn>model
        """.trimIndent()

        try {
            if (llmInference == null) {
                val activeSpec = downloadManager?.getActiveModelSpec() ?: return@withContext emptyList()
                val modelFile = downloadManager.getModelFile(activeSpec.id)
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(DEFAULT_CONTEXT_MAX_TOKENS)
                    .setTemperature(0.3f)
                    .build()
                llmInference = LlmInference.createFromOptions(context, options)
                loadedModelPath = modelFile.absolutePath
            }

            val raw = llmInference?.generateResponse(prompt)?.trim() ?: return@withContext emptyList()

            raw.lines()
                .map { line ->
                    line.replace(Regex("""^[\d\.\-\*\s]+"""), "")
                        .replace("<end_of_turn>", "")
                        .replace("<start_of_turn>", "")
                        .trim()
                }
                .filter { it.length in 3..50 && !it.contains("suggestion", ignoreCase = true) }
                .take(maxSuggestions)
        } catch (t: Throwable) {
            emptyList()
        }
    }
}
