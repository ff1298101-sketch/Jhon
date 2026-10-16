package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.UserEntity
import com.example.data.repository.JamesChatRepository
import com.example.ui.components.AvatarWithStatus
import com.example.ui.components.JamesVBadge
import com.example.ui.components.QuickProfileSheet
import com.example.ui.components.VideoPreviewDialog
import com.example.ui.theme.JamesOnlineGreen
import com.example.util.JamesI18n
import com.example.util.JamesIdentityUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ContactsListScreen(
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onStartChatWithUser: (UserEntity) -> Unit,
    onStartCallWithUser: (UserEntity, Boolean) -> Unit
) {
    val allUsers by repository.dao.getAllUsersFlow().collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForSheet by remember { mutableStateOf<UserEntity?>(null) }
    var userForVideoPreview by remember { mutableStateOf<UserEntity?>(null) }

    var showAddContactDialog by remember { mutableStateOf(false) }
    var showScanQrDialog by remember { mutableStateOf(false) }
    var showMyQrDialog by remember { mutableStateOf(false) }

    var exactMatchedUser by remember { mutableStateOf<UserEntity?>(null) }
    var isSearchingExact by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // 5-Way Search resolver
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.length >= 2) {
            isSearchingExact = true
            delay(150)
            val match = repository.findUserByContactMethod(q)
            exactMatchedUser = if (match != null && match.id != currentUser?.id) match else null
            isSearchingExact = false
        } else {
            exactMatchedUser = null
            isSearchingExact = false
        }
    }

    val filteredUsers = remember(allUsers, searchQuery, currentUser, exactMatchedUser) {
        val list = allUsers.filter { it.id != currentUser?.id && it.username.isNotEmpty() }
        if (searchQuery.isBlank()) {
            list
        } else {
            val q = searchQuery.lowercase().trim()
            val matches = list.filter { user ->
                val fourCode = JamesIdentityUtil.getFourDigitId(user)
                val tag = JamesIdentityUtil.getJamesTag(user).lowercase()
                val customId = JamesIdentityUtil.getJamesCustomId(user).lowercase()
                user.username.lowercase().contains(q) ||
                user.firstName.lowercase().contains(q) ||
                user.lastName.lowercase().contains(q) ||
                fourCode.contains(q) ||
                tag.contains(q) ||
                customId.contains(q)
            }.toMutableList()

            // If exact user found via 5-way method, prioritize at the top
            exactMatchedUser?.let { exact ->
                matches.removeAll { it.id == exact.id }
                matches.add(0, exact)
            }
            matches
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddContactDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_contact_fab")
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Ajouter un contact")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Quick Access Strip (Scan QR / Mon QR / Ajouter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { showScanQrDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scanner QR", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = { showMyQrDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mon QR Code", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = { showAddContactDialog = true },
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Universal 5-Way Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("JAMES#4827, @username, JMS-4827, 4827...", fontSize = 13.sp) },
                leadingIcon = {
                    if (isSearchingExact) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Effacer")
                        }
                    } else {
                        IconButton(onClick = { showScanQrDialog = true }) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scanner", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("contacts_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Method Hint Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    MethodChip(label = "🏷️ JAMES#4827", active = searchQuery.startsWith("JAMES#", ignoreCase = true)) {
                        searchQuery = "JAMES#"
                    }
                }
                item {
                    MethodChip(label = "👤 @username", active = searchQuery.startsWith("@")) {
                        searchQuery = "@"
                    }
                }
                item {
                    MethodChip(label = "🆔 JMS-4827", active = searchQuery.startsWith("JMS-", ignoreCase = true)) {
                        searchQuery = "JMS-"
                    }
                }
                item {
                    MethodChip(label = "🔢 4 Chiffres", active = searchQuery.all { it.isDigit() } && searchQuery.isNotEmpty()) {
                        searchQuery = "100"
                    }
                }
            }

            // Highlighted Exact Match Banner (if found via exact identifier)
            exactMatchedUser?.let { exact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { selectedUserForSheet = exact },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarWithStatus(
                            name = if (exact.firstName.isNotEmpty()) "${exact.firstName} ${exact.lastName}" else exact.username,
                            avatarUrl = exact.avatarUrl,
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (exact.firstName.isNotEmpty()) "${exact.firstName} ${exact.lastName}" else exact.username,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "✓ Vérifié",
                                    fontSize = 11.sp,
                                    color = JamesOnlineGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Tag: ${JamesIdentityUtil.getJamesTag(exact)} • @${exact.username}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = { onStartChatWithUser(exact) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Chat", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Contact List
            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "🔍 Aucun contact correspondant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Recherchez par Tag (JAMES#XXXX), @username, ID à 4 chiffres (XXXX), James ID (JMS-XXXX) ou scannez un QR Code.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = { showAddContactDialog = true }) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ajouter un contact")
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredUsers) { user ->
                        ContactItemRow(
                            user = user,
                            onOpenSheet = { selectedUserForSheet = user },
                            onStartChat = { onStartChatWithUser(user) },
                            onStartAudioCall = { onStartCallWithUser(user, false) },
                            onStartVideoCall = { onStartCallWithUser(user, true) },
                            onWatchVideo = { userForVideoPreview = user }
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
    }

    // Modal: Quick Profile Sheet
    if (selectedUserForSheet != null) {
        QuickProfileSheet(
            targetUser = selectedUserForSheet!!,
            isContact = true,
            isBlocked = false,
            lang = currentLang,
            onDismiss = { selectedUserForSheet = null },
            onSendMessage = { user ->
                selectedUserForSheet = null
                onStartChatWithUser(user)
            },
            onStartCall = { user, isVideo ->
                selectedUserForSheet = null
                onStartCallWithUser(user, isVideo)
            },
            onToggleContact = { user ->
                scope.launch { repository.addContact(user.id) }
            },
            onToggleBlock = { user ->
                scope.launch { repository.setContactBlocked(user.id, true) }
            },
            onReport = { user ->
                scope.launch { repository.reportUser(user, "Signalement rapide", "") }
            },
            onWatchProfileVideo = { user ->
                userForVideoPreview = user
            }
        )
    }

    // Dialog: Video Preview
    if (userForVideoPreview != null) {
        VideoPreviewDialog(
            title = "Vidéo de @${userForVideoPreview!!.username}",
            videoUrl = userForVideoPreview!!.profileVideoUrl,
            onDismiss = { userForVideoPreview = null }
        )
    }

    // Dialog: Mon QR Code Personnel
    if (showMyQrDialog && currentUser != null) {
        MyQrCodeDialog(
            user = currentUser!!,
            onDismiss = { showMyQrDialog = false }
        )
    }

    // Dialog: Scanner QR Code
    if (showScanQrDialog) {
        ScanQrCodeDialog(
            repository = repository,
            onDismiss = { showScanQrDialog = false },
            onUserResolved = { foundUser ->
                showScanQrDialog = false
                selectedUserForSheet = foundUser
            }
        )
    }

    // Dialog: Ajouter un Contact (5 Méthodes)
    if (showAddContactDialog) {
        AddContactMultiMethodDialog(
            repository = repository,
            onDismiss = { showAddContactDialog = false },
            onUserSelected = { foundUser ->
                showAddContactDialog = false
                selectedUserForSheet = foundUser
            },
            onScanQrClicked = {
                showAddContactDialog = false
                showScanQrDialog = true
            }
        )
    }
}

@Composable
private fun MethodChip(label: String, active: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ContactItemRow(
    user: UserEntity,
    onOpenSheet: () -> Unit,
    onStartChat: () -> Unit,
    onStartAudioCall: (() -> Unit)? = null,
    onStartVideoCall: (() -> Unit)? = null,
    onWatchVideo: (() -> Unit)? = null
) {
    val fourDigit = JamesIdentityUtil.getFourDigitId(user)
    val jamesTag = JamesIdentityUtil.getJamesTag(user)
    val jamesCustomId = JamesIdentityUtil.getJamesCustomId(user)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenSheet() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("contact_row_${user.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarWithStatus(
            name = if (user.firstName.isNotEmpty()) "${user.firstName} ${user.lastName}" else user.username,
            avatarUrl = user.avatarUrl,
            hasProfileVideo = user.profileVideoUrl.isNotEmpty() && user.showVideoOnProfile,
            isOnline = user.isOnline,
            size = 50.dp,
            onClick = {
                if (user.profileVideoUrl.isNotEmpty() && user.showVideoOnProfile && onWatchVideo != null) {
                    onWatchVideo()
                } else {
                    onOpenSheet()
                }
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (user.firstName.isNotEmpty()) "${user.firstName} ${user.lastName}" else user.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    JamesVBadge(
                        size = 14.dp,
                        style = com.example.data.model.VBadgeStyle.fromCode(user.verificationBadgeStyle),
                        userAccountName = user.username
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Primary Identifier Display: JAMES#4827 Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = jamesTag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "@${user.username} • $jamesCustomId",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        IconButton(onClick = onStartChat, modifier = Modifier.testTag("contact_start_chat_btn")) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "Message",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        if (onStartAudioCall != null) {
            IconButton(onClick = onStartAudioCall, modifier = Modifier.testTag("contact_audio_call_btn")) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Appel audio",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (onStartVideoCall != null) {
            IconButton(onClick = onStartVideoCall, modifier = Modifier.testTag("contact_video_call_btn")) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Appel vidéo",
                    tint = JamesOnlineGreen
                )
            }
        }
    }
}

/**
 * Dialog showing current user's personal QR Code and all identifiers.
 */
@Composable
fun MyQrCodeDialog(
    user: UserEntity,
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val fourDigit = JamesIdentityUtil.getFourDigitId(user)
    val jamesTag = JamesIdentityUtil.getJamesTag(user)
    val jamesCustomId = JamesIdentityUtil.getJamesCustomId(user)
    val qrBitmap = remember(user) {
        JamesIdentityUtil.generateUserQrCodeBitmap(user, size = 300)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Mon QR Code James Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                // Primary James Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = jamesTag,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Mon QR Code",
                        modifier = Modifier
                            .size(230.dp)
                            .padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tag Principal :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(jamesTag, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ID à 4 chiffres :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(fourDigit, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Nom d'utilisateur :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("@${user.username}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("James ID :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(jamesCustomId, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val payload = JamesIdentityUtil.getQrPayload(user)
                    clipboard.setText(AnnotatedString(payload))
                }
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copier le lien")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

/**
 * QR Code Scanner simulation dialog:
 * Allows scanning via animated simulated viewfinder, pasting payload, or quick demo test.
 */
@Composable
fun ScanQrCodeDialog(
    repository: JamesChatRepository,
    onDismiss: () -> Unit,
    onUserResolved: (UserEntity) -> Unit
) {
    var rawInput by remember { mutableStateOf("") }
    var resolvedUser by remember { mutableStateOf<UserEntity?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Scanner laser animation
    val transition = rememberInfiniteTransition(label = "scan")
    val laserY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    fun resolve(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        isChecking = true
        errorMessage = null
        scope.launch {
            val user = repository.findUserByContactMethod(trimmed)
            isChecking = false
            if (user != null) {
                resolvedUser = user
            } else {
                errorMessage = "Aucun utilisateur James Chat trouvé pour ce QR Code."
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scanner un QR Code", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Viewfinder Frame
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(120.dp)
                    )

                    // Laser beam
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .padding(horizontal = 12.dp)
                            .align(Alignment.TopCenter)
                            .padding(top = (laserY * 180).dp)
                            .background(Color(0xFF00FF66))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pointez vers le QR Code ou collez le lien / identifiant ci-dessous :",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rawInput,
                    onValueChange = {
                        rawInput = it
                        resolve(it)
                    },
                    placeholder = { Text("Collez le lien ou JAMES#4827", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        if (isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    }
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // Resolved preview
                resolvedUser?.let { user ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarWithStatus(
                                name = if (user.firstName.isNotEmpty()) "${user.firstName} ${user.lastName}" else user.username,
                                avatarUrl = user.avatarUrl,
                                size = 42.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (user.firstName.isNotEmpty()) "${user.firstName} ${user.lastName}" else user.username,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${JamesIdentityUtil.getJamesTag(user)} • @${user.username}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Button(
                                onClick = { onUserResolved(user) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Ouvrir", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Demo Scan Buttons
                Text("Exemples de test rapides :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = {
                            rawInput = "JAMES#1001"
                            resolve("JAMES#1001")
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("JAMES#1001", fontSize = 10.sp)
                    }
                    FilledTonalButton(
                        onClick = {
                            rawInput = "JAMES#1002"
                            resolve("JAMES#1002")
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("JAMES#1002", fontSize = 10.sp)
                    }
                    FilledTonalButton(
                        onClick = {
                            rawInput = "4827"
                            resolve("4827")
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Code 4827", fontSize = 10.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (resolvedUser != null) {
                Button(onClick = { onUserResolved(resolvedUser!!) }) {
                    Text("Voir le profil")
                }
            } else {
                Button(onClick = { resolve(rawInput) }, enabled = rawInput.isNotBlank()) {
                    Text("Vérifier")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Multi-Method Add Contact Dialog:
 * Offers tabs for the 5 contact methods:
 * 1. James Tag (JAMES#4827) - Identifiant principal
 * 2. ID à 4 chiffres (4827)
 * 3. Nom d'utilisateur (@username)
 * 4. James ID (JMS-4827)
 * 5. QR Code
 */
@Composable
fun AddContactMultiMethodDialog(
    repository: JamesChatRepository,
    onDismiss: () -> Unit,
    onUserSelected: (UserEntity) -> Unit,
    onScanQrClicked: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var inputQuery by remember { mutableStateOf("") }
    var searchResultUser by remember { mutableStateOf<UserEntity?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var notFoundError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val tabTitles = listOf("James Tag", "4 Chiffres", "@Username", "James ID", "QR Code")

    fun performSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        isSearching = true
        notFoundError = false
        searchResultUser = null
        scope.launch {
            val user = repository.findUserByContactMethod(trimmed)
            isSearching = false
            if (user != null) {
                searchResultUser = user
            } else {
                notFoundError = true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajouter un contact", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("5 méthodes uniques de recherche vérifiée", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                inputQuery = when (index) {
                                    0 -> "JAMES#"
                                    1 -> ""
                                    2 -> "@"
                                    3 -> "JMS-"
                                    else -> ""
                                }
                                searchResultUser = null
                                notFoundError = false
                            },
                            text = { Text(title, fontSize = 10.sp, maxLines = 1) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTab) {
                    0 -> { // James Tag (JAMES#4827)
                        Text(
                            text = "🏷️ James Tag (Identifiant Principal)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Format : JAMES#4827. Chaque Tag est unique et garanti sans doublons.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = {
                                inputQuery = it.uppercase()
                                performSearch(it)
                            },
                            placeholder = { Text("ex: JAMES#4827") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    1 -> { // 4-digit code (4827)
                        Text(
                            text = "🔢 ID James Chat à 4 chiffres",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Exemple : 4827. Attribué de manière strictement unique lors de l'inscription.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = {
                                val filtered = it.filter { ch -> ch.isDigit() }.take(4)
                                inputQuery = filtered
                                if (filtered.length == 4) performSearch(filtered)
                            },
                            placeholder = { Text("ex: 4827") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    2 -> { // @username
                        Text(
                            text = "👤 Nom d'utilisateur unique",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Exemple : @jamesdesigner. Recherche directe avec @.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = {
                                inputQuery = it.lowercase().replace(" ", "_")
                                performSearch(it)
                            },
                            placeholder = { Text("ex: @jamesdesigner") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    3 -> { // James ID (JMS-4827)
                        Text(
                            text = "🆔 James ID Personnalisé",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Exemple : JMS-4827. Identifiant facilement partageable.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = {
                                inputQuery = it.uppercase()
                                performSearch(it)
                            },
                            placeholder = { Text("ex: JMS-4827") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    4 -> { // QR Code
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📷 Scan de QR Code Personnel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Chaque utilisateur possède son propre QR Code ouvrant directement son profil.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onScanQrClicked,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ouvrir le scanner QR")
                            }
                        }
                    }
                }

                if (isSearching) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vérification dans la base de données...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (notFoundError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aucun utilisateur correspondant. Identifiant non attribué ou inexistant.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                searchResultUser?.let { user ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarWithStatus(
                                    name = if (user.firstName.isNotEmpty()) "${user.firstName} ${user.lastName}" else user.username,
                                    avatarUrl = user.avatarUrl,
                                    size = 46.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (user.firstName.isNotEmpty()) "${user.firstName} ${user.lastName}" else user.username,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        if (user.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            JamesVBadge(
                                                size = 14.dp,
                                                style = com.example.data.model.VBadgeStyle.fromCode(user.verificationBadgeStyle),
                                                userAccountName = user.username
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${JamesIdentityUtil.getJamesTag(user)} • @${user.username}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "ID: ${JamesIdentityUtil.getFourDigitId(user)} • ${JamesIdentityUtil.getJamesCustomId(user)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onUserSelected(user) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sélectionner ce profil", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab != 4) {
                Button(onClick = { performSearch(inputQuery) }, enabled = inputQuery.isNotBlank()) {
                    Text("Rechercher")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
