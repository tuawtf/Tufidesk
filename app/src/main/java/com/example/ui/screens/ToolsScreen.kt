package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ToolItemData(
    val key: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val containerColor: @Composable () -> Color,
    val iconContainerColor: @Composable () -> Color,
    val iconContentColor: @Composable () -> Color,
    val badgeText: String? = null,
    val isAvailable: Boolean = true,
    val onClick: () -> Unit
)

/**
 * Material 3 Tasarım Sistemine Uyarlanmış, Uzun Basmalı Yüzen Menü ve Taşıma/Sıralama Özellikli Araçlar Ekranı.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolsScreen(
    noteCount: Int,
    examCount: Int = 0,
    pinnedBottomTools: List<String> = emptyList(),
    toolsOrder: List<String> = listOf("notes", "exams", "timer", "schedule"),
    onTogglePinToBottom: (String) -> Unit = {},
    onMoveTool: (String, Boolean) -> Unit = { _, _ -> },
    onResetOrder: () -> Unit = {},
    onOpenNotes: () -> Unit,
    onOpenExams: () -> Unit,
    onOpenTimer: () -> Unit,
    onSelectionActiveChange: (Boolean) -> Unit = {},
    onNotify: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Seçili araç (Uzun basınca aktif olan yüzen menü için)
    var selectedToolKey by rememberSaveable { mutableStateOf<String?>(null) }
    var showReorderDialog by rememberSaveable { mutableStateOf(false) }

    val isSelectionMode = selectedToolKey != null

    // Geri tuşuna basıldığında yüzen menüyü kapat
    BackHandler(enabled = isSelectionMode) {
        selectedToolKey = null
    }

    // Tüm araçların tanımları
    val allToolsMap = remember(noteCount, examCount) {
        mapOf(
            "notes" to ToolItemData(
                key = "notes",
                title = "Notlar",
                description = if (noteCount > 0) "$noteCount not kayıtlı" else "Not defteri, liste ve çizimler",
                icon = Icons.Default.Description,
                containerColor = { MaterialTheme.colorScheme.surfaceContainerLow },
                iconContainerColor = { MaterialTheme.colorScheme.primaryContainer },
                iconContentColor = { MaterialTheme.colorScheme.onPrimaryContainer },
                badgeText = if (noteCount > 0) "$noteCount" else null,
                isAvailable = true,
                onClick = onOpenNotes
            ),
            "exams" to ToolItemData(
                key = "exams",
                title = "Denemeler",
                description = if (examCount > 0) "$examCount sınav karnesi" else "TYT, AYT ve LGS net takibi",
                icon = Icons.Default.Assessment,
                containerColor = { MaterialTheme.colorScheme.surfaceContainerLow },
                iconContainerColor = { MaterialTheme.colorScheme.errorContainer },
                iconContentColor = { MaterialTheme.colorScheme.onErrorContainer },
                badgeText = if (examCount > 0) "$examCount" else "Yeni",
                isAvailable = true,
                onClick = onOpenExams
            ),
            "timer" to ToolItemData(
                key = "timer",
                title = "Sayaç & Odak",
                description = "Pomodoro, Kronometre & Geri Sayım",
                icon = Icons.Default.HourglassTop,
                containerColor = { MaterialTheme.colorScheme.surfaceContainerLow },
                iconContainerColor = { MaterialTheme.colorScheme.tertiaryContainer },
                iconContentColor = { MaterialTheme.colorScheme.onTertiaryContainer },
                badgeText = "Aktif",
                isAvailable = true,
                onClick = onOpenTimer
            ),
            "schedule" to ToolItemData(
                key = "schedule",
                title = "Ders Programı",
                description = "Haftalık etüt ve ders çizelgesi",
                icon = Icons.Default.CalendarMonth,
                containerColor = { MaterialTheme.colorScheme.surfaceContainerLowest },
                iconContainerColor = { MaterialTheme.colorScheme.secondaryContainer },
                iconContentColor = { MaterialTheme.colorScheme.onSecondaryContainer },
                badgeText = "Yakında",
                isAvailable = false,
                onClick = {}
            )
        )
    }

    // Kullanıcının belirlediği sıralamaya göre liste oluştur
    val orderedTools = remember(toolsOrder, allToolsMap) {
        val list = mutableListOf<ToolItemData>()
        toolsOrder.forEach { key ->
            allToolsMap[key]?.let { list.add(it) }
        }
        // Eksik kalan varsa sona ekle
        allToolsMap.values.forEach { tool ->
            if (!list.any { it.key == tool.key }) {
                list.add(tool)
            }
        }
        list
    }

    val selectedToolData = remember(selectedToolKey, allToolsMap) {
        selectedToolKey?.let { allToolsMap[it] }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("tools_screen")
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Material 3 Üst Başlık Banner Kartı
            item(span = { GridItemSpan(2) }) {
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Araçlar & Modüller",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Uzun basarak alt bara sabitleyin veya taşıyın",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Sıralama & Taşıma Butonu
                        FilledTonalIconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showReorderDialog = true
                            },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Araçları Sırala",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Araç Kartları (Uzun Tıklama Destekli)
            items(orderedTools, key = { it.key }) { tool ->
                val isSelected = selectedToolKey == tool.key
                val isPinned = pinnedBottomTools.contains(tool.key)

                M3InteractiveToolCard(
                    tool = tool,
                    isSelected = isSelected,
                    isPinned = isPinned,
                    onClick = {
                        if (isSelectionMode) {
                            // Seçim modundaysa seçili aracı değiştir veya kapat
                            selectedToolKey = if (selectedToolKey == tool.key) null else tool.key
                        } else {
                            tool.onClick()
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedToolKey = tool.key
                        onNotify("📌 '${tool.title}' seçildi. Aşağıdaki yüzen menüden alt bara sabitleyebilir veya taşıyabilirsiniz.")
                    }
                )
            }
        }

        // ================= TIPKI NOTLARDAKİ GİBİ: UZUN BASILINCA AÇILAN YÜZEN EYLEM MENÜSÜ =================
        AnimatedVisibility(
            visible = isSelectionMode && selectedToolData != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 88.dp, start = 16.dp, end = 16.dp)
        ) {
            val tool = selectedToolData ?: return@AnimatedVisibility
            val isPinned = pinnedBottomTools.contains(tool.key)

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                border = BorderStroke(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .wrapContentWidth()
                    .testTag("floating_tool_menu")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Seçilen Araç Başlığı & İkonu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tool.title,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 1. MENÜ BARINA EKLE / KALDIR BUTONU
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTogglePinToBottom(tool.key)
                            if (isPinned) {
                                onNotify("'${tool.title}' menü barından kaldırıldı.")
                            } else {
                                onNotify("✨ '${tool.title}' menü barına eklendi!")
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPinned) "Menüden Kaldır" else "Menü Barına Ekle",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // 2. TAŞI / SIRALA BUTONU
                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showReorderDialog = true
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Reorder,
                            contentDescription = "Taşı ve Sırala",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 3. AÇ BUTONU
                    if (tool.isAvailable) {
                        FilledTonalIconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedToolKey = null
                                tool.onClick()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Aç",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // 4. KAPAT BUTONU
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedToolKey = null
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // ================= TAŞIMA VE SIRALAMA MODAL DİALOGU =================
    if (showReorderDialog) {
        ReorderToolsDialog(
            orderedTools = orderedTools,
            pinnedBottomTools = pinnedBottomTools,
            onMoveUp = { key -> onMoveTool(key, true) },
            onMoveDown = { key -> onMoveTool(key, false) },
            onTogglePin = { key -> onTogglePinToBottom(key) },
            onResetOrder = onResetOrder,
            onDismiss = { showReorderDialog = false }
        )
    }
}

/**
 * Uzun basılabilir, dokunsal titreşimli ve seçim vurgulu M3 Araç Kartı.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun M3InteractiveToolCard(
    tool: ToolItemData,
    isSelected: Boolean,
    isPinned: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.94f
            isSelected -> 1.03f
            else -> 1f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tool_card_scale"
    )

    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else if (tool.isAvailable) {
                tool.containerColor()
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isSelected) 6.dp else if (tool.isAvailable) 2.dp else 0.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .then(
                if (isSelected) {
                    Modifier.background(Color.Transparent, RoundedCornerShape(22.dp))
                } else Modifier
            )
            .clip(RoundedCornerShape(22.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("tool_card_${tool.key}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Üst Bölüm: İkon Konteynırı ve Rozetler (Alt Bar Pinned / Seçili / Badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (tool.isAvailable) tool.iconContainerColor() else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = null,
                        tint = if (tool.isAvailable) tool.iconContentColor() else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Alt Bara Sabitlendi Rozeti
                    if (isPinned) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Alt Bara Sabitlendi",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    // Seçim Onay Rozeti
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seçildi",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else if (tool.badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (tool.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (tool.isAvailable) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Text(
                                text = tool.badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alt Bölüm: Başlık, Açıklama ve Giriş Oku
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tool.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = if (tool.isAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (tool.isAvailable) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

/**
 * Araçları Taşıma, Sıralama ve Alt Bara Sabitleme Modal Dialogu.
 */
@Composable
private fun ReorderToolsDialog(
    orderedTools: List<ToolItemData>,
    pinnedBottomTools: List<String>,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onResetOrder: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Araçları Sırala & Taşı",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Yukarı/aşağı butonları ile araçların yerini değiştirebilir, anahtarı açarak alt gezinme çubuğuna sabitleyebilirsiniz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(orderedTools, key = { _, item -> item.key }) { index, item ->
                        val isPinned = pinnedBottomTools.contains(item.key)
                        val isFirst = index == 0
                        val isLast = index == orderedTools.size - 1

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isPinned) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // İkon ve Başlık
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isPinned) "Alt barda gösteriliyor" else "Yalnızca araçlarda",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Aksiyonlar: Alt Bar Switch + Yukarı / Aşağı
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Alt Bar Switch
                                    Switch(
                                        checked = isPinned,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onTogglePin(item.key)
                                        },
                                        modifier = Modifier.scale(0.8f)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Yukarı Taşı
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onMoveUp(item.key)
                                        },
                                        enabled = !isFirst,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Yukarı Taşı",
                                            tint = if (!isFirst) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Aşağı Taşı
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onMoveDown(item.key)
                                        },
                                        enabled = !isLast,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Aşağı Taşı",
                                            tint = if (!isLast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tamam")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onResetOrder()
                }
            ) {
                Text("Varsayılana Sıfırla")
            }
        }
    )
}
