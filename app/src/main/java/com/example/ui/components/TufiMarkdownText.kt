package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit

/**
 * Tufi AI Cevaplarını Şık ve Modern Bir Şekilde Render Eden Markdown Bileşeni.
 * Desteklenen Özellikler:
 * - Başlıklar (#, ##, ###)
 * - Kalın (**metin**) ve İtalik (*metin*)
 * - Kod Blokları (```kod```) ve Tek Tıkla Kopyalama
 * - Satır İçi Kod (`kod`)
 * - Madde İşaretleri (- , * , • ) ve Numaralı Listeler (1. , 2. )
 * - Alıntılar (> metin)
 * - Ayraç Çizgileri (---)
 */
@Composable
fun TufiMarkdownText(
    markdownText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFFECECEC)
) {
    val context = LocalContext.current
    val blocks = parseMarkdownBlocks(markdownText)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val style = when (block.level) {
                        1 -> MarkdownHeaderStyle(20.sp, FontWeight.Bold, Color.White, 8.dp)
                        2 -> MarkdownHeaderStyle(18.sp, FontWeight.Bold, Color(0xFF60A5FA), 6.dp)
                        else -> MarkdownHeaderStyle(16.sp, FontWeight.SemiBold, Color.White, 4.dp)
                    }
                    Spacer(modifier = Modifier.height(style.topSpace))
                    Text(
                        text = parseInlineMarkdown(block.text),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = style.fontSize,
                            fontWeight = style.fontWeight,
                            color = style.color,
                            lineHeight = (style.fontSize.value + 6).sp
                        )
                    )
                }

                is MarkdownBlock.CodeBlock -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E1E1E),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Üst Bar: Dil etiketi & Kopyala butonu
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF282828))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = block.language.ifBlank { "kod" },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFB0B0B0),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Kod", block.code))
                                        Toast.makeText(context, "Kod kopyalandı! 📋", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Kodu Kopyala",
                                        tint = Color(0xFFB0B0B0),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            // Kod Metni (Yatay kaydırılabilir)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = block.code,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF86EFAC), // Pastel yeşil kod vurgusu
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                    }
                }

                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.padding(start = 2.dp, end = 8.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.Blockquote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(22.dp)
                                .background(Color(0xFF3B82F6), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFCBD5E1),
                                fontStyle = FontStyle.Italic,
                                fontSize = 14.5.sp,
                                lineHeight = 21.sp
                            )
                        )
                    }
                }

                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        color = Color(0xFF333333),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                is MarkdownBlock.Paragraph -> {
                    if (block.text.isNotBlank()) {
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = textColor,
                                fontSize = 15.sp,
                                lineHeight = 23.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Satır içi Markdown ayrıştırıcı (**kalın**, *italik*, `kod`, [link](url))
 */
fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = text.length

        while (i < len) {
            when {
                // Kalın: **metin**
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White))
                        append(text.substring(i + 2, end))
                        pop()
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // Satır İçi Kod: `kod`
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        pushStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = Color(0xFF2A2A2A),
                                color = Color(0xFF93C5FD),
                                fontSize = 13.5.sp
                            )
                        )
                        append(" ${text.substring(i + 1, end)} ")
                        pop()
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // İtalik: *metin* (eğer tek yıldızsa)
                text.startsWith("*", i) && !text.startsWith("**", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1 && end > i + 1) {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(text.substring(i + 1, end))
                        pop()
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // Link: [başlık](url)
                text.startsWith("[", i) -> {
                    val closeBracket = text.indexOf("]", i + 1)
                    val openParen = if (closeBracket != -1 && closeBracket + 1 < len && text[closeBracket + 1] == '(') closeBracket + 1 else -1
                    val closeParen = if (openParen != -1) text.indexOf(")", openParen + 1) else -1

                    if (closeBracket != -1 && openParen != -1 && closeParen != -1) {
                        val title = text.substring(i + 1, closeBracket)
                        pushStyle(SpanStyle(color = Color(0xFF60A5FA), textDecoration = TextDecoration.Underline))
                        append(title)
                        pop()
                        i = closeParen + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

/**
 * Blok bazlı Markdown Ayrıştırıcı
 */
private sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
    data class Blockquote(val text: String) : MarkdownBlock()
    object Divider : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

private fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        // 1. Kod Bloğu (```lang ... ```)
        if (trimmed.startsWith("```")) {
            val language = trimmed.removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            if (i < lines.size && lines[i].trim().startsWith("```")) {
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(language, codeLines.joinToString("\n")))
            continue
        }

        // 2. Başlıklar
        when {
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Header(3, trimmed.removePrefix("### ").trim()))
                i++
                continue
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Header(2, trimmed.removePrefix("## ").trim()))
                i++
                continue
            }
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Header(1, trimmed.removePrefix("# ").trim()))
                i++
                continue
            }
        }

        // 3. Ayraç Çizgisi
        if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
            blocks.add(MarkdownBlock.Divider)
            i++
            continue
        }

        // 4. Alıntı
        if (trimmed.startsWith("> ")) {
            blocks.add(MarkdownBlock.Blockquote(trimmed.removePrefix("> ").trim()))
            i++
            continue
        }

        // 5. Madde İşareti Listesi (- , * , • )
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) {
            val content = trimmed.substring(2).trim()
            blocks.add(MarkdownBlock.BulletItem(content))
            i++
            continue
        }

        // 6. Numaralı Liste (1. , 2. )
        val numberRegex = Regex("""^(\d+)\.\s+(.*)$""")
        val numberMatch = numberRegex.find(trimmed)
        if (numberMatch != null) {
            val num = numberMatch.groupValues[1]
            val content = numberMatch.groupValues[2]
            blocks.add(MarkdownBlock.NumberedItem(num, content))
            i++
            continue
        }

        // 7. Normal Paragraf
        if (trimmed.isNotEmpty()) {
            blocks.add(MarkdownBlock.Paragraph(line))
        }
        i++
    }

    return blocks
}

private data class MarkdownHeaderStyle(
    val fontSize: TextUnit,
    val fontWeight: FontWeight,
    val color: Color,
    val topSpace: androidx.compose.ui.unit.Dp
)
