package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.data.model.Homework
import com.example.data.model.HomeworkVerification
import com.example.data.verification.HomeworkVerificationService
import com.example.data.verification.HomeworkVerificationSettings
import com.example.data.verification.PageRangeExtractor
import com.example.data.verification.VerificationDecision
import kotlinx.coroutines.launch
import java.io.File

enum class PageVerifyState {
    IDLE,
    PROCESSING,
    APPROVED,
    REJECTED
}

data class PageRowData(
    val page: Int,
    val state: PageVerifyState = PageVerifyState.IDLE,
    val message: String? = null,
    val hash: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoVerificationDialog(
    homework: Homework,
    existingHashes: List<Long>,
    onCompleteWithVerification: (verifications: List<HomeworkVerification>) -> Unit,
    onForceCompleteWithoutVerification: (pages: List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    if (isTablet) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .heightIn(max = 680.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                PhotoVerificationContent(
                    homework = homework,
                    existingHashes = existingHashes,
                    onCompleteWithVerification = onCompleteWithVerification,
                    onForceCompleteWithoutVerification = onForceCompleteWithoutVerification,
                    onDismiss = onDismiss
                )
            }
        }
    } else {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            PhotoVerificationContent(
                homework = homework,
                existingHashes = existingHashes,
                onCompleteWithVerification = onCompleteWithVerification,
                onForceCompleteWithoutVerification = onForceCompleteWithoutVerification,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
fun PhotoVerificationContent(
    homework: Homework,
    existingHashes: List<Long>,
    onCompleteWithVerification: (verifications: List<HomeworkVerification>) -> Unit,
    onForceCompleteWithoutVerification: (pages: List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allowGallery = remember { HomeworkVerificationSettings.isGalleryAllowed(context) }
    val allowForce = remember { HomeworkVerificationSettings.isForceCompleteAllowed(context) }

    // Sayfaları ayıkla
    val initialPages = remember(homework.title, homework.description) {
        PageRangeExtractor.extractPages(homework.title, homework.description)
    }

    var expectedPages by remember { mutableStateOf(initialPages) }
    var manualPagesInput by remember { mutableStateOf("") }
    var manualInputError by remember { mutableStateOf<String?>(null) }

    // Sayfa durum haritası
    val pageStates = remember { mutableStateMapOf<Int, PageRowData>() }

    LaunchedEffect(expectedPages) {
        for (page in expectedPages) {
            if (!pageStates.containsKey(page)) {
                pageStates[page] = PageRowData(page = page)
            }
        }
    }

    // Kamera ve galeri fırlatıcıları
    var activePageForCapture by remember { mutableStateOf<Int?>(null) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val page = activePageForCapture
        val file = currentPhotoFile
        if (success && page != null && file != null && file.exists()) {
            scope.launch {
                pageStates[page] = PageRowData(page = page, state = PageVerifyState.PROCESSING, message = "İnceleniyor...")
                val bitmap = loadBitmapFromFile(file)
                if (bitmap != null) {
                    val decision = HomeworkVerificationService.verifyPagePhoto(
                        context = context,
                        photoBitmap = bitmap,
                        expectedPage = page,
                        existingHashes = existingHashes + pageStates.values.map { it.hash }.filter { it != 0L }
                    )
                    when (decision) {
                        is VerificationDecision.Success -> {
                            pageStates[page] = PageRowData(page = page, state = PageVerifyState.APPROVED, message = "Onaylandı", hash = decision.hash)
                        }
                        is VerificationDecision.Rejected -> {
                            pageStates[page] = PageRowData(page = page, state = PageVerifyState.REJECTED, message = decision.reason, hash = decision.hash)
                        }
                        is VerificationDecision.Error -> {
                            pageStates[page] = PageRowData(page = page, state = PageVerifyState.REJECTED, message = decision.errorMessage)
                        }
                    }
                } else {
                    pageStates[page] = PageRowData(page = page, state = PageVerifyState.REJECTED, message = "Fotoğraf okunamadı")
                }
                // Gizlilik: Fotoğrafı saklamıyoruz, geçici dosyayı temizle
                try { file.delete() } catch (_: Exception) {}
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val page = activePageForCapture
        if (uri != null && page != null) {
            scope.launch {
                pageStates[page] = PageRowData(page = page, state = PageVerifyState.PROCESSING, message = "İnceleniyor...")
                val bitmap = loadBitmapFromUri(context, uri)
                if (bitmap != null) {
                    val decision = HomeworkVerificationService.verifyPagePhoto(
                        context = context,
                        photoBitmap = bitmap,
                        expectedPage = page,
                        existingHashes = existingHashes + pageStates.values.map { it.hash }.filter { it != 0L }
                    )
                    when (decision) {
                        is VerificationDecision.Success -> {
                            pageStates[page] = PageRowData(page = page, state = PageVerifyState.APPROVED, message = "Onaylandı", hash = decision.hash)
                        }
                        is VerificationDecision.Rejected -> {
                            pageStates[page] = PageRowData(page = page, state = PageVerifyState.REJECTED, message = decision.reason, hash = decision.hash)
                        }
                        is VerificationDecision.Error -> {
                            pageStates[page] = PageRowData(page = page, state = PageVerifyState.REJECTED, message = decision.errorMessage)
                        }
                    }
                } else {
                    pageStates[page] = PageRowData(page = page, state = PageVerifyState.REJECTED, message = "Fotoğraf yüklenemedi")
                }
            }
        }
    }

    fun startCameraCapture(page: Int) {
        activePageForCapture = page
        try {
            val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
            val file = File.createTempFile("verify_${homework.id}_${page}_", ".jpg", cameraDir)
            currentPhotoFile = file
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            currentPhotoUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            pageStates[page] = PageRowData(page = page, state = PageVerifyState.REJECTED, message = "Kamera başlatılamadı: ${e.localizedMessage}")
        }
    }

    fun startGalleryPick(page: Int) {
        activePageForCapture = page
        galleryLauncher.launch("image/*")
    }

    val allApproved = expectedPages.isNotEmpty() && expectedPages.all { page ->
        pageStates[page]?.state == PageVerifyState.APPROVED
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Üst Başlık Satırı
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Fotoğrafla Onayla",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = homework.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sayfa aralığı bulunamadıysa manuel giriş alanı
        if (expectedPages.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hangi sayfalar kontrol edilecek?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ödev başlığında sayfa bulunamadı. Lütfen kontrol edilecek sayfaları girin (örn. 26-29 veya 26, 27).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = manualPagesInput,
                        onValueChange = {
                            manualPagesInput = it
                            manualInputError = null
                        },
                        label = { Text("Sayfa Aralığı") },
                        placeholder = { Text("Örn: 26-29") },
                        isError = manualInputError != null,
                        supportingText = manualInputError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val parsed = PageRangeExtractor.parsePages(manualPagesInput)
                            if (parsed.isNotEmpty()) {
                                expectedPages = parsed
                            } else {
                                manualInputError = "Geçerli bir sayfa numarası veya aralık girin."
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Sayfaları Listele")
                    }
                }
            }
        } else {
            // Bilgi bannerı
            Text(
                text = "Yapay zeka sadece sayfa numarası ve ödev varlığını doğrular; soruların cevaplarını değerlendirmez.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Sayfalar Listesi
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(expectedPages, key = { it }) { page ->
                    val rowData = pageStates[page] ?: PageRowData(page = page)
                    PageVerificationRow(
                        rowData = rowData,
                        allowGallery = allowGallery,
                        onTakePhoto = { startCameraCapture(page) },
                        onPickGallery = { startGalleryPick(page) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Aksiyon Butonları
            Column(modifier = Modifier.fillMaxWidth()) {
                if (allApproved) {
                    Button(
                        onClick = {
                            val verifications = expectedPages.map { page ->
                                val row = pageStates[page]
                                HomeworkVerification(
                                    homeworkId = homework.id,
                                    page = page,
                                    hash = row?.hash ?: 0L,
                                    status = "APPROVED"
                                )
                            }
                            onCompleteWithVerification(verifications)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tüm Sayfalar Onaylandı • Ödevi Tamamla")
                    }
                } else if (allowForce) {
                    TextButton(
                        onClick = {
                            onForceCompleteWithoutVerification(expectedPages)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Yine de tamamla (doğrulanamadı)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PageVerificationRow(
    rowData: PageRowData,
    allowGallery: Boolean,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit
) {
    val borderColor = when (rowData.state) {
        PageVerifyState.APPROVED -> MaterialTheme.colorScheme.primary
        PageVerifyState.REJECTED -> MaterialTheme.colorScheme.error
        PageVerifyState.PROCESSING -> MaterialTheme.colorScheme.secondary
        PageVerifyState.IDLE -> MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sayfa Etiketi
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${rowData.page}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${rowData.page}. Sayfa",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Butonlar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (rowData.state == PageVerifyState.PROCESSING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Button(
                            onClick = onTakePhoto,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (rowData.state == PageVerifyState.APPROVED) "Yeniden Çek" else "Fotoğraf Çek",
                                fontSize = 13.sp
                            )
                        }

                        if (allowGallery) {
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = onPickGallery,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Galeriden Seç",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Durum Göstergesi (Çip / Mesaj)
            if (rowData.state != PageVerifyState.IDLE) {
                Spacer(modifier = Modifier.height(10.dp))
                StatusChip(rowData = rowData)
            }
        }
    }
}

@Composable
fun StatusChip(rowData: PageRowData) {
    val (bgColor, textColor, icon) = when (rowData.state) {
        PageVerifyState.APPROVED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.CheckCircle
        )
        PageVerifyState.REJECTED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            determineErrorIcon(rowData.message)
        )
        PageVerifyState.PROCESSING -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            Icons.Default.PhotoCamera
        )
        PageVerifyState.IDLE -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.PhotoCamera
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = rowData.message ?: "Durum bilinmiyor",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = textColor
            )
        }
    }
}

fun determineErrorIcon(message: String?): ImageVector {
    val msg = message ?: ""
    return when {
        msg.contains("bulanık") -> Icons.Default.BlurOn
        msg.contains("yazı") -> Icons.Default.EditOff
        msg.contains("kullanıldı") -> Icons.Default.ContentCopy
        msg.contains("Yanlış sayfa") -> Icons.Default.Warning
        else -> Icons.Default.ErrorOutline
    }
}

private fun loadBitmapFromFile(file: File): Bitmap? {
    return try {
        BitmapFactory.decodeFile(file.absolutePath)
    } catch (_: Exception) {
        null
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (_: Exception) {
        null
    }
}
