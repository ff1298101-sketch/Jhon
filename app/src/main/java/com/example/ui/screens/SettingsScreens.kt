package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Verified
import com.example.util.JamesSoundPlayer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ThemeMode
import com.example.data.repository.JamesChatRepository
import com.example.util.JamesI18n
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOpenWeb: () -> Unit = {},
    onOpenAcademy: () -> Unit = {},
    onOpenGames: () -> Unit = {},
    onOpenAdmin: () -> Unit = {}
) {
    val themeMode by repository.themeMode.collectAsState()
    val fontSizeScale by repository.fontSizeScale.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundsEnabled by repository.soundsEnabled.collectAsState()
    val batterySaver by repository.batterySaver.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val customRingtone by repository.customRingtone.collectAsState()
    val isSilentMode by repository.isSilentMode.collectAsState()
    val isVibrationEnabled by repository.isVibrationEnabled.collectAsState()
    val vibrationPattern by repository.vibrationPattern.collectAsState()
    val scope = rememberCoroutineScope()

    var showThemeMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showRingtoneMenu by remember { mutableStateOf(false) }
    var showVibrationPatternMenu by remember { mutableStateOf(false) }
    var isPreviewingRingtone by remember { mutableStateOf(false) }
    var showVBadgeRequestDialog by remember { mutableStateOf(false) }
    var sliderFontScale by remember(fontSizeScale) { mutableFloatStateOf(fontSizeScale) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(JamesI18n.t("tab_settings", currentLang), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
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
                .padding(16.dp)
        ) {
            Text(
                text = JamesI18n.t("appearance", currentLang),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.ColorLens,
                        title = "Thème d'affichage",
                        subtitle = when (themeMode) {
                            ThemeMode.LIGHT -> "Clair"
                            ThemeMode.DARK -> "Sombre"
                            ThemeMode.AMOLED -> "AMOLED Noir Pur"
                            ThemeMode.SYSTEM -> "Selon le système"
                        },
                        onClick = { showThemeMenu = true }
                    )

                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false }
                    ) {
                        ThemeMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name) },
                                onClick = {
                                    repository.setThemeMode(mode)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Taille du texte", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        }
                        Slider(
                            value = sliderFontScale,
                            onValueChange = {
                                sliderFontScale = it
                                repository.setFontSizeScale(it)
                            },
                            valueRange = 0.85f..1.25f,
                            steps = 3,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Préférences & Langue",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Language,
                        title = JamesI18n.t("language_title", currentLang),
                        subtitle = "${currentLang.flag} ${currentLang.displayName}",
                        onClick = { showLanguageMenu = true }
                    )

                    if (showLanguageMenu) {
                        com.example.ui.components.JamesLanguagePickerDialog(
                            currentLanguage = currentLang,
                            onLanguageSelected = { lang ->
                                repository.setLanguage(lang)
                            },
                            onDismiss = { showLanguageMenu = false }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Sons originaux James Chat", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Harmonies douces bleu & cyan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = soundsEnabled,
                            onCheckedChange = { repository.setSoundsEnabled(it) }
                        )
                    }

                    if (soundsEnabled) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    com.example.util.JamesSoundPlayer.playSound(
                                        context,
                                        com.example.util.JamesSoundPlayer.SoundType.STARTUP_JINGLE
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Jingle", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    com.example.util.JamesSoundPlayer.playSound(
                                        context,
                                        com.example.util.JamesSoundPlayer.SoundType.NEW_MESSAGE
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Message", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    com.example.util.JamesSoundPlayer.playSound(
                                        context,
                                        com.example.util.JamesSoundPlayer.SoundType.INCOMING_CALL
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Appel", fontSize = 11.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BatteryChargingFull, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Économiseur de batterie", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                        Switch(
                            checked = batterySaver,
                            onCheckedChange = { repository.setBatterySaver(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Appels, Sonneries & Vibrations",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    val currentStyle = JamesSoundPlayer.RingtoneStyle.fromId(customRingtone)

                    SettingsRow(
                        icon = Icons.Default.PhoneInTalk,
                        title = "Sonnerie d'appel personnalisée",
                        subtitle = "${currentStyle.title} • ${currentStyle.description}",
                        onClick = { showRingtoneMenu = true }
                    )

                    DropdownMenu(
                        expanded = showRingtoneMenu,
                        onDismissRequest = { showRingtoneMenu = false }
                    ) {
                        JamesSoundPlayer.RingtoneStyle.values().forEach { style ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(style.title, fontWeight = FontWeight.SemiBold)
                                        Text(style.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    repository.setCustomRingtone(style.id)
                                    showRingtoneMenu = false
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (isPreviewingRingtone) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Aperçu de la sonnerie", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text("Écouter la mélodie et la vibration", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Button(
                            onClick = {
                                if (isPreviewingRingtone) {
                                    JamesSoundPlayer.stopRingtone(context)
                                    isPreviewingRingtone = false
                                } else {
                                    isPreviewingRingtone = true
                                    JamesSoundPlayer.previewRingtone(context, currentStyle, isVibrationEnabled)
                                    scope.launch {
                                        kotlinx.coroutines.delay(3500)
                                        isPreviewingRingtone = false
                                    }
                                }
                            },
                            modifier = Modifier.testTag("settings_preview_ringtone_btn")
                        ) {
                            Text(if (isPreviewingRingtone) "Arrêter" else "Écouter", fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Mode silencieux pour appels", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text("Désactive la tonalité lors des appels entrants", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isSilentMode,
                            onCheckedChange = { repository.setSilentMode(it) },
                            modifier = Modifier.testTag("settings_silent_mode_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Vibration pour les appels", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text("Faire vibrer l'appareil lors d'un appel", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isVibrationEnabled,
                            onCheckedChange = { repository.setVibrationEnabled(it) },
                            modifier = Modifier.testTag("settings_vibration_switch")
                        )
                    }

                    if (isVibrationEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        SettingsRow(
                            icon = Icons.Default.Vibration,
                            title = "Motif de vibration",
                            subtitle = "Style actif : $vibrationPattern",
                            onClick = { showVibrationPatternMenu = true }
                        )

                        DropdownMenu(
                            expanded = showVibrationPatternMenu,
                            onDismissRequest = { showVibrationPatternMenu = false }
                        ) {
                            listOf("Standard", "Douce", "Intense").forEach { pattern ->
                                DropdownMenuItem(
                                    text = { Text(pattern) },
                                    onClick = {
                                        repository.setVibrationPattern(pattern)
                                        showVibrationPatternMenu = false
                                        JamesSoundPlayer.triggerVibrationPattern(context, pattern)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Éducation & Services James",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.School,
                        title = "🎓 James Academy (232 Matières)",
                        subtitle = "Histoire, Géographie, Sciences, Maths, Langues, Informatique & Certificats",
                        onClick = onOpenAcademy
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    SettingsRow(
                        icon = Icons.Default.SportsEsports,
                        title = "🎮 Jeux Éducatifs (40 Jeux interactifs)",
                        subtitle = "Quiz, Capitales, Drapeaux, Chronologie, Maths, Vocabulaire & Puzzles",
                        onClick = onOpenGames
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    SettingsRow(
                        icon = Icons.Default.Language,
                        title = "🌐 James Chat Web & Fichiers Volumineux",
                        subtitle = "Connexion PC, Partage de Films, Vidéos, Photos HD et Documents",
                        onClick = onOpenWeb
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Compte & Sécurité",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Devices,
                        title = "${JamesI18n.t("linked_devices", currentLang)} (James Web)",
                        subtitle = "Konekte sou òdinatè oswa navigatè web",
                        onClick = onOpenWeb
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    SettingsRow(
                        icon = Icons.Default.Verified,
                        title = "Demander un V Badge officiel",
                        subtitle = if (currentUser?.isVerified == true) "Compte certifié ✓ (${com.example.data.model.VBadgeStyle.fromCode(currentUser?.verificationBadgeStyle ?: "SEAL").title})" else "Certification créateur ou personnalité",
                        onClick = { showVBadgeRequestDialog = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = JamesI18n.t("personal_stats", currentLang),
                        subtitle = "Messages envoyés, stockage",
                        onClick = { /* Open personal stats */ }
                    )

                    if (repository.isCurrentUserAdmin()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        SettingsRow(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Panneau d'Administration",
                            subtitle = "Accès réservé aux administrateurs officiels",
                            onClick = onOpenAdmin
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    SettingsRow(
                        icon = Icons.Default.Logout,
                        title = JamesI18n.t("logout_btn", currentLang),
                        subtitle = "Fermer la session sur cet appareil",
                        onClick = {
                            scope.launch {
                                repository.logout()
                                onLogout()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "James Chat 🇭🇹 • Fèt an Ayiti pou tout mond lan",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "v6.0-v10.0 • Chiffrement E2EE & WebRTC HD",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }

    if (showVBadgeRequestDialog && currentUser != null) {
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
            onDismissRequest = { if (!isSubmitting) showVBadgeRequestDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.example.ui.components.JamesVBadge(size = 22.dp, style = reqPreferredStyle, showTooltipOnClick = false)
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
                        text = "La certification officielle V Badge authentifie votre profil auprès de toute la communauté James Chat.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = reqAccountName,
                        onValueChange = { reqAccountName = it },
                        label = { Text("Nom du compte") },
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
                                    com.example.ui.components.JamesVBadge(size = 20.dp, style = styleOpt, showTooltipOnClick = false)
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
                        placeholder = { Text("Ex: Créateur officiel de contenu...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = reqAdditionalInfo,
                        onValueChange = { reqAdditionalInfo = it },
                        label = { Text("Informations complémentaires (facultatif)") },
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
                                    showVBadgeRequestDialog = false
                                },
                                onFailure = {
                                    formError = it.message ?: "Erreur lors de l'envoi"
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
                TextButton(onClick = { showVBadgeRequestDialog = false }, enabled = !isSubmitting) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
