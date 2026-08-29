package com.example.domain.ai

enum class AiTaskCategory(
    val displayName: String,
    val description: String,
    val isPrivacySensitive: Boolean
) {
    COMPANION_CHAT(
        displayName = "Companion Chat & Banter",
        description = "Casual dialogue, greetings, encouragement, jokes",
        isPrivacySensitive = false
    ),
    WELLNESS_MOOD(
        displayName = "Private Wellness & Mood",
        description = "Sensitive mood reflections, stress venting, hydration & breath logging",
        isPrivacySensitive = true
    ),
    QUICK_DEVICE_ACTION(
        displayName = "Quick Local Action",
        description = "Direct task creation, schedule block insertion, local pet interactions",
        isPrivacySensitive = true
    ),
    VISION_MULTIMODAL(
        displayName = "Multimodal Vision",
        description = "Analyzing camera photos, screenshots, and visual assets",
        isPrivacySensitive = false
    ),
    DEEP_REASONING(
        displayName = "Deep Reasoning & Tutoring",
        description = "Complex multi-step problems, study tutoring, code synthesis, math",
        isPrivacySensitive = false
    ),
    TIMELINE_PLANNING(
        displayName = "Complex Schedule Planning",
        description = "Multi-day agenda optimization, conflict resolution, calendar restructuring",
        isPrivacySensitive = false
    ),
    BENCHMARK_TEST(
        displayName = "Engine Benchmark",
        description = "Throughput and latency stress testing",
        isPrivacySensitive = false
    )
}

enum class AiEngineProvider(val displayName: String) {
    ON_DEVICE_GEMMA("On-Device Gemma (MediaPipe / TFLite)"),
    CLOUD_GEMINI("Google Cloud Gemini API")
}

data class ModelSpec(
    val id: String,
    val displayName: String,
    val provider: AiEngineProvider,
    val isOfflineCapable: Boolean,
    val inputCostPerMillionTokensUsd: Double,
    val outputCostPerMillionTokensUsd: Double,
    val typicalLatencyMs: Int,
    val contextWindowTokens: Int,
    val hardwareTarget: String,
    val description: String
)

object AiModelRegistry {
    val GEMMA_2B_INT4 = ModelSpec(
        id = "gemma-2b-it-int4",
        displayName = "Gemma 2B IT (INT4)",
        provider = AiEngineProvider.ON_DEVICE_GEMMA,
        isOfflineCapable = true,
        inputCostPerMillionTokensUsd = 0.0,
        outputCostPerMillionTokensUsd = 0.0,
        typicalLatencyMs = 120,
        contextWindowTokens = 2048,
        hardwareTarget = "GPU (OpenCL / Vulkan) / ARM NPU",
        description = "Ultra-fast, zero-cost, 100% private on-device model for casual chat and wellness"
    )

    val GEMINI_2_5_FLASH = ModelSpec(
        id = "gemini-2.5-flash",
        displayName = "Gemini 2.5 Flash",
        provider = AiEngineProvider.CLOUD_GEMINI,
        isOfflineCapable = false,
        inputCostPerMillionTokensUsd = 0.075,
        outputCostPerMillionTokensUsd = 0.30,
        typicalLatencyMs = 650,
        contextWindowTokens = 1000000,
        hardwareTarget = "Cloud TPU v5e (Google Data Centers)",
        description = "Fast, multimodal vision, tool-calling intelligence for broad tasks"
    )

    val GEMINI_3_1_PRO = ModelSpec(
        id = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro Preview",
        provider = AiEngineProvider.CLOUD_GEMINI,
        isOfflineCapable = false,
        inputCostPerMillionTokensUsd = 1.25,
        outputCostPerMillionTokensUsd = 5.00,
        typicalLatencyMs = 1400,
        contextWindowTokens = 2000000,
        hardwareTarget = "Cloud TPU v5p (Google Data Centers)",
        description = "State-of-the-art reasoning for complex STEM, coding, and multi-constraint planning"
    )

    val ALL_MODELS = listOf(GEMMA_2B_INT4, GEMINI_2_5_FLASH, GEMINI_3_1_PRO)
}
