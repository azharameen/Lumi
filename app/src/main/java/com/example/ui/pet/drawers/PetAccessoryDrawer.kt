package com.example.ui.pet.drawers

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.domain.model.PetAccessory

/**
 * Renders wearable accessories and signature headpieces on top of Lumi's avatar.
 */
fun DrawScope.drawPetAccessory(
    accessory: PetAccessory,
    cx: Float,
    cy: Float,
    baseRadius: Float,
    breathProgress: Float
) {
    when (accessory) {
        PetAccessory.SPROUT -> {
            val topHeadY = cy - baseRadius * 0.95f
            val stemPath = Path().apply {
                moveTo(cx, topHeadY)
                cubicTo(cx + 4f, topHeadY - 18f, cx - 8f, topHeadY - 32f, cx - 2f, topHeadY - 42f)
            }
            drawPath(
                path = stemPath,
                color = Color(0xFF06D6A0),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
            val leafLeft = Path().apply {
                moveTo(cx - 2f, topHeadY - 42f)
                cubicTo(cx - 22f, topHeadY - 52f, cx - 26f, topHeadY - 36f, cx - 2f, topHeadY - 42f)
            }
            drawPath(path = leafLeft, color = Color(0xFF70E000), style = Fill)
            val leafRight = Path().apply {
                moveTo(cx - 2f, topHeadY - 42f)
                cubicTo(cx + 20f, topHeadY - 54f, cx + 24f, topHeadY - 36f, cx - 2f, topHeadY - 42f)
            }
            drawPath(path = leafRight, color = Color(0xFF38B000), style = Fill)
        }

        PetAccessory.GLASSES -> {
            val eyeCenterY = cy - baseRadius * 0.08f
            val eyeDist = baseRadius * 0.34f
            val frameRadius = baseRadius * 0.22f

            drawCircle(
                color = Color(0xFF00F0FF),
                radius = frameRadius,
                center = Offset(cx - eyeDist, eyeCenterY),
                style = Stroke(width = 3.5f)
            )
            drawCircle(
                color = Color(0xFF00F0FF),
                radius = frameRadius,
                center = Offset(cx + eyeDist, eyeCenterY),
                style = Stroke(width = 3.5f)
            )
            drawLine(
                color = Color(0xFF00F0FF),
                start = Offset(cx - eyeDist + frameRadius, eyeCenterY),
                end = Offset(cx + eyeDist - frameRadius, eyeCenterY),
                strokeWidth = 3.5f
            )
        }

        PetAccessory.HEADPHONES -> {
            val topY = cy - baseRadius * 0.88f
            val bandPath = Path().apply {
                moveTo(cx - baseRadius * 0.9f, cy)
                cubicTo(cx - baseRadius * 0.8f, topY - 14f, cx + baseRadius * 0.8f, topY - 14f, cx + baseRadius * 0.9f, cy)
            }
            drawPath(path = bandPath, color = Color(0xFFFFD166), style = Stroke(width = 6f, cap = StrokeCap.Round))

            drawRoundRect(
                color = Color(0xFFFF70A6),
                topLeft = Offset(cx - baseRadius * 1.05f, cy - 22f),
                size = Size(18f, 44f),
                cornerRadius = CornerRadius(9f, 9f)
            )
            drawRoundRect(
                color = Color(0xFFFF70A6),
                topLeft = Offset(cx + baseRadius * 0.92f, cy - 22f),
                size = Size(18f, 44f),
                cornerRadius = CornerRadius(9f, 9f)
            )
        }

        PetAccessory.HALO -> {
            val haloY = cy - baseRadius * 1.25f - (breathProgress * 6f)
            drawOval(
                brush = Brush.linearGradient(listOf(Color(0xFFFFD166), Color(0xFFFFF07C), Color(0xFFFFD166))),
                topLeft = Offset(cx - baseRadius * 0.65f, haloY),
                size = Size(baseRadius * 1.3f, 22f),
                style = Stroke(width = 5.5f)
            )
        }

        PetAccessory.CROWN -> {
            val crownY = cy - baseRadius * 0.96f
            val crownWidth = baseRadius * 0.6f
            val crownPath = Path().apply {
                moveTo(cx - crownWidth / 2, crownY)
                lineTo(cx - crownWidth / 2 - 6f, crownY - 26f)
                lineTo(cx - crownWidth / 5, crownY - 12f)
                lineTo(cx, crownY - 36f)
                lineTo(cx + crownWidth / 5, crownY - 12f)
                lineTo(cx + crownWidth / 2 + 6f, crownY - 26f)
                lineTo(cx + crownWidth / 2, crownY)
                close()
            }
            drawPath(path = crownPath, color = Color(0xFFFFD166), style = Fill)
            drawPath(path = crownPath, color = Color(0xFFFFAA00), style = Stroke(width = 2.5f))
            drawCircle(Color(0xFFFF70A6), radius = 3.5f, center = Offset(cx, crownY - 36f))
            drawCircle(Color(0xFF00F0FF), radius = 3f, center = Offset(cx - crownWidth / 2 - 6f, crownY - 26f))
            drawCircle(Color(0xFF00F0FF), radius = 3f, center = Offset(cx + crownWidth / 2 + 6f, crownY - 26f))
        }

        PetAccessory.NONE -> {
            // Signature adorable antenna / starlight glow from app icon
            val topHeadY = cy - baseRadius * 0.95f
            val sway = (breathProgress - 0.5f) * 6f
            val stemPath = Path().apply {
                moveTo(cx, topHeadY)
                cubicTo(
                    cx + baseRadius * 0.1f + sway,
                    topHeadY - baseRadius * 0.18f,
                    cx + baseRadius * 0.22f + sway * 1.5f,
                    topHeadY - baseRadius * 0.32f,
                    cx + baseRadius * 0.18f + sway * 2f,
                    topHeadY - baseRadius * 0.42f
                )
            }
            drawPath(
                path = stemPath,
                color = Color(0xFFFFD166),
                style = Stroke(width = (baseRadius * 0.055f).coerceAtLeast(2.5f), cap = StrokeCap.Round)
            )

            val tipX = cx + baseRadius * 0.18f + sway * 2f
            val tipY = topHeadY - baseRadius * 0.42f
            val bulbRadius = (baseRadius * 0.11f).coerceAtLeast(4f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFDF0),
                        Color(0xAAFFD166),
                        Color.Transparent
                    ),
                    center = Offset(tipX, tipY),
                    radius = bulbRadius * 2.6f
                ),
                radius = bulbRadius * 2.6f,
                center = Offset(tipX, tipY)
            )

            drawCircle(Color(0xFFFFD166), radius = bulbRadius, center = Offset(tipX, tipY))
            drawCircle(Color.White, radius = bulbRadius * 0.5f, center = Offset(tipX - 1.2f, tipY - 1.2f))
        }
    }
}
