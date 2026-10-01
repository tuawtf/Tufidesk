package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.example.data.model.Course
import com.example.data.model.Homework
import com.example.data.model.HomeworkWithCourse
import com.example.ui.HomeworkStats
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeeklyReportData(
    val weekNumber: Int,
    val weekTitle: String,
    val dateRangeText: String,
    val homeworks: List<HomeworkWithCourse>,
    val totalCount: Int,
    val completedCount: Int,
    val percentage: Int,
    val isCurrent: Boolean
)

@Composable
fun StatsScreen(
    stats: HomeworkStats,
    courses: List<Course>,
    homeworks: List<HomeworkWithCourse>,
    onToggleComplete: (Homework) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Kişiye özel haftalık ilerleme verileri (En sondan başa doğru: 3. Hafta, 2. Hafta, 1. Hafta)
    val weeklyReports by remember(homeworks) {
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

            val dateFmt = SimpleDateFormat("d MMMM", Locale("tr"))

            // 1. Hafta
            val w1Start = twoSundaysAgoCal.timeInMillis - 7L * 86400000L
            val w1End = twoSundaysAgoCal.timeInMillis
            val w1Hws = homeworks.filter { it.homework.dueDateMillis in (w1Start + 1)..w1End }
            val w1Total = w1Hws.size
            val w1Comp = w1Hws.count { it.homework.isCompleted }
            val w1Pct = if (w1Total > 0) (w1Comp * 100) / w1Total else 0

            // 2. Hafta
            val w2Start = lastSundayCal.timeInMillis - 7L * 86400000L
            val w2End = lastSundayCal.timeInMillis
            val w2Hws = homeworks.filter { it.homework.dueDateMillis in (w2Start + 1)..w2End }
            val w2Total = w2Hws.size
            val w2Comp = w2Hws.count { it.homework.isCompleted }
            val w2Pct = if (w2Total > 0) (w2Comp * 100) / w2Total else 0

            // 3. Hafta (Bu Hafta)
            val w3Start = upcomingSundayCal.timeInMillis - 7L * 86400000L
            val w3End = upcomingSundayCal.timeInMillis
            val w3Hws = homeworks.filter { it.homework.dueDateMillis in (w3Start + 1)..w3End }
            val w3Total = w3Hws.size
            val w3Comp = w3Hws.count { it.homework.isCompleted }
            val w3Pct = if (w3Total > 0) (w3Comp * 100) / w3Total else 0

            val w3 = WeeklyReportData(
                weekNumber = 3,
                weekTitle = "3. Hafta",
                dateRangeText = dateFmt.format(Date(upcomingSundayCal.timeInMillis)),
                homeworks = w3Hws,
                totalCount = w3Total,
                completedCount = w3Comp,
                percentage = w3Pct,
                isCurrent = true
            )
            val w2 = WeeklyReportData(
                weekNumber = 2,
                weekTitle = "2. Hafta",
                dateRangeText = dateFmt.format(Date(lastSundayCal.timeInMillis)),
                homeworks = w2Hws,
                totalCount = w2Total,
                completedCount = w2Comp,
                percentage = w2Pct,
                isCurrent = false
            )
            val w1 = WeeklyReportData(
                weekNumber = 1,
                weekTitle = "1. Hafta",
                dateRangeText = dateFmt.format(Date(twoSundaysAgoCal.timeInMillis)),
                homeworks = w1Hws,
                totalCount = w1Total,
                completedCount = w1Comp,
                percentage = w1Pct,
                isCurrent = false
            )

            // Sıralama: En son haftadan ilk haftaya doğru (3. Hafta / Bu hafta en üstte)
            listOf(w3, w2, w1)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Overview Card with Progress Ring
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Genel Başarı Durumu",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Circular Progress
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(105.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = {
                                    if (stats.totalCount > 0) stats.completedCount.toFloat() / stats.totalCount.toFloat() else 0f
                                },
                                modifier = Modifier.size(105.dp),
                                strokeWidth = 10.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "%${stats.completionPercentage}",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tamamlandı",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Quick metrics grid
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatRowItem(
                                label = "Toplam Ödev",
                                value = stats.totalCount.toString(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatRowItem(
                                label = "Teslim Edilen",
                                value = stats.completedCount.toString(),
                                color = Color(0xFF10B981)
                            )
                            StatRowItem(
                                label = "Bekleyen",
                                value = stats.pendingCount.toString(),
                                color = Color(0xFFD97706)
                            )
                            if (stats.overdueCount > 0) {
                                StatRowItem(
                                    label = "Geciken",
                                    value = stats.overdueCount.toString(),
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Estimated Hours banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Kalan tahmini çalışma süresi: ${String.format(Locale.US, "%.1f", stats.totalEstimatedHours)} saat",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // ================= HAFTALIK İLERLEME RAPORU =================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Haftalık İlerleme Raporu",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Haftalara göre ödev performansı ve detayları",
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

        items(
            items = weeklyReports,
            key = { it.weekNumber }
        ) { week ->
            WeeklyReportCard(
                week = week,
                onToggleComplete = onToggleComplete
            )
        }

        // Course Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Ders Bazında Tamamlanma",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val uniqueCourses = remember(courses) {
                        courses.distinctBy { it.name.trim().lowercase() }
                    }

                    if (uniqueCourses.isEmpty()) {
                        Text(
                            text = "Henüz ders eklenmedi.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uniqueCourses.forEach { course ->
                            val matchingCourseIds = courses.filter { it.name.trim().equals(course.name.trim(), ignoreCase = true) }.map { it.id }.toSet()
                            val hw = homeworks.filter { it.homework.courseId in matchingCourseIds }
                            val total = hw.size
                            val completed = hw.count { it.homework.isCompleted }
                            val pct = if (total > 0) (completed * 100) / total else 0
                            val cColor = remember(course.colorHex) {
                                try {
                                    Color(android.graphics.Color.parseColor(course.colorHex))
                                } catch (_: Exception) {
                                    Color(0xFF2563EB)
                                }
                            }

                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                val subjectIcon = com.example.ui.util.CourseIconHelper.getIconForCourse(course.name)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = subjectIcon,
                                            contentDescription = null,
                                            tint = cColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = course.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "$completed/$total (%$pct)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = cColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { if (total > 0) completed.toFloat() / total.toFloat() else 0f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = cColor,
                                    trackColor = cColor.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Motivational Quote & Productivity Tips Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Günün Çalışma İpucu",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "“Başarı, her gün tekrarlanan küçük çabaların toplamıdır.”",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pomodoro Tekniği: 25 dakika kesintisiz ödevine odaklan, ardından 5 dakika dinlen. Her 4 seansta bir 20 dakikalık uzun mola ver.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyReportCard(
    week: WeeklyReportData,
    onToggleComplete: (Homework) -> Unit,
    modifier: Modifier = Modifier
) {
    // Başlangıçta tüm kartlar kapalı (collapsed)
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
        border = BorderStroke(
            1.dp,
            if (week.isCurrent) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                    text = if (week.totalCount > 0 && week.percentage == 100) "Tümü Tamamlandı 🎉" else if (week.percentage >= 50) "Yarısı Bitti 👍" else "Devam Ediyor ⏳",
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
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = hw.isCompleted,
                                            onCheckedChange = { onToggleComplete(hw) },
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
                                    }
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
fun StatRowItem(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
