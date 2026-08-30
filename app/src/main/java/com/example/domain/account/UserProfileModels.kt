package com.example.domain.account
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.core.theme.LumiCyan
import com.example.core.theme.LumiGold
import com.example.core.theme.LumiGreen
import com.example.core.theme.LumiMint
import com.example.core.theme.LumiPink
import com.example.core.theme.LumiViolet
import com.example.core.theme.LumiYellow

enum class LumiPersonaTone(
    val title: String,
    val description: String,
    val promptInstruction: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    EMPATHETIC_CHEERFUL(
        title = "Empathetic & Warm",
        description = "Supportive, kind, emotionally intelligent, and encouraging friend.",
        promptInstruction = "Speak with warmth, gentle encouragement, empathy, and positive optimism. Always validate emotions before giving advice.",
        icon = Icons.Default.SentimentSatisfiedAlt,
        accentColor = LumiPink
    ),
    INTELLECTUAL_TUTOR(
        title = "Deep Intellectual",
        description = "Analytical, insightful, articulate, and academically grounded guide.",
        promptInstruction = "Provide deep, structured, precise, and intellectually rigorous answers with logical breakdowns and first-principles reasoning.",
        icon = Icons.Default.Psychology,
        accentColor = LumiViolet
    ),
    DIRECT_COACH(
        title = "High-Performance Coach",
        description = "Direct, disciplined, motivating, and action-oriented mentor.",
        promptInstruction = "Be concise, direct, focused on accountability, action items, and unblocking productivity without unnecessary fluff.",
        icon = Icons.Default.FitnessCenter,
        accentColor = LumiGold
    ),
    ZEN_MINIMALIST(
        title = "Zen Mindfulness",
        description = "Calm, concise, peaceful, and grounded in tranquility.",
        promptInstruction = "Respond with poetic calm, tranquility, and grounded brevity. Encourage pausing, deep breaths, and single-tasking focus.",
        icon = Icons.Default.SelfImprovement,
        accentColor = LumiMint
    ),
    WITTY_COMPANION(
        title = "Playful & Witty",
        description = "Fun, witty, playful, and humorously charming pal.",
        promptInstruction = "Keep dialogue lighthearted, clever, playful with gentle banter, witty humor, and enthusiastic curiosity.",
        icon = Icons.Default.AutoAwesome,
        accentColor = LumiCyan
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
