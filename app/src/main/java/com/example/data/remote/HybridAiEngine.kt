package com.example.data.remote

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

enum class AiRoutingMode {
    HYBRID_AUTO,
    STRICT_ON_DEVICE,
    CLOUD_TURBO
}

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
        _routingMode.value = mode
    }

    suspend fun clearAiAnalyticsLogs() {
        aiAnalyticsDao.clearAllLogs()
    }

    suspend fun executeUserTurn(
        userMessage: String,
        recentHistory: List<Pair<String, String>> = emptyList(),
        imageAttachment: Bitmap? = null
    ): EngineTurnResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val currentRoutingMode = _routingMode.value
        val isOnline = SmartAiRouter.checkNetworkAvailability(context)

        val decision = SmartAiRouter.routeTask(
            userPrompt = userMessage,
            hasImage = (imageAttachment != null),
            userRoutingMode = currentRoutingMode,
            isNetworkAvailable = isOnline
        )

        val turnResult = if (decision.engineProvider == AiEngineProvider.ON_DEVICE_GEMMA) {
            val localResult = onDeviceGemmaEngine.executeTurn(userMessage, imageAttachment)
            if (!localResult.isSuccess && isOnline && currentRoutingMode == AiRoutingMode.HYBRID_AUTO) {
                // Fallback to Gemini Cloud Turbo
                val cloudResult = geminiEngine.executeTurn(userMessage, recentHistory, imageAttachment)
                EngineTurnResult(
                    responseText = cloudResult.responseText,
                    inferredEmotion = cloudResult.inferredEmotion,
                    toolReports = cloudResult.toolReports,
                    usedEngine = "CLOUD_GEMINI_FALLBACK"
                )
            } else {
                EngineTurnResult(
                    responseText = localResult.responseText,
                    inferredEmotion = localResult.inferredEmotion,
                    toolReports = localResult.toolReports,
                    usedEngine = "ON_DEVICE_GEMMA"
                )
            }
        } else {
            val cloudResult = geminiEngine.executeTurn(userMessage, recentHistory, imageAttachment)
            EngineTurnResult(
                responseText = cloudResult.responseText,
                inferredEmotion = cloudResult.inferredEmotion,
                toolReports = cloudResult.toolReports,
                usedEngine = "CLOUD_GEMINI"
            )
        }

        val duration = System.currentTimeMillis() - startTime
        try {
            aiAnalyticsDao.insertLog(
                AiExecutionLogEntity(
                    taskCategory = decision.category.name,
                    engineType = turnResult.usedEngine,
                    modelName = if (turnResult.usedEngine.contains("GEMMA")) "gemma-2b-it-int4" else "gemini-2.5-flash",
                    promptPreview = userMessage.take(150),
                    responsePreview = turnResult.responseText.take(200),
                    promptTokens = ceil(userMessage.length / 4.0).toInt(),
                    completionTokens = ceil(turnResult.responseText.length / 4.0).toInt(),
                    totalTokens = ceil((userMessage.length + turnResult.responseText.length) / 4.0).toInt(),
                    estimatedCostUsd = if (turnResult.usedEngine.contains("GEMMA")) 0.0 else 0.0001,
                    startTimeMillis = startTime,
                    finishTimeMillis = System.currentTimeMillis(),
                    durationMs = duration,
                    isSuccess = true,
                    isOffline = !isOnline,
                    hardwareTarget = if (turnResult.usedEngine.contains("GEMMA")) "GPU OpenCL / NPU" else "Google Cloud Vertex AI",
                    routingReason = decision.reasoning
                )
            )
        } catch (e: Exception) {
            // Analytics log failure ignored gracefully
        }

        turnResult
    }
}

data class EngineTurnResult(
    val responseText: String,
    val inferredEmotion: PetEmotion,
    val toolReports: List<com.example.domain.model.ToolExecutionReport> = emptyList(),
    val usedEngine: String
)
