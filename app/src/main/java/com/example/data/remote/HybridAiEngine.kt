package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.dao.AiExecutionLogDao
import com.example.data.local.entity.AiExecutionLogEntity
import com.example.domain.ai.AiEngineProvider
import com.example.domain.ai.AiModelRegistry
import com.example.domain.ai.AiTaskCategory
import com.example.domain.ai.SmartAiRouter
import com.example.domain.model.PetEmotion
import com.example.domain.tools.AgentToolDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.math.ceil

class HybridAiEngine(
    private val toolDispatcher: AgentToolDispatcher,
    private val aiAnalyticsDao: AiExecutionLogDao,
    context: Context? = null
) {
    private val geminiEngine = GeminiAgentEngine(toolDispatcher)
    private val downloadManager = context?.let { ModelDownloadManager.getInstance(it) }
    val onDeviceGemmaEngine = OnDeviceGemmaEngine(toolDispatcher, downloadManager)

    private val _routingMode = MutableStateFlow(AiRoutingMode.HYBRID_AUTO)
    val routingMode = _routingMode.asStateFlow()

    fun setRoutingMode(mode: AiRoutingMode) {
        _routingMode.value = mode
    }

    suspend fun executeUserTurn(
        userMessage: String,
        recentHistory: List<Pair<String, String>> = emptyList(),
        imageAttachment: Bitmap? = null
    ): AgentExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val currentMode = _routingMode.value

        // Check if valid API key is present
        val apiKey = GeminiClient.getApiKey()
        val isCloudConfigured = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        // 1. Dynamic Intelligent Routing Decision
        val decision = SmartAiRouter.routeRequest(
            userMessage = userMessage,
            imageAttachment = imageAttachment,
            userRoutingMode = currentMode,
            isNetworkAvailable = isCloudConfigured
        )

        var executionResult: AgentExecutionResult
        var finalEngineType = decision.selectedModel.provider.name
        var finalModelName = decision.selectedModel.id
        var hardwareTarget = decision.selectedModel.hardwareTarget
        var isSuccess = true
        var errorMessage: String? = null
        var fallbackTriggered = false

        try {
            if (decision.isLocalOnDevice) {
                // Execute On-Device via Gemma
                executionResult = onDeviceGemmaEngine.executeOnDeviceTurn(userMessage, recentHistory)
            } else {
                // Execute Cloud via Gemini
                executionResult = geminiEngine.executeUserTurn(userMessage, recentHistory, imageAttachment)
            }
        } catch (e: Exception) {
            // Resilient Circuit Breaker: Auto Fallback to On-Device Gemma
            fallbackTriggered = true
            isSuccess = false
            errorMessage = "Cloud error (${e.message ?: "network failure"}). Auto-routed to On-Device Gemma fallback."

            finalEngineType = AiEngineProvider.ON_DEVICE_GEMMA.name
            val activeSpec = downloadManager?.getActiveModelSpec()
            finalModelName = activeSpec?.id ?: AiModelRegistry.GEMMA_2B_INT4.id
            hardwareTarget = "GPU (OpenCL / Fallback Local NPU)"

            executionResult = onDeviceGemmaEngine.executeOnDeviceTurn(userMessage, recentHistory)
        }

        val finishTime = System.currentTimeMillis()
        val durationMs = (finishTime - startTime).coerceAtLeast(1L)

        // Calculate token metrics
        val promptTokens = estimateTokens(userMessage) + if (imageAttachment != null) 258 else 0
        val completionTokens = estimateTokens(executionResult.responseText)
        val totalTokens = promptTokens + completionTokens

        // Pricing calculation from ModelSpec
        val estimatedCostUsd = if (finalEngineType == AiEngineProvider.CLOUD_GEMINI.name) {
            val inCost = (promptTokens / 1_000_000.0) * decision.selectedModel.inputCostPerMillionTokensUsd
            val outCost = (completionTokens / 1_000_000.0) * decision.selectedModel.outputCostPerMillionTokensUsd
            inCost + outCost
        } else {
            0.0 // 100% Free on-device inference!
        }

        // Persist structured analytics audit to Room database
        val logEntity = AiExecutionLogEntity(
            taskCategory = decision.taskCategory.displayName,
            engineType = finalEngineType,
            modelName = finalModelName,
            promptPreview = userMessage.take(80),
            responsePreview = executionResult.responseText.take(120),
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = totalTokens,
            estimatedCostUsd = estimatedCostUsd,
            startTimeMillis = startTime,
            finishTimeMillis = finishTime,
            durationMs = durationMs,
            isSuccess = isSuccess,
            isOffline = decision.isLocalOnDevice || fallbackTriggered,
            hardwareTarget = hardwareTarget
        )
        aiAnalyticsDao.insertLog(logEntity)

        executionResult
    }

    private fun estimateTokens(text: String): Int {
        if (text.isBlank()) return 0
        return ceil(text.length / 3.8).toInt().coerceAtLeast(1)
    }
}
