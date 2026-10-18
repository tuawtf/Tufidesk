package com.example.widget

import android.content.Context
import android.content.Intent
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
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
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

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(100.dp, 100.dp), // 2x2 Small
            DpSize(220.dp, 100.dp)  // 4x2 Wide
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
    val size = LocalSize.current
    val isWide = size.width >= 200.dp

    val flameColor = ColorProvider(Color(0xFFE65100))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        if (isWide) {
            WideWidgetLayout(data = data, flameColor = flameColor)
        } else {
            SmallWidgetLayout(data = data, flameColor = flameColor)
        }
    }
}

@Composable
fun SmallWidgetLayout(data: StreakWidgetData, flameColor: ColorProvider) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Flame Icon
        Image(
            provider = ImageProvider(
                if (data.todayDone) R.drawable.ic_widget_flame else R.drawable.ic_widget_flame_outline
            ),
            contentDescription = "Alev",
            modifier = GlanceModifier.size(32.dp),
            colorFilter = ColorFilter.tint(flameColor)
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Big streak number
        Text(
            text = "${data.currentStreak}",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface
            )
        )

        // Subtitle
        Text(
            text = "günlük seri",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Status text
        val statusText = if (data.todayDone) {
            "Bugün tamam"
        } else if (data.pendingTodayCount > 0) {
            "Bugün ${data.pendingTodayCount} ödev yap"
        } else {
            "Bugün 1 ödev yap"
        }

        val statusColor = if (data.todayDone) {
            flameColor
        } else {
            GlanceTheme.colors.onSurfaceVariant
        }

        Text(
            text = statusText,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = statusColor
            )
        )
    }
}

@Composable
fun WideWidgetLayout(data: StreakWidgetData, flameColor: ColorProvider) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Upper section: Left (Flame + count), Right (7 days status)
        Row(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(
                            if (data.todayDone) R.drawable.ic_widget_flame else R.drawable.ic_widget_flame_outline
                        ),
                        contentDescription = "Alev",
                        modifier = GlanceModifier.size(28.dp),
                        colorFilter = ColorFilter.tint(flameColor)
                    )

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    Text(
                        text = "${data.currentStreak}",
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(2.dp))

                Text(
                    text = "günlük seri",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }

            // Right side: 7 Days Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.End
            ) {
                val daysToShow = data.days.take(7)
                for (day in daysToShow) {
                    DayCircleItem(day = day, flameColor = flameColor)
                    Spacer(modifier = GlanceModifier.width(4.dp))
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Bottom section: "Bugün X ödev tamamlandı, Y ödev bekliyor"
        val bottomMsg = "Bugün ${data.completedTodayCount} ödev tamamlandı, ${data.pendingTodayCount} ödev bekliyor"
        Text(
            text = bottomMsg,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
    }
}

@Composable
fun DayCircleItem(day: StreakDay, flameColor: ColorProvider) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.dayLabel,
            style = TextStyle(
                fontSize = 9.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )

        Spacer(modifier = GlanceModifier.height(2.dp))

        val iconRes = when {
            day.isCompleted -> R.drawable.ic_widget_circle_filled
            day.isToday -> R.drawable.ic_widget_circle_outline
            else -> R.drawable.ic_widget_circle_empty
        }

        val colorFilter = when {
            day.isCompleted || day.isToday -> ColorFilter.tint(flameColor)
            else -> ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
        }

        Image(
            provider = ImageProvider(iconRes),
            contentDescription = day.dayLabel,
            modifier = GlanceModifier.size(16.dp),
            colorFilter = colorFilter
        )
    }
}
