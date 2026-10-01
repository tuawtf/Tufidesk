package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Note
import com.example.data.model.NoteFolder
import com.example.data.model.NoteType
import com.example.data.model.UserAccount
import com.example.ui.components.UserFlowerAvatar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Google Keep Authentic Material Pastel Colors
val GoogleKeepColors = listOf(
    "#FFFFFF" to "Varsayılan",
    "#F28B82" to "Mercan",
    "#FBBC04" to "Şeftali",
    "#FFF475" to "Kum",
    "#CCFF90" to "Nane",
    "#A7FFEB" to "Adaçayı",
    "#CBF0F8" to "Açık Sis",
    "#AECBFA" to "Fırtına",
    "#D7AEFB" to "Alacakaranlık",
    "#FDCFE8" to "Çiçek",
    "#E6C9A8" to "Kil",
    "#E8EAED" to "Tebeşir"
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
    currentUser: UserAccount? = null,
    currentUserAssigned: String = "Tuna",
    isGridLayout: Boolean = true,
    onToggleLayoutGrid: (Boolean) -> Unit = {},
    showWordCount: Boolean = true,
    confirmDelete: Boolean = true,
    moveCheckedToBottom: Boolean = true,
    onSwitchProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isFabMenuOpen by remember { mutableStateOf(false) }

    // Dialog states
    var showTextNoteDialog by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }
    var showDrawingDialog by remember { mutableStateOf(false) }
    var showImageNoteDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var noteToDeleteConfirm by remember { mutableStateOf<Note?>(null) }

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
        // Main Google Keep Staggered Grid (2-column on phone, 1-column list option)
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(if (isGridLayout) 2 else 1),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            // Header Item: Authentic Google Keep Pill Search Bar + Folder/Label Chips
            item(span = StaggeredGridItemSpan.FullLine) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Google Keep Style Floating Pill Search Bar
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(28.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Ara",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = {
                                    Text(
                                        "Notlarınızda arayın...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("notes_search_bar"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )

                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Temizle",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Layout Toggle (Single Column vs Multi Column Grid)
                            IconButton(onClick = { onToggleLayoutGrid(!isGridLayout) }) {
                                Icon(
                                    imageVector = if (isGridLayout) Icons.Default.ViewAgenda else Icons.Default.GridView,
                                    contentDescription = if (isGridLayout) "Tek Sütun Liste" else "Çift Sütun Kılavuz",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Profile Avatar
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable { onSwitchProfile() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentUser != null) {
                                    UserFlowerAvatar(user = currentUser, size = 32.dp)
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = currentUserAssigned.take(1).uppercase(Locale.getDefault()),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Keep Labels / Folders Chips Row
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
                            shape = RoundedCornerShape(12.dp),
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
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = folderColor.copy(alpha = 0.2f),
                                    selectedLabelColor = folderColor
                                )
                            )
                        }

                        // Add Folder Chip ("+ Klasör")
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showFolderDialog = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
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
            }

            // Empty State
            if (notes.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Burada henüz not yok",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ders notu, kontrol listesi, çizim veya TYT deneme raporu eklemek için sağ alttaki '+' butonuna dokunun.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Pinned Notes Header
            if (pinnedNotes.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 6.dp, top = 8.dp, bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SABİTLENENLER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(
                    items = pinnedNotes,
                    key = { it.id },
                    span = { note -> if (note.type == NoteType.REPORT) StaggeredGridItemSpan.FullLine else StaggeredGridItemSpan.SingleLane }
                ) { note ->
                    if (note.type == NoteType.REPORT) {
                        TytExamReportCard(
                            note = note,
                            onClick = { noteToEdit = note },
                            onTogglePin = { onTogglePin(note) }
                        )
                    } else {
                        GoogleKeepNoteCard(
                            note = note,
                            folder = folders.find { it.id == note.folderId },
                            onClick = { noteToEdit = note },
                            onTogglePin = { onTogglePin(note) }
                        )
                    }
                }
            }

            // Other Notes Header
            if (pinnedNotes.isNotEmpty() && otherNotes.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        text = "DİĞERLERİ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp, top = 16.dp, bottom = 4.dp)
                    )
                }
            }

            // Other Notes Items
            items(
                items = otherNotes,
                key = { it.id },
                span = { note -> if (note.type == NoteType.REPORT) StaggeredGridItemSpan.FullLine else StaggeredGridItemSpan.SingleLane }
            ) { note ->
                if (note.type == NoteType.REPORT) {
                    TytExamReportCard(
                        note = note,
                        onClick = { noteToEdit = note },
                        onTogglePin = { onTogglePin(note) }
                    )
                } else {
                    GoogleKeepNoteCard(
                        note = note,
                        folder = folders.find { it.id == note.folderId },
                        onClick = { noteToEdit = note },
                        onTogglePin = { onTogglePin(note) }
                    )
                }
            }
        }

        // Dim Scrim Backdrop when Speed Dial is open
        if (isFabMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isFabMenuOpen = false }
            )
        }

        // Google Keep Floating Action Button + Speed Dial Menu
        // Lowered to normal natural FAB level directly above the island bottom bar
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 4.dp)
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
                    // 1. Rapor (TYT Practice Exam Report - Red Scratched in Screenshot)
                    KeepSpeedDialPill(
                        text = "Rapor",
                        icon = Icons.Default.Assessment,
                        accentColor = Color(0xFFE53935),
                        onClick = {
                            isFabMenuOpen = false
                            showReportDialog = true
                        }
                    )

                    // 2. Klasör (Folder Creation - in place of 'Ses')
                    KeepSpeedDialPill(
                        text = "Klasör",
                        icon = Icons.Default.Folder,
                        accentColor = Color(0xFF1E88E5),
                        onClick = {
                            isFabMenuOpen = false
                            showFolderDialog = true
                        }
                    )

                    // 3. Resim (Photo / Image Note)
                    KeepSpeedDialPill(
                        text = "Resim",
                        icon = Icons.Default.Image,
                        accentColor = Color(0xFF43A047),
                        onClick = {
                            isFabMenuOpen = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    // 4. Çizim (Drawing / Sketch)
                    KeepSpeedDialPill(
                        text = "Çizim",
                        icon = Icons.Default.Brush,
                        accentColor = Color(0xFF8E24AA),
                        onClick = {
                            isFabMenuOpen = false
                            showDrawingDialog = true
                        }
                    )

                    // 5. Liste (Checklist / To-Do Items)
                    KeepSpeedDialPill(
                        text = "Liste",
                        icon = Icons.Default.FormatListBulleted,
                        accentColor = Color(0xFFFB8C00),
                        onClick = {
                            isFabMenuOpen = false
                            showChecklistDialog = true
                        }
                    )

                    // 6. Metin (Standard Text Note)
                    KeepSpeedDialPill(
                        text = "Metin",
                        icon = Icons.Default.TextFields,
                        accentColor = MaterialTheme.colorScheme.primary,
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
                    .size(56.dp)
                    .shadow(6.dp, CircleShape)
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

    // ================= DIALOGS & EDITORS =================

    // 1. Text Note Dialog (Google Keep style)
    if (showTextNoteDialog) {
        KeepNoteEditDialog(
            initialNote = null,
            initialType = NoteType.TEXT,
            folders = folders,
            selectedFolderId = selectedFolderId,
            currentUserAssigned = currentUserAssigned,
            showWordCount = showWordCount,
            onDismiss = { showTextNoteDialog = false },
            onSave = { note ->
                onInsertNote(note)
                showTextNoteDialog = false
            }
        )
    }

    // 2. Checklist Dialog
    if (showChecklistDialog) {
        KeepChecklistEditDialog(
            initialNote = null,
            folders = folders,
            selectedFolderId = selectedFolderId,
            currentUserAssigned = currentUserAssigned,
            moveCheckedToBottom = moveCheckedToBottom,
            onDismiss = { showChecklistDialog = false },
            onSave = { note ->
                onInsertNote(note)
                showChecklistDialog = false
            }
        )
    }

    // 3. Drawing / Sketch Dialog
    if (showDrawingDialog) {
        KeepDrawingEditDialog(
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

    // 4. Image Note Dialog
    if (showImageNoteDialog && selectedImageUri != null) {
        KeepImageNoteEditDialog(
            imageUri = selectedImageUri!!,
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

    // 5. Folder Dialog (Klasör)
    if (showFolderDialog) {
        KeepCreateFolderDialog(
            onDismiss = { showFolderDialog = false },
            onSave = { name, colorHex ->
                onCreateFolder(name, colorHex)
                showFolderDialog = false
            }
        )
    }

    // 6. TYT Report Dialog (Deneme Sınavı Raporu)
    if (showReportDialog) {
        KeepReportEditDialog(
            existingNote = null,
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

    // Edit Existing Note Dialog (routes based on type)
    if (noteToEdit != null) {
        when (noteToEdit!!.type) {
            NoteType.CHECKLIST -> {
                KeepChecklistEditDialog(
                    initialNote = noteToEdit,
                    folders = folders,
                    selectedFolderId = noteToEdit!!.folderId,
                    currentUserAssigned = noteToEdit!!.assignedTo,
                    moveCheckedToBottom = moveCheckedToBottom,
                    onDismiss = { noteToEdit = null },
                    onSave = { updated ->
                        onUpdateNote(updated)
                        noteToEdit = null
                    }
                )
            }
            NoteType.REPORT -> {
                KeepReportEditDialog(
                    existingNote = noteToEdit,
                    folders = folders,
                    selectedFolderId = noteToEdit!!.folderId,
                    currentUserAssigned = noteToEdit!!.assignedTo,
                    onDismiss = { noteToEdit = null },
                    onSave = { updated ->
                        onUpdateNote(updated)
                        noteToEdit = null
                    }
                )
            }
            else -> {
                KeepNoteEditDialog(
                    initialNote = noteToEdit,
                    initialType = noteToEdit!!.type,
                    folders = folders,
                    selectedFolderId = noteToEdit!!.folderId,
                    currentUserAssigned = noteToEdit!!.assignedTo,
                    showWordCount = showWordCount,
                    onDismiss = { noteToEdit = null },
                    onSave = { updated ->
                        onUpdateNote(updated)
                        noteToEdit = null
                    }
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (noteToDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { noteToDeleteConfirm = null },
            title = { Text("Notu Sil") },
            text = { Text("'${noteToDeleteConfirm?.title?.ifBlank { "Bu not" }}' silinsin mi?") },
            confirmButton = {
                TextButton(onClick = {
                    noteToDeleteConfirm?.let { onDeleteNote(it) }
                    noteToDeleteConfirm = null
                }) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDeleteConfirm = null }) {
                    Text("İptal")
                }
            }
        )
    }
}

// ================= TYT DENEME RAPORU KARTI =================
// Deneme raporları bir not görünümü olarak değil, özel bir sınav karnesi Card'ı olarak görünür.

@Composable
fun TytExamReportCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = BorderStroke(1.5.dp, Color(0xFF2563EB).copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: TYT Badge + Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2563EB).copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "TYT DENEME RAPORU",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = Color(0xFF2563EB)
                        )
                    }
                }

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Sabitle",
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else Color(0xFF5F6368),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = note.title.ifBlank { "TYT Deneme Sınavı" },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Parse Total Net
            val totalNetRegex = "TOPLAM NET:\\s*([0-9.]+)\\s*/\\s*120".toRegex()
            val netMatch = totalNetRegex.find(note.content)
            val netValue = netMatch?.groupValues?.get(1) ?: run {
                val altRegex = "TOPLAM NET:\\s*([0-9.]+)".toRegex()
                altRegex.find(note.content)?.groupValues?.get(1) ?: "--"
            }
            val netNum = netValue.toDoubleOrNull() ?: 0.0
            val percent = Math.round((netNum / 120.0) * 100)

            // Prominent Net Score Dashboard
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "HESAPLANAN NET",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                        Text(
                            text = "$netValue / 120",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "%$percent",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4 Mini Subject Pills (TYT: Türkçe, Matematik, Fen, Sosyal)
            val turkceNet = "Türkçe[^:]*:\\s*([0-9.]+) Net".toRegex().find(note.content)?.groupValues?.get(1) ?: "--"
            val matNet = "Matematik[^:]*:\\s*([0-9.]+) Net".toRegex().find(note.content)?.groupValues?.get(1) ?: "--"
            val fenNet = "Fen[^:]*:\\s*([0-9.]+) Net".toRegex().find(note.content)?.groupValues?.get(1) ?: "--"
            val sosyalNet = "Sosyal[^:]*:\\s*([0-9.]+) Net".toRegex().find(note.content)?.groupValues?.get(1) ?: "--"

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniSubjectCard("Türkçe", turkceNet, 40, Modifier.weight(1f))
                    MiniSubjectCard("Matematik", matNet, 40, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniSubjectCard("Fen Bil.", fenNet, 20, Modifier.weight(1f))
                    MiniSubjectCard("Sosyal Bil.", sosyalNet, 20, Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Öğrenci: ${note.assignedTo}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val dateStr = remember(note.updatedAtMillis) {
                    val sdf = SimpleDateFormat("dd MMM", Locale("tr"))
                    sdf.format(Date(note.updatedAtMillis))
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MiniSubjectCard(name: String, net: String, totalQ: Int, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$net N",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ================= NOTE CARD (GOOGLE KEEP STYLE) =================

@Composable
fun GoogleKeepNoteCard(
    note: Note,
    folder: NoteFolder?,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = remember(note.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(note.colorHex))
        } catch (_: Exception) {
            Color(0xFFFFFFFF)
        }
    }
    val isWhiteOrLight = remember(cardColor) {
        cardColor == Color.White || (cardColor.red > 0.9f && cardColor.green > 0.9f && cardColor.blue > 0.9f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(
            1.dp,
            if (isWhiteOrLight) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            else cardColor.copy(alpha = 0.8f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Title and Pin Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF202124),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (note.isPinned) "Sabitlemeyi Kaldır" else "Sabitle",
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else Color(0xFF5F6368),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Body Preview based on Type
            when (note.type) {
                NoteType.CHECKLIST -> {
                    val lines = remember(note.content) { note.content.lines().take(5) }
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        lines.forEach { line ->
                            val isChecked = line.startsWith("[x] ") || line.startsWith("[X] ")
                            val itemText = if (isChecked) line.substring(4) else if (line.startsWith("[ ] ")) line.substring(4) else line
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (isChecked) Color(0xFF5F6368) else Color(0xFF202124),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = itemText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (isChecked) Color(0xFF80868B) else Color(0xFF202124),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                NoteType.DRAWING -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = null,
                                tint = Color(0xFF8E24AA),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
                else -> {
                    if (note.content.isNotBlank()) {
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3C4043),
                            maxLines = 7,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // Footer: Folder Label & Date
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (folder != null) {
                    val folderColor = remember(folder.colorHex) {
                        try {
                            Color(android.graphics.Color.parseColor(folder.colorHex))
                        } catch (_: Exception) {
                            Color(0xFF1E88E5)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = folderColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = folderColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = folderColor
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                val dateStr = remember(note.updatedAtMillis) {
                    val sdf = SimpleDateFormat("dd MMM", Locale("tr"))
                    sdf.format(Date(note.updatedAtMillis))
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF5F6368)
                )
            }
        }
    }
}

// ================= SPEED DIAL PILL BUTTON =================

@Composable
fun KeepSpeedDialPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 5.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ================= 1. TEXT NOTE EDIT DIALOG =================
// Renk paleti sağ üstteki palet ikonuna basılınca aşağıdan yukarı doğru açılır.
// Değişiklik yapılmadan çıkıldığında güncelleme uyarısı vermez.

@Composable
fun KeepNoteEditDialog(
    initialNote: Note?,
    initialType: NoteType,
    folders: List<NoteFolder>,
    selectedFolderId: Long?,
    currentUserAssigned: String,
    showWordCount: Boolean,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    // Snapshot of original values to accurately prevent unwanted "Not güncellendi" warnings
    val initialTitle = remember(initialNote) { initialNote?.title ?: "" }
    val initialContent = remember(initialNote) { initialNote?.content ?: "" }
    val initialFolder = remember(initialNote) { initialNote?.folderId ?: if (initialNote == null) selectedFolderId else null }
    val initialColor = remember(initialNote) { initialNote?.colorHex?.ifBlank { "#FFFFFF" } ?: "#FFFFFF" }
    val initialPinned = remember(initialNote) { initialNote?.isPinned ?: false }

    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    var selectedFolder by remember { mutableStateOf(initialFolder) }
    var selectedColorHex by remember { mutableStateOf(initialColor) }
    var isPinned by remember { mutableStateOf(initialPinned) }
    var showColorPaletteSheet by remember { mutableStateOf(false) }

    val bgColor = remember(selectedColorHex) {
        try {
            Color(android.graphics.Color.parseColor(selectedColorHex))
        } catch (_: Exception) {
            Color.White
        }
    }

    val isDirty = remember(title, content, selectedFolder, selectedColorHex, isPinned) {
        if (initialNote == null) {
            title.isNotBlank() || content.isNotBlank()
        } else {
            title.trim() != initialTitle.trim() ||
            content.trim() != initialContent.trim() ||
            selectedFolder != initialFolder ||
            !selectedColorHex.equals(initialColor, ignoreCase = true) ||
            isPinned != initialPinned
        }
    }

    fun handleCloseOrSave() {
        if (isDirty && (title.isNotBlank() || content.isNotBlank())) {
            val now = System.currentTimeMillis()
            val note = initialNote?.copy(
                title = title.trim(),
                content = content.trim(),
                folderId = selectedFolder,
                colorHex = selectedColorHex,
                isPinned = isPinned,
                updatedAtMillis = now
            ) ?: Note(
                title = title.trim(),
                content = content.trim(),
                folderId = selectedFolder,
                colorHex = selectedColorHex,
                isPinned = isPinned,
                assignedTo = currentUserAssigned,
                type = initialType,
                createdAtMillis = now,
                updatedAtMillis = now
            )
            onSave(note)
        } else {
            // Nothing changed or blank, exit without updating/saving!
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { handleCloseOrSave() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = bgColor
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Top Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { handleCloseOrSave() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri ve Kaydet",
                                tint = Color(0xFF202124)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isPinned = !isPinned }) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "Sabitle",
                                    tint = if (isPinned) MaterialTheme.colorScheme.primary else Color(0xFF5F6368)
                                )
                            }
                            // Color palette button: Toggles sliding bottom palette
                            IconButton(onClick = { showColorPaletteSheet = !showColorPaletteSheet }) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Renk Seçici",
                                    tint = if (showColorPaletteSheet) MaterialTheme.colorScheme.primary else Color(0xFF5F6368)
                                )
                            }
                            IconButton(onClick = { handleCloseOrSave() }) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Kaydet",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Folder selector chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedFolder == null,
                            onClick = { selectedFolder = null },
                            label = { Text("Klasörsüz") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        folders.forEach { f ->
                            FilterChip(
                                selected = selectedFolder == f.id,
                                onClick = { selectedFolder = if (selectedFolder == f.id) null else f.id },
                                label = { Text(f.name) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text(
                                "Başlık",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF5F6368)
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF202124)),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    // Content Input
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = {
                            Text(
                                "Not...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF5F6368)
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF202124)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    // Word count
                    if (showWordCount) {
                        val words = remember(content) { if (content.isBlank()) 0 else content.trim().split("\\s+".toRegex()).size }
                        Text(
                            text = "$words kelime • ${content.length} karakter",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF5F6368),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                // Scrim Backdrop when Color Sheet is open
                if (showColorPaletteSheet) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showColorPaletteSheet = false }
                    )
                }

                // Sliding Bottom Sheet for Color Selection
                AnimatedVisibility(
                    visible = showColorPaletteSheet,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 16.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Not Rengi Seçin",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(
                                    onClick = { showColorPaletteSheet = false },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Kapat",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            KeepColorSwatchesRow(
                                selectedColorHex = selectedColorHex,
                                onSelectColor = {
                                    selectedColorHex = it
                                    showColorPaletteSheet = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= 2. CHECKLIST EDIT DIALOG =================

@Composable
fun KeepChecklistEditDialog(
    initialNote: Note?,
    folders: List<NoteFolder>,
    selectedFolderId: Long?,
    currentUserAssigned: String,
    moveCheckedToBottom: Boolean,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    val initialTitle = remember(initialNote) { initialNote?.title ?: "" }
    val initialFolder = remember(initialNote) { initialNote?.folderId ?: if (initialNote == null) selectedFolderId else null }
    val initialColor = remember(initialNote) { initialNote?.colorHex?.ifBlank { "#FFFFFF" } ?: "#FFFFFF" }
    val initialPinned = remember(initialNote) { initialNote?.isPinned ?: false }
    val initialContent = remember(initialNote) { initialNote?.content?.trim() ?: "" }

    var title by remember { mutableStateOf(initialTitle) }
    var selectedFolder by remember { mutableStateOf(initialFolder) }
    var selectedColorHex by remember { mutableStateOf(initialColor) }
    var isPinned by remember { mutableStateOf(initialPinned) }
    var showColorPaletteSheet by remember { mutableStateOf(false) }

    // Parse initial checklist items: format "[x] Item" or "[ ] Item"
    val itemsList = remember {
        mutableStateListOf<Pair<Boolean, String>>().apply {
            if (initialNote != null && initialNote.content.isNotBlank()) {
                initialNote.content.lines().forEach { line ->
                    if (line.isNotBlank()) {
                        val isChecked = line.startsWith("[x] ") || line.startsWith("[X] ")
                        val text = if (isChecked) line.substring(4) else if (line.startsWith("[ ] ")) line.substring(4) else line
                        add(isChecked to text)
                    }
                }
            }
            if (isEmpty() && initialNote == null) {
                add(false to "")
            }
        }
    }

    val bgColor = remember(selectedColorHex) {
        try {
            Color(android.graphics.Color.parseColor(selectedColorHex))
        } catch (_: Exception) {
            Color.White
        }
    }

    fun buildContentString(): String {
        val list = if (moveCheckedToBottom) {
            itemsList.sortedBy { it.first }
        } else {
            itemsList
        }
        return list.filter { it.second.isNotBlank() }
            .joinToString("\n") { (checked, text) ->
                if (checked) "[x] $text" else "[ ] $text"
            }
    }

    fun handleCloseOrSave() {
        val contentStr = buildContentString().trim()
        val isDirty = if (initialNote == null) {
            title.isNotBlank() || contentStr.isNotBlank()
        } else {
            title.trim() != initialTitle.trim() ||
            contentStr != initialContent ||
            selectedFolder != initialFolder ||
            !selectedColorHex.equals(initialColor, ignoreCase = true) ||
            isPinned != initialPinned
        }

        if (isDirty && (title.isNotBlank() || contentStr.isNotBlank())) {
            val now = System.currentTimeMillis()
            val note = initialNote?.copy(
                title = title.trim(),
                content = contentStr,
                folderId = selectedFolder,
                colorHex = selectedColorHex,
                isPinned = isPinned,
                updatedAtMillis = now
            ) ?: Note(
                title = title.trim(),
                content = contentStr,
                folderId = selectedFolder,
                colorHex = selectedColorHex,
                isPinned = isPinned,
                assignedTo = currentUserAssigned,
                type = NoteType.CHECKLIST,
                createdAtMillis = now,
                updatedAtMillis = now
            )
            onSave(note)
        } else {
            // Nothing changed, exit quietly without updating!
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { handleCloseOrSave() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = bgColor
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { handleCloseOrSave() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri ve Kaydet",
                                tint = Color(0xFF202124)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isPinned = !isPinned }) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "Sabitle",
                                    tint = if (isPinned) MaterialTheme.colorScheme.primary else Color(0xFF5F6368)
                                )
                            }
                            IconButton(onClick = { showColorPaletteSheet = !showColorPaletteSheet }) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Renk Seçici",
                                    tint = if (showColorPaletteSheet) MaterialTheme.colorScheme.primary else Color(0xFF5F6368)
                                )
                            }
                            IconButton(onClick = { handleCloseOrSave() }) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Kaydet",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text(
                                "Başlık",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF5F6368)
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF202124)),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    // Checklist Items
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(itemsList) { index, item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = item.first,
                                    onCheckedChange = { checked ->
                                        itemsList[index] = checked to item.second
                                    }
                                )
                                OutlinedTextField(
                                    value = item.second,
                                    onValueChange = { text ->
                                        itemsList[index] = item.first to text
                                    },
                                    placeholder = { Text("Liste öğesi...") },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = if (item.first) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (item.first) Color(0xFF80868B) else Color(0xFF202124)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )
                                IconButton(onClick = { itemsList.removeAt(index) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Öğeyi Sil",
                                        tint = Color(0xFF80868B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        itemsList.add(false to "")
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Madde ekle",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Scrim Backdrop when Color Sheet is open
                if (showColorPaletteSheet) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showColorPaletteSheet = false }
                    )
                }

                // Sliding Bottom Sheet for Color Selection
                AnimatedVisibility(
                    visible = showColorPaletteSheet,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 16.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Liste Rengi Seçin",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(
                                    onClick = { showColorPaletteSheet = false },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Kapat",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            KeepColorSwatchesRow(
                                selectedColorHex = selectedColorHex,
                                onSelectColor = {
                                    selectedColorHex = it
                                    showColorPaletteSheet = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= 3. DRAWING / SKETCH EDIT DIALOG =================

@Composable
fun KeepDrawingEditDialog(
    folders: List<NoteFolder>,
    selectedFolderId: Long?,
    currentUserAssigned: String,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var strokeColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableStateOf(5f) }

    val paths = remember { mutableStateListOf<Pair<Path, Pair<Color, Float>>>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Çizim Başlığı...") },
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        if (paths.isNotEmpty()) paths.removeAt(paths.lastIndex)
                    }) {
                        Icon(Icons.Default.Undo, contentDescription = "Geri Al")
                    }
                    IconButton(onClick = { paths.clear() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Temizle")
                    }
                    IconButton(onClick = {
                        val now = System.currentTimeMillis()
                        val note = Note(
                            title = title.ifBlank { "Çizim Notu" },
                            content = "Serbest El Çizimi",
                            colorHex = "#FFFFFF",
                            assignedTo = currentUserAssigned,
                            type = NoteType.DRAWING,
                            drawingData = "drawing_${paths.size}_strokes",
                            createdAtMillis = now,
                            updatedAtMillis = now
                        )
                        onSave(note)
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Kaydet", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                // Drawing Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val path = Path().apply { moveTo(offset.x, offset.y) }
                                    currentPath = path
                                    paths.add(path to (strokeColor to strokeWidth))
                                },
                                onDrag = { change, _ ->
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                    val last = paths.lastOrNull()
                                    if (last != null) {
                                        paths[paths.lastIndex] = last.first to (strokeColor to strokeWidth)
                                    }
                                },
                                onDragEnd = {
                                    currentPath = null
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        paths.forEach { (path, style) ->
                            drawPath(
                                path = path,
                                color = style.first,
                                style = Stroke(width = style.second)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Brush Colors Row
                val penColors = listOf(Color.Black, Color(0xFF1E88E5), Color(0xFFE53935), Color(0xFF43A047), Color(0xFF8E24AA), Color(0xFFFB8C00))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    penColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (strokeColor == color) 3.dp else 1.dp,
                                    color = if (strokeColor == color) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { strokeColor = color }
                        )
                    }
                }
            }
        }
    }
}

// ================= 4. IMAGE NOTE EDIT DIALOG =================

@Composable
fun KeepImageNoteEditDialog(
    imageUri: Uri,
    folders: List<NoteFolder>,
    selectedFolderId: Long?,
    currentUserAssigned: String,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                    Text("Görsel Notu", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = {
                        val now = System.currentTimeMillis()
                        val note = Note(
                            title = title.ifBlank { "Görsel Notu" },
                            content = content,
                            imageUri = imageUri.toString(),
                            colorHex = "#FFFFFF",
                            folderId = selectedFolderId,
                            assignedTo = currentUserAssigned,
                            type = NoteType.IMAGE,
                            createdAtMillis = now,
                            updatedAtMillis = now
                        )
                        onSave(note)
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Kaydet", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                // Image Preview Placeholder
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Seçilen Fotoğraf Hazır", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Açıklama / Not") },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }
    }
}

// ================= 5. FOLDER DIALOG =================

@Composable
fun KeepCreateFolderDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#1E88E5") }

    val folderColors = listOf(
        "#1E88E5", "#43A047", "#E53935", "#8E24AA",
        "#FB8C00", "#00ACC1", "#3949AB", "#D81B60"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Klasör Oluştur 📁") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Klasör Adı") },
                    placeholder = { Text("Örn: TYT Hazırlık, Matematik...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Klasör Rengi:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    folderColors.forEach { hex ->
                        val c = remember(hex) { Color(android.graphics.Color.parseColor(hex)) }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 1.dp,
                                    color = if (selectedColor == hex) Color.Black else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    if (folderName.isNotBlank()) {
                        onSave(folderName.trim(), selectedColor)
                    }
                },
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

// ================= 6. TYT REPORT EDIT DIALOG (DENEME SINAVI RAPORU) =================
// TYT Sınavı formatı: 120 Soru (Türkçe 40, Temel Mat 40, Fen 20, Sosyal 20).
// 4 Yanlış 1 Doğruyu Götürür (Net = Doğru - Yanlış / 4).
// Tema değiştirme kaldırılmıştır; sabit ve profesyonel kart tasarımı kullanılır.
// Değişiklik yapılmadığında sessizce kapatır, gereksiz bildirim göstermez.

@Composable
fun KeepReportEditDialog(
    existingNote: Note? = null,
    folders: List<NoteFolder>,
    selectedFolderId: Long?,
    currentUserAssigned: String,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    // Initial parse from existing content if present
    fun parseSubjectD(subject: String, defaultVal: String): String {
        if (existingNote == null) return defaultVal
        val regex = "$subject[^:]*:\\s*(\\d+)\\s*D".toRegex()
        return regex.find(existingNote.content)?.groupValues?.get(1) ?: defaultVal
    }
    fun parseSubjectY(subject: String, defaultVal: String): String {
        if (existingNote == null) return defaultVal
        val regex = "$subject[^:]*:\\s*\\d+\\s*D,\\s*(\\d+)\\s*Y".toRegex()
        return regex.find(existingNote.content)?.groupValues?.get(1) ?: defaultVal
    }

    val initialTitle = remember(existingNote) { existingNote?.title ?: "TYT 1. Genel Deneme Sınavı" }
    val initialTurkceD = remember(existingNote) { parseSubjectD("Türkçe", "33") }
    val initialTurkceY = remember(existingNote) { parseSubjectY("Türkçe", "4") }
    val initialMatD = remember(existingNote) { parseSubjectD("Temel Matematik", "29") }
    val initialMatY = remember(existingNote) { parseSubjectY("Temel Matematik", "3") }
    val initialFenD = remember(existingNote) { parseSubjectD("Fen Bilimleri", "16") }
    val initialFenY = remember(existingNote) { parseSubjectY("Fen Bilimleri", "2") }
    val initialSosyalD = remember(existingNote) { parseSubjectD("Sosyal Bilimler", "17") }
    val initialSosyalY = remember(existingNote) { parseSubjectY("Sosyal Bilimler", "2") }

    val initialEvalNotes = remember(existingNote) {
        if (existingNote != null) {
            val noteLine = existingNote.content.lines().find { it.startsWith("Notlar:") }
            noteLine?.removePrefix("Notlar:")?.trim() ?: ""
        } else {
            "Matematikte geometri ve problemler tekrar edilecek. Paragraf netleri oldukça başarılı!"
        }
    }

    var examTitle by remember { mutableStateOf(initialTitle) }
    var turkceD by remember { mutableStateOf(initialTurkceD) }
    var turkceY by remember { mutableStateOf(initialTurkceY) }
    var matD by remember { mutableStateOf(initialMatD) }
    var matY by remember { mutableStateOf(initialMatY) }
    var fenD by remember { mutableStateOf(initialFenD) }
    var fenY by remember { mutableStateOf(initialFenY) }
    var sosyalD by remember { mutableStateOf(initialSosyalD) }
    var sosyalY by remember { mutableStateOf(initialSosyalY) }
    var evaluationNotes by remember { mutableStateOf(initialEvalNotes) }

    // TYT Kuralı: 4 Yanlış 1 Doğruyu Götürür!
    fun calculateTytNet(d: String, y: String): Double {
        val dogru = d.toDoubleOrNull() ?: 0.0
        val yanlis = y.toDoubleOrNull() ?: 0.0
        val net = dogru - (yanlis / 4.0)
        return if (net < 0) 0.0 else (Math.round(net * 100.0) / 100.0)
    }

    val totalNet = calculateTytNet(turkceD, turkceY) +
            calculateTytNet(matD, matY) +
            calculateTytNet(fenD, fenY) +
            calculateTytNet(sosyalD, sosyalY)

    val roundedTotalNet = Math.round(totalNet * 100.0) / 100.0
    val successPercent = Math.round((roundedTotalNet / 120.0) * 100)

    fun handleCloseOrSave() {
        val hasChanged = existingNote == null || (
            examTitle.trim() != initialTitle.trim() ||
            turkceD != initialTurkceD ||
            turkceY != initialTurkceY ||
            matD != initialMatD ||
            matY != initialMatY ||
            fenD != initialFenD ||
            fenY != initialFenY ||
            sosyalD != initialSosyalD ||
            sosyalY != initialSosyalY ||
            evaluationNotes.trim() != initialEvalNotes.trim()
        )

        if (hasChanged && examTitle.isNotBlank()) {
            val summaryText = buildString {
                appendLine("📊 Sınav: ${examTitle.trim()}")
                appendLine("Türkçe (40): $turkceD D, $turkceY Y • ${calculateTytNet(turkceD, turkceY)} Net")
                appendLine("Temel Matematik (40): $matD D, $matY Y • ${calculateTytNet(matD, matY)} Net")
                appendLine("Fen Bilimleri (20): $fenD D, $fenY Y • ${calculateTytNet(fenD, fenY)} Net")
                appendLine("Sosyal Bilimler (20): $sosyalD D, $sosyalY Y • ${calculateTytNet(sosyalD, sosyalY)} Net")
                appendLine()
                appendLine("🎯 TOPLAM NET: $roundedTotalNet / 120 (%$successPercent Başarı)")
                if (evaluationNotes.isNotBlank()) {
                    appendLine("Notlar: ${evaluationNotes.trim()}")
                }
            }
            val now = System.currentTimeMillis()
            val note = existingNote?.copy(
                title = examTitle.trim(),
                content = summaryText.trim(),
                updatedAtMillis = now
            ) ?: Note(
                title = examTitle.trim(),
                content = summaryText.trim(),
                colorHex = "#1E293B",
                folderId = selectedFolderId,
                assignedTo = currentUserAssigned,
                type = NoteType.REPORT,
                createdAtMillis = now,
                updatedAtMillis = now
            )
            onSave(note)
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { handleCloseOrSave() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { handleCloseOrSave() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                    Text(
                        text = "TYT Deneme Sınavı Raporu 🎯",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { handleCloseOrSave() }) {
                        Icon(Icons.Default.Check, contentDescription = "Kaydet", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // TYT Total Net Score Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "HESAPLANAN TYT NETİ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$roundedTotalNet",
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "120 Soru Üzerinden • 4 Yanlış 1 Doğru",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "%$successPercent",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = examTitle,
                    onValueChange = { examTitle = it },
                    label = { Text("TYT Deneme Adı") },
                    placeholder = { Text("Örn: TYT 1. Özdebir, Türkiye Geneli TYT...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TYT Dersleri (120 Soru):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Net = D - Y/4",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // TYT 4 Test: Türkçe (40), Matematik (40), Fen (20), Sosyal (20)
                TytSubjectScoreRow("Türkçe (40 Soru)", turkceD, turkceY, 40, onDChange = { turkceD = it }, onYChange = { turkceY = it }, net = calculateTytNet(turkceD, turkceY))
                TytSubjectScoreRow("Temel Matematik (40 Soru)", matD, matY, 40, onDChange = { matD = it }, onYChange = { matY = it }, net = calculateTytNet(matD, matY))
                TytSubjectScoreRow("Fen Bilimleri (20 Soru)", fenD, fenY, 20, onDChange = { fenD = it }, onYChange = { fenY = it }, net = calculateTytNet(fenD, fenY))
                TytSubjectScoreRow("Sosyal Bilimler (20 Soru)", sosyalD, sosyalY, 20, onDChange = { sosyalD = it }, onYChange = { sosyalY = it }, net = calculateTytNet(sosyalD, sosyalY))

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = evaluationNotes,
                    onValueChange = { evaluationNotes = it },
                    label = { Text("Sınav Analizi ve Hedef Notları") },
                    placeholder = { Text("Hangi konularda eksik var, süre nasıldı...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }
    }
}

@Composable
private fun TytSubjectScoreRow(
    subjectName: String,
    d: String,
    y: String,
    maxQuestions: Int,
    onDChange: (String) -> Unit,
    onYChange: (String) -> Unit,
    net: Double
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subjectName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Maks. $maxQuestions soru",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = d,
                    onValueChange = { if (it.length <= 2) onDChange(it) },
                    label = { Text("D") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(52.dp)
                )

                OutlinedTextField(
                    value = y,
                    onValueChange = { if (it.length <= 2) onYChange(it) },
                    label = { Text("Y") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(52.dp)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = "$net Net",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// ================= KEEP COLOR SWATCHES ROW =================
// 2 satır halinde 12 renk tam olarak ekrana sığar, yarım gözükme veya kesilme olmaz.

@Composable
fun KeepColorSwatchesRow(
    selectedColorHex: String,
    onSelectColor: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val chunked = GoogleKeepColors.chunked(6)
        chunked.forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowColors.forEach { (hex, name) ->
                    val color = remember(hex) {
                        try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (_: Exception) {
                            Color.White
                        }
                    }
                    val isSelected = selectedColorHex.equals(hex, ignoreCase = true)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectColor(hex) }
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFDADCE0),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = name,
                                    tint = if (hex == "#FFFFFF") Color.Black else Color(0xFF202124),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
