package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural relaxing ambient audio synthesizer.
 * Generates continuous soothing ocean wave sounds and soft harmonic underwater chime drones
 * without relying on missing external MP3 files or network downloads.
 */
class ZenAmbientSynthesizer(private val context: Context) {

    private var audioJob: Job? = null
    private var isPlaying = false
    private var volume = 0.65f

    fun startAmbientTrack(scope: CoroutineScope) {
        if (isPlaying) return
        isPlaying = true

        audioJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(sampleRate / 4)

            val audioTrack = try {
                AudioTrack.Builder()
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
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                null
            }

            if (audioTrack == null) return@launch

            try {
                audioTrack.play()
                val buffer = ShortArray(bufferSize)
                var phasePink = 0.0
                var phaseDrone1 = 0.0
                var phaseDrone2 = 0.0
                var swellPhase = 0.0

                // Ambient ocean chord notes (E2, B2, E3, G#3 ambient frequencies: ~82Hz, ~123Hz, ~164Hz)
                val freq1 = 82.4
                val freq2 = 123.47
                val freq3 = 164.81

                var b0 = 0.0
                var b1 = 0.0
                var b2 = 0.0

                while (isActive && isPlaying) {
                    for (i in 0 until buffer.size step 2) {
                        // Gentle ambient swell (period ~10 seconds)
                        swellPhase += (2.0 * PI * 0.1) / sampleRate
                        val swell = (0.6 + 0.4 * sin(swellPhase)).toFloat()

                        // Pink noise generator for soothing ocean waves
                        val white = (Math.random() * 2.0 - 1.0).toFloat()
                        b0 = 0.99886 * b0 + white * 0.0555179
                        b1 = 0.99332 * b1 + white * 0.0750759
                        b2 = 0.96900 * b2 + white * 0.1538520
                        val waveNoise = (b0 + b1 + b2) * 0.08 * swell

                        // Harmonious singing bowl / warm water drone
                        phaseDrone1 += (2.0 * PI * freq1) / sampleRate
                        phaseDrone2 += (2.0 * PI * freq2) / sampleRate
                        val tone = (sin(phaseDrone1) * 0.12 + sin(phaseDrone2) * 0.08) * swell

                        val totalSample = (waveNoise + tone) * volume
                        val sampleClamped = (totalSample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()

                        // Stereo channels
                        buffer[i] = sampleClamped
                        buffer[i + 1] = sampleClamped
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                // Audio gracefully handled
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // Ignored
                }
            }
        }
    }

    fun stopAmbientTrack() {
        isPlaying = false
        audioJob?.cancel()
        audioJob = null
    }

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0.0f, 1.0f)
    }

    fun isPlayingAudio(): Boolean = isPlaying
}
