package com.example.service

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

/**
 * Direct Audio Record Amplitude analyzer.
 * Streams real-time audio loudness levels (0f..1f) so Lumi's aura and expressions
 * can pulse and react organically to speech, ambient sound, or music.
 */
class RealtimeAudioReactiveEngine(private val context: Context) {

    private val _audioLoudness = MutableStateFlow(0f)
    val audioLoudness: StateFlow<Float> = _audioLoudness.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = max(
        AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat),
        2048
    )

    fun startListening() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return
            }

            audioRecord?.startRecording()

            recordingJob = scope.launch {
                val buffer = ShortArray(bufferSize)
                while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    if (read > 0) {
                        var maxAmplitude = 0
                        for (i in 0 until read) {
                            val amp = abs(buffer[i].toInt())
                            if (amp > maxAmplitude) {
                                maxAmplitude = amp
                            }
                        }
                        // Normalize 0.0 to 1.0
                        val normalized = (maxAmplitude / 32767f * 2.5f).coerceIn(0f, 1f)
                        _audioLoudness.value = normalized
                    }
                    delay(30)
                }
            }
        } catch (e: Exception) {
            // Handled
        }
    }

    fun stopListening() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            // Handled
        }
        _audioLoudness.value = 0f
    }
}
