package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.data.model.NoteType
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExamCategory(val title: String) {
    ALL("Tümü"),
    TYT("TYT"),
    AYT("AYT"),
    BRANCH("Branş Denemesi")
}

data class BranchPreset(
    val name: String,
    val defaultQuestions: Int,
    val defaultDuration: Int
)

val PRESET_BRANCH_SUBJECTS = listOf(
    BranchPreset("Türkçe", 40, 45),
    BranchPreset("Matematik", 40, 50),
    BranchPreset("Fen Bilimleri", 20, 25),
    BranchPreset("Sosyal Bilimler", 20, 25),
    BranchPreset("Fizik", 14, 20),
    BranchPreset("Kimya", 13, 18),
    BranchPreset("Biyoloji", 13, 18),
    BranchPreset("Geometri", 10, 15),
    BranchPreset("Tarih", 10, 12),
    BranchPreset("Coğrafya", 10, 12),
    BranchPreset("Felsefe", 10, 10),
    BranchPreset("Din Kültürü", 10, 10)
)

data class SubjectScore(
    val name: String,
    val totalQuestions: Int,
    val correct: Int,
    val wrong: Int,
    val net: Double
)

data class ExamRecord(
    val id: Long,
    val examName: String,
    val category: String, // TYT, AYT, BRANS
    val dateMillis: Long,
    val totalNet: Double,
    val maxQuestions: Int,
    val targetNet: Double? = null,
    val durationMinutes: Int? = null,
    val difficulty: Int = 3, // 1 to 5
    val notes: String = "",
    val subjectScores: List<SubjectScore> = emptyList(),
    val rawNote: Note
)

/**
 * Material 3 Tasarım Sistemine Tam Uyumlu Denemeler Ekranı.
 */
@Composable
fun DenemeExamScreen(
    notes: List<Note>,
    currentUserAssigned: String,
    onInsertExam: (Note) -> Unit,
    onUpdateExam: (Note) -> Unit,
    onDeleteExam: (Note) -> Unit,
    onNotify: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    var selectedFilterCategory by rememberSaveable { mutableStateOf(ExamCategory.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }
    var examToEdit by remember { mutableStateOf<ExamRecord?>(null) }
    var examToDeleteConfirm by remember { mutableStateOf<ExamRecord?>(null) }

    // Parse note records with NoteType.REPORT into structured ExamRecords
    val examRecords = remember(notes) {
        notes.filter { it.type == NoteType.REPORT }.mapNotNull { note ->
            try {
                val json = if (note.reportData.isNullOrBlank()) JSONObject() else JSONObject(note.reportData)
                val examName = json.optString("examName", note.title.ifBlank { "Deneme Sınavı" })
                val rawCategory = json.optString("category", if (examName.contains("AYT", true)) "AYT" else if (examName.contains("Branş", true)) "Branş" else "TYT")
                val category = if (rawCategory.equals("LGS", ignoreCase = true)) "TYT" else rawCategory
                val totalNet = json.optDouble("totalNet", 0.0)
                val maxQuestions = json.optInt("maxQuestions", if (category == "AYT") 80 else if (category == "Branş" || category == "BRANS") 40 else 120)
                val targetNet = if (json.has("targetNet")) json.optDouble("targetNet") else null
                val duration = if (json.has("duration")) json.optInt("duration") else null
                val difficulty = json.optInt("difficulty", 3)
                val notesText = json.optString("notes", "")

                val subjects = mutableListOf<SubjectScore>()
                val subjectsObj = json.optJSONObject("subjects")
                if (subjectsObj != null) {
                    val keys = subjectsObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val sub = subjectsObj.getJSONObject(key)
                        val subName = sub.optString("name", key)
                        val totalQ = sub.optInt("total", 40)
                        val c = sub.optInt("d", 0)
                        val w = sub.optInt("y", 0)
                        val n = sub.optDouble("net", c - (w / 4.0))
                        subjects.add(SubjectScore(subName, totalQ, c, w, n))
                    }
                } else {
                    // Fallback to standard fields
                    if (json.has("turkce")) {
                        val t = json.getJSONObject("turkce")
                        val d = t.optInt("d", 0); val y = t.optInt("y", 0)
                        subjects.add(SubjectScore("Türkçe", 40, d, y, d - y/4.0))
                    }
                    if (json.has("matematik")) {
                        val m = json.getJSONObject("matematik")
                        val d = m.optInt("d", 0); val y = m.optInt("y", 0)
                        subjects.add(SubjectScore("Temel Matematik", 40, d, y, d - y/4.0))
                    }
                    if (json.has("fen")) {
                        val f = json.getJSONObject("fen")
                        val d = f.optInt("d", 0); val y = f.optInt("y", 0)
                        subjects.add(SubjectScore("Fen Bilimleri", 20, d, y, d - y/4.0))
                    }
                    if (json.has("sosyal")) {
                        val s = json.getJSONObject("sosyal")
                        val d = s.optInt("d", 0); val y = s.optInt("y", 0)
                        subjects.add(SubjectScore("Sosyal Bilimler", 20, d, y, d - y/4.0))
                    }
                }

                ExamRecord(
                    id = note.id,
                    examName = examName,
                    category = category,
                    dateMillis = note.createdAtMillis,
                    totalNet = totalNet,
                    maxQuestions = maxQuestions,
                    targetNet = targetNet,
                    durationMinutes = duration,
                    difficulty = difficulty,
                    notes = notesText,
                    subjectScores = subjects,
                    rawNote = note
                )
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.dateMillis }
    }

    val filteredExams = remember(examRecords, selectedFilterCategory) {
        when (selectedFilterCategory) {
            ExamCategory.ALL -> examRecords
            ExamCategory.TYT -> examRecords.filter { it.category.equals("TYT", ignoreCase = true) }
            ExamCategory.AYT -> examRecords.filter { it.category.equals("AYT", ignoreCase = true) }
            ExamCategory.BRANCH -> examRecords.filter { it.category.equals("BRANS", ignoreCase = true) || it.category.contains("Branş", ignoreCase = true) }
        }
    }

    // Stats calculations
    val totalExamsCount = filteredExams.size
    val averageNet = if (filteredExams.isNotEmpty()) filteredExams.map { it.totalNet }.average() else 0.0
    val highestNet = if (filteredExams.isNotEmpty()) filteredExams.maxOf { it.totalNet } else 0.0
    val latestExam = filteredExams.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("deneme_exam_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. M3 Üst İstatistik Kartı (Tonal Container)
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Deneme Sınav Takibi",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "TYT, AYT & Branş Netleri",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text(
                                    text = "$totalExamsCount Deneme",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3'lü M3 İstatistik Metrikleri
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Ortalama Net
                            M3StatPill(
                                title = "Ortalama Net",
                                value = String.format(Locale.getDefault(), "%.2f", averageNet),
                                icon = Icons.Default.BarChart,
                                iconColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )

                            // En Yüksek Net
                            M3StatPill(
                                title = "En Yüksek Net",
                                value = String.format(Locale.getDefault(), "%.2f", highestNet),
                                icon = Icons.Default.EmojiEvents,
                                iconColor = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )

                            // Son Net
                            M3StatPill(
                                title = "Son Deneme",
                                value = if (latestExam != null) String.format(Locale.getDefault(), "%.2f", latestExam.totalNet) else "-",
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                iconColor = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. M3 Filter Chips (Kategori Filtreleme)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ExamCategory.values()) { category ->
                        val isSelected = selectedFilterCategory == category
                        ElevatedFilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedFilterCategory = category
                            },
                            label = {
                                Text(
                                    text = category.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            elevation = FilterChipDefaults.elevatedFilterChipElevation(
                                elevation = if (isSelected) 2.dp else 0.dp
                            )
                        )
                    }
                }
            }

            // 3. Deneme Listesi Başlığı
            item {
                Text(
                    text = "Sınav Raporları & Karneler",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Deneme Kartları veya Boş Durum
            if (filteredExams.isEmpty()) {
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Henüz Deneme Kaydı Yok",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Aşağıdaki + butonuna basarak TYT, AYT veya LGS deneme netlerinizi kaydedebilirsiniz.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredExams, key = { it.id }) { exam ->
                    M3ExamCard(
                        exam = exam,
                        onEdit = { examToEdit = exam },
                        onDelete = { examToDeleteConfirm = exam },
                        onShare = {
                            val karneText = generateKarneText(exam)
                            clipboardManager.setText(AnnotatedString(karneText))
                            onNotify("📋 Deneme karnesi panoya kopyalandı!")
                        }
                    )
                }
            }
        }

        // Material 3 Floating Action Button (Yeni Deneme Ekle)
        FloatingActionButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                examToEdit = null
                showAddDialog = true
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 84.dp)
                .shadow(6.dp, RoundedCornerShape(16.dp))
                .testTag("add_exam_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Yeni Deneme",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    // Yeni Deneme Ekle / Düzenle Dialogu
    if (showAddDialog || examToEdit != null) {
        M3AddEditExamDialog(
            existingExam = examToEdit,
            currentUserAssigned = currentUserAssigned,
            onDismiss = {
                showAddDialog = false
                examToEdit = null
            },
            onSave = { note ->
                if (examToEdit != null) {
                    onUpdateExam(note)
                    onNotify("Deneme sınavı başarıyla güncellendi 🎯")
                } else {
                    onInsertExam(note)
                    onNotify("Yeni deneme sınavı kaydedildi 🎯")
                }
                showAddDialog = false
                examToEdit = null
            }
        )
    }

    // Silme Onay Dialogu
    if (examToDeleteConfirm != null) {
        val exam = examToDeleteConfirm!!
        AlertDialog(
            onDismissRequest = { examToDeleteConfirm = null },
            title = { Text("Denemeyi Sil", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = { Text("'${exam.examName}' deneme sınavı ve tüm ders netleri silinsin mi?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExam(exam.rawNote)
                        examToDeleteConfirm = null
                        onNotify("Deneme sınavı silindi")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { examToDeleteConfirm = null }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

/**
 * Material 3 İstatistik Hapı
 */
@Composable
private fun M3StatPill(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Material 3 Deneme Sınavı Kartı
 */
@Composable
private fun M3ExamCard(
    exam: ExamRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isExpanded by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("d MMMM yyyy", Locale("tr")) }
    val formattedDate = remember(exam.dateMillis) { dateFormatter.format(Date(exam.dateMillis)) }

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst Bölüm: Kategori Rozeti, Tarih ve Net
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = exam.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = exam.examName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Toplam Net M3 Rozeti
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f", exam.totalNet),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = "NET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            // Süre & Zorluk & Hedef Bilgileri
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (exam.durationMinutes != null && exam.durationMinutes > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${exam.durationMinutes} dk",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Zorluk Yıldızları
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i <= exam.difficulty) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                if (exam.targetNet != null && exam.targetNet > 0) {
                    val diff = exam.totalNet - exam.targetNet
                    Text(
                        text = if (diff >= 0) "Hedef aşıldı (+${String.format(Locale.getDefault(), "%.1f", diff)})" else "Hedefe ${String.format(Locale.getDefault(), "%.1f", -diff)} net kaldı",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (diff >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            // Ders Detayları (Açılır / Kapanır Bölüm)
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Text(
                        text = "Ders Net Dağılımı",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    exam.subjectScores.forEach { subject ->
                        Column(modifier = Modifier.padding(vertical = 5.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${subject.correct} D  ${subject.wrong} Y  •  ${String.format(Locale.getDefault(), "%.2f", subject.net)} Net",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val progress = if (subject.totalQuestions > 0) (subject.net / subject.totalQuestions).coerceIn(0.0, 1.0).toFloat() else 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }

                    if (exam.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "📝 Notlar & Hatalar: ${exam.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Alt Butonlar: Detayları Gör, Kopyala, Düzenle, Sil
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isExpanded) "Daha Az Göster" else "Ders Detayları",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Karne Kopyala
                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onShare()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Karne Kopyala", modifier = Modifier.size(16.dp))
                    }

                    // Düzenle
                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onEdit()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Düzenle", modifier = Modifier.size(16.dp))
                    }

                    // Sil
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDelete()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Yeni Deneme Ekleme & Düzenleme Dialogu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun M3AddEditExamDialog(
    existingExam: ExamRecord?,
    currentUserAssigned: String,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val categories = listOf("TYT", "AYT", "Branş")
    var selectedCategoryIndex by remember {
        val initialCat = existingExam?.category ?: "TYT"
        val idx = categories.indexOf(if (initialCat.equals("BRANS", ignoreCase = true)) "Branş" else initialCat)
        mutableIntStateOf(if (idx >= 0) idx else 0)
    }
    val examCategory = categories[selectedCategoryIndex]

    var examName by remember { mutableStateOf(existingExam?.examName ?: "TYT 1. Genel Deneme") }
    var durationText by remember { mutableStateOf(existingExam?.durationMinutes?.toString() ?: "135") }
    var targetNetText by remember { mutableStateOf(existingExam?.targetNet?.toString() ?: "95.0") }
    var difficulty by remember { mutableIntStateOf(existingExam?.difficulty ?: 3) }
    var notesText by remember { mutableStateOf(existingExam?.notes ?: "") }

    // Dersler listesi (Kategoriye göre dinamik ayarlanır)
    val subjectsState = remember(examCategory) {
        val list = mutableStateListOf<SubjectInputState>()
        when (examCategory) {
            "TYT" -> {
                list.add(SubjectInputState("Türkçe", 40, 32, 4))
                list.add(SubjectInputState("Temel Matematik", 40, 28, 3))
                list.add(SubjectInputState("Fen Bilimleri", 20, 15, 2))
                list.add(SubjectInputState("Sosyal Bilimler", 20, 16, 2))
            }
            "AYT" -> {
                list.add(SubjectInputState("Matematik", 40, 26, 4))
                list.add(SubjectInputState("Fizik", 14, 10, 2))
                list.add(SubjectInputState("Kimya", 13, 10, 1))
                list.add(SubjectInputState("Biyoloji", 13, 11, 1))
            }
            else -> {
                // Branş Denemesi varsayılan olarak 40 soruluk Türkçe ile başlar
                list.add(SubjectInputState("Türkçe", 40, 34, 3))
            }
        }
        list
    }

    // If editing, load previous subjects
    if (existingExam != null && existingExam.subjectScores.isNotEmpty()) {
        subjectsState.clear()
        existingExam.subjectScores.forEach {
            subjectsState.add(SubjectInputState(it.name, it.totalQuestions, it.correct, it.wrong))
        }
    }

    val divisor = 4.0
    val calculatedTotalNet = subjectsState.sumOf { (it.correct - (it.wrong / divisor)).coerceAtLeast(0.0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingExam != null) "Deneme Sınavını Düzenle" else "Yeni Deneme Sınavı Ekle 🎯",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Sınav Türü Segmented Button Row (M3 Standardı: TYT, AYT, Branş)
                item {
                    Text(
                        text = "Sınav Türü",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        categories.forEachIndexed { index, label ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = categories.size),
                                onClick = {
                                    selectedCategoryIndex = index
                                    if (label == "TYT") {
                                        examName = "TYT 1. Genel Deneme"
                                        durationText = "135"
                                        targetNetText = "95.0"
                                    } else if (label == "AYT") {
                                        examName = "AYT 1. Alan Denemesi"
                                        durationText = "180"
                                        targetNetText = "65.0"
                                    } else {
                                        examName = "Türkçe Branş Denemesi"
                                        durationText = "45"
                                        targetNetText = "35.0"
                                    }
                                },
                                selected = index == selectedCategoryIndex,
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                // ================= BRANŞ DENEMESİ İÇİN DERS SEÇİM BÖLÜMÜ =================
                if (examCategory == "Branş") {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Branş Dersi Seçimi",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Ders & Soru Sayısı",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // Hızlı Ders Seçim Çipleri (Türkçe: 40, Matematik: 40, Fen: 20, Sosyal: 20 vb.)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(PRESET_BRANCH_SUBJECTS) { preset ->
                                    val isSelected = subjectsState.firstOrNull()?.name == preset.name &&
                                            subjectsState.firstOrNull()?.totalQuestions == preset.defaultQuestions
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            subjectsState.clear()
                                            subjectsState.add(
                                                SubjectInputState(
                                                    name = preset.name,
                                                    totalQuestions = preset.defaultQuestions,
                                                    correct = (preset.defaultQuestions * 0.8).toInt(),
                                                    wrong = 2
                                                )
                                            )
                                            examName = "${preset.name} Branş Denemesi"
                                            durationText = preset.defaultDuration.toString()
                                            targetNetText = String.format(Locale.getDefault(), "%.1f", preset.defaultQuestions * 0.85)
                                        },
                                        label = {
                                            Text(
                                                text = "${preset.name} (${preset.defaultQuestions} Soru)",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Sınav Adı
                item {
                    OutlinedTextField(
                        value = examName,
                        onValueChange = { examName = it },
                        label = { Text("Deneme Adı & Yayın") },
                        placeholder = { Text("Örn: 3D TYT Türkiye Geneli 1") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Toplam Net Önizleme Çubuğu (M3 Tonal Container)
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Hesaplanan Toplam Net",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.2f Net", calculatedTotalNet),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text(
                                    text = "4 Yanlış 1 Doğru",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Ders Doğru - Yanlış Girişleri
                item {
                    Text(
                        text = "Ders Bazlı Net Girişi",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(subjectsState.size) { index ->
                    val subject = subjectsState[index]
                    val subNet = (subject.correct - (subject.wrong / divisor)).coerceAtLeast(0.0)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (examCategory == "Branş") {
                                // Branş denemesinde ders adı ve soru sayısını özelleştirebilme
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = subject.name,
                                        onValueChange = { newName ->
                                            subjectsState[index] = subject.copy(name = newName)
                                        },
                                        label = { Text("Branş Dersi") },
                                        modifier = Modifier.weight(1.5f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = if (subject.totalQuestions == 0) "" else subject.totalQuestions.toString(),
                                        onValueChange = { newTotal ->
                                            val t = newTotal.toIntOrNull() ?: 0
                                            subjectsState[index] = subject.copy(totalQuestions = t)
                                        },
                                        label = { Text("Soru Sayısı") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${subject.name} (${subject.totalQuestions} Soru)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.2f Net", subNet),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = if (subject.correct == 0) "" else subject.correct.toString(),
                                    onValueChange = { str ->
                                        val c = str.toIntOrNull() ?: 0
                                        subjectsState[index] = subject.copy(correct = c.coerceIn(0, subject.totalQuestions.coerceAtLeast(1)))
                                    },
                                    label = { Text("Doğru (D)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = if (subject.wrong == 0) "" else subject.wrong.toString(),
                                    onValueChange = { str ->
                                        val w = str.toIntOrNull() ?: 0
                                        val maxWrong = (subject.totalQuestions - subject.correct).coerceAtLeast(0)
                                        subjectsState[index] = subject.copy(wrong = w.coerceIn(0, maxWrong))
                                    },
                                    label = { Text("Yanlış (Y)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                // Süre (Dk) ve Hedef Net
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text("Süre (Dk)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = targetNetText,
                            onValueChange = { targetNetText = it },
                            label = { Text("Hedef Net") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // Sınav Notları
                item {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Sınav Analiz Notu & Hatalar") },
                        placeholder = { Text("Örn: Paragrafta süre yetişmedi, Geometride eksik formül...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                    // Construct JSON report
                    val json = JSONObject()
                    json.put("examName", examName.ifBlank { "Deneme Sınavı" })
                    json.put("category", examCategory)
                    json.put("totalNet", calculatedTotalNet)
                    json.put("duration", durationText.toIntOrNull() ?: 135)
                    json.put("targetNet", targetNetText.toDoubleOrNull() ?: 90.0)
                    json.put("difficulty", difficulty)
                    json.put("notes", notesText)

                    val subjectsObj = JSONObject()
                    val summaryLines = StringBuilder()
                    summaryLines.append("📊 Sınav: $examName ($examCategory)\n")

                    subjectsState.forEach { sub ->
                        val subJson = JSONObject()
                        subJson.put("name", sub.name)
                        subJson.put("total", sub.totalQuestions)
                        subJson.put("d", sub.correct)
                        subJson.put("y", sub.wrong)
                        val n = (sub.correct - (sub.wrong / divisor)).coerceAtLeast(0.0)
                        subJson.put("net", n)
                        subjectsObj.put(sub.name, subJson)

                        summaryLines.append("${sub.name}: ${sub.correct} D, ${sub.wrong} Y • ${String.format(Locale.getDefault(), "%.2f", n)} Net\n")
                    }
                    json.put("subjects", subjectsObj)
                    summaryLines.append("\n🎯 TOPLAM NET: ${String.format(Locale.getDefault(), "%.2f", calculatedTotalNet)}")

                    val note = Note(
                        id = existingExam?.id ?: 0,
                        title = examName.ifBlank { "$examCategory Deneme Sınavı" },
                        content = summaryLines.toString(),
                        reportData = json.toString(),
                        assignedTo = currentUserAssigned,
                        type = NoteType.REPORT,
                        createdAtMillis = existingExam?.dateMillis ?: System.currentTimeMillis(),
                        updatedAtMillis = System.currentTimeMillis()
                    )
                    onSave(note)
                }
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}

data class SubjectInputState(
    val name: String,
    val totalQuestions: Int,
    val correct: Int,
    val wrong: Int
)

/**
 * Formatlı karne metni oluşturucu
 */
private fun generateKarneText(exam: ExamRecord): String {
    val sb = StringBuilder()
    sb.append("🎯 *${exam.examName}* (${exam.category} Denemesi)\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━\n")
    exam.subjectScores.forEach { sub ->
        sb.append("• ${sub.name}: ${sub.correct} D / ${sub.wrong} Y ➔ ${String.format(Locale.getDefault(), "%.2f", sub.net)} Net\n")
    }
    sb.append("━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("🏆 *TOPLAM NET:* ${String.format(Locale.getDefault(), "%.2f", exam.totalNet)}\n")
    if (exam.durationMinutes != null && exam.durationMinutes > 0) {
        sb.append("⏱️ *Süre:* ${exam.durationMinutes} dakika\n")
    }
    if (exam.notes.isNotBlank()) {
        sb.append("📝 *Not:* ${exam.notes}\n")
    }
    sb.append("TufiDESK Deneme Takip Modülü")
    return sb.toString()
}
