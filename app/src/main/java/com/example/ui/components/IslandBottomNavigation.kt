package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Screen

/**
 * Sabit (Fixed) Standart Material 3 Alt Gezinme Çubuğu.
 * - Ekrana tam oturan, kenardan kenara standart alt çubuk.
 */
@Composable
fun FixedStandardBottomNavigation(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    navItems: List<Screen>,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fixed_bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp
    ) {
        navItems.forEachIndexed { index, screen ->
            val isSelected = currentTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * Modern Yüzen Ada (Floating Island Dock) Alt Gezinme Çubuğu.
 * - Butonlar birbirine yakın durur (spacedBy 6dp).
 * - Bir butona basıldığında yalnızca o buton yana doğru genişler ve ismi yanında yazar.
 * - Yanındakiler birbirine yakın kalmaya devam eder, kapsül ada boyutu animasyonla genişler.
 */
@Composable
fun IslandBottomNavigation(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    navItems: List<Screen>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            ),
            modifier = Modifier
                .wrapContentWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .testTag("bottom_nav_bar")
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, screen ->
                    val isSelected = currentTab == index
                    IslandBottomNavItem(
                        icon = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        label = screen.title,
                        isSelected = isSelected,
                        onClick = { onTabSelected(index) },
                        testTag = "nav_tab_${screen.route}"
                    )
                }
            }
        }
    }
}

/**
 * Dinamik olarak genişleyen ve daralan ada menü butonu.
 */
@Composable
fun IslandBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "island_item_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "island_item_content"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "island_icon_scale"
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = bgColor,
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = if (isSelected && label.isNotBlank()) 14.dp else 11.dp,
                    vertical = 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label.ifBlank { "Menü" },
                tint = contentColor,
                modifier = Modifier
                    .size(23.dp)
                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
            )

            // Seçildiğinde yana doğru pürüzsüzce genişleyip metni gösterir (metin boş değilse)
            AnimatedVisibility(
                visible = isSelected && label.isNotBlank(),
                enter = fadeIn(animationSpec = tween(180)) + expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = fadeOut(animationSpec = tween(80)) + shrinkHorizontally(
                    animationSpec = tween(120)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = contentColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
