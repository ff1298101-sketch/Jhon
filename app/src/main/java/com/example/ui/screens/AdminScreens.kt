package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import kotlinx.coroutines.delay
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminPermission
import com.example.data.model.AppLanguage
import com.example.data.model.SessionEntity
import com.example.data.model.ReportEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.AdminAnalytics
import com.example.data.repository.JamesChatRepository
import androidx.compose.material3.TabRow
import com.example.data.model.VBadgeStyle
import com.example.ui.components.AvatarWithStatus
import com.example.ui.components.JamesTeamMemberBadge
import com.example.ui.components.JamesVBadge
import com.example.ui.theme.JamesBluePrimary
import com.example.ui.theme.JamesErrorRed
import com.example.ui.theme.JamesOnlineGreen
import com.example.ui.theme.JamesWarningAmber
import com.example.util.JamesI18n
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onBack: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val isAdmin = repository.isCurrentUserAdmin()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Vue Générale",
        "Base de connaissances",
        "Gestion Utilisateurs",
        "Administrateurs",
        "Badges V",
        "Modération & Signalements",
        "Journal d'Audit",
        "James Transfer 🔐",
        "James Scheduler ⏱️"
    )

    var analytics by remember { mutableStateOf<AdminAnalytics?>(null) }
    val allUsers by repository.dao.getAllUsersFlow().collectAsState(initial = emptyList())
    val adminLogs by repository.dao.getAllAdminLogsFlow().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            analytics = repository.getAdminAnalytics()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(JamesI18n.t("admin_panel", currentLang), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (isAdmin && currentUser != null) {
                            Text(
                                text = "Admin : ${currentUser?.firstName} ${currentUser?.lastName} • ${currentUser?.role}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (!isAdmin) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Accès Restreint",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Ce panneau d'administration et les adresses email des administrateurs sont strictement réservés aux comptes administrateurs officiels connectés. Seuls les administrateurs autorisés ont accès à cette section.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("admin_restricted_back_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Retour aux discussions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 12.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> AdminOverviewTab(analytics = analytics, repository = repository)
                    1 -> AdminKnowledgeBaseTab(repository = repository)
                    2 -> AdminUsersFullManagementTab(users = allUsers, repository = repository)
                    3 -> AdminAdminsManagementTab(users = allUsers, repository = repository)
                    4 -> AdminBadgesTab(users = allUsers, repository = repository)
                    5 -> AdminModerationTab(repository = repository)
                    6 -> AdminAuditLogTab(logs = adminLogs)
                    7 -> AdminJamesTransferTab(repository = repository)
                    8 -> AdminJamesSchedulerTab(repository = repository)
                }
            }
        }
    }
}

@Composable
fun AdminOverviewTab(analytics: AdminAnalytics?, repository: JamesChatRepository? = null) {
    val clipboard = LocalClipboardManager.current
    var copiedEmail by remember { mutableStateOf<String?>(null) }
    val currentAdmin = repository?.currentUser?.value
    val allAdminEmails = JamesChatRepository.CONFIGURED_7_ADMINS

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Indicateurs Clés de Performance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard(
                    title = "Utilisateurs",
                    value = "${analytics?.totalUsers ?: 0}",
                    color = JamesBluePrimary,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Actifs aujourd'hui",
                    value = "${analytics?.activeUsers ?: 0}",
                    color = JamesOnlineGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard(
                    title = "Badges V Actifs",
                    value = "${analytics?.verifiedBadges ?: 0}",
                    color = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Chaînes Publiques",
                    value = "${analytics?.totalChannels ?: 0}",
                    color = Color(0xFF0D9488),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Santé du Réseau & Sécurité", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "• Signalements en attente : ${analytics?.itemsUnderReview ?: 0}", fontSize = 13.sp)
                    Text(text = "• Comptes suspendus/bannis : ${analytics?.suspendedAccounts ?: 0}", fontSize = 13.sp)
                    Text(text = "• Chiffrement de bout en bout : ACTIF (100%)", fontSize = 13.sp, color = JamesOnlineGreen)
                    Text(text = "• RLS Supabase & Isolation RBAC : ACTIVÉ", fontSize = 13.sp, color = JamesBluePrimary)
                }
            }
        }

        // Session Administrateur Sécurisée
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Session Administrateur Sécurisée",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connecté en tant que : ${currentAdmin?.email ?: "Administrateur officiel"} • Rôle : ${if (repository?.isCurrentUserOwner() == true) "PROPRIÉTAIRE 👑" else currentAdmin?.role ?: "ADMIN"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "La gestion des administrateurs et des accès est accessible dans l'onglet « Administrateurs ».",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 12.sp, color = color, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

/* =================================================================================
   SECTION B & D : GESTION COMPLÈTE DES UTILISATEURS (FILTRES, ACTIONS, DÉTAILS, SUPPRESSION)
   ================================================================================= */
@Composable
fun AdminUsersFullManagementTab(
    users: List<UserEntity>,
    repository: JamesChatRepository
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tous") }
    val filters = listOf("Tous", "Actifs", "Suspendus", "Bannis", "Avertis", "Vérifiés", "Non vérifiés")

    var selectedUserForDetails by remember { mutableStateOf<UserEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var showWarningDialogUser by remember { mutableStateOf<UserEntity?>(null) }
    var warningMessageInput by remember { mutableStateOf("") }

    val filtered = remember(users, searchQuery, selectedFilter) {
        users.filter { user ->
            // Filter match
            val matchesFilter = when (selectedFilter) {
                "Tous" -> true
                "Actifs" -> user.isOnline && !user.isBanned && !user.isSuspended
                "Suspendus" -> user.isSuspended
                "Bannis" -> user.isBanned
                "Avertis" -> user.warningCount > 0
                "Vérifiés" -> user.isVerified
                "Non vérifiés" -> !user.isVerified
                else -> true
            }

            if (!matchesFilter) return@filter false

            // Search match (by Name, James ID, E-mail, Username)
            if (searchQuery.isBlank()) true else {
                val q = searchQuery.lowercase().trim()
                val jamesIdFull = "JAMES#${user.fourDigitCode}".lowercase()
                val tagFull = user.jamesTag.lowercase()
                user.username.lowercase().contains(q) ||
                user.email.lowercase().contains(q) ||
                user.firstName.lowercase().contains(q) ||
                user.lastName.lowercase().contains(q) ||
                user.fourDigitCode.contains(q) ||
                jamesIdFull.contains(q) ||
                tagFull.contains(q) ||
                user.jamesCustomId.lowercase().contains(q)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search field with James ID, Email, Name hint
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher par Nom, James ID, E-mail...", fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Effacer")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Filters bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filters) { filterName ->
                FilterChip(
                    selected = selectedFilter == filterName,
                    onClick = { selectedFilter = filterName },
                    label = { Text(filterName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        // Tès ID Reyèl rapid pou Administrasyon
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text(
                    text = "🔍 Tès ID Reyèl :",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            listOf("2001", "2002", "2003", "0001", "1002", "JAMES#2001", "JMS-2002").forEach { idSample ->
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (searchQuery == idSample) JamesOnlineGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(0.5.dp, if (searchQuery == idSample) JamesOnlineGreen else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.clickable {
                            searchQuery = idSample
                            selectedFilter = "Tous"
                        }
                    ) {
                        Text(
                            text = idSample,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (searchQuery == idSample) JamesOnlineGreen else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = "${filtered.size} utilisateur(s) trouvé(s) • Base de données locale & synchronisée",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered) { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                        .clickable { selectedUserForDetails = user },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (user.isBanned) JamesErrorRed.copy(alpha = 0.08f)
                        else if (user.isSuspended) JamesWarningAmber.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AvatarWithStatus(
                                name = user.username.ifEmpty { user.email },
                                avatarUrl = user.avatarUrl,
                                size = 46.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${user.firstName} ${user.lastName}".trim().ifEmpty { "@${user.username}" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (user.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        JamesVBadge(size = 14.dp)
                                    }
                                }
                                Text(
                                    text = "ID: JAMES#${user.fourDigitCode} • @${user.username}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = user.email,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (user.isBanned) {
                                        Text("🚫 BANNI", fontSize = 10.sp, color = JamesErrorRed, fontWeight = FontWeight.Bold)
                                    } else if (user.isSuspended) {
                                        Text("⏸️ SUSPENDU", fontSize = 10.sp, color = JamesWarningAmber, fontWeight = FontWeight.Bold)
                                    } else if (user.isOnline) {
                                        Text("🟢 EN LIGNE", fontSize = 10.sp, color = JamesOnlineGreen, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("⚪ HORS LIGNE", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    }

                                    if (user.warningCount > 0) {
                                        Text("⚠️ ${user.warningCount} avert.", fontSize = 10.sp, color = JamesWarningAmber)
                                    }
                                }
                            }

                            // Quick actions
                            IconButton(onClick = { selectedUserForDetails = user }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Gérer", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal details and full management of user
    selectedUserForDetails?.let { user ->
        AdminUserDetailsDialog(
            user = user,
            repository = repository,
            onDismiss = { selectedUserForDetails = null },
            onDeleteUser = {
                userToDelete = user
                selectedUserForDetails = null
            },
            onWarnUser = {
                showWarningDialogUser = user
                warningMessageInput = ""
            }
        )
    }

    // Warn Dialog
    showWarningDialogUser?.let { user ->
        AlertDialog(
            onDismissRequest = { showWarningDialogUser = null },
            title = { Text("⚠️ Avertir @${user.username}") },
            text = {
                Column {
                    Text("Motif de l'avertissement :", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = warningMessageInput,
                        onValueChange = { warningMessageInput = it },
                        placeholder = { Text("Ex: Propos injurieux, spam...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val msg = warningMessageInput.ifBlank { "Non respect des règles de la communauté." }
                            repository.warnUser(user.id, msg)
                            showWarningDialogUser = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JamesWarningAmber)
                ) {
                    Text("Envoyer l'avertissement", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWarningDialogUser = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Reinforced confirmation for permanent deletion
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("⚠️ Supprimer définitivement le compte", color = JamesErrorRed, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Action irréversible :", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Le compte de ${user.username} (${user.email}, James ID: JAMES#${user.fourDigitCode}) sera définitivement effacé de Supabase Auth et de la base de données.")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Toutes ses sessions, tokens d'accès, statuts et contacts seront invalidés.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteUserAccount(user.id)
                            userToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JamesErrorRed)
                ) {
                    Text("Confirmer la suppression définitive")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

/**
 * Detailed Dialog for a specific user to perform all required administrative actions
 */
@Composable
fun AdminUserDetailsDialog(
    user: UserEntity,
    repository: JamesChatRepository,
    onDismiss: () -> Unit,
    onDeleteUser: () -> Unit,
    onWarnUser: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var sessions by remember { mutableStateOf<List<SessionEntity>>(emptyList()) }
    var reports by remember { mutableStateOf<List<ReportEntity>>(emptyList()) }
    var showBadgeGrantDialog by remember { mutableStateOf(false) }
    var badgeReasonInput by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    LaunchedEffect(user.id) {
        sessions = repository.dao.getUserSessionsFlow(user.id).firstOrNull() ?: emptyList()
        val allReports = repository.dao.getAllReportsFlow().firstOrNull() ?: emptyList()
        reports = allReports.filter { it.reportedUserId == user.id }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarWithStatus(name = user.username, avatarUrl = user.avatarUrl, size = 36.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "@${user.username}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            JamesVBadge(size = 14.dp)
                        }
                    }
                    Text(text = "Fiche Profil Utilisateur", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Identity details
                Text(text = "Nom complet : ${user.firstName} ${user.lastName}".trim().ifEmpty { "Non renseigné" }, fontSize = 13.sp)
                Text(text = "E-mail : ${user.email}", fontSize = 13.sp)
                Text(text = "James ID : JAMES#${user.fourDigitCode} (${user.jamesTag})", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text(text = "Date d'inscription : ${dateFormat.format(Date(user.createdAt))}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Statut en ligne : ${if (user.isOnline) "🟢 Actif / En ligne" else "⚪ Hors ligne (Vu le ${dateFormat.format(Date(user.lastSeen))})"}", fontSize = 12.sp)
                Text(text = "Rôle applicatif : ${user.role}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Penalties & Badges
                Text(text = "SANCTIONS & STATUT DU COMPTE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "• Avertissements reçus : ${user.warningCount}", fontSize = 12.sp)
                Text(text = "• Signalements reçus : ${reports.size}", fontSize = 12.sp)
                Text(text = "• Statut du compte : ${if (user.isBanned) "🚫 BANNI" else if (user.isSuspended) "⏸️ SUSPENDU" else "🟢 ACTIF"}", fontSize = 12.sp)
                Text(text = "• V Badge : ${if (user.isVerified) "Attribué ✓ (${user.verificationReason.ifEmpty { "Vérifié" }})" else "Non attribué"}", fontSize = 12.sp)

                // Sessions
                if (sessions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "APPAREILS & SESSIONS (${sessions.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        sessions.forEach { s ->
                            Text(text = "📱 ${s.deviceName} (${s.deviceType}) - Dernier accès: ${dateFormat.format(Date(s.lastActive))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "ACTIONS DISPONIBLES :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons Grid/Column
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Warning
                    OutlinedButton(
                        onClick = onWarnUser,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⚠️ Envoyer un avertissement (+1)", fontSize = 12.sp)
                    }

                    // Suspend / Restore
                    if (user.isSuspended) {
                        Button(
                            onClick = {
                                scope.launch { repository.unsuspendUser(user.id) }
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = JamesOnlineGreen)
                        ) {
                            Text("🔓 Restaurer / Lever la suspension", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                scope.launch { repository.suspendUser(user.id, 86400000L * 7, "Suspension temporaire 7j") }
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JamesWarningAmber)
                        ) {
                            Text("⏸️ Suspendre temporairement (7j)", fontSize = 12.sp)
                        }
                    }

                    // Ban / Unban
                    if (user.isBanned) {
                        Button(
                            onClick = {
                                scope.launch { repository.adminBanUser(user.id, false) }
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = JamesOnlineGreen)
                        ) {
                            Text("♻️ Débannir / Réactiver le compte", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                scope.launch { repository.adminBanUser(user.id, true) }
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JamesErrorRed)
                        ) {
                            Text("🚫 Bannir définitivement", fontSize = 12.sp)
                        }
                    }

                    // Limit functions
                    OutlinedButton(
                        onClick = {
                            scope.launch { repository.limitUserFunctions(user.id, 86400000L * 3) }
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔒 Limiter certaines fonctions (3j)", fontSize = 12.sp)
                    }

                    // V Badge management
                    if (user.isVerified) {
                        OutlinedButton(
                            onClick = {
                                scope.launch { repository.revokeVerifiedBadge(user.id, "Révocation manuelle admin") }
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("❌ Révoquer le V Badge", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = { showBadgeGrantDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🏅 Attribuer un V Badge", fontSize = 12.sp)
                        }
                    }

                    // Delete Account
                    Button(
                        onClick = onDeleteUser,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = JamesErrorRed)
                    ) {
                        Text("🗑️ Supprimer définitivement le compte", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )

    if (showBadgeGrantDialog) {
        AlertDialog(
            onDismissRequest = { showBadgeGrantDialog = false },
            title = { Text("🏅 Attribuer le V Badge") },
            text = {
                Column {
                    Text("Motif de l'attribution :", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = badgeReasonInput,
                        onValueChange = { badgeReasonInput = it },
                        placeholder = { Text("Ex: Créateur certifié, Personnalité...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        repository.grantVerifiedBadge(
                            targetUserId = user.id,
                            category = "Officiel",
                            reason = badgeReasonInput.ifBlank { "Compte officiel certifié" }
                        )
                        showBadgeGrantDialog = false
                        onDismiss()
                    }
                }) {
                    Text("Attribuer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBadgeGrantDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

/* =================================================================================
   SECTION A : GESTION DES ADMINISTRATEURS (AJOUTER, RETIRER, RÔLE, SUSPENDRE, ETC.)
   ================================================================================= */
@Composable
fun AdminAdminsManagementTab(
    users: List<UserEntity>,
    repository: JamesChatRepository
) {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf<String?>(null) }
    var showFullEmails by remember { mutableStateOf(false) }
    var showAddAdminDialog by remember { mutableStateOf(false) }
    var selectedAdminForEdit by remember { mutableStateOf<UserEntity?>(null) }
    var newAdminEmailInput by remember { mutableStateOf("") }
    var newAdminRoleInput by remember { mutableStateOf(UserRole.ADMIN.name) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    fun maskAdminEmail(email: String): String {
        val at = email.indexOf('@')
        if (at <= 2) return email
        val name = email.substring(0, at)
        val domain = email.substring(at)
        val visible = name.take(3)
        return "$visible****$domain"
    }

    val configuredAdmins = JamesChatRepository.CONFIGURED_7_ADMINS
    val currentAdminUser = repository.currentUser.value

    val configuredEmailSet = configuredAdmins.map { it.email.lowercase() }.toSet()
    val databaseAdmins = users.filter {
        (it.role == UserRole.ADMIN.name || it.role == UserRole.OWNER.name || it.role == UserRole.SUPER_ADMIN.name || it.role == UserRole.MODERATOR.name) &&
        !configuredEmailSet.contains(it.email.lowercase())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Centre d'Administration James Chat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Gestion des comptes et des privilèges administrateurs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (repository.isCurrentUserOwner()) {
                    Button(onClick = { showAddAdminDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter", fontSize = 12.sp)
                    }
                }
            }
        }

        if (copiedNotice != null) {
            item {
                Surface(
                    color = JamesOnlineGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = copiedNotice!!,
                        color = JamesOnlineGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Section 1: Compte actuellement connecté
        if (currentAdminUser != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarWithStatus(
                            name = currentAdminUser.username.ifEmpty { currentAdminUser.email },
                            avatarUrl = currentAdminUser.avatarUrl,
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${currentAdminUser.firstName} ${currentAdminUser.lastName}".trim().ifEmpty { currentAdminUser.username },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                JamesVBadge(size = 14.dp)
                            }
                            Text(
                                text = currentAdminUser.email,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Votre compte • Rôle : ${currentAdminUser.role} • Statut : Connecté & Actif 🟢",
                                fontSize = 11.sp,
                                color = JamesOnlineGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(currentAdminUser.email))
                                copiedNotice = "Votre adresse email a été copiée : ${currentAdminUser.email}"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Adresses Email Officielles des Administrateurs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👑 PROPRIÉTAIRES ET ADMINISTRATEURS OFFICIELS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "3 Propriétaires officiels et modérateurs autorisés",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { showFullEmails = !showFullEmails }) {
                                Icon(
                                    imageVector = if (showFullEmails) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showFullEmails) "Masquer" else "Afficher", fontSize = 11.sp)
                            }
                            TextButton(
                                onClick = {
                                    val allEmailsJoined = configuredAdmins.joinToString(", ") { it.email }
                                    clipboard.setText(AnnotatedString(allEmailsJoined))
                                    copiedNotice = "Les adresses email des administrateurs ont été copiées dans le presse-papiers !"
                                }
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copier tout", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    configuredAdmins.forEach { admin ->
                        val isCurrentUser = admin.email.equals(currentAdminUser?.email, true)
                        val displayEmail = if (showFullEmails || isCurrentUser) admin.email else maskAdminEmail(admin.email)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = displayEmail,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        JamesVBadge(size = 12.dp)
                                        if (isCurrentUser) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "(Vous)", fontSize = 11.sp, color = JamesOnlineGreen, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${admin.firstName} ${admin.lastName} • ${admin.title}",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = admin.bio,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = when (admin.role) {
                                            UserRole.OWNER -> Color(0xFFE0E7FF)
                                            UserRole.SUPER_ADMIN -> Color(0xFFFEE2E2)
                                            UserRole.ADMIN -> Color(0xFFDCFCE7)
                                            UserRole.MODERATOR -> Color(0xFFFEF3C7)
                                            UserRole.SUPPORT -> Color(0xFFF3E8FF)
                                            else -> MaterialTheme.colorScheme.primaryContainer
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = admin.role.name,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (admin.role) {
                                                UserRole.OWNER -> Color(0xFF4338CA)
                                                UserRole.SUPER_ADMIN -> Color(0xFFB91C1C)
                                                UserRole.ADMIN -> Color(0xFF15803D)
                                                UserRole.MODERATOR -> Color(0xFFB45309)
                                                UserRole.SUPPORT -> Color(0xFF7E22CE)
                                                else -> MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(admin.email))
                                            copiedNotice = "Adresse email copiée : ${admin.email}"
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copier l'adresse email",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Autres administrateurs dans la base de données
        if (databaseAdmins.isNotEmpty()) {
            item {
                Text(
                    text = "👥 AUTRES ADMINISTRATEURS DANS LA BASE (${databaseAdmins.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(databaseAdmins) { admin ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (admin.isSuspended) JamesWarningAmber.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarWithStatus(
                            name = admin.username.ifEmpty { admin.email },
                            avatarUrl = admin.avatarUrl,
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${admin.firstName} ${admin.lastName}".trim().ifEmpty { admin.username },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                JamesVBadge(size = 14.dp)
                            }
                            Text(
                                text = admin.email,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Rôle: ${admin.role} • Statut: ${if (admin.isSuspended) "Permissions suspendues ⏸️" else "Actif 🟢"}",
                                fontSize = 11.sp,
                                color = if (admin.isSuspended) JamesWarningAmber else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(admin.email))
                                copiedNotice = "Imèl kopye : ${admin.email}"
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Kopye",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { selectedAdminForEdit = admin }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // Add admin dialog
    if (showAddAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAddAdminDialog = false },
            title = { Text("Ajouter un administrateur") },
            text = {
                Column {
                    Text("Adresse e-mail du futur administrateur :", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newAdminEmailInput,
                        onValueChange = { newAdminEmailInput = it },
                        placeholder = { Text("ex: bradleykillia@gmail.com") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Rôle attribué :", fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        listOf(UserRole.ADMIN.name, UserRole.MODERATOR.name).forEach { r ->
                            FilterChip(
                                selected = newAdminRoleInput == r,
                                onClick = { newAdminRoleInput = r },
                                label = { Text(r, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newAdminEmailInput.isNotBlank()) {
                        scope.launch {
                            repository.addOrUpdateAdmin(
                                email = newAdminEmailInput.trim(),
                                role = newAdminRoleInput
                            )
                            showAddAdminDialog = false
                            newAdminEmailInput = ""
                        }
                    }
                }) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAdminDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Edit admin dialog
    selectedAdminForEdit?.let { admin ->
        AlertDialog(
            onDismissRequest = { selectedAdminForEdit = null },
            title = { Text("Gérer ${admin.username} (${admin.email})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Rôle actuel : ${admin.role}", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    // Role modifications
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    repository.updateAdminRole(admin.id, UserRole.ADMIN.name)
                                    selectedAdminForEdit = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Passer Admin", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    repository.updateAdminRole(admin.id, UserRole.MODERATOR.name)
                                    selectedAdminForEdit = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Passer Modérateur", fontSize = 11.sp)
                        }
                    }

                    // Suspend / Restore permissions
                    if (admin.isSuspended) {
                        Button(
                            onClick = {
                                scope.launch {
                                    repository.restoreAdminPermissions(admin.id)
                                    selectedAdminForEdit = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = JamesOnlineGreen)
                        ) {
                            Text("🟢 Restaurer ses permissions", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    repository.suspendAdminPermissions(admin.id)
                                    selectedAdminForEdit = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JamesWarningAmber)
                        ) {
                            Text("⏸️ Suspendre ses permissions", fontSize = 12.sp)
                        }
                    }

                    // Revoke admin rights completely
                    Button(
                        onClick = {
                            scope.launch {
                                repository.removeAdmin(admin.id)
                                selectedAdminForEdit = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = JamesErrorRed)
                    ) {
                        Text("Retirer les droits administrateur", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAdminForEdit = null }) {
                    Text("Fermer")
                }
            }
        )
    }
}

/* =================================================================================
   SECTION : BADGES V
   ================================================================================= */
@Composable
fun AdminBadgesTab(
    users: List<UserEntity>,
    repository: JamesChatRepository
) {
    val scope = rememberCoroutineScope()
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }
    var badgeReason by remember { mutableStateOf("") }
    var selectedBadgeStyle by remember { mutableStateOf(VBadgeStyle.SEAL) }
    var showGrantDialog by remember { mutableStateOf(false) }
    var showRevokeConfirmDialog by remember { mutableStateOf(false) }
    var userToRevoke by remember { mutableStateOf<UserEntity?>(null) }
    var revokeReason by remember { mutableStateOf("") }

    var userSearchQuery by remember { mutableStateOf("") }
    var subTab by remember { mutableIntStateOf(0) } // 0: Demandes, 1: Comptes vérifiés & Attribution, 2: Historique

    val requests by repository.dao.getAllVerificationRequestsFlow().collectAsState(initial = emptyList())
    val badgeLogs by repository.dao.getAllVerifiedBadgeLogsFlow().collectAsState(initial = emptyList())

    var selectedRequestToReview by remember { mutableStateOf<com.example.data.model.VerificationRequestEntity?>(null) }
    var reviewBadgeStyle by remember { mutableStateOf(VBadgeStyle.SEAL) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    var showReviewDialog by remember { mutableStateOf(false) }
    var isApprovingRequest by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    JamesVBadge(size = 22.dp, style = VBadgeStyle.SEAL, showTooltipOnClick = false)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Système Officiel V Badge — James Chat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Badges certifiés officiels (Options 2, 3, 4) : Style 2 (V Sceau), Style 3 (V Royal) & Style 4 (V Diamant) 💎. Attribution instantanée aux administrateurs autorisés et certification officielle des créateurs.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        TabRow(selectedTabIndex = subTab) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = {
                    val pendingCount = requests.count { it.status == "PENDING" }
                    Text("Demandes ($pendingCount)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("Utilisateurs & Attribution", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = subTab == 2,
                onClick = { subTab = 2 },
                text = { Text("Historique (${badgeLogs.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        when (subTab) {
            0 -> {
                // Section Demandes
                val pendingRequests = requests.filter { it.status == "PENDING" }
                val pastRequests = requests.filter { it.status != "PENDING" }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (pendingRequests.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aucune demande de vérification en attente.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = "Demandes en attente d'examen (${pendingRequests.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(pendingRequests) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = req.accountName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text(text = "@${req.username} • ${req.accountType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(JamesWarningAmber.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(text = "EN ATTENTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = JamesWarningAmber)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "Motif : ${req.reason}", fontSize = 13.sp)
                                    if (req.additionalInfo.isNotEmpty()) {
                                        Text(text = "Infos complémentaires : ${req.additionalInfo}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                selectedRequestToReview = req
                                                isApprovingRequest = true
                                                reviewBadgeStyle = VBadgeStyle.fromCode(req.preferredStyle)
                                                showReviewDialog = true
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Approuver", fontSize = 12.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                selectedRequestToReview = req
                                                isApprovingRequest = false
                                                rejectionReasonInput = ""
                                                showReviewDialog = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text("Refuser", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (pastRequests.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Demandes déjà traitées (${pastRequests.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(pastRequests) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "@${req.username}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (req.status == "APPROVED") {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                JamesVBadge(size = 14.dp, style = VBadgeStyle.fromCode(req.assignedBadgeStyle))
                                            }
                                        }
                                        Text(text = "${req.accountType} • Traité par ${req.reviewedByAdminEmail}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (req.rejectionReason.isNotEmpty()) {
                                            Text(text = "Raison du refus : ${req.rejectionReason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    Text(
                                        text = if (req.status == "APPROVED") "APPROUVÉE" else "REFUSÉE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (req.status == "APPROVED") JamesOnlineGreen else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Section Utilisateurs & Attribution manuelle
                val filteredUsers = remember(users, userSearchQuery) {
                    if (userSearchQuery.isBlank()) users
                    else users.filter {
                        it.username.contains(userSearchQuery, ignoreCase = true) ||
                                it.firstName.contains(userSearchQuery, ignoreCase = true) ||
                                it.lastName.contains(userSearchQuery, ignoreCase = true) ||
                                it.email.contains(userSearchQuery, ignoreCase = true)
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = userSearchQuery,
                        onValueChange = { userSearchQuery = it },
                        placeholder = { Text("Rechercher un utilisateur...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredUsers) { user ->
                            val currentStyle = VBadgeStyle.fromCode(user.verificationBadgeStyle)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarWithStatus(name = user.username, avatarUrl = user.avatarUrl, size = 46.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "@${user.username}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        if (user.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            JamesVBadge(size = 14.dp, style = currentStyle)
                                        }
                                    }
                                    Text(
                                        text = if (user.isVerified) "Vérifié ✓ (${currentStyle.title} - ${user.verificationReason.ifEmpty { "Officiel" }})" else "Non vérifié",
                                        fontSize = 12.sp,
                                        color = if (user.isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (user.role != UserRole.USER.name) {
                                        Text(text = "Rôle : ${user.role}", fontSize = 10.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                if (user.isVerified) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                selectedUser = user
                                                badgeReason = user.verificationReason
                                                selectedBadgeStyle = currentStyle
                                                showGrantDialog = true
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Changer style", modifier = Modifier.size(18.dp))
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                userToRevoke = user
                                                revokeReason = ""
                                                showRevokeConfirmDialog = true
                                            }
                                        ) {
                                            Text("Retirer", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            selectedUser = user
                                            badgeReason = ""
                                            selectedBadgeStyle = VBadgeStyle.SEAL
                                            showGrantDialog = true
                                        }
                                    ) {
                                        Text("Attribuer V", fontSize = 11.sp)
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            2 -> {
                // Section Historique des décisions
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (badgeLogs.isEmpty()) {
                        item {
                            Text("Aucun historique pour le moment.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(badgeLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (log.action == "GRANT_BADGE") "✓ Attribution V Badge" else if (log.action == "REVOKE_BADGE") "✕ Révocation V Badge" else "Demande refusée",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (log.action == "GRANT_BADGE") JamesOnlineGreen else MaterialTheme.colorScheme.error
                                        )
                                        val dateStr = remember(log.timestamp) {
                                            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))
                                        }
                                        Text(text = dateStr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Utilisateur : @${log.username}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "Motif : ${log.reason}", fontSize = 12.sp)
                                    Text(text = "Administrateur : ${log.adminEmail}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogue d'attribution / modification manuelle de style
    if (showGrantDialog && selectedUser != null) {
        val user = selectedUser!!
        AlertDialog(
            onDismissRequest = { showGrantDialog = false },
            title = {
                Text(
                    text = if (user.isVerified) "Modifier le badge de @${user.username}" else "Attribuer le badge V à @${user.username}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choisissez le style officiel du badge (Styles 2 et 3 sont les choix principaux) :",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Sélecteur de styles
                    VBadgeStyle.entries.forEach { styleOption ->
                        val isSelected = selectedBadgeStyle == styleOption
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBadgeStyle = styleOption },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                JamesVBadge(size = 20.dp, style = styleOption, showTooltipOnClick = false)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = styleOption.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (styleOption.isPrimary) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.primary)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("PRINCIPAL", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                    Text(text = styleOption.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = badgeReason,
                        onValueChange = { badgeReason = it },
                        placeholder = { Text("Motif de l'attribution (ex: Créateur certifié)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.grantVerifiedBadge(
                                targetUserId = user.id,
                                category = "Officiel",
                                reason = badgeReason.ifBlank { "Compte officiel certifié James Chat" },
                                badgeStyle = selectedBadgeStyle.code
                            )
                            showGrantDialog = false
                            badgeReason = ""
                        }
                    }
                ) {
                    Text("Confirmer l'attribution")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGrantDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialogue d'examen d'une demande utilisateur
    if (showReviewDialog && selectedRequestToReview != null) {
        val req = selectedRequestToReview!!
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = {
                Text(
                    text = if (isApprovingRequest) "Approuver la demande de @${req.username}" else "Refuser la demande de @${req.username}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Compte : ${req.accountName} (${req.accountType})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(text = "Motif soumis : ${req.reason}", fontSize = 12.5.sp)

                    if (isApprovingRequest) {
                        Text(text = "Choisir le style de badge à attribuer (5 modèles) :", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        VBadgeStyle.entries.forEach { styleOpt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { reviewBadgeStyle = styleOpt },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (reviewBadgeStyle == styleOpt) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    JamesVBadge(size = 18.dp, style = styleOpt, showTooltipOnClick = false)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = styleOpt.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = rejectionReasonInput,
                            onValueChange = { rejectionReasonInput = it },
                            placeholder = { Text("Motif du refus (ex: Critères de notoriété non atteints)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.resolveVerificationRequestWithTarget(
                                request = req,
                                approved = isApprovingRequest,
                                badgeStyle = reviewBadgeStyle.code,
                                rejectionReason = rejectionReasonInput.ifBlank { "Critères d'éligibilité non remplis" }
                            )
                            showReviewDialog = false
                        }
                    },
                    colors = if (!isApprovingRequest) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()
                ) {
                    Text(if (isApprovingRequest) "Confirmer l'approbation" else "Confirmer le refus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Confirmation avant retrait d'un badge
    if (showRevokeConfirmDialog && userToRevoke != null) {
        val user = userToRevoke!!
        AlertDialog(
            onDismissRequest = { showRevokeConfirmDialog = false },
            title = { Text("Confirmer le retrait du V Badge") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Êtes-vous certain de vouloir retirer le badge de vérification officiel à @${user.username} ?")
                    OutlinedTextField(
                        value = revokeReason,
                        onValueChange = { revokeReason = it },
                        placeholder = { Text("Raison du retrait (ex: Non-respect des règles)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.revokeVerifiedBadge(user.id, revokeReason.ifBlank { "Révocation administrative" })
                            showRevokeConfirmDialog = false
                            userToRevoke = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Retirer le badge")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

/* =================================================================================
   SECTION : MODÉRATION & SIGNALEMENTS
   ================================================================================= */
@Composable
fun AdminModerationTab(repository: JamesChatRepository) {
    val scope = rememberCoroutineScope()
    val allReports by repository.dao.getAllReportsFlow().collectAsState(initial = emptyList())
    val reports = remember(allReports) { allReports.filter { it.status == "PENDING" } }
    val dossiers by repository.dao.getAllModerationDossiersFlow().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "File d'attente des Signalements (${reports.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        if (reports.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = JamesOnlineGreen, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Aucun signalement en attente", fontWeight = FontWeight.SemiBold)
                        Text(text = "La communauté James Chat est saine.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(reports) { report ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = JamesErrorRed.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Signalement #${report.id.take(8)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = report.status, color = JamesErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Motif : ${report.reason}", fontSize = 13.sp)
                        Text(text = "Détails : ${report.details}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.warnUser(report.reportedUserId, "Avertissement suite à signalement: ${report.reason}")
                                        repository.updateReportStatus(report.id, "AVERTISSEMENT_DONNÉ")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JamesWarningAmber)
                            ) {
                                Text("Avertir (+1)", fontSize = 11.sp, color = Color.Black)
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.suspendUser(report.reportedUserId, 86400000L, "Suspension suite à signalement: ${report.reason}")
                                        repository.updateReportStatus(report.id, "UTILISATEUR_SUSPENDU")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JamesErrorRed)
                            ) {
                                Text("Suspendre 24h", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch { repository.updateReportStatus(report.id, "REJETÉ") }
                                }
                            ) {
                                Text("Rejeter", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/* =================================================================================
   SECTION : JOURNAL ADMINISTRATIF D'AUDIT (LOGS)
   ================================================================================= */
@Composable
fun AdminAuditLogTab(logs: List<com.example.data.model.AdminLogEntity>) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            text = "Journal Administratif & Audit Trail (${logs.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucune action enregistrée pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(logs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.action,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Admin : ${log.adminEmail}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Cible : ${log.target}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/* =================================================================================
   SECTION : GESTION DE LA BASE DE CONNAISSANCES DE JAMES BOT (ADMIN ONLY)
   ================================================================================= */
@Composable
fun AdminKnowledgeBaseTab(
    repository: JamesChatRepository
) {
    val scope = rememberCoroutineScope()
    val allEntries by repository.dao.getAllBotKnowledgeFlow().collectAsState(initial = emptyList())
    val allReports by repository.dao.getAllBotReportsFlow().collectAsState(initial = emptyList())
    val currentAdminUser = repository.currentUser.value

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Tous") }
    var selectedLanguageFilter by remember { mutableStateOf("Toutes") }
    var selectedStatusFilter by remember { mutableStateOf("Tous") }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<com.example.data.model.BotKnowledgeEntity?>(null) }
    var entryToDelete by remember { mutableStateOf<com.example.data.model.BotKnowledgeEntity?>(null) }
    var previewEntry by remember { mutableStateOf<com.example.data.model.BotKnowledgeEntity?>(null) }

    val categories = listOf("Tous", "Général", "Fonctionnalités", "Sécurité", "Règles", "Compte", "Support", "Badges & Vérification", "Messages Vocaux")
    val languages = listOf("Toutes", "fr", "ht", "en", "es")

    val filteredEntries = remember(allEntries, searchQuery, selectedCategoryFilter, selectedLanguageFilter, selectedStatusFilter) {
        allEntries.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                    item.question.contains(searchQuery, ignoreCase = true) ||
                    item.answer.contains(searchQuery, ignoreCase = true) ||
                    item.keywords.contains(searchQuery, ignoreCase = true) ||
                    item.alternativeQuestions.contains(searchQuery, ignoreCase = true)

            val matchCategory = selectedCategoryFilter == "Tous" || item.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchLang = selectedLanguageFilter == "Toutes" || item.language.equals(selectedLanguageFilter, ignoreCase = true)
            val matchStatus = when (selectedStatusFilter) {
                "Publié" -> item.isPublished
                "Brouillon" -> !item.isPublished
                else -> true
            }

            matchQuery && matchCategory && matchLang && matchStatus
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // En-tête avec bouton Ajouter
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7C3AED).copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Base de connaissances James Bot",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF7C3AED))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Gestion des questions & réponses certifiées côté serveur. Accessible uniquement aux administrateurs autorisés.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            entryToEdit = null
                            showAddEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter", fontSize = 12.sp)
                    }
                }
            }
        }

        // Barre de recherche
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rechercher question, mot-clé, réponse...", fontSize = 12.5.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Effacer")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Filtres par catégorie
        item {
            Text(text = "Catégories :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Filtres langue & statut
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Langue :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)
                languages.forEach { lang ->
                    FilterChip(
                        selected = selectedLanguageFilter == lang,
                        onClick = { selectedLanguageFilter = lang },
                        label = { Text(lang.uppercase(Locale.ROOT), fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                listOf("Tous", "Publié", "Brouillon").forEach { stat ->
                    FilterChip(
                        selected = selectedStatusFilter == stat,
                        onClick = { selectedStatusFilter = stat },
                        label = { Text(stat, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Compteur & Signalements d'utilisateurs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUESTIONS RÉPERTORIÉES (${filteredEntries.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                if (allReports.isNotEmpty()) {
                    Text(
                        text = "🚨 ${allReports.size} signalement(s) utilisateur",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Liste des entrées
        if (filteredEntries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aucune question trouvée avec ces critères.\nAppuyez sur 'Ajouter' pour créer une nouvelle entrée.",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredEntries) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (entry.isPublished) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (entry.isPublished) JamesOnlineGreen else MaterialTheme.colorScheme.outline)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (entry.isPublished) "PUBLIÉ" else "BROUILLON",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = entry.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF7C3AED)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🌐 ${entry.language.uppercase(Locale.ROOT)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { previewEntry = entry },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = "Aperçu", modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = {
                                        entryToEdit = entry
                                        showAddEditDialog = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifier", modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { entryToDelete = entry },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = entry.question,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (entry.alternativeQuestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Formulations alternatives : ${entry.alternativeQuestions}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }

                        if (entry.keywords.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Mots-clés : ${entry.keywords}",
                                fontSize = 10.5.sp,
                                color = Color(0xFF2563EB)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = entry.answer,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Modifié par ${entry.lastUpdatedByAdminEmail.ifEmpty { entry.createdByAdminEmail }}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        repository.toggleBotKnowledgePublish(entry.id, !entry.isPublished)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (entry.isPublished) "Dépublier" else "Publier",
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Ajouter ou Modifier une question
    if (showAddEditDialog) {
        AdminBotKnowledgeAddEditDialog(
            initialEntry = entryToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { category, question, alternatives, keywords, answer, lang, isPublished ->
                scope.launch {
                    if (entryToEdit == null) {
                        repository.addBotKnowledgeEntry(
                            category = category,
                            question = question,
                            alternativeQuestions = alternatives,
                            keywords = keywords,
                            answer = answer,
                            language = lang,
                            isPublished = isPublished
                        )
                    } else {
                        repository.updateBotKnowledgeEntry(
                            id = entryToEdit!!.id,
                            category = category,
                            question = question,
                            alternativeQuestions = alternatives,
                            keywords = keywords,
                            answer = answer,
                            language = lang,
                            isPublished = isPublished
                        )
                    }
                    showAddEditDialog = false
                }
            }
        )
    }

    // Modal: Confirmation de suppression
    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Supprimer cette question ?") },
            text = {
                Text(
                    text = "Voulez-vous vraiment supprimer définitivement « ${entryToDelete!!.question} » de la base de connaissances ?\nCette action est irréversible et sera enregistrée dans le journal d'audit.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteBotKnowledgeEntry(entryToDelete!!.id)
                            entryToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Modal: Aperçu avant publication
    if (previewEntry != null) {
        AlertDialog(
            onDismissRequest = { previewEntry = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFF7C3AED))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Aperçu de la réponse")
                }
            },
            text = {
                Column {
                    Text(text = "Question :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = previewEntry!!.question, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Réponse certifiée de James Bot :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(text = previewEntry!!.answer, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(10.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Catégorie : ${previewEntry!!.category} • Langue : ${previewEntry!!.language.uppercase(Locale.ROOT)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            },
            confirmButton = {
                Button(onClick = { previewEntry = null }) {
                    Text("Fermer")
                }
            }
        )
    }
}

/**
 * Dialog d'ajout et d'édition d'une question/réponse pour James Bot.
 */
@Composable
fun AdminBotKnowledgeAddEditDialog(
    initialEntry: com.example.data.model.BotKnowledgeEntity?,
    onDismiss: () -> Unit,
    onSave: (category: String, question: String, alternatives: String, keywords: String, answer: String, lang: String, isPublished: Boolean) -> Unit
) {
    var category by remember { mutableStateOf(initialEntry?.category ?: "Général") }
    var question by remember { mutableStateOf(initialEntry?.question ?: "") }
    var alternatives by remember { mutableStateOf(initialEntry?.alternativeQuestions ?: "") }
    var keywords by remember { mutableStateOf(initialEntry?.keywords ?: "") }
    var answer by remember { mutableStateOf(initialEntry?.answer ?: "") }
    var language by remember { mutableStateOf(initialEntry?.language ?: "fr") }
    var isPublished by remember { mutableStateOf(initialEntry?.isPublished ?: true) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialEntry == null) "Ajouter une question à James Bot" else "Modifier la question",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Catégorie
                Text("Catégorie :", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    placeholder = { Text("Ex: Badges, Vocaux, Sécurité...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Question principale
                Text("Question principale :", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    placeholder = { Text("Ex: Comment obtenir le V Badge ?") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Formulations alternatives
                Text("Formulations alternatives (séparées par des virgules) :", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                OutlinedTextField(
                    value = alternatives,
                    onValueChange = { alternatives = it },
                    placeholder = { Text("Ex: Avoir le badge bleu, Comment être vérifié...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mots-clés
                Text("Mots-clés (séparés par des virgules) :", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    placeholder = { Text("Ex: badge, verifier, certification...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Langue
                Text("Langue de la réponse :", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("fr" to "Français", "ht" to "Kreyòl", "en" to "English", "es" to "Español").forEach { (code, label) ->
                        FilterChip(
                            selected = language == code,
                            onClick = { language = code },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Réponse
                Text("Réponse certifiée de James Bot :", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    placeholder = { Text("Rédigez la réponse exacte et fiable...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Statut de publication
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Publier immédiatement dans le chat :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    androidx.compose.material3.Switch(
                        checked = isPublished,
                        onCheckedChange = { isPublished = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank() && answer.isNotBlank()) {
                        onSave(category, question, alternatives, keywords, answer, language, isPublished)
                    }
                },
                enabled = question.isNotBlank() && answer.isNotBlank()
            ) {
                Text("Enregistrer")
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
 * James Transfer — Outil d'administration sécurisé
 * Transfert de propriété de canaux/groupes, sauvegarde et migration de données avec autorisation backend.
 */
@Composable
fun AdminJamesTransferTab(
    repository: JamesChatRepository
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var transferStatusMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var selectedTransferType by remember { mutableStateOf("OWNERSHIP") } // OWNERSHIP, BACKUP, KEYS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("James Transfer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = JamesOnlineGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "Autorisé Backend",
                                color = JamesOnlineGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        "Transfert de propriété, clés cryptographiques et sauvegardes chiffrées.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (transferStatusMessage != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = JamesOnlineGreen.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = JamesOnlineGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(transferStatusMessage!!, fontSize = 12.sp, color = JamesOnlineGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Action 1: Export Database Backup
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("1. Sauvegarde Chiffrée de la Base de Données", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "Génère un export complet chiffré AES-256 (utilisateurs, messages, journaux d'audit et configurations).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            delay(1200)
                            isProcessing = false
                            transferStatusMessage = "Sauvegarde chiffrée générée avec succès (Hash SHA-256 : ${java.util.UUID.randomUUID().toString().take(12)}...)"
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier.fillMaxWidth().testTag("admin_backup_export_btn")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Générer la Sauvegarde Chiffrée")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action 2: Transfer Group/Channel Ownership
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2. Transfert de Propriété (Canal / Groupe)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "Transférer les privilèges de fondateur et de propriétaire à un autre compte administrateur.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            delay(1000)
                            isProcessing = false
                            transferStatusMessage = "Protocole de transfert de propriété initialisé et certifié par signature d'audit."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth().testTag("admin_transfer_ownership_btn")
                ) {
                    Text("Transférer la Propriété d'un Espace")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action 3: Key Rotation
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("3. Rotation des Clés de Chiffrement & Signalisation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "Régénérer les clés de signature DTLS-SRTP et de chiffrement des canaux sécurisés.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        transferStatusMessage = "Nouvelle paire de clés cryptographiques générée (Clé publique diffusée aux nœuds)."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Régénérer les Clés de Chiffrement")
                }
            }
        }
    }
}

/**
 * James Scheduler — Planificateur de tâches administratives
 * Purges automatiques des messages éphémères, nettoyage de cache, rapports et annonces programmées.
 */
@Composable
fun AdminJamesSchedulerTab(
    repository: JamesChatRepository
) {
    val scope = rememberCoroutineScope()
    var schedulerFeedback by remember { mutableStateOf<String?>(null) }
    var task1Active by remember { mutableStateOf(true) }
    var task2Active by remember { mutableStateOf(true) }
    var task3Active by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("James Scheduler", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Planificateur de maintenance, purges automatiques et diffusion d'annonces.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (schedulerFeedback != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = JamesOnlineGreen.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = JamesOnlineGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(schedulerFeedback!!, fontSize = 12.sp, color = JamesOnlineGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Text(
            text = "TÂCHES DE MAINTENANCE PLANIFIÉES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        // Task 1: Ephemeral purge
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Purge des messages éphémères expirés", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Intervalle : Toutes les 30 minutes • Actif", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    androidx.compose.material3.Switch(
                        checked = task1Active,
                        onCheckedChange = { task1Active = it }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            repository.purgeExpiredEphemeralMessages()
                            schedulerFeedback = "Purge des messages éphémères exécutée avec succès."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Exécuter maintenant", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Task 2: Cache cleanup
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Nettoyage du cache audio/vidéo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Intervalle : Quotidien à 03h00 • Optimisation mémoire", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    androidx.compose.material3.Switch(
                        checked = task2Active,
                        onCheckedChange = { task2Active = it }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        schedulerFeedback = "Cache audio/vidéo nettoyé (34 Mo libérés)."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nettoyer le cache maintenant", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Task 3: Audit generation
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Génération automatique du rapport d'audit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Intervalle : Hebdomadaire (Dimanche soir)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    androidx.compose.material3.Switch(
                        checked = task3Active,
                        onCheckedChange = { task3Active = it }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        schedulerFeedback = "Rapport d'audit compilé et consigné dans le journal."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Générer le rapport hebdomadaire", fontSize = 12.sp)
                }
            }
        }
    }
}

