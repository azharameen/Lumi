package com.example.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

enum class SoundscapeType(val title: String, val description: String, val iconEmoji: String) {
    BINAURAL_FOCUS("40Hz Gamma Binaural", "Synchronizes brainwaves for deep cognitive focus", "🧠"),
    RAIN_ON_LEAVES("Forest Rainstorm", "Natural pink-noise rainfall for steady relaxation", "🌧️"),
    ZEN_OCEAN_WAVES("Deep Ocean Waves", "Slow oscillating low-frequency wave swells", "🌊"),
    CAMPFIRE_CRACKLE("Cozy Campfire", "Warm ambient crackling hearth for calming anxiety", "🔥"),
    LOFI_STUDY_ROOM("Lo-Fi Ambience", "Subtle acoustic vinyl warmth & gentle harmonic hum", "☕")
}

data class SoundscapeState(
    val isPlaying: Boolean = false,
    val activeType: SoundscapeType = SoundscapeType.BINAURAL_FOCUS,
    val volume: Float = 0.65f,
    val remainingSeconds: Int = 1500, // 25 min default
    val totalSeconds: Int = 1500,
    val isTimerActive: Boolean = false
)

class ProceduralSoundscapeEngine private constructor() {

    private val _state = MutableStateFlow(SoundscapeState())
    val state: StateFlow<SoundscapeState> = _state.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private val random = Random()

    fun setSoundscape(type: SoundscapeType) {
        _state.value = _state.value.copy(activeType = type)
        if (_state.value.isPlaying) {
            startSoundscape(type)
        }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _state.value = _state.value.copy(volume = clamped)
        audioTrack?.setVolume(clamped)
    }

    fun startSoundscape(type: SoundscapeType = _state.value.activeType) {
        stopSoundscape()

        _state.value = _state.value.copy(
            isPlaying = true,
            activeType = type
        )

        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        audioTrack?.setVolume(_state.value.volume)

        playbackJob = scope.launch {
            val buffer = ShortArray(bufferSize / 2)
            var phaseLeft = 0.0
            var phaseRight = 0.0
            var waveAngle = 0.0
            var brownianValue = 0.0

            while (isActive && _state.value.isPlaying) {
                when (type) {
                    SoundscapeType.BINAURAL_FOCUS -> {
                        // 220Hz carrier on left, 260Hz carrier on right -> 40Hz Gamma binaural difference
                        val freqLeft = 220.0
                        val freqRight = 260.0
                        val phaseIncLeft = 2.0 * Math.PI * freqLeft / sampleRate
                        val phaseIncRight = 2.0 * Math.PI * freqRight / sampleRate

                        for (i in 0 until buffer.size step 2) {
                            val leftSample = (sin(phaseLeft) * 10000).toInt().toShort()
                            val rightSample = (sin(phaseRight) * 10000).toInt().toShort()
                            buffer[i] = leftSample
                            if (i + 1 < buffer.size) buffer[i + 1] = rightSample
                            phaseLeft += phaseIncLeft
                            phaseRight += phaseIncRight
                        }
                    }
                    SoundscapeType.RAIN_ON_LEAVES -> {
                        // Filtered pink / brown noise generator with occasional droplet crackles
                        for (i in 0 until buffer.size step 2) {
                            val white = (random.nextGaussian() * 2500).toInt()
                            brownianValue = (brownianValue * 0.95) + (white * 0.05)
                            val rainSample = brownianValue.toInt().coerceIn(-30000, 30000).toShort()
                            buffer[i] = rainSample
                            if (i + 1 < buffer.size) buffer[i + 1] = rainSample
                        }
                    }
                    SoundscapeType.ZEN_OCEAN_WAVES -> {
                        // Modulated noise with slow 0.1Hz sine envelope mimicking ocean swells
                        for (i in 0 until buffer.size step 2) {
                            val swellEnvelope = (sin(waveAngle) + 1.0) / 2.0 // 0 to 1
                            val noise = (random.nextGaussian() * 6000 * swellEnvelope).toInt()
                            val waveSample = noise.coerceIn(-30000, 30000).toShort()
                            buffer[i] = waveSample
                            if (i + 1 < buffer.size) buffer[i + 1] = waveSample
                            waveAngle += 2.0 * Math.PI * 0.08 / sampleRate
                        }
                    }
                    SoundscapeType.CAMPFIRE_CRACKLE -> {
                        // Warm low rumble + random sharp pops
                        for (i in 0 until buffer.size step 2) {
                            val isPop = random.nextDouble() < 0.0008
                            val sample = if (isPop) {
                                (random.nextDouble() * 24000).toInt().toShort()
                            } else {
                                (random.nextGaussian() * 1200).toInt().toShort()
                            }
                            buffer[i] = sample
                            if (i + 1 < buffer.size) buffer[i + 1] = sample
                        }
                    }
                    SoundscapeType.LOFI_STUDY_ROOM -> {
                        // Warm 110Hz gentle chord + subtle vinyl crackle
                        val phaseInc = 2.0 * Math.PI * 110.0 / sampleRate
                        for (i in 0 until buffer.size step 2) {
                            val drone = (sin(phaseLeft) * 4500).toInt()
                            val crackle = if (random.nextDouble() < 0.002) (random.nextGaussian() * 8000).toInt() else 0
                            val sample = (drone + crackle).coerceIn(-30000, 30000).toShort()
                            buffer[i] = sample
                            if (i + 1 < buffer.size) buffer[i + 1] = sample
                            phaseLeft += phaseInc
                        }
                    }
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stopSoundscape() {
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        _state.value = _state.value.copy(isPlaying = false)
    }

    fun startFocusTimer(minutes: Int) {
        timerJob?.cancel()
        val totalSec = minutes * 60
        _state.value = _state.value.copy(
            totalSeconds = totalSec,
            remainingSeconds = totalSec,
            isTimerActive = true
        )
        if (!_state.value.isPlaying) {
            startSoundscape(_state.value.activeType)
        }

        timerJob = scope.launch {
            while (isActive && _state.value.remainingSeconds > 0 && _state.value.isTimerActive) {
                delay(1000)
                val remaining = _state.value.remainingSeconds - 1
                _state.value = _state.value.copy(remainingSeconds = remaining)
                if (remaining <= 0) {
                    stopSoundscape()
                    _state.value = _state.value.copy(isTimerActive = false)
                }
            }
        }
    }

    fun stopFocusTimer() {
        timerJob?.cancel()
        timerJob = null
        _state.value = _state.value.copy(isTimerActive = false)
        stopSoundscape()
    }

    companion object {
        @Volatile
        private var instance: ProceduralSoundscapeEngine? = null

        fun getInstance(): ProceduralSoundscapeEngine {
            return instance ?: synchronized(this) {
                instance ?: ProceduralSoundscapeEngine().also { instance = it }
            }
        }
    }
}
