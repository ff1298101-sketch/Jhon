package com.example

import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.AppLanguage
import com.example.data.model.UserEntity
import com.example.data.repository.JamesChatRepository
import com.example.ui.components.JamesBottomNav
import com.example.ui.components.JamesTopBar
import com.example.ui.components.MainTab
import com.example.ui.screens.ActiveCallScreen
import com.example.ui.screens.IncomingCallScreen
import com.example.data.model.CallType
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CallsListScreen
import com.example.ui.screens.ChannelDetailScreen
import com.example.ui.screens.ChannelsListScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChooseUsernameScreen
import com.example.ui.screens.ContactsListScreen
import com.example.ui.screens.ConversationsListScreen
import com.example.ui.screens.GlobalSearchScreen
import com.example.ui.screens.GroupsListScreen
import com.example.ui.screens.JamesGamesScreen
import com.example.ui.screens.JamesWebScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignupScreen
import com.example.ui.screens.StatusesListScreen
import com.example.ui.theme.JamesChatTheme
import com.example.util.JamesNotificationManager
import kotlinx.coroutines.launch

enum class Screen {
    SPLASH,
    LOGIN,
    SIGNUP,
    CHOOSE_USERNAME,
    MAIN,
    CHAT_DETAIL,
    CHANNEL_LIST,
    CHANNEL_DETAIL,
    PROFILE,
    SETTINGS,
    ADMIN,
    ACTIVE_CALL,
    INCOMING_CALL,
    JAMES_WEB,
    JAMES_GAMES,
    GLOBAL_SEARCH
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        JamesNotificationManager.init(this)

        val repository = JamesChatRepository.getInstance(applicationContext)

        setContent {
            val themeMode by repository.themeMode.collectAsState()
            val currentLang by repository.currentLanguage.collectAsState()
            val currentUser by repository.currentUser.collectAsState()
            val isOnline by repository.isOnline.collectAsState()

            var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
            var currentTab by remember { mutableStateOf(MainTab.CHATS) }

            // Sub-navigation state
            var selectedConversationId by remember { mutableStateOf("") }
            var selectedChannelId by remember { mutableStateOf("") }
            var activeCallTargetUser by remember { mutableStateOf<UserEntity?>(null) }
            var activeCallIsVideo by remember { mutableStateOf(false) }
            var incomingCallUser by remember { mutableStateOf<UserEntity?>(null) }
            var incomingCallIsVideo by remember { mutableStateOf(false) }
            var selectedGameId by remember { mutableStateOf<String?>(null) }

            val scope = rememberCoroutineScope()

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                val convFromIntent = intent.getStringExtra("conversation_id")
                if (!convFromIntent.isNullOrEmpty()) {
                    selectedConversationId = convFromIntent
                    currentScreen = Screen.CHAT_DETAIL
                }

                val incomingCallerId = intent.getStringExtra("incoming_call_user_id")
                val incomingCallerName = intent.getStringExtra("incoming_call_user_name") ?: "Contact"
                val isVideoIntent = intent.getBooleanExtra("incoming_call_is_video", false)
                if (!incomingCallerId.isNullOrEmpty()) {
                    val caller = repository.dao.getUserById(incomingCallerId) ?: UserEntity(
                        id = incomingCallerId,
                        email = "contact@jameschat.ht",
                        username = incomingCallerName,
                        firstName = incomingCallerName
                    )
                    if (intent.action == JamesNotificationManager.ACTION_ACCEPT_CALL) {
                        activeCallTargetUser = caller
                        activeCallIsVideo = isVideoIntent
                        currentScreen = Screen.ACTIVE_CALL
                    } else if (intent.action == JamesNotificationManager.ACTION_DECLINE_CALL) {
                        repository.recordIncomingCall(caller, isVideoIntent, 0, CallType.MISSED)
                        currentScreen = Screen.MAIN
                    } else {
                        incomingCallUser = caller
                        incomingCallIsVideo = isVideoIntent
                        currentScreen = Screen.INCOMING_CALL
                    }
                }
            }

            LaunchedEffect(currentUser) {
                if (currentUser == null && currentScreen != Screen.SPLASH && currentScreen != Screen.SIGNUP) {
                    currentScreen = Screen.LOGIN
                }
            }

            JamesChatTheme(themeMode = themeMode) {
                // Back handler for nested screens
                BackHandler(enabled = currentScreen != Screen.MAIN && currentScreen != Screen.LOGIN) {
                    when (currentScreen) {
                        Screen.CHAT_DETAIL -> currentScreen = Screen.MAIN
                        Screen.CHANNEL_DETAIL -> currentScreen = Screen.CHANNEL_LIST
                        Screen.CHANNEL_LIST -> currentScreen = Screen.MAIN
                        Screen.PROFILE -> currentScreen = Screen.MAIN
                        Screen.SETTINGS -> currentScreen = Screen.MAIN
                        Screen.JAMES_WEB -> currentScreen = Screen.MAIN
                        Screen.JAMES_GAMES -> currentScreen = Screen.MAIN
                        Screen.ADMIN -> currentScreen = Screen.MAIN
                        Screen.ACTIVE_CALL -> currentScreen = Screen.MAIN
                        Screen.INCOMING_CALL -> {
                            scope.launch {
                                incomingCallUser?.let {
                                    repository.recordIncomingCall(it, incomingCallIsVideo, 0, CallType.MISSED)
                                }
                            }
                            incomingCallUser = null
                            currentScreen = Screen.MAIN
                        }
                        Screen.SIGNUP -> currentScreen = Screen.LOGIN
                        Screen.CHOOSE_USERNAME -> currentScreen = Screen.MAIN
                        else -> {}
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            Screen.SPLASH -> {
                                com.example.ui.screens.JamesAnimatedSplashScreen(
                                    onFinished = {
                                        currentScreen = if (currentUser != null) Screen.MAIN else Screen.LOGIN
                                    }
                                )
                            }

                            Screen.LOGIN -> {
                                LoginScreen(
                                    repository = repository,
                                    currentLang = currentLang,
                                    onNavigateToSignup = { currentScreen = Screen.SIGNUP },
                                    onNavigateToChooseUsername = { currentScreen = Screen.CHOOSE_USERNAME },
                                    onLoginSuccess = { currentScreen = Screen.MAIN }
                                )
                            }

                            Screen.SIGNUP -> {
                                SignupScreen(
                                    repository = repository,
                                    currentLang = currentLang,
                                    onNavigateToLogin = { currentScreen = Screen.LOGIN },
                                    onSignupSuccess = { currentScreen = Screen.CHOOSE_USERNAME }
                                )
                            }

                            Screen.CHOOSE_USERNAME -> {
                                ChooseUsernameScreen(
                                    repository = repository,
                                    currentLang = currentLang,
                                    onUsernameSet = { currentScreen = Screen.MAIN }
                                )
                            }

                            Screen.MAIN -> {
                                Scaffold(
                                    topBar = {
                                        JamesTopBar(
                                            currentUser = currentUser,
                                            isOnline = isOnline,
                                            currentLang = currentLang,
                                            onLanguageChange = { repository.setLanguage(it) },
                                            onOpenSearch = {
                                                currentScreen = Screen.GLOBAL_SEARCH
                                            },
                                            onOpenProfile = { currentScreen = Screen.PROFILE },
                                            onOpenSettings = { currentScreen = Screen.SETTINGS },
                                            onOpenAdmin = { currentScreen = Screen.ADMIN },
                                            onOpenChannels = { currentScreen = Screen.CHANNEL_LIST },
                                            onOpenWeb = { currentScreen = Screen.JAMES_WEB },
                                            onOpenGames = { currentScreen = Screen.JAMES_GAMES },
                                            isAdmin = repository.isCurrentUserAdmin()
                                        )
                                    },
                                    bottomBar = {
                                        JamesBottomNav(
                                            currentTab = currentTab,
                                            onTabSelected = { currentTab = it },
                                            lang = currentLang
                                        )
                                    }
                                ) { innerPadding ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(innerPadding)
                                    ) {
                                        when (currentTab) {
                                            MainTab.CHATS -> {
                                                ConversationsListScreen(
                                                    repository = repository,
                                                    currentLang = currentLang,
                                                    onSelectConversation = { convId ->
                                                        selectedConversationId = convId
                                                        currentScreen = Screen.CHAT_DETAIL
                                                    },
                                                    onStartNewChat = {
                                                        currentTab = MainTab.CONTACTS
                                                    }
                                                )
                                            }

                                            MainTab.GROUPS -> {
                                                GroupsListScreen(
                                                    repository = repository,
                                                    currentLang = currentLang,
                                                    onSelectGroup = { groupId ->
                                                        selectedConversationId = groupId
                                                        currentScreen = Screen.CHAT_DETAIL
                                                    }
                                                )
                                            }

                                            MainTab.STATUS -> {
                                                StatusesListScreen(
                                                    repository = repository,
                                                    currentLang = currentLang
                                                )
                                            }

                                            MainTab.CALLS -> {
                                                CallsListScreen(
                                                    repository = repository,
                                                    currentLang = currentLang,
                                                    onStartCall = { targetUser, isVideo ->
                                                        activeCallTargetUser = targetUser
                                                        activeCallIsVideo = isVideo
                                                        currentScreen = Screen.ACTIVE_CALL
                                                    },
                                                    onSimulateIncomingCall = { caller, isVideo ->
                                                        incomingCallUser = caller
                                                        incomingCallIsVideo = isVideo
                                                        currentScreen = Screen.INCOMING_CALL
                                                    }
                                                )
                                            }

                                            MainTab.CONTACTS -> {
                                                ContactsListScreen(
                                                    repository = repository,
                                                    currentLang = currentLang,
                                                    onStartChatWithUser = { user ->
                                                        scope.launch {
                                                            val convId = repository.getOrCreateDirectConversation(user)
                                                            selectedConversationId = convId
                                                            currentScreen = Screen.CHAT_DETAIL
                                                        }
                                                    },
                                                    onStartCallWithUser = { user, isVideo ->
                                                        activeCallTargetUser = user
                                                        activeCallIsVideo = isVideo
                                                        currentScreen = Screen.ACTIVE_CALL
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Screen.CHAT_DETAIL -> {
                                ChatDetailScreen(
                                    conversationId = selectedConversationId,
                                    repository = repository,
                                    currentLang = currentLang,
                                    onBack = { currentScreen = Screen.MAIN },
                                    onStartCall = { user, isVideo ->
                                        activeCallTargetUser = user
                                        activeCallIsVideo = isVideo
                                        currentScreen = Screen.ACTIVE_CALL
                                    }
                                )
                            }

                            Screen.CHANNEL_LIST -> {
                                ChannelsListScreen(
                                    repository = repository,
                                    currentLang = currentLang,
                                    onBack = { currentScreen = Screen.MAIN },
                                    onSelectChannel = { channelId ->
                                        selectedChannelId = channelId
                                        currentScreen = Screen.CHANNEL_DETAIL
                                    }
                                )
                            }

                            Screen.CHANNEL_DETAIL -> {
                                ChannelDetailScreen(
                                    channelId = selectedChannelId,
                                    repository = repository,
                                    currentLang = currentLang,
                                    onBack = { currentScreen = Screen.CHANNEL_LIST }
                                )
                            }

                            Screen.PROFILE -> {
                                ProfileScreen(
                                    repository = repository,
                                    currentLang = currentLang,
                                    onBack = { currentScreen = Screen.MAIN },
                                    onLogout = { currentScreen = Screen.LOGIN }
                                )
                            }

                            Screen.SETTINGS -> {
                                SettingsScreen(
                                    repository = repository,
                                    currentLang = currentLang,
                                    onBack = { currentScreen = Screen.MAIN },
                                    onLogout = { currentScreen = Screen.LOGIN },
                                    onOpenWeb = { currentScreen = Screen.JAMES_WEB },
                                    onOpenAcademy = { currentScreen = Screen.JAMES_GAMES },
                                    onOpenGames = { currentScreen = Screen.JAMES_GAMES },
                                    onOpenAdmin = { currentScreen = Screen.ADMIN }
                                )
                            }

                            Screen.JAMES_WEB -> {
                                JamesWebScreen(
                                    repository = repository,
                                    currentLang = currentLang,
                                    onOpenGames = { gameId ->
                                        selectedGameId = gameId
                                        currentScreen = Screen.JAMES_GAMES
                                    },
                                    onBack = { currentScreen = Screen.MAIN }
                                )
                            }

                            Screen.JAMES_GAMES -> {
                                JamesGamesScreen(
                                    repository = repository,
                                    currentLang = currentLang,
                                    initialGameId = selectedGameId,
                                    onBack = {
                                        selectedGameId = null
                                        currentScreen = Screen.MAIN
                                    }
                                )
                            }

                            Screen.ADMIN -> {
                                if (repository.isCurrentUserAdmin()) {
                                    AdminDashboardScreen(
                                        repository = repository,
                                        currentLang = currentLang,
                                        onBack = { currentScreen = Screen.MAIN }
                                    )
                                } else {
                                    currentScreen = Screen.MAIN
                                }
                            }

                            Screen.ACTIVE_CALL -> {
                                if (activeCallTargetUser != null) {
                                    ActiveCallScreen(
                                        targetUser = activeCallTargetUser!!,
                                        isVideo = activeCallIsVideo,
                                        lang = currentLang,
                                        onEndCall = { duration ->
                                            scope.launch {
                                                repository.recordCall(
                                                    receiver = activeCallTargetUser!!,
                                                    isVideo = activeCallIsVideo,
                                                    durationSeconds = duration
                                                )
                                                currentScreen = Screen.MAIN
                                            }
                                        }
                                    )
                                } else {
                                    currentScreen = Screen.MAIN
                                }
                            }

                            Screen.INCOMING_CALL -> {
                                if (incomingCallUser != null) {
                                    val ringtone by repository.customRingtone.collectAsState()
                                    val isSilent by repository.isSilentMode.collectAsState()
                                    val isVib by repository.isVibrationEnabled.collectAsState()
                                    val vibPattern by repository.vibrationPattern.collectAsState()

                                    IncomingCallScreen(
                                        callerUser = incomingCallUser!!,
                                        isVideo = incomingCallIsVideo,
                                        lang = currentLang,
                                        customRingtone = ringtone,
                                        isSilentMode = isSilent,
                                        isVibrationEnabled = isVib,
                                        vibrationPattern = vibPattern,
                                        onAccept = {
                                            activeCallTargetUser = incomingCallUser
                                            activeCallIsVideo = incomingCallIsVideo
                                            incomingCallUser = null
                                            currentScreen = Screen.ACTIVE_CALL
                                        },
                                        onDecline = {
                                            scope.launch {
                                                repository.recordIncomingCall(
                                                    caller = incomingCallUser!!,
                                                    isVideo = incomingCallIsVideo,
                                                    durationSeconds = 0,
                                                    callType = CallType.MISSED
                                                )
                                            }
                                            incomingCallUser = null
                                            currentScreen = Screen.MAIN
                                        }
                                    )
                                } else {
                                    currentScreen = Screen.MAIN
                                }
                            }

                            Screen.GLOBAL_SEARCH -> {
                                GlobalSearchScreen(
                                    repository = repository,
                                    currentLang = currentLang,
                                    onBack = { currentScreen = Screen.MAIN },
                                    onSelectUser = { user ->
                                        scope.launch {
                                            val convId = repository.getOrCreateDirectConversation(user)
                                            selectedConversationId = convId
                                            currentScreen = Screen.CHAT_DETAIL
                                        }
                                    },
                                    onSelectConversation = { convId ->
                                        selectedConversationId = convId
                                        currentScreen = Screen.CHAT_DETAIL
                                    },
                                    onSelectChannel = { chanId ->
                                        selectedChannelId = chanId
                                        currentScreen = Screen.CHANNEL_DETAIL
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
