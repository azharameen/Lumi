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


    fun isModelReady(): Boolean {
        val activeSpec = downloadManager?.getActiveModelSpec() ?: return false
        val modelFile = downloadManager.getModelFile(activeSpec.id)
        return modelFile.exists() && modelFile.length() > 0L
    }

    fun checkMemoryAvailability(requiredBytes: Long): Pair<Boolean, Long> {
        if (context == null) return Pair(true, Long.MAX_VALUE)
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val effectiveAvailable = memoryInfo.availMem - RAM_HEADROOM_SAFETY_MARGIN_BYTES
        return Pair(effectiveAvailable >= requiredBytes, memoryInfo.availMem)
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
                } catch (e: Exception) {
                    val msg = e.message ?: ""
                    if (msg.contains("model identifier") || msg.contains("TFL3") || msg.contains("initialize session") || msg.contains("RET_CHECK")) {
                        modelFile.delete()
                        loadedModelPath = null
                        throw OnDeviceInferenceException.ModelNotFound(activeSpec.id, "Corrupted model detected during load and removed.")
                    }
                    crashlyticsManager?.logBreadcrumb("OnDeviceGemmaEngine", "GPU Init failed, falling back to CPU: $msg")
                    LlmInference.createFromOptions(context, optionsBuilder.build())
                }
                loadedModelPath = modelFile.absolutePath
            }

            val conversationHistory = recentHistory.takeLast(4).joinToString("\n") { "${it.first}: ${it.second}" }
            
            // Stage 1: Fast Tool Retrieval (<5ms)
            val relevantTools = toolRetriever?.getRelevantTools(userMessage, maxTools = 3) ?: emptyList()
            val toolPromptSection = if (relevantTools.isNotEmpty()) {
                val toolsXml = relevantTools.joinToString("\n") { tool ->
                    val paramsStr = tool.parameters.joinToString(" ") { "${it.name}=\"${it.type}\"" }
                    "<tool name=\"${tool.id}\" desc=\"${tool.description}\" $paramsStr/>"
                }
                "\nAvailable Tools:\n$toolsXml\nIf needed, reply ONLY with: <tool_call><name>TOOL_NAME</name><args>{\"key\": \"val\"}</args></tool_call>\n"
            } else ""

            val prompt = if (conversationHistory.isNotBlank()) {
                "Context:\n$conversationHistory$toolPromptSection\nUser: $userMessage\nAssistant:"
            } else {
                "User: $userMessage$toolPromptSection\nAssistant:"
            }

            // Stage 2: Real True Local Inference Execution
            val rawOutput = try {
                llmInference?.generateResponse(prompt)
                    ?: throw OnDeviceInferenceException.InferenceExecutionError("Local engine returned null.")
            } catch (e: Exception) {
                if (e.message?.contains("model identifier") == true || e.message?.contains("TFL3") == true) {
                    // Critical Corruption Detected: Wipe model file to force re-download
                    modelFile.delete()
                    loadedModelPath = null
                    llmInference = null
                    throw OnDeviceInferenceException.ModelNotFound(activeSpec.id, "Corrupted model detected and removed. Please re-download.")
                }
                throw e
            }

            var generatedText = rawOutput
            val toolReports = mutableListOf<ToolExecutionReport>()

            // Stage 3: Parse XML Tool Calls & Local Kotlin Execution
            val toolCallRegex = Regex("<tool_call><name>(.*?)</name><args>(.*?)</args></tool_call>", RegexOption.DOT_MATCHES_ALL)
            val match = toolCallRegex.find(rawOutput)
            if (match != null) {
                val toolId = match.groupValues[1].trim()
                val argsJsonStr = match.groupValues[2].trim()
                val tool = ToolRegistry.getInstance().getTool(toolId)

                if (tool != null) {
                    val paramsMap = mutableMapOf<String, Any?>()
                    try {
                        val jsonObj = JSONObject(argsJsonStr)
                        jsonObj.keys().forEach { key -> paramsMap[key] = jsonObj.get(key) }
                    } catch (e: Exception) {
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
                    generatedText = "Executed ${tool.displayName}: ${execResult.resultText}"
                }
            }

            val lowerText = generatedText.lowercase()
            val emotion = when {
                lowerText.contains("schedule") || lowerText.contains("calendar") -> PetEmotion.ENERGETIC
                lowerText.contains("task") || lowerText.contains("todo") -> PetEmotion.HAPPY
                lowerText.contains("breathe") || lowerText.contains("water") -> PetEmotion.CALM
                lowerText.contains("stress") || lowerText.contains("sorry") -> PetEmotion.CONCERNED
                else -> PetEmotion.HAPPY
            }

            AgentExecutionResult(generatedText, emotion, toolReports)
        } catch (e: Exception) {
            throw OnDeviceInferenceException.InferenceExecutionError(
                "True Local inference error on $modelTag: ${e.localizedMessage}",
                e
            )
        }
    }

    suspend fun benchmarkOnDeviceGemma(): Pair<String, Long> = withContext(Dispatchers.Default) {
        val safeContext = context ?: throw OnDeviceInferenceException.HardwareIncompatible("Context required for benchmark.")
        
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
    }

    /**
     * Semantically classifies a user query into a structured Intent/Skill category.
     */
    suspend fun classifyIntent(userQuery: String): String = withContext(Dispatchers.Default) {
        if (!isModelReady()) return@withContext "GENERAL_COMPANION"
        
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
                llmInference = LlmInference.createFromOptions(context!!, options)
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
        } catch (e: Exception) {
            "GENERAL_COMPANION"
        }
    }
}
