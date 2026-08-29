package com.example.ui.pet.drawers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.domain.model.PetEmotion

/**
 * Renders Lumi's 3D glowing body sphere, drop shadows, and ambient radial aura.
 */
fun DrawScope.drawPetAura(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    auraPulse: Float,
    auraColor: Color
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(auraColor, Color.Transparent),
            center = Offset(cx, cy),
            radius = baseRadius * 1.55f * auraPulse
        ),
        radius = baseRadius * 1.55f * auraPulse,
        center = Offset(cx, cy)
    )
}

fun DrawScope.drawPetShadow(
    cx: Float,
    canvasHeight: Float,
    baseRadius: Float,
    squishX: Float,
    squishY: Float
) {
    val shadowWidth = baseRadius * 1.4f * squishX
    val shadowHeight = 22f * squishY
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x55000000), Color.Transparent),
            center = Offset(cx, canvasHeight - 24f),
            radius = shadowWidth / 2
        ),
        topLeft = Offset(cx - shadowWidth / 2, canvasHeight - 35f),
        size = Size(shadowWidth, shadowHeight)
    )
}

fun DrawScope.drawPetBody(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    gradStart: Color,
    gradMid: Color,
    gradEnd: Color
) {
    val bodyPath = Path().apply {
        val topY = cy - baseRadius * 1.05f
        val bottomY = cy + baseRadius * 0.95f
        val leftX = cx - baseRadius * 0.98f
        val rightX = cx + baseRadius * 0.98f

        moveTo(cx, topY)
        cubicTo(rightX + 8f, topY + 12f, rightX + 8f, bottomY - 10f, cx, bottomY)
        cubicTo(leftX - 8f, bottomY - 10f, leftX - 8f, topY + 12f, cx, topY)
        close()
    }

    // Shaded gradient fill
    drawPath(
        path = bodyPath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.92f),
                gradStart,
                gradMid,
                gradEnd
            ),
            center = Offset(cx - baseRadius * 0.35f, cy - baseRadius * 0.38f),
            radius = baseRadius * 1.35f
        )
    )

    // Secondary ambient rim light
    drawPath(
        path = bodyPath,
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.32f)),
            center = Offset(cx + baseRadius * 0.6f, cy + baseRadius * 0.6f),
            radius = baseRadius * 0.8f
        ),
        style = Stroke(width = 3.5f)
    )
}

fun DrawScope.drawPetCheeks(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    emotion: PetEmotion
) {
    val cheekOffsetY = cy + baseRadius * 0.15f
    val cheekDistance = baseRadius * 0.52f
    val cheekRadius = baseRadius * 0.16f
    val cheekColor = when (emotion) {
        PetEmotion.LOVING, PetEmotion.HAPPY -> Color(0x66FF4081)
        else -> Color(0x40FF70A6)
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
