package com.example.domain.model
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

enum class PetEmotion(val displayName: String, val glowHex: String) {
    HAPPY("Happy & Cheerful", "#00F0FF"),
    CALM("Calm & Centered", "#06D6A0"),
    ENERGETIC("Energetic & Hyped", "#FFD166"),
    SLEEPY("Sleepy & Cozy", "#B5A6FF"),
    THINKING("Thinking Deeply", "#9D65FF"),
    LOVING("Loving & Empathetic", "#FF70A6"),
    PLAYFUL("Playful & Curious", "#FF5964"),
    CONCERNED("Supportive & Gentle", "#70C1FF")
}

enum class BloubShape(val id: String, val displayName: String, val iconEmoji: String) {
    SPHERE("SPHERE", "Sphere", "🔮"),
    CUBE("CUBE", "Cube", "🧊"),
    CAPSULE("CAPSULE", "Capsule", "💊")
}

enum class BloubSkinColor(
    val id: String,
    val displayName: String,
    val primaryHex: Long,
    val midHex: Long,
    val endHex: Long,
    val glowHex: Long
) {
    ELECTRIC_CYAN("CYAN", "Electric Cyan", 0xFF00F0FF, 0xFF00B4D8, 0xFF0077B6, 0x6600F0FF),
    BUBBLEGUM_PINK("PINK", "Bubblegum Clay", 0xFFFF70A6, 0xFFFF4081, 0xFFE63946, 0x66FF70A6),
    SUNSHINE_GOLD("GOLD", "Sunshine Honey", 0xFFFFD166, 0xFFFFB703, 0xFFFB8500, 0x66FFD166),
    LAVENDER_VIOLET("LAVENDER", "Lavender Velvet", 0xFFB5A6FF, 0xFF9D65FF, 0xFF7209B7, 0x66B5A6FF),
    MINT_JELLY("MINT", "Mint Glaze", 0xFF06D6A0, 0xFF2EC4B6, 0xFF118AB2, 0x6606D6A0),
    MATCHA_CREAM("MATCHA", "Matcha Cream", 0xFFA7C957, 0xFF6A994E, 0xFF386641, 0x66A7C957),
    PEACH_SUNSET("PEACH", "Peach Sunset", 0xFFFF9E7D, 0xFFFF6392, 0xFFD81159, 0x66FF9E7D),
    HOLO_OBSIDIAN("OBSIDIAN", "Dark Holo", 0xFF64DFDF, 0xFF48CAE4, 0xFF3A0CA3, 0x6648CAE4)
}

@Immutable
data class PetStatus(
    val name: String = "Lumi",
    val level: Int = 1,
    val exp: Int = 0,
    val expToNextLevel: Int = 100,
    val bondScore: Int = 50,
    val happiness: Int = 85,
    val energy: Int = 90,
    val personalityTrait: String = "Empathetic Explorer",
    val currentEmotion: PetEmotion = PetEmotion.HAPPY,
    val bloubShape: BloubShape = BloubShape.SPHERE,
    val bloubSkinColor: BloubSkinColor = BloubSkinColor.ELECTRIC_CYAN,
    val isSpeaking: Boolean = false,
    val isListening: Boolean = false,
    val isThinking: Boolean = false,
    val speechBubbleText: String? = "Hey friend! How are you feeling today?",
    val daysTogether: Int = 1,
    val totalInteractions: Int = 0
)

@Immutable
data class ToolExecutionReport(
    val toolName: String,
    val title: String,
    val description: String,
    val isSuccess: Boolean = true,
    val payloadPreview: String = ""
)
