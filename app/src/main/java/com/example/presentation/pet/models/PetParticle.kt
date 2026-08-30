package com.example.presentation.pet.models

import androidx.compose.ui.graphics.Color

/**
 * Represents a dynamic floating visual particle (heart, sparkle, star) spawned during interactions.
 */
data class PetParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    val type: String, // HEART, SPARKLE, ZZZ, STAR
    val color: Color,
    val size: Float,
    val rotation: Float = 0f
)
