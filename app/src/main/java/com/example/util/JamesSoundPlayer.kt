package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * JamesSoundPlayer — Identité sonore originale James Chat
 * Génère des signatures harmoniques synthétiques de haute fidélité.
 * Prend en charge les sonneries personnalisées, aperçus sonores, vibrations et mode silencieux.
 */
object JamesSoundPlayer {

    enum class SoundType {
        STARTUP_JINGLE,      // Jingle de démarrage James Chat
        NEW_MESSAGE,         // Bip harmonique discret
        INCOMING_CALL,       // Sonnerie mélodique
        CALL_ENDED,          // Fin d'appel douce
        GROUP_INVITE,        // Carillon accueillant
        CONTACT_REQUEST,     // Deux tons montants
        ADMIN_ALERT          // Ton d'avertissement solennel
    }

    enum class RingtoneStyle(val id: String, val title: String, val description: String) {
        JAMES_SYNTH("JAMES_SYNTH", "James Synthétique", "Harmonie originale bleu & cyan"),
        CYBER_BELL("CYBER_BELL", "Carillon Cyber", "Notes cristallines haute fréquence"),
        CARIBBEAN_BREEZE("CARIBBEAN_BREEZE", "Brise Caribéenne", "Mélodie caribéenne chaleureuse"),
        ECHO_WAVE("ECHO_WAVE", "Onde Echo", "Pulsations futuristes douces"),
        SILENT("SILENT", "Silencieux", "Mode muet sans tonalité");

        companion object {
            fun fromId(id: String): RingtoneStyle {
                return values().find { it.id.equals(id, ignoreCase = true) } ?: JAMES_SYNTH
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private var ringtoneJob: Job? = null
    private var previewJob: Job? = null
    private var activeAudioTrack: AudioTrack? = null

    /**
     * Joue un son ponctuel de manière non-bloquante.
     */
    fun playSound(context: Context, type: SoundType, vibrate: Boolean = true, isSilent: Boolean = false) {
        if (isSilent) return
        scope.launch {
            try {
                if (vibrate) {
                    triggerVibration(context, type)
                }
                playSynthesizedTone(type)
            } catch (_: Exception) {}
        }
    }

    /**
     * Lance la sonnerie en boucle pour un appel entrant.
     */
    fun startRingtoneLoop(
        context: Context,
        style: RingtoneStyle = RingtoneStyle.JAMES_SYNTH,
        isSilent: Boolean = false,
        isVibrate: Boolean = true,
        vibrationPattern: String = "Standard"
    ) {
        stopRingtone(context)

        ringtoneJob = scope.launch {
            while (isActive) {
                if (isVibrate) {
                    triggerVibrationPattern(context, vibrationPattern)
                }
                if (!isSilent && style != RingtoneStyle.SILENT) {
                    playRingtoneTone(style)
                }
                delay(600)
            }
        }
    }

    /**
     * Joue un court extrait (aperçu) de la sonnerie sélectionnée.
     */
    fun previewRingtone(context: Context, style: RingtoneStyle, vibrate: Boolean = true) {
        previewJob?.cancel()
        previewJob = scope.launch {
            try {
                if (style == RingtoneStyle.SILENT) {
                    if (vibrate) triggerVibrationPattern(context, "Standard")
                    return@launch
                }
                if (vibrate) triggerVibrationPattern(context, "Douce")
                playRingtoneTone(style)
            } catch (_: Exception) {}
        }
    }

    /**
     * Arrête immédiatement toute sonnerie ou aperçu en cours.
     */
    fun stopRingtone(context: Context? = null) {
        ringtoneJob?.cancel()
        ringtoneJob = null
        previewJob?.cancel()
        previewJob = null

        try {
            activeAudioTrack?.stop()
            activeAudioTrack?.release()
            activeAudioTrack = null
        } catch (_: Exception) {}

        if (context != null) {
            cancelVibration(context)
        }
    }

    fun triggerVibrationPattern(context: Context, pattern: String) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (pattern) {
                    "Douce" -> VibrationEffect.createWaveform(longArrayOf(0, 100, 150, 100), -1)
                    "Intense" -> VibrationEffect.createWaveform(longArrayOf(0, 400, 150, 400, 150, 400), -1)
                    else -> VibrationEffect.createWaveform(longArrayOf(0, 250, 150, 250), -1) // Standard
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(250)
            }
        } catch (_: Exception) {}
    }

    private fun cancelVibration(context: Context) {
        try {
            getVibrator(context)?.cancel()
        } catch (_: Exception) {}
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun triggerVibration(context: Context, type: SoundType) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    SoundType.STARTUP_JINGLE -> VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 120), -1)
                    SoundType.NEW_MESSAGE -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    SoundType.INCOMING_CALL -> VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300), -1)
                    SoundType.CALL_ENDED -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                    SoundType.GROUP_INVITE, SoundType.CONTACT_REQUEST -> VibrationEffect.createWaveform(longArrayOf(0, 60, 40, 60), -1)
                    SoundType.ADMIN_ALERT -> VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(80)
            }
        } catch (_: Exception) {}
    }

    private fun playRingtoneTone(style: RingtoneStyle) {
        val sampleRate = 44100
        val buffer = when (style) {
            RingtoneStyle.JAMES_SYNTH -> {
                // Accord électronique mélodique 3 temps : D5 (587Hz) -> F#5 (740Hz) -> A5 (880Hz)
                val notes = listOf(587.33, 739.99, 880.00, 739.99)
                synthesizeNotes(notes, durationMs = 220, sampleRate = sampleRate)
            }
            RingtoneStyle.CYBER_BELL -> {
                // Carillon cyber cristallin : C6 (1046Hz) -> E6 (1318Hz) -> G6 (1567Hz)
                val notes = listOf(1046.50, 1318.51, 1567.98, 1046.50)
                synthesizeNotes(notes, durationMs = 180, sampleRate = sampleRate)
            }
            RingtoneStyle.CARIBBEAN_BREEZE -> {
                // Mélodie caribéenne chaleureuse : G4 (392Hz) -> B4 (493Hz) -> D5 (587Hz) -> G5 (784Hz)
                val notes = listOf(392.00, 493.88, 587.33, 783.99)
                synthesizeNotes(notes, durationMs = 200, sampleRate = sampleRate)
            }
            RingtoneStyle.ECHO_WAVE -> {
                // Onde futuriste à battement doux
                val totalSamples = (sampleRate * 0.9).toInt()
                val samples = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 432.0 + sin(2.0 * Math.PI * 4.0 * t) * 30.0
                    val env = sin(Math.PI * (i.toDouble() / totalSamples)).coerceIn(0.0, 1.0)
                    val s = sin(2.0 * Math.PI * freq * t) * env * 14000
                    samples[i] = s.toInt().toShort()
                }
                samples
            }
            RingtoneStyle.SILENT -> ShortArray(0)
        }

        if (buffer.isNotEmpty()) {
            playRawBuffer(buffer, sampleRate)
        }
    }

    private fun synthesizeNotes(notes: List<Double>, durationMs: Int, sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * (notes.size * durationMs) / 1000)
        val samples = ShortArray(totalSamples)

        notes.forEachIndexed { noteIdx, freq ->
            val startSample = (noteIdx * durationMs * sampleRate) / 1000
            val numSamples = (durationMs * sampleRate) / 1000
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = (1.0 - (i.toDouble() / numSamples)).coerceIn(0.0, 1.0)
                val fundamental = sin(2.0 * Math.PI * freq * t)
                val harmonic = 0.25 * sin(2.0 * Math.PI * (freq * 2) * t)
                val sampleVal = ((fundamental + harmonic) * env * 14000).toInt()
                val sampleIdx = startSample + i
                if (sampleIdx < samples.size) {
                    samples[sampleIdx] = sampleVal.coerceIn(-32767, 32767).toShort()
                }
            }
        }
        return samples
    }

    private fun playSynthesizedTone(type: SoundType) {
        val sampleRate = 44100
        val buffer = when (type) {
            SoundType.STARTUP_JINGLE -> {
                val notes = listOf(523.25, 659.25, 783.99, 1046.50)
                synthesizeNotes(notes, durationMs = 180, sampleRate = sampleRate)
            }
            SoundType.NEW_MESSAGE -> {
                val totalSamples = (sampleRate * 0.16).toInt()
                val samples = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = if (i < totalSamples / 2) 784.0 else 1046.5
                    val env = (1.0 - (i.toDouble() / totalSamples)).coerceIn(0.0, 1.0)
                    val s = sin(2.0 * Math.PI * freq * t) * env * 12000
                    samples[i] = s.toInt().toShort()
                }
                samples
            }
            SoundType.INCOMING_CALL -> {
                val totalSamples = (sampleRate * 0.8).toInt()
                val samples = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = sin(Math.PI * (i.toDouble() / totalSamples)).coerceIn(0.0, 1.0)
                    val s = (sin(2.0 * Math.PI * 440.0 * t) + sin(2.0 * Math.PI * 660.0 * t)) * 0.5 * env * 14000
                    samples[i] = s.toInt().toShort()
                }
                samples
            }
            SoundType.CALL_ENDED -> {
                val totalSamples = (sampleRate * 0.25).toInt()
                val samples = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 440.0 * (1.0 - (i.toDouble() / totalSamples) * 0.4)
                    val env = (1.0 - (i.toDouble() / totalSamples)).coerceIn(0.0, 1.0)
                    val s = sin(2.0 * Math.PI * freq * t) * env * 11000
                    samples[i] = s.toInt().toShort()
                }
                samples
            }
            SoundType.GROUP_INVITE, SoundType.CONTACT_REQUEST -> {
                val totalSamples = (sampleRate * 0.22).toInt()
                val samples = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 587.33 + (i.toDouble() / totalSamples) * 300.0
                    val env = (1.0 - (i.toDouble() / totalSamples)).coerceIn(0.0, 1.0)
                    val s = sin(2.0 * Math.PI * freq * t) * env * 12000
                    samples[i] = s.toInt().toShort()
                }
                samples
            }
            SoundType.ADMIN_ALERT -> {
                val totalSamples = (sampleRate * 0.3).toInt()
                val samples = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val s = sin(2.0 * Math.PI * 330.0 * t) * 12000
                    samples[i] = s.toInt().toShort()
                }
                samples
            }
        }

        playRawBuffer(buffer, sampleRate)
    }

    private fun playRawBuffer(buffer: ShortArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
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
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        activeAudioTrack = audioTrack
        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()

        Thread.sleep((buffer.size * 1000L / sampleRate) + 50L)
        audioTrack.stop()
        audioTrack.release()
        if (activeAudioTrack == audioTrack) {
            activeAudioTrack = null
        }
    }
}
