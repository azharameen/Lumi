package com.example.ui.pet.drawers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * Normalized DrawScope extensions for rendering interactive floating particles
 * (hearts, sparkles, stars). All vectors, curves, and angles scale strictly relative to size.
 */
fun DrawScope.drawHeartParticle(size: Float, color: Color) {
    val path = Path().apply {
        val half = size * 0.5f
        moveTo(0f, size * 0.32f)
        cubicTo(
            -half * 1.05f, -half * 0.45f,
            -half * 1.05f, half * 0.85f,
            0f, size * 0.95f
        )
        cubicTo(
            half * 1.05f, half * 0.85f,
            half * 1.05f, -half * 0.45f,
            0f, size * 0.32f
        )
        close()
    }
    drawPath(path = path, color = color, style = Fill)
}

fun DrawScope.drawSparkleParticle(size: Float, color: Color, rotation: Float) {
    rotate(rotation) {
        val path = Path().apply {
            val half = size * 0.5f
            moveTo(0f, -half)
            quadraticTo(0f, 0f, half, 0f)
            quadraticTo(0f, 0f, 0f, half)
            quadraticTo(0f, 0f, -half, 0f)
            quadraticTo(0f, 0f, 0f, -half)
            close()
        }
        drawPath(path = path, color = color, style = Fill)
    }
}

fun DrawScope.drawStarParticle(size: Float, color: Color, rotation: Float) {
    rotate(rotation) {
        val path = Path().apply {
            val outer = size * 0.5f
            val inner = outer * 0.42f
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outer else inner
                val angle = (i * Math.PI / 5.0).toFloat()
                val x = (r * kotlin.math.cos(angle))
                val y = (r * kotlin.math.sin(angle))
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(path = path, color = color, style = Fill)
    }
}
