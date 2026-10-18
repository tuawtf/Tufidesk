package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StreakDay
import com.example.data.model.StreakState

/**
 * Tema bağımsız sabit sıcak turuncu alev renk paleti.
 */
object StreakColors {
    val LightFlame = Color(0xFFE65100)
    val LightContainer = Color(0xFFFFDCC2)
    val LightOnContainer = Color(0xFF5B2C00)

    val DarkFlame = Color(0xFFFFB784)
    val DarkContainer = Color(0xFF5B2C00)
    val DarkOnContainer = Color(0xFFFFDCC2)

    @Composable
    fun flameColor(): Color = if (isSystemInDarkTheme()) DarkFlame else LightFlame

    @Composable
    fun containerColor(): Color = if (isSystemInDarkTheme()) DarkContainer else LightContainer

    @Composable
    fun onContainerColor(): Color = if (isSystemInDarkTheme()) DarkOnContainer else LightOnContainer
}

/**
 * Üst çubukta (TopAppBar) gösterilen küçük seri hapı/rozet (pill/chip).
 */
@Composable
fun StreakBadge(
    streak: StreakState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAnimating: Boolean = false
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            scale.animateTo(
                targetValue = 1.35f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            )
        }
    }

    val activeFlame = StreakColors.flameColor()
    val activeContainer = StreakColors.containerColor()
    val activeOnContainer = StreakColors.onContainerColor()

    // 3 Durum:
    // 1. Bugün tamamlandı (turuncu kap)
    // 2. Risk altında (seri > 0 ama bugün henüz tamamlanmadı - outline)
    // 3. Seri bitti / 0 (nötr gri kap & outlined alev)
    val containerBg: Color
    val contentColor: Color
    val iconVector = if (streak.current > 0) Icons.Default.LocalFireDepartment else Icons.Outlined.LocalFireDepartment
    val borderStroke: BorderStroke?

    when {
        streak.todayDone -> {
            containerBg = activeContainer
            contentColor = activeOnContainer
            borderStroke = null
        }
        streak.atRisk -> {
            containerBg = MaterialTheme.colorScheme.surface
            contentColor = MaterialTheme.colorScheme.onSurface
            borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        }
        else -> {
            containerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }

    Surface(
        modifier = modifier
            .scale(scale.value)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("streak_badge_button"),
        shape = RoundedCornerShape(20.dp),
        color = containerBg,
        border = borderStroke,
        tonalElevation = if (streak.todayDone) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = "Günlük Seri",
                tint = if (streak.todayDone) activeOnContainer else if (streak.atRisk) activeFlame else contentColor,
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = "${streak.current}",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = contentColor
            )
        }
    }
}

/**
 * Seri detay içeriği (Sheet veya Dialog içinde kullanılır).
 */
@Composable
fun StreakDetailContent(
    streak: StreakState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flameColor = StreakColors.flameColor()
    val containerColor = StreakColors.containerColor()
    val onContainerColor = StreakColors.onContainerColor()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Büyük Alev İkonu
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (streak.current > 0) Icons.Default.LocalFireDepartment else Icons.Outlined.LocalFireDepartment,
                contentDescription = null,
                tint = if (streak.current > 0) onContainerColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // X günlük seri başlığı
        Text(
            text = "${streak.current} Günlük Seri",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // En uzun seri bilgisi
        Text(
            text = "En uzun seri: ${streak.longest} gün",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Haftalık Çubuk (P S Ç P C C P)
        WeeklyStreakRow(
            days = streak.last7Days,
            flameColor = flameColor,
            containerColor = containerColor,
            onContainerColor = onContainerColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Yönlendirme Metni Kartı
        val guidanceMessage = when {
            streak.todayDone -> "Bugün tamam, yarın da devam et! Harika gidiyorsun."
            streak.atRisk -> "Seriyi korumak için bugün en az bir ödev tamamla!"
            else -> "Seriyi başlatmak için bugün en az bir ödev tamamla!"
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (streak.todayDone) {
                    containerColor.copy(alpha = 0.5f)
                } else if (streak.atRisk) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (streak.todayDone) Icons.Default.LocalFireDepartment else Icons.Outlined.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (streak.todayDone) onContainerColor else if (streak.atRisk) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = guidanceMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tamam / Kapat butonu
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("streak_detail_dismiss_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Tamam",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Haftalık 7 günlük gösterge satırı.
 */
@Composable
private fun WeeklyStreakRow(
    days: List<StreakDay>,
    flameColor: Color,
    containerColor: Color,
    onContainerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEach { day ->
                DayStreakItem(
                    day = day,
                    flameColor = flameColor,
                    containerColor = containerColor,
                    onContainerColor = onContainerColor
                )
            }
        }
    }
}

@Composable
private fun DayStreakItem(
    day: StreakDay,
    flameColor: Color,
    containerColor: Color,
    onContainerColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Gün etiketi: P, S, Ç, P, C, C, P
        Text(
            text = day.dayLabel,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Daire Göstergesi
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .then(
                    when {
                        day.isCompleted -> Modifier.background(containerColor)
                        day.isToday -> Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .border(BorderStroke(2.dp, flameColor), CircleShape)
                        day.isFuture -> Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CircleShape)
                        else -> Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)), CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                day.isCompleted -> {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Tamamlandı",
                        tint = onContainerColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                day.isToday -> {
                    Icon(
                        imageVector = Icons.Outlined.LocalFireDepartment,
                        contentDescription = "Bugün",
                        tint = flameColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
                day.isFuture -> {
                    // Gelecek günler boş daire
                }
                else -> {
                    // Geçmiş tamamlanmamış günler
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }

        // Gün numarası
        Text(
            text = "${day.dayNumber}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

/**
 * Telefon ekranı için ModalBottomSheet wrapper.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakDetailBottomSheet(
    streak: StreakState,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        StreakDetailContent(
            streak = streak,
            onDismiss = onDismiss
        )
    }
}

/**
 * Tablet ekranı için ortalanmış Dialog wrapper.
 */
@Composable
fun StreakDetailDialog(
    streak: StreakState,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.widthIn(max = 480.dp)
        ) {
            StreakDetailContent(
                streak = streak,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * 7, 30, 100 ve 365. günlerde açılan kutlama AlertDialog'u.
 */
@Composable
fun StreakCelebrationDialog(
    milestoneDays: Int,
    onDismiss: () -> Unit
) {
    val containerColor = StreakColors.containerColor()
    val onContainerColor = StreakColors.onContainerColor()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = onContainerColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        title = {
            Text(
                text = "Tebrikler! 🎉",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "$milestoneDays günlük seriye ulaştın! Muhteşem bir disiplin ve kararlılık sergiliyorsun. Aynen böyle devam et!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("celebration_confirm_button")
            ) {
                Text(
                    text = "Harika!",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    )
}
