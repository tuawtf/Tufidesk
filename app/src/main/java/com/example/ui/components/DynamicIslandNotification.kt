package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class IslandNotificationType {
    SUCCESS,
    INFO,
    WARNING,
    ERROR
}

data class IslandNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val title: String? = null,
    val type: IslandNotificationType = IslandNotificationType.SUCCESS,
    val durationMillis: Long = 2200L
)

/**
 * Dinamik Ada bildirim kontrolcüsü.
 * Yeni bildirim geldiğinde titreşimi tetikler, eğer ekranda zaten bir bildirim varsa
 * yeni bildirimi animasyonsuz anında değiştirir ve zamanlayıcıyı sıfırlar.
 */
class DynamicIslandController {
    private val _notification = MutableStateFlow<IslandNotification?>(null)
    val notification = _notification.asStateFlow()

    private var dismissJob: Job? = null

    fun show(
        message: String,
        title: String? = null,
        type: IslandNotificationType = IslandNotificationType.SUCCESS,
        durationMillis: Long = 2200L,
        scope: CoroutineScope,
        context: Context? = null,
        view: View? = null
    ) {
        // Her bildirim geldiğinde anında titreşim çalıştır
        if (context != null) {
            triggerHapticFeedback(context, view)
        }

        dismissJob?.cancel()

        // Bildirim nesnesini ata
        _notification.value = IslandNotification(
            id = System.currentTimeMillis(),
            message = message,
            title = title,
            type = type,
            durationMillis = durationMillis
        )

        // Bildirimler arası süreyi kısa tutarak otomatik kapatma
        dismissJob = scope.launch {
            delay(durationMillis)
            _notification.value = null
        }
    }

    fun dismiss() {
        dismissJob?.cancel()
        _notification.value = null
    }

    private fun triggerHapticFeedback(context: Context, view: View?) {
        try {
            view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } catch (_: Exception) {}

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(45L)
                }
            }
        } catch (e: Exception) {
            Log.w("DynamicIsland", "Titreşim hatası: ${e.message}")
        }
    }
}

/**
 * Ekranın üst kısmında kamera deliği/çentiği noktasından genişleyen modern Dinamik Ada bileşeni.
 * - Kamera yerini tespit eder (Cutout center).
 * - Siyah noktadan yanlara doğru animasyonla açılır.
 * - Ekranın tamamını asla kaplamaz (genişlik sınırlıdır).
 * - Material temayla uyumlu renk ve simgeler içerir (Başarılı, Bilgi, Uyarı, Hata).
 * - Bildirim zaten açıkken yenisi gelirse içerik hemen güncellenir (animasyon tekrar baştan başlamaz).
 */
@Composable
fun DynamicIslandOverlay(
    controller: DynamicIslandController,
    modifier: Modifier = Modifier
) {
    val currentNotif by controller.notification.collectAsState()
    val isVisible = currentNotif != null

    val view = LocalView.current
    val density = LocalDensity.current

    // Kamera deliği tespit mantığı (X ekseninde yatay merkez oranı: 0.0 sol, 0.5 orta, 1.0 sağ)
    var cutoutPivotX by remember { mutableFloatStateOf(0.5f) }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LaunchedEffect(view, view.width) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val rootInsets = view.rootWindowInsets
            val cutout = rootInsets?.displayCutout
            val rects = cutout?.boundingRects
            val topCutout = rects?.firstOrNull { it.top <= 120 }
            if (topCutout != null && view.width > 0) {
                val cutoutCenterX = (topCutout.left + topCutout.right) / 2f
                cutoutPivotX = (cutoutCenterX / view.width).coerceIn(0.15f, 0.85f)
            } else {
                cutoutPivotX = 0.5f
            }
        } else {
            cutoutPivotX = 0.5f
        }
    }

    // İlk açılış animasyonu: Kamera deliğinden yanlara doğru yayılma
    val expandProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = if (isVisible) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        } else {
            tween(durationMillis = 180)
        },
        label = "IslandExpandAnimation"
    )

    if (expandProgress > 0.01f || isVisible) {
        // En son geçerli bildirimi saklayarak kapanış anında metnin kaybolmasını engelle
        var displayNotif by remember { mutableStateOf<IslandNotification?>(null) }
        LaunchedEffect(currentNotif) {
            if (currentNotif != null) {
                displayNotif = currentNotif
            }
        }

        val active = displayNotif ?: return

        val (typeIcon, typeColor, typeBg) = when (active.type) {
            IslandNotificationType.SUCCESS -> Triple(
                Icons.Default.CheckCircle,
                Color(0xFF10B981), // Emerald 500
                Color(0xFF064E3B)
            )
            IslandNotificationType.INFO -> Triple(
                Icons.Default.Info,
                Color(0xFF38BDF8), // Sky 400
                Color(0xFF0C4A6E)
            )
            IslandNotificationType.WARNING -> Triple(
                Icons.Default.Warning,
                Color(0xFFF59E0B), // Amber 500
                Color(0xFF78350F)
            )
            IslandNotificationType.ERROR -> Triple(
                Icons.Default.Error,
                Color(0xFFEF4444), // Red 500
                Color(0xFF7F1D1D)
            )
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = maxOf(statusBarTop, 8.dp), start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        // Kamera deliği merkezinden açılma
                        transformOrigin = TransformOrigin(cutoutPivotX, 0f)
                        scaleX = 0.15f + (0.85f * expandProgress)
                        scaleY = 0.2f + (0.8f * expandProgress)
                        alpha = expandProgress.coerceIn(0f, 1f)
                    }
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = typeColor.copy(alpha = 0.45f),
                        ambientColor = Color.Black
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F172A), // Midnight Dark
                                Color(0xFF020617)  // Deep OLED Obsidian
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                typeColor.copy(alpha = 0.7f),
                                typeColor.copy(alpha = 0.25f),
                                typeColor.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .clickable { controller.dismiss() }
                    // ASLA TÜM EKRANI KAPLAMAZ: min 200dp, max 340dp
                    .widthIn(min = 200.dp, max = 340.dp)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Sol: Renkli İkon Rozeti
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(typeBg.copy(alpha = 0.6f))
                            .border(1.dp, typeColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Orta: Başlık ve Mesaj
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (!active.title.isNullOrBlank()) {
                            Text(
                                text = active.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = typeColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = active.message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sağ: Dinamik Ada küçük gösterge noktası
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(typeColor)
                    )
                }
            }
        }
    }
}
