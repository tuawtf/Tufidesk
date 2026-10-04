package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Screen
import com.example.data.ai.TufiAICoachAdvice
import com.example.data.ai.TufiAIService
import com.example.data.ai.TufiChatMessage
import com.example.data.model.Course
import com.example.data.model.HomeworkWithCourse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tufi AI Alt Barının Morfizasyon Aşamaları:
 * 1. TABS: Standart alt gezinme ada çubuğu; Tufi AI yıldız ikonu 720 derece animasyonla döner.
 * 2. EXPANDING: Alt bar yukarı ve yana doğru akıcı bir yay (spring) animasyonuyla genişler.
 * 3. TEXTBOX: Bar tam bir soru sorma metin giriş kutusuna (prompt textbox) dönüşür.
 */
enum class TufiBarMorphState {
    TABS,
    EXPANDING,
    TEXTBOX
}

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

    // Tab state: 0 -> 💬 Soru Sor (ChatGPT / Gemini / Claude Sohbet), 1 -> 📋 Günlük Rota (Ödev Raporu)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Coaching advice (Günlük Plan) state
    var advice by remember { mutableStateOf<TufiAICoachAdvice?>(null) }
    var isAdviceLoading by remember { mutableStateOf(false) }

    // Chat state
    val chatMessages = remember { mutableStateListOf<TufiChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // ==========================================
    // ALT BAR YILDIZ DÖNME & TEXTBOX DÖNÜŞÜM ANİMASYONU
    // ==========================================
    var morphState by remember { mutableStateOf(TufiBarMorphState.TABS) }
    val starRotation = remember { Animatable(0f) }
    val starScale = remember { Animatable(1f) }

    // Geri tuşu: Textbox halindeyken menüye döner, menüdeyken ana ekrana döner
    BackHandler(enabled = morphState == TufiBarMorphState.TEXTBOX) {
        morphState = TufiBarMorphState.TABS
    }
    BackHandler(enabled = morphState == TufiBarMorphState.TABS) {
        onBack()
    }

    // Tufi AI ekranı ilk açıldığında veya Soru Sor sekmesine geçildiğinde animasyon dizisini tetikle
    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            morphState = TufiBarMorphState.TABS
            starRotation.snapTo(0f)
            starScale.snapTo(1f)

            // 1. AŞAMA: Yıldızlar hızla dönsün (720 derece) ve hafifçe büyüsün
            launch {
                starScale.animateTo(
                    targetValue = 1.35f,
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                )
                starScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            }
            starRotation.animateTo(
                targetValue = 720f,
                animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing)
            )

            // 2. AŞAMA: Alt bar yukarı ve yana doğru genişlesin
            morphState = TufiBarMorphState.EXPANDING
            delay(220)

            // 3. AŞAMA: Alt bar tam bir soru sorma metin kutusuna (textbox) dönüşsün
            morphState = TufiBarMorphState.TEXTBOX
        } else {
            // Günlük Plan sekmesinde normal gezinme çubuğuna geri dön
            morphState = TufiBarMorphState.TABS
        }
    }

    // Load initial advice and chat history
    LaunchedEffect(Unit) {
        val loaded = TufiAIService.loadChatHistory(context)
        chatMessages.clear()
        chatMessages.addAll(loaded)
    }

    LaunchedEffect(homeworks) {
        isAdviceLoading = true
        advice = TufiAIService.getCoachingAdvice(
            context = context,
            courses = courses,
            homeworks = homeworks,
            forceRefresh = false
        )
        isAdviceLoading = false
    }

    fun refreshAdvice() {
        coroutineScope.launch {
            isAdviceLoading = true
            advice = TufiAIService.getCoachingAdvice(
                context = context,
                courses = courses,
                homeworks = homeworks,
                forceRefresh = true
            )
            isAdviceLoading = false
            Toast.makeText(context, "Tufi AI ödev planı güncellendi! 🚀", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyAdviceToClipboard() {
        val current = advice ?: return
        val fullText = buildString {
            appendLine("🤖 Tufi AI Çalışma Raporu")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📌 ${current.headline}")
            appendLine("${current.summary}\n")
            appendLine("📋 Bugünkü Ödevler Durumu:")
            appendLine("${current.todayTasksInsight}\n")
            appendLine("⚠️ Eksik & Bekleyenler:")
            appendLine("${current.missingTasksInsight}\n")
            appendLine("📅 Haftalık Görünüm:")
            appendLine("${current.weeklyInsight}\n")
            appendLine("🚀 Adım Adım Yol Haritası:")
            current.stepByStepPlan.forEach { step ->
                appendLine("• $step")
            }
            appendLine("\n💡 Çalışma Taktiği:\n${current.studyMethodTip}")
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Tufi AI Planı", fullText))
        Toast.makeText(context, "Plan panoya kopyalandı! 📋", Toast.LENGTH_SHORT).show()
    }

    fun copyMessageToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Tufi AI Yanıtı", text))
        Toast.makeText(context, "Yanıt panoya kopyalandı! 📋", Toast.LENGTH_SHORT).show()
    }

    fun sendMessage(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isBlank() || isGenerating) return

        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val userMsg = TufiChatMessage(isUser = true, text = trimmed)
        chatMessages.add(userMsg)
        inputText = ""
        isGenerating = true

        coroutineScope.launch {
            // Scroll to bottom
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

            // Scroll to new AI message
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    fun clearChat() {
        chatMessages.clear()
        TufiAIService.clearChatHistory(context)
        Toast.makeText(context, "Sohbet temizlendi.", Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("tufi_ai_screen")
    ) {
        // ==========================================
        // ÜST İÇERİK (Sekmeler ve Sohbet / Plan Akışı)
        // ==========================================
        Column(modifier = Modifier.fillMaxSize()) {
            // SEGMENTED TAB SWITCHER & ACTIONS (Minimalist üst sekmeler)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.50f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f))
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Tab 0: Soru Sor
                        val isChatSelected = selectedTab == 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isChatSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { selectedTab = 0 }
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = if (isChatSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Soru Sor",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isChatSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isChatSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Tab 1: Günlük Plan
                        val isPlanSelected = selectedTab == 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isPlanSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { selectedTab = 1 }
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    tint = if (isPlanSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Günlük Plan",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isPlanSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isPlanSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Hızlı Aksiyon Butonları
                if (selectedTab == 0) {
                    if (chatMessages.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { clearChat() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Sohbeti Temizle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { copyAdviceToClipboard() },
                        enabled = advice != null,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Kopyala",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { refreshAdvice() },
                        enabled = !isAdviceLoading,
                        modifier = Modifier.size(36.dp)
                    ) {
                        if (isAdviceLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Yenile",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Sekme İçerikleri
            if (selectedTab == 0) {
                // SOHBET ALANI
                if (chatMessages.isEmpty()) {
                    TufiChatEmptyState(
                        onPromptClick = { prompt ->
                            morphState = TufiBarMorphState.TEXTBOX
                            sendMessage(prompt)
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 84.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(chatMessages, key = { it.id }) { msg ->
                            TufiChatMessageItem(
                                message = msg,
                                onCopy = { copyMessageToClipboard(msg.text) }
                            )
                        }

                        if (isGenerating) {
                            item {
                                TufiThinkingBubble()
                            }
                        }
                    }
                }
            } else {
                // GÜNLÜK ÇALIŞMA PLANI
                val currentAdvice = advice ?: remember(homeworks) {
                    TufiAIService.generateBuiltInAdvice(homeworks)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Token Tasarrufu Bildirimi
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Token Tasarrufu Aktif • Ödevler değiştikçe akıllı güncellenir.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Başlık & Özet Kartı
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = currentAdvice.headline,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentAdvice.summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 1. Bugünkü Ödevler
                    item {
                        TufiAISimpleCard(
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            iconTint = MaterialTheme.colorScheme.primary,
                            title = "Bugünkü Ödevler",
                            description = currentAdvice.todayTasksInsight
                        )
                    }

                    // 2. Eksik & Bekleyenler
                    item {
                        TufiAISimpleCard(
                            icon = Icons.Default.WarningAmber,
                            iconTint = Color(0xFFEF4444),
                            title = "Eksik & Bekleyen Ödevler",
                            description = currentAdvice.missingTasksInsight
                        )
                    }

                    // 3. Haftalık Görünüm & Tempo
                    item {
                        TufiAISimpleCard(
                            icon = Icons.Default.DateRange,
                            iconTint = Color(0xFF3B82F6),
                            title = "Haftalık Görünüm & Tempo",
                            description = currentAdvice.weeklyInsight
                        )
                    }

                    // 4. Adım Adım Yol Haritası
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Nasıl İlerletmelisin? (Yol Haritası)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                currentAdvice.stepByStepPlan.forEachIndexed { index, step ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${index + 1}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = step.removePrefix("${index + 1}. Adım:").trim(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Verimli Çalışma Tüyosu
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                            border = BorderStroke(0.8.dp, Color(0xFFF59E0B).copy(alpha = 0.40f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Verimli Çalışma Tüyosu",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFB45309)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentAdvice.studyMethodTip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Sohbet Sekmesine Geçiş Çağrısı
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTab = 0 }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Aklına bir soru mu takıldı? Tufi AI'a sor 💬",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // DÖNÜŞEN ALT BAR (Bottom Bar -> Dönen Yıldızlar -> Yukarı Genişleme -> Textbox)
        // ==========================================
        TufiMorphingBottomBar(
            morphState = morphState,
            starRotation = starRotation.value,
            starScale = starScale.value,
            inputText = inputText,
            onInputTextChange = { inputText = it },
            onSend = { sendMessage(inputText) },
            isEnabled = !isGenerating,
            navItems = if (navItems.isNotEmpty()) navItems else listOf(
                Screen.Home,
                Screen.Calendar,
                Screen.TufiAI,
                Screen.Stats,
                Screen.Tools
            ),
            currentTab = currentTab,
            onTabSelected = onTabSelected,
            onToggleTabs = {
                morphState = if (morphState == TufiBarMorphState.TEXTBOX) {
                    TufiBarMorphState.TABS
                } else {
                    TufiBarMorphState.TEXTBOX
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Tufi AI Morfizasyon Alt Barı:
 * - TABS: Standart yüzen ada menüsü, Tufi AI yıldız ikonu 720 derece animasyonlu döner.
 * - EXPANDING: Alt bar akıcı biçimde yukarı doğru genişler.
 * - TEXTBOX: Tam donanımlı, minimalist ChatGPT/Gemini tarzı soru giriş alanına dönüşür.
 */
@Composable
private fun TufiMorphingBottomBar(
    morphState: TufiBarMorphState,
    starRotation: Float,
    starScale: Float,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isEnabled: Boolean,
    navItems: List<Screen>,
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onToggleTabs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val imeInsets = WindowInsets.ime.asPaddingValues()
    val isKeyboardOpen = imeInsets.calculateBottomPadding() > 0.dp

    val targetHeight = when (morphState) {
        TufiBarMorphState.TABS -> 52.dp
        TufiBarMorphState.EXPANDING, TufiBarMorphState.TEXTBOX -> 62.dp
    }
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tufi_bar_height"
    )

    val targetCorner = when (morphState) {
        TufiBarMorphState.TABS -> 28.dp
        TufiBarMorphState.EXPANDING, TufiBarMorphState.TEXTBOX -> 30.dp
    }
    val animatedCorner by animateDpAsState(
        targetValue = targetCorner,
        animationSpec = tween(durationMillis = 280),
        label = "tufi_bar_corner"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(
                start = 14.dp,
                end = 14.dp,
                bottom = if (isKeyboardOpen) 6.dp else 12.dp,
                top = 4.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(animatedCorner),
            color = if (morphState == TufiBarMorphState.TEXTBOX) {
                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.94f)
            } else {
                MaterialTheme.colorScheme.surface
            },
            tonalElevation = 6.dp,
            shadowElevation = if (morphState == TufiBarMorphState.TEXTBOX) 12.dp else 9.dp,
            border = BorderStroke(
                width = 1.dp,
                color = if (morphState == TufiBarMorphState.TEXTBOX) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                }
            ),
            modifier = Modifier
                .then(
                    if (morphState == TufiBarMorphState.TEXTBOX || morphState == TufiBarMorphState.EXPANDING) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.wrapContentWidth()
                    }
                )
                .height(animatedHeight)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .testTag("tufi_morphing_bottom_bar")
        ) {
            AnimatedContent(
                targetState = morphState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "tufi_bar_content_morph"
            ) { targetState ->
                when (targetState) {
                    TufiBarMorphState.TABS -> {
                        // 1. AŞAMA: STANDART ADA MENÜSÜ & DÖNEN YILDIZLAR
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            navItems.forEachIndexed { index, screen ->
                                val isTufiAI = screen.route == Screen.TufiAI.route
                                val isSelected = currentTab == index || isTufiAI

                                TufiIslandItem(
                                    icon = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    label = screen.title,
                                    isSelected = isSelected,
                                    isTufiStar = isTufiAI,
                                    starRotation = starRotation,
                                    starScale = starScale,
                                    onClick = {
                                        if (isTufiAI) {
                                            onToggleTabs()
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onTabSelected(index)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    TufiBarMorphState.EXPANDING -> {
                        // 2. AŞAMA: YUKARI DOĞRU GENİŞLEME GEÇİŞİ
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .graphicsLayer(rotationZ = starRotation, scaleX = starScale, scaleY = starScale)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Tufi AI hazırlanıyor...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    TufiBarMorphState.TEXTBOX -> {
                        // 3. AŞAMA: MORFİZE OLMUŞ TAM DONANIMLI TEXTBOX
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sol Kısım: Menüye dönme veya Tufi AI durum ikonu
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true),
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onToggleTabs()
                                        }
                                    )
                                    .testTag("tufi_textbox_toggle_tabs")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Menüye Dön",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            // Orta Kısım: Metin Giriş Alanı
                            TextField(
                                value = inputText,
                                onValueChange = onInputTextChange,
                                placeholder = {
                                    Text(
                                        text = "Tufi AI'a bir soru sor...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .animateContentSize(),
                                maxLines = 4,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    errorBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Send
                                ),
                                keyboardActions = KeyboardActions(
                                    onSend = { onSend() }
                                )
                            )

                            // Sağ Kısım: Dairesel Gönder Butonu (Yukarı Ok İkonu)
                            val canSend = inputText.isNotBlank() && isEnabled

                            FilledIconButton(
                                onClick = onSend,
                                enabled = canSend,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("tufi_textbox_send_button"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Gönder",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TufiIslandItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isTufiStar: Boolean,
    starRotation: Float,
    starScale: Float,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tufi_item_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "tufi_item_content"
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = bgColor,
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isSelected && label.isNotBlank()) 14.dp else 11.dp,
                vertical = 10.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier
                    .size(23.dp)
                    .graphicsLayer(
                        rotationZ = if (isTufiStar) starRotation else 0f,
                        scaleX = if (isTufiStar) starScale else 1f,
                        scaleY = if (isTufiStar) starScale else 1f
                    )
            )

            AnimatedVisibility(
                visible = isSelected && label.isNotBlank(),
                enter = fadeIn(animationSpec = tween(180)) + expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = fadeOut(animationSpec = tween(80)) + shrinkHorizontally(
                    animationSpec = tween(120)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = contentColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

/**
 * ChatGPT / Gemini tarzı boş sohbet karşılama ekranı ve hazır soru kartları.
 */
@Composable
private fun TufiChatEmptyState(
    onPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Nasıl yardımcı olabilirim?",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Bugünkü ödevlerin, çalışma planın veya sınav taktiklerin hakkında merak ettiğin her şeyi sorabilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        val promptStarters = listOf(
            "⚡ Bugün ilk hangi ödevden başlamalıyım?",
            "⏱️ 25 dakikalık verimli bir Pomodoro planı yap",
            "⚠️ Eksik ödevlerimi en hızlı nasıl eritirim?",
            "📐 Matematik yeni nesil problem çözme taktiği ver"
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            promptStarters.forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPromptClick(prompt) }
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                    )
                }
            }
        }
    }
}

/**
 * Sohbet mesaj balonu (Kullanıcı ve Tufi AI ayrımı + Markdown zengin metin desteği).
 */
@Composable
private fun TufiChatMessageItem(
    message: TufiChatMessage,
    onCopy: () -> Unit
) {
    if (message.isUser) {
        // Kullanıcı Mesajı (Sağa Hizalı)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    } else {
        // Tufi AI Mesajı (Sola Hizalı, Claude/Gemini kart stili + Tam Markdown Desteği)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tufi AI",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = onCopy,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Kopyala",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Markdown Metin Çıktısı (**kalın**, *italik*, # başlıklar, listeler vb.)
                        MarkdownFormattedText(
                            markdown = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            primaryColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarkdownFormattedText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    val annotatedString = remember(markdown, color, primaryColor) {
        parseMarkdownToAnnotatedString(markdown, primaryColor, color)
    }

    Text(
        text = annotatedString,
        style = style,
        modifier = modifier
    )
}

fun parseMarkdownToAnnotatedString(
    markdown: String,
    primaryColor: Color,
    onSurfaceColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        val lines = markdown.lines()
        lines.forEachIndexed { index, rawLine ->
            val line = rawLine.trimEnd()
            val trimmedStart = line.trimStart()

            when {
                // H1 Başlık (# ...)
                trimmedStart.startsWith("# ") -> {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = primaryColor
                        )
                    ) {
                        append(trimmedStart.removePrefix("# ").trim())
                    }
                }
                // H2 Başlık (## ...)
                trimmedStart.startsWith("## ") -> {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = primaryColor
                        )
                    ) {
                        append(trimmedStart.removePrefix("## ").trim())
                    }
                }
                // H3 Başlık (### ...)
                trimmedStart.startsWith("### ") -> {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp,
                            color = primaryColor
                        )
                    ) {
                        append(trimmedStart.removePrefix("### ").trim())
                    }
                }
                // Yatay Ayırıcı Çizgi (---)
                trimmedStart == "---" || trimmedStart == "***" || trimmedStart == "___" -> {
                    withStyle(SpanStyle(color = onSurfaceColor.copy(alpha = 0.25f))) {
                        append("─────────────────────────")
                    }
                }
                else -> {
                    // Madde işareti kontrolü
                    var contentLine = line
                    if (trimmedStart.startsWith("- ") || (trimmedStart.startsWith("* ") && !trimmedStart.startsWith("**"))) {
                        val indent = line.takeWhile { it.isWhitespace() }
                        append("$indent• ")
                        contentLine = line.substring(indent.length + 2)
                    }

                    // Satır içi biçimlendirmeler: **kalın**, *italik*, `kod`
                    var cursor = 0
                    val len = contentLine.length
                    while (cursor < len) {
                        // Kalın: **metin**
                        if (contentLine.startsWith("**", cursor)) {
                            val nextEnd = contentLine.indexOf("**", cursor + 2)
                            if (nextEnd != -1) {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = onSurfaceColor)) {
                                    append(contentLine.substring(cursor + 2, nextEnd))
                                }
                                cursor = nextEnd + 2
                                continue
                            }
                        }

                        // Kod: `metin`
                        if (contentLine.startsWith("`", cursor)) {
                            val nextEnd = contentLine.indexOf("`", cursor + 1)
                            if (nextEnd != -1) {
                                withStyle(
                                    SpanStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        background = primaryColor.copy(alpha = 0.12f),
                                        color = primaryColor
                                    )
                                ) {
                                    append(" ${contentLine.substring(cursor + 1, nextEnd)} ")
                                }
                                cursor = nextEnd + 1
                                continue
                            }
                        }

                        // İtalik: *metin*
                        if (contentLine.startsWith("*", cursor) && !contentLine.startsWith("**", cursor)) {
                            val nextEnd = contentLine.indexOf("*", cursor + 1)
                            if (nextEnd != -1 && nextEnd > cursor + 1) {
                                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = onSurfaceColor.copy(alpha = 0.90f))) {
                                    append(contentLine.substring(cursor + 1, nextEnd))
                                }
                                cursor = nextEnd + 1
                                continue
                            }
                        }

                        // Normal karakter
                        append(contentLine[cursor])
                        cursor++
                    }
                }
            }

            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}

/**
 * Düşünme balonu animasyonu (Tufi AI yanıt hazırlarken).
 */
@Composable
private fun TufiThinkingBubble() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)),
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer(alpha = alpha)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tufi AI düşünüyor...",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TufiAISimpleCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}
