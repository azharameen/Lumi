package com.example.presentation.overlay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainActivity
import com.example.core.theme.LumiPink
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.repository.LumiRepository
import com.example.presentation.overlay.components.OverlaySpeechBubble
import com.example.presentation.pet.LumiPetView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing

/**
 * Clean & Reactive Voice-First Pet Overlay Root.
 * - Single Tap: Directly activates Voice Input to speak to Lumi companion.
 * - Double Tap: Opens the full-screen MainActivity application.
 * - Long-drag: Smoothly repositions the companion across the screen with gaze tracking.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PetOverlayRoot(
    context: Context,
    repository: LumiRepository,
    isDockedPeeking: Boolean = false,
    onDragStart: (Float, Float) -> Unit,
    onDragMove: (Float, Float) -> Unit,
    onDragEnd: () -> Boolean,
    onPetTapped: () -> Unit = {},
    onCloseService: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    val petStatus by repository.petStatus.collectAsStateWithLifecycle(
        initialValue = PetStatus(
            name = "Lumi",
            level = 1,
            exp = 0,
            happiness = 100,
            energy = 100,
            currentEmotion = PetEmotion.HAPPY
        )
    )
    val messages by repository.chatMessages.collectAsStateWithLifecycle(initialValue = emptyList())

    // Voice Input Speech Recognizer Setup
    var isListening by remember { mutableStateOf(false) }
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    // Floating Speech Bubble synchronized with petStatus and chat messages
    val activeSpeechBubble = petStatus.speechBubbleText ?: messages.lastOrNull { it.sender == "LUMI" }?.content
    var showSpeechBubble by remember { mutableStateOf(false) }

    LaunchedEffect(activeSpeechBubble) {
        if (!activeSpeechBubble.isNullOrBlank()) {
            showSpeechBubble = true
            delay(8000)
            showSpeechBubble = false
        }
    }

    // Interactive Petting & Wiggle Physics
    val petScale = remember { Animatable(1f) }
    val petRotation = remember { Animatable(0f) }
    val petOffsetDeltaY = remember { Animatable(0f) }

    // Gaze tracking for cursor / finger
    var externalGazeX by remember { mutableFloatStateOf(0f) }
    var externalGazeY by remember { mutableFloatStateOf(0f) }

    // Natural companion idle jumps
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(14000, 24000))
            if (!petStatus.isSpeaking && !petStatus.isThinking && !isListening) {
                petOffsetDeltaY.animateTo(-14f, tween(180))
                petOffsetDeltaY.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 300f))
            }
        }
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                coroutineScope.launch { repository.setListening(true) }
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }
            override fun onError(error: Int) {
                isListening = false
                coroutineScope.launch { repository.setListening(false) }
            }
            override fun onResults(results: Bundle?) {
                isListening = false
                coroutineScope.launch { repository.setListening(false) }
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val voiceText = matches?.firstOrNull()
                if (!voiceText.isNullOrBlank()) {
                    coroutineScope.launch {
                        repository.setPetEmotion(PetEmotion.THINKING)
                        showSpeechBubble = true
                        repository.sendMessage(voiceText)
                        // Trigger joyful bounce upon sending
                        petScale.animateTo(1.18f, tween(100))
                        petScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 350f))
                    }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer?.setRecognitionListener(listener)

        onDispose {
            speechRecognizer?.destroy()
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
        isListening = true
        coroutineScope.launch {
            repository.setListening(true)
            repository.setPetEmotion(PetEmotion.ENERGETIC)
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        coroutineScope.launch { repository.setListening(false) }
    }

    // Pulsing aura animation when listening
    val infiniteTransition = rememberInfiniteTransition(label = "listeningPulse")
    val listeningPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val listeningPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .width(180.dp)
            .padding(horizontal = MaterialTheme.spacing.extraSmall),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Floating Speech & Voice Status Bubble
            OverlaySpeechBubble(
                speechText = activeSpeechBubble,
                isVisible = showSpeechBubble,
                isListening = isListening,
                isThinking = petStatus.isThinking,
                onBubbleClicked = {
                    if (isListening) {
                        stopListening()
                    } else {
                        showSpeechBubble = false
                    }
                }
            )

            // Compact Floating Lumi Pet
            var lastTapTime by remember { mutableStateOf(0L) }
            var prevRawX by remember { mutableFloatStateOf(0f) }
            val density = LocalDensity.current
            val petCenterPx = with(density) { 46.dp.toPx() }

            val petAlpha by animateFloatAsState(
                targetValue = if (isDockedPeeking && !isListening) 0.88f else 1f,
                animationSpec = tween(350),
                label = "petAlpha"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(94.dp)
                    .offset { IntOffset(0, petOffsetDeltaY.value.roundToInt()) }
                    .graphicsLayer {
                        scaleX = petScale.value
                        scaleY = petScale.value
                        rotationZ = petRotation.value
                        alpha = petAlpha
                    }
                    .pointerInteropFilter { motionEvent ->
                        when (motionEvent.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                prevRawX = motionEvent.rawX
                                onDragStart(motionEvent.rawX, motionEvent.rawY)
                                externalGazeX = ((motionEvent.x - petCenterPx) / petCenterPx).coerceIn(-1f, 1f)
                                externalGazeY = ((motionEvent.y - petCenterPx) / petCenterPx).coerceIn(-1f, 1f)
                                true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                onDragMove(motionEvent.rawX, motionEvent.rawY)
                                val deltaScreenX = motionEvent.rawX - prevRawX
                                prevRawX = motionEvent.rawX
                                if (abs(deltaScreenX) > 2f) {
                                    coroutineScope.launch {
                                        petRotation.animateTo(if (deltaScreenX > 0) 6f else -6f, tween(40))
                                    }
                                }
                                externalGazeX = ((motionEvent.x - petCenterPx) / petCenterPx).coerceIn(-1f, 1f)
                                externalGazeY = ((motionEvent.y - petCenterPx) / petCenterPx).coerceIn(-1f, 1f)
                                true
                            }
                            MotionEvent.ACTION_UP -> {
                                val wasDragging = onDragEnd()
                                externalGazeX = 0f
                                externalGazeY = 0f
                                coroutineScope.launch {
                                    petRotation.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 300f))
                                }

                                onPetTapped()

                                if (!wasDragging) {
                                    val now = System.currentTimeMillis()
                                    val timeDelta = now - lastTapTime
                                    if (timeDelta in 40..380) {
                                        // DOUBLE TAP: Open the main application!
                                        lastTapTime = 0L
                                        val launchIntent = Intent(context, MainActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        }
                                        context.startActivity(launchIntent)
                                    } else {
                                        // SINGLE TAP: Speak to the pet! Trigger voice listening directly!
                                        lastTapTime = now
                                        coroutineScope.launch {
                                            repository.petTheCharacter()
                                            petScale.animateTo(1.2f, tween(80))
                                            petScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 400f))
                                        }
                                        if (isListening) {
                                            stopListening()
                                        } else {
                                            startListening()
                                        }
                                    }
                                }
                                true
                            }
                            MotionEvent.ACTION_CANCEL -> {
                                onDragEnd()
                                externalGazeX = 0f
                                externalGazeY = 0f
                                coroutineScope.launch {
                                    petRotation.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 300f))
                                }
                                true
                            }
                            else -> false
                        }
                    }
            ) {
                // Voice listening glowing ripple effect
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(listeningPulseScale)
                            .background(LumiPink.copy(alpha = listeningPulseAlpha * 0.4f), CircleShape)
                            .border(1.5.dp, LumiPink.copy(alpha = listeningPulseAlpha), CircleShape)
                    )
                }

                LumiPetView(
                    petStatus = petStatus,
                    size = 88.dp,
                    enableInternalGestures = false,
                    externalGazeX = externalGazeX,
                    externalGazeY = externalGazeY,
                    onPetTouched = {
                        coroutineScope.launch { repository.petTheCharacter() }
                    },
                    onPetPetted = {
                        coroutineScope.launch { repository.petTheCharacter() }
                    }
                )
            }
        }
    }
}
