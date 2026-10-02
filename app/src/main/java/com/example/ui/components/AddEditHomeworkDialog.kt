package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Course
import com.example.data.model.Homework
import java.text.SimpleDateFormat
import com.example.data.model.UserAccount
import com.example.ui.HomeworkViewModel
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditHomeworkDialog(
    initialHomework: Homework? = null,
    initialDateMillis: Long? = null,
    courses: List<Course>,
    activeUser: UserAccount? = null,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        courseId: Long,
        title: String,
        description: String,
        dueDateMillis: Long,
        dueTimeText: String,
        priority: String,
        estimatedMinutes: Int,
        assignedTo: String
    ) -> Unit,
    onAddNewCourseRequest: () -> Unit
) {
    val isEditing = initialHomework != null

    var title by remember { mutableStateOf(initialHomework?.title ?: "") }
    var description by remember { mutableStateOf(initialHomework?.description ?: "") }
    var assignedTo by remember {
        mutableStateOf(
            initialHomework?.assignedTo ?: (activeUser?.name ?: "Tuna")
        )
    }
    var selectedCourseId by remember {
        mutableStateOf(
            initialHomework?.courseId ?: courses.firstOrNull()?.id ?: 0L
        )
    }

    var dueDateMillis by remember {
        mutableLongStateOf(
            initialHomework?.dueDateMillis ?: initialDateMillis ?: System.currentTimeMillis()
        )
    }

    var dueTimeText by remember { mutableStateOf(initialHomework?.dueTimeText ?: "23:59") }
    var priority by remember { mutableStateOf(initialHomework?.priority ?: "MEDIUM") }
    var estimatedMinutes by remember { mutableIntStateOf(initialHomework?.estimatedMinutes ?: 45) }

    var titleError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selected ->
                            // Preserve hour & minute
                            val calPrev = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
                            val calNew = Calendar.getInstance().apply {
                                timeInMillis = selected
                                set(Calendar.HOUR_OF_DAY, calPrev.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, calPrev.get(Calendar.MINUTE))
                            }
                            dueDateMillis = calNew.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("İptal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("add_edit_homework_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Ödevi Düzenle" else "Yeni Ödev Ekle",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Ödev Başlığı *") },
                    placeholder = { Text("Örn: Türev Alıştırmaları Sayfa 45") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) Text("Ödev başlığı boş bırakılamaz")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("homework_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Student Assignment
                Text(
                    text = "Kime Atanacak? *",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("Tuna", "🎓 Tuna", Color(0xFF2563EB)),
                        Triple("Elif", "🌸 Elif", Color(0xFFDB2777)),
                        Triple("Herkes", "👥 Herkes", Color(0xFF059669))
                    ).forEach { (studentId, label, color) ->
                        val isSelected = assignedTo.equals(studentId, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { assignedTo = studentId },
                            label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.2f),
                                selectedLabelColor = color
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Course Selection
                Text(
                    text = "Ders Seçimi *",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    courses.forEach { course ->
                        val isSelected = course.id == selectedCourseId
                        val cColor = remember(course.colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(course.colorHex))
                            } catch (_: Exception) {
                                Color(0xFF2563EB)
                            }
                        }

                        val subjectIcon = com.example.ui.util.CourseIconHelper.getIconForCourse(course.name)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCourseId = course.id },
                            label = { Text(course.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = subjectIcon,
                                    contentDescription = null,
                                    tint = cColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cColor.copy(alpha = 0.2f),
                                selectedLabelColor = cColor
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Quick Add Course Button
                    androidx.compose.material3.AssistChip(
                        onClick = onAddNewCourseRequest,
                        label = {
                            Text(
                                "Yeni Ders Ekle",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Yeni Ders Ekle",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Due Date Selector
                Text(
                    text = "Ödevin Yapılacağı / Planlanan Gün *",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Ödev takvimde ve günlük planda bu gün altında gösterilir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                val dateFormat = remember { SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr")) }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Tarih Seç",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = dateFormat.format(Date(dueDateMillis)),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        Text(
                            text = "Değiştir",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sunday Weekly Deadline Info Card
                val sundayDeadlineMillis = remember(dueDateMillis) {
                    HomeworkViewModel.getSundayOfWeek(dueDateMillis)
                }
                val sundayFormat = remember { SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr")) }
                val targetDayNameFormat = remember { SimpleDateFormat("EEEE", Locale("tr")) }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Otomatik Hafta Sonu Teslimi (Pazar)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Son Teslim: ${sundayFormat.format(Date(sundayDeadlineMillis))} ($dueTimeText)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${targetDayNameFormat.format(Date(dueDateMillis))} günü tamamlansa bile ödev Pazar gününe kadar gecikmiş sayılmaz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Date Preset Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val presets = listOf(
                        "Bugün" to 0,
                        "Yarın" to 1,
                        "3 Gün" to 3,
                        "1 Hafta" to 7
                    )
                    presets.forEach { (label, daysAhead) ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, daysAhead)
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                }
                                dueDateMillis = cal.timeInMillis
                            },
                            label = { Text(label) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time & Duration Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Time field
                    OutlinedTextField(
                        value = dueTimeText,
                        onValueChange = { dueTimeText = it },
                        label = { Text("Saat") },
                        placeholder = { Text("23:59") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Estimated minutes field
                    OutlinedTextField(
                        value = if (estimatedMinutes > 0) "$estimatedMinutes dk" else "",
                        onValueChange = { input ->
                            val num = input.filter { it.isDigit() }
                            estimatedMinutes = num.toIntOrNull() ?: 0
                        },
                        label = { Text("Tahmini Süre") },
                        placeholder = { Text("45 dk") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama & Notlar (İsteğe Bağlı)") },
                    placeholder = { Text("Sayfa aralıkları, teslim kuralları, proje detayları...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("İptal")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                                return@Button
                            }
                            if (selectedCourseId == 0L && courses.isNotEmpty()) {
                                selectedCourseId = courses.first().id
                            }
                            onSave(
                                initialHomework?.id ?: 0L,
                                selectedCourseId,
                                title,
                                description,
                                dueDateMillis,
                                dueTimeText,
                                priority,
                                estimatedMinutes,
                                assignedTo
                            )
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_homework_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (isEditing) "Güncelle" else "Ödevi Kaydet",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
