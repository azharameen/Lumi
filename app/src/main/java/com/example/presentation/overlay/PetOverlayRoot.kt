package com.example.presentation.overlay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainActivity
import com.example.core.theme.*
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.repository.LumiRepository
import com.example.presentation.overlay.components.OverlaySpeechBubble
import com.example.presentation.pet.LumiPetView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Clean & Reactive Voice-First Pet Overlay Root.
 * - Single Tap: Directly activates Voice Input & Pets Lumi (microphones turns on instantly).
 * - Long Press: Triggers haptic feedback and opens the Floating Fidget Popover window.
 * - Anchored Layout: Popovers & Speech Bubbles float adaptively without EVER shifting the pet's position!
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PetOverlayRoot(
    context: Context,
    repository: LumiRepository,
    isDockedPeeking: Boolean = false,
    windowY: Int = 300,
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
    var showFidgetPopover by remember { mutableStateOf(false) }

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
            delay(7000)
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
        try {
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
        } catch (_: Exception) {
            isListening = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
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

    val isNearTopEdge = windowY < 200

    Box(
        modifier = Modifier
            .width(220.dp)
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Anchored Floating Lum Pet (Position NEVER shifts when popups open)
        var prevRawX by remember { mutableFloatStateOf(0f) }
        var longPressJob by remember { mutableStateOf<Job?>(null) }
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

                            // Schedule Long Press for Floating Fidget Popover
                            longPressJob?.cancel()
                            longPressJob = coroutineScope.launch {
                                delay(420)
                                showFidgetPopover = !showFidgetPopover
                                try {
                                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        vibrator?.vibrate(android.os.VibrationEffect.createOneShot(45, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                    }
                                } catch (_: Exception) {}
                            }
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            onDragMove(motionEvent.rawX, motionEvent.rawY)
                            val deltaScreenX = motionEvent.rawX - prevRawX
                            prevRawX = motionEvent.rawX
                            if (abs(deltaScreenX) > 4f) {
                                longPressJob?.cancel()
                                coroutineScope.launch {
                                    petRotation.animateTo(if (deltaScreenX > 0) 6f else -6f, tween(40))
                                }
                            }
                            externalGazeX = ((motionEvent.x - petCenterPx) / petCenterPx).coerceIn(-1f, 1f)
                            externalGazeY = ((motionEvent.y - petCenterPx) / petCenterPx).coerceIn(-1f, 1f)
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            longPressJob?.cancel()
                            val wasDragging = onDragEnd()
                            externalGazeX = 0f
                            externalGazeY = 0f
                            coroutineScope.launch {
                                petRotation.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 300f))
                            }

                            onPetTapped()

                            if (!wasDragging) {
                                // SINGLE TAP: Turn on Microphone & Pet Lumi
                                coroutineScope.launch {
                                    repository.petTheCharacter()
                                    petScale.animateTo(1.22f, tween(80))
                                    petScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 400f))
                                }
                                if (isListening) {
                                    stopListening()
                                } else {
                                    startListening()
                                }
                            }
                            true
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            longPressJob?.cancel()
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

        // 2. Absolute Floating Speech Bubble & Popover Container (Floating above or below without affecting Pet's anchor)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = if (isNearTopEdge) 110.dp else (-115).dp)
        ) {
            // Floating Fidget Popover Window (Long Press)
            AnimatedVisibility(
                visible = showFidgetPopover,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    color = ObsidianDark.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LumiCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lumi Companion 🌸",
                                color = LumiCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { showFidgetPopover = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Pet Fidget Button
                            Surface(
                                color = LumiPink.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        coroutineScope.launch {
                                            repository.petTheCharacter()
                                            petScale.animateTo(1.25f, tween(100))
                                            petScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 400f))
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Pet", tint = LumiPink, modifier = Modifier.size(20.dp))
                                }
                            }

                            // Feed Fidget Button
                            Surface(
                                color = LumiGold.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        coroutineScope.launch {
                                            repository.feedPet("Sweet Berry")
                                            petScale.animateTo(1.2f, tween(100))
                                            petScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 400f))
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Restaurant, contentDescription = "Feed", tint = LumiGold, modifier = Modifier.size(20.dp))
                                }
                            }

                            // Mic / Voice Talk Button
                            Surface(
                                color = LumiCyan.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        showFidgetPopover = false
                                        if (isListening) stopListening() else startListening()
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice", tint = LumiCyan, modifier = Modifier.size(20.dp))
                                }
                            }

                            // Open App Button
                            Surface(
                                color = LumiViolet.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        showFidgetPopover = false
                                        val launchIntent = Intent(context, MainActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        }
                                        context.startActivity(launchIntent)
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Launch, contentDescription = "Open App", tint = LumiViolet, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Floating Speech & Voice Status Bubble
            OverlaySpeechBubble(
                speechText = activeSpeechBubble,
                isVisible = showSpeechBubble && !showFidgetPopover,
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
        }
    }
}
