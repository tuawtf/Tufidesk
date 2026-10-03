package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.Course
import com.example.data.model.Homework
import com.example.data.model.HomeworkWithCourse
import com.example.data.model.UserAccount
import com.example.ui.HomeworkStats
import com.example.ui.SortOrder
import com.example.ui.StatusFilter
import com.example.ui.TimeFilter
import com.example.ui.components.FilterBar
import com.example.ui.components.HomeworkItemCard
import com.example.ui.components.SwipeableHeroSection
import com.example.ui.components.UserFlowerAvatar
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TextButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    homeworks: List<HomeworkWithCourse>,
    stats: HomeworkStats,
    statusFilter: StatusFilter,
    onStatusChange: (StatusFilter) -> Unit,
    timeFilter: TimeFilter,
    onTimeChange: (TimeFilter) -> Unit,
    sortOrder: SortOrder,
    onSortChange: (SortOrder) -> Unit,
    onToggleComplete: (Homework) -> Unit,
    onEditHomework: (Homework) -> Unit,
    onDeleteHomework: (Homework) -> Unit,
    canManage: Boolean = true,
    hideCompletedInSettings: Boolean = false,
    currentUser: UserAccount? = null,
    adminAssignedFilter: String = "Elif",
    onAdminAssignedFilterChange: (String) -> Unit = {},
    onSwitchProfile: () -> Unit = {},
    courses: List<Course> = emptyList(),
    modifier: Modifier = Modifier
) {
    val todayFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM", Locale("tr"))
        sdf.format(Date()).replaceFirstChar { it.uppercase() }
    }

    // Partition homeworks into active and completed
    val (activeHomeworks, completedHomeworks) = remember(homeworks) {
        homeworks.partition { !it.homework.isCompleted }
    }

    var completedExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Minimalist Material 3 Hero Greeting & Progress Header (Yana kaydırarak TufiAI Derece Koçuna geçilebilir)
        item {
            SwipeableHeroSection(
                todayDate = todayFormatted,
                stats = stats,
                courses = courses,
                homeworks = homeworks
            )
        }

        // Quick Stat Counter Badges Row
        item {
            QuickStatsRow(
                stats = stats,
                onStatClick = { filter ->
                    onTimeChange(filter)
                }
            )
        }

        // Filter and Sort Bar
        item {
            FilterBar(
                statusFilter = statusFilter,
                onStatusChange = onStatusChange,
                timeFilter = timeFilter,
                onTimeChange = onTimeChange,
                sortOrder = sortOrder,
                onSortChange = onSortChange
            )
        }

        // Section 1: Active Homeworks (Yapılacaklar)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Yapılacak Ödevler (${activeHomeworks.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Active items or Empty State
        if (activeHomeworks.isEmpty()) {
            item {
                EmptyHomeworksCard(
                    isSearchActive = statusFilter != StatusFilter.ALL ||
                            timeFilter != TimeFilter.ALL,
                    message = if (completedHomeworks.isNotEmpty()) "Harika! Bekleyen tüm ödevlerini tamamladın." else null
                )
            }
        } else {
            items(
                items = activeHomeworks,
                key = { it.homework.id }
            ) { item ->
                HomeworkItemCard(
                    item = item,
                    onToggleComplete = { onToggleComplete(item.homework) },
                    onEdit = { onEditHomework(item.homework) },
                    onDelete = { onDeleteHomework(item.homework) },
                    canManage = canManage
                )
            }
        }

        // Section 2: Completed Homeworks (Tamamlananlar - En Alta Ayrılmış Bölme)
        if (!hideCompletedInSettings && completedHomeworks.isNotEmpty() && statusFilter != StatusFilter.PENDING) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { completedExpanded = !completedExpanded }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tamamlananlar (${completedHomeworks.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = if (completedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (completedExpanded) "Gizle" else "Göster",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (completedExpanded) {
                items(
                    items = completedHomeworks,
                    key = { it.homework.id }
                ) { item ->
                    HomeworkItemCard(
                        item = item,
                        onToggleComplete = { onToggleComplete(item.homework) },
                        onEdit = { onEditHomework(item.homework) },
                        onDelete = { onDeleteHomework(item.homework) },
                        canManage = canManage
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalHeroBanner(
    todayDate: String,
    stats: HomeworkStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp)
            .testTag("hero_banner_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todayDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "İyi Çalışmalar 📚",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Progress Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "%${stats.completionPercentage}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "bitti",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Motivation text
            val motivationText = when {
                stats.totalCount == 0 -> "Henüz ödev eklenmedi. Yeni ödev eklemek için '+' butonuna bas."
                stats.completedCount == stats.totalCount -> "Tebrikler! Tüm ödevlerini başarıyla tamamladın 🎉"
                stats.dueTodayCount > 0 -> "Bugün teslim edilecek ${stats.dueTodayCount} ödevin var."
                stats.pendingCount > 0 -> "${stats.pendingCount} ödevin teslim edilmeyi bekliyor."
                else -> "Ödevlerini düzenli takip et."
            }

            Text(
                text = motivationText,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Progress Bar
            LinearProgressIndicator(
                progress = {
                    if (stats.totalCount > 0) stats.completedCount.toFloat() / stats.totalCount.toFloat() else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun QuickStatsRow(
    stats: HomeworkStats,
    onStatClick: (TimeFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            StatPill(
                title = "Bekleyen",
                count = stats.pendingCount,
                icon = Icons.Default.PendingActions,
                color = MaterialTheme.colorScheme.primary,
                bgColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { onStatClick(TimeFilter.ALL) }
            )
        }

        item {
            StatPill(
                title = "Bugün",
                count = stats.dueTodayCount,
                icon = Icons.Default.NotificationsActive,
                color = MaterialTheme.colorScheme.tertiary,
                bgColor = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = { onStatClick(TimeFilter.TODAY) }
            )
        }

        item {
            StatPill(
                title = "Tamamlanan",
                count = stats.completedCount,
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.secondary,
                bgColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { onStatClick(TimeFilter.ALL) }
            )
        }

        if (stats.overdueCount > 0) {
            item {
                StatPill(
                    title = "Geciken",
                    count = stats.overdueCount,
                    icon = Icons.Default.Warning,
                    color = MaterialTheme.colorScheme.error,
                    bgColor = MaterialTheme.colorScheme.errorContainer,
                    onClick = { onStatClick(TimeFilter.OVERDUE) }
                )
            }
        }
    }
}

@Composable
fun StatPill(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = color.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun EmptyHomeworksCard(
    isSearchActive: Boolean,
    message: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = when {
                    message != null -> message
                    isSearchActive -> "Eşleşen Ödev Bulunamadı"
                    else -> "Bekleyen Ödevin Yok"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isSearchActive) "Arama kriterlerini veya filtreleri değiştirerek tekrar deneyebilirsin."
                else "Yeni bir ödev eklemek için '+' butonuna dokunabilirsin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

