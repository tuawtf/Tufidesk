package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import android.widget.Toast
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.SelectAll
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
import androidx.compose.ui.graphics.luminance
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DrawingPoint(val x: Float, val y: Float)
data class DrawingStroke(val colorHex: String, val width: Float, val points: List<DrawingPoint>)

fun serializeDrawingStrokes(strokes: List<DrawingStroke>): String {
    return strokes.joinToString(";") { stroke ->
        val pts = stroke.points.joinToString(" ") { "${it.x.toInt()},${it.y.toInt()}" }
        "${stroke.colorHex}|${stroke.width}|$pts"
    }
}

fun parseDrawingStrokes(data: String): List<DrawingStroke> {
    if (data.isBlank()) return emptyList()
    val list = mutableListOf<DrawingStroke>()
    val strokeTokens = data.split(";")
    for (token in strokeTokens) {
        if (token.isBlank()) continue
        val parts = token.split("|")
        if (parts.size >= 3) {
            val colorHex = parts[0]
            val width = parts[1].toFloatOrNull() ?: 5f
            val pointsStr = parts[2].trim()
            val points = mutableListOf<DrawingPoint>()
            if (pointsStr.isNotBlank()) {
                val pointTokens = pointsStr.split(" ")
                for (pt in pointTokens) {
                    val coords = pt.split(",")
                    if (coords.size == 2) {
                        val x = coords[0].toFloatOrNull() ?: continue
                        val y = coords[1].toFloatOrNull() ?: continue
                        points.add(DrawingPoint(x, y))
                    }
                }
            }
            if (points.isNotEmpty()) {
                list.add(DrawingStroke(colorHex, width, points))
            }
        }
    }
    return list
}

fun saveImageToInternalStorage(context: Context, sourceUri: Uri): Uri {
    return try {
        val imagesDir = File(context.filesDir, "note_images").apply { mkdirs() }
        val destFile = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(destFile)
    } catch (_: Exception) {
        sourceUri
    }
}

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
    onUpdateFolder: (NoteFolder) -> Unit = {},
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
    onSelectionModeChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var isFabMenuOpen by remember { mutableStateOf(false) }

    // Multi-selection mode state
    var selectedNoteIds by remember { mutableStateOf(setOf<Long>()) }
    val isSelectionMode = selectedNoteIds.isNotEmpty()

    LaunchedEffect(isSelectionMode) {
        onSelectionModeChange(isSelectionMode)
    }

    BackHandler(enabled = isSelectionMode) {
        selectedNoteIds = emptySet()
    }

    // Drag and drop states
    val folderBounds = remember { mutableStateMapOf<Long, Rect>() }
    var draggedNote by remember { mutableStateOf<Note?>(null) }
    var dragPositionInRoot by remember { mutableStateOf<Offset?>(null) }
    var hoveredFolderId by remember { mutableStateOf<Long?>(null) }

    // Dialog states
    var showTextNoteDialog by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }
    var showDrawingDialog by remember { mutableStateOf(false) }
    var showImageNoteDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showMoveToFolderDialog by remember { mutableStateOf(false) }
    var showBatchColorDialog by remember { mutableStateOf(false) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    var folderOptionsToManage by remember { mutableStateOf<NoteFolder?>(null) }
    var folderToEdit by remember { mutableStateOf<NoteFolder?>(null) }
    var folderToDeleteConfirm by remember { mutableStateOf<NoteFolder?>(null) }

    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var noteToDeleteConfirm by remember { mutableStateOf<Note?>(null) }

    // Photo picker for Image Note
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedUri = saveImageToInternalStorage(context, uri)
            selectedImageUri = savedUri
            showImageNoteDialog = true
        }
    }

    // Filter notes by selected folder if any.
    // Deneme sınav raporları (NoteType.REPORT) tüm notlar ekranına dahil edilmez, sadece deneme sınavları klasöründe gösterilir.
    val currentFolderNotes = remember(notes, selectedFolderId) {
        if (selectedFolderId == null) {
            notes.filter { it.type != NoteType.REPORT }
        } else {
            notes.filter { it.folderId == selectedFolderId }
        }
    }

    // Partition notes into pinned and others
    val pinnedNotes = remember(currentFolderNotes) { currentFolderNotes.filter { it.isPinned } }
    val otherNotes = remember(currentFolderNotes) { currentFolderNotes.filter { !it.isPinned } }

    val regularFolders = remember(folders) {
        folders.filter { !it.name.contains("Deneme", ignoreCase = true) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("notes_screen")
    ) {
        // Main Google Keep Staggered Grid (2-column on phone, 1-column list option)
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(if (isGridLayout) 2 else 1),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = if (isSelectionMode) 100.dp else 80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            // Header Item: Google Keep Style Floating Pill Search Bar
            item(span = StaggeredGridItemSpan.FullLine) {
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
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleLayoutGrid(!isGridLayout)
                        }) {
                            Icon(
                                imageVector = if (isGridLayout) Icons.Default.ViewAgenda else Icons.Default.GridView,
                                contentDescription = if (isGridLayout) "Tek Sütun Liste" else "Çift Sütun Kılavuz",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // If a specific folder is currently active, show a Breadcrumb / Return Card
            if (selectedFolderId != null) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    val currentFolder = folders.find { it.id == selectedFolderId }
                    val isDeneme = currentFolder?.name?.contains("Deneme", ignoreCase = true) == true
                    val fColor = remember(currentFolder?.colorHex) {
                        try { Color(android.graphics.Color.parseColor(currentFolder?.colorHex ?: "#1B65D4")) } catch (_: Exception) { Color(0xFF1B65D4) }
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = fColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.2.dp, fColor.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSelectFolder(null)
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Tüm Notlara Dön",
                                    tint = fColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentFolder?.name ?: "Klasör",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "← Tüm notlara dön (${currentFolderNotes.size} not)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = fColor
                                    )
                                }
                            }

                            if (currentFolder != null && !isDeneme) {
                                IconButton(onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    folderToDeleteConfirm = currentFolder
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Klasörü Sil",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Root view: Folders displayed directly in grid as cards (1 or 2 grid layout)
            if (selectedFolderId == null && searchQuery.isBlank() && folders.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Row(
                        modifier = Modifier.padding(start = 6.dp, top = 6.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KLASÖRLER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showFolderDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Yeni Klasör", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Regular Folders (Material Theme styled, delete button removed, long-press to manage)
                items(
                    items = regularFolders,
                    key = { "folder_${it.id}" }
                ) { folder ->
                    val count = remember(notes, folder.id) {
                        notes.count { it.folderId == folder.id }
                    }
                    NoteFolderCard(
                        folder = folder,
                        noteCount = count,
                        isHovered = hoveredFolderId == folder.id,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectFolder(folder.id)
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            folderOptionsToManage = folder
                        },
                        modifier = Modifier.onGloballyPositioned { coords ->
                            folderBounds[folder.id] = coords.boundsInRoot()
                        }
                    )
                }
            }

            // Empty State
            if (currentFolderNotes.isEmpty()) {
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
                    val isSelected = selectedNoteIds.contains(note.id)
                    val clickAction = {
                        if (isSelectionMode) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedNoteIds = if (isSelected) selectedNoteIds - note.id else selectedNoteIds + note.id
                        } else {
                            noteToEdit = note
                        }
                    }
                    val longClickAction = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedNoteIds = if (isSelected) selectedNoteIds - note.id else selectedNoteIds + note.id
                    }

                    if (note.type == NoteType.REPORT) {
                        TytExamReportCard(
                            note = note,
                            isSelected = isSelected,
                            onClick = clickAction,
                            onLongClick = longClickAction,
                            onTogglePin = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTogglePin(note)
                            },
                            onStartDrag = { n, startPos ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                draggedNote = n
                                dragPositionInRoot = startPos
                            },
                            onDragAmount = { delta ->
                                val cur = dragPositionInRoot ?: Offset.Zero
                                val next = cur + delta
                                dragPositionInRoot = next
                                val target = folderBounds.entries.firstOrNull { (_, rect) -> rect.contains(next) }
                                if (hoveredFolderId != target?.key) {
                                    if (target != null) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    hoveredFolderId = target?.key
                                }
                            },
                            onDragEnd = {
                                val targetId = hoveredFolderId
                                val currentDragged = draggedNote
                                if (currentDragged != null && targetId != null) {
                                    val targetFolder = folders.find { it.id == targetId }
                                    if (targetFolder != null && currentDragged.folderId != targetFolder.id) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onUpdateNote(currentDragged.copy(folderId = targetFolder.id, updatedAtMillis = System.currentTimeMillis()))
                                        Toast.makeText(context, "'${currentDragged.title.ifBlank { "Rapor" }}' -> '${targetFolder.name}' klasörüne taşındı 📁", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                draggedNote = null
                                dragPositionInRoot = null
                                hoveredFolderId = null
                            },
                            onDragCancel = {
                                draggedNote = null
                                dragPositionInRoot = null
                                hoveredFolderId = null
                            }
                        )
                    } else {
                        GoogleKeepNoteCard(
                            note = note,
                            folder = folders.find { it.id == note.folderId },
                            isSelected = isSelected,
                            onClick = clickAction,
                            onLongClick = longClickAction,
                            onTogglePin = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTogglePin(note)
                            },
                            onStartDrag = { n, startPos ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                draggedNote = n
                                dragPositionInRoot = startPos
                            },
                            onDragAmount = { delta ->
                                val cur = dragPositionInRoot ?: Offset.Zero
                                val next = cur + delta
                                dragPositionInRoot = next
                                val target = folderBounds.entries.firstOrNull { (_, rect) -> rect.contains(next) }
                                if (hoveredFolderId != target?.key) {
                                    if (target != null) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    hoveredFolderId = target?.key
                                }
                            },
                            onDragEnd = {
                                val targetId = hoveredFolderId
                                val currentDragged = draggedNote
                                if (currentDragged != null && targetId != null) {
                                    val targetFolder = folders.find { it.id == targetId }
                                    if (targetFolder != null && currentDragged.folderId != targetFolder.id) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onUpdateNote(currentDragged.copy(folderId = targetFolder.id, updatedAtMillis = System.currentTimeMillis()))
                                        Toast.makeText(context, "'${currentDragged.title.ifBlank { "Not" }}' -> '${targetFolder.name}' klasörüne taşındı 📁", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                draggedNote = null
                                dragPositionInRoot = null
                                hoveredFolderId = null
                            },
                            onDragCancel = {
                                draggedNote = null
                                dragPositionInRoot = null
                                hoveredFolderId = null
                            }
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
                val isSelected = selectedNoteIds.contains(note.id)
                val clickAction = {
                    if (isSelectionMode) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedNoteIds = if (isSelected) selectedNoteIds - note.id else selectedNoteIds + note.id
                    } else {
                        noteToEdit = note
                    }
                }
                val longClickAction = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedNoteIds = if (isSelected) selectedNoteIds - note.id else selectedNoteIds + note.id
                }

                if (note.type == NoteType.REPORT) {
                    TytExamReportCard(
                        note = note,
                        isSelected = isSelected,
                        onClick = clickAction,
                        onLongClick = longClickAction,
                        onTogglePin = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTogglePin(note)
                        },
                        onStartDrag = { n, startPos ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            draggedNote = n
                            dragPositionInRoot = startPos
                        },
                        onDragAmount = { delta ->
                            val cur = dragPositionInRoot ?: Offset.Zero
                            val next = cur + delta
                            dragPositionInRoot = next
                            val target = folderBounds.entries.firstOrNull { (_, rect) -> rect.contains(next) }
                            if (hoveredFolderId != target?.key) {
                                if (target != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                hoveredFolderId = target?.key
                            }
                        },
                        onDragEnd = {
                            val targetId = hoveredFolderId
                            val currentDragged = draggedNote
                            if (currentDragged != null && targetId != null) {
                                val targetFolder = folders.find { it.id == targetId }
                                if (targetFolder != null && currentDragged.folderId != targetFolder.id) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onUpdateNote(currentDragged.copy(folderId = targetFolder.id, updatedAtMillis = System.currentTimeMillis()))
                                    Toast.makeText(context, "'${currentDragged.title.ifBlank { "Rapor" }}' -> '${targetFolder.name}' klasörüne taşındı 📁", Toast.LENGTH_SHORT).show()
                                }
                            }
                            draggedNote = null
                            dragPositionInRoot = null
                            hoveredFolderId = null
                        },
                        onDragCancel = {
                            draggedNote = null
                            dragPositionInRoot = null
                            hoveredFolderId = null
                        }
                    )
                } else {
                    GoogleKeepNoteCard(
                        note = note,
                        folder = folders.find { it.id == note.folderId },
                        isSelected = isSelected,
                        onClick = clickAction,
                        onLongClick = longClickAction,
                        onTogglePin = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTogglePin(note)
                        },
                        onStartDrag = { n, startPos ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            draggedNote = n
                            dragPositionInRoot = startPos
                        },
                        onDragAmount = { delta ->
                            val cur = dragPositionInRoot ?: Offset.Zero
                            val next = cur + delta
                            dragPositionInRoot = next
                            val target = folderBounds.entries.firstOrNull { (_, rect) -> rect.contains(next) }
                            if (hoveredFolderId != target?.key) {
                                if (target != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                hoveredFolderId = target?.key
                            }
                        },
                        onDragEnd = {
                            val targetId = hoveredFolderId
                            val currentDragged = draggedNote
                            if (currentDragged != null && targetId != null) {
                                val targetFolder = folders.find { it.id == targetId }
                                if (targetFolder != null && currentDragged.folderId != targetFolder.id) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onUpdateNote(currentDragged.copy(folderId = targetFolder.id, updatedAtMillis = System.currentTimeMillis()))
                                    Toast.makeText(context, "'${currentDragged.title.ifBlank { "Not" }}' -> '${targetFolder.name}' klasörüne taşındı 📁", Toast.LENGTH_SHORT).show()
                                }
                            }
                            draggedNote = null
                            dragPositionInRoot = null
                            hoveredFolderId = null
                        },
                        onDragCancel = {
                            draggedNote = null
                            dragPositionInRoot = null
                            hoveredFolderId = null
                        }
                    )
                }
            }
        }

        // ================= FLOATING CONTEXTUAL CONTROL BAR =================
        // Ekranda yüzer şekilde açılır; kullanıcı sayfanın en altında olsa bile yukarı çıkmadan silme, taşıma, renklendirme yapar.
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .navigationBarsPadding()
                .zIndex(95f)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.shadow(10.dp, RoundedCornerShape(28.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedNoteIds = emptySet()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Seçimi Temizle",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${selectedNoteIds.size}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Select all / deselect
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedNoteIds = if (selectedNoteIds.size == currentFolderNotes.size) {
                            emptySet()
                        } else {
                            currentFolderNotes.map { it.id }.toSet()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.SelectAll,
                            contentDescription = "Tümünü Seç",
                            tint = if (selectedNoteIds.size == currentFolderNotes.size) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Pin / Unpin
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val selectedNotes = notes.filter { selectedNoteIds.contains(it.id) }
                        val anyUnpinned = selectedNotes.any { !it.isPinned }
                        selectedNotes.forEach { note ->
                            onUpdateNote(note.copy(isPinned = anyUnpinned))
                        }
                    }) {
                        val allPinned = notes.filter { selectedNoteIds.contains(it.id) }.all { it.isPinned }
                        Icon(
                            imageVector = if (allPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Sabitle / Kaldır",
                            tint = if (allPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Move to folder
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showMoveToFolderDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.DriveFileMove,
                            contentDescription = "Klasöre Taşı",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Batch theme color
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showBatchColorDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Renk Değiştir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Batch delete
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (confirmDelete) {
                            showBatchDeleteConfirm = true
                        } else {
                            notes.filter { selectedNoteIds.contains(it.id) }.forEach { onDeleteNote(it) }
                            selectedNoteIds = emptySet()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // ================= DRAG & DROP FLOATING PREVIEW =================
        if (draggedNote != null && dragPositionInRoot != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(100f)
            ) {
                val pos = dragPositionInRoot!!
                val targetFolder = folders.find { it.id == hoveredFolderId }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 14.dp,
                    border = BorderStroke(
                        2.dp,
                        if (hoveredFolderId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = (pos.x - 110.dp.toPx()).coerceAtLeast(0f)
                            translationY = (pos.y - 70.dp.toPx()).coerceAtLeast(0f)
                            rotationZ = -3f
                            alpha = 0.95f
                        }
                        .width(220.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hoveredFolderId != null) Icons.Default.DriveFileMove else Icons.Default.Description,
                                contentDescription = null,
                                tint = if (hoveredFolderId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (targetFolder != null) "${targetFolder.name} Klasörüne Bırak 📥" else "Klasöre Taşı...",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (hoveredFolderId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = draggedNote!!.title.ifBlank { "Başlıksız Not" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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

        // Google Keep Floating Action Button + Speed Dial Menu (Hidden in Selection Mode)
        if (!isSelectionMode) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 84.dp)
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
                        // 1. Klasör
                        KeepSpeedDialPill(
                            text = "Klasör",
                            icon = Icons.Default.Folder,
                            accentColor = Color(0xFF1E88E5),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isFabMenuOpen = false
                                showFolderDialog = true
                            }
                        )

                        // 3. Resim
                        KeepSpeedDialPill(
                            text = "Resim",
                            icon = Icons.Default.Image,
                            accentColor = Color(0xFF43A047),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isFabMenuOpen = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )

                        // 4. Çizim
                        KeepSpeedDialPill(
                            text = "Çizim",
                            icon = Icons.Default.Brush,
                            accentColor = Color(0xFF8E24AA),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isFabMenuOpen = false
                                showDrawingDialog = true
                            }
                        )

                        // 5. Liste
                        KeepSpeedDialPill(
                            text = "Liste",
                            icon = Icons.Default.FormatListBulleted,
                            accentColor = Color(0xFFFB8C00),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isFabMenuOpen = false
                                showChecklistDialog = true
                            }
                        )

                        // 6. Metin
                        KeepSpeedDialPill(
                            text = "Metin",
                            icon = Icons.Default.TextFields,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isFabMenuOpen = false
                                showTextNoteDialog = true
                            }
                        )
                    }
                }

                // Main FAB: Toggle '+' or '✕' - Rounded square shape matching Homework FAB
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isFabMenuOpen = !isFabMenuOpen
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = if (isFabMenuOpen) Color(0xFFA6D0F2) else MaterialTheme.colorScheme.primary,
                    contentColor = if (isFabMenuOpen) Color(0xFF003355) else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp))
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
            initialNote = null,
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
            initialNote = null,
            initialImageUri = selectedImageUri,
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
            NoteType.DRAWING -> {
                KeepDrawingEditDialog(
                    initialNote = noteToEdit,
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
            NoteType.IMAGE -> {
                KeepImageNoteEditDialog(
                    initialNote = noteToEdit,
                    initialImageUri = null,
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

    // Batch Move to Folder Dialog
    if (showMoveToFolderDialog) {
        KeepMoveNotesToFolderDialog(
            selectedCount = selectedNoteIds.size,
            folders = folders,
            onDismiss = { showMoveToFolderDialog = false },
            onSelectTargetFolder = { targetFolderId ->
                val selectedNotes = notes.filter { selectedNoteIds.contains(it.id) }
                selectedNotes.forEach { note ->
                    onUpdateNote(note.copy(folderId = targetFolderId, updatedAtMillis = System.currentTimeMillis()))
                }
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val targetName = folders.find { it.id == targetFolderId }?.name ?: "Genel / Klasörsüz"
                Toast.makeText(context, "${selectedNotes.size} not '$targetName' klasörüne taşındı 📁", Toast.LENGTH_SHORT).show()
                selectedNoteIds = emptySet()
                showMoveToFolderDialog = false
            }
        )
    }

    // Batch Change Color Dialog
    if (showBatchColorDialog) {
        KeepBatchColorDialog(
            selectedCount = selectedNoteIds.size,
            onDismiss = { showBatchColorDialog = false },
            onColorSelected = { colorHex ->
                val selectedNotes = notes.filter { selectedNoteIds.contains(it.id) }
                selectedNotes.forEach { note ->
                    onUpdateNote(note.copy(colorHex = colorHex, updatedAtMillis = System.currentTimeMillis()))
                }
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                Toast.makeText(context, "${selectedNotes.size} notun teması güncellendi 🎨", Toast.LENGTH_SHORT).show()
                selectedNoteIds = emptySet()
                showBatchColorDialog = false
            }
        )
    }

    // Batch Delete Confirmation Dialog
    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text("Seçilen Notları Sil") },
            text = { Text("Seçili ${selectedNoteIds.size} not silinsin mi? Bu işlem geri alınamaz.") },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val toDelete = notes.filter { selectedNoteIds.contains(it.id) }
                    toDelete.forEach { onDeleteNote(it) }
                    selectedNoteIds = emptySet()
                    showBatchDeleteConfirm = false
                    Toast.makeText(context, "${toDelete.size} not silindi", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirm = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Folder Options Dialog (Triggered on folder long-press)
    if (folderOptionsToManage != null) {
        val folder = folderOptionsToManage!!
        val isDeneme = folder.name.contains("Deneme", ignoreCase = true)
        AlertDialog(
            onDismissRequest = { folderOptionsToManage = null },
            icon = {
                Icon(
                    imageVector = if (isDeneme) Icons.Default.Assessment else Icons.Default.Folder,
                    contentDescription = null,
                    tint = try { Color(android.graphics.Color.parseColor(folder.colorHex)) } catch (_: Exception) { MaterialTheme.colorScheme.primary }
                )
            },
            title = { Text(folder.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isDeneme) {
                        Text(
                            text = "Bu klasör TYT & AYT Deneme Sınavları ve Net Karneleri için özel olarak ayrılmış kalıcı bir sistem klasörüdür ve silinemez.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Bu klasör için yapmak istediğiniz işlemi seçin:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                folderToEdit = folder
                                folderOptionsToManage = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Klasörü Düzenle (Ad & Renk)")
                        }
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                folderToDeleteConfirm = folder
                                folderOptionsToManage = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Klasörü Sil", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { folderOptionsToManage = null }) {
                    Text("Tamam")
                }
            }
        )
    }

    // Edit Folder Dialog
    if (folderToEdit != null) {
        KeepEditFolderDialog(
            folder = folderToEdit!!,
            onDismiss = { folderToEdit = null },
            onSave = { updatedFolder ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onUpdateFolder(updatedFolder)
                folderToEdit = null
            }
        )
    }

    // Delete Folder Confirmation Dialog
    if (folderToDeleteConfirm != null) {
        val folder = folderToDeleteConfirm!!
        val isDeneme = folder.name.contains("Deneme", ignoreCase = true)
        if (!isDeneme) {
            AlertDialog(
                onDismissRequest = { folderToDeleteConfirm = null },
                title = { Text("Klasörü Sil") },
                text = { Text("'${folder.name}' klasörü ve içerisindeki notlar silinsin mi?") },
                confirmButton = {
                    TextButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteFolder(folder)
                        folderToDeleteConfirm = null
                    }) {
                        Text("Sil", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { folderToDeleteConfirm = null }) {
                        Text("İptal")
                    }
                }
            )
        } else {
            folderToDeleteConfirm = null
        }
    }

    // Delete Note Confirmation Dialog
    if (noteToDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { noteToDeleteConfirm = null },
            title = { Text("Notu Sil") },
            text = { Text("'${noteToDeleteConfirm?.title?.ifBlank { "Bu not" }}' silinsin mi?") },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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

// ================= 1. FEATURED LONG CARD: DENEME SINAVLARI (NON-DELETABLE) =================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DenemeExamFolderCard(
    folder: NoteFolder,
    noteCount: Int,
    isHovered: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) Color(0xFFDBEAFE) else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(
            if (isHovered) 2.5.dp else 1.2.dp,
            if (isHovered) MaterialTheme.colorScheme.primary else Color(0xFF2563EB).copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF2563EB).copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Deneme Sınavları",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2563EB).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "SİSTEM",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = Color(0xFF2563EB),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isHovered) "📥 Deneme Raporunu Buraya Bırak" else "$noteCount Deneme Raporu • TYT & AYT Net Takibi 📊",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                tint = Color(0xFF2563EB).copy(alpha = 0.75f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ================= 2. REGULAR NOTE FOLDER CARDS (MATERIAL 3 STYLED) =================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteFolderCard(
    folder: NoteFolder,
    noteCount: Int,
    isHovered: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val folderColor = remember(folder.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(folder.colorHex))
        } catch (_: Exception) {
            Color(0xFF1B65D4)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) folderColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(
            if (isHovered) 2.dp else 1.dp,
            if (isHovered) folderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = folderColor.copy(alpha = 0.18f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = folderColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isHovered) "📥 Notu Buraya Bırak" else "$noteCount not",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isHovered) folderColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ================= 3. BATCH & FOLDER DIALOGS =================

@Composable
fun KeepMoveNotesToFolderDialog(
    selectedCount: Int,
    folders: List<NoteFolder>,
    onDismiss: () -> Unit,
    onSelectTargetFolder: (Long?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$selectedCount Notu Klasöre Taşı 📁") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Option: Root / Genel
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectTargetFolder(null) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Genel (Klasörsüz)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                }

                items(folders) { folder ->
                    val fColor = remember(folder.colorHex) {
                        try { Color(android.graphics.Color.parseColor(folder.colorHex)) } catch (_: Exception) { Color(0xFF1B65D4) }
                    }
                    val isDeneme = folder.name.contains("Deneme", ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = fColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, fColor.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectTargetFolder(folder.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDeneme) Icons.Default.Assessment else Icons.Default.Folder,
                                contentDescription = null,
                                tint = fColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun KeepBatchColorDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$selectedCount Notun Temasını Değiştir 🎨") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tüm seçili notlar için bir renk teması seçin:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp
                ) {
                    items(GoogleKeepColors) { (hex, name) ->
                        val isDefault = hex == "#FFFFFF"
                        val c = remember(hex) {
                            try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.White }
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (isDefault) MaterialTheme.colorScheme.surfaceVariant else c,
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable { onColorSelected(hex) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isDefault) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = name,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun KeepEditFolderDialog(
    folder: NoteFolder,
    onDismiss: () -> Unit,
    onSave: (NoteFolder) -> Unit
) {
    var name by remember { mutableStateOf(folder.name) }
    var selectedColor by remember { mutableStateOf(folder.colorHex) }

    val folderColors = listOf(
        "#1E88E5", "#43A047", "#E53935", "#8E24AA",
        "#FB8C00", "#00ACC1", "#3949AB", "#D81B60"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Klasörü Düzenle 📁") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Klasör Adı") },
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
                                    width = if (selectedColor.equals(hex, ignoreCase = true)) 3.dp else 1.dp,
                                    color = if (selectedColor.equals(hex, ignoreCase = true)) Color.Black else Color.Transparent,
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
                    if (name.isNotBlank()) {
                        onSave(folder.copy(name = name.trim(), colorHex = selectedColor))
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun AddFolderCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Yeni Klasör Ekle",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ================= TYT DENEME RAPORU KARTI =================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TytExamReportCard(
    note: Note,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onTogglePin: () -> Unit,
    onStartDrag: ((Note, Offset) -> Unit)? = null,
    onDragAmount: ((Offset) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    onDragCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var cardOffsetInRoot by remember { mutableStateOf(Offset.Zero) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(1.5.dp, Color(0xFF2563EB).copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords -> cardOffsetInRoot = coords.boundsInRoot().topLeft }
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: TYT Badge + Drag Handle + Pin / Selection
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Seçildi",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp).padding(end = 4.dp)
                        )
                    }

                    // Drag Handle icon for drag & drop
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Sürükle ve Klasöre Bırak",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(24.dp)
                            .padding(horizontal = 2.dp)
                            .pointerInput(note.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { localOffset ->
                                        onStartDrag?.invoke(note, cardOffsetInRoot + localOffset)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onDragAmount?.invoke(dragAmount)
                                    },
                                    onDragEnd = { onDragEnd?.invoke() },
                                    onDragCancel = { onDragCancel?.invoke() }
                                )
                            }
                    )

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

// ================= DRAWING PREVIEW CANVAS =================

@Composable
fun DrawingPreviewCanvas(strokes: List<DrawingStroke>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize().padding(4.dp)) {
        if (strokes.isEmpty()) return@Canvas
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        for (stroke in strokes) {
            for (p in stroke.points) {
                if (p.x < minX) minX = p.x
                if (p.x > maxX) maxX = p.x
                if (p.y < minY) minY = p.y
                if (p.y > maxY) maxY = p.y
            }
        }
        val dataWidth = (maxX - minX).coerceAtLeast(1f)
        val dataHeight = (maxY - minY).coerceAtLeast(1f)
        val scale = minOf(size.width / dataWidth, size.height / dataHeight).coerceAtMost(1f)
        val offsetX = (size.width - dataWidth * scale) / 2f - minX * scale
        val offsetY = (size.height - dataHeight * scale) / 2f - minY * scale

        for (stroke in strokes) {
            val strokeColor = try {
                Color(android.graphics.Color.parseColor(stroke.colorHex))
            } catch (_: Exception) {
                Color.Black
            }
            val scaledWidth = (stroke.width * scale).coerceAtLeast(1.5f)
            if (stroke.points.size == 1) {
                val p = stroke.points[0]
                drawCircle(
                    color = strokeColor,
                    radius = scaledWidth / 2f,
                    center = androidx.compose.ui.geometry.Offset(p.x * scale + offsetX, p.y * scale + offsetY)
                )
            } else if (stroke.points.size > 1) {
                val path = Path()
                path.moveTo(stroke.points[0].x * scale + offsetX, stroke.points[0].y * scale + offsetY)
                for (i in 1 until stroke.points.size) {
                    path.lineTo(stroke.points[i].x * scale + offsetX, stroke.points[i].y * scale + offsetY)
                }
                drawPath(
                    path = path,
                    color = strokeColor,
                    style = Stroke(width = scaledWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

// ================= NOTE CARD (GOOGLE KEEP STYLE) =================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoogleKeepNoteCard(
    note: Note,
    folder: NoteFolder?,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onTogglePin: () -> Unit,
    onStartDrag: ((Note, Offset) -> Unit)? = null,
    onDragAmount: ((Offset) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    onDragCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var cardOffsetInRoot by remember { mutableStateOf(Offset.Zero) }

    val isDefaultColor = remember(note.colorHex) {
        note.colorHex.isBlank() || note.colorHex.equals("#FFFFFF", ignoreCase = true) || note.colorHex.equals("DEFAULT", ignoreCase = true)
    }

    val defaultSurfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val cardColor = remember(note.colorHex, isDefaultColor, defaultSurfaceColor) {
        if (isDefaultColor) {
            defaultSurfaceColor
        } else {
            try {
                Color(android.graphics.Color.parseColor(note.colorHex))
            } catch (_: Exception) {
                defaultSurfaceColor
            }
        }
    }

    val defaultTextColor = MaterialTheme.colorScheme.onSurface
    val defaultVariantTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val titleColor = remember(isDefaultColor, cardColor, defaultTextColor) {
        if (isDefaultColor) defaultTextColor
        else if (cardColor.luminance() > 0.5f) Color(0xFF202124) else Color.White
    }
    val contentTextColor = remember(isDefaultColor, cardColor, defaultVariantTextColor) {
        if (isDefaultColor) defaultVariantTextColor
        else if (cardColor.luminance() > 0.5f) Color(0xFF3C4043) else Color(0xFFE0E0E0)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(
                1.dp,
                if (isDefaultColor) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                else cardColor.copy(alpha = 0.8f)
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords -> cardOffsetInRoot = coords.boundsInRoot().topLeft }
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Title, Drag Handle, Pin Icon and Selection Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = titleColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Seçildi",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp).padding(end = 4.dp)
                        )
                    }

                    // Drag handle for dragging note to folder
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Sürükle ve Klasöre Bırak",
                        tint = contentTextColor.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(24.dp)
                            .padding(horizontal = 2.dp)
                            .pointerInput(note.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { localOffset ->
                                        onStartDrag?.invoke(note, cardOffsetInRoot + localOffset)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onDragAmount?.invoke(dragAmount)
                                    },
                                    onDragEnd = { onDragEnd?.invoke() },
                                    onDragCancel = { onDragCancel?.invoke() }
                                )
                            }
                    )

                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (note.isPinned) "Sabitlemeyi Kaldır" else "Sabitle",
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else contentTextColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
                                    tint = if (isChecked) contentTextColor.copy(alpha = 0.6f) else contentTextColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = itemText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (isChecked) contentTextColor.copy(alpha = 0.6f) else contentTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                NoteType.DRAWING -> {
                    val strokes = remember(note.drawingData) { parseDrawingStrokes(note.drawingData ?: "") }
                    if (strokes.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.95f),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .padding(vertical = 4.dp)
                        ) {
                            DrawingPreviewCanvas(strokes = strokes)
                        }
                    } else {
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
                }
                NoteType.IMAGE -> {
                    if (!note.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = note.imageUri,
                            contentDescription = note.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .padding(vertical = 4.dp)
                        )
                    }
                    if (note.content.isNotBlank()) {
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentTextColor,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                else -> {
                    if (note.content.isNotBlank()) {
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentTextColor,
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
                    color = contentTextColor.copy(alpha = 0.8f)
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

    val isDefaultColor = remember(selectedColorHex) {
        selectedColorHex.isBlank() || selectedColorHex.equals("#FFFFFF", ignoreCase = true) || selectedColorHex.equals("DEFAULT", ignoreCase = true)
    }
    val defaultSurfaceColor = MaterialTheme.colorScheme.surface
    val defaultOnSurface = MaterialTheme.colorScheme.onSurface
    val defaultOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val bgColor = remember(selectedColorHex, isDefaultColor, defaultSurfaceColor) {
        if (isDefaultColor) defaultSurfaceColor
        else {
            try {
                Color(android.graphics.Color.parseColor(selectedColorHex))
            } catch (_: Exception) {
                defaultSurfaceColor
            }
        }
    }
    val isLightBg = remember(isDefaultColor, bgColor) {
        if (isDefaultColor) null else bgColor.luminance() > 0.45f
    }
    val titleTextColor = remember(isDefaultColor, isLightBg, defaultOnSurface) {
        if (isDefaultColor) defaultOnSurface
        else if (isLightBg == true) Color(0xFF1F1F1F) else Color(0xFFFFFFFF)
    }
    val bodyTextColor = remember(isDefaultColor, isLightBg, defaultOnSurface) {
        if (isDefaultColor) defaultOnSurface
        else if (isLightBg == true) Color(0xFF202124) else Color(0xFFF1F5F9)
    }
    val placeholderColor = remember(isDefaultColor, isLightBg, defaultOnSurfaceVariant) {
        if (isDefaultColor) defaultOnSurfaceVariant.copy(alpha = 0.65f)
        else if (isLightBg == true) Color(0xFF5F6368) else Color(0xFF94A3B8)
    }
    val iconColor = remember(isDefaultColor, isLightBg, defaultOnSurface) {
        if (isDefaultColor) defaultOnSurface
        else if (isLightBg == true) Color(0xFF3C4043) else Color(0xFFF8FAFC)
    }
    val secondaryIconColor = remember(isDefaultColor, isLightBg, defaultOnSurfaceVariant) {
        if (isDefaultColor) defaultOnSurfaceVariant
        else if (isLightBg == true) Color(0xFF5F6368) else Color(0xFFCBD5E1)
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
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            color = bgColor
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { handleCloseOrSave() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri ve Kaydet",
                                tint = iconColor
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isPinned = !isPinned }) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "Sabitle",
                                    tint = if (isPinned) MaterialTheme.colorScheme.primary else secondaryIconColor
                                )
                            }
                            // Color palette button: Toggles sliding bottom palette
                            IconButton(onClick = { showColorPaletteSheet = !showColorPaletteSheet }) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Renk Seçici",
                                    tint = if (showColorPaletteSheet) MaterialTheme.colorScheme.primary else secondaryIconColor
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

                    // Scrollable Note Body - Keyboard will never hide the text
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
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
                                    color = placeholderColor
                                )
                            },
                            textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = titleTextColor),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = titleTextColor
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Content Input (expands naturally and scrolls)
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            placeholder = {
                                Text(
                                    "Not...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = placeholderColor
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = bodyTextColor),
                            modifier = Modifier
                                .fillMaxWidth(),
                            minLines = 8,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = bodyTextColor
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Word count
                        if (showWordCount) {
                            val words = remember(content) { if (content.isBlank()) 0 else content.trim().split("\\s+".toRegex()).size }
                            Text(
                                text = "$words kelime • ${content.length} karakter",
                                style = MaterialTheme.typography.labelSmall,
                                color = placeholderColor,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(140.dp))
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

    val isDefaultColor = remember(selectedColorHex) {
        selectedColorHex.isBlank() || selectedColorHex.equals("#FFFFFF", ignoreCase = true) || selectedColorHex.equals("DEFAULT", ignoreCase = true)
    }
    val defaultSurfaceColor = MaterialTheme.colorScheme.surface
    val defaultOnSurface = MaterialTheme.colorScheme.onSurface
    val defaultOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val bgColor = remember(selectedColorHex, isDefaultColor, defaultSurfaceColor) {
        if (isDefaultColor) defaultSurfaceColor
        else {
            try {
                Color(android.graphics.Color.parseColor(selectedColorHex))
            } catch (_: Exception) {
                defaultSurfaceColor
            }
        }
    }
    val isLightBg = remember(isDefaultColor, bgColor) {
        if (isDefaultColor) null else bgColor.luminance() > 0.45f
    }
    val titleTextColor = remember(isDefaultColor, isLightBg, defaultOnSurface) {
        if (isDefaultColor) defaultOnSurface
        else if (isLightBg == true) Color(0xFF1F1F1F) else Color(0xFFFFFFFF)
    }
    val bodyTextColor = remember(isDefaultColor, isLightBg, defaultOnSurface) {
        if (isDefaultColor) defaultOnSurface
        else if (isLightBg == true) Color(0xFF202124) else Color(0xFFF1F5F9)
    }
    val placeholderColor = remember(isDefaultColor, isLightBg, defaultOnSurfaceVariant) {
        if (isDefaultColor) defaultOnSurfaceVariant.copy(alpha = 0.65f)
        else if (isLightBg == true) Color(0xFF5F6368) else Color(0xFF94A3B8)
    }
    val iconColor = remember(isDefaultColor, isLightBg, defaultOnSurface) {
        if (isDefaultColor) defaultOnSurface
        else if (isLightBg == true) Color(0xFF3C4043) else Color(0xFFF8FAFC)
    }
    val secondaryIconColor = remember(isDefaultColor, isLightBg, defaultOnSurfaceVariant) {
        if (isDefaultColor) defaultOnSurfaceVariant
        else if (isLightBg == true) Color(0xFF5F6368) else Color(0xFFCBD5E1)
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
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
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
                                tint = iconColor
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isPinned = !isPinned }) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "Sabitle",
                                    tint = if (isPinned) MaterialTheme.colorScheme.primary else secondaryIconColor
                                )
                            }
                            IconButton(onClick = { showColorPaletteSheet = !showColorPaletteSheet }) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Renk Seçici",
                                    tint = if (showColorPaletteSheet) MaterialTheme.colorScheme.primary else secondaryIconColor
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
                                color = placeholderColor
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = titleTextColor),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = titleTextColor
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
                                    placeholder = { Text("Liste öğesi...", color = placeholderColor) },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = if (item.first) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (item.first) placeholderColor else bodyTextColor
                                    ),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        cursorColor = bodyTextColor
                                    )
                                )
                                IconButton(onClick = { itemsList.removeAt(index) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Öğeyi Sil",
                                        tint = secondaryIconColor,
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

                        item {
                            Spacer(modifier = Modifier.height(140.dp))
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
    initialNote: Note? = null,
    folders: List<NoteFolder>,
    selectedFolderId: Long?,
    currentUserAssigned: String,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    val initialTitle = remember(initialNote) { initialNote?.title ?: "" }
    var title by remember { mutableStateOf(initialTitle) }
    var strokeColor by remember { mutableStateOf(Color.Black) }
    var strokeColorHex by remember { mutableStateOf("#000000") }
    var strokeWidth by remember { mutableStateOf(6f) }

    val strokes = remember {
        mutableStateListOf<DrawingStroke>().apply {
            if (initialNote != null && !initialNote.drawingData.isNullOrBlank()) {
                addAll(parseDrawingStrokes(initialNote.drawingData))
            }
        }
    }
    var currentPoints by remember { mutableStateOf<List<DrawingPoint>>(emptyList()) }

    fun handleCloseOrSave() {
        if (strokes.isNotEmpty() || title.isNotBlank()) {
            val now = System.currentTimeMillis()
            val serialized = serializeDrawingStrokes(strokes)
            val note = initialNote?.copy(
                title = title.trim().ifBlank { "Çizim Notu" },
                drawingData = serialized,
                content = if (strokes.isNotEmpty()) "Çizim (${strokes.size} çizgi)" else "Boş çizim",
                updatedAtMillis = now
            ) ?: Note(
                title = title.trim().ifBlank { "Çizim Notu" },
                content = if (strokes.isNotEmpty()) "Çizim (${strokes.size} çizgi)" else "Boş çizim",
                colorHex = "#FFFFFF",
                folderId = selectedFolderId,
                assignedTo = currentUserAssigned,
                type = NoteType.DRAWING,
                drawingData = serialized,
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
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { handleCloseOrSave() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri ve Kaydet")
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Çizim Başlığı...") },
                        modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                        },
                        enabled = strokes.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Geri Al")
                    }
                    IconButton(
                        onClick = { strokes.clear() },
                        enabled = strokes.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Temizle")
                    }
                    IconButton(onClick = { handleCloseOrSave() }) {
                        Icon(Icons.Default.Check, contentDescription = "Kaydet", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Drawing Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .background(Color(0xFFFAFAFA))
                        .pointerInput(strokeColorHex, strokeWidth) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPoints = listOf(DrawingPoint(offset.x, offset.y))
                                },
                                onDrag = { change, _ ->
                                    currentPoints = currentPoints + DrawingPoint(change.position.x, change.position.y)
                                },
                                onDragEnd = {
                                    if (currentPoints.isNotEmpty()) {
                                        strokes.add(DrawingStroke(strokeColorHex, strokeWidth, currentPoints))
                                        currentPoints = emptyList()
                                    }
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw completed strokes
                        strokes.forEach { stroke ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(stroke.colorHex))
                            } catch (_: Exception) {
                                Color.Black
                            }
                            if (stroke.points.size == 1) {
                                val pt = stroke.points[0]
                                drawCircle(
                                    color = color,
                                    radius = stroke.width / 2f,
                                    center = androidx.compose.ui.geometry.Offset(pt.x, pt.y)
                                )
                            } else if (stroke.points.size > 1) {
                                val path = Path().apply {
                                    moveTo(stroke.points[0].x, stroke.points[0].y)
                                    for (i in 1 until stroke.points.size) {
                                        lineTo(stroke.points[i].x, stroke.points[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = color,
                                    style = Stroke(width = stroke.width, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }

                        // Draw active stroke live
                        if (currentPoints.size == 1) {
                            val pt = currentPoints[0]
                            drawCircle(
                                color = strokeColor,
                                radius = strokeWidth / 2f,
                                center = androidx.compose.ui.geometry.Offset(pt.x, pt.y)
                            )
                        } else if (currentPoints.size > 1) {
                            val path = Path().apply {
                                moveTo(currentPoints[0].x, currentPoints[0].y)
                                for (i in 1 until currentPoints.size) {
                                    lineTo(currentPoints[i].x, currentPoints[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = strokeColor,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Brush thickness selector & Color Palette
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thickness options
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(3f to "İnce", 7f to "Orta", 14f to "Kalın").forEach { (width, label) ->
                            FilterChip(
                                selected = strokeWidth == width,
                                onClick = { strokeWidth = width },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Pen Colors
                    val penColors = listOf(
                        "#000000" to Color.Black,
                        "#1E88E5" to Color(0xFF1E88E5),
                        "#E53935" to Color(0xFFE53935),
                        "#43A047" to Color(0xFF43A047),
                        "#8E24AA" to Color(0xFF8E24AA),
                        "#FB8C00" to Color(0xFFFB8C00)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        penColors.forEach { (hex, color) ->
                            val isSelected = strokeColorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        strokeColor = color
                                        strokeColorHex = hex
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= 4. IMAGE NOTE EDIT DIALOG =================

@Composable
fun KeepImageNoteEditDialog(
    initialNote: Note? = null,
    initialImageUri: Uri? = null,
    folders: List<NoteFolder>,
    selectedFolderId: Long?,
    currentUserAssigned: String,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    val context = LocalContext.current
    val initialTitle = remember(initialNote) { initialNote?.title ?: "" }
    val initialContent = remember(initialNote) { initialNote?.content ?: "" }
    val initialUri = remember(initialNote, initialImageUri) {
        initialNote?.imageUri ?: initialImageUri?.toString() ?: ""
    }
    val initialFolder = remember(initialNote) { initialNote?.folderId ?: selectedFolderId }

    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    var currentImageUri by remember { mutableStateOf(initialUri) }
    var selectedFolder by remember { mutableStateOf(initialFolder) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedUri = saveImageToInternalStorage(context, uri)
            currentImageUri = savedUri.toString()
        }
    }

    fun handleCloseOrSave() {
        if (currentImageUri.isNotBlank() || title.isNotBlank() || content.isNotBlank()) {
            val now = System.currentTimeMillis()
            val note = initialNote?.copy(
                title = title.trim().ifBlank { "Görsel Notu" },
                content = content.trim(),
                imageUri = currentImageUri,
                folderId = selectedFolder,
                updatedAtMillis = now
            ) ?: Note(
                title = title.trim().ifBlank { "Görsel Notu" },
                content = content.trim(),
                imageUri = currentImageUri,
                colorHex = "#FFFFFF",
                folderId = selectedFolder,
                assignedTo = currentUserAssigned,
                type = NoteType.IMAGE,
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
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { handleCloseOrSave() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri ve Kaydet")
                    }
                    Text(
                        "Görsel Notu 📷",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { handleCloseOrSave() }) {
                        Icon(Icons.Default.Check, contentDescription = "Kaydet", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Image Display & Change Button
                if (currentImageUri.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp, max = 280.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = currentImageUri,
                            contentDescription = "Not Fotoğrafı",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Değiştir",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Fotoğraf Seçmek İçin Dokunun",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Folder selector chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
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

                Spacer(modifier = Modifier.height(10.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık") },
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Content Input
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Açıklama / Not") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 120.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(140.dp))
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
            val denemeFolderId = folders.find { it.name.contains("Deneme", ignoreCase = true) }?.id
            val targetFolderId = selectedFolderId ?: denemeFolderId ?: folders.firstOrNull()?.id
            val note = existingNote?.copy(
                title = examTitle.trim(),
                content = summaryText.trim(),
                folderId = existingNote.folderId ?: targetFolderId,
                updatedAtMillis = now
            ) ?: Note(
                title = examTitle.trim(),
                content = summaryText.trim(),
                colorHex = "#1E293B",
                folderId = targetFolderId,
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
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            color = MaterialTheme.colorScheme.background
        ) {
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

                Spacer(modifier = Modifier.height(140.dp))
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
