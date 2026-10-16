package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JamesBluePrimary

/**
 * Composant officiel James Chat pour le logo animé.
 * - Apparition progressive (fade-in)
 * - Léger zoom élégant à l'apparition
 * - Halo lumineux discret (luminous glow halo)
 * - Balayage de lumière / shimmer fluide sans surcharge CPU
 * - Texte de marque "JAMES CHAT" avec transition harmonieuse
 * - Adaptable pour téléphone, tablette et grand écran
 */
@Composable
fun JamesOfficialLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showBrandText: Boolean = true,
    brandTextSize: TextUnit = 18.sp,
    textColor: Color? = null,
    isVertical: Boolean = false,
    subtitle: String? = null,
    animated: Boolean = true,
    withHalo: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    // Initial entrance animations
    val entranceAlpha = remember { Animatable(if (animated) 0f else 1f) }
    val entranceScale = remember { Animatable(if (animated) 0.85f else 1f) }
    val textAlpha = remember { Animatable(if (animated) 0f else 1f) }

    LaunchedEffect(animated) {
        if (animated) {
            // Step 1: Logo fade-in & smooth zoom
            entranceAlpha.animateTo(1f, animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing))
            entranceScale.animateTo(1f, animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing))
            // Step 2: Brand title smooth fade-in
            textAlpha.animateTo(1f, animationSpec = tween(durationMillis = 400, easing = LinearEasing))
        }
    }

    // Discreet continuous luminous halo shimmer effect (lightweight)
    val infiniteTransition = rememberInfiniteTransition(label = "logo_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_sweep"
    )

    val effectiveTextColor = textColor ?: MaterialTheme.colorScheme.primary

    val logoContent = @Composable {
        Box(
            modifier = Modifier
                .size(size)
                .scale(entranceScale.value)
                .alpha(entranceAlpha.value),
            contentAlignment = Alignment.Center
        ) {
            // Discreet ambient radiant halo
            if (withHalo) {
                Box(
                    modifier = Modifier
                        .size(size * 1.25f)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.28f),
                                    Color(0xFF8B5CF6).copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Circular container with subtle elevation border
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1D4ED8),
                                Color(0xFF2563EB),
                                Color(0xFF3B82F6)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Official Vector Drawable Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_james_chat_logo),
                    contentDescription = "Logo officiel James Chat",
                    modifier = Modifier
                        .size(size * 0.92f)
                        .padding(size * 0.04f)
                )

                // Discreet light beam sweep halo (shimmer highlight)
                if (animated) {
                    Box(
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.22f),
                                        Color(0xFF00E5FF).copy(alpha = 0.35f),
                                        Color.Transparent
                                    ),
                                    startX = (shimmerOffset * size.value * 2),
                                    endX = ((shimmerOffset + 0.6f) * size.value * 2)
                                )
                            )
                    )
                }
            }
        }
    }

    val textContent = @Composable {
        if (showBrandText) {
            Column(
                modifier = Modifier.alpha(textAlpha.value),
                horizontalAlignment = if (isVertical) Alignment.CenterHorizontally else Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "JAMES CHAT",
                        fontWeight = FontWeight.Black,
                        fontSize = brandTextSize,
                        letterSpacing = 1.sp,
                        color = effectiveTextColor,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🇭🇹",
                        fontSize = (brandTextSize.value * 0.9f).sp
                    )
                }
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = (brandTextSize.value * 0.62f).coerceAtLeast(10f).sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }

    val combinedModifier = modifier
        .testTag("james_official_logo")
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    if (isVertical) {
        Column(
            modifier = combinedModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            logoContent()
            if (showBrandText) {
                Spacer(modifier = Modifier.height(10.dp))
                textContent()
            }
        }
    } else {
        Row(
            modifier = combinedModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            logoContent()
            if (showBrandText) {
                Spacer(modifier = Modifier.width(10.dp))
                textContent()
            }
        }
    }
}

/**
 * Logo officiel de James Bot :
 * - Symbole J stylisé
 * - Assistant intelligent moderne
 * - Nuances de bleu (#2563EB) et violet (#7C3AED / #9333EA)
 * - Halo lumineux futuriste
 */
@Composable
fun JamesBotLogo(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    showLabel: Boolean = false,
    labelSize: TextUnit = 16.sp,
    withHalo: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "james_bot_anim")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bot_shimmer"
    )

    val content = @Composable {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            if (withHalo) {
                Box(
                    modifier = Modifier
                        .size(size * 1.25f)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF8B5CF6).copy(alpha = 0.35f),
                                    Color(0xFF3B82F6).copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E1B4B),
                                Color(0xFF4338CA),
                                Color(0xFF7C3AED)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_james_bot_logo),
                    contentDescription = "Logo James Bot",
                    modifier = Modifier
                        .size(size * 0.90f)
                        .padding(size * 0.05f)
                )

                // Shimmer glow
                Box(
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFC084FC).copy(alpha = 0.35f),
                                    Color(0xFF67E8F9).copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                startX = (shimmerOffset * size.value * 2),
                                endX = ((shimmerOffset + 0.6f) * size.value * 2)
                            )
                        )
                )
            }
        }
    }

    val boxModifier = modifier
        .testTag("james_bot_logo")
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    if (showLabel) {
        Row(
            modifier = boxModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "James Bot",
                        fontWeight = FontWeight.Bold,
                        fontSize = labelSize,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF7C3AED))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "OFFICIEL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = "Assistant IA & Support",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Box(modifier = boxModifier) {
            content()
        }
    }
}
