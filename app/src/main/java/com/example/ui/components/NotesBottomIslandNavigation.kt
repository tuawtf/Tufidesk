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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteFolder
import com.example.ui.util.FolderIconHelper
import com.example.ui.util.FolderIconItem

/**
 * Google Notlar için Modern Yüzen Ada (Floating Island Dock) Alt Gezinme Çubuğu.
 * - Sırayla "Tümü" ve oluşturduğumuz klasörler yer alır.
 * - Klasörlere tek tıklayınca o klasör açılır.
 * - Klasöre uzunca tıklayınca Google Fonts / Material Symbols simge değiştirme ayarı çıkar.
 * - EN SAĞDA ise Araçlar menüsüne geçiş düğmesi yer alır.
 */
@Composable
fun NotesBottomIslandNavigation(
    folders: List<NoteFolder>,
    selectedFolderId: Long?,
    onSelectFolder: (Long?) -> Unit,
    onLongClickFolder: (NoteFolder) -> Unit,
    onBackToTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                .widthIn(max = 520.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .testTag("notes_bottom_nav_bar")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SOL KISIM: YATAY KAYDIRILABİLİR KLASÖRLER LİSTESİ ("Tümü" + Klasörler)
                Row(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. "TÜMÜ" / "NOTLAR" DÜĞMESİ
                    val isAllSelected = selectedFolderId == null
                    NotesIslandItem(
                        icon = Icons.Default.FolderOpen,
                        label = "Tümü",
                        isSelected = isAllSelected,
                        colorHex = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectFolder(null)
                        },
                        onLongClick = null,
                        testTag = "notes_tab_all"
                    )

                    // 2. KULLANICININ OLUŞTURDUĞU KLASÖRLER
                    folders.forEach { folder ->
                        val isSelected = selectedFolderId == folder.id
                        val icon = FolderIconHelper.getIcon(folder.iconName)

                        NotesIslandItem(
                            icon = icon,
                            label = folder.name,
                            isSelected = isSelected,
                            colorHex = folder.colorHex,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectFolder(if (isSelected) null else folder.id)
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongClickFolder(folder)
                            },
                            testTag = "notes_tab_folder_${folder.id}"
                        )
                    }
                }

                // EN SAĞDA ARAÇLAR DÜĞMESİ (Sadece telefon modunda gösterilir, tablet için gizlenir)
                val configuration = LocalConfiguration.current
                val isTablet = configuration.screenWidthDp >= 600 || configuration.smallestScreenWidthDp >= 600

                if (!isTablet) {
                    // İNCE DİKEY AYIRICI
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onBackToTools()
                                }
                            )
                            .testTag("notes_back_to_tools_button")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Araçlar Menüsü",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Notlar adası içindeki tek bir buton (Tıklandığında genişler, uzun basıldığında simge değiştirir).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotesIslandItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    colorHex: String?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val folderColor = remember(colorHex) {
        if (!colorHex.isNullOrBlank()) {
            try {
                Color(android.graphics.Color.parseColor(colorHex))
            } catch (_: Exception) {
                null
            }
        } else null
    }

    val defaultActiveBg = MaterialTheme.colorScheme.primaryContainer
    val defaultActiveContent = MaterialTheme.colorScheme.onPrimaryContainer

    val activeBg = folderColor?.copy(alpha = 0.25f) ?: defaultActiveBg
    val activeContent = folderColor ?: defaultActiveContent

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) activeBg else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "notes_island_item_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            activeContent
        } else {
            folderColor ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "notes_island_item_content"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "notes_island_icon_scale"
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = bgColor,
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick
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
                    horizontal = if (isSelected && label.isNotBlank()) 13.dp else 10.dp,
                    vertical = 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier
                    .size(21.dp)
                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
            )

            // Seçildiğinde yana doğru pürüzsüzce genişleyip metni gösterir
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
                            fontSize = 12.sp,
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

/**
 * Klasör simgesini değiştirmek için Google Fonts / Material Symbols seçim penceresi.
 * - Kalp, yıldız, satranç, smile vb. özel simgeler
 * - Matematik, fizik, kimya, biyoloji vb. ders simgeleri
 */
@Composable
fun FolderIconPickerDialog(
    folder: NoteFolder,
    onSelectIcon: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedCategory by remember { mutableStateOf("Özel") } // "Özel" veya "Dersler"

    val folderColor = remember(folder.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(folder.colorHex))
        } catch (_: Exception) {
            Color(0xFF1B65D4)
        }
    }

    val currentIcon = FolderIconHelper.getIcon(folder.iconName)
    val displayedIcons = remember(selectedCategory) {
        FolderIconHelper.ALL_ICONS.filter { it.category == selectedCategory }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(folderColor.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentIcon,
                        contentDescription = null,
                        tint = folderColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Klasör Simgesi Seç",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                // Kategori Filtre Hapları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "Özel",
                        onClick = { selectedCategory = "Özel" },
                        label = { Text("✨ Popüler & Özel", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = selectedCategory == "Dersler",
                        onClick = { selectedCategory = "Dersler" },
                        label = { Text("📚 Dersler", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Simgeler Izgarası (Grid)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 6.dp)
                ) {
                    items(displayedIcons, key = { it.key }) { item ->
                        val isCurrent = folder.iconName.equals(item.key, ignoreCase = true)
                        FolderIconGridItem(
                            item = item,
                            isSelected = isCurrent,
                            folderColor = folderColor,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectIcon(item.key)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
private fun FolderIconGridItem(
    item: FolderIconItem,
    isSelected: Boolean,
    folderColor: Color,
    onClick: () -> Unit
) {
    val bg = if (isSelected) folderColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.50f)
    val border = if (isSelected) BorderStroke(1.5.dp, folderColor) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    val iconTint = if (isSelected) folderColor else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = border,
        modifier = Modifier
            .size(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = if (isSelected) folderColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
