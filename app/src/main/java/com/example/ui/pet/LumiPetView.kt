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
import com.example.ui.pet.drawers.drawPetAccessory
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
 * Living Jetpack Compose view representing Lumi with physics, procedural breathing,
 * interactive gaze tracking, emotional transformations, and particle bursts.
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
    val animatedGazeX by animateFloatAsState(targetValue = activeGazeX, animationSpec = spring(stiffness = 300f), label = "GazeX")
    val animatedGazeY by animateFloatAsState(targetValue = activeGazeY, animationSpec = spring(stiffness = 300f), label = "GazeY")

    // Interactive squish & bounce physics
    val squishX = remember { Animatable(1f) }
    val squishY = remember { Animatable(1f) }
    val jumpOffsetY = remember { Animatable(0f) }
    val rotationZ = remember { Animatable(0f) }

    // Natural periodic blinking
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2800, 5200))
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
    val infiniteTransition = rememberInfiniteTransition(label = "PetPhysics")
    val breathProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Breath"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Float"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Aura"
    )

    val mouthTalkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(160, easing = LinearEasing),
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
                    p.alpha -= 0.022f
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
            val speed = Random.nextFloat() * 4.5f + 1.5f
            particles.add(
                PetParticle(
                    x = 0f,
                    y = -20f,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed - 2.5f).toFloat(),
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
                            squishX.animateTo(1.22f, tween(90))
                            squishX.animateTo(0.92f, tween(110))
                            squishX.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 400f))
                        }
                        coroutineScope.launch {
                            squishY.animateTo(0.82f, tween(90))
                            squishY.animateTo(1.15f, tween(110))
                            squishY.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 400f))
                        }
                        coroutineScope.launch {
                            jumpOffsetY.animateTo(-28f, tween(120))
                            jumpOffsetY.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 300f))
                        }
                        spawnParticles("HEART", 4, Color(0xFFFF70A6))
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

                        if (Math.abs(dragAmount.x) > 3 || Math.abs(dragAmount.y) > 3) {
                            if (Random.nextFloat() > 0.75f) {
                                onPetPetted()
                                spawnParticles("HEART", 2, Color(0xFFFF70A6))
                            }
                        }
                    },
                    onDragEnd = {
                        targetGazeX = 0f
                        targetGazeY = 0f
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
            val cy = this.size.height / 2f + floatingOffset + jumpOffsetY.value
            val baseRadius = (this.size.width / 2.7f) * (0.94f + breathProgress * 0.08f)

            // Setup color palette based on PetEmotion
            val (gradStart, gradMid, gradEnd, auraColor) = when (petStatus.currentEmotion) {
                PetEmotion.HAPPY -> listOf(Color(0xFF80F5FF), Color(0xFF9D65FF), Color(0xFFFF70A6), Color(0x4000F0FF))
                PetEmotion.CALM -> listOf(Color(0xFF80FFDB), Color(0xFF48CAE4), Color(0xFF0077B6), Color(0x3506D6A0))
                PetEmotion.ENERGETIC -> listOf(Color(0xFFFFD166), Color(0xFFFF9E00), Color(0xFFFF5400), Color(0x45FFD166))
                PetEmotion.SLEEPY -> listOf(Color(0xFFD8B4FE), Color(0xFF818CF8), Color(0xFF312E81), Color(0x35818CF8))
                PetEmotion.THINKING -> listOf(Color(0xFFC084FC), Color(0xFF7E22CE), Color(0xFF3B82F6), Color(0x409D65FF))
                PetEmotion.LOVING -> listOf(Color(0xFFFFB3C6), Color(0xFFFF4D6D), Color(0xFFC9184A), Color(0x45FF70A6))
                PetEmotion.PLAYFUL -> listOf(Color(0xFFFF70A6), Color(0xFFFFD166), Color(0xFF06D6A0), Color(0x40FF70A6))
                PetEmotion.CONCERNED -> listOf(Color(0xFFBAE6FD), Color(0xFF38BDF8), Color(0xFF0284C7), Color(0x3538BDF8))
            }

            // 1. Aura Glow
            drawPetAura(cx, cy, baseRadius, auraPulse, auraColor)

            // 2. Soft Drop Shadow
            drawPetShadow(cx, this.size.height, baseRadius, squishX.value, squishY.value)

            // 3. Main Pet Avatar with squish and rotation physics
            scale(scaleX = squishX.value, scaleY = squishY.value, pivot = Offset(cx, cy + baseRadius)) {
                rotate(degrees = rotationZ.value, pivot = Offset(cx, cy)) {
                    // Shaded Body
                    drawPetBody(cx, cy, baseRadius, gradStart, gradMid, gradEnd)

                    // Rosy Cheeks
                    drawPetCheeks(cx, cy, baseRadius, petStatus.currentEmotion)

                    // Eyes
                    val eyeDistance = baseRadius * 0.34f
                    val eyeOffsetY = cy - baseRadius * 0.08f
                    val eyeRadiusX = baseRadius * 0.13f
                    val eyeRadiusY = if (isBlinking || petStatus.currentEmotion == PetEmotion.SLEEPY) 2.2f else baseRadius * 0.15f

                    drawPetEye(
                        center = Offset(cx - eyeDistance, eyeOffsetY),
                        radiusX = eyeRadiusX,
                        radiusY = eyeRadiusY,
                        isBlinking = isBlinking || petStatus.currentEmotion == PetEmotion.SLEEPY,
                        gazeX = animatedGazeX,
                        gazeY = animatedGazeY,
                        isThinking = petStatus.isThinking
                    )

                    drawPetEye(
                        center = Offset(cx + eyeDistance, eyeOffsetY),
                        radiusX = eyeRadiusX,
                        radiusY = eyeRadiusY,
                        isBlinking = isBlinking || petStatus.currentEmotion == PetEmotion.SLEEPY,
                        gazeX = animatedGazeX,
                        gazeY = animatedGazeY,
                        isThinking = petStatus.isThinking
                    )

                    // Mouth
                    val mouthY = cy + baseRadius * 0.22f
                    drawPetMouth(
                        center = Offset(cx, mouthY),
                        emotion = petStatus.currentEmotion,
                        isSpeaking = petStatus.isSpeaking,
                        talkAmount = mouthTalkProgress,
                        baseRadius = baseRadius
                    )

                    // Wearable Accessories
                    drawPetAccessory(
                        accessory = petStatus.activeAccessory,
                        cx = cx,
                        cy = cy,
                        baseRadius = baseRadius,
                        breathProgress = breathProgress
                    )
                }
            }

            // 4. Floating Active Particles
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
