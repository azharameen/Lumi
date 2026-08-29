package com.example.ui.pet.drawers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.domain.model.PetEmotion

/**
 * Renders Lumi's expressive eyes, gaze tracking, lip sync, and emotional facial states.
 */
fun DrawScope.drawPetEye(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    isBlinking: Boolean,
    gazeX: Float,
    gazeY: Float,
    isThinking: Boolean
) {
    if (isBlinking) {
        drawArc(
            color = Color(0xFF140D26),
            startAngle = 10f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(center.x - radiusX, center.y - 4f),
            size = Size(radiusX * 2, 8f),
            style = Stroke(width = 3.2f, cap = StrokeCap.Round)
        )
    } else {
        drawOval(
            color = Color(0xFF130D28),
            topLeft = Offset(center.x - radiusX, center.y - radiusY),
            size = Size(radiusX * 2, radiusY * 2)
        )

        val gazeOffsetMax = radiusX * 0.38f
        val pupilX = center.x + (gazeX * gazeOffsetMax)
        val pupilY = center.y + (gazeY * gazeOffsetMax)

        drawCircle(
            color = Color.White,
            radius = radiusX * 0.42f,
            center = Offset(pupilX - radiusX * 0.2f, pupilY - radiusY * 0.25f)
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = radiusX * 0.2f,
            center = Offset(pupilX + radiusX * 0.28f, pupilY + radiusY * 0.3f)
        )
    }
}

fun DrawScope.drawPetMouth(
    center: Offset,
    emotion: PetEmotion,
    isSpeaking: Boolean,
    talkAmount: Float,
    baseRadius: Float
) {
    val mouthWidth = baseRadius * 0.22f

    if (isSpeaking) {
        val openHeight = (baseRadius * 0.12f) * (0.4f + talkAmount * 0.6f)
        drawOval(
            color = Color(0xFF140D26),
            topLeft = Offset(center.x - mouthWidth / 2, center.y - openHeight / 2),
            size = Size(mouthWidth, openHeight)
        )
        drawOval(
            color = Color(0xFFFF5964),
            topLeft = Offset(center.x - mouthWidth * 0.3f, center.y),
            size = Size(mouthWidth * 0.6f, openHeight * 0.5f)
        )
    } else {
        when (emotion) {
            PetEmotion.HAPPY, PetEmotion.LOVING, PetEmotion.PLAYFUL, PetEmotion.ENERGETIC -> {
                val path = Path().apply {
                    moveTo(center.x - mouthWidth, center.y)
                    quadraticTo(center.x, center.y + baseRadius * 0.14f, center.x + mouthWidth, center.y)
                }
                drawPath(
                    path = path,
                    color = Color(0xFF140D26),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
            }
            PetEmotion.CALM -> {
                val path = Path().apply {
                    moveTo(center.x - mouthWidth * 0.7f, center.y)
                    quadraticTo(center.x, center.y + baseRadius * 0.08f, center.x + mouthWidth * 0.7f, center.y)
                }
                drawPath(
                    path = path,
                    color = Color(0xFF140D26),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
            }
            PetEmotion.THINKING -> {
                drawCircle(
                    color = Color(0xFF140D26),
                    radius = baseRadius * 0.06f,
                    center = center,
                    style = Stroke(width = 3f)
                )
            }
            PetEmotion.CONCERNED -> {
                drawLine(
                    color = Color(0xFF140D26),
                    start = Offset(center.x - mouthWidth * 0.5f, center.y),
                    end = Offset(center.x + mouthWidth * 0.5f, center.y),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
            PetEmotion.SLEEPY -> {
                drawCircle(
                    color = Color(0xFF140D26),
                    radius = 3f,
                    center = center
                )
            }
        }
    }
}
