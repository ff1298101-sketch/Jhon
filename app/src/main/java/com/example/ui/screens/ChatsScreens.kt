package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Pin
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
import com.example.util.JamesVoicePlayer
import com.example.util.JamesVoiceRecorder
import com.example.ui.components.ForwardMessageDialog
import com.example.ui.components.GroupManageSheet
import com.example.ui.components.ReportContentDialog
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.UserEntity
import com.example.data.repository.JamesChatRepository
import com.example.ui.components.AvatarWithStatus
import com.example.ui.components.MessageBubble
import com.example.ui.components.ReactionDetailDialog
import com.example.ui.components.StickerPickerSheet
import com.example.ui.components.VideoPreviewDialog
import com.example.ui.theme.JamesBluePrimary
import com.example.ui.theme.JamesOnlineGreen
import com.example.ui.theme.JamesWarningAmber
import com.example.util.JamesI18n
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationsListScreen(
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onSelectConversation: (String) -> Unit,
    onStartNewChat: () -> Unit
) {
    val conversations by repository.dao.getAllConversationsFlow().collectAsState(initial = emptyList())
    val typingUsers by repository.typingUsers.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartNewChat,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("new_chat_fab")
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = "Nouvelle discussion")
            }
        }
    ) { padding ->
        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune discussion pour le moment.\nTouchez '+' pour démarrer une conversation.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
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
                items(conversations) { conv ->
                    ConversationItemRow(
                        conversation = conv,
                        typingText = typingUsers[conv.id],
                        lang = currentLang,
                        onClick = { onSelectConversation(conv.id) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItemRow(
    conversation: ConversationEntity,
    typingText: String?,
    lang: AppLanguage,
    onClick: () -> Unit
) {
    val timeFormatted = remember(conversation.lastMessageTimestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(conversation.lastMessageTimestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("conversation_row_${conversation.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarWithStatus(
            name = conversation.title,
            avatarUrl = conversation.avatarUrl,
            size = 52.dp,
            showOnlineBadge = false
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (conversation.isPinned) {
                        Icon(
                            imageVector = Icons.Default.Pin,
                            contentDescription = "Épinglé",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = conversation.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    val isBotRow = conversation.id == "conv_james_bot" || conversation.title.contains("James Bot", ignoreCase = true)
                    if (isBotRow) {
                        Spacer(modifier = Modifier.width(4.dp))
                        com.example.ui.components.JamesVBadge(
                            size = 14.dp,
                            style = com.example.data.model.VBadgeStyle.SEAL,
                            userAccountName = "James Bot"
                        )
                    }
                }
                Text(
                    text = timeFormatted,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (!typingText.isNullOrEmpty()) "✍️ $typingText ${JamesI18n.t("typing", lang)}" else conversation.lastMessageSnippet.ifEmpty { "Démarrer la discussion" },
                    fontSize = 13.5.sp,
                    color = if (!typingText.isNullOrEmpty()) JamesOnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                if (conversation.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("conversation_unread_badge_${conversation.id}")
                    ) {
                        Text(
                            text = if (conversation.unreadCount > 99) "99+" else "${conversation.unreadCount}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onBack: () -> Unit,
    onStartCall: (UserEntity, Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    val conversation by repository.dao.getConversationByIdFlow(conversationId).collectAsState(initial = null)
    val messages by repository.dao.getMessagesFlow(conversationId).collectAsState(initial = emptyList())
    val allReactions by repository.dao.getAllReactionsFlow().collectAsState(initial = emptyList())
    val allUsers by repository.dao.getAllUsersFlow().collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState()
    val typingUsers by repository.typingUsers.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var editingMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var showEphemeralMenu by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showStickerSheet by remember { mutableStateOf(false) }
    var viewingReactionsMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var previewVideoUrl by remember { mutableStateOf<String?>(null) }

    var messageToForward by remember { mutableStateOf<MessageEntity?>(null) }
    var messageToReport by remember { mutableStateOf<MessageEntity?>(null) }
    var messageToDelete by remember { mutableStateOf<MessageEntity?>(null) }
    var showGroupManageSheet by remember { mutableStateOf(false) }
    var showBotProfileSheet by remember { mutableStateOf(false) }
    var isSearchInChatActive by remember { mutableStateOf(false) }
    var chatSearchKeyword by remember { mutableStateOf("") }

    val currentGroup by repository.dao.getGroupByIdFlow(conversationId).collectAsState(initial = null)

    val isRecordingVoice by JamesVoiceRecorder.isRecording.collectAsState()
    val recordingVoiceSeconds by JamesVoiceRecorder.recordingSeconds.collectAsState()
    val liveRecordingAmplitudes by JamesVoiceRecorder.liveAmplitudes.collectAsState()

    LaunchedEffect(conversationId) {
        repository.markConversationAsRead(conversationId)
    }

    DisposableEffect(Unit) {
        onDispose {
            JamesVoicePlayer.stop()
            JamesVoiceRecorder.cancelRecording()
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            JamesVoiceRecorder.startRecording(context)
        } else {
            Toast.makeText(context, "Permission microphone requise pour enregistrer un message vocal", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(conversation?.draftText) {
        if (!conversation?.draftText.isNullOrEmpty() && inputText.isEmpty()) {
            inputText = conversation?.draftText ?: ""
        }
    }

    LaunchedEffect(inputText) {
        repository.saveConversationDraft(conversationId, inputText)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                repository.sendMediaMessage(
                    conversationId = conversationId,
                    type = MessageType.PHOTO,
                    mediaUrl = uri.toString(),
                    caption = "📷 Foto pèsonèl"
                )
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                repository.sendMediaMessage(
                    conversationId = conversationId,
                    type = MessageType.VIDEO,
                    mediaUrl = uri.toString(),
                    caption = "🎬 Vidéo pèsonèl"
                )
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
        repository.dao.resetConversationUnread(conversationId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (conversation?.isGroup == true) {
                                showGroupManageSheet = true
                            } else if (conversation?.title?.contains("James Bot", ignoreCase = true) == true || conversationId == "conv_james_bot") {
                                showBotProfileSheet = true
                            }
                        }
                    ) {
                        AvatarWithStatus(
                            name = conversation?.title ?: "Chat",
                            avatarUrl = conversation?.avatarUrl ?: "",
                            size = 38.dp,
                            showOnlineBadge = false
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = conversation?.title ?: "Discussion",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                val isBot = conversationId == "conv_james_bot" ||
                                        conversation?.title?.contains("James Bot", ignoreCase = true) == true
                                if (isBot) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    com.example.ui.components.JamesVBadge(
                                        size = 14.dp,
                                        style = com.example.data.model.VBadgeStyle.SEAL,
                                        userAccountName = "James Bot"
                                    )
                                }
                            }
                            val isTyping = typingUsers[conversationId] != null
                            Text(
                                text = if (isTyping) "✍️ ${JamesI18n.t("typing", currentLang)}" else if (conversation?.isGroup == true) "Groupe • Touchez pour infos" else "En ligne",
                                fontSize = 11.sp,
                                color = if (isTyping) JamesOnlineGreen else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isSearchInChatActive = !isSearchInChatActive },
                        modifier = Modifier.testTag("chat_search_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Rechercher dans le chat")
                    }
                    IconButton(
                        onClick = {
                            val otherUserId = conversation?.participantIds?.split(",")?.map { it.trim() }?.find { it != currentUser?.id && it.isNotEmpty() }
                            val target = if (otherUserId != null) allUsers.find { it.id == otherUserId } else null
                            val targetUser = target ?: UserEntity(
                                id = otherUserId ?: "call-partner",
                                email = "partner@jameschat.ht",
                                username = conversation?.title ?: "Contact",
                                firstName = conversation?.title ?: "Contact",
                                avatarUrl = conversation?.avatarUrl ?: ""
                            )
                            onStartCall(targetUser, false)
                        },
                        modifier = Modifier.testTag("chat_audio_call_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Appel audio")
                    }
                    IconButton(
                        onClick = {
                            val otherUserId = conversation?.participantIds?.split(",")?.map { it.trim() }?.find { it != currentUser?.id && it.isNotEmpty() }
                            val target = if (otherUserId != null) allUsers.find { it.id == otherUserId } else null
                            val targetUser = target ?: UserEntity(
                                id = otherUserId ?: "call-partner",
                                email = "partner@jameschat.ht",
                                username = conversation?.title ?: "Contact",
                                firstName = conversation?.title ?: "Contact",
                                avatarUrl = conversation?.avatarUrl ?: ""
                            )
                            onStartCall(targetUser, true)
                        },
                        modifier = Modifier.testTag("chat_video_call_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Appel vidéo")
                    }

                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(JamesI18n.t("ephemeral_title", currentLang)) },
                                onClick = {
                                    showOptionsMenu = false
                                    showEphemeralMenu = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Fond d'écran du chat") },
                                onClick = {
                                    showOptionsMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Effacer le contenu", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showOptionsMenu = false
                                    scope.launch {
                                        repository.dao.clearConversationMessages(conversationId)
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = Modifier.imePadding()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (conversation?.ephemeralDurationMs ?: 0L > 0L) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Messages éphémères activés (disparaissent après lecture)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (!conversation?.pinnedMessageText.isNullOrEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Pin, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Épinglé : ${conversation?.pinnedMessageText}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { scope.launch { repository.setChatPinnedMessage(conversationId, null, null) } },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Détacher", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            if (isSearchInChatActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatSearchKeyword,
                        onValueChange = { chatSearchKeyword = it },
                        placeholder = { Text("Rechercher dans cette discussion...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(20.dp)
                    )
                    IconButton(onClick = {
                        isSearchInChatActive = false
                        chatSearchKeyword = ""
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }
            }

            val displayedMessages = remember(messages, chatSearchKeyword) {
                if (chatSearchKeyword.trim().isEmpty()) messages
                else messages.filter { it.text.contains(chatSearchKeyword, ignoreCase = true) }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                items(displayedMessages) { msg ->
                    val msgReactions = allReactions.filter { it.messageId == msg.id }
                    MessageBubble(
                        message = msg,
                        isFromMe = msg.senderId == currentUser?.id,
                        reactions = msgReactions,
                        lang = currentLang,
                        onReply = { replyingToMessage = it },
                        onEdit = {
                            editingMessage = it
                            inputText = it.text
                        },
                        onDelete = { messageToDelete = it },
                        onCopy = { clipboardManager.setText(AnnotatedString(it.text)) },
                        onForward = { messageToForward = it },
                        onTogglePin = {
                            scope.launch {
                                val newPinned = !msg.isPinned
                                repository.togglePinMessage(msg.id, newPinned)
                                if (newPinned) {
                                    repository.setChatPinnedMessage(conversationId, msg.id, msg.text)
                                } else if (conversation?.pinnedMessageId == msg.id) {
                                    repository.setChatPinnedMessage(conversationId, null, null)
                                }
                            }
                        },
                        onAddReaction = { targetMsg, emoji ->
                            scope.launch { repository.addOrToggleReaction(targetMsg.id, emoji) }
                        },
                        onViewReactions = { viewingReactionsMessage = it },
                        onOpenMedia = { url, type ->
                            if (type == MessageType.VIDEO) {
                                previewVideoUrl = url.ifEmpty { "demo_video" }
                            }
                        },
                        onToggleStar = { targetMsg ->
                            scope.launch { repository.toggleStarMessage(targetMsg.id, !targetMsg.isStarred) }
                        },
                        onReport = { targetMsg ->
                            messageToReport = targetMsg
                        }
                    )
                }
            }

            val isChatWithJamesBot = conversation?.title?.contains("James Bot", ignoreCase = true) == true ||
                    conversationId == "conv_james_bot"

            if (isChatWithJamesBot) {
                val suggestions = remember(currentLang) {
                    com.example.util.JamesBotEngine.getQuickSuggestions(currentLang)
                }
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(suggestions) { sugg ->
                        Card(
                            modifier = Modifier.clickable {
                                inputText = sugg
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF7C3AED).copy(alpha = 0.12f)
                            )
                        ) {
                            Text(
                                text = sugg,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7C3AED),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (replyingToMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Réponse à ${replyingToMessage?.senderUsername}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = replyingToMessage?.text ?: "",
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { replyingToMessage = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Annuler")
                        }
                    }
                }
            }

            if (editingMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Modification du message...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        IconButton(onClick = {
                            editingMessage = null
                            inputText = ""
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Annuler")
                        }
                    }
                }
            }

            // Mode "Seuls les administrateurs peuvent écrire" check
            val canSendInGroup = remember(currentGroup, currentUser) {
                val grp = currentGroup
                val user = currentUser
                if (grp == null || user == null) true
                else if (!grp.onlyAdminsCanSend) true
                else {
                    val isCreator = grp.creatorId == user.id
                    val isAdmin = grp.adminIds.split(",").contains(user.id) || grp.coAdminIds.split(",").contains(user.id)
                    isCreator || isAdmin
                }
            }

            if (!canSendInGroup) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = JamesWarningAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seuls les administrateurs peuvent envoyer des messages dans ce groupe.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Surface(
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isRecordingVoice) {
                        // Barre d'enregistrement audio en direct avec onde sonore animée WhatsApp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Point rouge clignotant
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935))
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Timer d'enregistrement
                            Text(
                                text = String.format("%02d:%02d", recordingVoiceSeconds / 60, recordingVoiceSeconds % 60),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFE53935)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Onde sonore animée en temps réel selon la voix au micro
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
                                    val canvasW = size.width
                                    val canvasH = size.height
                                    val bars = liveRecordingAmplitudes.takeLast(26)
                                    val count = if (bars.isNotEmpty()) bars.size else 16
                                    val spacing = 3.dp.toPx()
                                    val totalSpacing = spacing * (count - 1)
                                    val barWidth = ((canvasW - totalSpacing) / count).coerceAtLeast(2.dp.toPx())

                                    for (i in 0 until count) {
                                        val amp = if (i < bars.size) bars[i] else 0.25f
                                        val h = (amp * canvasH).coerceAtLeast(4.dp.toPx())
                                        val top = (canvasH - h) / 2f
                                        drawRoundRect(
                                            color = Color(0xFF0284C7),
                                            topLeft = androidx.compose.ui.geometry.Offset(i * (barWidth + spacing), top),
                                            size = androidx.compose.ui.geometry.Size(barWidth, h),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Bouton Annuler l'enregistrement
                            IconButton(
                                onClick = { JamesVoiceRecorder.cancelRecording() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("chat_cancel_voice_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Annuler",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Bouton Envoyer le message vocal
                            IconButton(
                                onClick = {
                                    val result = JamesVoiceRecorder.stopRecording()
                                    if (result != null) {
                                        scope.launch {
                                            repository.sendMediaMessage(
                                                conversationId = conversationId,
                                                type = MessageType.AUDIO,
                                                mediaUrl = result.filePath,
                                                durationSeconds = result.durationSeconds,
                                                waveformData = result.waveformData
                                            )
                                            com.example.util.JamesSoundPlayer.playSound(context, com.example.util.JamesSoundPlayer.SoundType.NEW_MESSAGE)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .testTag("chat_send_voice_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Envoyer vocal",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        // Saisie standard
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showStickerSheet = true },
                                modifier = Modifier.testTag("chat_open_stickers_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEmotions,
                                    contentDescription = "Stickers & Emojis",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.testTag("chat_attach_photo_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Photo, contentDescription = "Photo")
                            }

                            IconButton(
                                onClick = {
                                    videoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                modifier = Modifier.testTag("chat_attach_video_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Videocam, contentDescription = "Vidéo")
                            }

                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_field"),
                                placeholder = { Text(JamesI18n.t("type_message", currentLang), fontSize = 14.sp) },
                                maxLines = 4,
                                shape = RoundedCornerShape(20.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            if (inputText.trim().isEmpty()) {
                                IconButton(
                                    onClick = {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            JamesVoiceRecorder.startRecording(context)
                                        } else {
                                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .testTag("chat_record_audio_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Enregistrer vocal", tint = Color.White)
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            if (editingMessage != null) {
                                                repository.editMessage(editingMessage!!.id, inputText)
                                                editingMessage = null
                                            } else {
                                                repository.sendTextMessage(
                                                    conversationId = conversationId,
                                                    text = inputText,
                                                    replyToId = replyingToMessage?.id,
                                                    replyToText = replyingToMessage?.text,
                                                    replyToSender = replyingToMessage?.senderUsername
                                                )
                                                com.example.util.JamesSoundPlayer.playSound(context, com.example.util.JamesSoundPlayer.SoundType.NEW_MESSAGE)
                                                replyingToMessage = null
                                            }
                                            inputText = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .testTag("chat_send_btn")
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStickerSheet) {
        StickerPickerSheet(
            repository = repository,
            onDismiss = { showStickerSheet = false },
            onSelectSticker = { sticker ->
                scope.launch {
                    repository.sendStickerMessage(
                        conversationId = conversationId,
                        stickerItem = sticker,
                        replyToId = replyingToMessage?.id,
                        replyToText = replyingToMessage?.text,
                        replyToSender = replyingToMessage?.senderUsername
                    )
                    replyingToMessage = null
                    showStickerSheet = false
                }
            }
        )
    }

    if (viewingReactionsMessage != null) {
        val reactionsForTarget = allReactions.filter { it.messageId == viewingReactionsMessage?.id }
        ReactionDetailDialog(
            reactions = reactionsForTarget,
            currentUserId = currentUser?.id ?: "",
            onDismiss = { viewingReactionsMessage = null },
            onRemoveMyReaction = {
                scope.launch {
                    repository.removeReaction(viewingReactionsMessage!!.id)
                    viewingReactionsMessage = null
                }
            }
        )
    }

    if (previewVideoUrl != null) {
        VideoPreviewDialog(
            title = "Aperçu Vidéo James Chat",
            videoUrl = previewVideoUrl!!,
            onDismiss = { previewVideoUrl = null }
        )
    }

    if (messageToForward != null) {
        ForwardMessageDialog(
            message = messageToForward!!,
            repository = repository,
            onDismiss = { messageToForward = null },
            onForwardToConversation = { targetId ->
                scope.launch {
                    repository.forwardMessage(targetId, messageToForward!!)
                    messageToForward = null
                }
            }
        )
    }

    if (messageToReport != null) {
        ReportContentDialog(
            targetType = "MESSAGE",
            targetTitle = messageToReport!!.text.take(30),
            onDismiss = { messageToReport = null },
            onSubmitReport = { reason, details ->
                scope.launch {
                    repository.reportContent("MESSAGE", messageToReport!!.id, messageToReport!!.senderId, reason, details)
                    messageToReport = null
                }
            }
        )
    }

    if (messageToDelete != null) {
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("Supprimer le message ?") },
            text = { Text("Choisissez comment vous souhaitez supprimer ce message :") },
            confirmButton = {
                Column(horizontalAlignment = Alignment.End) {
                    if (messageToDelete?.senderId == currentUser?.id) {
                        Button(
                            onClick = {
                                scope.launch {
                                    repository.deleteMessageForEveryone(messageToDelete!!.id)
                                    messageToDelete = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Supprimer pour tout le monde")
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    TextButton(
                        onClick = {
                            scope.launch {
                                repository.deleteMessageForMe(messageToDelete!!.id)
                                messageToDelete = null
                            }
                        }
                    ) {
                        Text("Supprimer pour moi", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showEphemeralMenu) {
        AlertDialog(
            onDismissRequest = { showEphemeralMenu = false },
            title = { Text("Messages éphémères") },
            text = {
                Column {
                    Text("Choisissez la durée après laquelle les nouveaux messages disparaîtront automatiquement :", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(
                        "Désactivé" to 0L,
                        "1 heure" to 3600000L,
                        "24 heures" to 86400000L,
                        "7 jours" to 604800000L
                    ).forEach { (label, duration) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        repository.setEphemeralDuration(conversationId, duration)
                                        showEphemeralMenu = false
                                    }
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showEphemeralMenu = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    if (showGroupManageSheet && currentGroup != null) {
        GroupManageSheet(
            group = currentGroup!!,
            repository = repository,
            onDismiss = { showGroupManageSheet = false }
        )
    }

    if (showBotProfileSheet) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showBotProfileSheet = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            JamesBotProfileScreen(
                onBack = { showBotProfileSheet = false },
                onStartChat = { showBotProfileSheet = false }
            )
        }
    }
}
