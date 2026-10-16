package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageEntity
import com.example.util.JamesVoicePlayer
import com.example.util.JamesVoiceRecorder
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * Lecteur audio moderne style WhatsApp pour James Chat avec :
 * - Onde sonore animée en temps réel avec ondulation fluide pendant la lecture
 * - Coloration bicolore exacte (partie écoutée vs partie restante)
 * - Défilement et scrubbing interactif par toucher ou glissement
 * - Sélecteur de vitesse de lecture (1x, 1.5x, 2x)
 * - Affichage précis du temps écoulé et de la durée totale
 * - Garantie d'écoute exclusive (un seul audio actif à la fois)
 */
@Composable
fun JamesVoicePlayerView(
    message: MessageEntity,
    isFromMe: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentPlayingId by JamesVoicePlayer.currentPlayingId.collectAsState()
    val isGlobalPlaying by JamesVoicePlayer.isPlaying.collectAsState()
    val currentPositionMs by JamesVoicePlayer.currentPositionMs.collectAsState()
    val durationMs by JamesVoicePlayer.durationMs.collectAsState()
    val playbackSpeed by JamesVoicePlayer.playbackSpeed.collectAsState()

    val isThisMessageActive = currentPlayingId == message.id
    val isPlaying = isThisMessageActive && isGlobalPlaying

    // Animation du temps réel (tick pour micro-ondulations des barres pendant la lecture)
    var animTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            animTick++
            delay(50)
        }
    }

    val totalDurationSeconds = remember(message.mediaDurationSeconds) {
        if (message.mediaDurationSeconds > 0) message.mediaDurationSeconds else 8
    }

    val progress = remember(isThisMessageActive, currentPositionMs, durationMs, totalDurationSeconds) {
        if (!isThisMessageActive) {
            0f
        } else {
            val total = if (durationMs > 0) durationMs else totalDurationSeconds * 1000L
            (currentPositionMs.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }
    }

    // Amplitudes de l'onde sonore (30 barres)
    val waveformBars = remember(message.waveformData, message.id) {
        JamesVoiceRecorder.parseWaveform(
            waveString = message.waveformData,
            count = 30,
            seed = message.id
        )
    }

    val playedColor = if (isFromMe) Color.White else MaterialTheme.colorScheme.primary
    val unplayedColor = if (isFromMe) Color.White.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
    val textColor = if (isFromMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val buttonBgColor = if (isFromMe) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.primaryContainer
    val buttonIconColor = if (isFromMe) Color.White else MaterialTheme.colorScheme.primary

    // Formatage du temps
    val timeLabel = remember(isThisMessageActive, currentPositionMs, totalDurationSeconds) {
        if (isThisMessageActive && currentPositionMs > 0) {
            val elapsedSec = (currentPositionMs / 1000).toInt()
            val elapsedMin = elapsedSec / 60
            val remSec = elapsedSec % 60
            val totMin = totalDurationSeconds / 60
            val totSec = totalDurationSeconds % 60
            String.format("%d:%02d / %d:%02d", elapsedMin, remSec, totMin, totSec)
        } else {
            val totMin = totalDurationSeconds / 60
            val totSec = totalDurationSeconds % 60
            String.format("%d:%02d", totMin, totSec)
        }
    }

    Column(
        modifier = modifier
            .width(245.dp)
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Bouton Play / Pause
            IconButton(
                onClick = {
                    JamesVoicePlayer.playOrPause(
                        context = context,
                        messageId = message.id,
                        audioPath = message.mediaUrl,
                        durationSeconds = totalDurationSeconds
                    )
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(buttonBgColor)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Lecture audio",
                    tint = buttonIconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Zone de l'onde sonore animée avec support du scrubber tactile
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .pointerInput(message.id) {
                        detectTapGestures { offset ->
                            val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            JamesVoicePlayer.seekTo(message.id, fraction)
                        }
                    }
                    .pointerInput(message.id) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                            JamesVoicePlayer.seekTo(message.id, fraction)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(32.dp)) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val numBars = waveformBars.size
                    val spacing = 3.dp.toPx()
                    val totalSpacing = spacing * (numBars - 1)
                    val barWidth = ((canvasWidth - totalSpacing) / numBars).coerceAtLeast(2.dp.toPx())

                    val currentProgressX = progress * canvasWidth

                    waveformBars.forEachIndexed { i, baseAmp ->
                        val barCenterX = i * (barWidth + spacing) + barWidth / 2f

                        // Ondulation fluide WhatsApp en direct autour de la tête de lecture
                        val waveOffset = if (isPlaying) {
                            val distFromHead = (barCenterX - currentProgressX) / canvasWidth
                            val bounce = sin((animTick * 0.45) + (i * 0.55)) * 0.22f
                            (bounce * (1f - distFromHead.coerceIn(-0.6f, 0.6f))).toFloat()
                        } else 0f

                        val effectiveAmp = (baseAmp + waveOffset).coerceIn(0.18f, 1.0f)
                        val barHeight = (effectiveAmp * canvasHeight).coerceAtLeast(4.dp.toPx())

                        val isPlayed = barCenterX <= currentProgressX
                        val color = if (isPlayed) playedColor else unplayedColor

                        val top = (canvasHeight - barHeight) / 2f
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(i * (barWidth + spacing), top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )

                        // Curseur interactif WhatsApp sur la barre active
                        if (isThisMessageActive && isPlayed && (i == (progress * numBars).toInt())) {
                            drawCircle(
                                color = playedColor,
                                radius = barWidth * 0.95f,
                                center = Offset(barCenterX, top + barHeight / 2f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Barre d'informations : Durée / Temps écoulé + Vitesse de lecture
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = timeLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.85f)
                )
            }

            // Bouton de vitesse (1x, 1.5x, 2x) affiché lorsque cet audio est sélectionné
            if (isThisMessageActive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFromMe) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primaryContainer)
                        .clickable { JamesVoicePlayer.cycleSpeed() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${playbackSpeed}x",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFromMe) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
