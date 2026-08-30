package com.example.presentation.pet.drawers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.domain.model.PetEmotion

/**
 * Normalized expressive facial renderer with wide-open glossy eyes,
 * multi-stop specular catchlights, star & heart pupil reflections,
 * dynamic gaze tracking, and reactive mouth expressions.
 * All strokes, control points, and offsets scale relative to radiusX/Y and baseRadius.
 */
fun DrawScope.drawPetEye(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    isBlinking: Boolean,
    gazeX: Float,
    gazeY: Float,
    isThinking: Boolean,
    emotion: PetEmotion,
    isLeftEye: Boolean
) {
    val eyelidStrokeWidth = (radiusY * 0.22f).coerceAtLeast(1.8f)

    if (isBlinking) {
        // Natural blink eyelid arc with normalized stroke
        drawArc(
            color = Color(0xFF140D26),
            startAngle = 15f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(center.x - radiusX, center.y - (radiusY * 0.3f)),
            size = Size(radiusX * 2f, radiusY * 0.8f),
            style = Stroke(width = eyelidStrokeWidth, cap = StrokeCap.Round)
        )
        return
    }

    when (emotion) {
        PetEmotion.SLEEPY -> {
            // Peaceful sleeping curved eyelids
            val path = Path().apply {
                val topArcY = center.y - (radiusY * 0.1f)
                moveTo(center.x - (radiusX * 0.95f), topArcY)
                cubicTo(
                    center.x - (radiusX * 0.3f), topArcY + (radiusY * 0.8f),
                    center.x + (radiusX * 0.3f), topArcY + (radiusY * 0.8f),
                    center.x + (radiusX * 0.95f), topArcY
                )
            }
            drawPath(
                path = path,
                color = Color(0xFF130D28),
                style = Stroke(width = eyelidStrokeWidth * 1.15f, cap = StrokeCap.Round)
            )
            // Sleep glint above
            drawCircle(
                color = Color(0xFFB5A6FF).copy(alpha = 0.5f),
                radius = radiusX * 0.18f,
                center = Offset(center.x + (gazeX * radiusX * 0.25f), center.y - (radiusY * 0.3f))
            )
        }

        PetEmotion.HAPPY -> {
            drawGlossyOpenEye(
                center = center,
                rx = radiusX * 1.05f,
                ry = radiusY * 1.05f,
                gazeX = gazeX,
                gazeY = gazeY,
                sparkleType = EyeSparkleType.DUAL_SHINE,
                lidLift = 0.18f
            )
        }

        PetEmotion.ENERGETIC -> {
            drawGlossyOpenEye(
                center = center,
                rx = radiusX * 1.1f,
                ry = radiusY * 1.1f,
                gazeX = gazeX,
                gazeY = gazeY,
                sparkleType = EyeSparkleType.STAR
            )
        }

        PetEmotion.LOVING -> {
            drawGlossyOpenEye(
                center = center,
                rx = radiusX * 1.05f,
                ry = radiusY * 1.05f,
                gazeX = gazeX,
                gazeY = gazeY,
                sparkleType = EyeSparkleType.HEART
            )
        }

        PetEmotion.PLAYFUL -> {
            drawGlossyOpenEye(
                center = center,
                rx = radiusX * 1.08f,
                ry = radiusY * 1.08f,
                gazeX = gazeX,
                gazeY = gazeY,
                sparkleType = if (isLeftEye) EyeSparkleType.STAR else EyeSparkleType.DUAL_SHINE
            )
        }

        PetEmotion.THINKING -> {
            val thinkGazeX = if (gazeX == 0f) 0.55f else gazeX
            val thinkGazeY = if (gazeY == 0f) -0.55f else gazeY
            drawGlossyOpenEye(
                center = center,
                rx = radiusX * 0.98f,
                ry = radiusY * 1.02f,
                gazeX = thinkGazeX,
                gazeY = thinkGazeY,
                sparkleType = EyeSparkleType.PINPOINT
            )
        }

        PetEmotion.CONCERNED -> {
            drawGlossyOpenEye(
                center = center,
                rx = radiusX * 1.02f,
                ry = radiusY * 1.04f,
                gazeX = gazeX,
                gazeY = gazeY,
                sparkleType = EyeSparkleType.SOFT_EMPATHY
            )
        }

        PetEmotion.CALM -> {
            drawGlossyOpenEye(
                center = center,
                rx = radiusX,
                ry = radiusY,
                gazeX = gazeX,
                gazeY = gazeY,
                sparkleType = EyeSparkleType.DUAL_SHINE
            )
        }
    }
}

private enum class EyeSparkleType {
    DUAL_SHINE,
    STAR,
    HEART,
    PINPOINT,
    SOFT_EMPATHY
}

/**
 * Renders a wide-open glossy 3D eye socket, dark deep pupil, and multi-layered specular reflections.
 */
private fun DrawScope.drawGlossyOpenEye(
    center: Offset,
    rx: Float,
    ry: Float,
    gazeX: Float,
    gazeY: Float,
    sparkleType: EyeSparkleType,
    lidLift: Float = 0f
) {
    // 1. Deep glossy dark eye socket
    drawOval(
        color = Color(0xFF120B24),
        topLeft = Offset(center.x - rx, center.y - ry),
        size = Size(rx * 2f, ry * 2f)
    )

    // Optional lower joyful lid lift accent
    if (lidLift > 0f) {
        val liftY = center.y + (ry * (1f - lidLift))
        val strokeW = (ry * 0.12f).coerceAtLeast(1.2f)
        drawArc(
            color = Color(0xFF1D1438),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(center.x - rx, liftY - (ry * 0.4f)),
            size = Size(rx * 2f, ry * 0.8f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
    }

    val maxGazeOffsetX = rx * 0.36f
    val maxGazeOffsetY = ry * 0.36f
    val pupilX = center.x + (gazeX * maxGazeOffsetX)
    val pupilY = center.y + (gazeY * maxGazeOffsetY)

    when (sparkleType) {
        EyeSparkleType.DUAL_SHINE -> {
            // Main primary top-left catchlight
            drawCircle(
                color = Color.White,
                radius = rx * 0.44f,
                center = Offset(pupilX - (rx * 0.22f), pupilY - (ry * 0.26f))
            )
            // Secondary bottom-right bounce catchlight
            drawCircle(
                color = Color.White.copy(alpha = 0.92f),
                radius = rx * 0.22f,
                center = Offset(pupilX + (rx * 0.28f), pupilY + (ry * 0.28f))
            )
        }

        EyeSparkleType.STAR -> {
            // 4-Point radiant star pupil sparkle
            drawStarSparkle(Offset(pupilX - (rx * 0.1f), pupilY - (ry * 0.1f)), rx * 0.85f)
            // Secondary shiny dot
            drawCircle(
                color = Color.White.copy(alpha = 0.95f),
                radius = rx * 0.24f,
                center = Offset(pupilX + (rx * 0.32f), pupilY + (ry * 0.32f))
            )
        }

        EyeSparkleType.HEART -> {
            // Glowing heart specular catchlight in center
            drawHeartCatchlight(Offset(pupilX - (rx * 0.08f), pupilY - (ry * 0.12f)), rx * 0.8f)
            // Secondary sparkle
            drawCircle(
                color = Color.White.copy(alpha = 0.95f),
                radius = rx * 0.2f,
                center = Offset(pupilX + (rx * 0.3f), pupilY + (ry * 0.3f))
            )
        }

        EyeSparkleType.PINPOINT -> {
            // Sharp focused thinking catchlights
            drawCircle(
                color = Color.White,
                radius = rx * 0.38f,
                center = Offset(pupilX - (rx * 0.24f), pupilY - (ry * 0.28f))
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = rx * 0.18f,
                center = Offset(pupilX + (rx * 0.25f), pupilY + (ry * 0.22f))
            )
        }

        EyeSparkleType.SOFT_EMPATHY -> {
            // Soft dewy large catchlight
            drawCircle(
                color = Color.White,
                radius = rx * 0.46f,
                center = Offset(pupilX - (rx * 0.2f), pupilY - (ry * 0.22f))
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = rx * 0.24f,
                center = Offset(pupilX + (rx * 0.24f), pupilY + (ry * 0.26f))
            )
        }
    }
}

private fun DrawScope.drawStarSparkle(center: Offset, size: Float) {
    val s = size * 0.5f
    val path = Path().apply {
        moveTo(center.x, center.y - s)
        cubicTo(center.x, center.y, center.x, center.y, center.x + s, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x, center.y + s)
        cubicTo(center.x, center.y, center.x, center.y, center.x - s, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x, center.y - s)
        close()
    }
    drawPath(path = path, color = Color.White, style = Fill)
}

private fun DrawScope.drawHeartCatchlight(center: Offset, size: Float) {
    val r = size * 0.5f
    val path = Path().apply {
        moveTo(center.x, center.y + (r * 0.7f))
        cubicTo(
            center.x - (r * 1.1f), center.y - (r * 0.3f),
            center.x - (r * 0.5f), center.y - (r * 1.1f),
            center.x, center.y - (r * 0.3f)
        )
        cubicTo(
            center.x + (r * 0.5f), center.y - (r * 1.1f),
            center.x + (r * 1.1f), center.y - (r * 0.3f),
            center.x, center.y + (r * 0.7f)
        )
        close()
    }
    drawPath(path = path, color = Color.White, style = Fill)
}

fun DrawScope.drawPetMouth(
    center: Offset,
    emotion: PetEmotion,
    isSpeaking: Boolean,
    talkAmount: Float,
    baseRadius: Float
) {
    val mouthWidth = baseRadius * 0.28f
    val mouthStrokeWidth = (baseRadius * 0.036f).coerceAtLeast(1.8f)

    if (isSpeaking) {
        // Animated speaking mouth with cute pink tongue
        val openHeight = (baseRadius * 0.18f) * (0.35f + (talkAmount * 0.65f))
        drawOval(
            color = Color(0xFF140D26),
            topLeft = Offset(center.x - (mouthWidth * 0.5f), center.y - (openHeight * 0.5f)),
            size = Size(mouthWidth, openHeight)
        )
        // Cute pink tongue inside
        drawOval(
            color = Color(0xFFFF4081),
            topLeft = Offset(center.x - (mouthWidth * 0.3f), center.y + (openHeight * 0.05f)),
            size = Size(mouthWidth * 0.6f, openHeight * 0.45f)
        )
    } else {
        when (emotion) {
            PetEmotion.HAPPY -> {
                // Cheerful open smile with cute pink tongue
                val smileHeight = baseRadius * 0.14f
                val path = Path().apply {
                    moveTo(center.x - (mouthWidth * 0.55f), center.y - (smileHeight * 0.3f))
                    cubicTo(
                        center.x - (mouthWidth * 0.4f), center.y + smileHeight,
                        center.x + (mouthWidth * 0.4f), center.y + smileHeight,
                        center.x + (mouthWidth * 0.55f), center.y - (smileHeight * 0.3f)
                    )
                    close()
                }
                drawPath(path = path, color = Color(0xFF140D26), style = Fill)
                drawOval(
                    color = Color(0xFFFF4081),
                    topLeft = Offset(center.x - (mouthWidth * 0.25f), center.y + (smileHeight * 0.25f)),
                    size = Size(mouthWidth * 0.5f, smileHeight * 0.6f)
                )
            }

            PetEmotion.ENERGETIC -> {
                // Big joyful open mouth
                val mouthHeight = baseRadius * 0.16f
                val path = Path().apply {
                    moveTo(center.x - (mouthWidth * 0.55f), center.y - (mouthHeight * 0.3f))
                    cubicTo(
                        center.x - (mouthWidth * 0.45f), center.y + (mouthHeight * 1.1f),
                        center.x + (mouthWidth * 0.45f), center.y + (mouthHeight * 1.1f),
                        center.x + (mouthWidth * 0.55f), center.y - (mouthHeight * 0.3f)
                    )
                    close()
                }
                drawPath(path = path, color = Color(0xFF140D26), style = Fill)
                drawOval(
                    color = Color(0xFFFF4081),
                    topLeft = Offset(center.x - (mouthWidth * 0.28f), center.y + (mouthHeight * 0.25f)),
                    size = Size(mouthWidth * 0.56f, mouthHeight * 0.65f)
                )
            }

            PetEmotion.PLAYFUL -> {
                // Cute W cat smile (3 ‿ 3)
                val wMouth = Path().apply {
                    val midOffset = baseRadius * 0.03f
                    val curveDepth = baseRadius * 0.09f
                    moveTo(center.x - (mouthWidth * 0.8f), center.y - midOffset)
                    quadraticTo(center.x - (mouthWidth * 0.4f), center.y + curveDepth, center.x, center.y)
                    quadraticTo(center.x + (mouthWidth * 0.4f), center.y + curveDepth, center.x + (mouthWidth * 0.8f), center.y - midOffset)
                }
                drawPath(
                    path = wMouth,
                    color = Color(0xFF140D26),
                    style = Stroke(width = mouthStrokeWidth, cap = StrokeCap.Round)
                )
            }

            PetEmotion.LOVING -> {
                // Sweet gentle curved smile
                val path = Path().apply {
                    moveTo(center.x - (mouthWidth * 0.65f), center.y - (baseRadius * 0.02f))
                    quadraticTo(center.x, center.y + (baseRadius * 0.1f), center.x + (mouthWidth * 0.65f), center.y - (baseRadius * 0.02f))
                }
                drawPath(
                    path = path,
                    color = Color(0xFF140D26),
                    style = Stroke(width = mouthStrokeWidth, cap = StrokeCap.Round)
                )
            }

            PetEmotion.CALM -> {
                // Serene gentle smile
                val path = Path().apply {
                    moveTo(center.x - (mouthWidth * 0.55f), center.y)
                    quadraticTo(center.x, center.y + (baseRadius * 0.07f), center.x + (mouthWidth * 0.55f), center.y)
                }
                drawPath(
                    path = path,
                    color = Color(0xFF140D26),
                    style = Stroke(width = (baseRadius * 0.032f).coerceAtLeast(1.8f), cap = StrokeCap.Round)
                )
            }

            PetEmotion.THINKING -> {
                // Cute tiny focused circle mouth ( o )
                drawCircle(
                    color = Color(0xFF140D26),
                    radius = baseRadius * 0.06f,
                    center = center,
                    style = Stroke(width = (baseRadius * 0.034f).coerceAtLeast(1.8f))
                )
            }

            PetEmotion.CONCERNED -> {
                // Gentle empathetic curve
                val path = Path().apply {
                    moveTo(center.x - (mouthWidth * 0.5f), center.y + (baseRadius * 0.03f))
                    quadraticTo(center.x, center.y - (baseRadius * 0.03f), center.x + (mouthWidth * 0.5f), center.y + (baseRadius * 0.03f))
                }
                drawPath(
                    path = path,
                    color = Color(0xFF140D26),
                    style = Stroke(width = (baseRadius * 0.03f).coerceAtLeast(1.6f), cap = StrokeCap.Round)
                )
            }

            PetEmotion.SLEEPY -> {
                // Cute tiny relaxed sleeping line
                drawLine(
                    color = Color(0xFF140D26),
                    start = Offset(center.x - (mouthWidth * 0.35f), center.y),
                    end = Offset(center.x + (mouthWidth * 0.35f), center.y),
                    strokeWidth = (baseRadius * 0.028f).coerceAtLeast(1.6f),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
