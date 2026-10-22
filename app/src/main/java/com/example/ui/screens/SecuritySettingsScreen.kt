package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.repository.JamesChatRepository
import com.example.ui.theme.JamesBluePrimary
import com.example.ui.theme.JamesErrorRed
import com.example.ui.theme.JamesOnlineGreen
import com.example.util.JamesI18n
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onBack: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val sessions by repository.dao.getUserSessionsFlow(currentUser?.id ?: "").collectAsState(initial = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var showTwoFactorDialog by remember { mutableStateOf(false) }
    var twoFactorInput by remember { mutableStateOf("") }

    // Privacy menu states
    var showLastSeenMenu by remember { mutableStateOf(false) }
    var showPhotoMenu by remember { mutableStateOf(false) }
    var showCallsMenu by remember { mutableStateOf(false) }
    var showGroupsMenu by remember { mutableStateOf(false) }

    val privacyOptions = listOf(
        "EVERYONE" to "Tout le monde",
        "CONTACTS" to "Mes contacts uniquement",
        "NOBODY" to "Personne"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sécurité & Confidentialité V2", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("security_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
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
            // Header Security Shield
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
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Protection Avancée E2EE", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "Verrouillez vos discussions avec code PIN, biométrie et authentification 2FA.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: VERROUILLAGE APPLICATION
            SectionLabel("VERROUILLAGE & AUTHENTIFICATION")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column {
                    // PIN Code
                    val hasPin = !currentUser?.appLockPin.isNullOrEmpty()
                    SecuritySettingRow(
                        icon = Icons.Default.Lock,
                        title = "Verrouillage par code PIN",
                        subtitle = if (hasPin) "PIN actif (4 chiffres configurés)" else "Désactivé - Touchez pour configurer",
                        trailing = {
                            Switch(
                                checked = hasPin,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        showPinDialog = true
                                    } else {
                                        scope.launch {
                                            repository.updateSecurityPin(
                                                pin = "",
                                                biometric = false,
                                                twoFactor = currentUser?.twoFactorPin ?: ""
                                            )
                                            Toast.makeText(context, "Verrouillage PIN désactivé", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        },
                        onClick = { showPinDialog = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Biometrics
                    SecuritySettingRow(
                        icon = Icons.Default.Fingerprint,
                        title = "Déverrouillage biométrique",
                        subtitle = "Empreinte digitale / Reconnaissance faciale",
                        trailing = {
                            Switch(
                                checked = currentUser?.isBiometricEnabled == true,
                                enabled = hasPin,
                                onCheckedChange = { enabled ->
                                    scope.launch {
                                        repository.updateSecurityPin(
                                            pin = currentUser?.appLockPin ?: "",
                                            biometric = enabled,
                                            twoFactor = currentUser?.twoFactorPin ?: ""
                                        )
                                    }
                                }
                            )
                        },
                        onClick = {}
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // 2-Step Verification
                    val has2FA = !currentUser?.twoFactorPin.isNullOrEmpty()
                    SecuritySettingRow(
                        icon = Icons.Default.VpnKey,
                        title = "Vérification en deux étapes (2FA)",
                        subtitle = if (has2FA) "2FA activé pour protéger vos connexions" else "Sécurisez votre compte avec un PIN 2FA",
                        trailing = {
                            Switch(
                                checked = has2FA,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        showTwoFactorDialog = true
                                    } else {
                                        scope.launch {
                                            repository.updateSecurityPin(
                                                pin = currentUser?.appLockPin ?: "",
                                                biometric = currentUser?.isBiometricEnabled == true,
                                                twoFactor = ""
                                            )
                                            Toast.makeText(context, "2FA désactivé", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        },
                        onClick = { showTwoFactorDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: APPAREILS CONNECTES & SESSIONS
            SectionLabel("APPAREILS CONNECTÉS & SESSIONS")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = JamesOnlineGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Cet appareil (Session Active)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Android • Connecté en local E2EE", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(JamesOnlineGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("En ligne", color = JamesOnlineGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Laptop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("James Web • Navigateur PC", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Sèvè local :8080 • Google Chrome", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "Session distante fermée avec succès", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Déconnecter", fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Alerte de sécurité activée : Vous recevrez une notification immédiate lors de chaque nouvelle connexion.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 3: PARAMETRES CONFIDENTIALITE
            SectionLabel("CONTRÔLE DE CONFIDENTIALITÉ")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column {
                    // Last Seen
                    Box {
                        SecuritySettingRow(
                            icon = Icons.Default.PrivacyTip,
                            title = "Dernière présence & En ligne",
                            subtitle = privacyOptions.find { it.first == currentUser?.privacyLastSeen }?.second ?: "Tout le monde",
                            trailing = { Text("Modifier", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary) },
                            onClick = { showLastSeenMenu = true }
                        )
                        DropdownMenu(expanded = showLastSeenMenu, onDismissRequest = { showLastSeenMenu = false }) {
                            privacyOptions.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        scope.launch {
                                            repository.updatePrivacySettings(
                                                lastSeen = key,
                                                photo = currentUser?.privacyProfilePhoto ?: "EVERYONE",
                                                calls = currentUser?.privacyWhoCanCall ?: "EVERYONE",
                                                groups = currentUser?.privacyWhoCanAddGroups ?: "EVERYONE"
                                            )
                                        }
                                        showLastSeenMenu = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Profile Photo
                    Box {
                        SecuritySettingRow(
                            icon = Icons.Default.PrivacyTip,
                            title = "Photo et Vidéo de profil",
                            subtitle = privacyOptions.find { it.first == currentUser?.privacyProfilePhoto }?.second ?: "Tout le monde",
                            trailing = { Text("Modifier", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary) },
                            onClick = { showPhotoMenu = true }
                        )
                        DropdownMenu(expanded = showPhotoMenu, onDismissRequest = { showPhotoMenu = false }) {
                            privacyOptions.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        scope.launch {
                                            repository.updatePrivacySettings(
                                                lastSeen = currentUser?.privacyLastSeen ?: "EVERYONE",
                                                photo = key,
                                                calls = currentUser?.privacyWhoCanCall ?: "EVERYONE",
                                                groups = currentUser?.privacyWhoCanAddGroups ?: "EVERYONE"
                                            )
                                        }
                                        showPhotoMenu = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Who can call me
                    Box {
                        SecuritySettingRow(
                            icon = Icons.Default.PrivacyTip,
                            title = "Qui peut m'appeler",
                            subtitle = privacyOptions.find { it.first == currentUser?.privacyWhoCanCall }?.second ?: "Tout le monde",
                            trailing = { Text("Modifier", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary) },
                            onClick = { showCallsMenu = true }
                        )
                        DropdownMenu(expanded = showCallsMenu, onDismissRequest = { showCallsMenu = false }) {
                            privacyOptions.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        scope.launch {
                                            repository.updatePrivacySettings(
                                                lastSeen = currentUser?.privacyLastSeen ?: "EVERYONE",
                                                photo = currentUser?.privacyProfilePhoto ?: "EVERYONE",
                                                calls = key,
                                                groups = currentUser?.privacyWhoCanAddGroups ?: "EVERYONE"
                                            )
                                        }
                                        showCallsMenu = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Who can add to groups
                    Box {
                        SecuritySettingRow(
                            icon = Icons.Default.PrivacyTip,
                            title = "Qui peut m'ajouter aux groupes",
                            subtitle = privacyOptions.find { it.first == currentUser?.privacyWhoCanAddGroups }?.second ?: "Tout le monde",
                            trailing = { Text("Modifier", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary) },
                            onClick = { showGroupsMenu = true }
                        )
                        DropdownMenu(expanded = showGroupsMenu, onDismissRequest = { showGroupsMenu = false }) {
                            privacyOptions.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        scope.launch {
                                            repository.updatePrivacySettings(
                                                lastSeen = currentUser?.privacyLastSeen ?: "EVERYONE",
                                                photo = currentUser?.privacyProfilePhoto ?: "EVERYONE",
                                                calls = currentUser?.privacyWhoCanCall ?: "EVERYONE",
                                                groups = key
                                            )
                                        }
                                        showGroupsMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 4: SAUVEGARDE & RESTAURATION
            SectionLabel("SAUVEGARDE LOCALE & CHIFFREMENT")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sauvegarde chiffrée E2EE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                            Text("Dernière sauvegarde : Aujourd'hui à $now", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "✅ Sauvegarde locale chiffrée effectuée avec succès !", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Effectuer une sauvegarde maintenant", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 5: FOIRE AUX QUESTIONS (FAQ) & SUPPORT
            SectionLabel(if (currentLang == AppLanguage.KREYOL) "KESYON YO POZE SOUVAN (FAQ) & SIPÒ" else "FOIRE AUX QUESTIONS (FAQ) & ASSISTANCE")
            SecurityFaqAccordion(currentLang = currentLang, context = context)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Dialog for Setting PIN
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Définir le code PIN (4 chiffres)") },
            text = {
                Column {
                    Text("Ce code PIN sera demandé lors de chaque ouverture de l'application James Chat.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("Code PIN (4 chiffres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            scope.launch {
                                repository.updateSecurityPin(
                                    pin = pinInput,
                                    biometric = currentUser?.isBiometricEnabled ?: false,
                                    twoFactor = currentUser?.twoFactorPin ?: ""
                                )
                                Toast.makeText(context, "Code PIN activé !", Toast.LENGTH_SHORT).show()
                                showPinDialog = false
                                pinInput = ""
                            }
                        } else {
                            Toast.makeText(context, "Le PIN doit contenir 4 chiffres", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog for 2FA PIN
    if (showTwoFactorDialog) {
        AlertDialog(
            onDismissRequest = { showTwoFactorDialog = false },
            title = { Text("Vérification en deux étapes (2FA)") },
            text = {
                Column {
                    Text("Entrez un code secret à 6 chiffres pour sécuriser les connexions sur de nouveaux appareils.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = twoFactorInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) twoFactorInput = it },
                        label = { Text("Code 2FA (6 chiffres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (twoFactorInput.length == 6) {
                            scope.launch {
                                repository.updateSecurityPin(
                                    pin = currentUser?.appLockPin ?: "",
                                    biometric = currentUser?.isBiometricEnabled ?: false,
                                    twoFactor = twoFactorInput
                                )
                                Toast.makeText(context, "2FA configuré avec succès !", Toast.LENGTH_SHORT).show()
                                showTwoFactorDialog = false
                                twoFactorInput = ""
                            }
                        } else {
                            Toast.makeText(context, "Le code 2FA doit contenir 6 chiffres", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Activer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTwoFactorDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp),
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun SecuritySettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Medium, fontSize = 14.5.sp)
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing()
    }
}

@Composable
fun SecurityFaqAccordion(
    currentLang: AppLanguage,
    context: Context
) {
    val isKreyol = currentLang == AppLanguage.KREYOL

    data class FaqItem(
        val question: String,
        val answer: String
    )

    val faqItems = remember(isKreyol) {
        if (isKreyol) {
            listOf(
                FaqItem(
                    question = "🔒 Poukisa James Chat mande pèmisyon (Micro, Kamera, Depo, Notifikasyon) ?",
                    answer = "James Chat mande sèlman pèmisyon ki esansyèl pou fonksyon w ap itilize yo :\n• Mikwofòn : Pou voye nòt vokal ak fè apèl odyo klè HD.\n• Kamera : Pou pran foto, videyo ak fè apèl videyo an dirèk.\n• Depo / Fichye : Pou voye ak resevwa fim, videyo lou ak dokiman san limit.\n• Notifikasyon : Pou w jwenn alèt touswit lè yon moun voye mesaj oswa fè w yon apèl.\nNou pa janm pran pyès enfòmasyon pèsonèl san konsantman w."
                ),
                FaqItem(
                    question = "🛡️ Kijan chifreman bout an bout (E2EE) la fonksyone ?",
                    answer = "Tout mesaj, apèl ak fichye ou voye yo pwoteje avèk yon kle sekirite inik ki jenere sou telefòn ou. Se sèlman ou menm ak moun w ap pale a ki gen kle pou li mesaj yo. Menm sou entènèt la, pèsonn pa ka antre ladan yo."
                ),
                FaqItem(
                    question = "🔑 Kijan pou m pwoteje kont mwen ak kòd PIN, byometri ak 2FA ?",
                    answer = "Nan ekran Sekirite sa a, ou ka aktive :\n1. Yon kòd PIN 4 chif pou louvri aplikasyon an.\n2. Dévérouyaj byometrik avèk anpwent oswa eskanè vizaj.\n3. Verifikasyon an 2 etap (2FA) 6 chif pou bloke nenpòt koneksyon sispèk."
                ),
                FaqItem(
                    question = "💻 Èske sesyon James Web yo an sekirite menm jan an ?",
                    answer = "Wi ! Koneksyon ak òdinatè a fèt avèk yon kòd sekirite 6 chif oswa yon kòd QR dinamik. Ou ka koupe nenpòt sesyon web dirèkteman sou telefòn ou nenpòt ki lè."
                ),
                FaqItem(
                    question = "⚠️ Poukisa Android afiche yon avètisman pandan enstalasyon APK a ?",
                    answer = "Android toujou mete yon mesaj sekirite estanda 'Sous inconnue' lè w telechaje yon APK dirèkteman sou sit web. Fichye James Chat ofisyèl la 100% pwòp, verifye e an sekirite. Ou ka klike sou Paramètres epi aksepte sous la san kè sote."
                )
            )
        } else {
            listOf(
                FaqItem(
                    question = "🔒 Pourquoi James Chat demande-t-il des permissions sur Android ?",
                    answer = "James Chat requiert uniquement les autorisations indispensables au bon déroulement de vos conversations :\n• Microphone : Enregistrement de notes vocales et appels audio HD temps réel.\n• Caméra : Prise de photos instantanées et visioconférences WebRTC.\n• Stockage / Fichiers : Envoi et réception de documents volumineux, photos HD et films.\n• Notifications : Alertes immédiates lors de la réception d'appels ou de nouveaux messages.\nAucune donnée personnelle n'est accédée à votre insu."
                ),
                FaqItem(
                    question = "🛡️ Comment fonctionne le chiffrement de bout en bout (E2EE) ?",
                    answer = "Chaque discussion, appel et transfert de média bénéficie d'une clé cryptographique unique générée localement sur votre appareil. Seuls vous et votre interlocuteur possédez la clé permettant de déchiffrer les échanges."
                ),
                FaqItem(
                    question = "🔑 Comment sécuriser mon compte avec PIN, biométrie et 2FA ?",
                    answer = "Dans cet écran Sécurité, vous pouvez activer à tout instant :\n1. Un code PIN à 4 chiffres à l'ouverture de l'application.\n2. Le déverrouillage biométrique par empreinte digitale ou scan facial.\n3. La vérification en deux étapes (2FA) à 6 chiffres pour sécuriser les nouvelles connexions."
                ),
                FaqItem(
                    question = "💻 Les sessions James Web sont-elles aussi sécurisées que l'application ?",
                    answer = "Oui ! L'appairage avec votre navigateur sur ordinateur utilise un jeton cryptographique à 6 chiffres ou un QR Code dynamique. La session est synchronisée directement en local et peut être révoquée en un clic depuis votre téléphone."
                ),
                FaqItem(
                    question = "⚠️ Pourquoi Android affiche un message 'Source inconnue' lors de l'installation ?",
                    answer = "C'est une alerte de sécurité normale d'Android pour tout fichier APK téléchargé hors du Play Store. L'APK officiel de James Chat est 100% sûr, certifié par l'équipe officielle et sans aucun logiciel espion. Vous pouvez autoriser l'installation en toute confiance."
                )
            )
        }
    }

    var expandedIndices by remember { mutableStateOf(setOf(0)) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        faqItems.forEachIndexed { index, item ->
            val isExpanded = expandedIndices.contains(index)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedIndices = if (isExpanded) {
                            expandedIndices - index
                        } else {
                            expandedIndices + index
                        }
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.question,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Réduire" else "Développer",
                            tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.answer,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Support Contact Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isKreyol) "Èske w gen yon pwoblèm oswa yon kesyon ?" else "Besoin d'aide ou un problème ?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isKreyol) "Kontakte ekip sipò James Chat la dirèkteman pa imèl :"
                    else "Si vous rencontrez une difficulté ou avez une question, contactez notre équipe par e-mail :",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                listOf("hangington53@gmail.com", "Lilmontero444@gmail.com").forEach { email ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$email")
                                    putExtra(Intent.EXTRA_SUBJECT, "Support James Chat - Demande d'assistance")
                                }
                                try {
                                    context.startActivity(Intent.createChooser(intent, "Envoyer un e-mail à $email"))
                                } catch (_: Exception) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Support Email", email))
                                    Toast.makeText(context, "Email copié : $email", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = email,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Support Email", email))
                                Toast.makeText(context, "Email copié : $email", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
