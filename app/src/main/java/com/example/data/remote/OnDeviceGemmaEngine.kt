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
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(512)
                    .setTopK(40)
                    .setTemperature(0.4f)
                    .build()
                llmInference = LlmInference.createFromOptions(context, options)
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
            val rawOutput = llmInference?.generateResponse(prompt)
                ?: throw OnDeviceInferenceException.InferenceExecutionError("Local engine returned null.")

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
                            toolName = tool.displayName,
                            executionTimeMs = duration,
                            isSuccess = execResult.success,
                            outputSummary = execResult.resultText
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
        if (llmInference == null) throw OnDeviceInferenceException.InferenceExecutionError("Not initialized")
        val start = System.currentTimeMillis()
        val response = llmInference?.generateResponse("Test") ?: "Failed"
        Pair(response, System.currentTimeMillis() - start)
    }
}
