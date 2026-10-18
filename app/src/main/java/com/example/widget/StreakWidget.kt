package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.R
import com.example.data.model.StreakDay

class StreakWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakWidget()
}

class StreakWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(100.dp, 100.dp),
            DpSize(220.dp, 100.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = StreakWidgetCache.get(context)
        provideContent {
            GlanceTheme {
                StreakWidgetContent(data)
            }
        }
    }
}

@Composable
fun StreakWidgetContent(data: StreakWidgetData) {
    // Main Container (Outer Box - Clipping overflow)
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFF3EFFB))
            .cornerRadius(28.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // 1. Left Section (Content Column)
        Column(
            modifier = GlanceModifier
                .fillMaxHeight()
                .padding(start = 18.dp, top = 16.dp, bottom = 16.dp, end = 100.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Upper Part: Streak Counter
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(44.dp)
                        .background(Color(0xFFE2D5F8))
                        .cornerRadius(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_flame),
                        contentDescription = "Alev",
                        modifier = GlanceModifier.size(24.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF6750A4)))
                    )
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                Text(
                    text = "${data.currentStreak} gün",
                    style = TextStyle(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color(0xFF1D1B20))
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // Middle Part: Weekly Days Capsule
            Row(
                modifier = GlanceModifier
                    .background(Color(0xFFE8DEF8))
                    .cornerRadius(18.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val weekDays = getWeekDaysList(data.days)
                weekDays.forEachIndexed { index, day ->
                    if (index > 0) {
                        Spacer(modifier = GlanceModifier.width(8.dp))
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.dayLabel,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorProvider(Color(0xFF49454F))
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        if (day.isCompleted) {
                            Box(
                                modifier = GlanceModifier
                                    .size(24.dp)
                                    .background(Color(0xFF7C5DFA))
                                    .cornerRadius(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_widget_check),
                                    contentDescription = "Tamamlandı",
                                    modifier = GlanceModifier.size(14.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = GlanceModifier
                                    .size(24.dp)
                                    .background(Color(0xFFD0BCFF))
                                    .cornerRadius(12.dp)
                            ) {}
                        }
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // Lower Part: Homework Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_clock),
                    contentDescription = "Saat",
                    modifier = GlanceModifier.size(16.dp),
                    colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF49454F)))
                )

                Spacer(modifier = GlanceModifier.width(6.dp))

                Text(
                    text = "Bugün ${data.completedTodayCount} ödev tamamlandı, ${data.pendingTodayCount} ödev bekliyor",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = ColorProvider(Color(0xFF49454F))
                    )
                )
            }
        }

        // 2. Right Section (Pony Illustration - Strictly within boundaries)
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Image(
                provider = ImageProvider(R.drawable.twilight_bottom_illustration),
                contentDescription = "Twilight Sparkle",
                modifier = GlanceModifier
                    .width(115.dp)
                    .height(130.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private fun getWeekDaysList(dataDays: List<StreakDay>): List<StreakDay> {
    if (dataDays.size >= 5) {
        return dataDays.take(5)
    }
    val fallbackLabels = listOf("Pzt", "Sal", "Çar", "Per", "Cum")
    return fallbackLabels.map { label ->
        StreakDay(
            dayLabel = label,
            dayNumber = 0,
            dateMillis = 0L,
            isCompleted = false,
            isToday = false,
            isFuture = false
        )
    }
}
