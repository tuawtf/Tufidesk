package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Material 3 Tasarım Sistemine Uyarlanmış Araçlar ve Modüller Ekranı.
 */
@Composable
fun ToolsScreen(
    noteCount: Int,
    examCount: Int = 0,
    onOpenNotes: () -> Unit,
    onOpenExams: () -> Unit,
    onOpenTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .testTag("tools_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Material 3 Üst Başlık Banner Kartı (Tonal Surface)
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
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Araçlar & Modüller",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ders çalışma, analiz ve odaklanma araçları",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 1. M3 KARE KART: NOTLAR (Google Keep Stili Not Defteri)
        item {
            M3SquareToolCard(
                title = "Notlar",
                description = if (noteCount > 0) "$noteCount not kayıtlı" else "Not defteri, liste ve çizimler",
                icon = Icons.Default.Description,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                badgeText = if (noteCount > 0) "$noteCount" else null,
                badgeContainerColor = MaterialTheme.colorScheme.primary,
                badgeContentColor = MaterialTheme.colorScheme.onPrimary,
                isAvailable = true,
                onClick = onOpenNotes,
                testTag = "tool_card_notes"
            )
        }

        // 2. M3 KARE KART: DENEMELER (TYT, AYT, LGS Net Takibi)
        item {
            M3SquareToolCard(
                title = "Denemeler",
                description = if (examCount > 0) "$examCount sınav karnesi" else "TYT, AYT ve LGS net takibi",
                icon = Icons.Default.Assessment,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                iconContentColor = MaterialTheme.colorScheme.onErrorContainer,
                badgeText = if (examCount > 0) "$examCount" else "Yeni",
                badgeContainerColor = MaterialTheme.colorScheme.error,
                badgeContentColor = MaterialTheme.colorScheme.onError,
                isAvailable = true,
                onClick = onOpenExams,
                testTag = "tool_card_exams"
            )
        }

        // 3. M3 KARE KART: SAYAÇ & ODAK (Pomodoro, Kronometre, Zamanlayıcı)
        item {
            M3SquareToolCard(
                title = "Sayaç & Odak",
                description = "Pomodoro, Kronometre & Geri Sayım",
                icon = Icons.Default.HourglassTop,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                badgeText = "Aktif",
                badgeContainerColor = MaterialTheme.colorScheme.tertiary,
                badgeContentColor = MaterialTheme.colorScheme.onTertiary,
                isAvailable = true,
                onClick = onOpenTimer,
                testTag = "tool_card_pomodoro"
            )
        }

        // 4. M3 KARE KART: DERS PROGRAMI (Haftalık Çizelge)
        item {
            M3SquareToolCard(
                title = "Ders Programı",
                description = "Haftalık etüt ve ders çizelgesi",
                icon = Icons.Default.CalendarMonth,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                badgeText = "Yakında",
                badgeContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                badgeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                isAvailable = false,
                onClick = {},
                testTag = "tool_card_schedule"
            )
        }
    }
}

/**
 * Material 3 Tasarım Sistemine uygun 1:1 Oranlı Kare Kart Bileşeni.
 */
@Composable
fun M3SquareToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    containerColor: Color,
    iconContainerColor: Color,
    iconContentColor: Color,
    badgeText: String? = null,
    badgeContainerColor: Color = MaterialTheme.colorScheme.primary,
    badgeContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    isAvailable: Boolean = true,
    onClick: () -> Unit = {},
    testTag: String = ""
) {
    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isAvailable) containerColor else MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isAvailable) 2.dp else 0.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(22.dp))
            .clickable(enabled = isAvailable, onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Üst Bölüm: İkon Konteynırı ve M3 Rozet (Badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isAvailable) iconContainerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isAvailable) iconContentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAvailable) badgeContainerColor else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isAvailable) badgeContentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alt Bölüm: Başlık, Açıklama ve Giriş İkonu
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = if (isAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isAvailable) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
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
