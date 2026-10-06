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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.data.speech.AudioRecorder
import com.example.data.speech.ModelManager
import com.example.data.speech.SpeechSettingsManager
import com.example.data.speech.TranscriptionResult
import com.example.data.speech.WhisperEngine
import com.example.data.speech.WhisperModelType
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.ai.TufiChatSession
import com.example.data.model.Course
import com.example.data.model.HomeworkWithCourse
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.example.ui.components.IslandNotificationType
import com.example.ui.components.IslandBottomNavItem
import com.example.ui.components.TufiAISettingsDialog
import com.example.ui.components.TufiChatHistoryDrawer
import com.example.ui.components.TufiMarkdownText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Uygulama Çalışma Döngüsü İçinde Tufi AI Oturum Yönetimi:
 * - Uygulamaya yeni girildiyse (Cold Start) -> Yeni bir sohbet açılır.
 * - Uygulama içinde kalınmışsa (sekmeler arası geçişte) -> Önceki sohbet korunur ve açılır.
 */
object TufiAISessionTracker {
    var hasOpenedInThisAppRun: Boolean = false
    val inMemoryMessages = mutableListOf<TufiChatMessage>()
    var inMemorySessionId: String? = null
}

/**
 * Morfizasyon Aşamaları
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

private fun lerpDp(start: Dp, stop: Dp, fraction: Float): Dp =
    (start.value + fraction * (stop.value - start.value)).dp

private fun formatSessionDate(millis: Long): String {
    val now = Calendar.getInstance()
    val sessionCal = Calendar.getInstance().apply { timeInMillis = millis }
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
    return when {
        now.get(Calendar.YEAR) == sessionCal.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == sessionCal.get(Calendar.DAY_OF_YEAR) -> "Bugün $timeStr"
        now.get(Calendar.YEAR) == sessionCal.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) - sessionCal.get(Calendar.DAY_OF_YEAR) == 1 -> "Dün $timeStr"
        else -> SimpleDateFormat("d MMMM HH:mm", Locale.forLanguageTag("tr-TR")).format(Date(millis))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TufiAIScreen(
    courses: List<Course>,
    homeworks: List<HomeworkWithCourse>,
    navItems: List<Screen> = emptyList(),
    currentTab: Int = 2,
    onTabSelected: (Int) -> Unit = {},
    onBack: () -> Unit = {},
    onShowNotification: ((String, IslandNotificationType, String?) -> Unit)? = null,
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

    // Oturum & Geçmiş Sohbetler State
    var activeSessionId by remember { mutableStateOf<String?>(null) }
    var showLeftDrawer by remember { mutableStateOf(false) }
    var pastSessions by remember { mutableStateOf<List<TufiChatSession>>(emptyList()) }

    // Menü ve Pencere Durumları
    var showTopMenu by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showActionsSheet by remember { mutableStateOf(false) }
    var showAdviceReportSheet by remember { mutableStateOf(false) }

    // Koçluk Raporu State
    var advice by remember { mutableStateOf<TufiAICoachAdvice?>(null) }
    var isAdviceLoading by remember { mutableStateOf(false) }

    // =========================================================================
    // SİNEMATİK ANİMASYON DURUMLARI (Yavaş, Akıcı & Animasyon Bitene Kadar Ekran Gizli)
    // =========================================================================
    var morphState by remember { mutableStateOf(TufiBarMorphState.TABS) }
    val starRotation = remember { Animatable(0f) }
    val starScale = remember { Animatable(1f) }
    val starMorphProgress = remember { Animatable(0f) } // 0f = Yıldız, 1f = 4'lü Kare
    val tabsShrinkProgress = remember { Animatable(0f) } // 0f = Sekmeler açık, 1f = Sekmeler kapalı
    val tufiTextProgress = remember { Animatable(1f) }   // 1f = "Tufi AI" yazısı açık, 0f = Kapalı
    val barExpandProgress = remember { Animatable(0f) }  // 0f = Ada boyutu, 1f = Tam genişlik
    val textboxAlpha = remember { Animatable(0f) }
    val screenContentAlpha = remember { Animatable(0f) } // Animasyon bitene kadar 0 kalır!

    // =========================================================================
    // GERİ DÖNÜŞ ANİMASYONU (Kareye basınca tersine sinematik animasyonla ada açılır)
    // =========================================================================
    fun morphBackToTabs() {
        coroutineScope.launch {
            // 1. Textbox içeriği hızla solar
            launch { textboxAlpha.animateTo(0f, tween(120)) }
            screenContentAlpha.snapTo(1f) // Kararma yok!

            // 2. Bar tam genişlikten ada boyutuna küçülür
            morphState = TufiBarMorphState.EXPANDING_UP
            launch { barExpandProgress.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
            delay(100)

            // 3. 4'lü kare yıldıza/sohbet ikonuna geri döner ve diğer sekmeler açılır
            morphState = TufiBarMorphState.STAR_MOVING_LEFT
            launch { starMorphProgress.animateTo(0f, tween(260, easing = FastOutSlowInEasing)) }
            launch { starRotation.animateTo(0f, tween(380, easing = FastOutSlowInEasing)) }
            launch { starScale.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
            launch { tabsShrinkProgress.animateTo(0f, tween(340, easing = FastOutSlowInEasing)) }

            delay(220)

            // 4. Sohbet yazısı açılır ve normal TABS durumuna geçilir
            morphState = TufiBarMorphState.TABS
            tufiTextProgress.animateTo(1f, tween(180))
        }
    }

    // =========================================================================
    // İLERİ ANİMASYON (Tufi AI tuşuna ve Sohbet tuşuna basınca sinematik dönüşüm)
    // =========================================================================
    fun startForwardCinematicAnimation(fromColdStart: Boolean = false) {
        coroutineScope.launch {
            if (fromColdStart) {
                // İlk açılışta sinematik animasyon
                screenContentAlpha.snapTo(0f)
                textboxAlpha.snapTo(0f)
                morphState = TufiBarMorphState.TABS
                starRotation.snapTo(0f)
                starScale.snapTo(1f)
                starMorphProgress.snapTo(0f)
                tabsShrinkProgress.snapTo(0f)
                tufiTextProgress.snapTo(1f)
                barExpandProgress.snapTo(0f)

                delay(120)

                // 1. AŞAMA: SADECE YILDIZ YAVAŞÇA DÖNÜP GENİŞLER
                morphState = TufiBarMorphState.STAR_SPINNING
                launch {
                    tufiTextProgress.animateTo(0f, tween(350, easing = FastOutSlowInEasing))
                }
                launch {
                    starScale.animateTo(1.5f, tween(450, easing = FastOutSlowInEasing))
                    starScale.animateTo(1.2f, tween(350, easing = FastOutSlowInEasing))
                }
                starRotation.animateTo(720f, tween(850, easing = FastOutSlowInEasing))

                // 2. AŞAMA: DİĞER SEKMELELER KAPANIR, YILDIZ 4'LÜ KAREYE DÖNÜŞÜR
                morphState = TufiBarMorphState.STAR_MOVING_LEFT
                launch {
                    tabsShrinkProgress.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
                }
                launch {
                    delay(180)
                    starMorphProgress.animateTo(1f, tween(350, easing = FastOutSlowInEasing))
                    starScale.animateTo(1.0f, tween(250))
                }
                delay(450)

                // 3. AŞAMA: BAR ÜSTE VE YANA GENİŞLER
                morphState = TufiBarMorphState.EXPANDING_UP
                launch {
                    barExpandProgress.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
                }
                launch {
                    delay(120)
                    screenContentAlpha.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
                }
                delay(320)

                // 4. AŞAMA: METİN GİRİŞ KUTUSU BELİRİR
                morphState = TufiBarMorphState.TEXTBOX
                textboxAlpha.animateTo(1f, tween(200))
            } else {
                // Tufi AI içindeyken "Sohbet" butonuna basıldığında: Ekran kararmaz, aynı şık animasyonla metin kutusuna dönüşür!
                screenContentAlpha.snapTo(1f)
                textboxAlpha.snapTo(0f)

                // 1. Sohbet yazısı kapanır, ikon döner
                morphState = TufiBarMorphState.STAR_SPINNING
                launch { tufiTextProgress.animateTo(0f, tween(200, easing = FastOutSlowInEasing)) }
                launch {
                    starScale.animateTo(1.4f, tween(280, easing = FastOutSlowInEasing))
                    starScale.animateTo(1.1f, tween(180, easing = FastOutSlowInEasing))
                }
                starRotation.animateTo(720f, tween(550, easing = FastOutSlowInEasing))

                // 2. Diğer sekmeler kapanır, ikon 4'lü kareye dönüşür
                morphState = TufiBarMorphState.STAR_MOVING_LEFT
                launch { tabsShrinkProgress.animateTo(1f, tween(340, easing = FastOutSlowInEasing)) }
                launch {
                    delay(90)
                    starMorphProgress.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
                    starScale.animateTo(1.0f, tween(200))
                }
                delay(280)

                // 3. Bar genişler
                morphState = TufiBarMorphState.EXPANDING_UP
                launch { barExpandProgress.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
                delay(220)

                // 4. Metin kutusu açılır
                morphState = TufiBarMorphState.TEXTBOX
                textboxAlpha.animateTo(1f, tween(180))
            }
        }
    }

    BackHandler(enabled = showLeftDrawer) {
        showLeftDrawer = false
    }
    BackHandler(enabled = !showLeftDrawer && (morphState == TufiBarMorphState.TEXTBOX || barExpandProgress.value > 0.1f)) {
        morphBackToTabs()
    }
    BackHandler(enabled = !showLeftDrawer && morphState == TufiBarMorphState.TABS && barExpandProgress.value <= 0.1f) {
        onBack()
    }

    // =========================================================================
    // OTURUM YÖNETİMİ (Yeni Açılış vs. Aynı Oturumda Sekme Değiştirme)
    // =========================================================================
    LaunchedEffect(Unit) {
        if (!TufiAISessionTracker.hasOpenedInThisAppRun) {
            // UYGULAMAYA YENİ GİRDİYSEK: Yeni bir sohbet açılır!
            TufiAISessionTracker.hasOpenedInThisAppRun = true
            chatMessages.clear()
            activeSessionId = null
            TufiAISessionTracker.inMemoryMessages.clear()
            TufiAISessionTracker.inMemorySessionId = null
        } else {
            // DAHA ÖNCE UYGULAMAYA GİRİP ÇIKMADIYSAK: O eski sohbet açılır!
            chatMessages.clear()
            if (TufiAISessionTracker.inMemoryMessages.isNotEmpty()) {
                chatMessages.addAll(TufiAISessionTracker.inMemoryMessages)
                activeSessionId = TufiAISessionTracker.inMemorySessionId
            } else {
                val loaded = TufiAIService.loadChatHistory(context)
                chatMessages.addAll(loaded)
                activeSessionId = TufiAIService.getActiveSessionId(context)
            }
        }

        // Sinematik yavaş açılış animasyonunu başlat
        startForwardCinematicAnimation(fromColdStart = true)
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

    // ==========================================
    // CİHAZ İÇİ WHISPER SES TANIMA & DİKTE
    // ==========================================
    val audioRecorder = remember { AudioRecorder(context) }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingTimeText by remember { mutableStateOf("00:00") }
    var isTranscribingAudio by remember { mutableStateOf(false) }
    var showModelDownloadPromptDialog by remember { mutableStateOf(false) }
    var isDownloadingModelInDialog by remember { mutableStateOf(false) }
    var modelDownloadProgressVal by remember { mutableStateOf<Float?>(null) }
    var modelDownloadStatusText by remember { mutableStateOf("") }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // İzin verilince doğrudan ses kaydını başlat
            val selectedModel = SpeechSettingsManager.getSelectedModel(context)
            if (!ModelManager.isModelDownloaded(context, selectedModel)) {
                showModelDownloadPromptDialog = true
            } else {
                coroutineScope.launch {
                    val started = audioRecorder.startRecording(
                        scope = coroutineScope,
                        onDurationTick = { _, formatted -> recordingTimeText = formatted },
                        onAmplitudeTick = { _ -> }
                    )
                    if (started) isRecordingAudio = true
                }
            }
        } else {
            Toast.makeText(context, "Sesle yazmak için mikrofon izni gereklidir.", Toast.LENGTH_SHORT).show()
        }
    }

    // Yedek: Google Sesle Yazma (Speech to Text)
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                if (inputText.isBlank()) {
                    inputText = spokenText
                } else {
                    inputText = "$inputText $spokenText"
                }
            }
        }
    }

    fun fallbackGoogleSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Tufi AI'a konuşun...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Google ses tanıma bu cihazda aktif değil.", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerVoiceInputFlow() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        if (isRecordingAudio) {
            // Durdur ve yazıya çevir
            isRecordingAudio = false
            isTranscribingAudio = true
            coroutineScope.launch {
                val recordResult = audioRecorder.stopRecording()
                val transResult = WhisperEngine.transcribe(context, recordResult)
                isTranscribingAudio = false
                when (transResult) {
                    is TranscriptionResult.Success -> {
                        val text = transResult.text
                        if (inputText.isBlank()) {
                            inputText = text
                        } else {
                            inputText = "$inputText $text"
                        }
                        if (transResult.wasAiCleaned) {
                            Toast.makeText(context, "Dikte Tufi AI ile düzenlendi ✨", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is TranscriptionResult.SilenceDetected -> {
                        Toast.makeText(context, "Ses algılanamadı, lütfen mikrofona daha yakın konuşun.", Toast.LENGTH_SHORT).show()
                    }
                    is TranscriptionResult.ModelNotAvailable -> {
                        showModelDownloadPromptDialog = true
                    }
                    is TranscriptionResult.Error -> {
                        Toast.makeText(context, transResult.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            val selectedModel = SpeechSettingsManager.getSelectedModel(context)
            if (!ModelManager.isModelDownloaded(context, selectedModel)) {
                showModelDownloadPromptDialog = true
                return
            }

            coroutineScope.launch {
                val started = audioRecorder.startRecording(
                    scope = coroutineScope,
                    onDurationTick = { _, formatted ->
                        recordingTimeText = formatted
                    },
                    onAmplitudeTick = { _ -> }
                )
                if (started) {
                    isRecordingAudio = true
                } else {
                    Toast.makeText(context, "Mikrofon başlatılamadı.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun clearChat() {
        chatMessages.clear()
        activeSessionId = null
        TufiAISessionTracker.inMemoryMessages.clear()
        TufiAISessionTracker.inMemorySessionId = null
        TufiAIService.clearChatHistory(context)
        if (onShowNotification != null) {
            onShowNotification("Yeni sohbet başlatıldı", IslandNotificationType.SUCCESS, "Tufi AI")
        } else {
            Toast.makeText(context, "Yeni sohbet başlatıldı", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadSelectedSession(session: TufiChatSession) {
        activeSessionId = session.id
        TufiAIService.setActiveSessionId(context, session.id)
        chatMessages.clear()
        chatMessages.addAll(session.messages)
        TufiAISessionTracker.inMemoryMessages.clear()
        TufiAISessionTracker.inMemoryMessages.addAll(session.messages)
        TufiAISessionTracker.inMemorySessionId = session.id
        showLeftDrawer = false
        if (onShowNotification != null) {
            onShowNotification("Sohbet açıldı: ${session.title}", IslandNotificationType.INFO, "Tufi AI")
        } else {
            Toast.makeText(context, "Sohbet açıldı: ${session.title}", Toast.LENGTH_SHORT).show()
        }
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

            // Oturuma ve hafızaya kaydet
            activeSessionId = TufiAIService.saveChatHistory(context, chatMessages, activeSessionId)
            TufiAISessionTracker.inMemoryMessages.clear()
            TufiAISessionTracker.inMemoryMessages.addAll(chatMessages)
            TufiAISessionTracker.inMemorySessionId = activeSessionId
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
            .background(Color(0xFF000000).copy(alpha = screenContentAlpha.value)) // Animasyon bitene kadar saydam/geçişli!
            .testTag("tufi_ai_screen")
    ) {
        // ==========================================
        // TUFİ AI EKRANI İÇERİĞİ (Animasyon tamamlanınca yumuşakça görünür)
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = screenContentAlpha.value }
        ) {
            // CHATGPT ÜST BAŞLIK (Header)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol: 3 ÇİZGİ TUŞU -> ESKİ SOHBETLERİ GÖSTERİR!
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF212121))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                try {
                                    pastSessions = TufiAIService.loadAllSessions(context)
                                } catch (e: Exception) {
                                    android.util.Log.e("TufiAIScreen", "Error loading history", e)
                                    pastSessions = emptyList()
                                }
                                showLeftDrawer = true
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
                                text = { Text("💬 Eski Sohbetler", color = Color.White) },
                                onClick = {
                                    showTopMenu = false
                                    pastSessions = TufiAIService.loadAllSessions(context)
                                    showLeftDrawer = true
                                }
                            )
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

            // SOHBET AKIŞI VEYA KARŞILAMA EKRANI
            if (chatMessages.isEmpty()) {
                // Claude Minimalist Karşılama Ekranı: Sadece "Let's noodle"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Let's noodle",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Normal,
                            fontSize = 36.sp,
                            color = Color(0xFFF3EFEA),
                            letterSpacing = (-0.5).sp
                        )
                    )
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
                                    color = Color(0xFF1E3A8A),
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
                            // Asistan (Tufi AI) Mesajı: Sola hizalı, kartsız, doğrudan zemin üstünde Markdown formatında
                            Column(modifier = Modifier.fillMaxWidth()) {
                                TufiMarkdownText(
                                    markdownText = msg.text,
                                    textColor = Color(0xFFECECEC)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // ChatGPT Tarzı 6 İkonluk Alt Aksiyon Çubuğu
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(onClick = { copyToClipboard(msg.text) }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = Color(0xFF8E8E93), modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { Toast.makeText(context, "Geri bildiriminiz için teşekkürler! 👍", Toast.LENGTH_SHORT).show() }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Outlined.ThumbUp, contentDescription = "Beğen", tint = Color(0xFF8E8E93), modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { Toast.makeText(context, "Geri bildirim kaydedildi.", Toast.LENGTH_SHORT).show() }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Outlined.ThumbDown, contentDescription = "Beğenme", tint = Color(0xFF8E8E93), modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { speakText(msg.text) }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Dinle", tint = Color(0xFF8E8E93), modifier = Modifier.size(17.dp))
                                    }
                                    IconButton(onClick = { shareText(msg.text) }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = "Paylaş", tint = Color(0xFF8E8E93), modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { copyToClipboard(msg.text) }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Daha Fazla", tint = Color(0xFF8E8E93), modifier = Modifier.size(16.dp))
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
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color(0xFF2563EB))
                                Text(text = "Tufi AI yanıtlıyor...", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93)))
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SİNEMATİK DÖNÜŞEN ALT BAR (Yavaş, Akıcı & Doğal Düzen)
        // =========================================================================
        val isTextbox = morphState == TufiBarMorphState.TEXTBOX
        val barHeightDp = lerpDp(52.dp, 58.dp, barExpandProgress.value)
        val cornerDp = lerpDp(28.dp, 29.dp, barExpandProgress.value)

        val barBgColor by animateColorAsState(
            targetValue = if (isTextbox || barExpandProgress.value > 0.5f) Color(0xFF212121) else MaterialTheme.colorScheme.surface,
            animationSpec = tween(280),
            label = "bar_bg_color"
        )
        val barBorderColor by animateColorAsState(
            targetValue = if (isTextbox || barExpandProgress.value > 0.5f) Color(0xFF383838) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            animationSpec = tween(280),
            label = "bar_border_color"
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = if (isTextbox || barExpandProgress.value > 0.1f) 14.dp else 16.dp,
                    end = if (isTextbox || barExpandProgress.value > 0.1f) 14.dp else 16.dp,
                    bottom = bottomOffset,
                    top = 4.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(cornerDp),
                color = barBgColor,
                tonalElevation = 6.dp,
                shadowElevation = if (isTextbox) 12.dp else 10.dp,
                border = BorderStroke(1.dp, barBorderColor),
                modifier = Modifier
                    .then(
                        if (isTextbox) {
                            Modifier.fillMaxWidth()
                        } else if (barExpandProgress.value > 0.01f) {
                            Modifier.fillMaxWidth(lerpFloat(0.68f, 1f, barExpandProgress.value))
                        } else {
                            Modifier.wrapContentWidth()
                        }
                    )
                    .height(barHeightDp)
                    .testTag("tufi_morphing_bottom_bar")
            ) {
                if (isTextbox) {
                    // "TUFI AI'A SOR" CHATGPT METİN GİRİŞ KAPSÜLÜ
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                            .graphicsLayer { alpha = textboxAlpha.value },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sol: 4'lü Kare Butonu (Kareye basınca kare ortaya gelir, yıldıza/sohbete dönüşür!)
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
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Öneriler & Ekle", tint = Color.White, modifier = Modifier.size(22.dp))
                        }

                        // Orta & Sağ: Kayıt Durumu / Transkripsiyon / Metin Giriş Alanı
                        if (isRecordingAudio) {
                            // Canlı Kayıt Durumu: Kırmızı yanıp sönen nokta ve süre
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dinleniyor... $recordingTimeText",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            // Kaydı Durdur Butonu (Kırmızı yuvarlak buton)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                                    .clickable { triggerVoiceInputFlow() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Kaydı Durdur",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else if (isTranscribingAudio) {
                            // Transkripsiyon Yapılıyor Göstergesi
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Yazıya çevriliyor...",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )
                            }
                        } else {
                            // Normal Metin Giriş Alanı ("Tufi AI'a sor...")
                            BasicTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontSize = 15.sp),
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
                                            style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF8E8E93), fontSize = 15.sp)
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            // Sağ: Mikrofon ve Mavi Dalga Ses Modu Butonu (Metin girilince Gönder oku)
                            if (inputText.isBlank()) {
                                IconButton(onClick = { triggerVoiceInputFlow() }, modifier = Modifier.size(36.dp)) {
                                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Sesle Yaz", tint = Color.White, modifier = Modifier.size(21.dp))
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2563EB))
                                        .clickable { triggerVoiceInputFlow() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    ChatGptWaveformIcon()
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable { sendMessage(inputText) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Gönder", tint = Color.Black, modifier = Modifier.size(19.dp))
                                }
                            }
                        }
                    }
                } else {
                    // SİNEMATİK ANİMASYONLU TÜM SEKMELELERİN BULUNDUĞU BOTTOM BAR
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy((6f * (1f - tabsShrinkProgress.value)).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        navItems.forEachIndexed { index, screen ->
                            val isSelected = currentTab == index
                            val isTufi = screen == Screen.TufiAI

                            if (isTufi) {
                                // Tufi AI Sekmesi -> Tufi AI ekranındayken "Sohbet" tuşu olarak görünür
                                // Tufi AI ve Sohbet butonuna basınca ikon döner, 4'lü kareye dönüşür; kareye basınca geri döner
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (morphState == TufiBarMorphState.TABS)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = (1f - tabsShrinkProgress.value).coerceIn(0f, 1f))
                                        )
                                        .padding(
                                            horizontal = (12f * (1f - tabsShrinkProgress.value) + 4f).dp,
                                            vertical = 6.dp
                                        )
                                        .clickable {
                                            if (morphState == TufiBarMorphState.TABS) {
                                                startForwardCinematicAnimation(fromColdStart = false)
                                            }
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy((6f * tufiTextProgress.value).dp)
                                ) {
                                    // Dönen, büyüyen ve Sohbet / Yıldız / 4'lü Kare arasında morph eden ikon
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .graphicsLayer {
                                                rotationZ = starRotation.value
                                                scaleX = starScale.value
                                                scaleY = starScale.value
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (starMorphProgress.value < 0.5f) {
                                            Icon(
                                                imageVector = if (morphState == TufiBarMorphState.TABS) Icons.Default.ChatBubbleOutline else Icons.Default.AutoAwesome,
                                                contentDescription = "Sohbet",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            FourSquaresIcon(
                                                color = if (barBgColor == Color(0xFF212121)) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                                sizeDp = 18.dp
                                            )
                                        }
                                    }

                                    // "Sohbet" Yazısı (Animasyonla açılır ve kapanır)
                                    val textAlpha = tufiTextProgress.value * (1f - tabsShrinkProgress.value)
                                    if (textAlpha > 0.05f) {
                                        Text(
                                            text = "Sohbet",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            maxLines = 1,
                                            modifier = Modifier.graphicsLayer {
                                                alpha = textAlpha
                                            }
                                        )
                                    }
                                }
                            } else {
                                // Diğer Sekmeler (Ödevler, Takvim, İstatistik, Notlar, Araçlar)
                                val tabScale = (1f - tabsShrinkProgress.value)
                                if (tabScale > 0.02f) {
                                    Box(
                                        modifier = Modifier.graphicsLayer {
                                            alpha = tabScale
                                            scaleX = tabScale
                                            scaleY = tabScale
                                        }
                                    ) {
                                        IslandBottomNavItem(
                                            icon = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                            label = screen.title,
                                            isSelected = isSelected,
                                            onClick = {
                                                if (morphState == TufiBarMorphState.TABS) {
                                                    onTabSelected(index)
                                                }
                                            },
                                            testTag = "nav_tab_${screen.route}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // ESKİ SOHBETLER SOL ÇEKMECE MENÜSÜ (3 Çizgi Tuşuna Basınca Açılır)
        // =========================================================================
        TufiChatHistoryDrawer(
            visible = showLeftDrawer,
            sessions = pastSessions,
            activeSessionId = activeSessionId,
            onSessionSelected = { session ->
                loadSelectedSession(session)
            },
            onNewChat = {
                showLeftDrawer = false
                clearChat()
            },
            onTasksClicked = {
                showLeftDrawer = false
                showAdviceReportSheet = true
                coroutineScope.launch {
                    isAdviceLoading = true
                    advice = TufiAIService.getCoachingAdvice(context, courses, homeworks, forceRefresh = false)
                    isAdviceLoading = false
                }
            },
            onSettingsClicked = {
                showLeftDrawer = false
                showApiKeyDialog = true
            },
            onDeleteSession = { sessionId ->
                TufiAIService.deleteSession(context, sessionId)
                pastSessions = TufiAIService.loadAllSessions(context)
                if (activeSessionId == sessionId) {
                    clearChat()
                }
                if (onShowNotification != null) {
                    onShowNotification("Sohbet silindi", IslandNotificationType.INFO, "Tufi AI")
                }
            },
            onDismiss = { showLeftDrawer = false }
        )

        // '+' BUTONU HIZLI AKSİYONLAR MODAL BOTTOM SHEET
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

        // GÜNLÜK ÖDEV RAPORU MODAL BOTTOM SHEET
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

        // API AYARLARI PENCERESİ
        if (showApiKeyDialog) {
            TufiAISettingsDialog(
                onDismiss = { showApiKeyDialog = false }
            )
        }

        // ÇEVRİMDIŞI MODEL İNDİRME / FALLBACK İLETİŞİM KUTUSU
        if (showModelDownloadPromptDialog) {
            val selectedModel = SpeechSettingsManager.getSelectedModel(context)
            AlertDialog(
                onDismissRequest = {
                    if (!isDownloadingModelInDialog) {
                        showModelDownloadPromptDialog = false
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = { Text("Çevrimdışı Ses Tanıma Modeli") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Tamamen internetsiz ve gizlilik odaklı Whisper ses tanıma için Türkçe ${selectedModel.displayName} modelinin indirilmesi gerekmektedir (~${selectedModel.approximateSizeMb} MB).\n\nİsterseniz modeli cihazınıza indirebilir veya eski Google Ses Tanıma sistemini kullanabilirsiniz."
                        )
                        if (isDownloadingModelInDialog) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { modelDownloadProgressVal ?: 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                            Text(
                                text = modelDownloadStatusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                confirmButton = {
                    if (!isDownloadingModelInDialog) {
                        Button(
                            onClick = {
                                isDownloadingModelInDialog = true
                                modelDownloadProgressVal = 0f
                                modelDownloadStatusText = "Bağlantı kuruluyor..."
                                coroutineScope.launch {
                                    val res = ModelManager.downloadModel(context, selectedModel) { prog, msg ->
                                        modelDownloadProgressVal = prog
                                        modelDownloadStatusText = msg
                                    }
                                    isDownloadingModelInDialog = false
                                    if (res.isSuccess) {
                                        showModelDownloadPromptDialog = false
                                        Toast.makeText(context, "Model hazır! Şimdi mikrofona dokunarak konuşabilirsiniz.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, res.exceptionOrNull()?.message ?: "İndirme başarısız", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Modeli İndir")
                        }
                    }
                },
                dismissButton = {
                    if (!isDownloadingModelInDialog) {
                        Row {
                            TextButton(
                                onClick = {
                                    showModelDownloadPromptDialog = false
                                    fallbackGoogleSpeechToText()
                                }
                            ) {
                                Text("Google ile Konuş")
                            }
                            TextButton(
                                onClick = { showModelDownloadPromptDialog = false }
                            ) {
                                Text("İptal")
                            }
                        }
                    }
                }
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

        drawRoundRect(color = color, topLeft = Offset(0f, 0f), size = Size(squareWidth, squareWidth), cornerRadius = CornerRadius(corner, corner))
        drawRoundRect(color = color, topLeft = Offset(squareWidth + gap, 0f), size = Size(squareWidth, squareWidth), cornerRadius = CornerRadius(corner, corner))
        drawRoundRect(color = color, topLeft = Offset(0f, squareWidth + gap), size = Size(squareWidth, squareWidth), cornerRadius = CornerRadius(corner, corner))
        drawRoundRect(color = color, topLeft = Offset(squareWidth + gap, squareWidth + gap), size = Size(squareWidth, squareWidth), cornerRadius = CornerRadius(corner, corner))
    }
}

/**
 * ChatGPT Sol Üst 3 Çizgili Menü İkonu (Hamburger)
 */
@Composable
private fun ChatGptMenuIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier.size(18.dp)) {
        val stroke = 2.dp.toPx()
        val w = size.width
        val y1 = size.height * 0.22f
        val y2 = size.height * 0.50f
        val y3 = size.height * 0.78f

        drawLine(color = color, start = Offset(0f, y1), end = Offset(w, y1), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(0f, y2), end = Offset(w, y2), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(0f, y3), end = Offset(w, y3), strokeWidth = stroke, cap = StrokeCap.Round)
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
