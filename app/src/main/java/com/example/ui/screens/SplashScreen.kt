package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JamesBluePrimary
import com.example.util.JamesSoundPlayer
import kotlinx.coroutines.delay

/**
 * Animated Brand Splash Screen for James Chat
 * Colors: Deep Black (#0B0F1A), Primary Blue (#007BFF), Cyan (#00E5FF), Purple (#8B5CF6)
 * Stages:
 * 1. Background dark appearance
 * 2. Logo progressive entrance with scale & alpha
 * 3. Glow radial halo
 * 4. Brand Name: "JAMES CHAT"
 * 5. Slogan: "Connecte les gens, rapproche les cœurs."
 * 6. Signature: "By James Designer"
 * Plays synthesized Jingle and smoothly transitions to main application.
 */
@Composable
fun JamesAnimatedSplashScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.6f) }
    val glowAlpha = remember { Animatable(0f) }
    val beamSweep = remember { Animatable(-1f) }
    val titleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val signatureAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1 & 2: Logo emergence + Soft Jingle
        JamesSoundPlayer.playSound(context, JamesSoundPlayer.SoundType.STARTUP_JINGLE)
        logoAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        logoScale.animateTo(1f, tween(600, easing = FastOutSlowInEasing))

        // Step 3: Subtle cyan/violet radiant glow + luminous beam sweep traversing logo
        glowAlpha.animateTo(0.8f, tween(400))
        beamSweep.animateTo(2f, tween(900, easing = LinearEasing))

        // Step 4: Brand Name
        titleAlpha.animateTo(1f, tween(400))

        // Step 5: Slogan
        taglineAlpha.animateTo(1f, tween(400))

        // Step 6: Signature
        signatureAlpha.animateTo(1f, tween(300))

        // Hold briefly then transition (total ~2.4 seconds)
        delay(600)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F1A))
            .testTag("james_animated_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Radiant Glow in Background
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(logoScale.value * 1.2f)
                .alpha(glowAlpha.value * 0.35f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF), // Cyan
                            Color(0xFF8B5CF6), // Purple
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Main Logo with Scale, Alpha & Shimmer Beam Sweep
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_james_chat_logo),
                    contentDescription = "Logo James Chat",
                    modifier = Modifier.size(110.dp)
                )
                // Luminous beam crossing the logo
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.28f),
                                    Color(0xFF00E5FF).copy(alpha = 0.45f),
                                    Color.Transparent
                                ),
                                startX = beamSweep.value * 220f,
                                endX = (beamSweep.value + 0.6f) * 220f
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Title
            Text(
                text = "JAMES CHAT",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = Color.White,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slogan
            Text(
                text = "Connecte les gens, rapproche les cœurs.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF00E5FF), // Cyan official accent
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Final Signature
            Text(
                text = "By James Designer",
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.alpha(signatureAlpha.value)
            )
        }

        // Accessibility Skip Button
        TextButton(
            onClick = onFinished,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Passer", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
    }
}
