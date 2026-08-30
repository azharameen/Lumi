package com.example.ui.screens.lifehub

fun getMoodEmoji(score: Int): String {
    return when (score) {
        1, 2 -> "😔"
        3, 4 -> "😐"
        5, 6 -> "🙂"
        7, 8 -> "😊"
        else -> "✨"
    }
}

fun getMoodLabel(score: Int): String {
    return when (score) {
        1, 2 -> "Drained"
        3, 4 -> "Meh"
        5, 6 -> "Calm"
        7, 8 -> "Great"
        else -> "Ecstatic"
    }
}
