package com.example.ui.overlay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.MainActivity
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.repository.LumiRepository
import com.example.ui.overlay.components.OverlayHubCard
import com.example.ui.overlay.components.OverlaySpeechBubble
import com.example.ui.overlay.models.OverlayTab
import com.example.ui.pet.LumiPetView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Root Composable for the system overlay window.
 * Handles state for hub expansion, breathing loops, voice interaction, and position updates.
 */
@Composable
fun PetOverlayRoot(
    context: Context,
    repository: LumiRepository,
    onMoveOverlay: (Float, Float) -> Unit,
    onCloseService: () -> Unit,
    onToggleRoamMode: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val petStatus by repository.petStatus.collectAsState(
        initial = PetStatus(
            name = "Lumi",
            level = 1,
            exp = 0,
            happiness = 100,
            energy = 100,
            currentEmotion = PetEmotion.HAPPY
        )
    )
    val tasks by repository.allTasks.collectAsState(initial = emptyList())
    val messages by repository.chatMessages.collectAsState(initial = emptyList())

    // UI Expansion and active tab state
    var isHubExpanded by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(OverlayTab.QUICK_MENU) }
    var isRoamMode by remember { mutableStateOf(false) }

    // Breathing Session in Hub
    var isBreathingActive by remember { mutableStateOf(false) }
    var breathingPhase by remember { mutableStateOf("Inhale (4s)") }
    var breathingProgress by remember { mutableFloatStateOf(0f) }

    // Floating Speech Bubble synchronized with petStatus and chat messages
    val activeSpeechBubble = petStatus.speechBubbleText ?: messages.lastOrNull { it.sender == "LUMI" }?.content
    var showSpeechBubble by remember { mutableStateOf(false) }

    LaunchedEffect(activeSpeechBubble) {
        if (!activeSpeechBubble.isNullOrBlank()) {
            showSpeechBubble = true
            delay(7500)
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

    // Overlay handle drag state
    var isHandleDragging by remember { mutableStateOf(false) }

    // Natural companion idle triggers
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(14000, 24000))
            if (!petStatus.isSpeaking && !petStatus.isThinking && !isHubExpanded) {
                petOffsetDeltaY.animateTo(-14f, tween(180))
                petOffsetDeltaY.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 300f))
            }
        }
    }

    // 4-7-8 Breathing Loop
    LaunchedEffect(isBreathingActive) {
        if (isBreathingActive) {
            repository.setPetEmotion(PetEmotion.CALM)
            while (isBreathingActive) {
                // Inhale 4s
                breathingPhase = "Inhale through nose (4s)"
                val inhaleSteps = 40
                for (i in 0..inhaleSteps) {
                    if (!isBreathingActive) break
                    breathingProgress = i / inhaleSteps.toFloat()
                    delay(100)
                }
                // Hold 7s
                breathingPhase = "Hold breath gently (7s)"
                val holdSteps = 70
                for (i in 0..holdSteps) {
                    if (!isBreathingActive) break
                    delay(100)
                }
                // Exhale 8s
                breathingPhase = "Exhale slowly through mouth (8s)"
                val exhaleSteps = 80
                for (i in 0..exhaleSteps) {
                    if (!isBreathingActive) break
                    breathingProgress = 1f - (i / exhaleSteps.toFloat())
                    delay(100)
                }
            }
        }
    }

    // Voice Input Speech Recognizer Setup
    var isListening by remember { mutableStateOf(false) }
    var askInputText by remember { mutableStateOf("") }
    var isSendingAsk by remember { mutableStateOf(false) }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListening = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
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
                        repository.sendMessage(voiceText)
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
        coroutineScope.launch { repository.setListening(true) }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        coroutineScope.launch { repository.setListening(false) }
    }

    Box(
        modifier = Modifier
            .width(if (isHubExpanded) 280.dp else 125.dp)
            .padding(4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. Floating Speech Bubble
            OverlaySpeechBubble(
                speechText = activeSpeechBubble,
                isVisible = showSpeechBubble && !isHubExpanded,
                onBubbleClicked = { isHubExpanded = true }
            )

            // 2. Expanded Companion Hub Card
            OverlayHubCard(
                isExpanded = isHubExpanded,
                petStatus = petStatus,
                activeTab = activeTab,
                onTabSelected = { activeTab = it },
                tasks = tasks,
                isListening = isListening,
                isSpeaking = petStatus.isSpeaking,
                isRoamMode = isRoamMode,
                askInputText = askInputText,
                onAskInputChanged = { askInputText = it },
                isSendingPrompt = isSendingAsk,
                onSendPrompt = {
                    if (askInputText.isNotBlank()) {
                        val prompt = askInputText
                        askInputText = ""
                        isSendingAsk = true
                        coroutineScope.launch {
                            repository.sendMessage(prompt)
                            isSendingAsk = false
                        }
                    }
                },
                isBreathingRunning = isBreathingActive,
                breathingPhase = breathingPhase,
                breathingProgress = breathingProgress,
                onToggleBreathing = { isBreathingActive = !isBreathingActive },
                onToggleRoam = {
                    isRoamMode = !isRoamMode
                    onToggleRoamMode(isRoamMode)
                },
                onToggleTaskComplete = { taskId ->
                    coroutineScope.launch { repository.toggleTaskCompleted(taskId, true) }
                },
                onVoiceToggle = {
                    if (isListening) stopListening() else startListening()
                },
                onHydrateClicked = {
                    coroutineScope.launch {
                        repository.logWellness(8, "Hydrated", 8, 1, "Quick hydration logged via Overlay")
                        repository.setPetEmotion(PetEmotion.HAPPY)
                    }
                },
                onPetJoyClicked = {
                    coroutineScope.launch {
                        repository.petTheCharacter()
                        petScale.animateTo(1.22f, tween(90))
                        petScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 400f))
                    }
                },
                onOpenApp = {
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(launchIntent)
                    isHubExpanded = false
                },
                onMinimize = { isHubExpanded = false },
                onCloseService = onCloseService,
                onDragStart = { isHandleDragging = true },
                onDragEnd = { isHandleDragging = false },
                onMoveOverlay = onMoveOverlay
            )

            // 3. Compact Floating Lumi Pet (Directly draggable to reposition overlay)
            var hasDragged by remember { mutableStateOf(false) }
            var totalDragDistance by remember { mutableFloatStateOf(0f) }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(92.dp)
                    .offset { IntOffset(0, petOffsetDeltaY.value.roundToInt()) }
                    .graphicsLayer {
                        scaleX = petScale.value
                        scaleY = petScale.value
                        rotationZ = petRotation.value
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                hasDragged = false
                                totalDragDistance = 0f
                                externalGazeX = (offset.x - 46.dp.toPx()) / 46.dp.toPx()
                                externalGazeY = (offset.y - 46.dp.toPx()) / 46.dp.toPx()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dist = hypot(dragAmount.x, dragAmount.y)
                                totalDragDistance += dist
                                if (totalDragDistance > 6f) {
                                    hasDragged = true
                                    onMoveOverlay(dragAmount.x, dragAmount.y)
                                }

                                externalGazeX = (change.position.x - 46.dp.toPx()) / 46.dp.toPx()
                                externalGazeY = (change.position.y - 46.dp.toPx()) / 46.dp.toPx()

                                if (Math.abs(dragAmount.x) > 2 || Math.abs(dragAmount.y) > 2) {
                                    coroutineScope.launch {
                                        petRotation.animateTo(if (dragAmount.x > 0) 7f else -7f, tween(50))
                                        petRotation.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 300f))
                                    }
                                }
                            },
                            onDragEnd = {
                                if (!hasDragged) {
                                    // Clean tap detected -> toggle expansion / pet reaction
                                    isHubExpanded = !isHubExpanded
                                    coroutineScope.launch {
                                        repository.petTheCharacter()
                                        petScale.animateTo(1.18f, tween(80))
                                        petScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 400f))
                                    }
                                }
                                externalGazeX = 0f
                                externalGazeY = 0f
                                hasDragged = false
                                totalDragDistance = 0f
                            },
                            onDragCancel = {
                                externalGazeX = 0f
                                externalGazeY = 0f
                                hasDragged = false
                                totalDragDistance = 0f
                            }
                        )
                    }
            ) {
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
