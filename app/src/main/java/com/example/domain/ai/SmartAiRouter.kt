package com.example.domain.ai

import com.example.data.remote.AiRoutingMode

data class RoutingDecision(
    val taskCategory: AiTaskCategory,
    val isLocalOnDevice: Boolean,
    val routingReason: String,
    val selectedModelId: String,
    val isFailoverTriggered: Boolean = false,
    val executionWarning: String? = null
)

/**
 * Multi-Tier AI Routing Engine.
 * Routes requests based on:
 * - User's explicitly selected model (primary decision)
 * - Image attachment presence (always routes to cloud for vision)
 * - STRICT_ON_DEVICE mode enforcement
 * - Network connectivity (offline fallback)
 *
 * Does NOT use keyword matching or semantic classification for routing.
 * Task category is preserved only for analytics/logging.
 */
object SmartAiRouter {

    fun routeRequest(
        userMessage: String,
        imageAttachment: ByteArray?,
        userRoutingMode: AiRoutingMode,
        selectedModelId: String?,
        modelSelectionEngine: ModelSelectionEngine,
        isNetworkAvailable: Boolean = true,
        isLocalModelReady: Boolean = true,
        isLowMemory: Boolean = false
    ): RoutingDecision {
        // Determine task category for analytics logging only (not for routing logic)
        val category = if (imageAttachment != null) AiTaskCategory.VISION_MULTIMODAL else AiTaskCategory.COMPANION_CHAT

        // 1. Multimodal Vision with image attachment -> always requires Cloud
        if (imageAttachment != null) {
            val cloudModelId = modelSelectionEngine.getCloudModels().firstOrNull()?.id
                ?: selectedModelId?.takeIf { modelSelectionEngine.resolveProvider(it) == AiEngineProvider.CLOUD_GEMINI }
                ?: "gemini-2.5-flash"
            return if (isNetworkAvailable) {
                RoutingDecision(
                    taskCategory = AiTaskCategory.VISION_MULTIMODAL,
                    selectedModelId = cloudModelId,
                    isLocalOnDevice = false,
                    routingReason = "Multimodal vision requires Cloud AI engine"
                )
            } else {
                RoutingDecision(
                    taskCategory = AiTaskCategory.VISION_MULTIMODAL,
                    selectedModelId = cloudModelId,
                    isLocalOnDevice = false,
                    routingReason = "Vision task requested but network is disconnected.",
                    executionWarning = "Network is offline. Vision analysis requires cloud connectivity."
                )
            }
        }

        // 2. User explicitly forced 100% Strict On-Device Mode
        if (userRoutingMode == AiRoutingMode.STRICT_ON_DEVICE) {
            val localModelId = modelSelectionEngine.getBestUtilityModelId() ?: ""
            val warning = when {
                !isLocalModelReady -> "Local model weights are not downloaded yet."
                isLowMemory -> "Device is experiencing low-memory pressure; execution may be constrained."
                else -> null
            }
            return RoutingDecision(
                taskCategory = category,
                selectedModelId = localModelId,
                isLocalOnDevice = true,
                routingReason = "Strict On-Device Mode enforced (100% Offline & Private)",
                executionWarning = warning
            )
        }

        // 3. Device is Offline -> always fall back to best local model
        if (!isNetworkAvailable) {
            val localModelId = modelSelectionEngine.getBestUtilityModelId() ?: ""
            return if (isLocalModelReady && !isLowMemory) {
                RoutingDecision(
                    taskCategory = category,
                    selectedModelId = localModelId,
                    isLocalOnDevice = true,
                    routingReason = "Device is offline: Automatically routed to On-Device model",
                    isFailoverTriggered = true
                )
            } else {
                RoutingDecision(
                    taskCategory = category,
                    selectedModelId = localModelId,
                    isLocalOnDevice = true,
                    routingReason = "Device is offline and local model may not be fully ready.",
                    executionWarning = if (!isLocalModelReady) "Offline & local model weights not downloaded" else "Offline & device in low-RAM state"
                )
            }
        }

        // 4. Normal execution: route based on user's selected model
        val resolvedModelId = selectedModelId?.takeIf { it.isNotBlank() }
            ?: modelSelectionEngine.getBestUtilityModelId()
            ?: ""

        val provider = modelSelectionEngine.resolveProvider(resolvedModelId)

        return if (provider == AiEngineProvider.ON_DEVICE_GEMMA) {
            val warning = if (isLowMemory) "Device is experiencing low-memory pressure." else null
            RoutingDecision(
                taskCategory = category,
                selectedModelId = resolvedModelId,
                isLocalOnDevice = true,
                routingReason = "User selected on-device model: $resolvedModelId",
                executionWarning = warning
            )
        } else {
            // Cloud model (CLOUD_GEMINI provider, or unresolved model -> default to cloud)
            RoutingDecision(
                taskCategory = category,
                selectedModelId = resolvedModelId,
                isLocalOnDevice = false,
                routingReason = "User selected cloud model: $resolvedModelId"
            )
        }
    }
}
