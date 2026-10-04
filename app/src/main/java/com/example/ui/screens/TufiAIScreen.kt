package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlaylistAddCheck
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
import com.example.data.ai.TufiAICoachAdvice
import com.example.data.ai.TufiAIService
import com.example.data.ai.TufiChatMessage
import com.example.data.model.Course
import com.example.data.model.HomeworkWithCourse
import kotlinx.coroutines.launch

@Composable
fun TufiAIScreen(
    courses: List<Course>,
    homeworks: List<HomeworkWithCourse>,
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

    // Keyboard and bottom dock height calculation
    val imeInsets = WindowInsets.ime.asPaddingValues()
    val isKeyboardOpen = imeInsets.calculateBottomPadding() > 0.dp
    // Klavye kapalıyken alttaki yüzen Bottom Bar ile çakışmaması için 84.dp boşluk bırakılır; klavye açıldığında 8.dp
    val bottomInputPadding = if (isKeyboardOpen) 8.dp else 84.dp

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("tufi_ai_screen")
    ) {
        // ==========================================
        // SEGMENTED TAB SWITCHER & ACTIONS (Üst Kısım)
        // Gereksiz ikinci Tufi AI başlığı kaldırıldı; doğrudan minimalist sekmeler yer alır.
        // ==========================================
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
                                imageVector = Icons.Default.Assignment,
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

            // Tab Bazlı Hızlı Aksiyon Butonları
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

        // ==========================================
        // TAB İÇERİĞİ
        // ==========================================
        if (selectedTab == 0) {
            // SOHBET ALANI (ChatGPT / Gemini / Claude Tarzı)
            Column(modifier = Modifier.fillMaxSize()) {
                // Mesaj Akışı veya Boş Karşılama
                if (chatMessages.isEmpty()) {
                    TufiChatEmptyState(
                        onPromptClick = { prompt -> sendMessage(prompt) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp),
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

                // Alt Kısım Giriş Çubuğu (Bottom Bar ile çakışmayan, klavye uyumlu yüzen hap)
                TufiChatInputBar(
                    inputText = inputText,
                    onInputTextChange = { inputText = it },
                    onSend = { sendMessage(inputText) },
                    isEnabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = bottomInputPadding)
                )
            }
        } else {
            // GÜNLÜK ÇALIŞMA PLANI (Ödev Raporu ve Strateji)
            val currentAdvice = advice ?: remember(homeworks) {
                TufiAIService.generateBuiltInAdvice(homeworks)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
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
                        icon = Icons.Default.Assignment,
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
                                    imageVector = Icons.Default.PlaylistAddCheck,
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

                // Sohbet Sekmesine Hızlı Geçiş Çağrısı
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
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

/**
 * Tufi AI yanıtlarını zengin biçimlendirilmiş Markdown olarak ekrana yansıtır.
 * - **kalın metin** -> FontWeight.Bold
 * - *italik metin* -> FontStyle.Italic
 * - `kod / kavram` -> Monospace arka planlı kutu
 * - # / ## / ### Başlıklar -> Vurgulu başlık boyutu
 * - - / * Madde işaretleri -> • İmli liste
 */
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

/**
 * Hızlı ve güvenilir Android Markdown -> AnnotatedString Ayrıştırıcısı.
 */
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
                    // Madde işareti kontrolü (- veya * ile başlayan satırlar)
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

                        // Kod / Vurgulu blok: `metin`
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

                        // İtalik: *metin* (ancak ** değilken)
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

/**
 * ChatGPT / Gemini / Claude tarzı minimalist alt giriş alanı (Floating Pill).
 */
@Composable
private fun TufiChatInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.60f)),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 3.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            val canSend = inputText.isNotBlank() && isEnabled

            FilledIconButton(
                onClick = onSend,
                enabled = canSend,
                shape = CircleShape,
                modifier = Modifier.size(38.dp),
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
