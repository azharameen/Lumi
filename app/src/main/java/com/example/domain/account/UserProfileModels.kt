package com.example.domain.account
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable


enum class LumiPersonaTone(
    val title: String,
    val description: String,
    val promptInstruction: String,
    
    
) {
    EMPATHETIC_CHEERFUL(
        title = "Empathetic & Warm",
        description = "Supportive, kind, emotionally intelligent, and encouraging friend.",
        promptInstruction = "Speak with warmth, gentle encouragement, empathy, and positive optimism. Always validate emotions before giving advice.",
        
        
    ),
    INTELLECTUAL_TUTOR(
        title = "Deep Intellectual",
        description = "Analytical, insightful, articulate, and academically grounded guide.",
        promptInstruction = "Provide deep, structured, precise, and intellectually rigorous answers with logical breakdowns and first-principles reasoning.",
        
        
    ),
    DIRECT_COACH(
        title = "High-Performance Coach",
        description = "Direct, disciplined, motivating, and action-oriented mentor.",
        promptInstruction = "Be concise, direct, focused on accountability, action items, and unblocking productivity without unnecessary fluff.",
        
        
    ),
    ZEN_MINIMALIST(
        title = "Zen Mindfulness",
        description = "Calm, concise, peaceful, and grounded in tranquility.",
        promptInstruction = "Respond with poetic calm, tranquility, and grounded brevity. Encourage pausing, deep breaths, and single-tasking focus.",
        
        
    ),
    WITTY_COMPANION(
        title = "Playful & Witty",
        description = "Fun, witty, playful, and humorously charming pal.",
        promptInstruction = "Keep dialogue lighthearted, clever, playful with gentle banter, witty humor, and enthusiastic curiosity.",
        
        
    )
}

@Immutable
data class UserProfileData(
    val userName: String = "Azhar Ameen",
    val userEmail: String = "azharameen52@gmail.com",
    val roleOrTitle: String = "Software Engineer & AI Architect",
    val primaryFocusGoal: String = "Deep Flow, Clean Code & Mindful Living",
    val dailyFocusTargetHours: Float = 4.0f,
    val targetHydrationCups: Int = 8,
    val targetDailySteps: Int = 8000,
    val wakeUpTime: String = "07:30 AM",
    val sleepTime: String = "11:30 PM",
    val personaTone: LumiPersonaTone = LumiPersonaTone.EMPATHETIC_CHEERFUL,
    val customAiInstructions: String = "Keep answers concise, actionable, and formatted in clean markdown bullet points.",
    val geminiModelChoice: String = "gemini-2.5-flash",
    val temperature: Float = 0.7f,
    val enableProactiveBriefings: Boolean = true,
    val enableToolCalling: Boolean = true,
    val enableBiometricLock: Boolean = false,
    val enableSpeechOutput: Boolean = true,
    val enableHapticFeedback: Boolean = true,
    val enableAmbientLocation: Boolean = true,
    val enableLocalAiFallback: Boolean = true,
    val enableOverlay: Boolean = true,
    val hasCompletedOnboarding: Boolean = false
)

@Immutable
data class UserFactItem(
    val id: String,
    val category: String, // e.g. "Work", "Preferences", "Health", "Routines", "Personal"
    val factText: String,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
