package com.example.domain.repository

import android.graphics.Bitmap
import com.example.data.remote.AiRoutingMode
import com.example.data.remote.HardwareAccelerator
import com.example.data.remote.LocalLlmModelSpec
import com.example.data.remote.ModelDownloadProgress
import com.example.domain.agent.hitl.HitlPendingAction
import com.example.domain.model.PetEmotion
import com.example.domain.model.ToolExecutionReport
import kotlinx.coroutines.flow.Flow

data class AiTurnResult(
    val responseText: String,
    val inferredEmotion: PetEmotion,
    val toolReports: List<ToolExecutionReport> = emptyList()
)

interface AiEngineRepository {
    val aiRoutingMode: Flow<AiRoutingMode>
    val selectedAccelerator: Flow<HardwareAccelerator>
    val activeLocalModelId: Flow<String?>
    val localModelCatalog: List<LocalLlmModelSpec>
    val modelDownloadStates: Flow<Map<String, ModelDownloadProgress>>
    val pendingHitlActions: Flow<List<HitlPendingAction>>

    fun setAiRoutingMode(mode: AiRoutingMode)
    fun setHardwareAccelerator(accelerator: HardwareAccelerator)
    fun downloadLocalModel(modelId: String)
    fun pauseModelDownload(modelId: String)
    fun cancelModelDownload(modelId: String)
    fun deleteLocalModel(modelId: String)
    fun setActiveLocalModel(modelId: String)
    suspend fun resolveHitlAction(actionId: String, approved: Boolean): Boolean
    suspend fun executeAiTurn(
        userMessage: String,
        recentHistory: List<Pair<String, String>> = emptyList(),
        imageAttachment: Bitmap? = null
    ): AiTurnResult
    suspend fun benchmarkOnDeviceGemma(): Pair<String, Long>
    suspend fun clearAiAnalyticsLogs()
}
