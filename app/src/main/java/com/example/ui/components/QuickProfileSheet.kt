package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.UserEntity
import com.example.ui.theme.JamesErrorRed
import com.example.ui.theme.JamesOnlineGreen
import com.example.util.JamesI18n
import com.example.util.JamesIdentityUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickProfileSheet(
    targetUser: UserEntity,
    isContact: Boolean,
    isBlocked: Boolean,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSendMessage: (UserEntity) -> Unit,
    onStartCall: (UserEntity, Boolean) -> Unit,
    onToggleContact: (UserEntity) -> Unit,
    onToggleBlock: (UserEntity) -> Unit,
    onReport: (UserEntity) -> Unit,
    onWatchProfileVideo: (UserEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showQrDialog by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val fourDigit = JamesIdentityUtil.getFourDigitId(targetUser)
    val jamesTag = JamesIdentityUtil.getJamesTag(targetUser)
    val jamesCustomId = JamesIdentityUtil.getJamesCustomId(targetUser)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                AvatarWithStatus(
                    name = if (targetUser.firstName.isNotEmpty()) "${targetUser.firstName} ${targetUser.lastName}" else targetUser.username,
                    avatarUrl = targetUser.avatarUrl,
                    hasProfileVideo = targetUser.profileVideoUrl.isNotEmpty() && targetUser.showVideoOnProfile,
                    isOnline = targetUser.isOnline,
                    size = 96.dp,
                    onClick = {
                        if (targetUser.profileVideoUrl.isNotEmpty()) {
                            onWatchProfileVideo(targetUser)
                        }
                    }
                )
                if (targetUser.profileVideoUrl.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { onWatchProfileVideo(targetUser) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(top = 80.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Regarder la vidéo", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(JamesI18n.t("profile_video", lang), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (targetUser.firstName.isNotEmpty()) "${targetUser.firstName} ${targetUser.lastName}" else targetUser.username,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Primary Identifier Badge: JAMES#4827
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🏷️ $jamesTag",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { clipboard.setText(AnnotatedString(jamesTag)) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier Tag",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showQrDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "@${targetUser.username} • ID: $fourDigit • $jamesCustomId",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (targetUser.isOnline) JamesOnlineGreen else Color.Gray)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (targetUser.isOnline) JamesI18n.t("online", lang) else JamesI18n.t("offline", lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (targetUser.bio.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = targetUser.bio,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (targetUser.city.isNotEmpty() || targetUser.country.isNotEmpty()) {
                val location = listOf(targetUser.city, targetUser.country).filter { it.isNotEmpty() }.joinToString(", ")
                Text(
                    text = "📍 $location",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilledTonalButton(
                    onClick = {
                        onDismiss()
                        onSendMessage(targetUser)
                    },
                    modifier = Modifier.testTag("quick_sheet_message_btn")
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = "Message", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat")
                }
                FilledTonalButton(
                    onClick = {
                        onDismiss()
                        onStartCall(targetUser, false)
                    },
                    modifier = Modifier.testTag("quick_sheet_call_audio_btn")
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Appel audio", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audio")
                }
                FilledTonalButton(
                    onClick = {
                        onDismiss()
                        onStartCall(targetUser, true)
                    },
                    modifier = Modifier.testTag("quick_sheet_call_video_btn")
                ) {
                    Icon(imageVector = Icons.Default.Videocam, contentDescription = "Appel vidéo", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Vidéo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { onToggleContact(targetUser) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isContact) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                        contentDescription = "Contact",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isContact) "Retirer" else JamesI18n.t("add_contact", lang), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { onToggleBlock(targetUser) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isBlocked) MaterialTheme.colorScheme.primary else JamesErrorRed
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Block, contentDescription = "Bloquer", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBlocked) JamesI18n.t("unblock_user", lang) else JamesI18n.t("block_user", lang), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onReport(targetUser) },
                    modifier = Modifier.testTag("quick_sheet_report_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = JamesI18n.t("report_user", lang),
                        tint = JamesErrorRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showQrDialog) {
        val qrBitmap = remember(targetUser) {
            JamesIdentityUtil.generateUserQrCodeBitmap(targetUser, size = 280)
        }
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "QR Code de ${targetUser.firstName.ifEmpty { targetUser.username }}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = jamesTag,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
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
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code personnel",
                            modifier = Modifier
                                .size(220.dp)
                                .padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scannez ce QR Code pour contacter directement @${targetUser.username}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "ID: $fourDigit • $jamesCustomId",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val payload = JamesIdentityUtil.getQrPayload(targetUser)
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
}
