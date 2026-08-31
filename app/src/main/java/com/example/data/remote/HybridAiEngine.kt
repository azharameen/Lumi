package com.example.data.remote

enum class AiRoutingMode {
    HYBRID_AUTO,
    STRICT_ON_DEVICE,
    CLOUD_TURBO
}


import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.LumiDatabase
import com.example.data.local.dao.AiExecutionLogDao
import com.example.data.local.entity.AiExecutionLogEntity
import com.example.domain.agent.hitl.HitlApprovalManager
import com.example.domain.ai.AiEngineProvider
import com.example.domain.ai.AiModelRegistry
import com.example.domain.ai.SmartAiRouter
import com.example.domain.model.PetEmotion
import com.example.domain.tools.AgentToolDispatcher
import com.example.domain.tools.ToolRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.math.ceil

class HybridAiEngine(
    private val toolDispatcher: AgentToolDispatcher,
    private val aiAnalyticsDao: AiExecutionLogDao,
    private val database: LumiDatabase,
    private val context: Context? = null,
    private val toolRetriever: ToolRetriever? = null
) {
    val hitlApprovalManager = HitlApprovalManager(database, toolDispatcher)
    private val geminiEngine = GeminiAgentEngine(toolDispatcher, database, hitlApprovalManager)
    val downloadManager = context?.let { ModelDownloadManager.getInstance(it) }
    val onDeviceGemmaEngine = OnDeviceGemmaEngine(toolDispatcher, downloadManager, context, toolRetriever)

    private val _routingMode = MutableStateFlow(AiRoutingMode.HYBRID_AUTO)
    val routingMode = _routingMode.asStateFlow()

    fun setRoutingMode(mode: AiRoutingMode) {

    suspend fun clearAiAnalyticsLogs() {
        aiAnalyticsDao.clearAllLogs()
    }

        _routingMode.value = mode
    }

    suspend fun executeUserTurn(
        userMessage: String,
        recentHistory: List<Pair<String, String>> = emptyList(),
        imageAttachment: Bitmap? = null
    ): AgentExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val currentMode = _routingMode.value

        // 1. System & Cloud Readiness Inspection
        val apiKey = GeminiClient.getApiKey()
        val isCloudConfigured = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        val activeSpec = downloadManager?.getActiveModelSpec()
        val isLocalModelDownloaded = if (activeSpec != null && downloadManager != null) {
            downloadManager.isModelDownloaded(activeSpec.id)
        } else {
            false
        }

        val requiredRam = activeSpec?.requiredRamBytes ?: 2_200_000_000L
        val (isMemSufficient, _) = onDeviceGemmaEngine.checkMemoryAvailability(requiredRam)
        val isLowMemory = !isMemSufficient

        // 2. Dynamic Intelligent Routing Decision
        val decision = SmartAiRouter.routeRequest(
            userMessage = userMessage,
            imageAttachment = imageAttachment,
            userRoutingMode = currentMode,
            isNetworkAvailable = isCloudConfigured,
            isLocalModelReady = isLocalModelDownloaded,
            isLowMemory = isLowMemory
        )

        var executionResult: AgentExecutionResult
        var finalEngineType = decision.selectedModel.provider.name
        var finalModelName = decision.selectedModel.id
        var hardwareTarget = decision.selectedModel.hardwareTarget
        var isSuccess = true
        var errorMessage: String? = null
        var fallbackTriggered = decision.isFailoverTriggered

        try {
            if (decision.isLocalOnDevice) {
                // Execute On-Device via Gemma Engine
                executionResult = onDeviceGemmaEngine.executeOnDeviceTurn(userMessage, recentHistory)
            } else {
                // Execute Cloud via Gemini Engine (State Machine DAG)
                executionResult = geminiEngine.executeUserTurn(userMessage, recentHistory, imageAttachment)
            }
        } catch (e: Exception) {
            // Failure in primary engine -> Resilient Circuit Breaker Failover Flow
            if (decision.isLocalOnDevice) {
                // Local failed (e.g. Model missing or OOM risk) -> Failover to Cloud Gemini
                if (isCloudConfigured) {
                    fallbackTriggered = true
                    finalEngineType = AiEngineProvider.CLOUD_GEMINI.name
                    finalModelName = AiModelRegistry.GEMINI_2_5_FLASH.id
                    hardwareTarget = "Cloud TPU (Failover from local: ${e.message?.take(60)})"

                    try {
                        executionResult = geminiEngine.executeUserTurn(userMessage, recentHistory, imageAttachment)
                    } catch (cloudErr: Exception) {
                        isSuccess = false
                        errorMessage = "Local error (${e.message}) and Cloud failover error (${cloudErr.message})"
                        executionResult = AgentExecutionResult(
                            responseText = "I encountered an error processing your request on-device (${e.localizedMessage ?: "Inference failure"}) and cloud failover was unreachable.",
                            inferredEmotion = PetEmotion.CONCERNED,
                            toolReports = emptyList()
                        )
                    }
                } else {
                    isSuccess = false
                    errorMessage = e.message ?: "Local inference failure"
                    executionResult = AgentExecutionResult(
                        responseText = e.localizedMessage ?: "On-device model is unavailable and network is offline.",
                        inferredEmotion = PetEmotion.CONCERNED,
                        toolReports = emptyList()
                    )
                }
            } else {
                // Cloud failed -> Failover to On-Device Gemma if ready
                if (isLocalModelDownloaded && !isLowMemory) {
                    fallbackTriggered = true
                    finalEngineType = AiEngineProvider.ON_DEVICE_GEMMA.name
                    finalModelName = activeSpec?.id ?: AiModelRegistry.GEMMA_2B_INT4.id
                    hardwareTarget = "Local GPU/NPU (Failover from Cloud)"

                    try {
                        executionResult = onDeviceGemmaEngine.executeOnDeviceTurn(userMessage, recentHistory)
                    } catch (localErr: Exception) {
                        isSuccess = false
                        errorMessage = "Cloud error (${e.message}) and local failover error (${localErr.message})"
                        executionResult = AgentExecutionResult(
                            responseText = "Cloud service was unreachable and local fallback encountered an error: ${localErr.localizedMessage}",
                            inferredEmotion = PetEmotion.CONCERNED,
                            toolReports = emptyList()
                        )
                    }
                } else {
                    isSuccess = false
                    errorMessage = e.message ?: "Cloud request failure"
                    val reason = if (!isLocalModelDownloaded) "Local model is not downloaded" else "Device RAM is constrained"
                    executionResult = AgentExecutionResult(
                        responseText = "Cloud service connection failed (${e.localizedMessage ?: "Network error"}). Cannot failover to local engine: $reason.",
                        inferredEmotion = PetEmotion.CONCERNED,
                        toolReports = emptyList()
                    )
                }
            }
        }

        val finishTime = System.currentTimeMillis()
        val durationMs = (finishTime - startTime).coerceAtLeast(1L)

        // Calculate token metrics
        val promptTokens = estimateTokens(userMessage) + if (imageAttachment != null) 258 else 0
        val completionTokens = estimateTokens(executionResult.responseText)
        val totalTokens = promptTokens + completionTokens

        // Pricing calculation
        val estimatedCostUsd = if (finalEngineType == AiEngineProvider.CLOUD_GEMINI.name) {
            val inCost = (promptTokens / 1_000_000.0) * decision.selectedModel.inputCostPerMillionTokensUsd
            val outCost = (completionTokens / 1_000_000.0) * decision.selectedModel.outputCostPerMillionTokensUsd
            inCost + outCost
        } else {
            0.0
        }

        // Persist structured analytics audit to Room database
        try {
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
                isOffline = finalEngineType == AiEngineProvider.ON_DEVICE_GEMMA.name,
                hardwareTarget = hardwareTarget
            )
            aiAnalyticsDao.insertLog(logEntity)
        } catch (_: Exception) {}

        executionResult
    }

    private fun estimateTokens(text: String): Int {
        if (text.isBlank()) return 0
        return ceil(text.length / 3.8).toInt().coerceAtLeast(1)
    }
}
