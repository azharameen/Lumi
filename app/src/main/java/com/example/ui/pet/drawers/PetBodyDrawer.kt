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
 * High-craft 3D procedural body renderer for the 3 signature shapes:
 * SPHERE, CUBE, CAPSULE.
 * Features multi-layer clay gradients, glossy specular highlights,
 * ambient bounce lighting, and responsive drop shadows.
 */
fun DrawScope.drawPetAura(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    auraPulse: Float,
    auraColor: Color
) {
    // Outer atmospheric glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                auraColor.copy(alpha = 0.45f),
                auraColor.copy(alpha = 0.18f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = baseRadius * 1.75f * auraPulse
        ),
        radius = baseRadius * 1.75f * auraPulse,
        center = Offset(cx, cy)
    )
}

fun DrawScope.drawPetShadow(
    cx: Float,
    canvasHeight: Float,
    baseRadius: Float,
    squishX: Float,
    squishY: Float,
    floatingOffsetY: Float = 0f
) {
    // Ground shadow scales and blurs with height offset
    val heightFactor = (1f - (floatingOffsetY / 40f)).coerceIn(0.6f, 1.25f)
    val shadowWidth = baseRadius * 1.5f * squishX * heightFactor
    val shadowHeight = 26f * squishY * heightFactor
    val shadowAlpha = (0.35f * heightFactor).coerceIn(0.12f, 0.45f)

    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF000000).copy(alpha = shadowAlpha), Color.Transparent),
            center = Offset(cx, canvasHeight - 20f),
            radius = shadowWidth / 2
        ),
        topLeft = Offset(cx - shadowWidth / 2, canvasHeight - 20f - shadowHeight / 2),
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

    // 1. Base 3D Shaded Gradient Fill (rich clay lighting with subtle top highlight)
    val highlightOffset = Offset(cx - baseRadius * 0.32f, cy - baseRadius * 0.36f)

    drawPath(
        path = bodyPath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
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
                gradStart.copy(alpha = 0.4f)
            ),
            center = Offset(cx, cy + baseRadius * 0.95f),
            radius = baseRadius * 0.85f
        )
    )

    // 3. Signature Glossy Specular Highlights (Top-left shiny gel/clay reflection)
    drawShapeGlossyHighlights(cx, cy, baseRadius, shape)

    // 4. Subtle Outer Rim Polish
    drawPath(
        path = bodyPath,
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.35f)),
            center = Offset(cx + baseRadius * 0.5f, cy + baseRadius * 0.5f),
            radius = baseRadius * 0.95f
        ),
        style = Stroke(width = 3f)
    )
}

/**
 * Generates the clean, accurate, and appealing vector geometry for each of the 3 shapes:
 * SPHERE, CUBE, CAPSULE.
 */
private fun generateShapePath(
    cx: Float,
    cy: Float,
    r: Float,
    shape: BloubShape,
    breath: Float
): Path {
    val bOffset = (breath - 0.5f) * 3f
    return Path().apply {
        when (shape) {
            BloubShape.SPHERE -> {
                // 1. Perfect 3D volumetric round sphere
                val radius = r * 1.02f + bOffset
                addOval(
                    Rect(
                        cx - radius,
                        cy - radius,
                        cx + radius,
                        cy + radius
                    )
                )
            }

            BloubShape.CUBE -> {
                // 2. Modern cute rounded cube / clay box
                val size = r * 0.94f + bOffset
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
                // 3. Smooth tall rounded pill/capsule
                val pillWidth = r * 0.82f
                val pillHeight = r * 1.12f + bOffset
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
 * Shape-tailored glossy specular light reflections.
 */
private fun DrawScope.drawShapeGlossyHighlights(
    cx: Float,
    cy: Float,
    r: Float,
    shape: BloubShape
) {
    val glossCenterX = cx - r * 0.36f
    val glossCenterY = cy - r * 0.42f
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
            radius = glossWidth / 1.3f
        ),
        topLeft = Offset(glossCenterX - glossWidth / 2, glossCenterY - glossHeight / 2),
        size = Size(glossWidth, glossHeight)
    )

    // Secondary mini shine spot
    drawCircle(
        color = Color.White.copy(alpha = 0.92f),
        radius = r * 0.055f,
        center = Offset(glossCenterX + glossWidth * 0.42f, glossCenterY + glossHeight * 0.45f)
    )
}

fun DrawScope.drawPetCheeks(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    emotion: PetEmotion
) {
    val cheekOffsetY = cy + baseRadius * 0.16f
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
