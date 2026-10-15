package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.HomeworkWithCourse
import com.example.ui.HomeworkViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeworkItemCard(
    item: HomeworkWithCourse,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    canManage: Boolean = true,
    modifier: Modifier = Modifier
) {
    val homework = item.homework
    val course = item.course
    var menuExpanded by remember { mutableStateOf(false) }

    val courseColor = remember(course?.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(course?.colorHex ?: "#2563EB"))
        } catch (_: Exception) {
            Color(0xFF2563EB)
        }
    }

    val cardBg by animateColorAsState(
        targetValue = if (homework.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = tween(durationMillis = 300),
        label = "cardBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("homework_card_${homework.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Course Tag with Logo and Options Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Course badge with subject logo
                val courseIcon = com.example.ui.util.CourseIconHelper.getIconForCourse(course?.name ?: "Genel")
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = courseColor.copy(alpha = 0.14f),
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = courseIcon,
                            contentDescription = null,
                            tint = courseColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = course?.name ?: "Genel",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = courseColor
                            )
                        )
                    }
                }

                if (canManage) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Options Menu Button
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("menu_button_${homework.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Daha Fazla Seçenek",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Düzenle") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Düzenle",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onEdit()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Sil",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main row: Custom Checkbox + Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Interactive Checkbox Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (homework.isCompleted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onToggleComplete() }
                        .testTag("toggle_complete_${homework.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (homework.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Tamamlandı",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = homework.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (homework.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (homework.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    if (homework.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = homework.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom details row: Due Date Tag & Duration chip
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Due date countdown / tag
                DueDateChip(
                    dueDateMillis = homework.dueDateMillis,
                    dueTimeText = homework.dueTimeText,
                    isCompleted = homework.isCompleted
                )

                // Estimated time chip
                if (homework.estimatedMinutes > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Süre",
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${homework.estimatedMinutes} dk",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DueDateChip(
    dueDateMillis: Long,
    dueTimeText: String,
    isCompleted: Boolean
) {
    val now = System.currentTimeMillis()
    val calNow = Calendar.getInstance().apply { timeInMillis = now }
    val calDue = Calendar.getInstance().apply { timeInMillis = dueDateMillis }

    val isSameDay = calNow.get(Calendar.YEAR) == calDue.get(Calendar.YEAR) &&
            calNow.get(Calendar.DAY_OF_YEAR) == calDue.get(Calendar.DAY_OF_YEAR)

    calNow.add(Calendar.DAY_OF_YEAR, 1)
    val isTomorrow = calNow.get(Calendar.YEAR) == calDue.get(Calendar.YEAR) &&
            calNow.get(Calendar.DAY_OF_YEAR) == calDue.get(Calendar.DAY_OF_YEAR)
    calNow.add(Calendar.DAY_OF_YEAR, -1) // restore

    // Teslim tarihi her zaman o haftanın Pazar günüdür.
    val sundayDeadlineMillis = HomeworkViewModel.getSundayOfWeek(dueDateMillis)
    val isOverdue = HomeworkViewModel.isOverdue(dueDateMillis, isCompleted, now)

    val dayFormat = SimpleDateFormat("EEEE", Locale("tr"))
    val sundayDateFormat = SimpleDateFormat("d MMMM EEEE", Locale("tr"))

    val targetDayName = dayFormat.format(Date(dueDateMillis))
    val timeSuffix = if (dueTimeText.isNotBlank()) " $dueTimeText" else ""

    val (chipText, chipBg, chipColor, chipIcon) = when {
        isCompleted -> Quadruple(
            "Tamamlandı",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            Icons.Default.Check
        )
        isOverdue -> Quadruple(
            "Gecikti",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Warning
        )
        isSameDay -> Quadruple(
            "Bugün$timeSuffix",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Default.AccessTime
        )
        isTomorrow -> Quadruple(
            "Yarın$timeSuffix",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.AccessTime
        )
        else -> Quadruple(
            "$targetDayName$timeSuffix",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.AccessTime
        )
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = chipBg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = chipIcon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = chipColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = chipText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = chipColor
            )
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
