package com.example.ui.pet.drawers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * Draw Scope extensions for rendering interactive floating particles (hearts, sparkles, stars).
 */
fun DrawScope.drawHeartParticle(size: Float, color: Color) {
    val path = Path().apply {
        moveTo(0f, size * 0.35f)
        cubicTo(-size * 0.5f, -size * 0.2f, -size * 0.5f, size * 0.45f, 0f, size * 0.85f)
        cubicTo(size * 0.5f, size * 0.45f, size * 0.5f, -size * 0.2f, 0f, size * 0.35f)
        close()
    }
    drawPath(path = path, color = color, style = Fill)
}

fun DrawScope.drawSparkleParticle(size: Float, color: Color, rotation: Float) {
    rotate(rotation) {
        val path = Path().apply {
            moveTo(0f, -size)
            quadraticTo(0f, 0f, size, 0f)
            quadraticTo(0f, 0f, 0f, size)
            quadraticTo(0f, 0f, -size, 0f)
            quadraticTo(0f, 0f, 0f, -size)
            close()
        }
        drawPath(path = path, color = color, style = Fill)
    }
}
