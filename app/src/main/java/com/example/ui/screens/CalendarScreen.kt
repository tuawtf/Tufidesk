package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Homework
import com.example.data.model.HomeworkWithCourse
import com.example.ui.HomeworkViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthCalendarDay(
    val millis: Long,
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSunday: Boolean
)

@Composable
fun CalendarScreen(
    homeworks: List<HomeworkWithCourse>,
    selectedDateMillis: Long,
    onSelectDate: (Long) -> Unit,
    onToggleComplete: (Homework) -> Unit,
    onEditHomework: (Homework) -> Unit,
    onDeleteHomework: (Homework) -> Unit,
    onAddNewHomeworkForDate: (Long) -> Unit = {},
    canManage: Boolean = true,
    modifier: Modifier = Modifier
) {
    var monthOffset by remember { mutableIntStateOf(0) }

    val baseCalendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val monthYearTitle = remember(baseCalendar) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("tr"))
        sdf.format(baseCalendar.time).replaceFirstChar { it.uppercase() }
    }

    val todayStartMillis = remember {
        HomeworkViewModel.getStartOfDay(System.currentTimeMillis())
    }

    val selectedDayStart = remember(selectedDateMillis) {
        HomeworkViewModel.getStartOfDay(selectedDateMillis)
    }

    val selectedDayEnd = remember(selectedDateMillis) {
        HomeworkViewModel.getEndOfDay(selectedDateMillis)
    }

    // Homeworks on the selected day
    val selectedDayHomeworks = remember(homeworks, selectedDayStart, selectedDayEnd) {
        homeworks.filter { it.homework.dueDateMillis in selectedDayStart..selectedDayEnd }
    }

    // Formatted title for the selected day
    val selectedDayTitle = remember(selectedDateMillis) {
        val sdf = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr"))
        sdf.format(Date(selectedDateMillis))
    }

    // Weeks of Month Grid
    val weeksOfMonth = remember(baseCalendar) {
        val firstDayOfMonth = baseCalendar.clone() as Calendar
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        val currentMonth = firstDayOfMonth.get(Calendar.MONTH)

        val dayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
        val daysBefore = (dayOfWeek - Calendar.MONDAY + 7) % 7

        val gridRunner = (firstDayOfMonth.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, -daysBefore)
        }

        val weeks = mutableListOf<List<MonthCalendarDay>>()
        var weekIdx = 0
        while (weekIdx < 6) {
            val weekDays = mutableListOf<MonthCalendarDay>()
            for (i in 0 until 7) {
                val startOfDay = HomeworkViewModel.getStartOfDay(gridRunner.timeInMillis)
                val isCurMonth = gridRunner.get(Calendar.MONTH) == currentMonth
                val dayNum = gridRunner.get(Calendar.DAY_OF_MONTH)
                val isToday = startOfDay == todayStartMillis
                val isSunday = gridRunner.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

                weekDays.add(
                    MonthCalendarDay(
                        millis = startOfDay,
                        dayNumber = dayNum,
                        isCurrentMonth = isCurMonth,
                        isToday = isToday,
                        isSunday = isSunday
                    )
                )
                gridRunner.add(Calendar.DAY_OF_MONTH, 1)
            }

            if (weekIdx < 5 || weekDays.any { it.isCurrentMonth }) {
                weeks.add(weekDays)
            }
            weekIdx++
        }
        weeks
    }

    val dayHeaders = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen")
    ) {
        val isTablet = maxWidth >= 600.dp

        if (isTablet) {
            // Tablet Layout: Split screen (Left: Calendar, Right: Selected Day Homeworks)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Column: Calendar
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MonthCalendarCard(
                            monthYearTitle = monthYearTitle,
                            monthOffset = monthOffset,
                            onPreviousMonth = { monthOffset-- },
                            onNextMonth = { monthOffset++ },
                            onResetToToday = {
                                monthOffset = 0
                                onSelectDate(todayStartMillis)
                            },
                            dayHeaders = dayHeaders,
                            weeksOfMonth = weeksOfMonth,
                            selectedDayStart = selectedDayStart,
                            homeworks = homeworks,
                            onSelectDate = onSelectDate
                        )
                    }
                }

                // Right Column: Selected Day's Homeworks
                LazyColumn(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        SelectedDayHeader(
                            selectedDayTitle = selectedDayTitle,
                            homeworkCount = selectedDayHomeworks.size,
                            canManage = canManage,
                            onAddNewHomework = { onAddNewHomeworkForDate(selectedDateMillis) }
                        )
                    }

                    if (selectedDayHomeworks.isEmpty()) {
                        item {
                            EmptyDayHomeworksCard(
                                onAddNewHomework = { onAddNewHomeworkForDate(selectedDateMillis) },
                                canManage = canManage
                            )
                        }
                    } else {
                        items(
                            items = selectedDayHomeworks,
                            key = { it.homework.id }
                        ) { item ->
                            DayHomeworkCard(
                                item = item,
                                canManage = canManage,
                                onToggleComplete = { onToggleComplete(item.homework) },
                                onEdit = { onEditHomework(item.homework) },
                                onDelete = { onDeleteHomework(item.homework) }
                            )
                        }
                    }
                }
            }
        } else {
            // Phone Layout: Calendar at top, Selected Day Homeworks below
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MonthCalendarCard(
                        monthYearTitle = monthYearTitle,
                        monthOffset = monthOffset,
                        onPreviousMonth = { monthOffset-- },
                        onNextMonth = { monthOffset++ },
                        onResetToToday = {
                            monthOffset = 0
                            onSelectDate(todayStartMillis)
                        },
                        dayHeaders = dayHeaders,
                        weeksOfMonth = weeksOfMonth,
                        selectedDayStart = selectedDayStart,
                        homeworks = homeworks,
                        onSelectDate = onSelectDate
                    )
                }

                item {
                    SelectedDayHeader(
                        selectedDayTitle = selectedDayTitle,
                        homeworkCount = selectedDayHomeworks.size,
                        canManage = canManage,
                        onAddNewHomework = { onAddNewHomeworkForDate(selectedDateMillis) }
                    )
                }

                if (selectedDayHomeworks.isEmpty()) {
                    item {
                        EmptyDayHomeworksCard(
                            onAddNewHomework = { onAddNewHomeworkForDate(selectedDateMillis) },
                            canManage = canManage
                        )
                    }
                } else {
                    items(
                        items = selectedDayHomeworks,
                        key = { it.homework.id }
                    ) { item ->
                        DayHomeworkCard(
                            item = item,
                            canManage = canManage,
                            onToggleComplete = { onToggleComplete(item.homework) },
                            onEdit = { onEditHomework(item.homework) },
                            onDelete = { onDeleteHomework(item.homework) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthCalendarCard(
    monthYearTitle: String,
    monthOffset: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetToToday: () -> Unit,
    dayHeaders: List<String>,
    weeksOfMonth: List<List<MonthCalendarDay>>,
    selectedDayStart: Long,
    homeworks: List<HomeworkWithCourse>,
    onSelectDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Month Title & Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthYearTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (monthOffset != 0) {
                        OutlinedButton(
                            onClick = onResetToToday,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bugün", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Önceki Ay",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Sonraki Ay",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day of Week Names Row (Pzt ... Paz)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayHeaders.forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Week-by-Week Rows
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                weeksOfMonth.forEach { weekDays ->
                    val isCurrentWeek = weekDays.any { it.isToday }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isCurrentWeek) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else Color.Transparent
                            )
                            .then(
                                if (isCurrentWeek) Modifier.border(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    RoundedCornerShape(12.dp)
                                ) else Modifier
                            )
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { day ->
                            val dayHws = remember(homeworks, day.millis) {
                                val s = HomeworkViewModel.getStartOfDay(day.millis)
                                val e = HomeworkViewModel.getEndOfDay(day.millis)
                                homeworks.filter { it.homework.dueDateMillis in s..e }
                            }
                            val hwCount = dayHws.size
                            val hasOverdue = dayHws.any { HomeworkViewModel.isOverdue(it.homework.dueDateMillis, it.homework.isCompleted) }
                            val hasPending = dayHws.any { !it.homework.isCompleted }
                            val isSelected = day.millis == selectedDayStart

                            MonthDayCell(
                                day = day,
                                isSelected = isSelected,
                                isCurrentWeek = isCurrentWeek,
                                homeworkCount = hwCount,
                                hasPending = hasPending,
                                hasOverdue = hasOverdue,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                onClick = {
                                    onSelectDate(day.millis)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthDayCell(
    day: MonthCalendarDay,
    isSelected: Boolean,
    isCurrentWeek: Boolean = false,
    homeworkCount: Int,
    hasPending: Boolean,
    hasOverdue: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    day.isToday -> MaterialTheme.colorScheme.primaryContainer
                    isCurrentWeek && day.isCurrentMonth -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (day.isToday && !isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    day.isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // Homework Indicator Dot
            if (homeworkCount > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                hasOverdue -> MaterialTheme.colorScheme.error
                                hasPending -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                )
            } else {
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}

@Composable
fun SelectedDayHeader(
    selectedDayTitle: String,
    homeworkCount: Int,
    canManage: Boolean,
    onAddNewHomework: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedDayTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (homeworkCount > 0) "$homeworkCount ödev planlandı" else "Bu güne ait ödev yok",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (canManage) {
            Button(
                onClick = onAddNewHomework,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Ödev Ekle",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun EmptyDayHomeworksCard(
    onAddNewHomework: () -> Unit,
    canManage: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Bu Güne Ait Ödev Yok",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Seçilen tarihe henüz bir görev veya ödev eklenmemiş.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (canManage) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onAddNewHomework,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bu Güne Ödev Ekle")
                }
            }
        }
    }
}

@Composable
fun DayHomeworkCard(
    item: HomeworkWithCourse,
    canManage: Boolean,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hw = item.homework
    val course = item.course
    val courseColor = remember(course?.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(course?.colorHex ?: "#2563EB"))
        } catch (_: Exception) {
            Color(0xFF2563EB)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hw.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.dp,
            if (hw.isCompleted) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (hw.isCompleted) 0.dp else 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = hw.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (course != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = courseColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = courseColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (hw.dueTimeText.isNotBlank()) {
                        Text(
                            text = "⏰ ${hw.dueTimeText}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val isOverdue = HomeworkViewModel.isOverdue(hw.dueDateMillis, hw.isCompleted)
                    if (isOverdue) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "⚠️ Gecikti",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = hw.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (hw.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                        textDecoration = if (hw.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (hw.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                if (hw.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = hw.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (canManage) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Düzenle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
