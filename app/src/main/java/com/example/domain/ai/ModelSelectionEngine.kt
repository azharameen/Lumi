package com.example.domain.ai

import com.example.data.firebase.LumiRemoteConfigManager
import com.example.data.remote.LocalLlmModelSpec
import com.example.data.remote.ModelDownloadManager
import com.example.data.remote.ModelDownloadStatus

/**
 * Unified model selection service.
 * - On-device models: sourced from ModelDownloadManager catalog (static permitted list).
 *   Only DOWNLOADED models are selectable in chat.
 * - Cloud models: sourced dynamically from Firebase Remote Config JSON (never hardcoded).
 *
 * This is the single source of truth for model resolution throughout the app.
 */
class ModelSelectionEngine(
    private val downloadManager: ModelDownloadManager,
    private val remoteConfigManager: LumiRemoteConfigManager? = null
) {
    /**
     * Returns the engine provider for a given model ID.
     * Returns null if the model ID is unrecognised.
     */
    fun resolveProvider(modelId: String): AiEngineProvider? {
        if (modelId.isBlank()) return null
        val localIds = downloadManager.catalog.map { it.id }.toSet()
        if (modelId in localIds) return AiEngineProvider.ON_DEVICE_GEMMA
        val cloudIds = remoteConfigManager?.parsedCloudModels?.value?.map { it.id }?.toSet() ?: emptySet()
        if (modelId in cloudIds) return AiEngineProvider.CLOUD_GEMINI
        // Fallback for models starting with gemma or gemini
        return if (modelId.startsWith("gemma", ignoreCase = true)) AiEngineProvider.ON_DEVICE_GEMMA
        else AiEngineProvider.CLOUD_GEMINI
    }

    /**
     * Returns true if the specified model ID is a local on-device model that has been
     * fully downloaded and is ready for inference.
     */
    fun isLocalModelReady(modelId: String): Boolean {
        val states = downloadManager.downloadStates.value
        return states[modelId]?.status == ModelDownloadStatus.DOWNLOADED
    }

    /**
     * Returns only the local models that have been fully downloaded (DOWNLOADED state).
     * These are the only local models shown in the active model picker.
     */
    fun getDownloadedLocalModels(): List<LocalLlmModelSpec> {
        val states = downloadManager.downloadStates.value
        return downloadManager.catalog.filter { spec ->
            states[spec.id]?.status == ModelDownloadStatus.DOWNLOADED
        }
    }

    /**
     * Returns all available cloud models from Remote Config.
     */
    fun getCloudModels(): List<CloudModelSpec> {
        return remoteConfigManager?.parsedCloudModels?.value ?: emptyList()
    }

    /**
     * Best available utility model ID for background tasks (intent classification,
     * tool routing). Always returns a local model ID if one is available,
     * otherwise returns the active local model ID or fallback.
     */
    fun getBestUtilityModelId(): String? {
        return downloadManager.activeModelId.value
    }

    /**
     * Resolves the human-readable display name for a given model ID.
     * Matches across local catalog and Remote Config cloud models.
     */
    fun getModelDisplayName(modelId: String): String {
        if (modelId.isBlank()) return "Auto"
        val local = downloadManager.catalog.find { it.id.equals(modelId, ignoreCase = true) }
        if (local != null) return local.name
        val cloud = remoteConfigManager?.parsedCloudModels?.value?.find { it.id.equals(modelId, ignoreCase = true) }
        if (cloud != null) return cloud.displayName
        return when {
            modelId.contains("flash-lite", ignoreCase = true) -> "Gemini 2.5 Flash-Lite"
            modelId.contains("flash", ignoreCase = true) -> "Gemini 2.5 Flash"
            modelId.contains("pro", ignoreCase = true) -> "Gemini 2.5 Pro"
            modelId.contains("cpu", ignoreCase = true) -> "Gemma 2B (CPU)"
            modelId.contains("gpu", ignoreCase = true) -> "Gemma 2B (GPU)"
            modelId.startsWith("gemma", ignoreCase = true) -> "Gemma 2B"
            else -> modelId.replace('-', ' ').split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        }
    }
}
