package com.example.presentation.pet.drawers

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.domain.model.BloubShape
import com.example.domain.model.PetEmotion

/**
 * Normalized 3D procedural body renderer for signature shapes:
 * SPHERE, CUBE, CAPSULE.
 * All coordinates, stroke widths, and lighting gradients are strictly
 * relative to the Canvas DrawScope and baseRadius.
 */
fun DrawScope.drawPetAura(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    auraPulse: Float,
    auraColor: Color
) {
    val auraRadius = baseRadius * 1.72f * auraPulse
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                auraColor.copy(alpha = 0.42f),
                auraColor.copy(alpha = 0.16f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = auraRadius
        ),
        radius = auraRadius,
        center = Offset(cx, cy)
    )
}

fun DrawScope.drawPetShadow(
    cx: Float,
    canvasHeight: Float,
    baseRadius: Float,
    squishX: Float,
    squishY: Float,
    floatingOffsetRatio: Float = 0f
) {
    // Ground shadow scales and softens dynamically with normalized vertical displacement
    val heightFactor = (1f - (floatingOffsetRatio * 0.6f)).coerceIn(0.55f, 1.25f)
    val shadowWidth = baseRadius * 1.52f * squishX * heightFactor
    val shadowHeight = baseRadius * 0.24f * squishY * heightFactor
    val shadowAlpha = (0.32f * heightFactor).coerceIn(0.10f, 0.45f)
    val shadowY = canvasHeight - (baseRadius * 0.16f)

    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Black.copy(alpha = shadowAlpha),
                Color.Transparent
            ),
            center = Offset(cx, shadowY),
            radius = shadowWidth * 0.5f
        ),
        topLeft = Offset(cx - (shadowWidth * 0.5f), shadowY - (shadowHeight * 0.5f)),
        size = Size(shadowWidth, shadowHeight)
    )
}

fun DrawScope.drawPetBody(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    shape: BloubShape,
    gradStart: Color,
    gradMid: Color,
    gradEnd: Color,
    breathProgress: Float = 0f
) {
    val bodyPath = generateShapePath(cx, cy, baseRadius, shape, breathProgress)

    // 1. Base 3D Clay Gradient Fill (proportional highlight focal offset)
    val highlightOffset = Offset(cx - (baseRadius * 0.32f), cy - (baseRadius * 0.36f))

    drawPath(
        path = bodyPath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.94f),
                gradStart,
                gradMid,
                gradEnd,
                gradEnd.copy(alpha = 0.98f)
            ),
            center = highlightOffset,
            radius = baseRadius * 1.45f
        )
    )

    // 2. Bottom Ambient Bounce Light (adds soft 3D ground reflection volume)
    drawPath(
        path = bodyPath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.28f),
                gradStart.copy(alpha = 0.38f)
            ),
            center = Offset(cx, cy + (baseRadius * 0.92f)),
            radius = baseRadius * 0.85f
        )
    )

    // 3. Signature Glossy Specular Highlights
    drawShapeGlossyHighlights(cx, cy, baseRadius, shape)

    // 4. Subtle Outer Rim Polish with normalized stroke thickness
    val rimStrokeWidth = (baseRadius * 0.024f).coerceAtLeast(1.5f)
    drawPath(
        path = bodyPath,
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.35f)),
            center = Offset(cx + (baseRadius * 0.48f), cy + (baseRadius * 0.48f)),
            radius = baseRadius * 0.96f
        ),
        style = Stroke(width = rimStrokeWidth)
    )
}

/**
 * Generates normalized vector geometry for each shape:
 * SPHERE, CUBE, CAPSULE without magic numbers.
 */
private fun generateShapePath(
    cx: Float,
    cy: Float,
    r: Float,
    shape: BloubShape,
    breath: Float
): Path {
    val bOffset = (breath - 0.5f) * (r * 0.035f)
    return Path().apply {
        when (shape) {
            BloubShape.SPHERE -> {
                val radius = (r * 1.02f) + bOffset
                addOval(
                    Rect(
                        left = cx - radius,
                        top = cy - radius,
                        right = cx + radius,
                        bottom = cy + radius
                    )
                )
            }

            BloubShape.CUBE -> {
                val size = (r * 0.94f) + bOffset
                val corner = r * 0.36f
                addRoundRect(
                    RoundRect(
                        left = cx - size,
                        top = cy - size,
                        right = cx + size,
                        bottom = cy + size,
                        cornerRadius = CornerRadius(corner, corner)
                    )
                )
            }

            BloubShape.CAPSULE -> {
                val pillWidth = r * 0.82f
                val pillHeight = (r * 1.12f) + bOffset
                addRoundRect(
                    RoundRect(
                        left = cx - pillWidth,
                        top = cy - pillHeight,
                        right = cx + pillWidth,
                        bottom = cy + pillHeight,
                        cornerRadius = CornerRadius(pillWidth, pillWidth)
                    )
                )
            }
        }
    }
}

/**
 * Normalized glossy specular light reflections scaled strictly by baseRadius.
 */
private fun DrawScope.drawShapeGlossyHighlights(
    cx: Float,
    cy: Float,
    r: Float,
    shape: BloubShape
) {
    val glossCenterX = cx - (r * 0.36f)
    val glossCenterY = cy - (r * 0.42f)
    val glossWidth = r * 0.34f
    val glossHeight = r * 0.18f

    // Main primary specular bean/oval
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.88f),
                Color.White.copy(alpha = 0.45f),
                Color.Transparent
            ),
            center = Offset(glossCenterX, glossCenterY),
            radius = glossWidth * 0.75f
        ),
        topLeft = Offset(glossCenterX - (glossWidth * 0.5f), glossCenterY - (glossHeight * 0.5f)),
        size = Size(glossWidth, glossHeight)
    )

    // Secondary mini shine spot
    drawCircle(
        color = Color.White.copy(alpha = 0.92f),
        radius = r * 0.055f,
        center = Offset(glossCenterX + (glossWidth * 0.42f), glossCenterY + (glossHeight * 0.45f))
    )
}

fun DrawScope.drawPetCheeks(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    emotion: PetEmotion
) {
    val cheekOffsetY = cy + (baseRadius * 0.16f)
    val cheekDistance = baseRadius * 0.52f
    val cheekRadius = baseRadius * 0.18f
    val cheekColor = when (emotion) {
        PetEmotion.LOVING, PetEmotion.HAPPY -> Color(0x77FF3385)
        PetEmotion.PLAYFUL, PetEmotion.ENERGETIC -> Color(0x66FF5964)
        else -> Color(0x45FF70A6)
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(cheekColor, Color.Transparent),
            center = Offset(cx - cheekDistance, cheekOffsetY),
            radius = cheekRadius
        ),
        radius = cheekRadius,
        center = Offset(cx - cheekDistance, cheekOffsetY)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(cheekColor, Color.Transparent),
            center = Offset(cx + cheekDistance, cheekOffsetY),
            radius = cheekRadius
        ),
        radius = cheekRadius,
        center = Offset(cx + cheekDistance, cheekOffsetY)
    )
}

/**
 * Procedural 3D Vector Accessory Renderer.
 * Draws custom wearable items on Lumi's head/body based on activeAccessory id.
 */
fun DrawScope.drawPetAccessory(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    accessoryId: String
) {
    when (accessoryId) {
        "SPROUT" -> {
            val stemTopY = cy - (baseRadius * 0.95f)
            val stemBaseY = cy - (baseRadius * 0.78f)
            // Tiny green sprout stem
            val stemPath = Path().apply {
                moveTo(cx, stemBaseY)
                quadraticTo(cx - (baseRadius * 0.04f), (stemTopY + stemBaseY) * 0.5f, cx, stemTopY)
            }
            drawPath(
                path = stemPath,
                color = Color(0xFF4CAF50),
                style = Stroke(width = (baseRadius * 0.045f).coerceAtLeast(2f), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            // Left Leaf
            val leftLeaf = Path().apply {
                moveTo(cx, stemTopY)
                cubicTo(
                    cx - (baseRadius * 0.22f), stemTopY - (baseRadius * 0.12f),
                    cx - (baseRadius * 0.28f), stemTopY + (baseRadius * 0.05f),
                    cx, stemTopY + (baseRadius * 0.02f)
                )
                close()
            }
            drawPath(leftLeaf, color = Color(0xFF66BB6A))
            // Right Leaf
            val rightLeaf = Path().apply {
                moveTo(cx, stemTopY + (baseRadius * 0.02f))
                cubicTo(
                    cx + (baseRadius * 0.25f), stemTopY - (baseRadius * 0.08f),
                    cx + (baseRadius * 0.30f), stemTopY + (baseRadius * 0.08f),
                    cx, stemTopY + (baseRadius * 0.06f)
                )
                close()
            }
            drawPath(rightLeaf, color = Color(0xFF81C784))
        }

        "CROWN" -> {
            val crownY = cy - (baseRadius * 0.88f)
            val crownW = baseRadius * 0.7f
            val crownH = baseRadius * 0.4f
            val crownPath = Path().apply {
                moveTo(cx - (crownW * 0.5f), crownY)
                lineTo(cx - (crownW * 0.55f), crownY - (crownH * 0.8f))
                lineTo(cx - (crownW * 0.22f), crownY - (crownH * 0.35f))
                lineTo(cx, crownY - crownH)
                lineTo(cx + (crownW * 0.22f), crownY - (crownH * 0.35f))
                lineTo(cx + (crownW * 0.55f), crownY - (crownH * 0.8f))
                lineTo(cx + (crownW * 0.5f), crownY)
                close()
            }
            drawPath(
                path = crownPath,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFFEE58), Color(0xFFFFB300), Color(0xFFFF8F00)),
                    startY = crownY - crownH,
                    endY = crownY
                )
            )
            // Crown jewels
            drawCircle(Color(0xFFE91E63), radius = baseRadius * 0.04f, center = Offset(cx, crownY - (crownH * 0.88f)))
            drawCircle(Color(0xFF29B6F6), radius = baseRadius * 0.032f, center = Offset(cx - (crownW * 0.45f), crownY - (crownH * 0.72f)))
            drawCircle(Color(0xFF29B6F6), radius = baseRadius * 0.032f, center = Offset(cx + (crownW * 0.45f), crownY - (crownH * 0.72f)))
        }

        "HEADPHONES" -> {
            val topHeadY = cy - (baseRadius * 0.82f)
            val bandW = baseRadius * 1.05f
            // Headband arc
            drawArc(
                color = Color(0xFF37474F),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - (bandW * 0.5f), topHeadY - (baseRadius * 0.15f)),
                size = Size(bandW, baseRadius * 0.9f),
                style = Stroke(width = (baseRadius * 0.06f).coerceAtLeast(3f), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            // Left Ear Cup
            val cupW = baseRadius * 0.26f
            val cupH = baseRadius * 0.46f
            drawRoundRect(
                color = Color(0xFFFF4081),
                topLeft = Offset(cx - (bandW * 0.52f) - (cupW * 0.5f), cy - (cupH * 0.35f)),
                size = Size(cupW, cupH),
                cornerRadius = CornerRadius(cupW * 0.4f, cupW * 0.4f)
            )
            // Right Ear Cup
            drawRoundRect(
                color = Color(0xFFFF4081),
                topLeft = Offset(cx + (bandW * 0.52f) - (cupW * 0.5f), cy - (cupH * 0.35f)),
                size = Size(cupW, cupH),
                cornerRadius = CornerRadius(cupW * 0.4f, cupW * 0.4f)
            )
        }

        "WIZARD_HAT" -> {
            val hatBaseY = cy - (baseRadius * 0.75f)
            val brimW = baseRadius * 0.95f
            val brimH = baseRadius * 0.22f
            // Hat Brim Oval
            drawOval(
                color = Color(0xFF4A148C),
                topLeft = Offset(cx - (brimW * 0.5f), hatBaseY - (brimH * 0.5f)),
                size = Size(brimW, brimH)
            )
            // Pointed Wizard Cone
            val conePath = Path().apply {
                moveTo(cx - (brimW * 0.35f), hatBaseY)
                quadraticTo(cx - (baseRadius * 0.1f), hatBaseY - (baseRadius * 0.5f), cx + (baseRadius * 0.15f), hatBaseY - (baseRadius * 0.95f))
                quadraticTo(cx + (baseRadius * 0.25f), hatBaseY - (baseRadius * 0.4f), cx + (brimW * 0.35f), hatBaseY)
                close()
            }
            drawPath(
                path = conePath,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF7B1FA2), Color(0xFF4A148C)),
                    startY = hatBaseY - (baseRadius * 0.95f),
                    endY = hatBaseY
                )
            )
            // Gold Star on hat tip
            drawCircle(Color(0xFFFFD54F), radius = baseRadius * 0.05f, center = Offset(cx + (baseRadius * 0.15f), hatBaseY - (baseRadius * 0.95f)))
        }

        "HALO" -> {
            val haloY = cy - (baseRadius * 1.05f)
            val haloW = baseRadius * 0.9f
            val haloH = baseRadius * 0.24f
            // Golden glowing ring
            drawOval(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFFEA00), Color(0xFFFFD600), Color(0x00FFD600)),
                    center = Offset(cx, haloY),
                    radius = haloW * 0.6f
                ),
                topLeft = Offset(cx - (haloW * 0.5f), haloY - (haloH * 0.5f)),
                size = Size(haloW, haloH),
                style = Stroke(width = (baseRadius * 0.065f).coerceAtLeast(3f))
            )
        }

        "STAR_GLASSES" -> {
            val glassesY = cy - (baseRadius * 0.08f)
            val glassSize = baseRadius * 0.32f
            val dist = baseRadius * 0.35f
            // Bridge
            drawLine(
                color = Color(0xFFFFD700),
                start = Offset(cx - (dist * 0.5f), glassesY),
                end = Offset(cx + (dist * 0.5f), glassesY),
                strokeWidth = (baseRadius * 0.035f).coerceAtLeast(2f)
            )
            // Left star
            drawCircle(Color(0xBBFFD700), radius = glassSize * 0.5f, center = Offset(cx - dist, glassesY))
            // Right star
            drawCircle(Color(0xBBFFD700), radius = glassSize * 0.5f, center = Offset(cx + dist, glassesY))
        }
    }
}
