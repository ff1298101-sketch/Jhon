package com.example.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.AppLanguage
import com.example.data.model.StatusEntity
import com.example.data.model.StatusType
import com.example.data.repository.JamesChatRepository
import com.example.ui.components.AvatarWithStatus
import com.example.ui.theme.JamesBluePrimary
import com.example.util.JamesI18n
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusesListScreen(
    repository: JamesChatRepository,
    currentLang: AppLanguage
) {
    val statuses by repository.dao.getActiveStatusesFlow().collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var initialDialogTab by remember { mutableStateOf(0) } // 0: Text, 1: Photo, 2: Video
    var viewingStatus by remember { mutableStateOf<StatusEntity?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary mini FAB for Media (Photo/Video)
                FloatingActionButton(
                    onClick = {
                        initialDialogTab = 1
                        showCreateDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("add_media_status_fab")
                ) {
                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Statut Photo / Vidéo", modifier = Modifier.size(20.dp))
                }

                // Primary FAB for Status
                FloatingActionButton(
                    onClick = {
                        initialDialogTab = 0
                        showCreateDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_status_fab")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Nouveau statut")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // My status row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            initialDialogTab = 0
                            showCreateDialog = true
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        AvatarWithStatus(
                            name = currentUser?.firstName ?: "Moi",
                            avatarUrl = currentUser?.avatarUrl ?: "",
                            size = 54.dp,
                            showOnlineBadge = false
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(JamesBluePrimary)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Ajouter",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = JamesI18n.t("my_status", currentLang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Touchez pour publier un texte, une photo ou une vidéo",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            initialDialogTab = 1
                            showCreateDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Photo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = {
                            initialDialogTab = 2
                            showCreateDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Vidéo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Text(
                    text = JamesI18n.t("recent_updates", currentLang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            if (statuses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun statut récent.\nPartagez une pensée, une photo ou une vidéo avec vos proches !",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(statuses) { status ->
                    StatusItemRow(
                        status = status,
                        onClick = {
                            viewingStatus = status
                            scope.launch { repository.markStatusViewed(status.id) }
                        }
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
        CreateStatusDialog(
            lang = currentLang,
            initialTab = initialDialogTab,
            onDismiss = { showCreateDialog = false },
            onCreate = { type, content, caption, colorHex ->
                scope.launch {
                    repository.createStatus(
                        type = type,
                        content = content,
                        caption = caption,
                        backgroundColorHex = colorHex
                    )
                    showCreateDialog = false
                }
            }
        )
    }

    if (viewingStatus != null) {
        StatusViewerDialog(
            status = viewingStatus!!,
            onDismiss = { viewingStatus = null }
        )
    }
}

@Composable
fun StatusItemRow(
    status: StatusEntity,
    onClick: () -> Unit
) {
    val timeFormatted = remember(status.createdAt) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(status.createdAt))
    }

    val typeIcon = when (status.type) {
        StatusType.VIDEO.name -> "🎥 Vidéo"
        StatusType.PHOTO.name -> "📷 Photo"
        else -> "📝"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("status_row_${status.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarWithStatus(
            name = status.username,
            avatarUrl = status.userAvatar,
            size = 52.dp,
            showOnlineBadge = false
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = status.username,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            val summary = if (status.type == StatusType.TEXT.name) {
                status.content.take(35)
            } else if (status.caption.isNotEmpty()) {
                "$typeIcon • ${status.caption.take(30)}"
            } else {
                typeIcon
            }
            Text(
                text = "$timeFormatted • $summary",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CreateStatusDialog(
    lang: AppLanguage,
    initialTab: Int = 0,
    onDismiss: () -> Unit,
    onCreate: (StatusType, String, String, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) } // 0: Text, 1: Photo, 2: Video
    var textContent by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var selectedMediaUri by remember { mutableStateOf<String?>(null) }
    var selectedColor by remember { mutableStateOf("#1E3A8A") }
    val colors = listOf("#1E3A8A", "#0D9488", "#7C3AED", "#EA580C", "#0284C7", "#111827", "#BE123C")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = JamesI18n.t("add_status", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Tabs: Texte, Photo, Vidéo
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Texte", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Photo", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Vidéo", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // TEXT STATUS
                        OutlinedTextField(
                            value = textContent,
                            onValueChange = { textContent = it },
                            placeholder = { Text("Quoi de neuf aujourd'hui ? Partagez une pensée...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("status_content_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "Couleur de fond :",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            colors.forEach { hex ->
                                val color = Color(android.graphics.Color.parseColor(hex))
                                val isSelected = selectedColor == hex
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = hex }
                                )
                            }
                        }
                    }

                    1 -> {
                        // PHOTO STATUS
                        if (selectedMediaUri == null) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Choisir une Photo dans la galerie", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = selectedMediaUri,
                                    contentDescription = "Aperçu de la photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { selectedMediaUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        OutlinedTextField(
                            value = caption,
                            onValueChange = { caption = it },
                            placeholder = { Text("Ajouter une légende (optionnel)...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    2 -> {
                        // VIDEO STATUS
                        if (selectedMediaUri == null) {
                            OutlinedButton(
                                onClick = {
                                    videoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        modifier = Modifier.size(34.dp),
                                        tint = Color(0xFFE11D48)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Choisir une Vidéo pour le Statut", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE11D48).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Videocam,
                                                contentDescription = null,
                                                tint = Color(0xFFE11D48),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Vidéo sélectionnée", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Prête pour publication", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                        }
                                    }

                                    IconButton(onClick = { selectedMediaUri = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = caption,
                            onValueChange = { caption = it },
                            placeholder = { Text("Légende de la vidéo (optionnel)...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            val canPublish = when (selectedTab) {
                0 -> textContent.isNotBlank()
                1 -> selectedMediaUri != null
                2 -> selectedMediaUri != null
                else -> false
            }

            Button(
                onClick = {
                    when (selectedTab) {
                        0 -> {
                            if (textContent.isNotBlank()) {
                                onCreate(StatusType.TEXT, textContent, "", selectedColor)
                            }
                        }
                        1 -> {
                            selectedMediaUri?.let { uri ->
                                onCreate(StatusType.PHOTO, uri, caption, selectedColor)
                            }
                        }
                        2 -> {
                            selectedMediaUri?.let { uri ->
                                onCreate(StatusType.VIDEO, uri, caption, selectedColor)
                            }
                        }
                    }
                },
                enabled = canPublish,
                colors = ButtonDefaults.buttonColors(containerColor = JamesBluePrimary),
                modifier = Modifier.testTag("publish_status_btn")
            ) {
                Text(JamesI18n.t("publish_status", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun StatusViewerDialog(
    status: StatusEntity,
    onDismiss: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var isVideoMuted by remember { mutableStateOf(false) }

    val isVideo = status.type == StatusType.VIDEO.name || status.content.endsWith(".mp4", ignoreCase = true)
    val isImage = status.type == StatusType.PHOTO.name || status.content.startsWith("content://") && !isVideo || status.content.startsWith("http") && !isVideo

    // Duration: 15s for video, 6s for text/photo
    val totalMs = if (isVideo) 15000L else 6000L

    LaunchedEffect(isPaused) {
        val stepMs = 50L
        while (!isPaused && progress < 1.0f) {
            delay(stepMs)
            progress += (stepMs.toFloat() / totalMs.toFloat())
            if (progress >= 1.0f) {
                onDismiss()
                break
            }
        }
    }

    val bgColor = remember(status.backgroundColorHex) {
        try {
            Color(android.graphics.Color.parseColor(status.backgroundColorHex))
        } catch (_: Exception) {
            Color(0xFF0F172A)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isVideo || isImage) Color.Black else bgColor)
                .clickable {
                    // Tap to pause/resume
                    isPaused = !isPaused
                }
        ) {
            // Main Media Content
            if (isVideo) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            try {
                                setVideoURI(Uri.parse(status.content))
                                val controller = MediaController(ctx)
                                controller.setAnchorView(this)
                                setMediaController(controller)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    start()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (isImage) {
                AsyncImage(
                    model = status.content,
                    contentDescription = "Statut Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = status.content,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 38.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Top Header: Progress Bar, User Profile, Close Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarWithStatus(
                            name = status.username,
                            avatarUrl = status.userAvatar,
                            size = 38.dp,
                            showOnlineBadge = false
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = status.username,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            val timeFormatted = remember(status.createdAt) {
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(status.createdAt))
                            }
                            Text(
                                text = "$timeFormatted • ${if (isVideo) "Vidéo" else if (isImage) "Photo" else "Statut"}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isVideo) {
                            IconButton(onClick = { isPaused = !isPaused }) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (isPaused) "Play" else "Pause",
                                    tint = Color.White
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }
                }
            }

            // Bottom Caption if available
            if (status.caption.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = status.caption,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
