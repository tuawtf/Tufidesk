package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import com.example.ui.theme.LocalIsTwilightNight
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.data.model.Note
import com.example.data.model.NoteType
import com.example.ui.HomeworkStats
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ExamAnalysisItem(
    val id: Long,
    val examName: String,
    val category: String, // TYT, AYT, BRANS
    val dateMillis: Long,
    val totalNet: Double,
    val targetNet: Double?,
    val durationMinutes: Int?,
    val difficulty: Int,
    val subjectNets: Map<String, Double>
)

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
    notes: List<Note> = emptyList(),
    onToggleComplete: (Homework) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 0: Tümü, 1: Ödevler, 2: Deneme Analizi
    var selectedStatsTab by remember { mutableIntStateOf(0) } // Varsayılan olarak Tümü

    // Parse note records with NoteType.REPORT into structured ExamAnalysisItems
    val examAnalysisList = remember(notes) {
        notes.filter { it.type == NoteType.REPORT }.mapNotNull { note ->
            try {
                val json = if (note.reportData.isNullOrBlank()) JSONObject() else JSONObject(note.reportData)
                val examName = json.optString("examName", note.title.ifBlank { "Deneme Sınavı" })
                val rawCategory = json.optString("category", if (examName.contains("AYT", true)) "AYT" else if (examName.contains("Branş", true)) "Branş" else "TYT")
                val category = if (rawCategory.equals("LGS", ignoreCase = true)) "TYT" else rawCategory
                val totalNet = json.optDouble("totalNet", 0.0)
                val targetNet = if (json.has("targetNet")) json.optDouble("targetNet") else null
                val duration = if (json.has("duration")) json.optInt("duration") else null
                val difficulty = json.optInt("difficulty", 3)

                val subjectNets = mutableMapOf<String, Double>()
                val subjectsObj = json.optJSONObject("subjects")
                if (subjectsObj != null) {
                    val keys = subjectsObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val sub = subjectsObj.getJSONObject(key)
                        val subName = sub.optString("name", key)
                        val c = sub.optInt("d", 0)
                        val w = sub.optInt("y", 0)
                        val n = sub.optDouble("net", c - (w / 4.0))
                        subjectNets[subName] = n
                    }
                }

                ExamAnalysisItem(
                    id = note.id,
                    examName = examName,
                    category = category,
                    dateMillis = note.createdAtMillis,
                    totalNet = totalNet,
                    targetNet = targetNet,
                    durationMinutes = duration,
                    difficulty = difficulty,
                    subjectNets = subjectNets
                )
            } catch (_: Exception) {
                null
            }
        }.sortedBy { it.dateMillis } // En eskiden en yeniye kronolojik (grafik için)
    }

    // Kişiye özel haftalık ilerleme verileri (Ödevlerin tarihlerine göre DİNAMİK belirlenir)
    val weeklyReports = remember(homeworks) {
        val dateFmt = SimpleDateFormat("d MMMM", Locale("tr"))

        // Herhangi bir zaman damgası için haftanın Pazartesi (00:00) ve Pazar (23:59:59) aralığını hesapla
            fun getWeekBounds(timestampMillis: Long): Pair<Long, Long> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = timestampMillis
                    firstDayOfWeek = Calendar.MONDAY
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    val dow = get(Calendar.DAY_OF_WEEK)
                    val daysFromMonday = if (dow == Calendar.SUNDAY) 6 else (dow - Calendar.MONDAY)
                    add(Calendar.DAY_OF_MONTH, -daysFromMonday)
                }
                val start = cal.timeInMillis
                val endCal = (cal.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, 6)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val end = endCal.timeInMillis
                return Pair(start, end)
            }

            val now = System.currentTimeMillis()
            val currentWeekBounds = getWeekBounds(now)

            // Mevcut tüm ödevlerin haftalarını dinamik tespit et
            val weekBoundsSet = mutableSetOf<Pair<Long, Long>>()
            // Bu haftayı her zaman dahil et
            weekBoundsSet.add(currentWeekBounds)

            for (hw in homeworks) {
                val due = hw.homework.dueDateMillis
                if (due > 0L) {
                    weekBoundsSet.add(getWeekBounds(due))
                }
            }

            // Haftaları en eskiden en yeniye doğru kronolojik olarak sırala
            val sortedWeeks = weekBoundsSet.sortedBy { it.first }

            // Her haftaya kronolojik olarak 1, 2, 3... hafta numarası ata
            val reports = sortedWeeks.mapIndexed { index, bounds ->
                val weekNum = index + 1
                val (wStart, wEnd) = bounds
                val isCurrent = (now in wStart..wEnd)

                val wHws = homeworks.filter { it.homework.dueDateMillis in wStart..wEnd }
                val total = wHws.size
                val comp = wHws.count { it.homework.isCompleted }
                val pct = if (total > 0) (comp * 100) / total else 0

                val rangeText = "${dateFmt.format(Date(wStart))} - ${dateFmt.format(Date(wEnd))}"

                WeeklyReportData(
                    weekNumber = weekNum,
                    weekTitle = "$weekNum. Hafta",
                    dateRangeText = rangeText,
                    homeworks = wHws,
                    totalCount = total,
                    completedCount = comp,
                    percentage = pct,
                    isCurrent = isCurrent
                )
            }

            // En yeni haftadan eskiye doğru sırala (Yeni haftalar en üstte)
            reports.reversed()
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_screen")
    ) {
        // Üst Kategori Değiştirici (Zarif, modern, pinned kapsül bar)
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        Triple(0, "Tümü", Icons.Default.BarChart),
                        Triple(1, "Ödevler", Icons.Default.PendingActions),
                        Triple(2, "Deneme Analizi", Icons.Default.Timeline)
                    )
                    tabs.forEach { (index, title, icon) ->
                        val isSelected = selectedStatsTab == index
                        val animatedBgColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            label = "tab_bg"
                        )
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            label = "tab_content"
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = animatedBgColor,
                            shadowElevation = if (isSelected) 1.5.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedStatsTab = index }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    ),
                                    color = contentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================= ÖDEV İSTATİSTİKLERİ BÖLÜMÜ (Tümü veya Ödevler) =================
            if (selectedStatsTab == 0 || selectedStatsTab == 1) {
                // Top Overview Card with Progress Ring
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Ödev Başarı Durumu",
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
                                val isTwilightNight = LocalIsTwilightNight.current
                                StatRowItem(
                                    label = "Teslim Edilen",
                                    value = stats.completedCount.toString(),
                                    color = if (isTwilightNight) MaterialTheme.colorScheme.secondary else Color(0xFF10B981)
                                )
                                StatRowItem(
                                    label = "Bekleyen",
                                    value = stats.pendingCount.toString(),
                                    color = if (isTwilightNight) MaterialTheme.colorScheme.tertiary else Color(0xFFD97706)
                                )
                                if (stats.overdueCount > 0) {
                                    StatRowItem(
                                        label = "Geciken",
                                        value = stats.overdueCount.toString(),
                                        color = if (isTwilightNight) MaterialTheme.colorScheme.error else Color(0xFFDC2626)
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
                            text = "Ödevler teslim tarihlerine göre haftalık listelenir",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (weeklyReports.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PendingActions,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Henüz ödev kaydı bulunmuyor",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ödev ekledikçe haftalık ilerleme ve başarı oranları burada otomatik listelenecektir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(weeklyReports) { week ->
                    WeeklyReportCard(
                        week = week,
                        onToggleComplete = onToggleComplete
                    )
                }
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

        // ================= DENEME SINAVI ANALİZ GRAFİĞİ BÖLÜMÜ (Tümü veya Deneme Analizi) =================
        if (selectedStatsTab == 0 || selectedStatsTab == 2) {
            if (selectedStatsTab == 0) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Deneme Sınavı Analizi",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            item {
                DenemeAnalysisChartSection(
                    examList = examAnalysisList
                )
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

/**
 * Material 3 Deneme Sınavı Analiz ve Net Gelişim Grafiği Bölümü
 */
@Composable
fun DenemeAnalysisChartSection(
    examList: List<ExamAnalysisItem>,
    modifier: Modifier = Modifier
) {
    val categories = listOf("Tümü", "TYT", "AYT", "Branş")
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val selectedCategory = categories[selectedCategoryIndex]

    val filteredExams = remember(examList, selectedCategory) {
        when (selectedCategory) {
            "Tümü" -> examList
            "TYT" -> examList.filter { it.category.equals("TYT", ignoreCase = true) }
            "AYT" -> examList.filter { it.category.equals("AYT", ignoreCase = true) }
            else -> examList.filter { it.category.equals("Branş", ignoreCase = true) || it.category.equals("BRANS", ignoreCase = true) }
        }
    }

    var selectedExamId by remember(filteredExams) {
        mutableStateOf(filteredExams.lastOrNull()?.id)
    }

    val selectedExam = remember(filteredExams, selectedExamId) {
        filteredExams.find { it.id == selectedExamId } ?: filteredExams.lastOrNull()
    }

    // İstatistiksel Özetler
    val totalCount = filteredExams.size
    val averageNet = if (filteredExams.isNotEmpty()) filteredExams.map { it.totalNet }.average() else 0.0
    val maxNet = if (filteredExams.isNotEmpty()) filteredExams.maxOf { it.totalNet } else 0.0
    val latestNet = filteredExams.lastOrNull()?.totalNet ?: 0.0

    // Ders bazında ortalama netler
    val subjectAverages = remember(filteredExams) {
        val sums = mutableMapOf<String, Pair<Double, Int>>() // Subject -> (TotalNet, Count)
        filteredExams.forEach { exam ->
            exam.subjectNets.forEach { (sub, net) ->
                val curr = sums.getOrDefault(sub, Pair(0.0, 0))
                sums[sub] = Pair(curr.first + net, curr.second + 1)
            }
        }
        sums.mapValues { it.value.first / it.value.second.toDouble() }
            .toList()
            .sortedByDescending { it.second }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Üst Kategori Çipleri (Zarif, oval hap çipler)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEachIndexed { index, label ->
                    val isSelected = index == selectedCategoryIndex
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { selectedCategoryIndex = index }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            if (totalCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ) {
                    Text(
                        text = "$totalCount Sınav",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Eğer hiç deneme yoksa
        if (filteredExams.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Henüz $selectedCategory Deneme Kaydı Yok",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Deneme Sınavları sekmesinden karne ekledikçe net gelişim grafiğiniz burada çizilecektir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // 2. 3'lü KPI Kartları (Hafif, estetik ve renkli mini kartlar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Ortalama Net
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ortalama",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", averageNet),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                val isTwilightNight = LocalIsTwilightNight.current

                // Zirve Net
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isTwilightNight) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f) else Color(0xFF10B981).copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, if (isTwilightNight) MaterialTheme.colorScheme.outlineVariant else Color(0xFF10B981).copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = if (isTwilightNight) MaterialTheme.colorScheme.secondary else Color(0xFF10B981),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Zirve Net",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", maxNet),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isTwilightNight) MaterialTheme.colorScheme.secondary else Color(0xFF10B981)
                        )
                    }
                }

                // Son Sınav
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isTwilightNight) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f) else Color(0xFF6366F1).copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, if (isTwilightNight) MaterialTheme.colorScheme.outlineVariant else Color(0xFF6366F1).copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = if (isTwilightNight) MaterialTheme.colorScheme.tertiary else Color(0xFF6366F1),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Son Sınav",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", latestNet),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isTwilightNight) MaterialTheme.colorScheme.tertiary else Color(0xFF6366F1)
                        )
                    }
                }
            }

            // 3. NET GELİŞİM ÇİZGİ GRAFİĞİ (Geniş, ferah ve modern Canvas)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Net Gelişim Grafiği",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Grafik üzerindeki noktalara dokunarak sınavı inceleyin",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (selectedExam != null) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f", selectedExam.totalNet)} Net",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val primaryColor = MaterialTheme.colorScheme.primary
                    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                    val surfaceColor = MaterialTheme.colorScheme.surface

                    val maxChartNet = remember(filteredExams) {
                        val peak = filteredExams.maxOfOrNull { it.totalNet } ?: 100.0
                        (Math.ceil(peak.coerceAtLeast(30.0) / 20.0) * 20.0).toFloat()
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Y ekseni etiketleri
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = 8.dp, top = 2.dp, bottom = 2.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.End
                        ) {
                            val stepCount = 3
                            for (i in stepCount downTo 0) {
                                val labelVal = (maxChartNet * (i / stepCount.toFloat())).toInt()
                                Text(
                                    text = "$labelVal",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            }
                        }

                        // Canvas Alanı
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(filteredExams) {
                                    detectTapGestures { offset ->
                                        if (filteredExams.isEmpty()) return@detectTapGestures
                                        val stepX = size.width / (filteredExams.size - 1).coerceAtLeast(1)
                                        val clickedIndex = ((offset.x / stepX) + 0.5f).toInt().coerceIn(0, filteredExams.size - 1)
                                        selectedExamId = filteredExams[clickedIndex].id
                                    }
                                }
                        ) {
                            val w = size.width
                            val h = size.height
                            val paddingY = 8.dp.toPx()
                            val usableHeight = h - (paddingY * 2)

                            // 1. Dotted Horizontal Guidelines
                            val steps = 3
                            for (i in 0..steps) {
                                val y = paddingY + usableHeight * (1f - (i / steps.toFloat()))
                                drawLine(
                                    color = gridLineColor,
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 0.8.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                )
                            }

                            if (filteredExams.size == 1) {
                                val singleNet = filteredExams.first().totalNet.toFloat()
                                val ratio = (singleNet / maxChartNet).coerceIn(0f, 1f)
                                val cy = paddingY + usableHeight * (1f - ratio)
                                val cx = w / 2f

                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.20f),
                                    radius = 16.dp.toPx(),
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = primaryColor,
                                    radius = 6.dp.toPx(),
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = surfaceColor,
                                    radius = 2.5.dp.toPx(),
                                    center = Offset(cx, cy)
                                )
                            } else {
                                val stepX = w / (filteredExams.size - 1).toFloat()
                                val points = filteredExams.mapIndexed { index, exam ->
                                    val px = index * stepX
                                    val ratio = (exam.totalNet.toFloat() / maxChartNet).coerceIn(0f, 1f)
                                    val py = paddingY + usableHeight * (1f - ratio)
                                    Offset(px, py)
                                }

                                val linePath = Path().apply {
                                    moveTo(points.first().x, points.first().y)
                                    for (i in 1 until points.size) {
                                        val prev = points[i - 1]
                                        val curr = points[i]
                                        val cpx = (prev.x + curr.x) / 2f
                                        cubicTo(cpx, prev.y, cpx, curr.y, curr.x, curr.y)
                                    }
                                }

                                val fillPath = Path().apply {
                                    addPath(linePath)
                                    lineTo(points.last().x, h)
                                    lineTo(points.first().x, h)
                                    close()
                                }

                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.22f),
                                            primaryColor.copy(alpha = 0.01f)
                                        ),
                                        startY = 0f,
                                        endY = h
                                    )
                                )

                                drawPath(
                                    path = linePath,
                                    color = primaryColor,
                                    style = Stroke(
                                        width = 2.5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                )

                                points.forEachIndexed { index, pt ->
                                    val isPointSelected = filteredExams[index].id == selectedExam?.id
                                    if (isPointSelected) {
                                        drawLine(
                                            color = primaryColor.copy(alpha = 0.35f),
                                            start = Offset(pt.x, 0f),
                                            end = Offset(pt.x, h),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                        )
                                        drawCircle(
                                            color = primaryColor.copy(alpha = 0.22f),
                                            radius = 11.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                    drawCircle(
                                        color = primaryColor,
                                        radius = if (isPointSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = surfaceColor,
                                        radius = if (isPointSelected) 2.2.dp.toPx() else 1.5.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }
                    }

                    // Sınav Yatay Seçim Listesi (Oval hap çipler)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredExams) { exam ->
                            val isSelected = exam.id == selectedExam?.id
                            val dateFmt = remember { SimpleDateFormat("d MMM", Locale("tr")) }
                            val dateStr = dateFmt.format(Date(exam.dateMillis))

                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) else null,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { selectedExamId = exam.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%.1f", exam.totalNet)}N",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. SEÇİLİ SINAV AYRINTI KARTI (Ferah, modern kart)
            if (selectedExam != null) {
                val dateFmt = remember { SimpleDateFormat("d MMMM yyyy", Locale("tr")) }
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedExam.examName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${selectedExam.category} • ${dateFmt.format(Date(selectedExam.dateMillis))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.2f", selectedExam.totalNet)} Net",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Ders Bazlı Dağılım Çipleri
                        if (selectedExam.subjectNets.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Ders Netleri",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedExam.subjectNets.forEach { (subName, net) ->
                                    val subColor = getSubjectColor(subName)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = subColor.copy(alpha = 0.12f),
                                        border = BorderStroke(0.5.dp, subColor.copy(alpha = 0.25f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = subName,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${String.format(Locale.getDefault(), "%.1f", net)} Net",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = subColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. DERS & BRANŞ NET DAĞILIMI (Renkli, estetik ilerleme çubukları)
            if (subjectAverages.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ders Bazlı Genel Ortalama",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        subjectAverages.forEach { (subjectName, avgNet) ->
                            val maxQ = if (subjectName.contains("Türkçe", true) || subjectName.contains("Matematik", true)) 40.0 else 20.0
                            val pct = (avgNet / maxQ).coerceIn(0.0, 1.0).toFloat()
                            val subColor = getSubjectColor(subjectName)

                            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = subjectName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%.1f", avgNet)} Net",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = subColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = subColor,
                                    trackColor = subColor.copy(alpha = 0.15f),
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getSubjectColor(subjectName: String): Color {
    val lower = subjectName.lowercase()
    return when {
        lower.contains("türkçe") || lower.contains("turkce") || lower.contains("edebiyat") -> Color(0xFF2563EB)
        lower.contains("mat") || lower.contains("geometri") -> Color(0xFF6366F1)
        lower.contains("fen") || lower.contains("biyoloji") -> Color(0xFF10B981)
        lower.contains("fizik") -> Color(0xFF0284C7)
        lower.contains("kimya") -> Color(0xFFEC4899)
        lower.contains("sosyal") || lower.contains("tarih") -> Color(0xFFF59E0B)
        lower.contains("coğrafya") || lower.contains("cografya") -> Color(0xFFF97316)
        lower.contains("felsefe") || lower.contains("din") -> Color(0xFF8B5CF6)
        else -> Color(0xFF3B82F6)
    }
}
