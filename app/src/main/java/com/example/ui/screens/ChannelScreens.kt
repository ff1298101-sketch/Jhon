package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.AppLanguage
import com.example.data.model.ChannelCommentEntity
import com.example.data.model.ChannelEntity
import com.example.data.model.ChannelPostEntity
import com.example.data.model.MessageType
import com.example.data.repository.JamesChatRepository
import com.example.ui.components.AvatarWithStatus
import com.example.ui.components.JamesVBadge
import com.example.ui.components.VideoPreviewDialog
import com.example.util.JamesVoicePlayer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelsListScreen(
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onBack: () -> Unit,
    onSelectChannel: (String) -> Unit
) {
    val channels by repository.dao.getAllChannelsFlow().collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📢 Chaînes James Chat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_channel_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Créer une chaîne")
            }
        }
    ) { padding ->
        if (channels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune chaîne pour l'instant.\nAppuyez sur '+' pour lancer votre première chaîne !",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(channels) { channel ->
                    ChannelItemRow(
                        channel = channel,
                        onClick = { onSelectChannel(channel.id) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateChannelDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, handle, desc ->
                scope.launch {
                    val res = repository.createChannel(name, handle, desc)
                    showCreateDialog = false
                    res.onSuccess { ch -> onSelectChannel(ch.id) }
                }
            }
        )
    }
}

@Composable
fun ChannelItemRow(
    channel: ChannelEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("channel_row_${channel.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarWithStatus(
            name = channel.name,
            avatarUrl = channel.avatarUrl,
            size = 52.dp,
            showOnlineBadge = false
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = channel.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                if (channel.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    JamesVBadge(size = 14.dp)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "@${channel.username} • ${channel.subscribersCount} abonnés",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = channel.description.ifEmpty { "Canal d'information officiel style WhatsApp" },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelDetailScreen(
    channelId: String,
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onBack: () -> Unit
) {
    val channel by repository.dao.getChannelByIdFlow(channelId).collectAsState(initial = null)
    val posts by repository.getChannelPostsFlow(channelId).collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var postInputText by remember { mutableStateOf("") }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var selectedMediaType by remember { mutableStateOf<MessageType>(MessageType.TEXT) }
    var showMediaPickerMenu by remember { mutableStateOf(false) }

    // Dialog preview states
    var previewPhotoUrl by remember { mutableStateOf<String?>(null) }
    var previewVideoUrl by remember { mutableStateOf<String?>(null) }
    var viewingCommentsPostId by remember { mutableStateOf<String?>(null) }

    var isSubscribed by remember { mutableStateOf(false) }

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri
            selectedMediaType = MessageType.PHOTO
        }
    }

    // Video picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri
            selectedMediaType = MessageType.VIDEO
        }
    }

    LaunchedEffect(channelId) {
        val subs = repository.dao.getSubscriptionsForChannel(channelId)
        isSubscribed = subs.any { it.userId == currentUser?.id }
    }

    val isCreator = channel?.creatorId == currentUser?.id || repository.isCurrentUserAdmin()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarWithStatus(
                            name = channel?.name ?: "Canal",
                            avatarUrl = channel?.avatarUrl ?: "",
                            size = 38.dp,
                            showOnlineBadge = false
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = channel?.name ?: "Canal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                if (channel?.isVerified == true) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    JamesVBadge(size = 14.dp)
                                }
                            }
                            Text(
                                text = "${channel?.subscribersCount ?: 0} abonnés • Canal public",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            scope.launch {
                                isSubscribed = repository.toggleChannelSubscription(channelId)
                            }
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (isSubscribed) "Abonné ✓" else "S'abonner", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (posts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucune publication pour le moment sur cette chaîne.\nLes publications du créateur apparaîtront ici.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                items(posts) { post ->
                    ChannelPostCard(
                        post = post,
                        onReact = { emoji ->
                            scope.launch { repository.reactToChannelPost(post.id, emoji) }
                        },
                        onOpenPhoto = { url -> previewPhotoUrl = url },
                        onOpenVideo = { url -> previewVideoUrl = url },
                        onOpenComments = { viewingCommentsPostId = post.id },
                        onShare = {
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, "${post.authorUsername} sur James Chat:\n${post.text}")
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, "Partager la publication"))
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // CREATOR BROADCAST BAR (Photos, Vidéos, Vocal & Texte)
            if (isCreator) {
                Surface(
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // Attached media preview if selected
                        if (selectedMediaUri != null) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (selectedMediaType == MessageType.PHOTO) {
                                        AsyncImage(
                                            model = selectedMediaUri,
                                            contentDescription = "Photo attachée",
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VideoLibrary,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (selectedMediaType == MessageType.PHOTO) "📷 Photo prête à publier" else "🎬 Vidéo prête à publier",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Ajoutez une légende ci-dessous puis appuyez sur envoyer",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedMediaUri = null
                                            selectedMediaType = MessageType.TEXT
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Retirer", tint = Color.Red)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Bouton Photo
                            IconButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Envoyer Photo",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Bouton Vidéo
                            IconButton(
                                onClick = {
                                    videoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = "Envoyer Vidéo",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }

                            OutlinedTextField(
                                value = postInputText,
                                onValueChange = { postInputText = it },
                                placeholder = {
                                    Text(
                                        if (selectedMediaUri != null) "Légende pour la photo/vidéo..." else "Diffuser un message aux abonnés...",
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                maxLines = 4,
                                shape = RoundedCornerShape(20.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Bouton Publier
                            val canSend = postInputText.isNotBlank() || selectedMediaUri != null
                            IconButton(
                                onClick = {
                                    if (canSend) {
                                        val mediaStr = selectedMediaUri?.toString() ?: ""
                                        val type = selectedMediaType
                                        val textToSend = postInputText.trim()
                                        scope.launch {
                                            repository.publishChannelPost(
                                                channelId = channelId,
                                                text = textToSend,
                                                type = type,
                                                mediaUrl = mediaStr
                                            )
                                            postInputText = ""
                                            selectedMediaUri = null
                                            selectedMediaType = MessageType.TEXT
                                        }
                                    }
                                },
                                enabled = canSend,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (canSend) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Publier",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Photo Plein Écran
    previewPhotoUrl?.let { photoUrl ->
        ChannelPhotoViewerDialog(
            photoUrl = photoUrl,
            onDismiss = { previewPhotoUrl = null }
        )
    }

    // Modal Lecteur Vidéo Plein Écran
    previewVideoUrl?.let { videoUrl ->
        VideoPreviewDialog(
            title = "🎬 Vidéo Chaîne",
            videoUrl = videoUrl,
            onDismiss = { previewVideoUrl = null }
        )
    }

    // BottomSheet Commentaires
    viewingCommentsPostId?.let { postId ->
        ChannelCommentsBottomSheet(
            postId = postId,
            repository = repository,
            onDismiss = { viewingCommentsPostId = null }
        )
    }
}

@Composable
fun ChannelPostCard(
    post: ChannelPostEntity,
    onReact: (String) -> Unit,
    onOpenPhoto: (String) -> Unit,
    onOpenVideo: (String) -> Unit,
    onOpenComments: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    val timeFormatted = remember(post.createdAt) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        sdf.format(Date(post.createdAt))
    }

    val isPlayingAudio by JamesVoicePlayer.isPlaying.collectAsState()
    val playingAudioId by JamesVoicePlayer.currentPlayingId.collectAsState()
    val isThisAudioPlaying = isPlayingAudio && playingAudioId == post.id

    val hasPhoto = post.type == MessageType.PHOTO.name || post.mediaUrl.endsWith(".jpg", true) ||
            post.mediaUrl.endsWith(".png", true) || post.mediaUrl.endsWith(".jpeg", true) ||
            post.mediaUrl.startsWith("content://") && post.type == MessageType.PHOTO.name
    val hasVideo = post.type == MessageType.VIDEO.name || post.mediaUrl.endsWith(".mp4", true) ||
            post.mediaUrl.endsWith(".mkv", true) || post.mediaUrl.endsWith(".webm", true)
    val hasAudio = post.type == MessageType.AUDIO.name

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: auteur, badge vérifié et date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.authorUsername,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    JamesVBadge(size = 14.dp)
                }
                Text(
                    text = timeFormatted,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // PHOTO DISPLAY (Style WhatsApp)
            if (hasPhoto && post.mediaUrl.isNotBlank()) {
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = "Photo du canal",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 340.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenPhoto(post.mediaUrl) },
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // VIDEO DISPLAY (Style WhatsApp avec bouton Play)
            if (hasVideo && post.mediaUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .clickable { onOpenVideo(post.mediaUrl) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = "Lire la vidéo",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Toucher pour lire la vidéo",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // AUDIO NOTE DISPLAY
            if (hasAudio) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            JamesVoicePlayer.playOrPause(
                                context = context,
                                messageId = post.id,
                                audioPath = post.mediaUrl,
                                durationSeconds = 15
                            )
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = if (isThisAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause Audio",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isThisAudioPlaying) "Lecture en cours... 🎵" else "Note vocale de la chaîne 🎤",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // TEXT / CAPTION
            if (post.text.isNotBlank()) {
                Text(
                    text = post.text,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // INTERACTIONS BAR (Style WhatsApp: Réactions, Vues, Commentaires, Partage)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emojis de réaction
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("❤️", "🔥", "👍", "🇭🇹", "👏").forEach { emoji ->
                        val count = remember(post.reactionsJson, emoji) {
                            post.reactionsJson.split(",").count { it.trim() == emoji }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (count > 0) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .clickable { onReact(emoji) }
                                .padding(1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 16.sp)
                                if (count > 0) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "$count",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Actions: Vues, Commentaires, Partage
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Vues
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${post.viewsCount}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Commentaires
                    IconButton(
                        onClick = onOpenComments,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Commentaires",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Partager
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Partager",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modal d'affichage de photo plein écran
 */
@Composable
fun ChannelPhotoViewerDialog(
    photoUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }
                }

                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Photo agrandie",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 450.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

/**
 * Modal BottomSheet des commentaires pour une publication
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelCommentsBottomSheet(
    postId: String,
    repository: JamesChatRepository,
    onDismiss: () -> Unit
) {
    val comments by repository.getChannelCommentsFlow(postId).collectAsState(initial = emptyList())
    var commentText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Commentaires (${comments.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Soyez le premier à commenter cette publication !",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    items(comments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            AvatarWithStatus(
                                name = comment.username,
                                avatarUrl = comment.userAvatar,
                                size = 32.dp,
                                showOnlineBadge = false
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = comment.username,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = comment.text,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Écrire un commentaire...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            val text = commentText
                            commentText = ""
                            scope.launch {
                                repository.addChannelComment(postId, text)
                            }
                        }
                    },
                    enabled = commentText.isNotBlank(),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CreateChannelDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Créer une nouvelle chaîne", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la chaîne") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it },
                    label = { Text("Identifiant unique (@nom)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && handle.isNotBlank()) {
                        onCreate(name, handle, description)
                    }
                },
                enabled = name.isNotBlank() && handle.isNotBlank()
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

private suspend fun com.example.data.local.JamesChatDao.getSubscriptionsForChannel(channelId: String): List<com.example.data.model.ChannelSubscriptionEntity> {
    return emptyList()
}
