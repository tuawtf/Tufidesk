package com.example.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

enum class TimerTab(val title: String, val icon: ImageVector) {
    STOPWATCH("Kronometre", Icons.Default.Timer),
    COUNTDOWN("Zamanlayıcı", Icons.Default.HourglassTop),
    POMODORO("Pomodoro", Icons.Default.SelfImprovement)
}

enum class PomodoroMode(val title: String, val defaultMinutes: Int) {
    FOCUS("Odaklanma", 25),
    SHORT_BREAK("Kısa Mola", 5),
    LONG_BREAK("Uzun Mola", 15)
}

data class LapRecord(
    val lapNumber: Int,
    val lapTimeMillis: Long,
    val totalTimeMillis: Long
)

/**
 * Google Saat (Google Clock) tarzı, dokunsal titreşim (Haptic) ve yay animasyonlu Sayaç & Odak Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    onNotify: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = TimerTab.values()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_timer_screen")
    ) {
        // Google Clock Material 3 Primary Tab Row
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        Modifier.tabIndicatorOffset(selectedTab),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("timer_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }

        // Seçilen moda göre içerik
        when (selectedTab) {
            0 -> GoogleStopwatchContent()
            1 -> GoogleTimerContent(onNotify = onNotify)
            2 -> GooglePomodoroContent(onNotify = onNotify)
        }
    }
}

/**
 * 1. GOOGLE SAAT KRONOMETRE (Stopwatch)
 */
@Composable
private fun GoogleStopwatchContent() {
    val haptic = LocalHapticFeedback.current

    var isRunning by rememberSaveable { mutableStateOf(false) }
    var baseTimeMillis by rememberSaveable { mutableLongStateOf(0L) }
    var accumulatedTimeMillis by rememberSaveable { mutableLongStateOf(0L) }
    var currentTimeMillis by rememberSaveable { mutableLongStateOf(0L) }

    val lapRecords = remember { mutableStateListOf<LapRecord>() }

    // Accurate real-time monotonic stopwatch loop
    LaunchedEffect(isRunning) {
        if (isRunning) {
            baseTimeMillis = SystemClock.elapsedRealtime()
            while (isRunning) {
                val now = SystemClock.elapsedRealtime()
                currentTimeMillis = accumulatedTimeMillis + (now - baseTimeMillis)
                delay(16L) // ~60fps smooth rendering
            }
        }
    }

    val totalElapsed = if (isRunning) {
        currentTimeMillis
    } else {
        accumulatedTimeMillis
    }

    val hours = (totalElapsed / 3600000).toInt()
    val minutes = ((totalElapsed % 3600000) / 60000).toInt()
    val seconds = ((totalElapsed % 60000) / 1000).toInt()
    val hundredths = ((totalElapsed % 1000) / 10).toInt()

    // Find fastest and slowest lap (if at least 2 laps exist)
    val fastestLapTime = if (lapRecords.size >= 2) lapRecords.minOf { it.lapTimeMillis } else null
    val slowestLapTime = if (lapRecords.size >= 2) lapRecords.maxOf { it.lapTimeMillis } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Google Clock Dairesel Kadran & Büyük Dijital Ekran
        Box(
            modifier = Modifier
                .size(260.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Arka plan rayı (Track)
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeWidth = 10.dp,
                strokeCap = StrokeCap.Round
            )

            // Dönen 60 saniyelik ilerleme çemberi (Google Clock sweep)
            val secondSweepProgress = ((totalElapsed % 60000).toFloat() / 60000f).coerceIn(0f, 1f)
            CircularProgressIndicator(
                progress = { if (totalElapsed > 0) secondSweepProgress else 0f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 10.dp,
                strokeCap = StrokeCap.Round
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Saat varsa göster, yoksa dakika:saniye.salise
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (hours > 0) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 38.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 48.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = String.format(Locale.getDefault(), ".%02d", hundredths),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isRunning) "⏱️ Kronometre Çalışıyor" else if (totalElapsed > 0) "Durduruldu" else "Hazır",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Google Clock 3'lü Kontrol Çubuğu (Sıfırla | Play/Pause | Tur)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SOL: Sıfırla Butonu (Yay animasyonlu)
            AnimatedPressIconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isRunning = false
                    accumulatedTimeMillis = 0L
                    currentTimeMillis = 0L
                    baseTimeMillis = 0L
                    lapRecords.clear()
                },
                enabled = totalElapsed > 0L,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                size = 56.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sıfırla",
                    modifier = Modifier.size(24.dp)
                )
            }

            // ORTA: Google Saat Büyük FAB (Oynat / Durdur - Bouncy Scale Animasyonlu)
            val fabInteraction = remember { MutableInteractionSource() }
            val isFabPressed by fabInteraction.collectIsPressedAsState()
            val fabScale by animateFloatAsState(
                targetValue = if (isFabPressed) 0.88f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "fab_scale"
            )

            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isRunning) {
                        accumulatedTimeMillis = currentTimeMillis
                        isRunning = false
                    } else {
                        baseTimeMillis = SystemClock.elapsedRealtime()
                        isRunning = true
                    }
                },
                shape = CircleShape,
                containerColor = if (isRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                contentColor = if (isRunning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                interactionSource = fabInteraction,
                modifier = Modifier
                    .size(76.dp)
                    .scale(fabScale)
            ) {
                AnimatedContent(
                    targetState = isRunning,
                    transitionSpec = { (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut()) },
                    label = "play_pause_icon"
                ) { running ->
                    Icon(
                        imageVector = if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (running) "Durdur" else "Başlat",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // SAĞ: Tur (Lap) Butonu (Yay animasyonlu)
            AnimatedPressIconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val lastTotal = lapRecords.firstOrNull()?.totalTimeMillis ?: 0L
                    val lapDiff = totalElapsed - lastTotal
                    val newLap = LapRecord(
                        lapNumber = lapRecords.size + 1,
                        lapTimeMillis = lapDiff,
                        totalTimeMillis = totalElapsed
                    )
                    lapRecords.add(0, newLap)
                },
                enabled = isRunning,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                size = 56.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Tur",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tur Listesi (Google Clock Formatı)
        if (lapRecords.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lapRecords, key = { it.lapNumber }) { lap ->
                    val isFastest = fastestLapTime != null && lap.lapTimeMillis == fastestLapTime
                    val isSlowest = slowestLapTime != null && lap.lapTimeMillis == slowestLapTime

                    val lapMin = (lap.lapTimeMillis / 60000).toInt()
                    val lapSec = ((lap.lapTimeMillis % 60000) / 1000).toInt()
                    val lapHun = ((lap.lapTimeMillis % 1000) / 10).toInt()

                    val totMin = (lap.totalTimeMillis / 60000).toInt()
                    val totSec = ((lap.totalTimeMillis % 60000) / 1000).toInt()
                    val totHun = ((lap.totalTimeMillis % 1000) / 10).toInt()

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when {
                            isFastest -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            isSlowest -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isFastest -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                isSlowest -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(Locale.getDefault(), "Tur %02d", lap.lapNumber),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isFastest) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text(
                                            text = "En Hızlı",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (isSlowest) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(
                                            text = "En Yavaş",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format(Locale.getDefault(), "+%02d:%02d.%02d", lapMin, lapSec, lapHun),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = if (isFastest) MaterialTheme.colorScheme.primary else if (isSlowest) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:%02d.%02d", totMin, totSec, totHun),
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. GOOGLE SAAT ZAMANLAYICI (Keypad Sayı Tuş Takımı ile Süre Oluşturma & Geri Sayım)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoogleTimerContent(
    onNotify: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var inputDigits by rememberSaveable { mutableStateOf("") }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var isTimerActive by rememberSaveable { mutableStateOf(false) }
    var totalTimerSeconds by rememberSaveable { mutableIntStateOf(0) }
    var secondsRemaining by rememberSaveable { mutableIntStateOf(0) }

    fun playAlarmSound() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 1200)
        } catch (_: Throwable) {}
    }

    LaunchedEffect(isRunning, secondsRemaining) {
        if (isRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
        } else if (isRunning && secondsRemaining == 0) {
            isRunning = false
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            playAlarmSound()
            onNotify("⏰ Zamanlayıcı Süresi Doldu! 🎉")
        }
    }

    val padded = inputDigits.padStart(6, '0')
    val inputHours = padded.substring(0, 2).toInt()
    val inputMinutes = padded.substring(2, 4).toInt()
    val inputSeconds = padded.substring(4, 6).toInt()
    val calculatedTotalSeconds = inputHours * 3600 + inputMinutes * 60 + inputSeconds

    if (!isTimerActive) {
        // ================= DURUM 1: TUŞ TAKIMI İLE SÜRE OLUŞTURMA =================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Üst Dijital Zaman Göstergesi (Animasyonlu Sayı Geçişleri)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SAAT
                    val hColor = if (inputDigits.length >= 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    AnimatedContent(
                        targetState = inputHours,
                        transitionSpec = { (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut()) },
                        label = "hours_anim"
                    ) { h ->
                        Text(
                            text = String.format(Locale.getDefault(), "%02d", h),
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                            color = hColor
                        )
                    }
                    Text(
                        text = "sa ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = hColor,
                        modifier = Modifier.padding(top = 10.dp, end = 8.dp)
                    )

                    // DAKİKA
                    val mColor = if (inputDigits.length >= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    AnimatedContent(
                        targetState = inputMinutes,
                        transitionSpec = { (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut()) },
                        label = "mins_anim"
                    ) { m ->
                        Text(
                            text = String.format(Locale.getDefault(), "%02d", m),
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                            color = mColor
                        )
                    }
                    Text(
                        text = "d ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = mColor,
                        modifier = Modifier.padding(top = 10.dp, end = 8.dp)
                    )

                    // SANİYE
                    val sColor = if (inputDigits.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    AnimatedContent(
                        targetState = inputSeconds,
                        transitionSpec = { (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut()) },
                        label = "secs_anim"
                    ) { s ->
                        Text(
                            text = String.format(Locale.getDefault(), "%02d", s),
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                            color = sColor
                        )
                    }
                    Text(
                        text = "s",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = sColor,
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    if (inputDigits.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                inputDigits = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Temizle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hızlı Süre Önayar Çipleri
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val quickPresets = listOf(
                    1 to "+1 dk",
                    5 to "+5 dk",
                    10 to "+10 dk",
                    15 to "+15 dk",
                    25 to "+25 dk (Pomodoro)",
                    45 to "+45 dk (Ders)",
                    90 to "+90 dk (Etüt)"
                )
                items(quickPresets) { (mins, label) ->
                    ElevatedFilterChip(
                        selected = false,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val targetSecs = mins * 60
                            totalTimerSeconds = targetSecs
                            secondsRemaining = targetSecs
                            isTimerActive = true
                            isRunning = true
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google Clock 3x4 Sayı Tuş Takımı (Titreşim ve Yay Animasyonlu Tuşlar)
            val keypadRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("00", "0", "BACKSPACE")
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                keypadRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            if (key == "BACKSPACE") {
                                AnimatedKeypadButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (inputDigits.isNotEmpty()) {
                                            inputDigits = inputDigits.dropLast(1)
                                        }
                                    },
                                    isTransparent = true
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Geri Al",
                                        tint = if (inputDigits.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            } else {
                                AnimatedKeypadButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (key == "00") {
                                            if (inputDigits.isNotEmpty() && inputDigits.length <= 4) {
                                                inputDigits += "00"
                                            }
                                        } else {
                                            if (inputDigits.length < 6) {
                                                if (inputDigits.isNotEmpty() || key != "0") {
                                                    inputDigits += key
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Başlat FAB (Titreşim ve Yay Animasyonlu)
            val startFabInteraction = remember { MutableInteractionSource() }
            val isStartPressed by startFabInteraction.collectIsPressedAsState()
            val startFabScale by animateFloatAsState(
                targetValue = if (isStartPressed) 0.88f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "start_fab_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = {
                        if (calculatedTotalSeconds > 0) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            totalTimerSeconds = calculatedTotalSeconds
                            secondsRemaining = calculatedTotalSeconds
                            isTimerActive = true
                            isRunning = true
                            inputDigits = ""
                        }
                    },
                    shape = CircleShape,
                    containerColor = if (calculatedTotalSeconds > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = if (calculatedTotalSeconds > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = if (calculatedTotalSeconds > 0) 4.dp else 0.dp),
                    interactionSource = startFabInteraction,
                    modifier = Modifier
                        .size(72.dp)
                        .scale(startFabScale)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Başlat",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    } else {
        // ================= DURUM 2: GERİ SAYIM EKRANI =================
        val progress = if (totalTimerSeconds > 0) {
            secondsRemaining.toFloat() / totalTimerSeconds.toFloat()
        } else 0f

        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            label = "timer_progress"
        )

        val remHours = secondsRemaining / 3600
        val remMins = (secondsRemaining % 3600) / 60
        val remSecs = secondsRemaining % 60

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Google Clock Dairesel İlerleme Kadranı
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (secondsRemaining == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (remHours > 0) {
                            String.format(Locale.getDefault(), "%02d:%02d:%02d", remHours, remMins, remSecs)
                        } else {
                            String.format(Locale.getDefault(), "%02d:%02d", remMins, remSecs)
                        },
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = if (remHours > 0) 36.sp else 46.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (secondsRemaining == 0) "🔔 SÜRE DOLDU!" else if (isRunning) "Kalan Süre" else "Durduruldu",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (secondsRemaining == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Google Saat Zamanlayıcı Kontrol Butonları (+1:00 | Play/Pause | Sil)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 96.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SOL: +1:00 Dk Ekleme Butonu (Yay animasyonlu)
                val plusOneInteraction = remember { MutableInteractionSource() }
                val isPlusPressed by plusOneInteraction.collectIsPressedAsState()
                val plusScale by animateFloatAsState(
                    targetValue = if (isPlusPressed) 0.9f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "plus_scale"
                )

                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        secondsRemaining += 60
                        totalTimerSeconds += 60
                    },
                    shape = RoundedCornerShape(16.dp),
                    interactionSource = plusOneInteraction,
                    modifier = Modifier
                        .height(56.dp)
                        .scale(plusScale)
                ) {
                    Text(
                        text = "+1:00",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // ORTA: Büyük FAB (Durdur / Devam Et)
                val timerFabInteraction = remember { MutableInteractionSource() }
                val isTimerFabPressed by timerFabInteraction.collectIsPressedAsState()
                val timerFabScale by animateFloatAsState(
                    targetValue = if (isTimerFabPressed) 0.88f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "timer_fab_scale"
                )

                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (secondsRemaining == 0) {
                            secondsRemaining = totalTimerSeconds
                            isRunning = true
                        } else {
                            isRunning = !isRunning
                        }
                    },
                    shape = CircleShape,
                    containerColor = if (isRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                    contentColor = if (isRunning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    interactionSource = timerFabInteraction,
                    modifier = Modifier
                        .size(76.dp)
                        .scale(timerFabScale)
                ) {
                    AnimatedContent(
                        targetState = isRunning,
                        transitionSpec = { (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut()) },
                        label = "timer_play_pause"
                    ) { running ->
                        Icon(
                            imageVector = if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (running) "Durdur" else "Başlat",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // SAĞ: Sıfırla / İptal Et (Keypad ekranına döner)
                AnimatedPressIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isRunning = false
                        isTimerActive = false
                        secondsRemaining = 0
                        totalTimerSeconds = 0
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.error,
                    size = 56.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Zamanlayıcıyı Sil",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * 3. GOOGLE SAAT TARZI POMODORO ODAKLANMA BÖLÜMÜ
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GooglePomodoroContent(
    onNotify: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var focusMinutes by rememberSaveable { mutableIntStateOf(25) }
    var shortBreakMinutes by rememberSaveable { mutableIntStateOf(5) }
    var longBreakMinutes by rememberSaveable { mutableIntStateOf(15) }

    var currentMode by rememberSaveable { mutableStateOf(PomodoroMode.FOCUS) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var secondsLeft by rememberSaveable { mutableIntStateOf(focusMinutes * 60) }
    var totalSecondsForCurrentMode by rememberSaveable { mutableIntStateOf(focusMinutes * 60) }
    var completedPomodorosCount by rememberSaveable { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    fun playAlarmSound() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 1000)
        } catch (_: Throwable) {}
    }

    LaunchedEffect(isRunning, secondsLeft) {
        if (isRunning && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        } else if (isRunning && secondsLeft == 0) {
            isRunning = false
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            playAlarmSound()

            if (currentMode == PomodoroMode.FOCUS) {
                val newCount = completedPomodorosCount + 1
                completedPomodorosCount = newCount
                val msg = "Tebrikler! 🎯 $newCount. Pomodoro seansını tamamladın!"
                onNotify(msg)

                if (newCount % 4 == 0) {
                    currentMode = PomodoroMode.LONG_BREAK
                    secondsLeft = longBreakMinutes * 60
                    totalSecondsForCurrentMode = secondsLeft
                } else {
                    currentMode = PomodoroMode.SHORT_BREAK
                    secondsLeft = shortBreakMinutes * 60
                    totalSecondsForCurrentMode = secondsLeft
                }
            } else {
                onNotify("Mola bitti! Yeni odaklanma seansına hazır mısın? 🔥")
                currentMode = PomodoroMode.FOCUS
                secondsLeft = focusMinutes * 60
                totalSecondsForCurrentMode = secondsLeft
            }
        }
    }

    val progress = if (totalSecondsForCurrentMode > 0) {
        (totalSecondsForCurrentMode - secondsLeft).toFloat() / totalSecondsForCurrentMode.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "pomodoro_progress"
    )

    val modeThemeColor = when (currentMode) {
        PomodoroMode.FOCUS -> MaterialTheme.colorScheme.primary
        PomodoroMode.SHORT_BREAK -> MaterialTheme.colorScheme.tertiary
        PomodoroMode.LONG_BREAK -> MaterialTheme.colorScheme.secondary
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Material 3 Mod Değiştirme Segmented Buttons
        item {
            val modes = PomodoroMode.values()
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                modes.forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentMode = mode
                            isRunning = false
                            val targetMinutes = when (mode) {
                                PomodoroMode.FOCUS -> focusMinutes
                                PomodoroMode.SHORT_BREAK -> shortBreakMinutes
                                PomodoroMode.LONG_BREAK -> longBreakMinutes
                            }
                            secondsLeft = targetMinutes * 60
                            totalSecondsForCurrentMode = secondsLeft
                        },
                        selected = currentMode == mode,
                        label = { Text(mode.title, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
        }

        // Büyük Dairesel Kadran
        item {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = modeThemeColor,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val minutes = secondsLeft / 60
                    val seconds = secondsLeft % 60
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 46.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentMode.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = modeThemeColor
                    )
                }
            }
        }

        // Kontrol Butonları (Google Saat Tarzı)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sıfırla (Yay Animasyonlu)
                AnimatedPressIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isRunning = false
                        val targetMinutes = when (currentMode) {
                            PomodoroMode.FOCUS -> focusMinutes
                            PomodoroMode.SHORT_BREAK -> shortBreakMinutes
                            PomodoroMode.LONG_BREAK -> longBreakMinutes
                        }
                        secondsLeft = targetMinutes * 60
                        totalSecondsForCurrentMode = secondsLeft
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    size = 56.dp
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sıfırla")
                }

                // Ana Başlat / Duraklat FAB (Yay Animasyonlu)
                val pomodoroFabInteraction = remember { MutableInteractionSource() }
                val isPomodoroFabPressed by pomodoroFabInteraction.collectIsPressedAsState()
                val pomodoroFabScale by animateFloatAsState(
                    targetValue = if (isPomodoroFabPressed) 0.88f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "pomodoro_fab_scale"
                )

                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isRunning = !isRunning
                    },
                    shape = CircleShape,
                    containerColor = if (isRunning) MaterialTheme.colorScheme.errorContainer else modeThemeColor,
                    contentColor = if (isRunning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    interactionSource = pomodoroFabInteraction,
                    modifier = Modifier
                        .size(76.dp)
                        .scale(pomodoroFabScale)
                ) {
                    AnimatedContent(
                        targetState = isRunning,
                        transitionSpec = { (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut()) },
                        label = "pomodoro_play_pause"
                    ) { running ->
                        Icon(
                            imageVector = if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (running) "Durdur" else "Başlat",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Ayarlar (Yay Animasyonlu)
                AnimatedPressIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showSettingsDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    size = 56.dp
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Ayarlar")
                }
            }
        }

        // Günlük Odaklanma Kartı
        item {
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Günlük Odaklanma",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = "$completedPomodorosCount Pomodoro",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4'lü Döngü Noktaları
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val cycleIndex = completedPomodorosCount % 4
                        for (i in 0 until 4) {
                            val isDone = i < cycleIndex || (cycleIndex == 0 && completedPomodorosCount > 0)
                            val isCurrent = i == cycleIndex
                            Surface(
                                shape = CircleShape,
                                color = if (isDone) MaterialTheme.colorScheme.primary else if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.size(22.dp)
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Her 4 seans tamamlandığında 15 dakikalık uzun mola hakkı kazanırsınız.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showSettingsDialog) {
        var tempFocus by remember { mutableIntStateOf(focusMinutes) }
        var tempShort by remember { mutableIntStateOf(shortBreakMinutes) }
        var tempLong by remember { mutableIntStateOf(longBreakMinutes) }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "Pomodoro Sürelerini Özelleştir",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column {
                        Text("Odaklanma Süresi: $tempFocus dakika", style = MaterialTheme.typography.labelLarge)
                        Slider(
                            value = tempFocus.toFloat(),
                            onValueChange = { tempFocus = it.toInt() },
                            valueRange = 10f..60f,
                            steps = 9
                        )
                    }

                    Column {
                        Text("Kısa Mola: $tempShort dakika", style = MaterialTheme.typography.labelLarge)
                        Slider(
                            value = tempShort.toFloat(),
                            onValueChange = { tempShort = it.toInt() },
                            valueRange = 1f..15f,
                            steps = 13
                        )
                    }

                    Column {
                        Text("Uzun Mola: $tempLong dakika", style = MaterialTheme.typography.labelLarge)
                        Slider(
                            value = tempLong.toFloat(),
                            onValueChange = { tempLong = it.toInt() },
                            valueRange = 10f..30f,
                            steps = 3
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    focusMinutes = tempFocus
                    shortBreakMinutes = tempShort
                    longBreakMinutes = tempLong
                    val targetMinutes = when (currentMode) {
                        PomodoroMode.FOCUS -> focusMinutes
                        PomodoroMode.SHORT_BREAK -> shortBreakMinutes
                        PomodoroMode.LONG_BREAK -> longBreakMinutes
                    }
                    secondsLeft = targetMinutes * 60
                    totalSecondsForCurrentMode = secondsLeft
                    showSettingsDialog = false
                }) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

/**
 * Tuşa basıldığında yay (spring bounce) ölçek animasyonu ve renk geri bildirimi veren Keypad tuşu.
 */
@Composable
private fun AnimatedKeypadButton(
    onClick: () -> Unit,
    isTransparent: Boolean = false,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "keypad_btn_scale"
    )

    val animatedColor by animateColorAsState(
        targetValue = when {
            isTransparent -> Color.Transparent
            isPressed -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "keypad_btn_color"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(68.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(animatedColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            )
    ) {
        content()
    }
}

/**
 * Tuşa basıldığında yay (spring bounce) ölçek animasyonu veren M3 İkon Butonu.
 */
@Composable
private fun AnimatedPressIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    size: androidx.compose.ui.unit.Dp = 56.dp,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.88f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "icon_btn_scale"
    )

    val effectiveContainer = if (enabled) containerColor else MaterialTheme.colorScheme.surfaceContainerLowest
    val effectiveContent = if (enabled) contentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(effectiveContainer)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            )
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides effectiveContent
        ) {
            content()
        }
    }
}
