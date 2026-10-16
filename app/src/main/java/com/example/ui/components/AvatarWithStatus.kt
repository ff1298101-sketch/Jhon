package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.JamesBluePrimary
import com.example.ui.theme.JamesOnlineGreen

@Composable
fun AvatarWithStatus(
    name: String,
    avatarUrl: String = "",
    hasProfileVideo: Boolean = false,
    isOnline: Boolean = false,
    size: Dp = 48.dp,
    showOnlineBadge: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        val isJamesBot = name.equals("James Bot", ignoreCase = true) ||
                name.contains("James Bot", ignoreCase = true) ||
                avatarUrl == "james_bot_official"

        if (isJamesBot) {
            JamesBotLogo(
                size = size,
                withHalo = true
            )
        } else if (avatarUrl.isNotEmpty()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "J"
            val colorHash = name.hashCode().let { if (it < 0) -it else it }
            val bgColors = listOf(
                Color(0xFF2563EB), Color(0xFF7C3AED), Color(0xFF0D9488),
                Color(0xFFEA580C), Color(0xFFDB2777), Color(0xFF0284C7)
            )
            val bgColor = bgColors[colorHash % bgColors.size]
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.42f).sp
                )
            }
        }
        if (hasProfileVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(size * 0.36f)
                    .clip(CircleShape)
                    .background(JamesBluePrimary)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Vidéo de profil",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.22f)
                )
            }
        }
        if (showOnlineBadge && isOnline) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.28f)
                    .clip(CircleShape)
                    .background(JamesOnlineGreen)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}
