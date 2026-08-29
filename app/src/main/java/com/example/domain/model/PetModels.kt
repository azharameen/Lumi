package com.example.domain.model

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

enum class PetAccessory(val id: String, val displayName: String, val requiredLevel: Int, val description: String) {
    NONE("NONE", "Natural Lumi", 1, "Pure ethereal companion"),
    SPROUT("SPROUT", "Sprout of Life", 1, "A tiny green seedling glowing with hope"),
    GLASSES("GLASSES", "Wisdom Specs", 2, "Chic glowing glasses for deep scheduling thoughts"),
    HEADPHONES("HEADPHONES", "Cozy Headphones", 3, "For jamming to relaxing lofi frequencies"),
    HALO("HALO", "Angel Halo", 4, "A radiant circle of empathetic warmth"),
    CROWN("CROWN", "Starlight Crown", 5, "Unlocked by master companions who care daily")
}

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
    val activeAccessory: PetAccessory = PetAccessory.SPROUT,
    val isSpeaking: Boolean = false,
    val isListening: Boolean = false,
    val isThinking: Boolean = false,
    val speechBubbleText: String? = "Hey friend! How are you feeling today?",
    val daysTogether: Int = 1,
    val totalInteractions: Int = 0
)

data class ToolExecutionReport(
    val toolName: String,
    val title: String,
    val description: String,
    val isSuccess: Boolean = true,
    val payloadPreview: String = ""
)
