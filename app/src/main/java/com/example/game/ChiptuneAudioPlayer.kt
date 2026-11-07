package com.example.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * 8-Bit Retro Chiptune Synthesizer & Lo-Fi Background Music Manager.
 * Features:
 * - Lo-Fi / Synthwave Background Music Loop with bass, chords, and arpeggios
 * - Separate toggles for Background Music (BGM) and Sound Effects (SFX)
 * - Dynamic sound effect synthesizers without external asset files
 */
class ChiptuneAudioPlayer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050

    var isBgmEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                stopBgm()
            } else if (isGamePlaying) {
                startBgm()
            }
        }

    var isSfxEnabled: Boolean = true

    private var isGamePlaying: Boolean = false
    private var bgmJob: Job? = null
    private var bgmTrack: AudioTrack? = null

    // Retro 8-bar Lo-Fi Synthwave progression (frequencies in Hz)
    // Progression: Am7 -> Fmaj7 -> Cmaj7 -> Gsus4
    private val chordRoots = floatArrayOf(
        220.00f, 220.00f, // A3
        174.61f, 174.61f, // F3
        130.81f, 130.81f, // C3
        196.00f, 196.00f  // G3
    )

    // Arpeggio notes for melodic lo-fi counterpoint
    private val arpeggioScales = arrayOf(
        floatArrayOf(440.00f, 523.25f, 659.25f, 783.99f), // A4, C5, E5, G5
        floatArrayOf(349.23f, 440.00f, 523.25f, 659.25f), // F4, A4, C5, E5
        floatArrayOf(261.63f, 329.63f, 392.00f, 523.25f), // C4, E4, G4, C5
        floatArrayOf(392.00f, 440.00f, 523.25f, 587.33f)  // G4, A4, C5, D5
    )

    fun onGameStart() {
        isGamePlaying = true
        if (isBgmEnabled) {
            startBgm()
        }
    }

    fun onGameStop() {
        isGamePlaying = false
        stopBgm()
    }

    fun startBgm() {
        if (bgmJob != null && bgmJob?.isActive == true) return
        bgmJob = scope.launch {
            try {
                val stepMs = 200 // 150 BPM 16th notes / 75 BPM 8th notes
                var step = 0

                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                bgmTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes((minBuf * 2).coerceAtLeast(sampleRate * 2))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                bgmTrack?.play()

                val numSamplesPerStep = (sampleRate * (stepMs / 1000f)).toInt()
                val buffer = ShortArray(numSamplesPerStep)

                while (isActive && isBgmEnabled && isGamePlaying) {
                    val barIndex = (step / 8) % chordRoots.size
                    val bassFreq = chordRoots[barIndex]
                    val arpPattern = arpeggioScales[barIndex % arpeggioScales.size]
                    val leadFreq = arpPattern[step % arpPattern.size]
                    val isKick = (step % 4 == 0)
                    val isHiHat = (step % 2 == 1)

                    for (i in 0 until numSamplesPerStep) {
                        val t = i.toFloat() / sampleRate.toFloat()
                        val progress = i.toFloat() / numSamplesPerStep.toFloat()

                        // 1. Warm Triangle Bass
                        val bassPhase = (t * (bassFreq * 0.5f)) % 1.0f
                        val bassSample = (2.0f * kotlin.math.abs(2.0f * (bassPhase - 0.5f)) - 1.0f) * 0.16f

                        // 2. Lo-Fi Chiptune Soft Square/Sine Melody
                        val leadPhase = (t * leadFreq) % 1.0f
                        val leadSample = sin(2.0 * Math.PI * leadPhase).toFloat() * 0.08f * (1.0f - progress * 0.7f)

                        // 3. Retro Lo-Fi Beat (Soft Drum Click & Hat)
                        var drumSample = 0.0f
                        if (isKick && progress < 0.25f) {
                            val kickFreq = 120.0f * (1.0f - progress * 3.5f).coerceAtLeast(0.1f)
                            drumSample += sin(2.0 * Math.PI * (t * kickFreq)).toFloat() * 0.22f * (1.0f - progress * 4.0f)
                        }
                        if (isHiHat && progress < 0.15f) {
                            // White noise burst
                            val noise = ((i * 1103515245 + 12345) and 0x7FFFFFFF) / 0x7FFFFFFF.toFloat() - 0.5f
                            drumSample += noise * 0.06f * (1.0f - progress * 6.0f)
                        }

                        val mixed = (bassSample + leadSample + drumSample).coerceIn(-1.0f, 1.0f)
                        buffer[i] = (mixed * 32767f * 0.65f).toInt().toShort()
                    }

                    bgmTrack?.write(buffer, 0, buffer.size)
                    step++
                }
            } catch (_: Exception) {
                // Ignore audio synthesis exceptions safely
            } finally {
                stopBgmTrack()
            }
        }
    }

    fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
        stopBgmTrack()
    }

    private fun stopBgmTrack() {
        try {
            bgmTrack?.pause()
            bgmTrack?.flush()
            bgmTrack?.stop()
            bgmTrack?.release()
            bgmTrack = null
        } catch (_: Exception) {}
    }

    // --- SOUND EFFECTS (SFX) SYNTHESIS ---

    private fun playSfxPcm(generator: (Int, Int) -> Short, durationMs: Int) {
        if (!isSfxEnabled) return
        scope.launch {
            try {
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                if (numSamples <= 0) return@launch
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    buffer[i] = generator(i, numSamples)
                }

                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2.coerceAtLeast(minBuf))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                audioTrack.setNotificationMarkerPosition(buffer.size)
                audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onMarkerReached(track: AudioTrack?) {
                        track?.release()
                    }
                    override fun onPeriodicNotification(track: AudioTrack?) {}
                })
            } catch (_: Exception) {
                // Ignore audio synthesis exceptions safely
            }
        }
    }

    fun playJump() {
        playToneSlide(startFreq = 260f, endFreq = 540f, durationMs = 120, waveType = WaveType.SQUARE, volume = 0.22f)
    }

    fun playDoubleJump() {
        playToneSlide(startFreq = 380f, endFreq = 780f, durationMs = 140, waveType = WaveType.SQUARE, volume = 0.22f)
    }

    fun playSlide() {
        playToneSlide(startFreq = 160f, endFreq = 70f, durationMs = 130, waveType = WaveType.SAWTOOTH, volume = 0.20f)
    }

    fun playVault() {
        playToneSlide(startFreq = 210f, endFreq = 480f, durationMs = 130, waveType = WaveType.TRIANGLE, volume = 0.25f)
    }

    fun playDash() {
        playToneSlide(startFreq = 520f, endFreq = 160f, durationMs = 150, waveType = WaveType.SAWTOOTH, volume = 0.22f)
    }

    fun playGlass() {
        playToneSlide(startFreq = 960f, endFreq = 320f, durationMs = 110, waveType = WaveType.SQUARE, volume = 0.24f)
    }

    fun playAmbush() {
        playToneSlide(startFreq = 840f, endFreq = 1250f, durationMs = 100, waveType = WaveType.SQUARE, volume = 0.26f)
    }

    fun playDeath() {
        playToneSlide(startFreq = 220f, endFreq = 48f, durationMs = 280, waveType = WaveType.SAWTOOTH, volume = 0.32f)
    }

    fun playWin() {
        playToneSlide(startFreq = 440f, endFreq = 880f, durationMs = 260, waveType = WaveType.TRIANGLE, volume = 0.28f)
    }

    private enum class WaveType { SQUARE, TRIANGLE, SAWTOOTH }

    private fun playToneSlide(
        startFreq: Float,
        endFreq: Float,
        durationMs: Int,
        waveType: WaveType,
        volume: Float
    ) {
        playSfxPcm({ i, total ->
            val progress = i.toFloat() / total.toFloat()
            val freq = startFreq + (endFreq - startFreq) * progress
            val t = i.toFloat() / sampleRate.toFloat()
            val phase = (t * freq) % 1.0f

            val rawSample = when (waveType) {
                WaveType.SQUARE -> if (phase < 0.5f) 1.0f else -1.0f
                WaveType.TRIANGLE -> (2.0f * kotlin.math.abs(2.0f * (phase - 0.5f)) - 1.0f)
                WaveType.SAWTOOTH -> 2.0f * (phase - 0.5f)
            }

            // Apply exponential-like envelope decay
            val env = (1.0f - progress * 0.9f)
            (rawSample * volume * env * 32767f).toInt().coerceIn(-32767, 32767).toShort()
        }, durationMs)
    }
}
