package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GroupEntity
import com.example.data.model.UserEntity
import com.example.data.repository.JamesChatRepository
import com.example.ui.theme.JamesBluePrimary
import com.example.ui.theme.JamesErrorRed
import com.example.ui.theme.JamesOnlineGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupManageSheet(
    group: GroupEntity,
    repository: JamesChatRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allUsers by repository.dao.getAllUsersFlow().collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState()

    val isAdmin = group.adminIds.split(",").contains(currentUser?.id) || group.creatorId == currentUser?.id
    val isCoAdmin = group.coAdminIds.split(",").contains(currentUser?.id)
    val hasAdminRights = isAdmin || isCoAdmin

    var onlyAdminsCanSend by remember { mutableStateOf(group.onlyAdminsCanSend) }
    var requireApproval by remember { mutableStateOf(group.requireApproval) }
    var descriptionText by remember { mutableStateOf(group.description) }
    var rulesText by remember { mutableStateOf(group.rules) }

    val membersList = remember(group.memberIds) {
        group.memberIds.split(",").filter { it.isNotEmpty() }
    }
    val coAdminsList = remember(group.coAdminIds) {
        group.coAdminIds.split(",").filter { it.isNotEmpty() }
    }
    val pendingList = remember(group.pendingMemberIds) {
        group.pendingMemberIds.split(",").filter { it.isNotEmpty() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = group.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = "${membersList.size} membres • Groupe James Chat V2",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Invite Link & QR
            val inviteLink = "https://jameschat.ht/join/${group.inviteCode}"
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lien d'invitation du groupe", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = inviteLink,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Invite Link", inviteLink))
                                Toast.makeText(context, "Lien copié dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copier le lien", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions section (If admin)
            if (hasAdminRights) {
                Text("PARAMÈTRES & MODÉRATION DU GROUPE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Seuls les admins peuvent écrire", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Bloque les messages des membres standard", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = onlyAdminsCanSend,
                                onCheckedChange = {
                                    onlyAdminsCanSend = it
                                    scope.launch {
                                        repository.updateGroupPolicy(group.id, it, requireApproval, rulesText, descriptionText)
                                    }
                                }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Approbation obligatoire des membres", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Les nouveaux membres doivent être approuvés", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = requireApproval,
                                onCheckedChange = {
                                    requireApproval = it
                                    scope.launch {
                                        repository.updateGroupPolicy(group.id, onlyAdminsCanSend, it, rulesText, descriptionText)
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Pending Approvals (if any)
            if (pendingList.isNotEmpty() && hasAdminRights) {
                Text("DEMANDES D'ADHÉSION EN ATTENTE (${pendingList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFD97706))
                Spacer(modifier = Modifier.height(8.dp))
                pendingList.forEach { pendingUserId ->
                    val user = allUsers.find { it.id == pendingUserId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = user?.username ?: pendingUserId, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Row {
                            IconButton(onClick = {
                                scope.launch {
                                    repository.approvePendingMember(group.id, pendingUserId)
                                    Toast.makeText(context, "Membre approuvé !", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Approuver", tint = JamesOnlineGreen)
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    repository.removeGroupMember(group.id, pendingUserId)
                                }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Rejeter", tint = JamesErrorRed)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Member list with Roles & Actions
            Text("MEMBRES DU GROUPE (${membersList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            membersList.forEach { memberId ->
                val memberUser = allUsers.find { it.id == memberId }
                val isCreator = memberId == group.creatorId
                val isMemberAdmin = group.adminIds.split(",").contains(memberId)
                val isMemberCoAdmin = coAdminsList.contains(memberId)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AvatarWithStatus(
                        name = memberUser?.username ?: memberId,
                        avatarUrl = memberUser?.avatarUrl ?: "",
                        size = 38.dp,
                        showOnlineBadge = memberUser?.isOnline == true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = memberUser?.username ?: memberId,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            if (isCreator) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("👑 Créateur", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            } else if (isMemberAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🛡️ Admin", fontSize = 10.sp, color = JamesOnlineGreen, fontWeight = FontWeight.Bold)
                            } else if (isMemberCoAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("⭐ Co-Admin", fontSize = 10.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (hasAdminRights && !isCreator && memberId != currentUser?.id) {
                        Row {
                            // Toggle Co-admin
                            IconButton(onClick = {
                                scope.launch {
                                    repository.toggleGroupCoAdmin(group.id, memberId)
                                }
                            }) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = "Co-Admin",
                                    tint = if (isMemberCoAdmin) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // Kick
                            IconButton(onClick = {
                                scope.launch {
                                    repository.removeGroupMember(group.id, memberId)
                                    Toast.makeText(context, "Membre retiré du groupe", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.PersonRemove, contentDescription = "Retirer", tint = JamesErrorRed)
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            }
        }
    }
}
