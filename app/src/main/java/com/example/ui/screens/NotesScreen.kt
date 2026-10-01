package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Note
import com.example.data.model.NoteFolder
import com.example.data.model.NoteType
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Google Keep pastel card colors
val KeepNoteColors = listOf(
    "#FFFFFF" to "Varsayılan",
    "#FFF9C4" to "Sarı",
    "#E8F5E9" to "Yeşil",
    "#E1F5FE" to "Mavi",
    "#F3E5F5" to "Mor",
    "#FCE4EC" to "Pembe",
    "#FFE0B2" to "Turuncu"
)

@Composable
fun NotesScreen(
    notes: List<Note>,
    folders: List<NoteFolder>,
    selectedFolderId: Long?,
    searchQuery: String,
    onSelectFolder: (Long?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onInsertNote: (Note) -> Unit,
    onUpdateNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onTogglePin: (Note) -> Unit,
    onCreateFolder: (name: String, colorHex: String) -> Unit,
    onDeleteFolder: (NoteFolder) -> Unit,
    canManage: Boolean = true,
    currentUserAssigned: String = "Tuna",
    modifier: Modifier = Modifier
) {
    var isFabMenuOpen by remember { mutableStateOf(false) }

    // Dialog states
    var showTextNoteDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showDrawingDialog by remember { mutableStateOf(false) }
    var showImageNoteDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }

    // Photo picker for Image Note
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            showImageNoteDialog = true
        }
    }

    // Partition notes into pinned and others
    val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
    val otherNotes = remember(notes) { notes.filter { !it.isPinned } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("notes_screen")
    ) {
        // Main Content List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notes_search_bar"),
                    placeholder = { Text("Notlarda veya raporlarda ara...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Temizle"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // Folder Filter Chips Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedFolderId == null,
                        onClick = { onSelectFolder(null) },
                        label = { Text("Tümü (${notes.size})") },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    folders.forEach { folder ->
                        val count = remember(notes, folder.id) {
                            notes.count { it.folderId == folder.id }
                        }
                        val isSelected = selectedFolderId == folder.id
                        val folderColor = remember(folder.colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(folder.colorHex))
                            } catch (_: Exception) {
                                Color(0xFF1B65D4)
                            }
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) onSelectFolder(null) else onSelectFolder(folder.id)
                            },
                            label = { Text("${folder.name} ($count)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = folderColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = folderColor.copy(alpha = 0.2f),
                                selectedLabelColor = folderColor
                            )
                        )
                    }

                    // Add Folder Chip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showFolderDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Klasör",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Empty state if no notes
            if (notes.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Henüz Not Eklenmedi",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Yeni ders notu, deneme neti raporu veya çizim eklemek için sağ alttaki '+' butonuna dokun.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Pinned Notes section
            if (pinnedNotes.isNotEmpty()) {
                item {
                    Text(
                        text = "SABİTLENENLER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                items(
                    items = pinnedNotes,
                    key = { it.id }
                ) { note ->
                    val folder = remember(note.folderId, folders) {
                        folders.find { it.id == note.folderId }
                    }
                    NoteCard(
                        note = note,
                        folder = folder,
                        onClick = { noteToEdit = note },
                        onTogglePin = { onTogglePin(note) },
                        onDelete = { onDeleteNote(note) }
                    )
                }
            }

            // Other Notes section
            if (otherNotes.isNotEmpty()) {
                if (pinnedNotes.isNotEmpty()) {
                    item {
                        Text(
                            text = "DİĞER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }
                }

                items(
                    items = otherNotes,
                    key = { it.id }
                ) { note ->
                    val folder = remember(note.folderId, folders) {
                        folders.find { it.id == note.folderId }
                    }
                    NoteCard(
                        note = note,
                        folder = folder,
                        onClick = { noteToEdit = note },
                        onTogglePin = { onTogglePin(note) },
                        onDelete = { onDeleteNote(note) }
                    )
                }
            }
        }

        // ================= FLOATING SPEED-DIAL POPUP MENU =================
        // Semi-transparent Scrim Backdrop when opened
        AnimatedVisibility(
            visible = isFabMenuOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isFabMenuOpen = false }
            )
        }

        // Floating Action Button & Speed Dial Options Stack at Bottom End
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 96.dp)
        ) {
            // Speed Dial Pill Buttons (Visible when open)
            AnimatedVisibility(
                visible = isFabMenuOpen,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Resim (Image Note)
                    SpeedDialPillButton(
                        text = "Resim",
                        icon = Icons.Default.Image,
                        onClick = {
                            isFabMenuOpen = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    // 2. Çizim (Drawing Sketch Note)
                    SpeedDialPillButton(
                        text = "Çizim",
                        icon = Icons.Default.Brush,
                        onClick = {
                            isFabMenuOpen = false
                            showDrawingDialog = true
                        }
                    )

                    // 3. Klasör (Folder Creation - Replaces "Ses")
                    SpeedDialPillButton(
                        text = "Klasör",
                        icon = Icons.Default.Folder,
                        onClick = {
                            isFabMenuOpen = false
                            showFolderDialog = true
                        }
                    )

                    // 4. Rapor (Trial Exam Net Score Report - Replaces red scribbled pill)
                    SpeedDialPillButton(
                        text = "Rapor",
                        icon = Icons.Default.Assessment,
                        onClick = {
                            isFabMenuOpen = false
                            showReportDialog = true
                        }
                    )

                    // 5. Metin (Standard Text Note)
                    SpeedDialPillButton(
                        text = "Metin",
                        icon = Icons.Default.TextFields,
                        onClick = {
                            isFabMenuOpen = false
                            showTextNoteDialog = true
                        }
                    )
                }
            }

            // Main FAB: Toggle '+' or '✕'
            FloatingActionButton(
                onClick = { isFabMenuOpen = !isFabMenuOpen },
                shape = CircleShape,
                containerColor = if (isFabMenuOpen) Color(0xFFA6D0F2) else MaterialTheme.colorScheme.primary,
                contentColor = if (isFabMenuOpen) Color(0xFF003355) else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(58.dp)
                    .testTag("notes_fab_toggle")
            ) {
                Icon(
                    imageVector = if (isFabMenuOpen) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (isFabMenuOpen) "Kapat" else "Yeni Ekle",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // ================= DIALOGS =================

    // 1. Text Note Dialog (Metin)
    if (showTextNoteDialog) {
        CreateOrEditNoteDialog(
            initialType = NoteType.TEXT,
            folders = folders,
            selectedFolderId = selectedFolderId,
            currentUserAssigned = currentUserAssigned,
            onDismiss = { showTextNoteDialog = false },
            onSave = { note ->
                onInsertNote(note)
                showTextNoteDialog = false
            }
        )
    }

    // 2. Folder Dialog (Klasör)
    if (showFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showFolderDialog = false },
            onSave = { name, colorHex ->
                onCreateFolder(name, colorHex)
                showFolderDialog = false
            }
        )
    }

    // 3. Report Dialog (Rapor - Deneme Netleri)
    if (showReportDialog) {
        CreateReportDialog(
            folders = folders,
            selectedFolderId = selectedFolderId,
            currentUserAssigned = currentUserAssigned,
            onDismiss = { showReportDialog = false },
            onSave = { note ->
                onInsertNote(note)
                showReportDialog = false
            }
        )
    }

    // 4. Drawing Canvas Dialog (Çizim)
    if (showDrawingDialog) {
        CreateDrawingDialog(
            folders = folders,
            selectedFolderId = selectedFolderId,
            currentUserAssigned = currentUserAssigned,
            onDismiss = { showDrawingDialog = false },
            onSave = { note ->
                onInsertNote(note)
                showDrawingDialog = false
            }
        )
    }

    // 5. Image Note Dialog (Resim)
    if (showImageNoteDialog) {
        CreateOrEditNoteDialog(
            initialType = NoteType.IMAGE,
            initialImageUri = selectedImageUri?.toString(),
            folders = folders,
            selectedFolderId = selectedFolderId,
            currentUserAssigned = currentUserAssigned,
            onDismiss = {
                showImageNoteDialog = false
                selectedImageUri = null
            },
            onSave = { note ->
                onInsertNote(note)
                showImageNoteDialog = false
                selectedImageUri = null
            }
        )
    }

    // 6. Edit/View Note Dialog
    noteToEdit?.let { note ->
        CreateOrEditNoteDialog(
            existingNote = note,
            folders = folders,
            selectedFolderId = note.folderId,
            currentUserAssigned = currentUserAssigned,
            onDismiss = { noteToEdit = null },
            onSave = { updated ->
                onUpdateNote(updated)
                noteToEdit = null
            },
            onDelete = {
                onDeleteNote(note)
                noteToEdit = null
            }
        )
    }
}

/**
 * Speed Dial Pill Button matching the user's uploaded screenshot.
 * Capsule shape, oceanic blue background, crisp white typography and icon.
 */
@Composable
fun SpeedDialPillButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = Color(0xFF265373), // Deep Oceanic Blue matching screenshot
        contentColor = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.shadow(6.dp, RoundedCornerShape(percent = 50))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 13.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = Color.White
            )
        }
    }
}

/**
 * Google Keep Style Note Card with soft pastel background, tag, pin button and snippet.
 */
@Composable
fun NoteCard(
    note: Note,
    folder: NoteFolder?,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = remember(note.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(note.colorHex))
        } catch (_: Exception) {
            Color.White
        }
    }

    val isDark = cardBg == Color.White && MaterialTheme.colorScheme.surface.hashCode() != 0

    val folderColor = remember(folder?.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(folder?.colorHex ?: "#1B65D4"))
        } catch (_: Exception) {
            Color(0xFF1B65D4)
        }
    }

    val dateFormatted = remember(note.updatedAtMillis) {
        val sdf = SimpleDateFormat("d MMM, HH:mm", Locale("tr"))
        sdf.format(Date(note.updatedAtMillis))
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.colorHex.equals("#FFFFFF", ignoreCase = true)) {
                MaterialTheme.colorScheme.surface
            } else {
                cardBg
            }
        ),
        border = BorderStroke(
            1.dp,
            if (note.isPinned) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (note.isPinned) 2.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Type Badge + Folder + Pin Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (folder != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = folderColor.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = folderColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = folder.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = folderColor
                                )
                            }
                        }
                    }

                    when (note.type) {
                        NoteType.REPORT -> {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE53935).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "📊 Rapor",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE53935),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        NoteType.DRAWING -> {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E88E5).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "✏️ Çizim",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1E88E5),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        NoteType.IMAGE -> {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF43A047).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "🖼️ Resimli",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF43A047),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        NoteType.TEXT -> {}
                    }
                }

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (note.isPinned) "Sabitlemeyi Kaldır" else "Sabitle",
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (note.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E1E1E),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF333333),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF757575)
                )

                if (note.assignedTo.isNotBlank()) {
                    Text(
                        text = note.assignedTo,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF757575)
                    )
                }
            }
        }
    }
}

/**
 * Text & Image Note Create/Edit Dialog
 */
@Composable
fun CreateOrEditNoteDialog(
    initialType: NoteType = NoteType.TEXT,
    initialImageUri: String? = null,
    existingNote: Note? = null,
    folders: List<NoteFolder>,
    selectedFolderId: Long?,
    currentUserAssigned: String,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var selectedFolder by remember { mutableStateOf(existingNote?.folderId ?: selectedFolderId) }
    var selectedColor by remember { mutableStateOf(existingNote?.colorHex ?: "#FFFFFF") }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existingNote != null) "Notu Düzenle" else "Yeni Not",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row {
                        IconButton(onClick = { isPinned = !isPinned }) {
                            Icon(
                                imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Sabitle",
                                tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (existingNote != null && onDelete != null) {
                            IconButton(onClick = onDelete) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Sil",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık") },
                    placeholder = { Text("Not başlığı girin...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Not İçeriği") },
                    placeholder = { Text("Ders notları, özetler veya hatırlatmalar...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Folder selector
                Text(
                    text = "Klasör Seç:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFolder == null,
                        onClick = { selectedFolder = null },
                        label = { Text("Yok") }
                    )
                    folders.forEach { f ->
                        FilterChip(
                            selected = selectedFolder == f.id,
                            onClick = { selectedFolder = f.id },
                            label = { Text(f.name) }
                        )
                    }
                }

                // Pastel color picker
                Text(
                    text = "Renk Seç:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KeepNoteColors.forEach { (hex, _) ->
                        val c = remember(hex) {
                            try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (_: Exception) {
                                Color.White
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (selectedColor.equals(hex, ignoreCase = true)) 2.5.dp else 1.dp,
                                    color = if (selectedColor.equals(hex, ignoreCase = true)) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor.equals(hex, ignoreCase = true)) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (hex == "#FFFFFF") Color.Black else Color.DarkGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("İptal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            val note = existingNote?.copy(
                                title = title.trim(),
                                content = content.trim(),
                                folderId = selectedFolder,
                                colorHex = selectedColor,
                                isPinned = isPinned,
                                updatedAtMillis = System.currentTimeMillis()
                            ) ?: Note(
                                title = title.trim(),
                                content = content.trim(),
                                folderId = selectedFolder,
                                colorHex = selectedColor,
                                isPinned = isPinned,
                                assignedTo = currentUserAssigned,
                                imageUri = initialImageUri,
                                type = initialType,
                                updatedAtMillis = System.currentTimeMillis()
                            )
                            onSave(note)
                        },
                        enabled = title.isNotBlank() || content.isNotBlank()
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

/**
 * Folder Creation Dialog
 */
@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#1B65D4") }

    val folderColors = listOf("#1B65D4", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899", "#06B6D4")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yeni Klasör Oluştur")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Klasör Adı") },
                    placeholder = { Text("Örn: LGS Matematik, Deneme Notları...") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Klasör Rengi:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    folderColors.forEach { hex ->
                        val c = remember(hex) { Color(android.graphics.Color.parseColor(hex)) }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == hex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = { onSave(folderName.trim(), selectedColor) },
                enabled = folderName.isNotBlank()
            ) {
                Text("Oluştur")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

/**
 * Trial Exam Net Score Report Dialog (Rapor)
 * Displays subjects, correct and incorrect inputs, computes net score = Doğru - (Yanlış / 3)
 */
@Composable
fun CreateReportDialog(
    folders: List<NoteFolder>,
    selectedFolderId: Long?,
    currentUserAssigned: String,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    var examTitle by remember { mutableStateOf("1. Kurumsal Deneme Sınavı") }

    // Subjects: Name, QuestionCount, D, Y
    var turkceD by remember { mutableStateOf("18") }
    var turkceY by remember { mutableStateOf("2") }

    var matD by remember { mutableStateOf("16") }
    var matY by remember { mutableStateOf("3") }

    var fenD by remember { mutableStateOf("19") }
    var fenY by remember { mutableStateOf("1") }

    var inkD by remember { mutableStateOf("10") }
    var inkY by remember { mutableStateOf("0") }

    var dinD by remember { mutableStateOf("10") }
    var dinY by remember { mutableStateOf("0") }

    var ingD by remember { mutableStateOf("9") }
    var ingY by remember { mutableStateOf("1") }

    fun calcNet(dStr: String, yStr: String): Double {
        val d = dStr.toIntOrNull() ?: 0
        val y = yStr.toIntOrNull() ?: 0
        return (d - (y / 3.0)).coerceAtLeast(0.0)
    }

    val totalNet = remember(turkceD, turkceY, matD, matY, fenD, fenY, inkD, inkY, dinD, dinY, ingD, ingY) {
        calcNet(turkceD, turkceY) +
                calcNet(matD, matY) +
                calcNet(fenD, fenY) +
                calcNet(inkD, inkY) +
                calcNet(dinD, dinY) +
                calcNet(ingD, ingY)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Deneme Sınavı Net Raporu",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedTextField(
                    value = examTitle,
                    onValueChange = { examTitle = it },
                    label = { Text("Deneme Adı / Yayın") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Total Net Live Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Toplam Net Skoru:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF047857)
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.2f", totalNet)} / 90.00",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF047857)
                        )
                    }
                }

                Text(
                    text = "Ders Netleri (Doğru / Yanlış):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Subject inputs
                SubjectNetRow("Türkçe (20 Soru)", turkceD, turkceY, { turkceD = it }, { turkceY = it })
                SubjectNetRow("Matematik (20 Soru)", matD, matY, { matD = it }, { matY = it })
                SubjectNetRow("Fen Bilimleri (20 Soru)", fenD, fenY, { fenD = it }, { fenY = it })
                SubjectNetRow("İnkılap Tarihi (10 Soru)", inkD, inkY, { inkD = it }, { inkY = it })
                SubjectNetRow("Din Kültürü (10 Soru)", dinD, dinY, { dinD = it }, { dinY = it })
                SubjectNetRow("İngilizce (10 Soru)", ingD, ingY, { ingD = it }, { ingY = it })

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("İptal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            val contentSummary = buildString {
                                appendLine("Tarih: ${SimpleDateFormat("d MMMM yyyy", Locale("tr")).format(Date())}")
                                appendLine("• Türkçe: $turkceD D, $turkceY Y (${String.format(Locale.US, "%.2f", calcNet(turkceD, turkceY))} Net)")
                                appendLine("• Matematik: $matD D, $matY Y (${String.format(Locale.US, "%.2f", calcNet(matD, matY))} Net)")
                                appendLine("• Fen Bilimleri: $fenD D, $fenY Y (${String.format(Locale.US, "%.2f", calcNet(fenD, fenY))} Net)")
                                appendLine("• İnkılap Tarihi: $inkD D, $inkY Y (${String.format(Locale.US, "%.2f", calcNet(inkD, inkY))} Net)")
                                appendLine("• Din Kültürü: $dinD D, $dinY Y (${String.format(Locale.US, "%.2f", calcNet(dinD, dinY))} Net)")
                                appendLine("• İngilizce: $ingD D, $ingY Y (${String.format(Locale.US, "%.2f", calcNet(ingD, ingY))} Net)")
                                appendLine("------------------------")
                                appendLine("🎯 Toplam Net: ${String.format(Locale.US, "%.2f", totalNet)} / 90")
                            }

                            val note = Note(
                                title = "🎯 $examTitle",
                                content = contentSummary,
                                colorHex = "#E8F5E9",
                                isPinned = true,
                                assignedTo = currentUserAssigned,
                                folderId = folders.find { it.name.contains("Deneme", ignoreCase = true) || it.name.contains("Rapor", ignoreCase = true) }?.id ?: selectedFolderId,
                                type = NoteType.REPORT,
                                reportData = totalNet.toString()
                            )
                            onSave(note)
                        },
                        enabled = examTitle.isNotBlank()
                    ) {
                        Text("Raporu Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectNetRow(
    subjectName: String,
    dVal: String,
    yVal: String,
    onDChange: (String) -> Unit,
    onYChange: (String) -> Unit
) {
    val net = remember(dVal, yVal) {
        val d = dVal.toIntOrNull() ?: 0
        val y = yVal.toIntOrNull() ?: 0
        (d - (y / 3.0)).coerceAtLeast(0.0)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = subjectName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Net: ${String.format(Locale.US, "%.2f", net)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.weight(1.4f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = dVal,
                onValueChange = onDChange,
                label = { Text("D") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = yVal,
                onValueChange = onYChange,
                label = { Text("Y") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

/**
 * Drawing Note Dialog (Canvas sketch pad)
 */
@Composable
fun CreateDrawingDialog(
    folders: List<NoteFolder>,
    selectedFolderId: Long?,
    currentUserAssigned: String,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    var title by remember { mutableStateOf("Çizim Notu ✏️") }
    val paths = remember { mutableStateListOf<Pair<Path, Color>>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var selectedColor by remember { mutableStateOf(Color.Black) }

    val penColors = listOf(Color.Black, Color(0xFF1E88E5), Color(0xFFE53935), Color(0xFF43A047), Color(0xFFF59E0B))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Çizim Notu Oluştur",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { paths.clear() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Temizle",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Çizim Başlığı") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Pen Colors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    penColors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { selectedColor = c }
                        ) {
                            if (selectedColor == c) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                // Drawing Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .pointerInput(selectedColor) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val path = Path().apply { moveTo(offset.x, offset.y) }
                                    currentPath = path
                                    paths.add(path to selectedColor)
                                },
                                onDrag = { change, _ ->
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                },
                                onDragEnd = {
                                    currentPath = null
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        paths.forEach { (path, color) ->
                            drawPath(
                                path = path,
                                color = color,
                                style = Stroke(width = 5.dp.toPx())
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("İptal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            val note = Note(
                                title = title.trim(),
                                content = "Çizim Notu (${paths.size} çizgi motifi kaydedildi)",
                                colorHex = "#E1F5FE",
                                folderId = selectedFolderId,
                                assignedTo = currentUserAssigned,
                                type = NoteType.DRAWING
                            )
                            onSave(note)
                        }
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}
