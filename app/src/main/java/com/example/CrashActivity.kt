package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ERROR_MESSAGE = "extra_error_message"
        const val EXTRA_STACK_TRACE = "extra_stack_trace"
        const val EXTRA_THREAD_NAME = "extra_thread_name"

        fun start(context: Context, throwable: Throwable) {
            val intent = Intent(context, CrashActivity::class.java).apply {
                putExtra(EXTRA_ERROR_MESSAGE, throwable.message ?: throwable.localizedMessage ?: "Bilinmeyen Hata")
                putExtra(EXTRA_STACK_TRACE, android.util.Log.getStackTraceString(throwable))
                putExtra(EXTRA_THREAD_NAME, Thread.currentThread().name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE) ?: "Bilinmeyen hata oluştu"
        val stackTrace = intent.getStringExtra(EXTRA_STACK_TRACE) ?: "Stack trace bulunamadı."
        val threadName = intent.getStringExtra(EXTRA_THREAD_NAME) ?: "Main"

        val crashTimestamp = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        val fullReport = buildString {
            appendLine("=== TUFIDESK WINDOWS BSOD CRASH DUMP ===")
            appendLine("Stop Code: STATUS_SYSTEM_PROCESS_TERMINATED (0xC000021A)")
            appendLine("Timestamp: $crashTimestamp")
            appendLine("Thread: $threadName")
            appendLine("OS / Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, SDK ${Build.VERSION.SDK_INT})")
            appendLine("----------------------------------------")
            appendLine("WHAT FAILED / ERROR MESSAGE:")
            appendLine(errorMessage)
            appendLine("----------------------------------------")
            appendLine("CALL STACK / STACK TRACE:")
            appendLine(stackTrace)
            appendLine("========================================")
        }

        setContent {
            WindowsBsodCrashScreen(
                errorMessage = errorMessage,
                fullReport = fullReport,
                onCopyReport = {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Windows BSOD Crash Dump", fullReport)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Loglar panoya kopyalandı! 📋", Toast.LENGTH_LONG).show()
                },
                onRestartApp = {
                    val restartIntent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(restartIntent)
                    finish()
                }
            )
        }
    }
}

/**
 * Windows BSOD (Blue Screen of Death) Çökme Ekranı
 * (Kullanıcının gönderdiği b0ff317a2dae6f93742288f992d53c92.jpg görseli baz alınmıştır)
 */
@Composable
fun WindowsBsodCrashScreen(
    errorMessage: String,
    fullReport: String,
    onCopyReport: () -> Unit,
    onRestartApp: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showDetails by remember { mutableStateOf(false) }
    var respectCount by remember { mutableIntStateOf(0) }
    val bsodBlue = Color(0xFF0078D7)

    // Yüzde Sayacı (%1 compelete -> %100 compelete)
    val progressAnim = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        progressAnim.animateTo(
            targetValue = 100f,
            animationSpec = tween(durationMillis = 3200, easing = LinearEasing)
        )
    }

    BackHandler {
        onRestartApp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bsodBlue)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. Dev Pixel Sad Face ":("
            Text(
                text = ":(",
                color = Color.White,
                fontSize = 86.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                lineHeight = 90.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Exact BSOD Funny Text From Photo
            Text(
                text = "your PC ran into problem and\nneeds to restart.\ni don't know what the F is going on.\nwe'll restart for you.",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 3. Exact Progress Text From Photo ("%d% compelete")
            Text(
                text = "${progressAnim.value.toInt()}% compelete",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 4. QR Code & Description Row (Görseldeki tam yerleşim)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Mavi-Beyaz Otantik QR Kod Görünümü
                BsodQrCode(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    qrBlue = bsodBlue
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "for more information about this issue\nand possible fixes, visit the QR code",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "if you sad T_T",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "press F for respect",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Eğlenceli "F" butonu
                            Surface(
                                shape = RoundedCornerShape(2.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color.White),
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        respectCount++
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "F",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            if (respectCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+$respectCount 🫡",
                                    color = Color(0xFFFFD700),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // =================================================================
            // 5. Windows Temasına Uygun Butonlar (Log Kopyala, Yeniden Başlat, Detay)
            // =================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // A) Windows Temalı Log Kopyalama Butonu (Ana Vurgu - Düz Beyaz Zemin)
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onCopyReport() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = bsodBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📋 COPY ERROR LOG (LOGLARI KOPYALA)",
                            color = bsodBlue,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // B) Windows Temalı Yeniden Başlat & Detay Butonları Yan Yana
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Yeniden Başlat Butonu (Beyaz Çerçeveli Windows Butonu)
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { onRestartApp() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RESTART PC",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Hata Detayları / Terminal Açma Butonu
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = if (showDetails) Color.White.copy(alpha = 0.25f) else Color.Transparent,
                        border = BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { showDetails = !showDetails }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (showDetails) "HIDE DUMP" else "SHOW DUMP",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // =================================================================
            // 6. Windows Komut Satırı (Command Prompt) Tarzında Detay Konsolu
            // =================================================================
            AnimatedVisibility(visible = showDetails) {
                Column(modifier = Modifier.padding(top = 18.dp)) {
                    // Windows CMD Başlık Çubuğu
                    Surface(
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                        color = Color(0xFF1F1F1F),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Command Prompt - C:\\WINDOWS\\system32\\crashdump.exe",
                                color = Color(0xFFCCCCCC),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Windows CMD Konsol Gövdesi
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp),
                        color = Color(0xFF0C0C0C),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        SelectionContainer {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "Microsoft Windows [Version 10.0.22631.3007]\n(c) Microsoft Corporation. All rights reserved.\n\nC:\\WINDOWS\\system32> tufidesk --dump-stacktrace\n\n$fullReport",
                                    color = Color(0xFFE5E5E5),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * Görseldeki Mavi-Beyaz Windows BSOD QR Kodunu Canvas ile Çizen Bileşen
 */
@Composable
private fun BsodQrCode(
    modifier: Modifier = Modifier,
    qrBlue: Color = Color(0xFF0078D7)
) {
    Canvas(modifier = modifier) {
        // Beyaz Zemin
        drawRect(color = Color.White)

        val gridCount = 21
        val cellSize = size.width / gridCount

        // 21x21 Standart QR Kod Matrisi (Köşe Tarayıcıları ve Veri Desenleri)
        val qrPattern = arrayOf(
            intArrayOf(1,1,1,1,1,1,1, 0, 1,0,1,0,1, 0, 1,1,1,1,1,1,1),
            intArrayOf(1,0,0,0,0,0,1, 0, 0,1,0,1,0, 0, 1,0,0,0,0,0,1),
            intArrayOf(1,0,1,1,1,0,1, 0, 1,1,0,0,1, 0, 1,0,1,1,1,0,1),
            intArrayOf(1,0,1,1,1,0,1, 0, 0,0,1,1,0, 0, 1,0,1,1,1,0,1),
            intArrayOf(1,0,1,1,1,0,1, 0, 1,0,1,0,1, 0, 1,0,1,1,1,0,1),
            intArrayOf(1,0,0,0,0,0,1, 0, 0,1,0,1,0, 0, 1,0,0,0,0,0,1),
            intArrayOf(1,1,1,1,1,1,1, 0, 1,0,1,0,1, 0, 1,1,1,1,1,1,1),
            intArrayOf(0,0,0,0,0,0,0, 0, 0,1,1,0,0, 0, 0,0,0,0,0,0,0),
            intArrayOf(1,0,1,0,1,1,1, 1, 0,0,1,1,0, 1, 1,0,1,0,1,1,0),
            intArrayOf(0,1,0,1,0,0,0, 1, 1,0,0,1,1, 0, 0,1,0,1,0,0,1),
            intArrayOf(1,1,0,0,1,1,0, 0, 1,1,0,0,1, 1, 0,0,1,1,0,1,0),
            intArrayOf(0,0,1,1,0,1,1, 1, 0,1,1,0,0, 0, 1,1,0,0,1,0,1),
            intArrayOf(1,0,1,0,1,0,0, 0, 1,0,1,1,1, 1, 0,1,0,1,0,1,0),
            intArrayOf(0,0,0,0,0,0,0, 0, 1,1,0,0,1, 0, 1,0,1,1,0,0,1),
            intArrayOf(1,1,1,1,1,1,1, 0, 0,0,1,1,0, 1, 0,1,0,0,1,1,0),
            intArrayOf(1,0,0,0,0,0,1, 0, 1,0,0,1,1, 0, 1,1,0,1,0,0,1),
            intArrayOf(1,0,1,1,1,0,1, 0, 0,1,1,0,0, 1, 0,0,1,0,1,1,0),
            intArrayOf(1,0,1,1,1,0,1, 0, 1,1,0,1,0, 0, 1,0,1,1,0,1,1),
            intArrayOf(1,0,1,1,1,0,1, 0, 0,0,1,0,1, 1, 0,1,0,0,1,0,0),
            intArrayOf(1,0,0,0,0,0,1, 0, 1,1,0,1,0, 0, 1,1,0,1,0,1,1),
            intArrayOf(1,1,1,1,1,1,1, 0, 0,1,1,0,1, 1, 0,0,1,1,1,0,1)
        )

        for (r in 0 until gridCount) {
            for (c in 0 until gridCount) {
                if (qrPattern[r][c] == 1) {
                    drawRect(
                        color = qrBlue,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}
