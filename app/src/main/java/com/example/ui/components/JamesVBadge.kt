package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VBadgeStyle
import com.example.ui.theme.JamesBluePrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Composant officiel JamesVBadge pour James Chat.
 * Prend en charge les 5 styles avec sélection des 2 styles principaux :
 * - STYLE 1 : V CLASSIQUE (Cercle bleu avec coche blanche)
 * - STYLE 2 : V SCEAU (Badge en forme de sceau étoilé/scallop bleu avec coche blanche au centre) [PRINCIPAL]
 * - STYLE 3 : V ROYAL (Bouclier bleu royal prestigieux avec coche blanche) [PRINCIPAL]
 * - STYLE 4 : V DIAMANT (Forme diamant bleue avec coche blanche)
 * - STYLE 5 : V HEXAGONE (Hexagone technologique avec coche blanche)
 *
 * Inclut un dialogue d'information sur clic expliquant la certification officielle de James Chat.
 */
@Composable
fun JamesVBadge(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    style: VBadgeStyle = VBadgeStyle.SEAL,
    badgeColor: Color = JamesBluePrimary,
    showTooltipOnClick: Boolean = true,
    userAccountName: String? = null
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    val baseModifier = modifier
        .size(size)
        .testTag("james_v_badge_${style.code.lowercase()}")
        .then(
            if (showTooltipOnClick) {
                Modifier.clickable { showInfoDialog = true }
            } else Modifier
        )

    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            // 1. Dessin de la forme géométrique selon le style choisi
            when (style) {
                VBadgeStyle.CLASSIC -> {
                    drawCircle(
                        color = badgeColor,
                        radius = w * 0.48f,
                        center = Offset(cx, cy)
                    )
                }
                VBadgeStyle.SEAL -> {
                    // Badge Sceau officiel (12 pointes douces)
                    val sealPath = Path()
                    val points = 12
                    val outerR = w * 0.48f
                    val innerR = w * 0.40f
                    val step = (2 * PI / (points * 2)).toFloat()
                    var angle = -PI.toFloat() / 2f

                    for (i in 0 until (points * 2)) {
                        val r = if (i % 2 == 0) outerR else innerR
                        val x = cx + r * cos(angle)
                        val y = cy + r * sin(angle)
                        if (i == 0) sealPath.moveTo(x, y) else sealPath.lineTo(x, y)
                        angle += step
                    }
                    sealPath.close()

                    drawPath(path = sealPath, color = badgeColor, style = Fill)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = innerR * 0.95f,
                        center = Offset(cx, cy),
                        style = Stroke(width = w * 0.04f)
                    )
                }
                VBadgeStyle.ROYAL -> {
                    // Badge Royal - Bouclier royal prestigieux
                    val shieldPath = Path().apply {
                        moveTo(cx, h * 0.04f)
                        lineTo(w * 0.92f, h * 0.16f)
                        quadraticTo(w * 0.94f, h * 0.60f, cx, h * 0.96f)
                        quadraticTo(w * 0.06f, h * 0.60f, w * 0.08f, h * 0.16f)
                        close()
                    }
                    drawPath(path = shieldPath, color = badgeColor, style = Fill)
                    // Liseré intérieur or discret ou blanc subtil
                    val innerShield = Path().apply {
                        moveTo(cx, h * 0.12f)
                        lineTo(w * 0.84f, h * 0.22f)
                        quadraticTo(w * 0.86f, h * 0.58f, cx, h * 0.88f)
                        quadraticTo(w * 0.14f, h * 0.58f, w * 0.16f, h * 0.22f)
                        close()
                    }
                    drawPath(
                        path = innerShield,
                        color = Color.White.copy(alpha = 0.30f),
                        style = Stroke(width = w * 0.04f)
                    )
                }
                VBadgeStyle.DIAMOND -> {
                    // Forme diamant losange élégante avec bordure interne
                    val diamondPath = Path().apply {
                        moveTo(cx, h * 0.04f)
                        lineTo(w * 0.94f, cy)
                        lineTo(cx, h * 0.96f)
                        lineTo(w * 0.06f, cy)
                        close()
                    }
                    drawPath(path = diamondPath, color = badgeColor, style = Fill)
                    val innerDiamond = Path().apply {
                        moveTo(cx, h * 0.14f)
                        lineTo(w * 0.84f, cy)
                        lineTo(cx, h * 0.86f)
                        lineTo(w * 0.16f, cy)
                        close()
                    }
                    drawPath(
                        path = innerDiamond,
                        color = Color.White.copy(alpha = 0.35f),
                        style = Stroke(width = w * 0.04f)
                    )
                }
                VBadgeStyle.HEXAGON -> {
                    // Hexagone moderne tech
                    val hexPath = Path()
                    val r = w * 0.48f
                    for (i in 0 until 6) {
                        val a = (i * 60f + 30f) * (PI.toFloat() / 180f)
                        val x = cx + r * cos(a)
                        val y = cy + r * sin(a)
                        if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
                    }
                    hexPath.close()
                    drawPath(path = hexPath, color = badgeColor, style = Fill)
                }
            }

            // 2. Coche blanche nette au centre (Checkmark ✓)
            val checkmarkPath = Path().apply {
                moveTo(w * 0.28f, h * 0.50f)
                lineTo(w * 0.44f, h * 0.68f)
                lineTo(w * 0.74f, h * 0.34f)
            }
            drawPath(
                path = checkmarkPath,
                color = Color.White,
                style = Stroke(
                    width = w * 0.14f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    JamesVBadge(
                        size = 24.dp,
                        style = style,
                        badgeColor = badgeColor,
                        showTooltipOnClick = false
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Compte Officiel Vérifié",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (!userAccountName.isNullOrBlank()) {
                            "Ce badge indique que le compte $userAccountName a été officiellement vérifié par l'équipe d'administration de James Chat."
                        } else {
                            "Ce badge indique que ce compte a été officiellement certifié et vérifié par l'équipe d'administration de James Chat."
                        },
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Style actif : ${style.title} (${style.description})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Seuls les créateurs, personnalités, communautés ou partenaires officiels et administrateurs autorisés disposent de cette distinction.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Compris", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun JamesTeamMemberBadge(
    modifier: Modifier = Modifier,
    text: String = "Membre James Chat",
    style: VBadgeStyle = VBadgeStyle.SEAL
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("team_member_badge"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JamesVBadge(size = 13.dp, style = style, showTooltipOnClick = false)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
