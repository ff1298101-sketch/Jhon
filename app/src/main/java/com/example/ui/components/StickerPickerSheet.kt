package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StickerItem
import com.example.data.repository.JamesChatRepository
import kotlinx.coroutines.launch

val JAMES_STICKER_CATALOG = listOf(
    StickerItem("stk_ht_1", "Haïti", "Drapeau Haïti", "🇭🇹", listOf("haiti", "drapeau", "fierte", "ayiti")),
    StickerItem("stk_ht_2", "Haïti", "Sak Pase", "🌴 Sak Pase!", listOf("sak pase", "salut", "bonjour", "kreyol")),
    StickerItem("stk_ht_3", "Haïti", "N'ap Boule", "🔥 N'ap Boule", listOf("nap boule", "forme", "energie", "kreyol")),
    StickerItem("stk_ht_4", "Haïti", "Ayiti Cheri", "❤️ Ayiti Cheri", listOf("ayiti", "cheri", "peyi", "lanmou")),
    StickerItem("stk_ht_5", "Haïti", "Bèl Bagay", "✨ Bèl Bagay", listOf("bel bagay", "top", "bravo", "super")),
    StickerItem("stk_ht_6", "Haïti", "Kenbe Fèm", "💪 Kenbe Fèm", listOf("kouraj", "kenbe", "fo", "force")),
    StickerItem("stk_joy_1", "Joyeux", "Super Sourire", "😄", listOf("sourire", "content", "happy", "joie")),
    StickerItem("stk_joy_2", "Joyeux", "MDR Rire", "🤣", listOf("mdr", "rire", "lol", "drole")),
    StickerItem("stk_joy_3", "Joyeux", "Yeux Étoilés", "🤩", listOf("etoiles", "fan", "admire", "wow")),
    StickerItem("stk_joy_4", "Joyeux", "Clin d'œil", "😉", listOf("clin", "wink", "daccord", "ok")),
    StickerItem("stk_joy_5", "Joyeux", "Cool Lunettes", "😎", listOf("cool", "swag", "style", "classe")),
    StickerItem("stk_joy_6", "Joyeux", "Génie", "🥳", listOf("fete", "youpi", "hourra", "joie")),
    StickerItem("stk_love_1", "Amour", "Cœur Étincelant", "💖", listOf("coeur", "amour", "love", "lanmou")),
    StickerItem("stk_love_2", "Amour", "Cœur En Flamme", "❤️‍🔥", listOf("feu", "flamme", "passion", "chaud")),
    StickerItem("stk_love_3", "Amour", "Gros Câlin", "🤗", listOf("calin", "hug", "tendresse", "reconfort")),
    StickerItem("stk_love_4", "Amour", "Bisou Volant", "😘", listOf("bisou", "kiss", "baiser", "amitie")),
    StickerItem("stk_love_5", "Amour", "Mains Cœur", "🫶", listOf("mains", "respect", "love", "merci")),
    StickerItem("stk_cel_1", "Célébration", "Trophée #1", "🏆", listOf("trophee", "victoire", "champion", "gagnant")),
    StickerItem("stk_cel_2", "Célébration", "Fusée En Vol", "🚀", listOf("fusee", "succes", "decollage", "haut")),
    StickerItem("stk_cel_3", "Célébration", "Feux d'Artifice", "🎆", listOf("feux", "magie", "nuit", "fete")),
    StickerItem("stk_cel_4", "Célébration", "Applaudissements", "👏", listOf("bravo", "felicitations", "top")),
    StickerItem("stk_cel_5", "Célébration", "100 Pourcent", "💯", listOf("parfait", "accord", "top", "100")),
    StickerItem("stk_sal_1", "Salutations", "Coucou La Main", "👋", listOf("bonjour", "salut", "coucou", "hello")),
    StickerItem("stk_sal_2", "Salutations", "Pause Café", "☕", listOf("cafe", "pause", "detente", "matin")),
    StickerItem("stk_sal_3", "Salutations", "Check Poing", "👊", listOf("check", "bro", "frere", "force")),
    StickerItem("stk_sal_4", "Salutations", "Bonne Nuit", "🌙", listOf("nuit", "sommeil", "dodo", "etoiles"))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerPickerSheet(
    repository: JamesChatRepository,
    onDismiss: () -> Unit,
    onSelectSticker: (StickerItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    val favorites by repository.getStickerFavoritesFlow().collectAsState(initial = emptyList())
    val recents by repository.getStickerRecentsFlow().collectAsState(initial = emptyList())
    val favoriteStickerIds = remember(favorites) { favorites.map { it.stickerId }.toSet() }
    val recentStickerIds = remember(recents) { recents.map { it.stickerId } }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tous") }
    val categories = listOf("Tous", "⭐ Favoris", "🕒 Récents", "Haïti", "Joyeux", "Amour", "Célébration", "Salutations")

    val displayedStickers = remember(searchQuery, selectedCategory, favoriteStickerIds, recentStickerIds) {
        val baseList = when (selectedCategory) {
            "⭐ Favoris" -> JAMES_STICKER_CATALOG.filter { favoriteStickerIds.contains(it.id) }
            "🕒 Récents" -> {
                val map = JAMES_STICKER_CATALOG.associateBy { it.id }
                recentStickerIds.mapNotNull { map[it] }
            }
            "Tous" -> JAMES_STICKER_CATALOG
            else -> JAMES_STICKER_CATALOG.filter { it.category == selectedCategory }
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            val q = searchQuery.lowercase().trim()
            baseList.filter {
                it.name.lowercase().contains(q) || it.keywords.any { k -> k.contains(q) }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✨ Stickers James Chat",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sticker_search_field"),
                placeholder = { Text("Rechercher un sticker (ex: Haïti, rire, cœur...)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 13.sp) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (displayedStickers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedCategory == "⭐ Favoris") "Aucun sticker favori pour le moment" else "Aucun sticker trouvé",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 88.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(displayedStickers) { sticker ->
                        val isFav = favoriteStickerIds.contains(sticker.id)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { onSelectSticker(sticker) }
                                .padding(8.dp)
                                .testTag("sticker_item_${sticker.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = sticker.symbol,
                                    fontSize = 28.sp,
                                    lineHeight = 32.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sticker.name,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .clickable {
                                        scope.launch { repository.toggleStickerFavorite(sticker.id) }
                                    }
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Favori",
                                    tint = if (isFav) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
