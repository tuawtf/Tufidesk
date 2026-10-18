package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import androidx.glance.layout.fillMaxWidth
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

    override val sizeMode: SizeMode = SizeMode.Single

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
    // Main Container (4x2 layout with tight vertical spacing)
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFF3EFFB))
            .cornerRadius(28.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // 1. Left Section (Tight content column centered vertically without giant gaps)
        Column(
            modifier = GlanceModifier
                .fillMaxHeight()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 105.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            // Upper Part: Flame Icon + Streak Count
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(
                        if (data.todayDone) R.drawable.ic_widget_flame else R.drawable.ic_widget_flame_outline
                    ),
                    contentDescription = "Alev",
                    modifier = GlanceModifier.size(30.dp),
                    colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF6750A4)))
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = "${data.currentStreak} gün",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color(0xFF1D1B20))
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Middle Part: Homework Status (Clock + text)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_clock),
                    contentDescription = "Saat",
                    modifier = GlanceModifier.size(13.dp),
                    colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF49454F)))
                )

                Spacer(modifier = GlanceModifier.width(5.dp))

                Text(
                    text = "Bugün ${data.completedTodayCount} tamamlandı, ${data.pendingTodayCount} bekliyor",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = ColorProvider(Color(0xFF49454F))
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Lower Part: Weekly Streak Table
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8DEF8))
                    .cornerRadius(16.dp)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val weekDays = getWeekDaysList(data.days)
                weekDays.forEachIndexed { index, day ->
                    if (index > 0) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
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

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        if (day.isCompleted) {
                            Box(
                                modifier = GlanceModifier
                                    .size(20.dp)
                                    .background(Color(0xFF7C5DFA))
                                    .cornerRadius(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_widget_check),
                                    contentDescription = "Tamamlandı",
                                    modifier = GlanceModifier.size(12.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = GlanceModifier
                                    .size(20.dp)
                                    .background(Color(0xFFD0BCFF))
                                    .cornerRadius(10.dp)
                            ) {}
                        }
                    }
                }
            }
        }

        // 2. Right Section (Pony Illustration)
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Image(
                provider = ImageProvider(R.drawable.twilight_bottom_illustration),
                contentDescription = "Twilight Sparkle",
                modifier = GlanceModifier
                    .width(105.dp)
                    .height(115.dp),
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
