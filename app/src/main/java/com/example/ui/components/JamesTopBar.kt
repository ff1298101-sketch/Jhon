package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.UserEntity
import com.example.ui.theme.JamesBluePrimary
import com.example.ui.theme.JamesWarningAmber
import com.example.util.JamesI18n

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JamesTopBar(
    currentUser: UserEntity?,
    isOnline: Boolean,
    currentLang: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenChannels: () -> Unit = {},
    onOpenWeb: () -> Unit = {},
    onOpenGames: () -> Unit = {},
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    JamesOfficialLogo(
                        size = 36.dp,
                        isVertical = false,
                        showBrandText = false,
                        animated = true,
                        withHalo = true,
                        onClick = onOpenProfile
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "JAMES CHAT",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "🇭🇹", fontSize = 16.sp)
                        }
                        if (currentUser != null && currentUser.username.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "@${currentUser.username}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (currentUser.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    JamesVBadge(size = 12.dp)
                                }
                            }
                        }
                    }
                }
            },
            actions = {
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier.testTag("top_bar_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = JamesI18n.t("search", currentLang)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showLanguageMenu = true },
                        modifier = Modifier.testTag("top_bar_lang_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = currentLang.flag, fontSize = 16.sp)
                        }
                    }
                    if (showLanguageMenu) {
                        JamesLanguagePickerDialog(
                            currentLanguage = currentLang,
                            onLanguageSelected = onLanguageChange,
                            onDismiss = { showLanguageMenu = false }
                        )
                    }
                }

                if (isAdmin) {
                    IconButton(
                        onClick = onOpenAdmin,
                        modifier = Modifier.testTag("top_bar_admin_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = JamesI18n.t("tab_admin", currentLang),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showOptionsMenu = true },
                        modifier = Modifier.testTag("top_bar_more_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }
                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("🎓 James Academy (232 Matières & Certificats)") },
                            onClick = {
                                showOptionsMenu = false
                                onOpenGames()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🌐 James Web (avec catégorie Jeux éducatifs James Academy)") },
                            onClick = {
                                showOptionsMenu = false
                                onOpenWeb()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🎮 Jeux Éducatifs (40 Jeux interactifs)") },
                            onClick = {
                                showOptionsMenu = false
                                onOpenGames()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("📢 Chaînes James") },
                            onClick = {
                                showOptionsMenu = false
                                onOpenChannels()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(JamesI18n.t("tab_profile", currentLang)) },
                            onClick = {
                                showOptionsMenu = false
                                onOpenProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(JamesI18n.t("tab_settings", currentLang)) },
                            onClick = {
                                showOptionsMenu = false
                                onOpenSettings()
                            }
                        )
                        if (isAdmin) {
                            DropdownMenuItem(
                                text = { Text("🛡️ ${JamesI18n.t("tab_admin", currentLang)}") },
                                onClick = {
                                    showOptionsMenu = false
                                    onOpenAdmin()
                                }
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (!isOnline) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JamesWarningAmber)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "Hors ligne",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = JamesI18n.t("offline_banner", currentLang),
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
