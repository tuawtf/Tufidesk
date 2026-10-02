package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
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
 * Bildirim geldiğinde titreşimi tetikler; eğer ekranda zaten bir ada açıksa
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
        // Her bildirim geldiğinde cihaz titreşimi
        if (context != null) {
            triggerHapticFeedback(context, view)
        }

        dismissJob?.cancel()

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
 * Dinamik Ada Bileşeni:
 * - Ön kamera deliğini tespit eder (ekranın üst kısmında o noktadan başlar).
 * - İlk açılışta kamera etrafında 26x26dp siyah bir daire olarak başlar.
 * - Daireden yanlara doğru akıcı bir şekilde genişleyerek kenarları yuvarlatılmış dikdörtgene dönüşür.
 * - Asla tüm ekran enini kaplamaz (~290dp ile sınırlıdır).
 * - Parlama (glow/neon) yoktur; sade, mat ve Material 3 temasıyla uyumlu yüzey ve ikon renklerine sahiptir.
 * - Halihazırda açıkken yeni bildirim gelirse ada kapanıp açılmaz; yeni bildirim içeriği animasyonsuz anında güncellenir.
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

    // Ön kamera deliğinin yatay merkez oranı (0.0 sol, 0.5 orta, 1.0 sağ)
    var cutoutPivotX by remember { mutableFloatStateOf(0.5f) }
    var cutoutTopDp by remember { mutableStateOf(8.dp) }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LaunchedEffect(view, view.width) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val rootInsets = view.rootWindowInsets
            val cutout = rootInsets?.displayCutout
            val rects = cutout?.boundingRects
            val topCutout = rects?.firstOrNull { it.top <= 140 }
            if (topCutout != null && view.width > 0) {
                val cutoutCenterX = (topCutout.left + topCutout.right) / 2f
                cutoutPivotX = (cutoutCenterX / view.width).coerceIn(0.15f, 0.85f)
                cutoutTopDp = with(density) { maxOf(topCutout.top.toDp(), 6.dp) }
            } else {
                cutoutPivotX = 0.5f
                cutoutTopDp = maxOf(statusBarTop / 2, 8.dp)
            }
        } else {
            cutoutPivotX = 0.5f
            cutoutTopDp = maxOf(statusBarTop / 2, 8.dp)
        }
    }

    // Açılma ilerlemesi: 0.0f = Kamera deliğinde küçük 26dp daire, 1.0f = 290dp x 48dp genişletilmiş ada
    val animProgress = remember { Animatable(0f) }
    var displayNotif by remember { mutableStateOf<IslandNotification?>(null) }

    LaunchedEffect(currentNotif) {
        if (currentNotif != null) {
            displayNotif = currentNotif
            // Eğer henüz açık değilse, kamera deliğinden büyüme animasyonunu başlat
            if (animProgress.value < 0.2f) {
                animProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
                )
            }
            // Ada zaten 1f'te açıksa: animasyon baştan başlamaz, yeni içerik direkt gösterilir!
        } else {
            if (animProgress.value > 0f) {
                // Kapanırken daireye doğru küçülerek yok olma
                animProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                )
                displayNotif = null
            }
        }
    }

    val progress = animProgress.value
    if (progress > 0.001f) {
        val active = displayNotif ?: return

        // Daireden (26dp x 26dp) yumuşatılmış dikdörtgen kapsüle (290dp x 48dp) fiziksel büyüme
        val targetWidth = 290.dp
        val targetHeight = 48.dp
        val currentWidth = lerp(26.dp, targetWidth, progress)
        val currentHeight = lerp(26.dp, targetHeight, progress)
        val cornerRadius = lerp(currentHeight / 2, 24.dp, progress)

        // İçerik (metin ve simgeler) ada yeterince genişledikten sonra görünür
        val contentAlpha = ((progress - 0.40f) / 0.60f).coerceIn(0f, 1f)

        // Material 3 renkleri (Parlama/Glow yok, sade ve net temayla uyumlu)
        val (typeIcon, typeColor, typeBg) = when (active.type) {
            IslandNotificationType.SUCCESS -> Triple(
                Icons.Default.CheckCircle,
                Color(0xFF34D399), // Temiz yeşil
                Color(0xFF065F46)
            )
            IslandNotificationType.INFO -> Triple(
                Icons.Default.Info,
                Color(0xFF60A5FA), // Temiz mavi
                Color(0xFF1E3A8A)
            )
            IslandNotificationType.WARNING -> Triple(
                Icons.Default.Warning,
                Color(0xFFFBBF24), // Temiz sarı/kehribar
                Color(0xFF78350F)
            )
            IslandNotificationType.ERROR -> Triple(
                Icons.Default.Error,
                Color(0xFFF87171), // Temiz kırmızı
                Color(0xFF7F1D1D)
            )
        }

        // Kamera deliği eksenine göre yatay hizalama (-1f sol, 0f orta, 1f sağ)
        val horizontalBias = ((cutoutPivotX * 2f) - 1f).coerceIn(-0.8f, 0.8f)

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = cutoutTopDp, start = 12.dp, end = 12.dp),
            contentAlignment = BiasAlignment(horizontalBias, -1f)
        ) {
            Box(
                modifier = Modifier
                    .width(currentWidth)
                    .height(currentHeight)
                    // Sade, doğal Material gölge (neon/parlama rengi kesinlikle yok)
                    .shadow(
                        elevation = (4 * progress).dp,
                        shape = RoundedCornerShape(cornerRadius),
                        clip = false
                    )
                    .clip(RoundedCornerShape(cornerRadius))
                    // Sade mat siyah gövde (kamera merceğiyle bütünleşen mat OLED koyusu)
                    .background(Color(0xFF1A1A1A))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .clickable { controller.dismiss() }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Ada içindeki içerik: Daire genişledikçe yumuşakça belirir
                if (contentAlpha > 0.05f) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(contentAlpha),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Sol: İkon Rozeti
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(typeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Orta: Metin Bilgisi
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
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Sağ: Sade durum noktası
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(typeColor.copy(alpha = 0.8f))
                        )
                    }
                }
            }
        }
    }
}
