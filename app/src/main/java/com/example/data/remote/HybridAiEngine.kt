package com.example.data.remote

import android.content.Context

import com.example.data.firebase.LumiAnalyticsManager
import com.example.data.firebase.LumiCrashlyticsManager
import com.example.data.firebase.LumiPerformanceManager
import com.example.data.local.LumiDatabase
import com.example.data.local.dao.AiExecutionLogDao
import com.example.data.local.entity.AiExecutionLogEntity
import com.example.domain.agent.hitl.HitlApprovalManager
import com.example.domain.ai.AiModelRegistry
import com.example.domain.ai.SmartAiRouter
import com.example.domain.model.PetEmotion
import com.example.domain.tools.AgentToolDispatcher
import com.example.domain.tools.ToolRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext
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
    private val toolRetriever: ToolRetriever? = null,
    val onDeviceGemmaEngine: OnDeviceGemmaEngine
) {
    val hitlApprovalManager = HitlApprovalManager(database, toolDispatcher)
    private val geminiEngine = GeminiAgentEngine(toolDispatcher, database, hitlApprovalManager, onDeviceGemmaEngine)
    val downloadManager = context?.let { ModelDownloadManager.getInstance(it) }

    private val _routingMode = MutableStateFlow(AiRoutingMode.HYBRID_AUTO)
    val routingMode = _routingMode.asStateFlow()

    private val performanceManager by lazy {
        try {
            GlobalContext.get().get<LumiPerformanceManager>()
        } catch (_: Exception) {
            null
        }
    }

    private val analyticsManager by lazy {
        try {
            GlobalContext.get().get<LumiAnalyticsManager>()
        } catch (_: Exception) {
            context?.let { LumiAnalyticsManager(it) }
        }
    }

    private val crashlyticsManager by lazy {
        try {
            GlobalContext.get().get<LumiCrashlyticsManager>()
        } catch (_: Exception) {
            null
        }
    }

    fun setRoutingMode(mode: AiRoutingMode) {
        _routingMode.value = mode
    }

    suspend fun clearAiAnalyticsLogs() {
        aiAnalyticsDao.clearAllLogs()
    }

    suspend fun executeUserTurn(
        userMessage: String,
        recentHistory: List<Pair<String, String>> = emptyList(),
        imageAttachment: ByteArray? = null,
        onThought: (String?) -> Unit = {}
    ): EngineTurnResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val currentRoutingMode = _routingMode.value

        val isLocalReady = onDeviceGemmaEngine.isModelReady()
        val decision = SmartAiRouter.routeRequest(
            userMessage = userMessage,
            imageAttachment = imageAttachment,
            userRoutingMode = currentRoutingMode,
            isLocalModelReady = isLocalReady
        )

        val turnResult = if (decision.isLocalOnDevice) {
            try {
                val localResult = onDeviceGemmaEngine.executeOnDeviceTurn(userMessage, recentHistory)
                EngineTurnResult(
                    responseText = localResult.responseText,
                    inferredEmotion = localResult.inferredEmotion,
                    toolReports = localResult.toolReports,
                    usedEngine = "ON_DEVICE_GEMMA"
                )
            } catch (e: Exception) {
                crashlyticsManager?.logBreadcrumb("HybridAiEngine", "On-device Gemma fallback to Cloud Gemini: ${e.message}")
                if (currentRoutingMode == AiRoutingMode.STRICT_ON_DEVICE) {
                    // Strict On-Device Mode: Never send to cloud without user consent
                    EngineTurnResult(
                        responseText = "⚠️ [On-Device Mode]: ${e.message ?: "Local model weights are not downloaded."}\n\nTo chat 100% offline, go to Settings > LLM Settings > On-Device Local LLM Hub and download Gemma 2B.",
                        inferredEmotion = PetEmotion.THINKING,
                        toolReports = emptyList(),
                        usedEngine = "ON_DEVICE_GEMMA_UNREADY"
                    )
                } else {
                    // Hybrid mode: Auto-failover to Cloud Gemini 2.5 Flash
                    val cloudResult = geminiEngine.executeUserTurn(userMessage, recentHistory, imageAttachment, onThought)
                    EngineTurnResult(
                        responseText = cloudResult.responseText,
                        inferredEmotion = cloudResult.inferredEmotion,
                        toolReports = cloudResult.toolReports,
                        usedEngine = "CLOUD_GEMINI_FALLBACK"
                    )
                }
            }
        } else {
            val cloudResult = geminiEngine.executeUserTurn(userMessage, recentHistory, imageAttachment, onThought)
            EngineTurnResult(
                responseText = cloudResult.responseText,
                inferredEmotion = cloudResult.inferredEmotion,
                toolReports = cloudResult.toolReports,
                usedEngine = "CLOUD_GEMINI"
            )
        }

        val duration = System.currentTimeMillis() - startTime
        val promptTokens = ceil(userMessage.length / 4.0).toInt()
        val completionTokens = ceil(turnResult.responseText.length / 4.0).toInt()
        val totalTokens = promptTokens + completionTokens

        // Log Firebase Analytics Event
        analyticsManager?.logAiChatMessage(
            mode = turnResult.usedEngine,
            messageLength = userMessage.length,
            modelUsed = if (decision.isLocalOnDevice) "ondevice-gemma" else "gemini-2.5-flash"
        )

        // Record custom trace in Firebase Performance Monitoring
        try {
            performanceManager?.startTrace("ai_user_turn")?.apply {
                putAttribute("engine_type", turnResult.usedEngine)
                putAttribute("task_category", decision.taskCategory.name)
                putAttribute("is_offline", decision.isLocalOnDevice.toString())
                putMetric("prompt_tokens", promptTokens.toLong())
                putMetric("completion_tokens", completionTokens.toLong())
                putMetric("total_tokens", totalTokens.toLong())
                putMetric("turn_duration_ms", duration)
                stop()
            }
        } catch (_: Exception) {}

        try {
            aiAnalyticsDao.insertLog(
                AiExecutionLogEntity(
                    taskCategory = decision.taskCategory.name,
                    engineType = turnResult.usedEngine,
                    modelName = if (turnResult.usedEngine.contains("GEMMA")) "gemma-2b-it-int4" else "gemini-2.5-flash",
                    promptPreview = userMessage.take(150),
                    responsePreview = turnResult.responseText.take(200),
                    promptTokens = promptTokens,
                    completionTokens = completionTokens,
                    totalTokens = totalTokens,
                    estimatedCostUsd = if (turnResult.usedEngine.contains("GEMMA")) 0.0 else 0.0001,
                    startTimeMillis = startTime,
                    finishTimeMillis = System.currentTimeMillis(),
                    durationMs = duration,
                    isSuccess = true,
                    isOffline = turnResult.usedEngine.contains("GEMMA"),
                    hardwareTarget = if (turnResult.usedEngine.contains("GEMMA")) "GPU OpenCL / NPU" else "Google Cloud Vertex AI",
                    routingReason = decision.routingReason
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
