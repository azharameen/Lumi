package com.example.data.remote

import android.content.Context

import com.example.data.firebase.LumiAnalyticsManager
import com.example.data.firebase.LumiCrashlyticsManager
import com.example.data.firebase.LumiPerformanceManager
import com.example.domain.repository.AgentStateRepository
import com.example.domain.memory.SemanticMemoryEngine
import com.example.data.local.dao.AiExecutionLogDao
import com.example.data.local.entity.AiExecutionLogEntity
import com.example.domain.agent.hitl.HitlApprovalManager
import com.example.domain.ai.ModelSelectionEngine
import com.example.domain.ai.SmartAiRouter
import com.example.domain.model.PetEmotion
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.data.preferences.dataStore
import com.example.domain.tools.AgentToolDispatcher
import com.example.domain.tools.ToolRetriever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private val agentStateRepository: AgentStateRepository,
    private val semanticMemoryEngine: SemanticMemoryEngine,
    private val context: Context? = null,
    private val toolRetriever: ToolRetriever? = null,
    val onDeviceGemmaEngine: OnDeviceGemmaEngine,
    private val modelSelectionEngine: ModelSelectionEngine? = null
) {
    val hitlApprovalManager = HitlApprovalManager(agentStateRepository, semanticMemoryEngine, toolDispatcher)
    private val geminiEngine = GeminiAgentEngine(toolDispatcher, agentStateRepository, semanticMemoryEngine, hitlApprovalManager, onDeviceGemmaEngine)
    val downloadManager = context?.let { ModelDownloadManager.getInstance(it) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _routingMode = MutableStateFlow(AiRoutingMode.HYBRID_AUTO)
    val routingMode = _routingMode.asStateFlow()

    companion object {
        private val KEY_AI_ROUTING_MODE = stringPreferencesKey("lumi_ai_routing_mode")
    }

    init {
        context?.let { ctx ->
            scope.launch {
                try {
                    val prefs = ctx.dataStore.data.first()
                    val savedModeName = prefs[KEY_AI_ROUTING_MODE]
                    if (savedModeName != null) {
                        _routingMode.value = AiRoutingMode.valueOf(savedModeName)
                    }
                } catch (_: Exception) {}
            }
        }
    }

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
        context?.let { ctx ->
            scope.launch {
                try {
                    ctx.dataStore.edit { prefs ->
                        prefs[KEY_AI_ROUTING_MODE] = mode.name
                    }
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun clearAiAnalyticsLogs() {
        aiAnalyticsDao.clearAllLogs()
    }

    suspend fun executeUserTurn(
        userMessage: String,
        recentHistory: List<Pair<String, String>> = emptyList(),
        imageAttachment: ByteArray? = null,
        selectedModelId: String? = null,
        onThought: (String?) -> Unit = {},
        onStreamToken: suspend (String) -> Unit = {}
    ): EngineTurnResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val currentRoutingMode = _routingMode.value

        val isLocalReady = onDeviceGemmaEngine.isModelReady()
        val decision = SmartAiRouter.routeRequest(
            userMessage = userMessage,
            imageAttachment = imageAttachment,
            userRoutingMode = currentRoutingMode,
            selectedModelId = selectedModelId,
            modelSelectionEngine = modelSelectionEngine ?: ModelSelectionEngine(
                downloadManager ?: return@withContext EngineTurnResult(
                    responseText = "Model not available.",
                    inferredEmotion = com.example.domain.model.PetEmotion.CONCERNED,
                    usedEngine = "ERROR"
                ),
                null
            ),
            isLocalModelReady = isLocalReady
        )

        val turnResult = if (decision.isLocalOnDevice) {
            if (currentRoutingMode == AiRoutingMode.STRICT_ON_DEVICE && !isLocalReady) {
                val errorMessage = "⚠️ [On-Device Mode]: Local model weights are not downloaded yet.\n\nTo chat 100% offline, go to Settings > LLM Settings > On-Device Local LLM Hub and download Gemma 2B."
                onStreamToken(errorMessage)
                EngineTurnResult(
                    responseText = errorMessage,
                    inferredEmotion = PetEmotion.THINKING,
                    toolReports = emptyList(),
                    usedEngine = "ON_DEVICE_GEMMA_UNREADY"
                )
            } else {
                try {
                    val graphResult = geminiEngine.executeUserTurn(
                        userMessage = userMessage,
                        recentHistory = recentHistory,
                        imageAttachment = imageAttachment,
                        selectedModelId = decision.selectedModelId,
                        onThought = onThought,
                        onStreamToken = onStreamToken
                    )
                    EngineTurnResult(
                        responseText = graphResult.responseText,
                        inferredEmotion = graphResult.inferredEmotion,
                        toolReports = graphResult.toolReports,
                        usedEngine = if (isLocalReady) "ON_DEVICE_GEMMA" else "CLOUD_GEMINI_FALLBACK",
                        finalThought = graphResult.finalThought
                    )
                } catch (e: Throwable) {
                    crashlyticsManager?.logBreadcrumb("HybridAiEngine", "Agent graph execution failed: ${e.message}")
                    if (currentRoutingMode == AiRoutingMode.STRICT_ON_DEVICE) {
                        val errorMessage = when (e) {
                            is OnDeviceInferenceException.HardwareIncompatible ->
                                "⚠️ [On-Device Mode]: MediaPipe GenAI requires an ARM-compatible Android device (arm64-v8a)."
                            is LinkageError ->
                                "⚠️ [On-Device Mode]: Native inference library error (libllm_inference_engine_jni.so)."
                            else ->
                                "⚠️ [On-Device Mode]: ${e.message ?: "Local execution failed."}"
                        }
                        onStreamToken(errorMessage)
                        EngineTurnResult(
                            responseText = errorMessage,
                            inferredEmotion = PetEmotion.THINKING,
                            toolReports = emptyList(),
                            usedEngine = "ON_DEVICE_GEMMA_ERROR"
                        )
                    } else {
                        val cloudResult = geminiEngine.executeUserTurn(userMessage, recentHistory, imageAttachment, decision.selectedModelId, onThought, onStreamToken)
                        EngineTurnResult(
                            responseText = cloudResult.responseText,
                            inferredEmotion = cloudResult.inferredEmotion,
                            toolReports = cloudResult.toolReports,
                            usedEngine = "CLOUD_GEMINI_FALLBACK",
                            finalThought = cloudResult.finalThought
                        )
                    }
                }
            }
        } else {
            val cloudResult = geminiEngine.executeUserTurn(userMessage, recentHistory, imageAttachment, decision.selectedModelId, onThought, onStreamToken)
            EngineTurnResult(
                responseText = cloudResult.responseText,
                inferredEmotion = cloudResult.inferredEmotion,
                toolReports = cloudResult.toolReports,
                usedEngine = "CLOUD_GEMINI",
                finalThought = cloudResult.finalThought
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
            modelUsed = decision.selectedModelId
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
                    modelName = decision.selectedModelId,
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
    val usedEngine: String,
    val finalThought: String? = null
)
