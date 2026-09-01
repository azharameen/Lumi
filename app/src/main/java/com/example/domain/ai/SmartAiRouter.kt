package com.example.domain.ai


import com.example.data.remote.AiRoutingMode

data class RoutingDecision(
    val taskCategory: AiTaskCategory,
    val selectedModel: ModelSpec,
    val isLocalOnDevice: Boolean,
    val routingReason: String,
    val fallbackModel: ModelSpec = AiModelRegistry.GEMMA_2B_INT4,
    val isFailoverTriggered: Boolean = false,
    val executionWarning: String? = null
)

/**
 * Intelligent Multi-Tier AI Routing Engine.
 * Dynamically routes user intent based on:
 * - Model download and artifact readiness
 * - Device RAM headroom and low-memory pressure
 * - Network connectivity state
 * - Privacy sensitivity and task modality
 */
object SmartAiRouter {

    fun routeRequest(
        userMessage: String,
        imageAttachment: ByteArray?,
        userRoutingMode: AiRoutingMode,
        isNetworkAvailable: Boolean = true,
        isLocalModelReady: Boolean = true,
        isLowMemory: Boolean = false,
        isLowBattery: Boolean = false
    ): RoutingDecision {
        val category = classifyTask(userMessage, imageAttachment)

        // 1. Multimodal Vision & Images -> Always requires Cloud Vision Encoder
        if (imageAttachment != null || category == AiTaskCategory.VISION_MULTIMODAL) {
            return if (isNetworkAvailable) {
                RoutingDecision(
                    taskCategory = AiTaskCategory.VISION_MULTIMODAL,
                    selectedModel = AiModelRegistry.GEMINI_2_5_FLASH,
                    isLocalOnDevice = false,
                    routingReason = "Multimodal camera / visual inspection: Routed to Cloud Gemini 2.5 Flash"
                )
            } else {
                RoutingDecision(
                    taskCategory = AiTaskCategory.VISION_MULTIMODAL,
                    selectedModel = AiModelRegistry.GEMINI_2_5_FLASH,
                    isLocalOnDevice = false,
                    routingReason = "Vision task requested but network is disconnected.",
                    executionWarning = "Network is offline. Vision analysis requires cloud connectivity."
                )
            }
        }

        // 2. User explicitly forced 100% Strict On-Device Mode
        if (userRoutingMode == AiRoutingMode.STRICT_ON_DEVICE) {
            val warning = when {
                !isLocalModelReady -> "Local model weights are not downloaded yet."
                isLowMemory -> "Device is experiencing low-memory pressure; execution may be constrained."
                else -> null
            }
            return RoutingDecision(
                taskCategory = category,
                selectedModel = AiModelRegistry.GEMMA_2B_INT4,
                isLocalOnDevice = true,
                routingReason = "Strict On-Device Mode enforced (100% Offline & Private)",
                executionWarning = warning
            )
        }

        // 3. Device is Offline (Network disconnected)
        if (!isNetworkAvailable) {
            return if (isLocalModelReady && !isLowMemory) {
                RoutingDecision(
                    taskCategory = category,
                    selectedModel = AiModelRegistry.GEMMA_2B_INT4,
                    isLocalOnDevice = true,
                    routingReason = "Device is offline: Automatically routed to On-Device Gemma",
                    isFailoverTriggered = true
                )
            } else {
                RoutingDecision(
                    taskCategory = category,
                    selectedModel = AiModelRegistry.GEMMA_2B_INT4,
                    isLocalOnDevice = true,
                    routingReason = "Device is offline and local model is not ready.",
                    executionWarning = if (!isLocalModelReady) "Offline & local model weights not downloaded" else "Offline & device in low-RAM state"
                )
            }
        }

        // 4. User explicitly selected Cloud Turbo Mode
        if (userRoutingMode == AiRoutingMode.CLOUD_TURBO) {
            val model = if (category == AiTaskCategory.DEEP_REASONING) {
                AiModelRegistry.GEMINI_3_1_PRO
            } else {
                AiModelRegistry.GEMINI_2_5_FLASH
            }
            return RoutingDecision(
                taskCategory = category,
                selectedModel = model,
                isLocalOnDevice = false,
                routingReason = "Cloud Turbo mode: Routed to ${model.displayName} for maximum capability"
            )
        }

        // 5. Smart Hybrid Auto-Routing based on Modality, Privacy, and System Health
        val prefersLocal = category == AiTaskCategory.WELLNESS_MOOD ||
                category == AiTaskCategory.COMPANION_CHAT ||
                category == AiTaskCategory.QUICK_DEVICE_ACTION ||
                category == AiTaskCategory.BENCHMARK_TEST

        if (prefersLocal) {
            if (isLocalModelReady && !isLowMemory) {
                val reason = when (category) {
                    AiTaskCategory.WELLNESS_MOOD -> "Privacy Protection: Personal wellness processed 100% on-device"
                    AiTaskCategory.COMPANION_CHAT -> "Ultra-Fast Latency: Companion chat executed on local GPU"
                    AiTaskCategory.QUICK_DEVICE_ACTION -> "Local Tool Execution: Fast deterministic action on-device"
                    AiTaskCategory.BENCHMARK_TEST -> "Hardware Benchmark: On-device GPU inference test"
                    else -> "Local execution preferred"
                }
                return RoutingDecision(
                    taskCategory = category,
                    selectedModel = AiModelRegistry.GEMMA_2B_INT4,
                    isLocalOnDevice = true,
                    routingReason = reason
                )
            } else {
                // Failover to Cloud Gemini due to missing local weights or Low RAM
                val failoverReason = if (!isLocalModelReady) {
                    "Local weights not downloaded yet; auto-failing over to Cloud Gemini 2.5 Flash"
                } else {
                    "Device is low on memory; failover to Cloud Gemini 2.5 Flash to prevent OOM"
                }
                return RoutingDecision(
                    taskCategory = category,
                    selectedModel = AiModelRegistry.GEMINI_2_5_FLASH,
                    isLocalOnDevice = false,
                    routingReason = failoverReason,
                    isFailoverTriggered = true
                )
            }
        } else {
            // Complex reasoning / deep timeline tasks prefer Cloud Gemini
            val cloudModel = if (category == AiTaskCategory.DEEP_REASONING) {
                AiModelRegistry.GEMINI_3_1_PRO
            } else {
                AiModelRegistry.GEMINI_2_5_FLASH
            }
            val reason = if (category == AiTaskCategory.DEEP_REASONING) {
                "Advanced Reasoning: Multi-step problem solving routed to Gemini 3.1 Pro"
            } else {
                "Timeline Optimization: Multi-constraint schedule planning routed to Gemini 2.5 Flash"
            }
            return RoutingDecision(
                taskCategory = category,
                selectedModel = cloudModel,
                isLocalOnDevice = false,
                routingReason = reason
            )
        }
    }

    fun classifyTask(message: String, image: ByteArray?): AiTaskCategory {
        if (image != null) return AiTaskCategory.VISION_MULTIMODAL

        // Quick heuristic overrides for obvious tool actions
        val lower = message.lowercase().trim()
        if (lower.startsWith("remind me") || lower.startsWith("add task") || lower.startsWith("set a timer")) {
            return AiTaskCategory.QUICK_DEVICE_ACTION
        }

        // Fast Semantic NLP Math (TF-IDF Cosine Similarity)
        val isLocal = SemanticIntentClassifier.isLocalIntent(message)

        return if (isLocal) {
            // Local paradigms
            if (lower.contains("stress") || lower.contains("anxious") || lower.contains("feel")) {
                AiTaskCategory.WELLNESS_MOOD
            } else {
                AiTaskCategory.COMPANION_CHAT
            }
        } else {
            // Cloud paradigms
            if (lower.contains("optimize") && lower.contains("schedule")) {
                AiTaskCategory.TIMELINE_PLANNING
            } else {
                AiTaskCategory.DEEP_REASONING
            }
        }
    }
}

