package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.GroupEntity
import com.example.data.model.UserEntity
import com.example.data.repository.JamesChatRepository
import com.example.ui.components.AvatarWithStatus
import com.example.ui.components.JamesVBadge
import com.example.ui.theme.JamesBluePrimary
import com.example.ui.theme.JamesErrorRed
import com.example.ui.theme.JamesOnlineGreen
import com.example.ui.theme.JamesWarningAmber
import com.example.util.JamesI18n
import com.example.util.JamesSoundPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val GROUP_CATEGORIES = listOf(
    "Tous",
    "Groupes scolaires",
    "Groupes de classe",
    "Groupes d'étudiants",
    "Groupes de professeurs",
    "Groupes d'églises",
    "Groupes de prière",
    "Groupes de familles",
    "Groupes d'amis",
    "Groupes de travail",
    "Groupes d'entreprises",
    "Groupes de jeux",
    "Groupes de créateurs",
    "Groupes sportifs",
    "Groupes communautaires",
    "Autres groupes"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsListScreen(
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onSelectGroup: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0: Mes Groupes, 1: Découvrir les groupes
    var selectedCategoryFilter by remember { mutableStateOf("Tous") }
    var searchQuery by remember { mutableStateOf("") }

    val allGroups by repository.dao.getAllGroupsFlow().collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedGroupForDetails by remember { mutableStateOf<GroupEntity?>(null) }

    // Seed default categories if empty
    LaunchedEffect(Unit) {
        if (allGroups.isEmpty() && currentUser != null) {
            val u = currentUser!!
            repository.createGroup(
                name = "Collège Exemple - 9e Année",
                description = "Groupe scolaire officiel de la classe de 9e année.",
                rules = "1. Respect mutuel\n2. Partage de devoirs autorisé\n3. Pas de spam",
                category = "Groupes scolaires",
                institution = "Collège Exemple",
                gradeLevel = "9e année",
                isPublic = true
            )
            repository.createGroup(
                name = "Jeunes de l'Église de la Grâce",
                description = "Activités, louange et annonces pour la jeunesse chrétienne.",
                rules = "Bienveillance, prière et partage fraternel.",
                category = "Groupes d'églises",
                institution = "Église de la Grâce",
                isPublic = true
            )
            repository.createGroup(
                name = "James Chat Gaming Hub",
                description = "Discussions et sessions multi-joueurs pour les membres.",
                rules = "Fair-play obligatoire.",
                category = "Groupes de jeux",
                isPublic = true
            )
        }
    }

    // Filtered lists
    val myGroups = remember(allGroups, currentUser, searchQuery) {
        val uid = currentUser?.id ?: ""
        allGroups.filter { g ->
            val isMember = g.creatorId == uid || g.memberIds.split(",").contains(uid)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                g.name.contains(searchQuery, ignoreCase = true) ||
                g.description.contains(searchQuery, ignoreCase = true) ||
                g.category.contains(searchQuery, ignoreCase = true)
            }
            isMember && matchesSearch
        }
    }

    val discoverableGroups = remember(allGroups, currentUser, selectedCategoryFilter, searchQuery) {
        val uid = currentUser?.id ?: ""
        allGroups.filter { g ->
            val matchesCategory = if (selectedCategoryFilter == "Tous") true else g.category == selectedCategoryFilter
            val matchesSearch = if (searchQuery.isBlank()) true else {
                g.name.contains(searchQuery, ignoreCase = true) ||
                g.description.contains(searchQuery, ignoreCase = true) ||
                g.institution.contains(searchQuery, ignoreCase = true) ||
                g.category.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_group_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Créer un groupe")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main Tabs (Mes Groupes | Découvrir les groupes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMainTab == 0,
                    onClick = { selectedMainTab = 0 },
                    label = { Text("Mes Groupes (${myGroups.size})", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedMainTab == 1,
                    onClick = { selectedMainTab = 1 },
                    label = { Text("Découvrir (${discoverableGroups.size})", fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.weight(1f)
                )
            }

            // Universal Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(if (selectedMainTab == 0) "Rechercher parmi mes groupes..." else "Rechercher par nom, école, église, catégorie...", fontSize = 13.sp)
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Effacer")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Chips Row (especially for Discover tab)
            ScrollableTabRow(
                selectedTabIndex = GROUP_CATEGORIES.indexOf(selectedCategoryFilter).coerceAtLeast(0),
                edgePadding = 12.dp,
                divider = {}
            ) {
                GROUP_CATEGORIES.forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 11.sp) },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            }

            // Content List
            if (selectedMainTab == 0) {
                if (myGroups.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Vous n'avez rejoint aucun groupe pour le moment.\nCréez votre première communauté ou explorez l'onglet Découvrir !",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(myGroups) { group ->
                            GroupItemRow(
                                group = group,
                                onClick = { onSelectGroup(group.id) },
                                onInfoClick = { selectedGroupForDetails = group }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 72.dp)
                            )
                        }
                    }
                }
            } else {
                // Discover Tab
                if (discoverableGroups.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aucun groupe trouvé dans cette catégorie.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(discoverableGroups) { group ->
                            val isMember = remember(group.memberIds, currentUser) {
                                val uid = currentUser?.id ?: ""
                                group.creatorId == uid || group.memberIds.split(",").contains(uid)
                            }
                            val isPending = remember(group.pendingMemberIds, currentUser) {
                                val uid = currentUser?.id ?: ""
                                group.pendingMemberIds.split(",").contains(uid)
                            }

                            DiscoverableGroupRow(
                                group = group,
                                isMember = isMember,
                                isPending = isPending,
                                onOpen = { onSelectGroup(group.id) },
                                onInfo = { selectedGroupForDetails = group },
                                onJoin = {
                                    scope.launch {
                                        val uid = currentUser?.id ?: return@launch
                                        if (group.requireApproval) {
                                            // Request to join
                                            val pendingList = (group.pendingMemberIds.split(",").filter { it.isNotEmpty() } + uid).distinct()
                                            repository.dao.updateGroupMembersAndPending(group.id, group.memberIds, pendingList.joinToString(","))
                                        } else {
                                            repository.addGroupMember(group.id, uid)
                                            JamesSoundPlayer.playSound(context, JamesSoundPlayer.SoundType.GROUP_INVITE)
                                        }
                                    }
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
        }
    }

    // Modal: Full Create Group with Categories, Photo/Video & Permissions
    if (showCreateDialog) {
        FullCreateGroupDialog(
            lang = currentLang,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc, rules, cat, inst, grade, avatarUrl, videoUrl, isPub, onlyAdmin, reqApp ->
                scope.launch {
                    val res = repository.createGroup(
                        name = name,
                        description = desc,
                        rules = rules,
                        category = cat,
                        institution = inst,
                        gradeLevel = grade,
                        avatarUrl = avatarUrl,
                        presentationVideoUrl = videoUrl,
                        isPublic = isPub,
                        onlyAdminsCanSend = onlyAdmin,
                        requireApproval = reqApp
                    )
                    showCreateDialog = false
                    res.onSuccess { group ->
                        JamesSoundPlayer.playSound(context, JamesSoundPlayer.SoundType.GROUP_INVITE)
                        onSelectGroup(group.id)
                    }
                }
            }
        )
    }

    // Modal: Detailed Group Info & Management
    selectedGroupForDetails?.let { group ->
        GroupDetailsModal(
            groupId = group.id,
            repository = repository,
            onDismiss = { selectedGroupForDetails = null },
            onOpenChat = {
                selectedGroupForDetails = null
                onSelectGroup(group.id)
            }
        )
    }
}

/**
 * Row for My Groups
 */
@Composable
fun GroupItemRow(
    group: GroupEntity,
    onClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val memberCount = remember(group.memberIds) {
        group.memberIds.split(",").filter { it.isNotEmpty() }.size
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("group_row_${group.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarWithStatus(
            name = group.name,
            avatarUrl = group.avatarUrl,
            size = 50.dp,
            showOnlineBadge = false
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (group.onlyAdminsCanSend) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🔒 Admins seuls",
                        fontSize = 10.sp,
                        color = JamesWarningAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${group.category} • $memberCount membres",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = group.description.ifEmpty { "Groupe officiel James Chat" },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        IconButton(onClick = onInfoClick) {
            Icon(Icons.Default.Security, contentDescription = "Infos du groupe", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * Row for Discoverable Groups
 */
@Composable
fun DiscoverableGroupRow(
    group: GroupEntity,
    isMember: Boolean,
    isPending: Boolean,
    onOpen: () -> Unit,
    onInfo: () -> Unit,
    onJoin: () -> Unit
) {
    val memberCount = remember(group.memberIds) {
        group.memberIds.split(",").filter { it.isNotEmpty() }.size
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInfo() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarWithStatus(
            name = group.name,
            avatarUrl = group.avatarUrl,
            size = 50.dp,
            showOnlineBadge = false
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1
            )
            Text(
                text = "${group.category} • $memberCount/500 membres",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            if (group.institution.isNotEmpty()) {
                Text(
                    text = "Établissement : ${group.institution}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = group.description.ifEmpty { "Communauté ouverte" },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isMember) {
            OutlinedButton(onClick = onOpen) {
                Text("Ouvrir", fontSize = 11.sp)
            }
        } else if (isPending) {
            Text(
                text = "En attente...",
                fontSize = 11.sp,
                color = JamesWarningAmber,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Button(onClick = onJoin) {
                Text(if (group.requireApproval) "Demander" else "Rejoindre", fontSize = 11.sp)
            }
        }
    }
}

/**
 * Full Create Group Modal with Categories, Storage Media Upload & Permissions
 */
@Composable
fun FullCreateGroupDialog(
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onCreate: (
        name: String,
        desc: String,
        rules: String,
        category: String,
        institution: String,
        gradeLevel: String,
        avatarUrl: String,
        videoUrl: String,
        isPublic: Boolean,
        onlyAdminsCanSend: Boolean,
        requireApproval: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Groupes d'amis") }
    var institution by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    var onlyAdminsCanSend by remember { mutableStateOf(false) }
    var requireApproval by remember { mutableStateOf(false) }

    var isUploadingMedia by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isUploadingMedia = true
            scope.launch {
                delay(800) // Simulated Supabase Storage upload
                avatarUrl = uri.toString()
                isUploadingMedia = false
            }
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isUploadingMedia = true
            scope.launch {
                delay(1200) // Simulated Supabase Storage upload
                videoUrl = uri.toString()
                isUploadingMedia = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Créer une Communauté James Chat", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du groupe *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("create_group_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Category selector
                Text("Catégorie du groupe :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GROUP_CATEGORIES.filter { it != "Tous" }) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                // Conditional school/church fields
                if (selectedCategory.contains("scolaire", ignoreCase = true) || selectedCategory.contains("classe", ignoreCase = true)) {
                    OutlinedTextField(
                        value = institution,
                        onValueChange = { institution = it },
                        label = { Text("École / Collège / Université") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = gradeLevel,
                        onValueChange = { gradeLevel = it },
                        label = { Text("Classe / Niveau (ex: 9e année)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (selectedCategory.contains("église", ignoreCase = true) || selectedCategory.contains("prière", ignoreCase = true)) {
                    OutlinedTextField(
                        value = institution,
                        onValueChange = { institution = it },
                        label = { Text("Nom de l'Église ou Ministère") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description de la communauté") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = rules,
                    onValueChange = { rules = it },
                    label = { Text("Règles du groupe (charte)") },
                    placeholder = { Text("1. Respect\n2. Pas de contenu illicite...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Media upload buttons (Supabase Storage)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (avatarUrl.isNotEmpty()) "Photo ✓" else "Photo", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { videoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (videoUrl.isNotEmpty()) "Vidéo Prés. ✓" else "Vidéo Prés.", fontSize = 11.sp)
                    }
                }

                if (isUploadingMedia) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Téléversement sécurisé vers Supabase Storage...", fontSize = 11.sp)
                    }
                }

                // Permissions Switches
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Seuls les admins peuvent écrire", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Les membres ordinaires peuvent lire mais pas envoyer de messages.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = onlyAdminsCanSend, onCheckedChange = { onlyAdminsCanSend = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Approbation requise pour rejoindre", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Un administrateur doit valider chaque demande d'adhésion.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = requireApproval, onCheckedChange = { requireApproval = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Groupe public / Découvrable", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Visible dans l'onglet 'Découvrir' par les autres utilisateurs.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isPublic, onCheckedChange = { isPublic = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, description, rules, selectedCategory, institution, gradeLevel, avatarUrl, videoUrl, isPublic, onlyAdminsCanSend, requireApproval)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("create_group_confirm_btn")
            ) {
                Text("Créer le groupe")
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
 * Group Details & Administrative Controls Modal
 */
@Composable
fun GroupDetailsModal(
    groupId: String,
    repository: JamesChatRepository,
    onDismiss: () -> Unit,
    onOpenChat: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val group by repository.dao.getGroupByIdFlow(groupId).collectAsState(initial = null)
    val groupMessages by repository.dao.getMessagesFlow(groupId).collectAsState(initial = emptyList())
    val allUsers by repository.dao.getAllUsersFlow().collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState()

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var addMemberJamesIdInput by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showReportGroupDialog by remember { mutableStateOf(false) }
    var reportReasonInput by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    if (group == null) return

    val g = group!!
    val currentUserId = currentUser?.id ?: ""
    val isCreator = g.creatorId == currentUserId
    val isAdmin = isCreator || g.adminIds.split(",").contains(currentUserId) || g.coAdminIds.split(",").contains(currentUserId)

    val memberIds = remember(g.memberIds) { g.memberIds.split(",").filter { it.isNotEmpty() } }
    val coAdminIds = remember(g.coAdminIds) { g.coAdminIds.split(",").filter { it.isNotEmpty() } }
    val pendingMemberIds = remember(g.pendingMemberIds) { g.pendingMemberIds.split(",").filter { it.isNotEmpty() } }

    val creatorUser = remember(allUsers, g.creatorId) { allUsers.find { it.id == g.creatorId } }
    val memberUsers = remember(allUsers, memberIds) { allUsers.filter { memberIds.contains(it.id) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarWithStatus(name = g.name, avatarUrl = g.avatarUrl, size = 44.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = g.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "${g.category} • ${memberIds.size} membres", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Video presentation banner if available
                if (g.presentationVideoUrl.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().height(110.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = JamesBluePrimary, modifier = Modifier.size(36.dp))
                                Text("Vidéo de présentation du groupe", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // General info
                Text(text = "DESCRIPTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = g.description.ifEmpty { "Aucune description renseignée." }, fontSize = 13.sp)

                if (g.rules.isNotEmpty()) {
                    Text(text = "RÈGLES DU GROUPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = g.rules, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Invitation Link & Code
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Lien d'invitation officiel :", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Text(text = "https://jameschat.ht/join/${g.inviteCode}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Shared Media, Documents & Links Gallery
                Text(text = "MÉDIAS, DOCUMENTS & LIENS PARTAGÉS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val photosCount = groupMessages.count { it.type == com.example.data.model.MessageType.PHOTO.name }
                        val videosCount = groupMessages.count { it.type == com.example.data.model.MessageType.VIDEO.name }
                        val audioCount = groupMessages.count { it.type == com.example.data.model.MessageType.AUDIO.name }
                        val linksCount = groupMessages.count { it.text.contains("http://") || it.text.contains("https://") }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Photos & Vidéos :", fontSize = 12.sp)
                            }
                            Text(text = "${photosCount + videosCount} fichier(s)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Messages Vocaux & Audios :", fontSize = 12.sp)
                            }
                            Text(text = "$audioCount audio(s)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Liens web partagés :", fontSize = 12.sp)
                            }
                            Text(text = "$linksCount lien(s)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Notification & Mute Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (g.isMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (g.isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Notifications du groupe", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = if (g.isMuted) "En sourdine" else "Actives", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = !g.isMuted,
                        onCheckedChange = { active ->
                            scope.launch {
                                repository.dao.updateGroup(g.copy(isMuted = !active))
                            }
                        }
                    )
                }

                // Mode Seuls les admins peuvent écrire
                if (isAdmin) {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mode Seuls Admins", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Autoriser uniquement les administrateurs à envoyer des messages.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = g.onlyAdminsCanSend,
                            onCheckedChange = { newValue ->
                                scope.launch {
                                    repository.toggleGroupAdminsOnly(g.id, newValue)
                                }
                            }
                        )
                    }
                }

                // Add Member by James ID
                if (isAdmin) {
                    OutlinedButton(
                        onClick = { showAddMemberDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ajouter un membre par James ID")
                    }
                }

                // Pending members list
                if (isAdmin && pendingMemberIds.isNotEmpty()) {
                    Text(text = "DEMANDES D'ADHÉSION EN ATTENTE (${pendingMemberIds.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JamesWarningAmber)
                    pendingMemberIds.forEach { pid ->
                        val pUser = allUsers.find { it.id == pid }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = pUser?.username ?: pid, fontSize = 13.sp)
                            Row {
                                TextButton(onClick = { scope.launch { repository.approvePendingMember(g.id, pid) } }) {
                                    Text("Accepter", fontSize = 11.sp)
                                }
                                TextButton(onClick = { scope.launch { repository.removeGroupMember(g.id, pid) } }) {
                                    Text("Refuser", fontSize = 11.sp, color = JamesErrorRed)
                                }
                            }
                        }
                    }
                }

                // Members List with distinct roles
                HorizontalDivider()
                Text(text = "MEMBRES & ADMINISTRATEURS (${memberIds.size}/500)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                memberUsers.forEach { user ->
                    val isMemberCreator = user.id == g.creatorId
                    val isMemberAdmin = isMemberCreator || coAdminIds.contains(user.id) || g.adminIds.split(",").contains(user.id)

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarWithStatus(name = user.username, avatarUrl = user.avatarUrl, size = 34.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "@${user.username}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (user.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        JamesVBadge(
                                            size = 12.dp,
                                            style = com.example.data.model.VBadgeStyle.fromCode(user.verificationBadgeStyle),
                                            userAccountName = user.username
                                        )
                                    }
                                }
                                Text(
                                    text = if (isMemberCreator) "👑 Créateur du groupe" else if (isMemberAdmin) "🛡️ Administrateur" else "Membre",
                                    fontSize = 10.sp,
                                    color = if (isMemberCreator) JamesWarningAmber else if (isMemberAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Role & removal actions
                            if (isCreator && !isMemberCreator) {
                                if (isMemberAdmin) {
                                    TextButton(onClick = { scope.launch { repository.setGroupAdminRole(g.id, user.id, false) } }) {
                                        Text("Rétrograder", fontSize = 10.sp)
                                    }
                                } else {
                                    TextButton(onClick = { scope.launch { repository.setGroupAdminRole(g.id, user.id, true) } }) {
                                        Text("Nommer Admin", fontSize = 10.sp)
                                    }
                                }
                            }

                            if (isAdmin && !isMemberCreator && (isCreator || !isMemberAdmin)) {
                                IconButton(
                                    onClick = { scope.launch { repository.removeGroupMember(g.id, user.id) } },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.PersonRemove, contentDescription = "Retirer", tint = JamesErrorRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom actions: Leave / Dissolve
                // Bottom actions: Leave / Dissolve / Report
                OutlinedButton(
                    onClick = { showReportGroupDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Signaler ce groupe pour abus", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                if (isCreator) {
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = JamesErrorRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dissoudre définitivement le groupe")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                repository.leaveGroup(g.id)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JamesErrorRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Quitter le groupe")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onOpenChat) {
                Text("Ouvrir la discussion")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )

    // Add Member Dialog
    if (showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Ajouter un membre") },
            text = {
                Column {
                    Text("Entrez le James ID ou @username de la personne :", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = addMemberJamesIdInput,
                        onValueChange = { addMemberJamesIdInput = it },
                        placeholder = { Text("ex: JAMES#2001 ou sophie_d") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val q = addMemberJamesIdInput.trim()
                    val targetUser = allUsers.find {
                        it.fourDigitCode == q ||
                        it.jamesTag.equals(q, ignoreCase = true) ||
                        it.username.equals(q, ignoreCase = true) ||
                        it.email.equals(q, ignoreCase = true)
                    }
                    if (targetUser != null) {
                        scope.launch {
                            repository.addGroupMember(g.id, targetUser.id)
                            showAddMemberDialog = false
                            addMemberJamesIdInput = ""
                        }
                    }
                }) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dissolve Confirmation
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Dissoudre le groupe", color = JamesErrorRed, fontWeight = FontWeight.Bold) },
            text = {
                Text("Êtes-vous certain de vouloir dissoudre '${g.name}' ? Tous les messages et l'accès pour les membres seront définitivement supprimés.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.dissolveGroup(g.id)
                            showDeleteConfirmDialog = false
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JamesErrorRed)
                ) {
                    Text("Confirmer la dissolution")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Report Group Dialog
    if (showReportGroupDialog) {
        AlertDialog(
            onDismissRequest = { showReportGroupDialog = false },
            title = { Text("Signaler le groupe", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Motif du signalement :", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportReasonInput,
                        onValueChange = { reportReasonInput = it },
                        placeholder = { Text("Spam, contenu inapproprié, harcèlement...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = reportReasonInput.ifBlank { "Contenu inapproprié ou spam" }
                        scope.launch {
                            repository.reportContent(
                                targetType = "GROUP",
                                targetId = g.id,
                                reportedUserId = g.creatorId,
                                reason = reason,
                                details = "Signalement envoyé contre le groupe ${g.name}"
                            )
                            showReportGroupDialog = false
                            reportReasonInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JamesErrorRed)
                ) {
                    Text("Envoyer le signalement")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportGroupDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
