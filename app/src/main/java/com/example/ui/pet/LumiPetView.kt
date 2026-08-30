package com.example.ui.pet

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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.pet.drawers.drawHeartParticle
import com.example.ui.pet.drawers.drawPetAura
import com.example.ui.pet.drawers.drawPetBody
import com.example.ui.pet.drawers.drawPetCheeks
import com.example.ui.pet.drawers.drawPetEye
import com.example.ui.pet.drawers.drawPetMouth
import com.example.ui.pet.drawers.drawPetShadow
import com.example.ui.pet.drawers.drawSparkleParticle
import com.example.ui.pet.models.PetParticle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Living Jetpack Compose view representing Bloub / Lumi mascot with organic clay physics,
 * procedural breathing, interactive gaze saccades, emotional expressions, and responsive jelly dynamics.
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
    val animatedGazeX by animateFloatAsState(targetValue = activeGazeX, animationSpec = spring(stiffness = 320f), label = "GazeX")
    val animatedGazeY by animateFloatAsState(targetValue = activeGazeY, animationSpec = spring(stiffness = 320f), label = "GazeY")

    // Interactive squish & jelly bounce physics
    val squishX = remember { Animatable(1f) }
    val squishY = remember { Animatable(1f) }
    val jumpOffsetY = remember { Animatable(0f) }
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

    // Breathing & floating physics
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

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
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

    // Particle manager loop
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

    fun spawnParticles(type: String, count: Int, baseColor: Color) {
        for (i in 0 until count) {
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val speed = Random.nextFloat() * 4.2f + 1.2f
            particles.add(
                PetParticle(
                    x = 0f,
                    y = -15f,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed - 2.8f).toFloat(),
                    alpha = 1.0f,
                    type = type,
                    color = baseColor,
                    size = Random.nextFloat() * 12f + 10f,
                    rotation = Random.nextFloat() * 360f
                )
            )
        }
    }

    val gestureModifier = if (enableInternalGestures) {
        Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onPetTouched()
                        coroutineScope.launch {
                            // Organic jelly squish & wobble
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
                            jumpOffsetY.animateTo(-26f, tween(110))
                            jumpOffsetY.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = 320f))
                        }
                        coroutineScope.launch {
                            rotationZ.animateTo(if (Random.nextBoolean()) 6f else -6f, tween(80))
                            rotationZ.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 300f))
                        }
                        spawnParticles("HEART", 5, Color(petStatus.bloubSkinColor.primaryHex))
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        targetGazeX = (offset.x - 300f) / 300f
                        targetGazeY = (offset.y - 300f) / 300f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        targetGazeX = ((change.position.x - 300f) / 300f).coerceIn(-1f, 1f)
                        targetGazeY = ((change.position.y - 300f) / 300f).coerceIn(-1f, 1f)

                        // Subtle dynamic squish leaning into drag direction
                        val dragIntensityX = (dragAmount.x / 20f).coerceIn(-0.15f, 0.15f)
                        val dragIntensityY = (dragAmount.y / 20f).coerceIn(-0.15f, 0.15f)
                        coroutineScope.launch {
                            squishX.snapTo(1f + dragIntensityX)
                            squishY.snapTo(1f + dragIntensityY)
                        }

                        if (Math.abs(dragAmount.x) > 3 || Math.abs(dragAmount.y) > 3) {
                            if (Random.nextFloat() > 0.72f) {
                                onPetPetted()
                                spawnParticles("HEART", 2, Color(0xFFFF70A6))
                            }
                        }
                    },
                    onDragEnd = {
                        targetGazeX = 0f
                        targetGazeY = 0f
                        coroutineScope.launch {
                            squishX.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 400f))
                            squishY.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 400f))
                        }
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
            val cx = this.size.width / 2f
            val currentFloatY = floatingOffset + jumpOffsetY.value
            val cy = this.size.height / 2f + currentFloatY
            val baseRadius = (this.size.width / 2.7f) * (0.95f + breathProgress * 0.06f)

            // Setup clay gradient colors based on Bloub skin color and emotional lighting
            val skin = petStatus.bloubSkinColor
            val gradStart = Color(skin.primaryHex)
            val gradMid = Color(skin.midHex)
            val gradEnd = Color(skin.endHex)
            val auraColor = Color(skin.glowHex)

            // 1. Atmosphere Aura Glow
            drawPetAura(cx, cy, baseRadius, auraPulse, auraColor)

            // 2. Dynamic Ground Soft Shadow
            drawPetShadow(
                cx = cx,
                canvasHeight = this.size.height,
                baseRadius = baseRadius,
                squishX = squishX.value,
                squishY = squishY.value,
                floatingOffsetY = currentFloatY
            )

            // 3. Main Bloub Character with spring dynamics
            scale(scaleX = squishX.value, scaleY = squishY.value, pivot = Offset(cx, cy + baseRadius)) {
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
                    drawPetCheeks(cx, faceCenterY, baseRadius, petStatus.currentEmotion)

                    // Big Glossy Bloub Eyes
                    val eyeDistance = baseRadius * 0.35f
                    val eyeOffsetY = faceCenterY - baseRadius * 0.08f
                    val eyeRadiusX = baseRadius * 0.14f
                    val eyeRadiusY = if (isBlinking) 2.2f else baseRadius * 0.16f

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
                    val mouthY = faceCenterY + baseRadius * 0.22f
                    drawPetMouth(
                        center = Offset(cx, mouthY),
                        emotion = petStatus.currentEmotion,
                        isSpeaking = petStatus.isSpeaking,
                        talkAmount = mouthTalkProgress,
                        baseRadius = baseRadius
                    )
                }
            }

            // 4. Floating Particles (Hearts, Sparkles)
            for (p in particles) {
                translate(left = cx + p.x, top = cy + p.y) {
                    when (p.type) {
                        "HEART" -> drawHeartParticle(p.size, p.color.copy(alpha = p.alpha))
                        "SPARKLE" -> drawSparkleParticle(p.size, p.color.copy(alpha = p.alpha), p.rotation)
                        else -> drawCircle(p.color.copy(alpha = p.alpha), radius = p.size / 2)
                    }
                }
            }
        }
    }
}
