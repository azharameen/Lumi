package com.example.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.core.theme.*
import com.example.domain.account.LumiPersonaTone

val LumiPersonaTone.icon: ImageVector
    get() = when (this) {
        LumiPersonaTone.EMPATHETIC_CHEERFUL -> Icons.Default.SentimentSatisfiedAlt
        LumiPersonaTone.INTELLECTUAL_TUTOR -> Icons.Default.Psychology
        LumiPersonaTone.DIRECT_COACH -> Icons.Default.FitnessCenter
        LumiPersonaTone.ZEN_MINIMALIST -> Icons.Default.SelfImprovement
        LumiPersonaTone.WITTY_COMPANION -> Icons.Default.AutoAwesome
    }

val LumiPersonaTone.accentColor: Color
    get() = when (this) {
        LumiPersonaTone.EMPATHETIC_CHEERFUL -> LumiPink
        LumiPersonaTone.INTELLECTUAL_TUTOR -> LumiViolet
        LumiPersonaTone.DIRECT_COACH -> LumiGold
        LumiPersonaTone.ZEN_MINIMALIST -> LumiMint
        LumiPersonaTone.WITTY_COMPANION -> LumiCyan
    }
