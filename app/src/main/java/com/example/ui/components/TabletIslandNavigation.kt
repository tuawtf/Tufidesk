package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.example.R
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
    colorPalette: String = "DYNAMIC",
    modifier: Modifier = Modifier
) {
    // Floating Island Navigation Card (Material 3 Sidebar: surfaceContainerLow + outlineVariant border)
    Card(
        modifier = modifier
            .fillMaxHeight()
            .width(230.dp)
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 6.dp)
            .testTag("tablet_island_navigation"),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 14.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section: App Brand & Navigation
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Island Header (Clean M3 Tonal Branding - İllüstrasyon veya çizim olmadan sade)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    if (colorPalette == "TWILIGHT") {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "TWILIGHT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
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
                        Text(
                            text = "TUFI DESK",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Modern alt çizgi (<hr>) - kenarlarla birleşmeyen şık çizgi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .height(1.5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                    // Araçlar Butonu (Geri açma/kapama oku ile)
                    val isToolsActive = !isSettingsOpen && (currentRoute == Screen.Tools.route || currentRoute == Screen.Notes.route || currentRoute == Screen.Exams.route || currentRoute == Screen.Timer.route)

                    Column(modifier = Modifier.fillMaxWidth()) {
                        TabletNavItem(
                            icon = if (isToolsActive) Icons.Filled.GridView else Icons.Outlined.GridView,
                            label = "Araçlar",
                            isSelected = isToolsActive,
                            trailingIcon = if (isToolsMenuOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            onClick = { isToolsMenuOpen = !isToolsMenuOpen },
                            testTag = "tablet_nav_item_tools"
                        )

                        // Panel üzerinde alt başlık gibi alta doğru açılan menüler (1 satır arayla)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isToolsMenuOpen,
                            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, top = 8.dp, bottom = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isNotesActive = isToolsActive && activeTool == "notes"
                                val isExamsActive = isToolsActive && activeTool == "exams"
                                val isTimerActive = isToolsActive && activeTool == "timer"

                                TabletSubNavItem(
                                    icon = Icons.Default.Description,
                                    label = "Notlar",
                                    isSelected = isNotesActive,
                                    onClick = { onSelectTool("notes") },
                                    testTag = "tablet_menu_item_notes"
                                )

                                TabletSubNavItem(
                                    icon = Icons.Default.Assessment,
                                    label = "Denemeler",
                                    isSelected = isExamsActive,
                                    onClick = { onSelectTool("exams") },
                                    testTag = "tablet_menu_item_exams"
                                )

                                TabletSubNavItem(
                                    icon = Icons.Default.HourglassTop,
                                    label = "Sayaç & Odak",
                                    isSelected = isTimerActive,
                                    onClick = { onSelectTool("timer") },
                                    testTag = "tablet_menu_item_timer"
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Island: Ayarlar Butonu Üstte, Profil En Altta (Bulut eşitlendi kaldırıldı)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tablet Paneli: Ayarlar Butonu (Profilin üstünde)
                TabletNavItem(
                    icon = if (isSettingsOpen) Icons.Filled.Settings else Icons.Outlined.Settings,
                    label = "Ayarlar",
                    isSelected = isSettingsOpen,
                    onClick = onSettingsClick,
                    testTag = "tablet_nav_item_settings"
                )

                // User Profile & Switch (En altta: surface-container zemininde 16px köşelerle)
                if (currentUser != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSwitchProfile() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                UserFlowerAvatar(
                                    user = currentUser,
                                    size = 32.dp,
                                    shape = CircleShape
                                )
                                Spacer(modifier = Modifier.width(10.dp))
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
    // M3 Hap (Pill) Şekli: Seçili ise primary (#F03A7E) & on-primary (#FFFFFF), pasif ise şeffaf & on-surface-variant (#B3ADC2)
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "nav_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "nav_content"
    )

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
                    tint = contentColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun TabletSubNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "subnav_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "subnav_content"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.5.sp
                ),
                color = contentColor
            )
        }
    }
}

