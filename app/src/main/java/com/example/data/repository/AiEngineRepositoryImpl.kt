package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.remote.*
import com.example.domain.agent.hitl.HitlPendingAction
import com.example.domain.repository.AiEngineRepository
import com.example.domain.repository.AiTurnResult
import kotlinx.coroutines.flow.Flow

class AiEngineRepositoryImpl(
    private val hybridAiEngine: HybridAiEngine
) : AiEngineRepository {

    override val aiRoutingMode: Flow<AiRoutingMode> = hybridAiEngine.routingMode
    override val selectedAccelerator: Flow<HardwareAccelerator> = hybridAiEngine.onDeviceGemmaEngine.selectedAccelerator
    override val activeLocalModelId: Flow<String?> = hybridAiEngine.onDeviceGemmaEngine.activeModelId
    override val localModelCatalog: List<LocalLlmModelSpec> = hybridAiEngine.downloadManager?.catalog ?: emptyList()
    override val modelDownloadStates: Flow<Map<String, ModelDownloadProgress>> = hybridAiEngine.downloadManager?.downloadStates ?: kotlinx.coroutines.flow.flowOf(emptyMap())
    override val pendingHitlActions: Flow<List<HitlPendingAction>> = hybridAiEngine.hitlApprovalManager.pendingActions

    override fun setAiRoutingMode(mode: AiRoutingMode) {
        hybridAiEngine.setRoutingMode(mode)
    }

    override fun setHardwareAccelerator(accelerator: HardwareAccelerator) {
        hybridAiEngine.onDeviceGemmaEngine.setHardwareAccelerator(accelerator)
    }

    override fun downloadLocalModel(modelId: String) {
        hybridAiEngine.downloadManager?.downloadModel(modelId)
    }

    override fun pauseModelDownload(modelId: String) {
        hybridAiEngine.downloadManager?.pauseDownload(modelId)
    }

    override fun cancelModelDownload(modelId: String) {
        hybridAiEngine.downloadManager?.cancelDownload(modelId)
    }

    override fun deleteLocalModel(modelId: String) {
        hybridAiEngine.downloadManager?.deleteModel(modelId)
    }

    override fun setActiveLocalModel(modelId: String) {
        hybridAiEngine.downloadManager?.setActiveModel(modelId)
    }

    override suspend fun resolveHitlAction(actionId: String, approved: Boolean): Boolean {
        val state = hybridAiEngine.hitlApprovalManager.resolveAction(actionId, approved)
        return state != null
    }

    override suspend fun executeAiTurn(
        userMessage: String,
        recentHistory: List<Pair<String, String>>,
        imageAttachment: Bitmap?
    ): AiTurnResult {
        val result = hybridAiEngine.executeUserTurn(userMessage, recentHistory, imageAttachment)
        return AiTurnResult(
            responseText = result.responseText,
            inferredEmotion = result.inferredEmotion,
            toolReports = result.toolReports
        )
    }

    override suspend fun benchmarkOnDeviceGemma(): Pair<String, Long> {
        return hybridAiEngine.onDeviceGemmaEngine.benchmarkOnDeviceGemma()
    }

    override suspend fun clearAiAnalyticsLogs() {
        hybridAiEngine.clearAiAnalyticsLogs()
    }
}
