package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AppLanguage
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.ReactionEntity
import com.example.ui.theme.JamesReadBlue
import com.example.ui.theme.JamesSentGrey
import com.example.util.JamesI18n
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageEntity,
    isFromMe: Boolean,
    reactions: List<ReactionEntity> = emptyList(),
    lang: AppLanguage,
    onReply: (MessageEntity) -> Unit,
    onEdit: (MessageEntity) -> Unit,
    onDelete: (MessageEntity) -> Unit,
    onCopy: (MessageEntity) -> Unit,
    onForward: (MessageEntity) -> Unit,
    onTogglePin: (MessageEntity) -> Unit,
    onAddReaction: (MessageEntity, String) -> Unit,
    onViewReactions: (MessageEntity) -> Unit = {},
    onOpenMedia: (String, MessageType) -> Unit,
    onToggleStar: (MessageEntity) -> Unit = {},
    onReport: (MessageEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }
    var isTranslated by remember { mutableStateOf(false) }
    val translatedText = remember(isTranslated, message.text, lang) {
        if (isTranslated && message.text.isNotEmpty()) {
            com.example.util.JamesOfflineTranslator.translate(message.text, lang.code).translatedText
        } else null
    }


    val bubbleColor = if (isFromMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isFromMe) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val bubbleShape = if (isFromMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            AnimatedVisibility(visible = showReactionPicker) {
                EmojiReactionPicker(
                    onSelectEmoji = { emoji ->
                        onAddReaction(message, emoji)
                        showReactionPicker = false
                    },
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .clip(bubbleShape)
                    .combinedClickable(
                        onClick = {
                            if (message.type == MessageType.VIDEO.name || message.type == MessageType.PHOTO.name) {
                                onOpenMedia(message.mediaUrl, MessageType.valueOf(message.type))
                            }
                        },
                        onLongClick = {
                            showReactionPicker = true
                            showMenu = true
                        }
                    )
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    if (!isFromMe && message.senderUsername.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "@${message.senderUsername}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            if (message.senderUsername.contains("James Bot", ignoreCase = true) ||
                                message.senderUsername.equals("jamesbot", ignoreCase = true) ||
                                message.senderId == "bot_james" ||
                                message.senderId == "user_james_bot"
                            ) {
                                Spacer(modifier = Modifier.width(3.dp))
                                JamesVBadge(
                                    size = 11.dp,
                                    style = com.example.data.model.VBadgeStyle.SEAL,
                                    userAccountName = "James Bot",
                                    showTooltipOnClick = false
                                )
                            }
                        }
                    }

                    if (message.isPinned) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pin,
                                contentDescription = "Épinglé",
                                tint = if (isFromMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = JamesI18n.t("pinned_message", lang),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFromMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (message.forwardedFrom.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Forward,
                                contentDescription = null,
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Transféré de @${message.forwardedFrom}",
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                    }

                    if (!message.replyToText.isNullOrEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isFromMe) Color.Black.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(24.dp)
                                    .background(if (isFromMe) Color.White else MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = message.replyToSender ?: "Réponse",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFromMe) Color.White else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = message.replyToText,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    color = textColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    when (message.type) {
                        MessageType.STICKER.name -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                if (message.mediaUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = message.mediaUrl,
                                        contentDescription = message.text.ifEmpty { "Sticker" },
                                        modifier = Modifier
                                            .size(130.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(
                                        text = message.text,
                                        fontSize = 44.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                        MessageType.PHOTO.name -> {
                            Box(
                                modifier = Modifier
                                    .width(240.dp)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.DarkGray)
                            ) {
                                if (message.mediaUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = message.mediaUrl,
                                        contentDescription = "Photo",
                                        modifier = Modifier.fillMaxWidth(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = "📷 Photo",
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                            if (message.text.isNotEmpty() && message.text != "📷 Photo") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = message.text, color = textColor, fontSize = 14.sp)
                            }
                        }
                        MessageType.VIDEO.name -> {
                            Box(
                                modifier = Modifier
                                    .width(240.dp)
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Lire la vidéo",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .padding(8.dp)
                                )
                                Text(
                                    text = "🎬 Vidéo HD",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                )
                            }
                        }
                        MessageType.AUDIO.name -> {
                            JamesVoicePlayerView(
                                message = message,
                                isFromMe = isFromMe
                            )
                        }
                        MessageType.DOCUMENT.name -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .width(220.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.1f))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "Document",
                                    tint = textColor,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = message.fileName.ifEmpty { "document.pdf" },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textColor,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = message.fileSize.ifEmpty { "1.4 MB" },
                                        fontSize = 10.sp,
                                        color = textColor.copy(alpha = 0.7f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Télécharger",
                                    tint = textColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        MessageType.POLL.name -> {
                            val pollData = message.text
                            val rawParts = if (pollData.startsWith("POLL:")) pollData.removePrefix("POLL:").split("|") else pollData.split("\n")
                            val question = rawParts.firstOrNull()?.trim()?.ifEmpty { "Sondage" } ?: "Sondage"
                            val options = rawParts.drop(1).map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf("Oui 👍", "Non 👎") }
                            var userVoteIndex by remember(message.id) { mutableStateOf<Int?>(null) }

                            Column(
                                modifier = Modifier
                                    .widthIn(min = 230.dp, max = 290.dp)
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Poll,
                                        contentDescription = "Sondage",
                                        tint = if (isFromMe) Color.White else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sondage interactif",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = if (isFromMe) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = question,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                options.forEachIndexed { idx, opt ->
                                    val isSelected = userVoteIndex == idx
                                    val percent = if (userVoteIndex != null) {
                                        if (isSelected) 100 else 0
                                    } else 0

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) {
                                            if (isFromMe) Color.White.copy(alpha = 0.28f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                        } else {
                                            if (isFromMe) Color.Black.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clickable {
                                                userVoteIndex = if (isSelected) null else idx
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${if (isSelected) "✓ " else "○ "}$opt",
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = textColor,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (userVoteIndex != null) {
                                                    Text(
                                                        text = "$percent%",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textColor.copy(alpha = 0.85f)
                                                    )
                                                }
                                            }
                                            if (userVoteIndex != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = { percent / 100f },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp)),
                                                    color = if (isFromMe) Color.White else MaterialTheme.colorScheme.primary,
                                                    trackColor = if (isFromMe) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (userVoteIndex != null) "✓ 1 vote enregistré • Choix unique" else "Touchez une option pour voter",
                                    fontSize = 10.5.sp,
                                    color = textColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                        MessageType.LOCATION.name -> {
                            val locText = message.text.ifEmpty { "Port-au-Prince, Haïti" }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isFromMe) Color.Black.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .widthIn(min = 220.dp, max = 280.dp)
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = if (isFromMe) Color.White else Color(0xFFEF4444),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Position partagée",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = textColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = locText,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📍 Coordonnées GPS certifiées • James Chat Live",
                                        fontSize = 10.sp,
                                        color = textColor.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        else -> {
                            val displayText = if (isTranslated && translatedText != null) translatedText else message.text
                            Text(
                                text = displayText,
                                color = textColor,
                                fontSize = 14.5.sp,
                                lineHeight = 19.sp
                            )
                            if (isTranslated) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isFromMe) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "🌐 Tradiksyon (${lang.displayName})",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isFromMe) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (message.isEdited) {
                            Text(
                                text = "(modifié)",
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                        if (message.expiresAt > 0) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = "Éphémère",
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        if (message.isStarred) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favori",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        if (message.isPinned) {
                            Icon(
                                imageVector = Icons.Default.Pin,
                                contentDescription = "Épinglé",
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        if (message.type == MessageType.TEXT.name) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier la réponse",
                                tint = textColor.copy(alpha = 0.75f),
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { onCopy(message) }
                            )
                        }
                        Text(
                            text = timeFormatted,
                            fontSize = 10.sp,
                            color = textColor.copy(alpha = 0.75f)
                        )
                        if (isFromMe) {
                            when (message.status) {
                                MessageDeliveryStatus.PENDING.name -> {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "En attente",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                MessageDeliveryStatus.SENT.name -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Envoyé",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                MessageDeliveryStatus.DELIVERED.name -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Livré",
                                        tint = JamesSentGrey,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                MessageDeliveryStatus.READ.name -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Lu",
                                        tint = JamesReadBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (reactions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(top = 2.dp, start = 4.dp, end = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .combinedClickable(
                            onClick = { onViewReactions(message) }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val grouped = reactions.groupBy { it.emoji }
                    grouped.forEach { (emoji, list) ->
                        Text(text = "$emoji ${if (list.size > 1) list.size else ""}", fontSize = 11.sp)
                    }
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(JamesI18n.t("react", lang)) },
                    onClick = {
                        showMenu = false
                        showReactionPicker = true
                    }
                )
                DropdownMenuItem(
                    text = { Text(JamesI18n.t("reply", lang)) },
                    onClick = {
                        showMenu = false
                        onReply(message)
                    }
                )
                DropdownMenuItem(
                    text = { Text(JamesI18n.t("copy", lang)) },
                    onClick = {
                        showMenu = false
                        onCopy(message)
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(if (isTranslated) "Texte original 🔄" else "Traduire en ${lang.displayName} 🌐")
                    },
                    onClick = {
                        showMenu = false
                        isTranslated = !isTranslated
                    }
                )
                DropdownMenuItem(
                    text = { Text(JamesI18n.t("forward", lang)) },
                    onClick = {
                        showMenu = false
                        onForward(message)
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (message.isPinned) JamesI18n.t("unpin", lang) else JamesI18n.t("pin", lang)) },
                    onClick = {
                        showMenu = false
                        onTogglePin(message)
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (message.isStarred) "Retirer favori" else "Ajouter aux favoris ⭐") },
                    onClick = {
                        showMenu = false
                        onToggleStar(message)
                    }
                )
                if (isFromMe && message.type == MessageType.TEXT.name) {
                    DropdownMenuItem(
                        text = { Text(JamesI18n.t("edit", lang)) },
                        onClick = {
                            showMenu = false
                            onEdit(message)
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(JamesI18n.t("delete", lang), color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showMenu = false
                        onDelete(message)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Signaler 🚨", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showMenu = false
                        onReport(message)
                    }
                )
            }
        }
    }
}
