package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
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
    val currentUser by repository.currentUser.collectAsState()
    val currentUserId = currentUser?.id ?: ""
    val myContactUsers by repository.dao.getContactUsersFlow(currentUserId).collectAsState(initial = emptyList())
    val allUsers by repository.dao.getAllUsersFlow().collectAsState(initial = emptyList())

    var selectedSectionTab by remember { mutableIntStateOf(0) } // 0: Mes Contacts, 1: Annuaire James Chat
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForSheet by remember { mutableStateOf<UserEntity?>(null) }
    var userForVideoPreview by remember { mutableStateOf<UserEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }

    var isSyncingContacts by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    var showAddContactDialog by remember { mutableStateOf(false) }
    var showScanQrDialog by remember { mutableStateOf(false) }
    var showMyQrDialog by remember { mutableStateOf(false) }
    var showIdVerifierDialog by remember { mutableStateOf(false) }

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
            exactMatchedUser = match
            isSearchingExact = false
        } else {
            exactMatchedUser = null
            isSearchingExact = false
        }
    }

    val displayUsers = remember(myContactUsers, allUsers, searchQuery, currentUser, exactMatchedUser, selectedSectionTab) {
        val baseList = if (searchQuery.isNotBlank()) {
            allUsers.filter { it.id != currentUser?.id && it.username.isNotEmpty() }
        } else {
            if (selectedSectionTab == 0) myContactUsers else allUsers.filter { it.id != currentUser?.id && it.username.isNotEmpty() }
        }

        if (searchQuery.isBlank()) {
            baseList
        } else {
            val q = searchQuery.lowercase().trim()
            val matches = baseList.filter { user ->
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
            // Header Bar: Tabs & Sync Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TabRow(
                    selectedTabIndex = selectedSectionTab,
                    modifier = Modifier.weight(1f)
                ) {
                    Tab(
                        selected = selectedSectionTab == 0,
                        onClick = { selectedSectionTab = 0 },
                        text = { Text("Mes Contacts (${myContactUsers.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedSectionTab == 1,
                        onClick = { selectedSectionTab = 1 },
                        text = { Text("Annuaire Global", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        scope.launch {
                            isSyncingContacts = true
                            val res = repository.syncContacts()
                            isSyncingContacts = false
                            syncMessage = if (res.isSuccess) {
                                "Contacts synchronisés avec le serveur !"
                            } else {
                                "Synchronisation : mode hors ligne (cache SQLite conservé)"
                            }
                        }
                    },
                    modifier = Modifier.testTag("contacts_sync_btn")
                ) {
                    if (isSyncingContacts) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Actualiser les contacts", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Sync message banner
            if (syncMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = syncMessage!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f))
                        IconButton(onClick = { syncMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Quick Access Strip (Scan QR / Mon QR / Ajouter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
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

            // Highlighted Exact Match Banner (with full profile, chat, and call actions)
            exactMatchedUser?.let { exact ->
                val isExactAContact = myContactUsers.any { it.id == exact.id }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedUserForSheet = exact },
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
                                    if (exact.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        JamesVBadge(
                                            size = 14.dp,
                                            style = com.example.data.model.VBadgeStyle.fromCode(exact.verificationBadgeStyle),
                                            userAccountName = exact.username
                                        )
                                    }
                                }
                                Text(
                                    text = "Tag: ${JamesIdentityUtil.getJamesTag(exact)} • @${exact.username}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "James ID: ${JamesIdentityUtil.getJamesCustomId(exact)} • Code: ${JamesIdentityUtil.getFourDigitId(exact)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isSelf = exact.id == currentUser?.id

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = JamesOnlineGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSelf) "PWÒP KONT PA W • ID CONFIRMÉ & OPÉRATIONNEL 🟢" else "ID REYÈL TROUVÉ & CONFIRMÉ DANS LA BASE 🟢",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = JamesOnlineGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isSelf) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = android.content.Context.CLIPBOARD_SERVICE
                                        val tag = JamesIdentityUtil.getJamesTag(exact)
                                        syncMessage = "James Tag kopye : $tag"
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kopye Tag", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { showMyQrDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mon QR Code", fontSize = 11.sp)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedUserForSheet = exact },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Profil", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { onStartChatWithUser(exact) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Chat", fontSize = 11.sp)
                                }

                                FilledTonalButton(
                                    onClick = { onStartCallWithUser(exact, false) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Audio", fontSize = 11.sp)
                                }

                                FilledTonalButton(
                                    onClick = { onStartCallWithUser(exact, true) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Vidéo", fontSize = 11.sp)
                                }

                                if (!isExactAContact) {
                                    FilledTonalButton(
                                        onClick = {
                                            scope.launch {
                                                repository.addContact(exact.id)
                                                syncMessage = "Contact @${exact.username} ajouté avec succès !"
                                            }
                                        },
                                        modifier = Modifier.weight(1.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+Ajout", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Contact List
            if (displayUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "🔍 Aucun utilisateur correspondant" else if (selectedSectionTab == 0) "📇 Aucun contact enregistré" else "Annuaire vide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "Vérifiez le format de l'identifiant : JAMES#XXXX, @username, JMS-XXXX ou scannez un QR Code."
                            } else {
                                "Ajoutez vos amis et collègues facilement grâce à leur James Tag (JAMES#XXXX), @username ou James ID."
                            },
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
                    items(displayUsers) { user ->
                        val isContact = myContactUsers.any { it.id == user.id }
                        ContactItemRow(
                            user = user,
                            isContact = isContact,
                            onOpenSheet = { selectedUserForSheet = user },
                            onStartChat = { onStartChatWithUser(user) },
                            onStartAudioCall = { onStartCallWithUser(user, false) },
                            onStartVideoCall = { onStartCallWithUser(user, true) },
                            onWatchVideo = { userForVideoPreview = user },
                            onDeleteContact = if (isContact) { { userToDelete = user } } else null
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

    // Modal: Confirmation de suppression de contact
    if (userToDelete != null) {
        val u = userToDelete!!
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Supprimer le contact", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Voulez-vous supprimer @${u.username} (${u.firstName} ${u.lastName}) de votre liste de contacts ? Vous pourrez le rajouter à tout moment.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.removeContact(u.id)
                            syncMessage = "Contact @${u.username} supprimé."
                            userToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Modal: Quick Profile Sheet
    if (selectedUserForSheet != null) {
        val target = selectedUserForSheet!!
        val isTargetAContact = myContactUsers.any { it.id == target.id }
        QuickProfileSheet(
            targetUser = target,
            isContact = isTargetAContact,
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
                scope.launch {
                    if (isTargetAContact) {
                        repository.removeContact(user.id)
                        syncMessage = "Contact @${user.username} retiré."
                    } else {
                        val addRes = repository.addContact(user.id)
                        syncMessage = if (addRes.isSuccess) {
                            "Contact @${user.username} ajouté avec succès !"
                        } else {
                            addRes.exceptionOrNull()?.message ?: "Erreur d'ajout"
                        }
                    }
                }
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

    // Dialog: Verifikatè ID an Tan Reyèl
    if (showIdVerifierDialog) {
        RealTimeIdVerifierDialog(
            repository = repository,
            currentLang = currentLang,
            onDismiss = { showIdVerifierDialog = false },
            onStartChat = { foundUser ->
                showIdVerifierDialog = false
                onStartChatWithUser(foundUser)
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
    isContact: Boolean = true,
    onOpenSheet: () -> Unit,
    onStartChat: () -> Unit,
    onStartAudioCall: (() -> Unit)? = null,
    onStartVideoCall: (() -> Unit)? = null,
    onWatchVideo: (() -> Unit)? = null,
    onDeleteContact: (() -> Unit)? = null
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

        if (isContact && onDeleteContact != null) {
            IconButton(onClick = onDeleteContact, modifier = Modifier.testTag("contact_delete_btn_${user.id}")) {
                Icon(
                    imageVector = Icons.Default.PersonRemove,
                    contentDescription = "Supprimer le contact",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
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

                            var isAddedState by remember(user.id) { mutableStateOf(false) }
                            var feedbackMessage by remember(user.id) { mutableStateOf<String?>(null) }
                            LaunchedEffect(user.id) {
                                isAddedState = repository.isContact(user.id)
                            }

                            if (feedbackMessage != null) {
                                Text(
                                    text = feedbackMessage!!,
                                    fontSize = 11.5.sp,
                                    color = if (isAddedState) JamesOnlineGreen else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (isAddedState) {
                                            feedbackMessage = "Ce contact est déjà dans votre liste."
                                        } else {
                                            scope.launch {
                                                val res = repository.addContact(user.id)
                                                if (res.isSuccess) {
                                                    isAddedState = true
                                                    feedbackMessage = "✓ Ajouté avec succès !"
                                                } else {
                                                    feedbackMessage = res.exceptionOrNull()?.message ?: "Erreur d'ajout"
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isAddedState) Icons.Default.CheckCircle else Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isAddedState) JamesOnlineGreen else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isAddedState) "En contact" else "Ajouter", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { onUserSelected(user) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Discuter", fontSize = 12.sp)
                                }
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

/**
 * Verifikatè ID an Tan Reyèl / Testeur d'Identifiants Réels James Chat
 * Pèmèt verifye an dirèk si tout ID yo (4 Chiffres, @Username, JMS-ID, JAMES#TAG, QR Code)
 * mache reyèlman nan baz done a ak tout detay pwofil, estati ak bouton aksyon.
 */
@Composable
fun RealTimeIdVerifierDialog(
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onStartChat: (UserEntity) -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    var testQuery by remember { mutableStateOf("2001") }
    var verifiedUser by remember { mutableStateOf<UserEntity?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var searchExecuted by remember { mutableStateOf(false) }
    var copyNotice by remember { mutableStateOf<String?>(null) }
    var addedNotice by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    fun verifyId(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        isChecking = true
        searchExecuted = true
        scope.launch {
            delay(120) // fluid UI response
            val result = repository.findUserByContactMethod(trimmed)
            verifiedUser = result
            isChecking = false
        }
    }

    LaunchedEffect(Unit) {
        verifyId("2001")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(JamesOnlineGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = JamesOnlineGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Verifikatè ID an Tan Reyèl", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Verifye si ID yo mache reyèl nan baz done a", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Chwazi oswa tape yon ID pou teste :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = testQuery,
                    onValueChange = {
                        testQuery = it
                        verifyId(it)
                    },
                    placeholder = { Text("ex: 2001, @jean_b, JMS-2002, JAMES#1002") },
                    trailingIcon = {
                        if (isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else if (testQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                testQuery = ""
                                verifiedUser = null
                                searchExecuted = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Bouton tès rapid (Itilizatè Reyèl nan Baz Done a) :",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick test sample chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = testQuery == "2001",
                            onClick = {
                                testQuery = "2001"
                                verifyId("2001")
                            },
                            label = { Text("Sophie (2001)", fontSize = 10.5.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = testQuery == "2002",
                            onClick = {
                                testQuery = "2002"
                                verifyId("2002")
                            },
                            label = { Text("Jean (2002)", fontSize = 10.5.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = testQuery == "2003",
                            onClick = {
                                testQuery = "2003"
                                verifyId("2003")
                            },
                            label = { Text("Carlos (2003)", fontSize = 10.5.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = testQuery == "0001",
                            onClick = {
                                testQuery = "0001"
                                verifyId("0001")
                            },
                            label = { Text("Bot (0001)", fontSize = 10.5.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = testQuery == "1002",
                            onClick = {
                                testQuery = "1002"
                                verifyId("1002")
                            },
                            label = { Text("Admin (1002)", fontSize = 10.5.sp) }
                        )
                    }
                    if (currentUser != null && currentUser!!.fourDigitCode.isNotEmpty()) {
                        item {
                            FilterChip(
                                selected = testQuery == currentUser!!.fourDigitCode,
                                onClick = {
                                    val code = currentUser!!.fourDigitCode
                                    testQuery = code
                                    verifyId(code)
                                },
                                label = { Text("Kont pa m (${currentUser!!.fourDigitCode})", fontSize = 10.5.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Verification Result Card
                if (verifiedUser != null) {
                    val user = verifiedUser!!
                    val isSelf = currentUser?.id == user.id

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = JamesOnlineGreen.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, JamesOnlineGreen.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = JamesOnlineGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSelf) "PWÒP KONT PA W (ID VALAB & REYÈL)" else "ID VALIDE ET CONFIRMÉ DANS LA BASE",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JamesOnlineGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

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
                                            text = "${user.firstName} ${user.lastName}".trim().ifEmpty { user.username },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (user.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            JamesVBadge(
                                                size = 14.dp,
                                                style = com.example.data.model.VBadgeStyle.fromCode(user.verificationBadgeStyle)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "@${user.username} • ${user.role}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (user.isOnline) "🟢 En ligne actuellement" else "⚪ Hors ligne",
                                        fontSize = 10.5.sp,
                                        color = if (user.isOnline) JamesOnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = JamesOnlineGreen.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Identification breakdown
                            Text(text = "Détails d'identification certifiés :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "• James Tag : ${JamesIdentityUtil.getJamesTag(user)}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(text = "• James ID : ${JamesIdentityUtil.getJamesCustomId(user)}", fontSize = 11.5.sp)
                            Text(text = "• Code Unique : ${JamesIdentityUtil.getFourDigitId(user)}", fontSize = 11.5.sp)
                            Text(text = "• Nom d'utilisateur : @${user.username}", fontSize = 11.5.sp)
                            val badgeStyle = com.example.data.model.VBadgeStyle.fromCode(user.verificationBadgeStyle)
                            Text(text = "• Badge V : ${if (user.isVerified) "Certifié (${badgeStyle.title})" else "Non certifié"}", fontSize = 11.sp)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val tag = JamesIdentityUtil.getJamesTag(user)
                                        clipboard.setText(AnnotatedString(tag))
                                        copyNotice = "Tag kopye : $tag"
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kopye Tag", fontSize = 10.5.sp)
                                }

                                if (!isSelf) {
                                    FilledTonalButton(
                                        onClick = {
                                            scope.launch {
                                                val res = repository.addContact(user.id)
                                                addedNotice = if (res.isSuccess) "Kontak ajoute !" else "Deja nan kontak ou"
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ajoute", fontSize = 10.5.sp)
                                    }

                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onStartChat(user)
                                        },
                                        modifier = Modifier.weight(1.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Chat", fontSize = 10.5.sp)
                                    }
                                }
                            }

                            if (copyNotice != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = copyNotice!!, fontSize = 11.sp, color = JamesOnlineGreen, fontWeight = FontWeight.Bold)
                            }
                            if (addedNotice != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = addedNotice!!, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (searchExecuted && !isChecking && testQuery.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ID NON TROUVÉ DANS LA BASE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pa gen itilizatè ki gen ID '$testQuery' sa a. Eseye teste youn nan chips anwo yo (2001, 2002, 1002, 0001) oswa verifye fòma a.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { verifyId(testQuery) }) {
                Text("Verifye kounye a")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fèmen")
            }
        }
    )
}
