package com.example.domain.ai

/**
 * Categorizes the type of AI task for analytics, logging, and routing hints.
 * Routing decisions are now model-provider-driven, not category-driven.
 */
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

/** Identifies which inference engine handles a given model. */
enum class AiEngineProvider(val displayName: String) {
    ON_DEVICE_GEMMA("On-Device Gemma (MediaPipe / TFLite)"),
    CLOUD_GEMINI("Google Cloud Gemini API")
}
