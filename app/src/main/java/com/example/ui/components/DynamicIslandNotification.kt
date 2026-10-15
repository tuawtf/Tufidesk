package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
    val durationMillis: Long = 2500L
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
        durationMillis: Long = 2500L,
        scope: CoroutineScope,
        context: Context? = null,
        view: View? = null
    ) {
        // Her bildirim geldiğinde titreşim
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
 * - Uyarı geldiğinde Status Bar gizlenir, çakışma önlenir; bildirim kapandığında Status Bar geri açılır.
 * - Boyutları Bottom Bar (Ada Menü) ile tam aynı boyutta tasarlanmıştır (64dp yükseklik, kenar boşlukları 16dp, yuvarlatılmış 26dp köşe).
 * - Ön kamera noktasından daire olarak doğup aşağı ve yanlara doğru pürüzsüzce genişler.
 * - Material 3 tik işareti ve dinamik durum rozeti içerir.
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
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }

    // Ön kamera deliğinin yatay merkez oranı (0.0 sol, 0.5 orta, 1.0 sağ)
    var cutoutPivotX by remember { mutableFloatStateOf(0.5f) }
    var cutoutTopDp by remember { mutableStateOf(8.dp) }

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
                cutoutTopDp = 8.dp
            }
        } else {
            cutoutPivotX = 0.5f
            cutoutTopDp = 8.dp
        }
    }

    // Açılma ilerlemesi: Yavaş ve akıcı animasyon (580ms)
    val animProgress = remember { Animatable(0f) }
    var displayNotif by remember { mutableStateOf<IslandNotification?>(null) }

    LaunchedEffect(currentNotif) {
        if (currentNotif != null) {
            displayNotif = currentNotif
            // Henüz açık değilse kamera noktasından yavaş ve akıcı büyüme animasyonu
            if (animProgress.value < 0.2f) {
                animProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 580, easing = FastOutSlowInEasing)
                )
            }
            // Ada zaten 1f'te açıksa: animasyon baştan başlamaz, yeni içerik direkt gösterilir!
        } else {
            if (animProgress.value > 0f) {
                // Kapanırken daireye doğru küçülerek yok olma
                animProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 260, easing = FastOutLinearInEasing)
                )
                displayNotif = null
            }
        }
    }

    val isTablet = configuration.screenWidthDp >= 600 || configuration.smallestScreenWidthDp >= 600

    // Uyarı geldiğinde status bar'ı otomatik gizle (telefonlar için); tabletler için devre dışı bırak
    val isIslandActive = isVisible || animProgress.value > 0.001f
    DisposableEffect(isIslandActive, isTablet, activity) {
        if (!isTablet) {
            activity?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                if (isIslandActive) {
                    insetsController.hide(WindowInsetsCompat.Type.statusBars())
                    insetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    insetsController.show(WindowInsetsCompat.Type.statusBars())
                }
            }
        }
        onDispose {
            if (!isTablet) {
                activity?.window?.let { window ->
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.show(WindowInsetsCompat.Type.statusBars())
                }
            }
        }
    }

    val progress = animProgress.value
    if (progress > 0.001f) {
        val active = displayNotif ?: return

        // Bottom Bar (Ada Menü) ile tam aynı boyutlandırma:
        // Kenarlardan 16dp + 16dp = 32dp boşluk, yükseklik 64dp, köşe yuvarlatma 26dp
        val screenWidth = configuration.screenWidthDp.dp
        val targetWidth = minOf(screenWidth - 32.dp, 560.dp)
        val targetHeight = 64.dp

        val currentWidth = lerp(28.dp, targetWidth, progress)
        val currentHeight = lerp(28.dp, targetHeight, progress)
        val cornerRadius = lerp(currentHeight / 2, 26.dp, progress)

        // İçerik (metin ve simgeler) ada yeterince genişledikten sonra görünür
        val contentAlpha = ((progress - 0.32f) / 0.68f).coerceIn(0f, 1f)

        // Dinamik tik işareti ve ikon yaylı büyüme animasyonu (spring bounce)
        val iconScale by animateFloatAsState(
            targetValue = if (contentAlpha > 0.25f) 1f else 0.2f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "islandIconScale"
        )

        // Material 3 temasıyla tam uyumlu renkler ve ikonlar
        val style = when (active.type) {
            IslandNotificationType.SUCCESS -> IslandTypeStyle(
                Icons.Default.Check,
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primaryContainer,
                "Tamamlandı"
            )
            IslandNotificationType.INFO -> IslandTypeStyle(
                Icons.Default.Info,
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.tertiaryContainer,
                "Bilgi"
            )
            IslandNotificationType.WARNING -> IslandTypeStyle(
                Icons.Default.Warning,
                MaterialTheme.colorScheme.error,
                MaterialTheme.colorScheme.errorContainer,
                "Uyarı"
            )
            IslandNotificationType.ERROR -> IslandTypeStyle(
                Icons.Default.Error,
                MaterialTheme.colorScheme.error,
                MaterialTheme.colorScheme.errorContainer,
                "Hata"
            )
        }
        val typeIcon = style.icon
        val typeColor = style.color
        val typeBg = style.bg
        val dynamicBadgeText = style.badgeText

        // Kamera deliği eksenine göre yatay hizalama (-1f sol, 0f orta, 1f sağ)
        val horizontalBias = ((cutoutPivotX * 2f) - 1f).coerceIn(-0.85f, 0.85f)
        val topPadding = if (isTablet) 38.dp else maxOf(cutoutTopDp, 10.dp)

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = topPadding, start = 16.dp, end = 16.dp),
            contentAlignment = BiasAlignment(horizontalBias, -1f)
        ) {
            Box(
                modifier = Modifier
                    .width(currentWidth)
                    .height(currentHeight)
                    // Bottom bar ile aynı doğal gölge ve köşe yapısı
                    .shadow(
                        elevation = (8 * progress).dp,
                        shape = RoundedCornerShape(cornerRadius),
                        clip = false
                    )
                    .clip(RoundedCornerShape(cornerRadius))
                    // Sade mat OLED koyu gövde
                    .background(Color(0xFF1B1B1F))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .clickable { controller.dismiss() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (contentAlpha > 0.05f) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(contentAlpha),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Sol: Dinamik Yaylı İkon Rozeti (Material 3 Tik İşareti)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                                .clip(CircleShape)
                                .background(typeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Orta: Başlık ve Mesaj Bilgisi (Genişletilmiş yükseklik sayesinde daha okunaklı)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (!active.title.isNullOrBlank()) {
                                Text(
                                    text = active.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    ),
                                    color = typeColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = active.message,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Sağ: Dinamik Material 3 Durum Rozeti
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = typeBg.copy(alpha = 0.85f),
                            contentColor = typeColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(typeColor)
                                )
                                Text(
                                    text = dynamicBadgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class IslandTypeStyle(
    val icon: ImageVector,
    val color: Color,
    val bg: Color,
    val badgeText: String
)
