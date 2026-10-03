package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.TufiAICoachAdvice
import com.example.data.ai.TufiAIService
import com.example.data.model.Course
import com.example.data.model.HomeworkWithCourse
import com.example.ui.HomeworkStats
import com.example.ui.screens.MinimalHeroBanner
import kotlinx.coroutines.launch

/**
 * Swipeable Hero Section:
 * Page 0: Standard MinimalHeroBanner ("İyi Çalışmalar 📚")
 * Page 1: Tufi AI Kartı
 * Both cards share the EXACT same 148.dp height and minimalist style for zero layout shift.
 * Bottom switcher buttons and pill buttons have been removed as requested.
 */
@Composable
fun SwipeableHeroSection(
    todayDate: String,
    stats: HomeworkStats,
    courses: List<Course>,
    homeworks: List<HomeworkWithCourse>,
    onNavigateToTufiAI: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = 0) { 2 }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp)
    ) { page ->
        if (page == 0) {
            MinimalHeroBanner(
                todayDate = todayDate,
                stats = stats,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            TufiAICoachCard(
                courses = courses,
                homeworks = homeworks,
                onNavigateToTufiAI = onNavigateToTufiAI,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Tufi AI Çalışma Kartı:
 * "İyi Çalışmalar" kartıyla tamamen aynı boyutta (148.dp),
 * Başlık sadece ve doğrudan "Tufi AI" yazar.
 * "Devamını Göster" tuşu ile alt bardaki / yeni kısımdaki Tufi AI ekranına geçiş yapılır.
 */
@Composable
fun TufiAICoachCard(
    courses: List<Course>,
    homeworks: List<HomeworkWithCourse>,
    onNavigateToTufiAI: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var advice by remember { mutableStateOf<TufiAICoachAdvice?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val todayCourseNames = remember(courses, homeworks) {
        val hwCourses = homeworks.mapNotNull { it.course?.name }.filter { it.isNotBlank() }
        val allCourses = courses.map { it.name }.filter { it.isNotBlank() }
        (hwCourses + allCourses).distinct().ifEmpty { listOf("Matematik", "Türkçe", "Fizik", "Tarih") }
    }

    val homeworkTitles = remember(homeworks) {
        homeworks.filter { !it.homework.isCompleted }.map { it.homework.title }
    }

    LaunchedEffect(homeworks) {
        isLoading = true
        advice = TufiAIService.getCoachingAdvice(
            context = context,
            courses = courses,
            homeworks = homeworks,
            forceRefresh = false
        )
        isLoading = false
    }

    fun refreshAdvice() {
        coroutineScope.launch {
            isLoading = true
            advice = TufiAIService.getCoachingAdvice(
                context = context,
                courses = courses,
                homeworks = homeworks,
                forceRefresh = true
            )
            isLoading = false
            Toast.makeText(context, "Tufi AI güncellendi! 🚀", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Robot Icon + Title (Dümdüz "Tufi AI") + Refresh Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Tufi AI",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Tufi AI",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { refreshAdvice() },
                    enabled = !isLoading,
                    modifier = Modifier.size(28.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Middle: 1-line crisp advice / summary
            val currentAdvice = advice ?: remember(homeworks) {
                TufiAIService.generateBuiltInAdvice(homeworks)
            }

            if (isLoading && advice == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Derece öğrencisi taktikleri analiz ediliyor...",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "🏆 ${currentAdvice.summary}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom Row: "Devamını Göster" Button on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onNavigateToTufiAI,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Devamını Göster",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }
        }
    }
}
