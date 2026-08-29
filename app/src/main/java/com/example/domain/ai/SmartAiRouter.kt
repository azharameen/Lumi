package com.example.domain.ai

import android.graphics.Bitmap
import com.example.data.remote.AiRoutingMode

data class RoutingDecision(
    val taskCategory: AiTaskCategory,
    val selectedModel: ModelSpec,
    val isLocalOnDevice: Boolean,
    val routingReason: String,
    val fallbackModel: ModelSpec = AiModelRegistry.GEMMA_2B_INT4
)

object SmartAiRouter {

    fun routeRequest(
        userMessage: String,
        imageAttachment: Bitmap?,
        userRoutingMode: AiRoutingMode,
        isNetworkAvailable: Boolean = true,
        isLowBattery: Boolean = false
    ): RoutingDecision {
        val category = classifyTask(userMessage, imageAttachment)

        // Rule 1: User explicitly forced 100% Strict On-Device Mode
        if (userRoutingMode == AiRoutingMode.STRICT_ON_DEVICE) {
            return RoutingDecision(
                taskCategory = category,
                selectedModel = AiModelRegistry.GEMMA_2B_INT4,
                isLocalOnDevice = true,
                routingReason = "User selected Strict On-Device Mode (100% Offline & Private)"
            )
        }

        // Rule 2: Device is Offline or Network is unavailable
        if (!isNetworkAvailable) {
            return RoutingDecision(
                taskCategory = category,
                selectedModel = AiModelRegistry.GEMMA_2B_INT4,
                isLocalOnDevice = true,
                routingReason = "Device is offline / Airplane mode: Routed to On-Device Gemma"
            )
        }

        // Rule 3: Multimodal Vision & Images -> Always Cloud Gemini Flash (requires heavy vision encoder)
        if (imageAttachment != null || category == AiTaskCategory.VISION_MULTIMODAL) {
            return RoutingDecision(
                taskCategory = AiTaskCategory.VISION_MULTIMODAL,
                selectedModel = AiModelRegistry.GEMINI_2_5_FLASH,
                isLocalOnDevice = false,
                routingReason = "Multimodal camera / visual inspection: Routed to Cloud Gemini 2.5 Flash"
            )
        }

        // Rule 4: User explicitly selected Cloud Turbo Mode
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

        // Rule 5: Smart Hybrid Auto-Routing based on Task Characteristics
        return when (category) {
            // Highly sensitive personal mood & journaling -> Local Gemma (Zero data leaves device)
            AiTaskCategory.WELLNESS_MOOD -> {
                RoutingDecision(
                    taskCategory = category,
                    selectedModel = AiModelRegistry.GEMMA_2B_INT4,
                    isLocalOnDevice = true,
                    routingReason = "Privacy Protection: Personal wellness & mood reflection processed 100% on-device"
                )
            }

            // Quick casual banter and companion dialogue -> Local Gemma (Ultra-low latency, zero cloud cost)
            AiTaskCategory.COMPANION_CHAT -> {
                RoutingDecision(
                    taskCategory = category,
                    selectedModel = AiModelRegistry.GEMMA_2B_INT4,
                    isLocalOnDevice = true,
                    routingReason = "Ultra-Fast Latency: Casual companion chat executed on local GPU in ~120ms"
                )
            }

            // Quick single-intent local actions (water logging, alarms, quick task) -> Local Gemma
            AiTaskCategory.QUICK_DEVICE_ACTION -> {
                RoutingDecision(
                    taskCategory = category,
                    selectedModel = AiModelRegistry.GEMMA_2B_INT4,
                    isLocalOnDevice = true,
                    routingReason = "Local Tool Execution: Fast deterministic action parsed by on-device model"
                )
            }

            // Complex STEM, coding, advanced tutoring -> Cloud Gemini Pro / Flash
            AiTaskCategory.DEEP_REASONING -> {
                RoutingDecision(
                    taskCategory = category,
                    selectedModel = AiModelRegistry.GEMINI_3_1_PRO,
                    isLocalOnDevice = false,
                    routingReason = "Advanced Reasoning: Complex multi-step problem solving routed to Gemini 3.1 Pro"
                )
            }

            // Complex multi-constraint scheduling -> Cloud Gemini Flash
            AiTaskCategory.TIMELINE_PLANNING -> {
                RoutingDecision(
                    taskCategory = category,
                    selectedModel = AiModelRegistry.GEMINI_2_5_FLASH,
                    isLocalOnDevice = false,
                    routingReason = "Timeline Optimization: Multi-constraint schedule planning routed to Gemini 2.5 Flash"
                )
            }

            AiTaskCategory.BENCHMARK_TEST -> {
                RoutingDecision(
                    taskCategory = category,
                    selectedModel = AiModelRegistry.GEMMA_2B_INT4,
                    isLocalOnDevice = true,
                    routingReason = "Hardware Benchmark: On-device GPU inference test"
                )
            }

            else -> {
                RoutingDecision(
                    taskCategory = category,
                    selectedModel = AiModelRegistry.GEMINI_2_5_FLASH,
                    isLocalOnDevice = false,
                    routingReason = "General Assistant Query: Handled by Cloud Gemini 2.5 Flash"
                )
            }
        }
    }

    fun classifyTask(message: String, image: Bitmap?): AiTaskCategory {
        if (image != null) return AiTaskCategory.VISION_MULTIMODAL
        val lower = message.lowercase().trim()

        return when {
            // Deep Reasoning keywords
            lower.contains("explain code") || lower.contains("debug") || lower.contains("algorithm") ||
            lower.contains("solve") || lower.contains("calculate") || lower.contains("math problem") ||
            lower.contains("essay") || lower.contains("physics") || lower.contains("chemistry") -> {
                AiTaskCategory.DEEP_REASONING
            }

            // Complex Schedule Planning keywords
            (lower.contains("optimize") || lower.contains("reorganize") || lower.contains("conflict") || lower.contains("reschedule")) &&
            (lower.contains("calendar") || lower.contains("week") || lower.contains("schedule")) -> {
                AiTaskCategory.TIMELINE_PLANNING
            }

            // Wellness & Mood keywords
            lower.contains("stress") || lower.contains("anxious") || lower.contains("depressed") ||
            lower.contains("sad") || lower.contains("tired") || lower.contains("burnout") ||
            lower.contains("journal") || lower.contains("hydrate") || lower.contains("drink water") ||
            lower.contains("breathe") || lower.contains("meditat") || lower.contains("feeling") -> {
                AiTaskCategory.WELLNESS_MOOD
            }

            // Quick Device Actions
            lower.startsWith("add task") || lower.startsWith("todo") || lower.startsWith("remind me") ||
            lower.startsWith("create task") || lower.startsWith("schedule meeting") || lower.startsWith("buy ") -> {
                AiTaskCategory.QUICK_DEVICE_ACTION
            }

            // Companion Chat & Banter
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ||
            lower.contains("how are you") || lower.contains("joke") || lower.contains("pet") ||
            lower.contains("who are you") || lower.contains("lumi") || lower.contains("love") ||
            lower.contains("cute") || lower.contains("thank") -> {
                AiTaskCategory.COMPANION_CHAT
            }

            // Longer multi-sentence prompts default to deep reasoning / general
            message.length > 180 -> AiTaskCategory.DEEP_REASONING

            else -> AiTaskCategory.COMPANION_CHAT
        }
    }
}
