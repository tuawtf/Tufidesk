package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Screen
import com.example.data.ai.TufiAICoachAdvice
import com.example.data.ai.TufiAIService
import com.example.data.ai.TufiChatMessage
import com.example.data.model.Course
import com.example.data.model.HomeworkWithCourse
import com.example.ui.components.IslandBottomNavItem
import com.example.ui.components.TufiAISettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Kullanıcının İstediği Özel Morfizasyon Aşamaları:
 * 1. TABS: Standart alt gezinme adası.
 * 2. STAR_SPINNING: SADECE YILDIZLAR 720 derece yavaş ve genişleyerek döner (yazı kesinlikle dönmez!).
 * 3. STAR_MOVING_LEFT: Yıldız akıcı animasyonla en sola geçer ve 4'LÜ KARE formatını alır.
 * 4. EXPANDING_UP: Bar üste doğru genişler ve "Tufi AI'a sor..." metin kutusu gelir.
 * 5. TEXTBOX: Tam aktif ChatGPT tarzı metin çubuğu.
 */
enum class TufiBarMorphState {
    TABS,
    STAR_SPINNING,
    STAR_MOVING_LEFT,
    EXPANDING_UP,
    TEXTBOX
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float =
    start + fraction * (stop - start)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TufiAIScreen(
    courses: List<Course>,
    homeworks: List<HomeworkWithCourse>,
    navItems: List<Screen> = emptyList(),
    currentTab: Int = 2,
    onTabSelected: (Int) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Chat state
    val chatMessages = remember { mutableStateListOf<TufiChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Menü ve Pencere Durumları
    var showTopMenu by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showActionsSheet by remember { mutableStateOf(false) }
    var showAdviceReportSheet by remember { mutableStateOf(false) }

    // Koçluk Raporu State
    var advice by remember { mutableStateOf<TufiAICoachAdvice?>(null) }
    var isAdviceLoading by remember { mutableStateOf(false) }

    // =========================================================================
    // SİNEMATİK ANİMASYON DURUMLARI
    // =========================================================================
    var morphState by remember { mutableStateOf(TufiBarMorphState.TABS) }
    val starRotation = remember { Animatable(0f) }
    val starScale = remember { Animatable(1f) }
    val starMorphProgress = remember { Animatable(0f) } // 0f = Yıldız, 1f = 4'lü Kare
    val starSlideToLeftFraction = remember { Animatable(0f) } // 0f = Orta/yerinde, 1f = En sol
    val otherTabsAlpha = remember { Animatable(1f) }
    val barHeight = remember { Animatable(52f) }
    val barWidthFraction = remember { Animatable(0.70f) } // 0.70f = Sekmeler boyutu, 1f = Tam genişlik
    val textboxAlpha = remember { Animatable(0f) }

    // Geri tuşu: Textbox'tayken sekme adasına geri döner, sekme adasındayken ana ekrana döner
    fun morphBackToTabs() {
        coroutineScope.launch {
            morphState = TufiBarMorphState.EXPANDING_UP
            textboxAlpha.animateTo(0f, tween(150))
            launch {
                barHeight.animateTo(52f, tween(280))
            }
            launch {
                barWidthFraction.animateTo(0.70f, tween(280))
            }
            delay(140)
            morphState = TufiBarMorphState.STAR_MOVING_LEFT
            launch {
                starMorphProgress.animateTo(0f, tween(300))
            }
            launch {
                starSlideToLeftFraction.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
            }
            delay(350)
            morphState = TufiBarMorphState.TABS
            starRotation.snapTo(0f)
            starScale.snapTo(1f)
            otherTabsAlpha.animateTo(1f, tween(260))
        }
    }

    fun startForwardCinematicAnimation() {
        coroutineScope.launch {
            // Başlangıç değerlerini sıfırla
            morphState = TufiBarMorphState.TABS
            starRotation.snapTo(0f)
            starScale.snapTo(1f)
            starMorphProgress.snapTo(0f)
            starSlideToLeftFraction.snapTo(0f)
            barHeight.snapTo(52f)
            barWidthFraction.snapTo(0.70f)
            otherTabsAlpha.snapTo(1f)
            textboxAlpha.snapTo(0f)

            delay(150)

            // 1. AŞAMA: SADECE YILDIZLAR DÖNÜP GENİŞLEYECEK (Daha yavaş ve akıcı, yazı dönmez!)
            morphState = TufiBarMorphState.STAR_SPINNING
            launch {
                otherTabsAlpha.animateTo(0f, tween(350))
            }
            launch {
                // Yıldız 1.55 katına kadar yavaşça büyür ve hafifçe geri oturur
                starScale.animateTo(1.55f, tween(500, easing = FastOutSlowInEasing))
                starScale.animateTo(1.30f, tween(400, easing = FastOutSlowInEasing))
            }
            // Yıldız 720 derece yavaş ve sinematik döner (900ms)
            starRotation.animateTo(720f, tween(900, easing = FastOutSlowInEasing))

            // 2. AŞAMA: YILDIZ EN SOLA GEÇECEK VE 4'LÜ KARE FORMATINI ALACAK
            morphState = TufiBarMorphState.STAR_MOVING_LEFT
            launch {
                // Sola kayma animasyonu
                starSlideToLeftFraction.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
            }
            launch {
                delay(180)
                // 4'lü kareye dönüşme animasyonu
                starMorphProgress.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
                starScale.animateTo(1.0f, tween(320))
            }
            delay(500)

            // 3. AŞAMA: BAR ÜSTE DOĞRU GENİŞLEYECEK
            morphState = TufiBarMorphState.EXPANDING_UP
            launch {
                barHeight.animateTo(60f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
            }
            launch {
                barWidthFraction.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
            }
            delay(240)

            // 4. AŞAMA: "TUFI AI'A SOR" TEXT BAR GELECEK
            morphState = TufiBarMorphState.TEXTBOX
            textboxAlpha.animateTo(1f, tween(250))
        }
    }

    BackHandler(enabled = morphState == TufiBarMorphState.TEXTBOX) {
        morphBackToTabs()
    }
    BackHandler(enabled = morphState == TufiBarMorphState.TABS) {
        onBack()
    }

    // Ekran açıldığında sinematik animasyonu başlat
    LaunchedEffect(Unit) {
        startForwardCinematicAnimation()
    }

    // TTS (Metin Seslendirme)
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var speechInstance: TextToSpeech? = null
        speechInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                speechInstance?.language = Locale.forLanguageTag("tr-TR")
            }
        }
        tts = speechInstance
        onDispose {
            speechInstance?.stop()
            speechInstance?.shutdown()
        }
    }

    fun speakText(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tufi_tts_${System.currentTimeMillis()}")
    }

    // Sesle Yazma (Speech to Text)
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = spokenText
            }
        }
    }

    fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Tufi AI'a konuşun...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Ses tanıma bu cihazda aktif değil.", Toast.LENGTH_SHORT).show()
        }
    }

    // Sohbet Geçmişini Yükle
    LaunchedEffect(Unit) {
        val loaded = TufiAIService.loadChatHistory(context)
        chatMessages.clear()
        chatMessages.addAll(loaded)
    }

    fun clearChat() {
        chatMessages.clear()
        TufiAIService.clearChatHistory(context)
        Toast.makeText(context, "Yeni sohbet başlatıldı. 🧹", Toast.LENGTH_SHORT).show()
    }

    fun sendMessage(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isBlank() || isGenerating) return

        val normalized = trimmed.lowercase(Locale.forLanguageTag("tr-TR")).trim()
        if (normalized == "sohbeti sil" || normalized == "sohbeti temizle" || normalized == "sohbeti sıfırla") {
            inputText = ""
            clearChat()
            return
        }

        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val userMsg = TufiChatMessage(isUser = true, text = trimmed)
        chatMessages.add(userMsg)
        inputText = ""
        isGenerating = true

        coroutineScope.launch {
            delay(50)
            listState.animateScrollToItem(chatMessages.size - 1)

            val replyText = TufiAIService.askTufiAI(
                context = context,
                question = trimmed,
                history = chatMessages.toList(),
                courses = courses,
                homeworks = homeworks
            )

            val aiMsg = TufiChatMessage(isUser = false, text = replyText)
            chatMessages.add(aiMsg)
            TufiAIService.saveChatHistory(context, chatMessages)
            isGenerating = false

            delay(50)
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Tufi AI", text))
        Toast.makeText(context, "Panoya kopyalandı! 📋", Toast.LENGTH_SHORT).show()
    }

    fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Paylaş"))
    }

    // Klavye & Gezinme Çubuğu Yükseklik Hesaplaması
    val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isKeyboardOpen = imePadding > 0.dp
    val bottomOffset = if (isKeyboardOpen) imePadding + 6.dp else navPadding + 10.dp
    val listBottomPadding = if (isKeyboardOpen) (imePadding + 76.dp) else (navPadding + 84.dp)

    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen && chatMessages.isNotEmpty()) {
            delay(120)
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // AMOLED Pitch Black ChatGPT Teması
            .testTag("tufi_ai_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==========================================
            // CHATGPT ÜST BAŞLIK (Header)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol: İki Çizgili Menü / Geri Butonu (=)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF212121))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (morphState == TufiBarMorphState.TEXTBOX) {
                                    morphBackToTabs()
                                } else {
                                    onBack()
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    ChatGptMenuIcon(color = Color.White)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Sağ: Hap Kapsül (Yeni Sohbet & 3 Nokta)
                Row(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF212121))
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Yeni Sohbet (Kalem / Edit İkonu)
                    IconButton(
                        onClick = { clearChat() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Yeni Sohbet",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // 3 Nokta Seçenekler Menüsü
                    Box {
                        IconButton(
                            onClick = { showTopMenu = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Daha Fazla",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false },
                            modifier = Modifier.background(Color(0xFF262626))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Yeni Sohbet Başlat", color = Color.White) },
                                onClick = {
                                    showTopMenu = false
                                    clearChat()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📋 Günlük Ödev Raporu", color = Color.White) },
                                onClick = {
                                    showTopMenu = false
                                    showAdviceReportSheet = true
                                    coroutineScope.launch {
                                        isAdviceLoading = true
                                        advice = TufiAIService.getCoachingAdvice(context, courses, homeworks, forceRefresh = false)
                                        isAdviceLoading = false
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("⚙️ Tufi AI & API Ayarları", color = Color.White) },
                                onClick = {
                                    showTopMenu = false
                                    showApiKeyDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // ==========================================
            // SOHBET AKIŞI VEYA KARŞILAMA EKRANI
            // ==========================================
            if (chatMessages.isEmpty()) {
                // Karşılama Ekranı (ChatGPT Minimalist Tarzı)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Nasılsın? Ne yapıyoruz bugün?",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 21.sp
                            )
                        )

                        Text(
                            text = "Ödevlerini, sınav hazırlığını ve çalışma planını birlikte yönetelim.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF8E8E93),
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hızlı Başlangıç Önerileri
                        val quickPrompts = listOf(
                            "🎯 Bugün hangi ödevle başlamalıyım?",
                            "📋 Günlük çalışma planımı hazırla",
                            "⏱️ 25 dakika Pomodoro çalışma taktiği ver",
                            "⚠️ Eksik ve geciken ödevlerimi göster"
                        )

                        quickPrompts.forEach { prompt ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFF1E1E1E),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { sendMessage(prompt) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color(0xFFECECEC),
                                            fontSize = 14.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Mesaj Akışı
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = listBottomPadding),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        if (msg.isUser) {
                            // Kullanıcı Mesajı: Sağa hizalı, koyu mavi ChatGPT baloncuk
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = Color(0xFF1E3A8A), // ChatGPT derin lacivert/mavi hap baloncuk
                                    modifier = Modifier.widthIn(max = 290.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = Color.White,
                                            fontSize = 15.5.sp,
                                            lineHeight = 22.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp)
                                    )
                                }
                            }
                        } else {
                            // Asistan (Tufi AI) Mesajı: Sola hizalı, kartsız, doğrudan zemin üstünde
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color(0xFFECECEC),
                                        fontSize = 15.5.sp,
                                        lineHeight = 24.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // ChatGPT Tarzı 6 İkonluk Alt Aksiyon Çubuğu
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    // 1. Kopyala
                                    IconButton(
                                        onClick = { copyToClipboard(msg.text) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Kopyala",
                                            tint = Color(0xFF8E8E93),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // 2. Beğen
                                    IconButton(
                                        onClick = { Toast.makeText(context, "Geri bildiriminiz için teşekkürler! 👍", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ThumbUp,
                                            contentDescription = "Beğen",
                                            tint = Color(0xFF8E8E93),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // 3. Beğenme
                                    IconButton(
                                        onClick = { Toast.makeText(context, "Geri bildirim kaydedildi.", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ThumbDown,
                                            contentDescription = "Beğenme",
                                            tint = Color(0xFF8E8E93),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // 4. Sesli Dinle (TTS)
                                    IconButton(
                                        onClick = { speakText(msg.text) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Dinle",
                                            tint = Color(0xFF8E8E93),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }

                                    // 5. Paylaş
                                    IconButton(
                                        onClick = { shareText(msg.text) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Paylaş",
                                            tint = Color(0xFF8E8E93),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // 6. Daha Fazla
                                    IconButton(
                                        onClick = { copyToClipboard(msg.text) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Daha Fazla",
                                            tint = Color(0xFF8E8E93),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isGenerating) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF2563EB)
                                )
                                Text(
                                    text = "Tufi AI yanıtlıyor...",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SİNEMATİK DÖNÜŞEN ALT BAR (Yıldız Dönüşü -> Sola Kayma -> 4'lü Kare -> Textbox)
        // =========================================================================
        val isTextboxFull = morphState == TufiBarMorphState.TEXTBOX
        val isExpandingOrDone = morphState == TufiBarMorphState.EXPANDING_UP || isTextboxFull

        val containerBg = if (isExpandingOrDone) Color(0xFF212121) else MaterialTheme.colorScheme.surface
        val containerBorder = if (isExpandingOrDone) Color(0xFF383838) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        val containerRadius = if (isExpandingOrDone) 30.dp else 28.dp

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = if (isExpandingOrDone) 14.dp else 16.dp,
                    end = if (isExpandingOrDone) 14.dp else 16.dp,
                    bottom = bottomOffset,
                    top = 4.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(containerRadius),
                color = containerBg,
                tonalElevation = 6.dp,
                shadowElevation = if (isExpandingOrDone) 12.dp else 10.dp,
                border = BorderStroke(1.dp, containerBorder),
                modifier = Modifier
                    .then(
                        if (isExpandingOrDone) {
                            Modifier.fillMaxWidth(barWidthFraction.value)
                        } else {
                            Modifier.wrapContentWidth()
                        }
                    )
                    .height(barHeight.value.dp)
                    .testTag("tufi_morphing_bottom_bar")
            ) {
                if (isTextboxFull) {
                    // =========================================================
                    // 5. AŞAMA: "TUFI AI'A SOR" CHATGPT METİN GİRİŞ KAPSÜLÜ
                    // =========================================================
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                            .graphicsLayer { alpha = textboxAlpha.value },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sol: 4'lü Kare Butonu (Sekmelere Geri Dönme)
                        IconButton(
                            onClick = { morphBackToTabs() },
                            modifier = Modifier.size(38.dp)
                        ) {
                            FourSquaresIcon(
                                color = Color(0xFFB0B0B0),
                                sizeDp = 18.dp
                            )
                        }

                        // '+' Ekle / Hızlı Öneriler Butonu
                        IconButton(
                            onClick = { showActionsSheet = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Öneriler & Ekle",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Orta: Metin Giriş Alanı ("Tufi AI'a sor...")
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontSize = 15.sp
                            ),
                            cursorBrush = SolidColor(Color.White),
                            singleLine = false,
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Default
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp),
                            decorationBox = { innerTextField ->
                                if (inputText.isBlank()) {
                                    Text(
                                        text = "Tufi AI'a sor...",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = Color(0xFF8E8E93),
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )

                        // Sağ: Mikrofon ve Mavi Dalga Ses Modu Butonu (Metin girilince Gönder oku)
                        if (inputText.isBlank()) {
                            // Mikrofon (Sesle Yazma)
                            IconButton(
                                onClick = { startSpeechToText() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Sesle Yaz",
                                    tint = Color.White,
                                    modifier = Modifier.size(21.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(2.dp))

                            // Mavi Yuvarlak Ses Dalgası Butonu (Canlı Ses Modu)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB))
                                    .clickable { startSpeechToText() },
                                contentAlignment = Alignment.Center
                            ) {
                                ChatGptWaveformIcon()
                            }
                        } else {
                            // Gönder Butonu (ChatGPT beyaz daire üstüne siyah ok)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { sendMessage(inputText) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Gönder",
                                    tint = Color.Black,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                } else {
                    // =========================================================
                    // 1, 2, 3 & 4. AŞAMALAR: YILDIZ DÖNMESİ -> SOLA KAYIŞ -> 4'LÜ KARE
                    // =========================================================
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Diğer sekmeler (Sadece TABS modundayken görünür, yıldız dönmeye başlayınca solar)
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                .graphicsLayer { alpha = otherTabsAlpha.value },
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            navItems.forEachIndexed { index, screen ->
                                val isSelected = currentTab == index
                                val isTufi = screen == Screen.TufiAI

                                if (isTufi) {
                                    // Tufi AI sekme yeri (burada sadece yazı var, yıldız üstte bağımsız animasyonla döner!)
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Yıldızın boşluğu (yer tutucu)
                                        Spacer(modifier = Modifier.size(20.dp))
                                        // YAZI DÖNMEZ! Sadece yerinde durur ve solar
                                        Text(
                                            text = screen.title,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                } else {
                                    IslandBottomNavItem(
                                        icon = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        label = screen.title,
                                        isSelected = isSelected,
                                        onClick = { onTabSelected(index) },
                                        testTag = "nav_tab_${screen.route}"
                                    )
                                }
                            }
                        }

                        // SADECE YILDIZ VE 4'LÜ KARE ANİMASYON KATMANI (Yazıdan tamamen bağımsız!)
                        val horizontalBias = lerpFloat(0.04f, -0.92f, starSlideToLeftFraction.value)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            contentAlignment = BiasAlignment(horizontalBias, 0f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        rotationZ = starRotation.value
                                        scaleX = starScale.value
                                        scaleY = starScale.value
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (starMorphProgress.value < 0.5f) {
                                    // 1. AŞAMA: SADECE YILDIZ DÖNÜYOR VE GENİŞLİYOR (Yazı kesinlikle dönmüyor!)
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Tufi AI Yıldızı",
                                        tint = if (isExpandingOrDone) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    // 2 & 3. AŞAMA: EN SOLA GEÇTİ VE 4'LÜ KARE FORMATINI ALDI
                                    FourSquaresIcon(
                                        color = Color.White,
                                        sizeDp = 18.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // '+' BUTONU HIZLI AKSİYONLAR MODAL BOTTOM SHEET
        // ==========================================
        if (showActionsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showActionsSheet = false },
                containerColor = Color(0xFF1E1E1E),
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tufi AI Hızlı Araçlar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    val sheetActions = listOf(
                        "📋 Günlük Ödev Raporu Hazırla" to "Bugünkü teslimleri, eksikleri ve adım adım çalışma planını oluşturur.",
                        "🎯 Bugün Nereden Başlamalıyım?" to "Zihnini yormadan ilk odaklanman gereken ödevi belirler.",
                        "⏱️ 25 Dakika Pomodoro Rotası" to "Derece öğrencisi mola ve odak stratejisi verir.",
                        "⚠️ Eksik ve Geciken Ödevler" to "Geciken ödevleri eritme taktiği sunar.",
                        "🧹 Sohbet Geçmişini Temizle" to "Mevcut sohbeti sıfırlar ve yeni sayfa açar."
                    )

                    sheetActions.forEach { (title, subtitle) ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF2A2A2A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showActionsSheet = false
                                    if (title.contains("Temizle")) {
                                        clearChat()
                                    } else {
                                        sendMessage(title)
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF9E9E9E)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // ==========================================
        // GÜNLÜK ÖDEV RAPORU MODAL BOTTOM SHEET
        // ==========================================
        if (showAdviceReportSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAdviceReportSheet = false },
                containerColor = Color(0xFF181818),
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📋 Tufi AI Günlük Ödev Raporu",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    if (isAdviceLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2563EB))
                        }
                    } else {
                        val currentAdvice = advice ?: remember(homeworks) {
                            TufiAIService.generateBuiltInAdvice(homeworks)
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF242424),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = currentAdvice.headline,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = currentAdvice.summary, color = Color(0xFFECECEC), fontSize = 14.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF242424),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "📅 Bugünkü Teslimler", fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = currentAdvice.todayTasksInsight, color = Color(0xFFB0B0B0), fontSize = 13.5.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF242424),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "🚀 Adım Adım Strateji", fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(6.dp))
                                currentAdvice.stepByStepPlan.forEach { step ->
                                    Text(text = "• $step", color = Color(0xFFECECEC), fontSize = 13.5.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // ==========================================
        // API AYARLARI PENCERESİ
        // ==========================================
        if (showApiKeyDialog) {
            TufiAISettingsDialog(
                onDismiss = { showApiKeyDialog = false }
            )
        }
    }
}

/**
 * 4'lü Kare Formatı İkonu (2x2 Grid)
 */
@Composable
private fun FourSquaresIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    sizeDp: Dp = 18.dp
) {
    Canvas(modifier = modifier.size(sizeDp)) {
        val squareWidth = size.width * 0.40f
        val gap = size.width * 0.20f
        val corner = 2.dp.toPx()

        // 1. Sol Üst Kare
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, 0f),
            size = Size(squareWidth, squareWidth),
            cornerRadius = CornerRadius(corner, corner)
        )
        // 2. Sağ Üst Kare
        drawRoundRect(
            color = color,
            topLeft = Offset(squareWidth + gap, 0f),
            size = Size(squareWidth, squareWidth),
            cornerRadius = CornerRadius(corner, corner)
        )
        // 3. Sol Alt Kare
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, squareWidth + gap),
            size = Size(squareWidth, squareWidth),
            cornerRadius = CornerRadius(corner, corner)
        )
        // 4. Sağ Alt Kare
        drawRoundRect(
            color = color,
            topLeft = Offset(squareWidth + gap, squareWidth + gap),
            size = Size(squareWidth, squareWidth),
            cornerRadius = CornerRadius(corner, corner)
        )
    }
}

/**
 * ChatGPT Sol Üst 2 Çizgili Menü İkonu (=)
 */
@Composable
private fun ChatGptMenuIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier.size(17.dp)) {
        val stroke = 2.dp.toPx()
        val w = size.width
        val y1 = size.height * 0.36f
        val y2 = size.height * 0.64f

        drawLine(
            color = color,
            start = Offset(0f, y1),
            end = Offset(w, y1),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(0f, y2),
            end = Offset(w, y2),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/**
 * ChatGPT Mavi Ses Modu Dalga İkonu (| | | | |)
 */
@Composable
private fun ChatGptWaveformIcon(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val heights = listOf(7.dp, 16.dp, 11.dp, 19.dp, 8.dp)
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(2.2.dp)
                    .height(h)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}
