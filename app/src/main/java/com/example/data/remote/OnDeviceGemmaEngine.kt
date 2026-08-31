package com.example.data.remote

import android.app.ActivityManager
import android.content.Context
import android.os.SystemClock
import com.example.domain.model.PetEmotion
import com.example.domain.tools.AgentToolDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.google.mediapipe.tasks.genai.llminference.LlmInference

enum class AiRoutingMode {
    HYBRID_AUTO,         // Smart auto-switching: Gemma for offline/banter/wellness, Gemini for vision/complex
    STRICT_ON_DEVICE,    // 100% On-Device Gemma (zero cloud data transmission, ultra privacy)
    CLOUD_TURBO          // Cloud Gemini 2.5 Flash prioritized for maximum reasoning capability
}

sealed class OnDeviceInferenceException(message: String, cause: Throwable? = null) : IllegalStateException(message, cause) {
    class ModelNotFound(val modelId: String, message: String) : OnDeviceInferenceException(message)
    class InsufficientMemory(val requiredBytes: Long, val availableBytes: Long, message: String) : OnDeviceInferenceException(message)
    class HardwareIncompatible(message: String) : OnDeviceInferenceException(message)
    class InferenceExecutionError(message: String, cause: Throwable? = null) : OnDeviceInferenceException(message, cause)
}

data class GemmaModelStatus(
    val modelName: String = "Local LLM",
    val isModelLoaded: Boolean = false,
    val modelSizeBytes: Long = 1_438_400_000L,
    val quantPrecision: String = "INT4-Q4_K_M (MediaPipe)",
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
    private val context: Context? = null
) {
    companion object {
        private const val RAM_HEADROOM_SAFETY_MARGIN_BYTES = 350_000_000L
    }

    private var llmInference: LlmInference? = null
    private var loadedModelPath: String? = null

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
            val prompt = if (conversationHistory.isNotBlank()) {
                "Context:\n$conversationHistory\nUser: $userMessage\nAssistant:"
            } else {
                "User: $userMessage\nAssistant:"
            }

            // Real True Local Inference Execution
            val generatedText = llmInference?.generateResponse(prompt)
                ?: throw OnDeviceInferenceException.InferenceExecutionError("Local engine returned null.")

            val lowerText = generatedText.lowercase()
            val emotion = when {
                lowerText.contains("schedule") || lowerText.contains("calendar") -> PetEmotion.ENERGETIC
                lowerText.contains("task") || lowerText.contains("todo") -> PetEmotion.HAPPY
                lowerText.contains("breathe") || lowerText.contains("water") -> PetEmotion.CALM
                lowerText.contains("stress") || lowerText.contains("sorry") -> PetEmotion.CONCERNED
                else -> PetEmotion.HAPPY
            }

            AgentExecutionResult(generatedText, emotion, emptyList())
        } catch (e: Exception) {
            throw OnDeviceInferenceException.InferenceExecutionError(
                "True Local inference error on $modelTag: ${e.localizedMessage}",
                e
            )
        }
    }
}
