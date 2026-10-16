package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.repository.JamesChatRepository
import com.example.ui.components.AvatarWithStatus
import com.example.ui.components.JamesTeamMemberBadge
import com.example.ui.components.JamesVBadge
import com.example.ui.components.VideoPreviewDialog
import com.example.ui.theme.JamesBluePrimary
import com.example.ui.theme.JamesOnlineGreen
import com.example.ui.theme.JamesWarningAmber
import com.example.util.JamesI18n
import com.example.util.JamesIdentityUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onBack: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var firstName by remember(currentUser) { mutableStateOf(currentUser?.firstName ?: "") }
    var lastName by remember(currentUser) { mutableStateOf(currentUser?.lastName ?: "") }
    var age by remember(currentUser) { mutableStateOf(currentUser?.age?.toString() ?: "") }
    var city by remember(currentUser) { mutableStateOf(currentUser?.city ?: "") }
    var country by remember(currentUser) { mutableStateOf(currentUser?.country ?: "") }
    var bio by remember(currentUser) { mutableStateOf(currentUser?.bio ?: "") }
    var avatarUrl by remember(currentUser) { mutableStateOf(currentUser?.avatarUrl ?: "") }
    var showVideoOnProfile by remember(currentUser) { mutableStateOf(currentUser?.showVideoOnProfile ?: false) }
    var profileVideoUrl by remember(currentUser) { mutableStateOf(currentUser?.profileVideoUrl ?: "") }
    var showVideoPreview by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf(false) }
    var showLinkGoogleDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showRequestVBadgeDialog by remember { mutableStateOf(false) }
    var requestFeedbackMessage by remember { mutableStateOf<String?>(null) }
    val latestVerificationReq by repository.dao.getLatestVerificationRequestFlow(currentUser?.id ?: "").collectAsState(initial = null)
    val clipboard = LocalClipboardManager.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val uriStr = uri.toString()
            avatarUrl = uriStr
            scope.launch {
                repository.updateProfile(
                    firstName = firstName,
                    lastName = lastName,
                    age = age.toIntOrNull() ?: 0,
                    city = city,
                    country = country,
                    bio = bio,
                    avatarUrl = uriStr,
                    profileVideoUrl = profileVideoUrl,
                    showVideoOnProfile = showVideoOnProfile
                )
                saveSuccessMessage = true
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val uriStr = uri.toString()
            profileVideoUrl = uriStr
            showVideoOnProfile = true
            scope.launch {
                repository.updateProfile(
                    firstName = firstName,
                    lastName = lastName,
                    age = age.toIntOrNull() ?: 0,
                    city = city,
                    country = country,
                    bio = bio,
                    avatarUrl = avatarUrl,
                    profileVideoUrl = uriStr,
                    showVideoOnProfile = true
                )
                saveSuccessMessage = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(JamesI18n.t("tab_profile", currentLang), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                AvatarWithStatus(
                    name = if (firstName.isNotEmpty()) "$firstName $lastName" else currentUser?.username ?: "Me",
                    avatarUrl = avatarUrl,
                    hasProfileVideo = profileVideoUrl.isNotEmpty() && showVideoOnProfile,
                    size = 104.dp,
                    showOnlineBadge = true,
                    onClick = {
                        if (profileVideoUrl.isNotEmpty() && showVideoOnProfile) {
                            showVideoPreview = true
                        } else {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    }
                )

                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(JamesBluePrimary)
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Changer photo", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Verified Badge / Team badge row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (firstName.isNotEmpty()) "$firstName $lastName" else "@${currentUser?.username ?: "username"}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (currentUser?.isVerified == true) {
                    Spacer(modifier = Modifier.width(6.dp))
                    JamesVBadge(
                        size = 20.dp,
                        style = com.example.data.model.VBadgeStyle.fromCode(currentUser?.verificationBadgeStyle ?: "SEAL"),
                        userAccountName = currentUser?.username
                    )
                }
            }

            if (currentUser?.isTeamMember == true) {
                Spacer(modifier = Modifier.height(4.dp))
                JamesTeamMemberBadge()
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Carte d'identité James Chat (James Tag As Primary Identifier)
            val currentFourDigit = currentUser?.let { JamesIdentityUtil.getFourDigitId(it) } ?: "0000"
            val currentJamesTag = currentUser?.let { JamesIdentityUtil.getJamesTag(it) } ?: "JAMES#0000"
            val currentCustomId = currentUser?.let { JamesIdentityUtil.getJamesCustomId(it) } ?: "JMS-0000"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "IDENTIFIANT PRINCIPAL JAMES CHAT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Prominent James Tag Badge
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.clickable { clipboard.setText(AnnotatedString(currentJamesTag)) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🏷️ $currentJamesTag",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ID 4 chiffres", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currentFourDigit, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Nom d'utilisateur", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("@${currentUser?.username ?: ""}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("James ID", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currentCustomId, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FilledTonalButton(
                        onClick = { showQrDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mon QR Code Personnel", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Informations personnelles", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text(JamesI18n.t("first_name", currentLang)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text(JamesI18n.t("last_name", currentLang)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text(JamesI18n.t("age_label", currentLang)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text(JamesI18n.t("city_label", currentLang)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text(JamesI18n.t("country_label", currentLang)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text(JamesI18n.t("bio_label", currentLang)) },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = JamesI18n.t("profile_media", currentLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Chwazi foto oswa videyo ki nan galri aparèy ou an pou w mete sou pwofil ou.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 1: PHOTO DE PROFIL
                    Text(
                        text = JamesI18n.t("profile_photo", currentLang),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(JamesI18n.t("choose_photo_gallery", currentLang), fontSize = 12.sp)
                        }

                        if (avatarUrl.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        repository.removeProfilePhoto()
                                        avatarUrl = ""
                                        saveSuccessMessage = true
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = JamesI18n.t("remove_photo", currentLang),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 2: VIDEO DE PROFIL
                    Text(
                        text = JamesI18n.t("profile_video", currentLang),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = JamesI18n.t("show_video_on_profile", currentLang), fontSize = 13.sp)
                        Switch(
                            checked = showVideoOnProfile,
                            onCheckedChange = {
                                showVideoOnProfile = it
                                scope.launch {
                                    repository.updateProfile(
                                        firstName = firstName,
                                        lastName = lastName,
                                        age = age.toIntOrNull() ?: 0,
                                        city = city,
                                        country = country,
                                        bio = bio,
                                        avatarUrl = avatarUrl,
                                        profileVideoUrl = profileVideoUrl,
                                        showVideoOnProfile = it
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (profileVideoUrl.isEmpty()) JamesI18n.t("choose_video_gallery", currentLang)
                                else JamesI18n.t("replace_video", currentLang),
                                fontSize = 12.sp
                            )
                        }

                        if (profileVideoUrl.isNotEmpty()) {
                            Button(
                                onClick = { showVideoPreview = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Lire la vidéo", modifier = Modifier.size(16.dp))
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        repository.removeProfileVideo()
                                        profileVideoUrl = ""
                                        showVideoOnProfile = false
                                        saveSuccessMessage = true
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = JamesI18n.t("remove_video", currentLang),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (profileVideoUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (profileVideoUrl.startsWith("content://") || profileVideoUrl.startsWith("file://"))
                                "🎬 Vidéyo chwazi nan galri telefòn ou (Klike sou flèch la pou w li l)"
                            else "🎬 Vidéyo pwofil anrejistre",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌐", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Compte Google", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val isGoogleLinked = currentUser?.email?.endsWith("@gmail.com") == true
                    if (isGoogleLinked) {
                        Text(
                            text = "Compte Google associé : ${currentUser?.email}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ Vos contacts, conversations, paramètres et profil sont conservés.",
                            fontSize = 11.sp,
                            color = JamesOnlineGreen
                        )
                    } else {
                        Text(
                            text = "Associez votre compte Google à votre profil James Chat tout en conservant votre nom d'utilisateur, vos conversations et vos contacts.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showLinkGoogleDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Associer mon compte Google", fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section V Badge Officiel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            JamesVBadge(
                                size = 20.dp,
                                style = com.example.data.model.VBadgeStyle.fromCode(currentUser?.verificationBadgeStyle ?: "SEAL"),
                                showTooltipOnClick = false
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Badge de Vérification (V Badge)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (currentUser?.isVerified == true) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JamesOnlineGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("VÉRIFIÉ ✓", color = JamesOnlineGreen, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentUser?.isVerified == true) {
                        val badgeStyleName = com.example.data.model.VBadgeStyle.fromCode(currentUser?.verificationBadgeStyle ?: "SEAL").title
                        Text(
                            text = "Votre compte est officiellement vérifié par James Chat avec le style $badgeStyleName.",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!currentUser?.verificationReason.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Distinction : ${currentUser?.verificationReason}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        val req = latestVerificationReq
                        if (req != null && req.status == "PENDING") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(JamesWarningAmber.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "⏳ Demande de V Badge en cours d'examen",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = JamesWarningAmber
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Type : ${req.accountType} • Style souhaité : ${com.example.data.model.VBadgeStyle.fromCode(req.preferredStyle).title}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Les administrateurs James Chat examinent votre dossier de certification.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else if (req != null && req.status == "REJECTED") {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "✕ Demande de vérification refusée",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        if (req.rejectionReason.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Motif : ${req.rejectionReason}",
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { showRequestVBadgeDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Soumettre une nouvelle demande", fontSize = 12.sp)
                                }
                            }
                        } else {
                            Text(
                                text = "Obtenez le badge bleu officiel (Style Sceau ou Royal) pour confirmer votre identité en tant que créateur, personnalité ou organisation.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { showRequestVBadgeDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Demander un V Badge officiel", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch {
                        repository.updateProfile(
                            firstName = firstName,
                            lastName = lastName,
                            age = age.toIntOrNull() ?: 0,
                            city = city,
                            country = country,
                            bio = bio,
                            avatarUrl = avatarUrl,
                            profileVideoUrl = profileVideoUrl,
                            showVideoOnProfile = showVideoOnProfile
                        )
                        saveSuccessMessage = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_profile_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(JamesI18n.t("save_btn", currentLang), fontWeight = FontWeight.Bold)
            }

            if (saveSuccessMessage) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Modifications enregistrées !", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            }
        }
    }

    if (showVideoPreview) {
        VideoPreviewDialog(
            title = "Vidéo de profil",
            videoUrl = profileVideoUrl,
            onDismiss = { showVideoPreview = false }
        )
    }

    if (showLinkGoogleDialog) {
        var linkEmail by remember { mutableStateOf("") }
        var linkError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showLinkGoogleDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🌐", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Associer un compte Google", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Entrez votre adresse email Google pour associer votre compte James Chat. Vos données, contacts, profil et conversations seront conservés :",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = linkEmail,
                        onValueChange = { linkEmail = it; linkError = null },
                        label = { Text("Adresse email Google (@gmail.com)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (linkError != null) {
                        Text(text = linkError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = linkEmail.trim()
                        if (trimmed.isNotEmpty()) {
                            scope.launch {
                                val res = repository.linkGoogleToCurrentAccount(trimmed)
                                res.fold(
                                    onSuccess = {
                                        showLinkGoogleDialog = false
                                        saveSuccessMessage = true
                                    },
                                    onFailure = {
                                        linkError = it.message ?: "Erreur d'association du compte Google"
                                    }
                                )
                            }
                        }
                    },
                    enabled = linkEmail.contains("@")
                ) {
                    Text("Associer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkGoogleDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showQrDialog && currentUser != null) {
        val user = currentUser!!
        val qrBitmap = remember(user) {
            JamesIdentityUtil.generateUserQrCodeBitmap(user, size = 300)
        }
        val fourDigit = JamesIdentityUtil.getFourDigitId(user)
        val jamesTag = JamesIdentityUtil.getJamesTag(user)
        val jamesCustomId = JamesIdentityUtil.getJamesCustomId(user)

        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Mon QR Code Personnel", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
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

                    Text(
                        text = "Partagez ce QR Code avec vos amis pour qu'ils vous ajoutent instantanément sur James Chat.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ID: $fourDigit • @${user.username} • $jamesCustomId",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
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
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Modal Formulaire : Demander un V Badge officiel
    if (showRequestVBadgeDialog && currentUser != null) {
        val user = currentUser!!
        var reqAccountName by remember { mutableStateOf("${user.firstName} ${user.lastName}".trim().ifEmpty { user.username }) }
        var reqUsername by remember { mutableStateOf(user.username) }
        var reqAccountType by remember { mutableStateOf("Créateur") }
        var reqReason by remember { mutableStateOf("") }
        var reqAdditionalInfo by remember { mutableStateOf("") }
        var reqPreferredStyle by remember { mutableStateOf(com.example.data.model.VBadgeStyle.SEAL) }
        var formError by remember { mutableStateOf<String?>(null) }
        var isSubmitting by remember { mutableStateOf(false) }

        val accountTypes = listOf("Créateur", "Personnalité publique", "Entreprise", "Communauté", "Autre")

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showRequestVBadgeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    JamesVBadge(size = 22.dp, style = reqPreferredStyle, showTooltipOnClick = false)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Demande de V Badge Officiel", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Remplissez ce formulaire pour soumettre votre compte à la certification officielle de James Chat. Les comptes administrateurs reçoivent le badge automatiquement.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = reqAccountName,
                        onValueChange = { reqAccountName = it },
                        label = { Text("Nom du compte ou entité") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = reqUsername,
                        onValueChange = { reqUsername = it },
                        label = { Text("Nom d'utilisateur (@)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Type de compte
                    Text(text = "Type de compte :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(accountTypes) { type ->
                            androidx.compose.material3.FilterChip(
                                selected = reqAccountType == type,
                                onClick = { reqAccountType = type },
                                label = { Text(type, fontSize = 11.5.sp) }
                            )
                        }
                    }

                    // Style préféré (Sceau vs Royal mis en avant)
                    Text(text = "Style de badge souhaité :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(com.example.data.model.VBadgeStyle.SEAL, com.example.data.model.VBadgeStyle.ROYAL).forEach { styleOpt ->
                            val isSel = reqPreferredStyle == styleOpt
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { reqPreferredStyle = styleOpt },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSel) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    JamesVBadge(size = 20.dp, style = styleOpt, showTooltipOnClick = false)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = styleOpt.title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reqReason,
                        onValueChange = { reqReason = it },
                        label = { Text("Motif de la demande *") },
                        placeholder = { Text("Ex: Créateur de contenu officiel, artiste, marque...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = reqAdditionalInfo,
                        onValueChange = { reqAdditionalInfo = it },
                        label = { Text("Informations complémentaires (facultatif)") },
                        placeholder = { Text("Liens, réseaux, références pour vérification...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (formError != null) {
                        Text(text = formError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reqReason.isBlank()) {
                            formError = "Veuillez préciser le motif de votre demande."
                            return@Button
                        }
                        isSubmitting = true
                        scope.launch {
                            val res = repository.submitVerificationRequest(
                                accountName = reqAccountName,
                                username = reqUsername,
                                accountType = reqAccountType,
                                reason = reqReason,
                                additionalInfo = reqAdditionalInfo,
                                preferredStyle = reqPreferredStyle.code
                            )
                            isSubmitting = false
                            res.fold(
                                onSuccess = {
                                    showRequestVBadgeDialog = false
                                },
                                onFailure = {
                                    formError = it.message ?: "Erreur lors de l'envoi de la demande"
                                }
                            )
                        }
                    },
                    enabled = !isSubmitting
                ) {
                    Text(if (isSubmitting) "Envoi..." else "Envoyer la demande")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestVBadgeDialog = false }, enabled = !isSubmitting) {
                    Text("Annuler")
                }
            }
        )
    }
}
