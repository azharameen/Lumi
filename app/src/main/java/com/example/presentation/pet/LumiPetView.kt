package com.example.presentation.pet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.presentation.pet.drawers.drawHeartParticle
import com.example.presentation.pet.drawers.drawPetAccessory
import com.example.presentation.pet.drawers.drawPetAura
import com.example.presentation.pet.drawers.drawPetBody
import com.example.presentation.pet.drawers.drawPetCheeks
import com.example.presentation.pet.drawers.drawPetEye
import com.example.presentation.pet.drawers.drawPetMouth
import com.example.presentation.pet.drawers.drawPetShadow
import com.example.presentation.pet.drawers.drawSparkleParticle
import com.example.presentation.pet.drawers.drawStarParticle
import com.example.presentation.pet.models.PetParticle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-performance Jetpack Compose Canvas mascot view representing Lumi / Bloub.
 * Implements 100% normalized procedural graphics, clay lighting, jelly spring physics,
 * dynamic gaze tracking, and zero-allocation draw loops.
 */
@Composable
fun LumiPetView(
    petStatus: PetStatus,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    enableInternalGestures: Boolean = true,
    externalGazeX: Float = 0f,
    externalGazeY: Float = 0f,
    onPetTouched: () -> Unit = {},
    onPetPetted: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    // Gaze tracking coordinates
    var targetGazeX by remember { mutableFloatStateOf(0f) }
    var targetGazeY by remember { mutableFloatStateOf(0f) }
    val activeGazeX = if (enableInternalGestures) targetGazeX else externalGazeX
    val activeGazeY = if (enableInternalGestures) targetGazeY else externalGazeY
    val animatedGazeX by animateFloatAsState(
        targetValue = activeGazeX,
        animationSpec = spring(stiffness = 320f),
        label = "GazeX"
    )
    val animatedGazeY by animateFloatAsState(
        targetValue = activeGazeY,
        animationSpec = spring(stiffness = 320f),
        label = "GazeY"
    )

    // Interactive squish & jelly bounce physics (normalized ratios)
    val squishX = remember { Animatable(1f) }
    val squishY = remember { Animatable(1f) }
    val jumpProgress = remember { Animatable(0f) }
    val rotationZ = remember { Animatable(0f) }

    // Natural periodic blinking with occasional double-blinks
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2600, 4800))
            isBlinking = true
            delay(130)
            isBlinking = false
            if (Random.nextFloat() > 0.65f) {
                delay(120)
                isBlinking = true
                delay(110)
                isBlinking = false
            }
        }
    }

    // Breathing, floating & aura transitions (normalized 0f..1f and -1f..1f ranges)
    val infiniteTransition = rememberInfiniteTransition(label = "BloubPhysics")
    val breathProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Breath"
    )

    val floatingProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Float"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Aura"
    )

    val mouthTalkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MouthTalk"
    )

    // Optimized Particle System
    val particles = remember { mutableStateListOf<PetParticle>() }
    LaunchedEffect(Unit) {
        while (true) {
            delay(32)
            if (particles.isNotEmpty()) {
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.x += p.vx
                    p.y += p.vy
                    p.alpha -= 0.024f
                    if (p.alpha <= 0f) {
                        iterator.remove()
                    }
                }
            }
        }
    }

    // Cache cosmetic colors from PetStatus to eliminate allocation inside draw loop
    val skin = petStatus.bloubSkinColor
    val gradStart = remember(skin.primaryHex) { Color(skin.primaryHex) }
    val gradMid = remember(skin.midHex) { Color(skin.midHex) }
    val gradEnd = remember(skin.endHex) { Color(skin.endHex) }
    val auraColor = remember(skin.glowHex) { Color(skin.glowHex) }

    fun spawnParticles(type: String, count: Int, baseColor: Color, baseRadius: Float) {
        for (i in 0 until count) {
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val speed = baseRadius * (Random.nextFloat() * 0.035f + 0.015f)
            particles.add(
                PetParticle(
                    x = 0f,
                    y = -baseRadius * 0.15f,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed - (baseRadius * 0.025f)).toFloat(),
                    alpha = 1.0f,
                    type = type,
                    color = baseColor,
                    size = baseRadius * (Random.nextFloat() * 0.14f + 0.10f),
                    rotation = Random.nextFloat() * 360f
                )
            )
        }
    }

    val gestureModifier = if (enableInternalGestures) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    onPetTouched()
                    coroutineScope.launch {
                        squishX.animateTo(1.24f, tween(75))
                        squishX.animateTo(0.88f, tween(95))
                        squishX.animateTo(1.08f, tween(110))
                        squishX.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 450f))
                    }
                    coroutineScope.launch {
                        squishY.animateTo(0.78f, tween(75))
                        squishY.animateTo(1.18f, tween(95))
                        squishY.animateTo(0.94f, tween(110))
                        squishY.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 450f))
                    }
                    coroutineScope.launch {
                        jumpProgress.animateTo(1f, tween(110))
                        jumpProgress.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = 320f))
                    }
                    coroutineScope.launch {
                        rotationZ.animateTo(if (Random.nextBoolean()) 6f else -6f, tween(80))
                        rotationZ.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 300f))
                    }
                    // Estimate approximate baseRadius based on size.toPx()
                    val approxRadius = size.toPx() * 0.38f
                    spawnParticles("HEART", 5, gradStart, approxRadius)
                }
            )
        }
    } else {
        Modifier
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .then(gestureModifier)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minDimension = minOf(this.size.width, this.size.height)
            val baseRadius = (minDimension * 0.38f) * (0.95f + (breathProgress * 0.06f))
            val cx = this.size.width * 0.5f

            val floatingDisplacement = floatingProgress * (baseRadius * 0.07f)
            val jumpDisplacement = jumpProgress.value * (-baseRadius * 0.22f)
            val currentFloatY = floatingDisplacement + jumpDisplacement
            val cy = (this.size.height * 0.5f) + currentFloatY

            val floatingOffsetRatio = (currentFloatY / (baseRadius * 0.25f)).coerceIn(-1.5f, 1.5f)

            // 1. Atmosphere Aura Glow
            drawPetAura(
                cx = cx,
                cy = cy,
                baseRadius = baseRadius,
                auraPulse = auraPulse,
                auraColor = auraColor
            )

            // 2. Dynamic Ground Soft Shadow
            drawPetShadow(
                cx = cx,
                canvasHeight = this.size.height,
                baseRadius = baseRadius,
                squishX = squishX.value,
                squishY = squishY.value,
                floatingOffsetRatio = floatingOffsetRatio
            )

            // 3. Main Character Body & Expressions with spring dynamics
            scale(
                scaleX = squishX.value,
                scaleY = squishY.value,
                pivot = Offset(cx, cy + baseRadius)
            ) {
                rotate(degrees = rotationZ.value, pivot = Offset(cx, cy)) {
                    // 3D Procedural Clay Body
                    drawPetBody(
                        cx = cx,
                        cy = cy,
                        baseRadius = baseRadius,
                        shape = petStatus.bloubShape,
                        gradStart = gradStart,
                        gradMid = gradMid,
                        gradEnd = gradEnd,
                        breathProgress = breathProgress
                    )

                    val faceCenterY = cy

                    // Blushing Cheeks
                    drawPetCheeks(
                        cx = cx,
                        cy = faceCenterY,
                        baseRadius = baseRadius,
                        emotion = petStatus.currentEmotion
                    )

                    // Big Glossy Eyes
                    val eyeDistance = baseRadius * 0.35f
                    val eyeOffsetY = faceCenterY - (baseRadius * 0.08f)
                    val eyeRadiusX = baseRadius * 0.14f
                    val eyeRadiusY = if (isBlinking) {
                        (baseRadius * 0.02f).coerceAtLeast(1.5f)
                    } else {
                        baseRadius * 0.16f
                    }

                    // Left Eye
                    drawPetEye(
                        center = Offset(cx - eyeDistance, eyeOffsetY),
                        radiusX = eyeRadiusX,
                        radiusY = eyeRadiusY,
                        isBlinking = isBlinking,
                        gazeX = animatedGazeX,
                        gazeY = animatedGazeY,
                        isThinking = petStatus.isThinking,
                        emotion = petStatus.currentEmotion,
                        isLeftEye = true
                    )

                    // Right Eye
                    drawPetEye(
                        center = Offset(cx + eyeDistance, eyeOffsetY),
                        radiusX = eyeRadiusX,
                        radiusY = eyeRadiusY,
                        isBlinking = isBlinking,
                        gazeX = animatedGazeX,
                        gazeY = animatedGazeY,
                        isThinking = petStatus.isThinking,
                        emotion = petStatus.currentEmotion,
                        isLeftEye = false
                    )

                    // Cute Animated Mouth
                    val mouthY = faceCenterY + (baseRadius * 0.22f)
                    drawPetMouth(
                        center = Offset(cx, mouthY),
                        emotion = petStatus.currentEmotion,
                        isSpeaking = petStatus.isSpeaking,
                        talkAmount = mouthTalkProgress,
                        baseRadius = baseRadius
                    )

                    // Equipped 3D Wearable Accessory (Crown, Sprout, Headphones, Hat, Halo, etc.)
                    if (petStatus.activeAccessory != "NONE") {
                        drawPetAccessory(
                            cx = cx,
                            cy = cy,
                            baseRadius = baseRadius,
                            accessoryId = petStatus.activeAccessory
                        )
                    }
                }
            }

            // 4. Floating Particles (Hearts, Sparkles, Stars)
            if (particles.isNotEmpty()) {
                for (p in particles) {
                    translate(left = cx + p.x, top = cy + p.y) {
                        val particleColor = p.color.copy(alpha = p.alpha)
                        when (p.type) {
                            "HEART" -> drawHeartParticle(p.size, particleColor)
                            "SPARKLE" -> drawSparkleParticle(p.size, particleColor, p.rotation)
                            "STAR" -> drawStarParticle(p.size, particleColor, p.rotation)
                            else -> drawCircle(particleColor, radius = p.size * 0.5f)
                        }
                    }
                }
            }
        }
    }
}
