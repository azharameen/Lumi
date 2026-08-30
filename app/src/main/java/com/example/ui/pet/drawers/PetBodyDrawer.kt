package com.example.ui.pet.drawers

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
