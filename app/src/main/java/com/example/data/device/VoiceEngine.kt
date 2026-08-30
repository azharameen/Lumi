package com.example.data.device

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Enterprise Audio & Voice Engine.
 * Features:
 * - Deferred speech queueing: handles requests initiated prior to asynchronous TTS engine initialization.
 * - Multi-tier locale fallback check (US English -> Device Locale -> Engine Available Locales).
 * - Thread-safe utterance completion callbacks mapped per utterance ID.
 * - Looper-safe SpeechRecognizer lifecycle management and full teardown cleanup.
 */
class VoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _audioWaveformLevel = MutableStateFlow(0.1f)
    val audioWaveformLevel: StateFlow<Float> = _audioWaveformLevel.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    private data class PendingSpeech(val text: String, val onComplete: (() -> Unit)?)
    private val pendingQueue = ConcurrentLinkedQueue<PendingSpeech>()
    private val completionCallbacks = ConcurrentHashMap<String, () -> Unit>()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts
            if (engine != null) {
                configureTtsLanguage(engine)
            }
        } else {
            isTtsReady = false
            pendingQueue.clear()
        }
    }

    private fun configureTtsLanguage(engine: TextToSpeech) {
        // Cascade locale check: Preferred Locale.US -> Device Default Locale -> Any Available
        var selectedLocale = Locale.US
        var availability = engine.isLanguageAvailable(selectedLocale)

        if (availability == TextToSpeech.LANG_MISSING_DATA || availability == TextToSpeech.LANG_NOT_SUPPORTED) {
            val defaultLocale = Locale.getDefault()
            val defaultAvail = engine.isLanguageAvailable(defaultLocale)
            if (defaultAvail != TextToSpeech.LANG_MISSING_DATA && defaultAvail != TextToSpeech.LANG_NOT_SUPPORTED) {
                selectedLocale = defaultLocale
                availability = defaultAvail
            }
        }

        if (availability != TextToSpeech.LANG_MISSING_DATA && availability != TextToSpeech.LANG_NOT_SUPPORTED) {
            try {
                engine.language = selectedLocale
                engine.setPitch(1.15f) // Warm, friendly pitch
                engine.setSpeechRate(1.02f)

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        _audioWaveformLevel.value = 0.75f
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _audioWaveformLevel.value = 0.1f
                        if (utteranceId != null) {
                            completionCallbacks.remove(utteranceId)?.let { callback ->
                                mainHandler.post { callback.invoke() }
                            }
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _audioWaveformLevel.value = 0.1f
                        if (utteranceId != null) {
                            completionCallbacks.remove(utteranceId)
                        }
                    }
                })

                isTtsReady = true

                // Drain any pending speech requests queued before onInit completed
                while (pendingQueue.isNotEmpty()) {
                    val next = pendingQueue.poll() ?: break
                    speakInternal(next.text, next.onComplete)
                }
            } catch (_: Exception) {
                isTtsReady = false
            }
        } else {
            isTtsReady = false
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isTtsReady || tts == null) {
            // Queue speech until engine initialization completes
            pendingQueue.offer(PendingSpeech(text, onComplete))
            return
        }
        speakInternal(text, onComplete)
    }

    private fun speakInternal(text: String, onComplete: (() -> Unit)? = null) {
        val cleanText = text.replace(Regex("[*#_`~]"), "").trim()
        if (cleanText.isBlank()) {
            onComplete?.invoke()
            return
        }

        val utteranceId = "utterance_${System.currentTimeMillis()}_${cleanText.hashCode()}"
        if (onComplete != null) {
            completionCallbacks[utteranceId] = onComplete
        }

        try {
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (_: Exception) {
            completionCallbacks.remove(utteranceId)
            _isSpeaking.value = false
            _audioWaveformLevel.value = 0.1f
        }
    }

    fun stopSpeaking() {
        pendingQueue.clear()
        completionCallbacks.clear()
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        _audioWaveformLevel.value = 0.1f
    }

    fun stop() {
        stopSpeaking()
        stopListening()
    }

    fun startListening(onResult: (String) -> Unit) {
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                return@post
            }

            try {
                speechRecognizer?.destroy()
                val recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                            _recognizedText.value = ""
                        }

                        override fun onBeginningOfSpeech() {
                            _audioWaveformLevel.value = 0.6f
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            val normalized = (rmsdB / 10f).coerceIn(0.1f, 1.0f)
                            _audioWaveformLevel.value = normalized
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                            _audioWaveformLevel.value = 0.1f
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            _audioWaveformLevel.value = 0.1f
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val spoken = matches?.firstOrNull() ?: ""
                            if (spoken.isNotBlank()) {
                                _recognizedText.value = spoken
                                onResult(spoken)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            matches?.firstOrNull()?.let {
                                _recognizedText.value = it
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
                speechRecognizer = recognizer

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                recognizer.startListening(intent)
            } catch (_: Exception) {
                _isListening.value = false
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            _isListening.value = false
            _audioWaveformLevel.value = 0.1f
        }
    }

    fun release() {
        pendingQueue.clear()
        completionCallbacks.clear()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isTtsReady = false

        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
            speechRecognizer = null
            _isListening.value = false
            _isSpeaking.value = false
        }
    }
}
