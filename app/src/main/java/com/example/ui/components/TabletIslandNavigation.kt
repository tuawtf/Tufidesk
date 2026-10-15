package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.Screen
import com.example.data.firebase.SyncState
import com.example.data.model.UserAccount
import com.example.data.model.UserRole

@Composable
fun TabletIslandNavigation(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onAddHomeworkClick: () -> Unit,
    navItems: List<Screen>,
    showAddAction: Boolean = true,
    currentUser: UserAccount? = null,
    isOnline: Boolean = true,
    syncState: SyncState = SyncState.IDLE,
    onSyncClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onSwitchProfile: () -> Unit = {},
    isSettingsOpen: Boolean = false,
    onSettingsClick: () -> Unit = {},
    activeTool: String? = null,
    onSelectTool: (String) -> Unit = {},
    currentRoute: String = Screen.Home.route,
    modifier: Modifier = Modifier
) {
    // Floating Island Navigation Card (Ada şeklinde sol menü)
    Card(
        modifier = modifier
            .fillMaxHeight()
            .width(230.dp)
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 6.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(26.dp))
            .testTag("tablet_island_navigation"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 14.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section: App Brand & "Yeni Ödev" Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Island Header (Minimalist Logo & Title)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TufiDESK",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tablet Modu",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showAddAction) {
                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action Button (Yeni Ödev)
                    Button(
                        onClick = onAddHomeworkClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("tablet_add_homework_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Yeni Ödev",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                var isToolsMenuOpen by remember { mutableStateOf(false) }

                // Navigation Items List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    navItems.forEachIndexed { index, screen ->
                        val isSelected = !isSettingsOpen && currentTab == index && currentRoute == screen.route
                        TabletNavItem(
                            icon = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            label = screen.title,
                            isSelected = isSelected,
                            onClick = {
                                isToolsMenuOpen = false
                                onTabSelected(index)
                            },
                            testTag = "tablet_nav_item_${screen.route}"
                        )
                    }

                    // Araçlar Açılır Menüsü (Tablet için doğrudan araçlar listesi)
                    val isToolsActive = !isSettingsOpen && (currentRoute == Screen.Tools.route || currentRoute == Screen.Notes.route || currentRoute == Screen.Exams.route || currentRoute == Screen.Timer.route)

                    Box(modifier = Modifier.fillMaxWidth()) {
                        TabletNavItem(
                            icon = if (isToolsActive) Icons.Filled.GridView else Icons.Outlined.GridView,
                            label = "Araçlar",
                            isSelected = isToolsActive,
                            trailingIcon = if (isToolsMenuOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            onClick = { isToolsMenuOpen = !isToolsMenuOpen },
                            testTag = "tablet_nav_item_tools"
                        )

                        DropdownMenu(
                            expanded = isToolsMenuOpen,
                            onDismissRequest = { isToolsMenuOpen = false },
                            modifier = Modifier
                                .width(202.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                        ) {
                            val isNotesActive = isToolsActive && activeTool == "notes"
                            val isExamsActive = isToolsActive && activeTool == "exams"
                            val isTimerActive = isToolsActive && activeTool == "timer"

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Notlar",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isNotesActive) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isNotesActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "Notlar",
                                        tint = if (isNotesActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    isToolsMenuOpen = false
                                    onSelectTool("notes")
                                },
                                modifier = Modifier.testTag("tablet_menu_item_notes")
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Deneme Sınavları",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isExamsActive) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isExamsActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = "Deneme Sınavları",
                                        tint = if (isExamsActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    isToolsMenuOpen = false
                                    onSelectTool("exams")
                                },
                                modifier = Modifier.testTag("tablet_menu_item_exams")
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Sayaç & Odak",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isTimerActive) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isTimerActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = "Sayaç & Odak",
                                        tint = if (isTimerActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    isToolsMenuOpen = false
                                    onSelectTool("timer")
                                },
                                modifier = Modifier.testTag("tablet_menu_item_timer")
                            )
                        }
                    }
                }
            }

            // Bottom Island: User Profile, Sync Status & Settings Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cloud Sync Pill (Clickable)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSyncClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val syncIcon = when {
                            !isOnline -> Icons.Default.CloudOff
                            syncState == SyncState.SYNCED -> Icons.Default.CloudDone
                            else -> Icons.Default.Sync
                        }
                        val syncColor = when {
                            !isOnline -> MaterialTheme.colorScheme.error
                            syncState == SyncState.SYNCED -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                        Icon(
                            imageVector = syncIcon,
                            contentDescription = null,
                            tint = syncColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                !isOnline -> "Çevrimdışı Mod"
                                syncState == SyncState.SYNCING -> "Eşitleniyor..."
                                syncState == SyncState.SYNCED -> "Bulut Eşitlendi"
                                else -> "Bulut Aktif"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // User Profile & Switch
                if (currentUser != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSwitchProfile() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                UserFlowerAvatar(
                                    user = currentUser,
                                    size = 30.dp,
                                    shape = CircleShape
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = currentUser.name,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Öğrenci",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Tablet Paneli En Altında Ayarlar Butonu
                TabletNavItem(
                    icon = if (isSettingsOpen) Icons.Filled.Settings else Icons.Outlined.Settings,
                    label = "Ayarlar",
                    isSelected = isSettingsOpen,
                    onClick = onSettingsClick,
                    testTag = "tablet_nav_item_settings"
                )
            }
        }
    }
}

@Composable
fun TabletNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    trailingIcon: ImageVector? = null
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
        label = "nav_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "nav_content"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
