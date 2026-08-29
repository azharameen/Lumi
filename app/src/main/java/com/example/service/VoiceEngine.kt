package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

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

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                // Warm, cheerful pitch & natural speed
                tts?.setPitch(1.25f)
                tts?.setSpeechRate(1.05f)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        _audioWaveformLevel.value = 0.75f
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _audioWaveformLevel.value = 0.1f
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _audioWaveformLevel.value = 0.1f
                    }
                })
            }
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isTtsReady || tts == null) return
        val cleanText = text.replace(Regex("[*#_`~]"), "") // Strip markdown formatting
        val utteranceId = System.currentTimeMillis().toString()
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        _audioWaveformLevel.value = 0.1f
    }

    fun stop() {
        stopSpeaking()
        stopListening()
    }

    fun startListening(onResult: (String) -> Unit) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _recognizedText.value = ""
                    }

                    override fun onBeginningOfSpeech() {
                        _audioWaveformLevel.value = 0.6f
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS level for waveform
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

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                startListening(intent)
            }
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
        _audioWaveformLevel.value = 0.1f
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
