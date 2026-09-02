package com.example.domain.onboarding

data class RecommendedDownload(
    val modelId: String,
    val displayName: String,
    val sizeDisplay: String,
    val description: String,
    val isRecommended: Boolean,
    val isZeroDownload: Boolean = false
)

class ModelRecommendationEngine {

    fun getRecommendations(deviceProfile: DeviceProfile): List<RecommendedDownload> {
        val list = mutableListOf<RecommendedDownload>()

        if (deviceProfile.isAiCoreAvailable) {
            list.add(
                RecommendedDownload(
                    modelId = "gemini-nano",
                    displayName = "Gemini Nano (Android AICore)",
                    sizeDisplay = "0 MB (Built into OS)",
                    description = "Hardware-accelerated NPU zero-shot model built into device.",
                    isRecommended = true,
                    isZeroDownload = true
                )
            )
        }

        val totalRamGb = deviceProfile.totalRamBytes / (1024 * 1024 * 1024)
        if (totalRamGb >= 4) {
            list.add(
                RecommendedDownload(
                    modelId = "gemma-2b-it-gpu-int4",
                    displayName = "Gemma 2B IT (GPU INT4)",
                    sizeDisplay = "1.34 GB",
                    description = "High-performance GPU quantized model for offline private execution.",
                    isRecommended = !deviceProfile.isAiCoreAvailable
                )
            )
        } else if (totalRamGb >= 3) {
            list.add(
                RecommendedDownload(
                    modelId = "gemma-2b-it-cpu-int4",
                    displayName = "Gemma 2B IT (CPU INT4)",
                    sizeDisplay = "1.34 GB",
                    description = "CPU multi-threaded offline execution.",
                    isRecommended = !deviceProfile.isAiCoreAvailable
                )
            )
        }

        list.add(
            RecommendedDownload(
                modelId = "universal-sentence-encoder",
                displayName = "MediaPipe TextEmbedder",
                sizeDisplay = "25 MB",
                description = "Dense vector embedding model for fast semantic memory search.",
                isRecommended = true
            )
        )

        return list
    }
}
