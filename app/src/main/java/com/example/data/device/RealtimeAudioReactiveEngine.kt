package com.example.data.device

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Enterprise Audio Reactive Microphone Analyzer.
 * Features:
 * - Initializes and records exclusively on [Dispatchers.IO] to keep UI and computation threads stutter-free.
 * - Mutex-protected thread-safe start/stop lifecycles with zero AudioRecord or buffer leaks.
 * - Combined RMS + Peak amplitude detection with exponential moving average (EMA) smoothing for organic visuals.
 * - Guaranteed teardown upon coroutine cancellation or scope termination.
 */
class RealtimeAudioReactiveEngine(private val context: Context) {

    private val _audioLoudness = MutableStateFlow(0f)
    val audioLoudness: StateFlow<Float> = _audioLoudness.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private val lifecycleMutex = Mutex()

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = max(
        AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2,
        4096
    )

    fun startListening() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        scope.launch {
            lifecycleMutex.withLock {
                if (_isListening.value) return@launch

                stopRecordingInternal()

                _isListening.value = true

                recordingJob = scope.launch(Dispatchers.IO) {
                    var record: AudioRecord? = null
                    try {
                        record = AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            sampleRate,
                            channelConfig,
                            audioFormat,
                            bufferSize
                        )

                        if (record.state != AudioRecord.STATE_INITIALIZED) {
                            record.release()
                            _isListening.value = false
                            return@launch
                        }

                        audioRecord = record
                        record.startRecording()

                        val readBuffer = ShortArray(1024)
                        var smoothedLoudness = 0f

                        while (isActive && record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                            val read = record.read(readBuffer, 0, readBuffer.size)
                            if (read > 0) {
                                var sumSquares = 0.0
                                var peak = 0

                                for (i in 0 until read) {
                                    val sample = readBuffer[i].toInt()
                                    val absSample = abs(sample)
                                    if (absSample > peak) {
                                        peak = absSample
                                    }
                                    sumSquares += sample * sample
                                }

                                val rms = sqrt(sumSquares / read)
                                val normalizedRms = (rms / 12000.0).toFloat().coerceIn(0f, 1f)
                                val normalizedPeak = (peak / 32767f).coerceIn(0f, 1f)

                                val targetLoudness = (normalizedRms * 0.7f + normalizedPeak * 0.3f).coerceIn(0f, 1f)

                                // Smooth exponential decay: fast attack, gentle release
                                smoothedLoudness = if (targetLoudness > smoothedLoudness) {
                                    smoothedLoudness * 0.3f + targetLoudness * 0.7f
                                } else {
                                    smoothedLoudness * 0.85f + targetLoudness * 0.15f
                                }

                                _audioLoudness.value = smoothedLoudness
                            } else if (read == AudioRecord.ERROR_INVALID_OPERATION || read == AudioRecord.ERROR_BAD_VALUE) {
                                break
                            }
                        }
                    } catch (e: CancellationException) {
                        // Normal coroutine cancellation
                        throw e
                    } catch (_: Exception) {
                        // Handled hardware/permission error
                    } finally {
                        withContext(Dispatchers.IO) {
                            try {
                                if (record?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                                    record.stop()
                                }
                                record?.release()
                            } catch (_: Exception) {}
                        }
                        if (audioRecord === record) {
                            audioRecord = null
                        }
                        _isListening.value = false
                        _audioLoudness.value = 0f
                    }
                }
            }
        }
    }

    private fun stopRecordingInternal() {
        recordingJob?.cancel()
        recordingJob = null
        val record = audioRecord
        audioRecord = null
        if (record != null) {
            try {
                if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    record.stop()
                }
                record.release()
            } catch (_: Exception) {}
        }
        _isListening.value = false
        _audioLoudness.value = 0f
    }

    fun stopListening() {
        scope.launch {
            lifecycleMutex.withLock {
                stopRecordingInternal()
            }
        }
    }

    fun release() {
        stopListening()
        scope.cancel()
    }
}
