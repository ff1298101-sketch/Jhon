package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Gestionnaire audio pour les sons de l'application James Chat :
 * - Bip / pop sonore à l'envoi de chaque message (style WhatsApp)
 * - Carillon / ding à la réception d'un message
 * - Bip de publication sur les canaux (Chaînes)
 */
object JamesSoundManager {
    private const val TAG = "JamesSoundManager"
    private const val SAMPLE_RATE = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled = _soundEnabled.asStateFlow()

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
    }

    /**
     * Joue le son de confirmation d'envoi de message (pop / tick doux et satisfaisant).
     */
    fun playMessageSentSound() {
        if (!_soundEnabled.value) return
        scope.launch {
            try {
                // Synthèse d'un son d'envoi doux : 700 Hz glissant vers 1050 Hz sur 55 ms
                val durationMs = 55
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 700.0 + (350.0 * progress)
                    val envelope = (1.0 - progress) * (1.0 - progress) // fade out doux
                    val sample = sin(2.0 * PI * freq * i / SAMPLE_RATE) * envelope * Short.MAX_VALUE * 0.45
                    buffer[i] = sample.toInt().toShort()
                }
                playPcmBuffer(buffer)
            } catch (e: Exception) {
                Log.w(TAG, "Erreur son envoi: ${e.message}")
            }
        }
    }

    /**
     * Joue le son de message reçu (double note carillon clair : 880 Hz puis 1320 Hz).
     */
    fun playMessageReceivedSound() {
        if (!_soundEnabled.value) return
        scope.launch {
            try {
                val durationMs = 120
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                val half = numSamples / 2
                for (i in 0 until numSamples) {
                    val sample = if (i < half) {
                        val p = i.toDouble() / half
                        val env = 1.0 - (p * 0.4)
                        sin(2.0 * PI * 880.0 * i / SAMPLE_RATE) * env * Short.MAX_VALUE * 0.4
                    } else {
                        val p = (i - half).toDouble() / (numSamples - half)
                        val env = (1.0 - p) * (1.0 - p)
                        sin(2.0 * PI * 1320.0 * (i - half) / SAMPLE_RATE) * env * Short.MAX_VALUE * 0.5
                    }
                    buffer[i] = sample.toInt().toShort()
                }
                playPcmBuffer(buffer)
            } catch (e: Exception) {
                Log.w(TAG, "Erreur son réception: ${e.message}")
            }
        }
    }

    /**
     * Joue un son de notification pour nouvelle publication de chaîne (3 notes ascendantes).
     */
    fun playChannelPostSound() {
        if (!_soundEnabled.value) return
        scope.launch {
            try {
                val durationMs = 150
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                val third = numSamples / 3
                for (i in 0 until numSamples) {
                    val freq = when {
                        i < third -> 660.0
                        i < third * 2 -> 880.0
                        else -> 1175.0
                    }
                    val stepIndex = i % third
                    val p = stepIndex.toDouble() / third
                    val env = (1.0 - p * 0.6)
                    val sample = sin(2.0 * PI * freq * i / SAMPLE_RATE) * env * Short.MAX_VALUE * 0.4
                    buffer[i] = sample.toInt().toShort()
                }
                playPcmBuffer(buffer)
            } catch (e: Exception) {
                Log.w(TAG, "Erreur son canal: ${e.message}")
            }
        }
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val size = maxOf(buffer.size * 2, minBufferSize)
        val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
        } else {
            @Suppress("DEPRECATION")
            AudioTrack(
                android.media.AudioManager.STREAM_NOTIFICATION,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                size,
                AudioTrack.MODE_STATIC
            )
        }

        try {
            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Nettoyage après lecture
            val waitMs = (buffer.size * 1000L / SAMPLE_RATE) + 50L
            Thread.sleep(waitMs)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            try { audioTrack.release() } catch (_: Exception) {}
        }
    }
}
