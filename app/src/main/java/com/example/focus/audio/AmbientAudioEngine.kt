package com.example.focus.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

/**
 * High-performance real-time procedural audio engine for Ambient Focus Soundscapes.
 * Uses AudioTrack with PCM 16-bit 44.1kHz stereo audio generation.
 * Generates zero-dependency, 100% offline, crystal-clear loopable ambient audio.
 */
class AmbientAudioEngine {

    companion object {
        private const val TAG = "AmbientAudioEngine"
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_FRAMES = 2048
    }

    private var audioTrack: AudioTrack? = null
    private var isEngineRunning = false
    private var synthesisThread: Thread? = null

    @Volatile
    private var currentSound: AmbientSoundType = AmbientSoundType.OFF

    @Volatile
    private var masterVolume: Float = 0.7f

    @Volatile
    private var isMuted: Boolean = false

    private val random = Random()

    // State variables for continuous procedural synthesis
    private var phaseL = 0.0
    private var phaseR = 0.0
    private var lfoPhase = 0.0
    private var lfoPhase2 = 0.0
    private var filterStateL = 0.0
    private var filterStateR = 0.0
    private var brownStateL = 0.0
    private var brownStateR = 0.0
    private var pinkB0 = 0.0
    private var pinkB1 = 0.0
    private var pinkB2 = 0.0
    private var birdTimer = 0
    private var birdPhase = 0.0
    private var birdActive = false
    private var birdFreq = 2200.0

    @Synchronized
    fun startSound(sound: AmbientSoundType, volume: Float = masterVolume) {
        masterVolume = volume.coerceIn(0f, 1f)
        currentSound = sound

        if (sound == AmbientSoundType.OFF) {
            stop()
            return
        }

        if (!isEngineRunning) {
            initAudioTrack()
            isEngineRunning = true
            synthesisThread = Thread({ audioLoop() }, "AmbientSynthThread").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }
        }
    }

    @Synchronized
    fun setSound(sound: AmbientSoundType) {
        currentSound = sound
        if (sound == AmbientSoundType.OFF) {
            stop()
        } else if (!isEngineRunning) {
            startSound(sound, masterVolume)
        }
    }

    @Synchronized
    fun setVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        audioTrack?.let {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    it.setVolume(masterVolume)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set AudioTrack volume", e)
            }
        }
    }

    @Synchronized
    fun pause() {
        isMuted = true
    }

    @Synchronized
    fun resume() {
        isMuted = false
    }

    @Synchronized
    fun stop() {
        currentSound = AmbientSoundType.OFF
        isEngineRunning = false
        synthesisThread?.interrupt()
        synthesisThread = null

        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing audio track", e)
        } finally {
            audioTrack = null
        }
    }

    private fun initAudioTrack() {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize, BUFFER_FRAMES * 4)

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        audioTrack = AudioTrack(
            attributes,
            format,
            bufferSize,
            AudioTrack.MODE_STREAM,
            android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setVolume(masterVolume)
            }
            play()
        }
    }

    private fun audioLoop() {
        val pcmBuffer = ShortArray(BUFFER_FRAMES * 2) // Stereo (L, R)

        while (isEngineRunning && !Thread.currentThread().isInterrupted) {
            val sound = currentSound
            val effVolume = if (isMuted || sound == AmbientSoundType.OFF) 0.0f else masterVolume

            if (effVolume <= 0.001f) {
                pcmBuffer.fill(0)
            } else {
                generateSamples(pcmBuffer, sound, effVolume)
            }

            val track = audioTrack ?: break
            try {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.write(pcmBuffer, 0, pcmBuffer.size)
                }
            } catch (e: Exception) {
                Log.w(TAG, "AudioTrack write exception", e)
                break
            }
        }
    }

    private fun generateSamples(buffer: ShortArray, sound: AmbientSoundType, volume: Float) {
        val gain = volume * 0.85f
        var index = 0

        for (i in 0 until BUFFER_FRAMES) {
            var sampleL = 0.0
            var sampleR = 0.0

            when (sound) {
                AmbientSoundType.RAIN -> {
                    // Rain: Layered low-pass noise + intermittent droplets & splatter
                    val white = (random.nextDouble() * 2.0 - 1.0)
                    // Pink filter approximation (Paul Kellet's filter)
                    pinkB0 = 0.99765 * pinkB0 + white * 0.0990460
                    pinkB1 = 0.96300 * pinkB1 + white * 0.1248000
                    pinkB2 = 0.57000 * pinkB2 + white * 0.2212000
                    val pink = (pinkB0 + pinkB1 + pinkB2 + white * 0.05) * 0.25

                    // Continuous rainfall hiss & low rumble
                    brownStateL = 0.96 * brownStateL + 0.04 * (random.nextDouble() * 2.0 - 1.0)
                    brownStateR = 0.96 * brownStateR + 0.04 * (random.nextDouble() * 2.0 - 1.0)

                    sampleL = pink * 0.6 + brownStateL * 0.4
                    sampleR = pink * 0.6 + brownStateR * 0.4

                    // Random raindrop impact impulses
                    if (random.nextDouble() < 0.008) {
                        val dropletPitch = 800.0 + random.nextDouble() * 1400.0
                        val dropAmp = 0.35 + random.nextDouble() * 0.4
                        val dropPan = random.nextDouble()
                        val dropTone = sin(phaseL * dropletPitch * 2.0 * PI / SAMPLE_RATE) * dropAmp
                        sampleL += dropTone * (1.0 - dropPan)
                        sampleR += dropTone * dropPan
                    }
                }

                AmbientSoundType.WHITE_NOISE -> {
                    // Gentle broadband focus white noise with gentle low-pass smoothing
                    val rawL = (random.nextDouble() * 2.0 - 1.0)
                    val rawR = (random.nextDouble() * 2.0 - 1.0)
                    filterStateL = 0.7 * filterStateL + 0.3 * rawL
                    filterStateR = 0.7 * filterStateR + 0.3 * rawR
                    sampleL = filterStateL * 0.45
                    sampleR = filterStateR * 0.45
                }

                AmbientSoundType.OCEAN_WAVES -> {
                    // Ocean Waves: 0.08Hz gentle wave swell modulation over deep brown noise
                    lfoPhase += (2.0 * PI * 0.08) / SAMPLE_RATE
                    if (lfoPhase > 2.0 * PI) lfoPhase -= 2.0 * PI
                    val waveSwell = (sin(lfoPhase) * 0.5 + 0.5)

                    // Secondary tide texture
                    lfoPhase2 += (2.0 * PI * 0.17) / SAMPLE_RATE
                    if (lfoPhase2 > 2.0 * PI) lfoPhase2 -= 2.0 * PI
                    val foamTexture = (sin(lfoPhase2) * 0.3 + 0.7)

                    val rawNoiseL = (random.nextDouble() * 2.0 - 1.0)
                    val rawNoiseR = (random.nextDouble() * 2.0 - 1.0)
                    brownStateL = 0.985 * brownStateL + 0.015 * rawNoiseL
                    brownStateR = 0.985 * brownStateR + 0.015 * rawNoiseR

                    sampleL = (brownStateL * 2.8 * waveSwell) + (rawNoiseL * 0.08 * foamTexture * waveSwell)
                    sampleR = (brownStateR * 2.8 * waveSwell) + (rawNoiseR * 0.08 * foamTexture * waveSwell)
                }

                AmbientSoundType.CAMPFIRE -> {
                    // Campfire: Warm low crackle and wood pops
                    val rawL = (random.nextDouble() * 2.0 - 1.0)
                    brownStateL = 0.97 * brownStateL + 0.03 * rawL
                    brownStateR = 0.97 * brownStateR + 0.03 * rawL

                    sampleL = brownStateL * 0.35
                    sampleR = brownStateR * 0.35

                    // Random wood snap & ember crackles
                    if (random.nextDouble() < 0.003) {
                        val pop = (random.nextDouble() * 2.0 - 1.0) * (0.6 + random.nextDouble() * 0.4)
                        sampleL += pop
                        sampleR += pop * 0.85
                    }
                }

                AmbientSoundType.BINAURAL_ALPHA -> {
                    // 200 Hz carrier with 10 Hz alpha wave beat (200Hz Left, 210Hz Right)
                    phaseL += (2.0 * PI * 196.0) / SAMPLE_RATE
                    phaseR += (2.0 * PI * 206.0) / SAMPLE_RATE
                    if (phaseL > 2.0 * PI) phaseL -= 2.0 * PI
                    if (phaseR > 2.0 * PI) phaseR -= 2.0 * PI

                    val toneL = sin(phaseL) * 0.28
                    val toneR = sin(phaseR) * 0.28

                    // Subtle soft pink bed underneath
                    val pink = (random.nextDouble() * 2.0 - 1.0) * 0.04

                    sampleL = toneL + pink
                    sampleR = toneR + pink
                }

                AmbientSoundType.FOREST_BREEZE -> {
                    // Gentle woodland wind + occasional distant birdsong
                    lfoPhase += (2.0 * PI * 0.05) / SAMPLE_RATE
                    if (lfoPhase > 2.0 * PI) lfoPhase -= 2.0 * PI
                    val windGain = (sin(lfoPhase) * 0.4 + 0.6)

                    val rawNoise = (random.nextDouble() * 2.0 - 1.0)
                    brownStateL = 0.95 * brownStateL + 0.05 * rawNoise
                    brownStateR = 0.95 * brownStateR + 0.05 * rawNoise

                    sampleL = brownStateL * 0.45 * windGain
                    sampleR = brownStateR * 0.45 * windGain

                    // Birdsong generator
                    if (!birdActive && birdTimer <= 0) {
                        if (random.nextDouble() < 0.0004) {
                            birdActive = true
                            birdTimer = (SAMPLE_RATE * (0.15 + random.nextDouble() * 0.25)).toInt()
                            birdFreq = 2000.0 + random.nextDouble() * 1200.0
                        }
                    } else if (birdActive) {
                        birdPhase += (2.0 * PI * birdFreq) / SAMPLE_RATE
                        if (birdPhase > 2.0 * PI) birdPhase -= 2.0 * PI
                        val chirp = sin(birdPhase) * 0.18
                        sampleL += chirp * 0.7
                        sampleR += chirp * 0.5
                        birdTimer--
                        if (birdTimer <= 0) {
                            birdActive = false
                            birdTimer = (SAMPLE_RATE * (1.5 + random.nextDouble() * 3.0)).toInt()
                        }
                    } else {
                        birdTimer--
                    }
                }

                AmbientSoundType.COFFEE_SHOP -> {
                    // Warm cozy coffee shop murmur resonance
                    val rawL = (random.nextDouble() * 2.0 - 1.0)
                    val rawR = (random.nextDouble() * 2.0 - 1.0)
                    filterStateL = 0.88 * filterStateL + 0.12 * rawL
                    filterStateR = 0.88 * filterStateR + 0.12 * rawR
                    sampleL = filterStateL * 0.38
                    sampleR = filterStateR * 0.38
                }

                AmbientSoundType.OFF -> {
                    sampleL = 0.0
                    sampleR = 0.0
                }
            }

            // Apply master gain and clamp to 16-bit PCM range
            val finalL = (sampleL * gain).coerceIn(-1.0, 1.0)
            val finalR = (sampleR * gain).coerceIn(-1.0, 1.0)

            buffer[index++] = (finalL * 32767.0).toInt().toShort()
            buffer[index++] = (finalR * 32767.0).toInt().toShort()
        }
    }
}
