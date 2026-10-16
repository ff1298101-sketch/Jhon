package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.ui.theme.JamesBluePrimary
import com.example.util.JamesI18n

enum class MainTab {
    CHATS,
    GROUPS,
    STATUS,
    CALLS,
    CONTACTS
}

@Composable
fun JamesBottomNav(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    lang: AppLanguage,
    unreadChatsCount: Int = 0,
    unreadStatusCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("james_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // Discussions
        NavigationBarItem(
            selected = currentTab == MainTab.CHATS,
            onClick = { onTabSelected(MainTab.CHATS) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadChatsCount > 0) {
                            Badge(containerColor = JamesBluePrimary) {
                                Text("$unreadChatsCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == MainTab.CHATS) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                        contentDescription = JamesI18n.t("tab_chats", lang)
                    )
                }
            },
            label = { Text(JamesI18n.t("tab_chats", lang)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("tab_chats_btn")
        )
        // Groupes
        NavigationBarItem(
            selected = currentTab == MainTab.GROUPS,
            onClick = { onTabSelected(MainTab.GROUPS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.GROUPS) Icons.Filled.Group else Icons.Outlined.Group,
                    contentDescription = JamesI18n.t("tab_groups", lang)
                )
            },
            label = { Text(JamesI18n.t("tab_groups", lang)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("tab_groups_btn")
        )
        // Statuts
        NavigationBarItem(
            selected = currentTab == MainTab.STATUS,
            onClick = { onTabSelected(MainTab.STATUS) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadStatusCount > 0) {
                            Badge(containerColor = JamesBluePrimary) {
                                Text("$unreadStatusCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == MainTab.STATUS) Icons.Filled.DonutLarge else Icons.Outlined.DonutLarge,
                        contentDescription = JamesI18n.t("tab_status", lang)
                    )
                }
            },
            label = { Text(JamesI18n.t("tab_status", lang)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("tab_status_btn")
        )
        // Appels
        NavigationBarItem(
            selected = currentTab == MainTab.CALLS,
            onClick = { onTabSelected(MainTab.CALLS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.CALLS) Icons.Filled.Call else Icons.Outlined.Call,
                    contentDescription = JamesI18n.t("tab_calls", lang)
                )
            },
            label = { Text(JamesI18n.t("tab_calls", lang)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("tab_calls_btn")
        )
        // Contacts
        NavigationBarItem(
            selected = currentTab == MainTab.CONTACTS,
            onClick = { onTabSelected(MainTab.CONTACTS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.CONTACTS) Icons.Filled.People else Icons.Outlined.People,
                    contentDescription = JamesI18n.t("tab_contacts", lang)
                )
            },
            label = { Text(JamesI18n.t("tab_contacts", lang)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("tab_contacts_btn")
        )
    }
}
