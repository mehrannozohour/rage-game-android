package com.example.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * High-performance 8-Bit Retro Chiptune Synthesizer & Lo-Fi Background Music Manager.
 * Optimized to eliminate audio thread GC churn and lag:
 * - Pre-allocated reusable sound buffers
 * - Low-overhead streaming audio tracks
 * - Separate toggles for BGM & SFX
 */
class ChiptuneAudioPlayer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 16000 // Optimized sample rate for zero-lag 8-bit retro sound

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

    // Progression: Am7 -> Fmaj7 -> Cmaj7 -> Gsus4
    private val chordRoots = floatArrayOf(
        220.00f, 220.00f, // A3
        174.61f, 174.61f, // F3
        130.81f, 130.81f, // C3
        196.00f, 196.00f  // G3
    )

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
                // Relentless Industrial Horror step timing (approx 170ms per beat step)
                val stepMs = 170
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
                    .setBufferSizeInBytes((minBuf * 2).coerceAtLeast(sampleRate))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                bgmTrack?.play()

                val numSamplesPerStep = (sampleRate * (stepMs / 1000f)).toInt()
                val buffer = ShortArray(numSamplesPerStep)

                while (isActive && isBgmEnabled && isGamePlaying) {
                    val stepInPattern = step % 16
                    // Heavy iron-on-iron collision on beats 0, 4, 8, 10, 12
                    val isHeavyMetalCrash = (stepInPattern == 0 || stepInPattern == 4 || stepInPattern == 8 || stepInPattern == 10 || stepInPattern == 12)
                    // Secondary metallic hammer/chain clang on offbeats
                    val isSecondaryClang = (stepInPattern == 2 || stepInPattern == 6 || stepInPattern == 14)
                    // Scraping screech
                    val isMetalScrape = (stepInPattern == 7 || stepInPattern == 15)

                    // Sinister sub-bass drone oscillating between low D1 (36.7Hz), Eb1 (38.9Hz), and G1 (49Hz)
                    val droneFreq = when ((step / 8) % 4) {
                        0 -> 36.7f
                        1 -> 38.9f // Unsettling minor second dissonance
                        2 -> 49.0f
                        else -> 51.9f // Tritone / augmented 4th
                    }

                    for (i in 0 until numSamplesPerStep) {
                        val t = i.toFloat() / sampleRate.toFloat()
                        val progress = i.toFloat() / numSamplesPerStep.toFloat()

                        // 1. Scary Dark Infrasound / Horror Sub Drone
                        val dronePhase = (t * droneFreq) % 1.0f
                        val droneWave = (sin(2.0 * Math.PI * dronePhase).toFloat() +
                                0.35f * sin(4.0 * Math.PI * dronePhase).toFloat())
                        // Add eerie low-frequency throbbing horror pulsation (3 Hz tremolo)
                        val horrorPulse = 0.7f + 0.3f * sin(2.0 * Math.PI * (t * 3.2f)).toFloat()
                        val subBassSample = droneWave * 0.22f * horrorPulse

                        // 2. Heavy Iron Slabs Clashing ("دو تا آهن می خورن به هم")
                        var metalClangSample = 0.0f
                        if (isHeavyMetalCrash) {
                            // Heavy low-end impact thud (iron mass)
                            val impactEnv = (1.0f - progress * 4.5f).coerceAtLeast(0f)
                            val impactThud = sin(2.0 * Math.PI * (t * 95f * (1.0f - progress * 2f).coerceAtLeast(0.2f))).toFloat() * 0.35f * impactEnv

                            // Resonant inharmonic iron partials (440Hz, 720Hz, 1180Hz, 1920Hz, 3100Hz)
                            val ringEnv = kotlin.math.exp(-progress * 6.5f)
                            val fmMod = sin(2.0 * Math.PI * (t * 720f)).toFloat() * 2.2f * kotlin.math.exp(-progress * 14f)
                            val primaryIron = sin(2.0 * Math.PI * (t * 440f + fmMod)).toFloat()
                            val harmonic2 = sin(2.0 * Math.PI * (t * 1180f)).toFloat() * 0.55f
                            val harmonic3 = sin(2.0 * Math.PI * (t * 1920f)).toFloat() * 0.35f
                            val harmonic4 = sin(2.0 * Math.PI * (t * 3100f)).toFloat() * 0.22f

                            // Violent impact noise burst in first 35ms (grinding metal edges)
                            val metalBurst = if (progress < 0.22f) {
                                val noise = ((i * 1664525 + 1013904223) and 0x7FFFFFFF) / 0x7FFFFFFF.toFloat() - 0.5f
                                noise * (1.0f - progress / 0.22f) * 0.40f
                            } else 0f

                            val ironClash = (primaryIron + harmonic2 + harmonic3 + harmonic4) * ringEnv * 0.38f
                            metalClangSample = impactThud + ironClash + metalBurst
                        } else if (isSecondaryClang) {
                            // High metallic iron hammer ping / chain strike
                            val pingEnv = kotlin.math.exp(-progress * 9f)
                            val ping1 = sin(2.0 * Math.PI * (t * 960f)).toFloat()
                            val ping2 = sin(2.0 * Math.PI * (t * 1680f)).toFloat() * 0.6f
                            val ping3 = sin(2.0 * Math.PI * (t * 2650f)).toFloat() * 0.3f
                            metalClangSample = (ping1 + ping2 + ping3) * pingEnv * 0.22f
                        } else if (isMetalScrape && progress < 0.45f) {
                            // Scary metallic screech / scraping blade
                            val scrapeNoise = ((i * 1103515245 + 12345) and 0x7FFFFFFF) / 0x7FFFFFFF.toFloat() - 0.5f
                            val scrapeFreq = 1800f - progress * 900f
                            val scrapeTone = sin(2.0 * Math.PI * (t * scrapeFreq)).toFloat()
                            metalClangSample = (scrapeNoise * 0.18f + scrapeTone * 0.12f) * (1.0f - progress / 0.45f)
                        }

                        // Distorted Industrial Overdrive clipping for raw, nerve-racking harshness
                        var mixed = subBassSample + metalClangSample
                        if (mixed > 0.55f) {
                            mixed = 0.55f + (mixed - 0.55f) * 0.3f
                        } else if (mixed < -0.55f) {
                            mixed = -0.55f + (mixed + 0.55f) * 0.3f
                        }
                        val finalSample = mixed.coerceIn(-0.95f, 0.95f)

                        buffer[i] = (finalSample * 32767f).toInt().toShort()
                    }

                    bgmTrack?.write(buffer, 0, buffer.size)
                    step++
                }
            } catch (_: Exception) {
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

    // --- OPTIMIZED SOUND EFFECTS (SFX) WITH SINGLE STREAM REUSE ---

    private var sfxTrack: AudioTrack? = null

    private fun getOrCreateSfxTrack(): AudioTrack? {
        if (sfxTrack == null) {
            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                sfxTrack = AudioTrack.Builder()
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
                    .setBufferSizeInBytes((minBuf * 2).coerceAtLeast(sampleRate))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                sfxTrack?.play()
            } catch (_: Exception) {
                sfxTrack = null
            }
        }
        return sfxTrack
    }

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
                val track = getOrCreateSfxTrack()
                track?.write(buffer, 0, buffer.size)
            } catch (_: Exception) {}
        }
    }

    fun playJump() {
        playToneSlide(startFreq = 260f, endFreq = 540f, durationMs = 90, waveType = WaveType.SQUARE, volume = 0.22f)
    }

    fun playDoubleJump() {
        playToneSlide(startFreq = 380f, endFreq = 780f, durationMs = 110, waveType = WaveType.SQUARE, volume = 0.22f)
    }

    fun playSlide() {
        playToneSlide(startFreq = 160f, endFreq = 70f, durationMs = 100, waveType = WaveType.SAWTOOTH, volume = 0.20f)
    }

    fun playVault() {
        playToneSlide(startFreq = 210f, endFreq = 480f, durationMs = 100, waveType = WaveType.TRIANGLE, volume = 0.25f)
    }

    fun playDash() {
        playToneSlide(startFreq = 520f, endFreq = 160f, durationMs = 120, waveType = WaveType.SAWTOOTH, volume = 0.22f)
    }

    fun playGlass() {
        playToneSlide(startFreq = 960f, endFreq = 320f, durationMs = 90, waveType = WaveType.SQUARE, volume = 0.24f)
    }

    fun playAmbush() {
        playToneSlide(startFreq = 840f, endFreq = 1250f, durationMs = 80, waveType = WaveType.SQUARE, volume = 0.26f)
    }

    fun playDeath() {
        playToneSlide(startFreq = 220f, endFreq = 48f, durationMs = 220, waveType = WaveType.SAWTOOTH, volume = 0.32f)
    }

    fun playWin() {
        playToneSlide(startFreq = 440f, endFreq = 880f, durationMs = 200, waveType = WaveType.TRIANGLE, volume = 0.28f)
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

            val env = (1.0f - progress * 0.9f)
            (rawSample * volume * env * 32767f).toInt().coerceIn(-32767, 32767).toShort()
        }, durationMs)
    }
}
