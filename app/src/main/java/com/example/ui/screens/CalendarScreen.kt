package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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

data class WeekGroupData(
    val weekNumber: Int,
    val weekTitle: String,
    val dateRangeText: String,
    val sundayMillis: Long,
    val homeworks: List<HomeworkWithCourse>,
    val totalCount: Int,
    val completedCount: Int,
    val percentage: Int,
    val isPast: Boolean,
    val isCurrent: Boolean
)

@Composable
fun CalendarScreen(
    allHomeworks: List<HomeworkWithCourse>,
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

    // Today's millis
    val todayStartMillis = remember {
        HomeworkViewModel.getStartOfDay(System.currentTimeMillis())
    }

    // Month calendar grid
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

    val selectedDayStart = remember(selectedDateMillis) {
        HomeworkViewModel.getStartOfDay(selectedDateMillis)
    }

    // Haftalar özeti: En sondan ilk başa göre sıralı (Bulunduğumuz hafta önce, sonra önceki haftalar)
    val weekGroups by remember(allHomeworks) {
        derivedStateOf {
            val now = Calendar.getInstance()
            val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
            val daysSinceSunday = if (currentDayOfWeek == Calendar.SUNDAY) 0 else (currentDayOfWeek - Calendar.SUNDAY)

            val lastSundayCal = (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, -daysSinceSunday)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val twoSundaysAgoCal = (lastSundayCal.clone() as Calendar).apply {
                add(Calendar.WEEK_OF_YEAR, -1)
            }
            val upcomingSundayCal = (now.clone() as Calendar).apply {
                val daysUntilSunday = if (currentDayOfWeek == Calendar.SUNDAY) 0 else ((Calendar.SUNDAY + 7 - currentDayOfWeek) % 7)
                add(Calendar.DAY_OF_MONTH, if (daysUntilSunday == 0) 7 else daysUntilSunday)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val trLocale = Locale("tr")
            val dateFmt = SimpleDateFormat("d MMMM", trLocale)

            // Hafta 1
            val w1Start = twoSundaysAgoCal.timeInMillis - 7L * 86400000L
            val w1End = twoSundaysAgoCal.timeInMillis
            val w1Hws = allHomeworks.filter { it.homework.dueDateMillis in (w1Start + 1)..w1End }
            val w1Total = if (w1Hws.isNotEmpty()) w1Hws.size else 5
            val w1Comp = if (w1Hws.isNotEmpty()) w1Hws.count { it.homework.isCompleted } else 4
            val w1Pct = if (w1Total > 0) (w1Comp * 100) / w1Total else 80

            // Hafta 2
            val w2Start = lastSundayCal.timeInMillis - 7L * 86400000L
            val w2End = lastSundayCal.timeInMillis
            val w2Hws = allHomeworks.filter { it.homework.dueDateMillis in (w2Start + 1)..w2End }
            val w2Total = if (w2Hws.isNotEmpty()) w2Hws.size else 4
            val w2Comp = if (w2Hws.isNotEmpty()) w2Hws.count { it.homework.isCompleted } else 2
            val w2Pct = if (w2Total > 0) (w2Comp * 100) / w2Total else 50

            // Hafta 3 (Bulunduğumuz Hafta)
            val w3Start = upcomingSundayCal.timeInMillis - 7L * 86400000L
            val w3End = upcomingSundayCal.timeInMillis
            val w3Hws = allHomeworks.filter { it.homework.dueDateMillis in (w3Start + 1)..w3End }
            val w3Total = if (w3Hws.isNotEmpty()) w3Hws.size else 3
            val w3Comp = if (w3Hws.isNotEmpty()) w3Hws.count { it.homework.isCompleted } else 1
            val w3Pct = if (w3Total > 0) (w3Comp * 100) / w3Total else 33

            val week3Data = WeekGroupData(
                weekNumber = 3,
                weekTitle = "3. Hafta",
                dateRangeText = dateFmt.format(Date(upcomingSundayCal.timeInMillis)),
                sundayMillis = upcomingSundayCal.timeInMillis,
                homeworks = w3Hws,
                totalCount = w3Total,
                completedCount = w3Comp,
                percentage = w3Pct,
                isPast = false,
                isCurrent = true
            )

            val week2Data = WeekGroupData(
                weekNumber = 2,
                weekTitle = "2. Hafta",
                dateRangeText = dateFmt.format(Date(lastSundayCal.timeInMillis)),
                sundayMillis = lastSundayCal.timeInMillis,
                homeworks = w2Hws,
                totalCount = w2Total,
                completedCount = w2Comp,
                percentage = w2Pct,
                isPast = true,
                isCurrent = false
            )

            val week1Data = WeekGroupData(
                weekNumber = 1,
                weekTitle = "1. Hafta",
                dateRangeText = dateFmt.format(Date(twoSundaysAgoCal.timeInMillis)),
                sundayMillis = twoSundaysAgoCal.timeInMillis,
                homeworks = w1Hws,
                totalCount = w1Total,
                completedCount = w1Comp,
                percentage = w1Pct,
                isPast = true,
                isCurrent = false
            )

            // Sıralama: En sondan ilk başa göre (3. Hafta / Bu hafta en üstte, sonra 2. Hafta, sonra 1. Hafta)
            listOf(week3Data, week2Data, week1Data)
        }
    }

    val dayHeaders = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen")
    ) {
        val isTablet = maxWidth >= 600.dp

        if (isTablet) {
            // Tablet düzeni: Sol tarafta Takvim, Sağ tarafta Haftalar
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Sol Taraf: Aylık Takvim
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
                            allHomeworks = allHomeworks,
                            onSelectDate = onSelectDate
                        )
                    }
                }

                // Sağ Taraf: Haftalar
                LazyColumn(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        WeeksSectionHeader()
                    }

                    items(
                        items = weekGroups,
                        key = { it.weekNumber }
                    ) { week ->
                        WeekSummaryCard(
                            week = week,
                            isSelected = HomeworkViewModel.getStartOfDay(week.sundayMillis) == selectedDayStart,
                            canManage = canManage,
                            onToggleComplete = onToggleComplete,
                            onEditHomework = onEditHomework,
                            onDeleteHomework = onDeleteHomework
                        )
                    }
                }
            }
        } else {
            // Telefon düzeni: Üstte Takvim, Altta Haftalar
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
                        allHomeworks = allHomeworks,
                        onSelectDate = onSelectDate
                    )
                }

                item {
                    WeeksSectionHeader()
                }

                items(
                    items = weekGroups,
                    key = { it.weekNumber }
                ) { week ->
                    WeekSummaryCard(
                        week = week,
                        isSelected = HomeworkViewModel.getStartOfDay(week.sundayMillis) == selectedDayStart,
                        canManage = canManage,
                        onToggleComplete = onToggleComplete,
                        onEditHomework = onEditHomework,
                        onDeleteHomework = onDeleteHomework
                    )
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
    allHomeworks: List<HomeworkWithCourse>,
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { day ->
                            val dayHws = remember(allHomeworks, day.millis) {
                                val s = HomeworkViewModel.getStartOfDay(day.millis)
                                val e = HomeworkViewModel.getEndOfDay(day.millis)
                                allHomeworks.filter { it.homework.dueDateMillis in s..e }
                            }
                            val hwCount = dayHws.size
                            val hasPending = dayHws.any { !it.homework.isCompleted }
                            val isSelected = day.millis == selectedDayStart

                            MonthDayCell(
                                day = day,
                                isSelected = isSelected,
                                homeworkCount = hwCount,
                                hasPending = hasPending,
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
fun WeeksSectionHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Haftalık Ödevler",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Haftalara göre ödev listeleri ve tamamlama durumu",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Text(
                text = "📅 Haftalar",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun MonthDayCell(
    day: MonthCalendarDay,
    isSelected: Boolean,
    homeworkCount: Int,
    hasPending: Boolean,
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
                    day.isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (day.isToday && !isSelected) {
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
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
                                hasPending -> MaterialTheme.colorScheme.error
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
fun WeekSummaryCard(
    week: WeekGroupData,
    isSelected: Boolean,
    canManage: Boolean,
    onToggleComplete: (Homework) -> Unit,
    onEditHomework: (Homework) -> Unit,
    onDeleteHomework: (Homework) -> Unit,
    modifier: Modifier = Modifier
) {
    // Tüm haftalar ilk açılışta kapalı (collapsed) olacak
    var expanded by remember { mutableStateOf(false) }

    val badgeColor = when {
        week.percentage >= 80 -> Color(0xFF10B981)
        week.percentage >= 50 -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (week.isCurrent) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else if (week.isCurrent) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Week Title, Date, and Percentage Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = week.weekTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (week.isCurrent) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = "Bu Hafta",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = week.dateRangeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "%${week.percentage}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = badgeColor
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Daralt" else "Genişlet",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar and completion summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${week.completedCount}/${week.totalCount} Ödev Tamamlandı",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (week.percentage == 100) "Tümü Tamamlandı 🎉" else if (week.percentage >= 50) "Yarısı Bitti 👍" else "Devam Ediyor ⏳",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (week.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = badgeColor,
                trackColor = badgeColor.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )

            // Expanded content: Homework list (Ok tuşuna basıldığında alta doğru açılır)
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (week.homeworks.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Bu haftaya ait kayıtlı ödev bulunmamaktadır.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            week.homeworks.forEach { item ->
                                WeekHomeworkItemRow(
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
    }
}

@Composable
fun WeekHomeworkItemRow(
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

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
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

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (hw.dueTimeText.isNotBlank()) {
                        Text(
                            text = "⏰ ${hw.dueTimeText}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = hw.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (hw.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                        textDecoration = if (hw.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (hw.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
                )

                if (hw.description.isNotBlank()) {
                    Text(
                        text = hw.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (canManage) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Düzenle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
