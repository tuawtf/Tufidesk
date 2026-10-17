package com.example.ui.screens.cloud

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.cloud.R2Client
import com.example.data.cloud.R2Result
import com.example.data.cloud.R2Settings
import com.example.ui.HomeworkViewModel
import kotlinx.coroutines.launch

@Composable
fun R2StorageSettingsCard(
    viewModel: HomeworkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var workerUrlInput by remember { mutableStateOf(R2Settings.getCustomWorkerUrl(context)) }
    var workerTokenInput by remember { mutableStateOf(R2Settings.getCustomWorkerToken(context)) }
    var isTokenVisible by remember { mutableStateOf(false) }

    val isUrlFromBuild = R2Settings.isWorkerUrlFromBuildConfig(context)
    val isTokenFromBuild = R2Settings.isWorkerTokenFromBuildConfig(context)

    var isUploadNotesEnabled by remember {
        mutableStateOf(R2Settings.isImageNotesUploadEnabled(context))
    }
    var isUploadLargeBlobsEnabled by remember {
        mutableStateOf(R2Settings.isLargeBlobUploadEnabled(context))
    }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultPair by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val isUploadingExisting by viewModel.isUploadingExistingNotes.collectAsState()
    val uploadProgress by viewModel.existingNotesUploadProgress.collectAsState()
    var batchUploadStatusMessage by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "R2 Depolama",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bulut Depolama (Cloudflare R2)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Not görsellerini ve büyük verileri R2'de saklayın",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bilgi Rozeti
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Güvenli Depolama",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Yapılandırma derleme zamanından (BuildConfig) gelebilir veya ayarlar üzerinden özelleştirilebilir.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Worker URL Alanı
            OutlinedTextField(
                value = workerUrlInput,
                onValueChange = {
                    workerUrlInput = it
                    testResultPair = null
                },
                label = { Text("Cloudflare Worker URL") },
                placeholder = {
                    Text(if (isUrlFromBuild) "Derlemeden yapılandırıldı" else "https://not-depolama.kullanici.workers.dev")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (workerUrlInput.isNotBlank()) {
                        IconButton(onClick = {
                            workerUrlInput = ""
                            R2Settings.setWorkerUrl(context, "")
                            testResultPair = null
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sıfırla")
                        }
                    }
                },
                supportingText = {
                    Text(
                        when {
                            workerUrlInput.isNotBlank() -> "Kullanıcı tarafından özel URL girildi"
                            isUrlFromBuild -> "Derlemeden yapılandırıldı (Varsayılan)"
                            else -> "Worker adresiniz (/files/{key} uç noktasına istek gönderilir)"
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Worker Token Alanı
            OutlinedTextField(
                value = workerTokenInput,
                onValueChange = {
                    workerTokenInput = it
                    testResultPair = null
                },
                label = { Text("Worker Authorization Token") },
                placeholder = {
                    Text(if (isTokenFromBuild) "Derlemeden yapılandırıldı" else "Bearer belirteciniz")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                        Icon(
                            imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isTokenVisible) "Gizle" else "Göster"
                        )
                    }
                },
                supportingText = {
                    Text(
                        when {
                            workerTokenInput.isNotBlank() -> "Kullanıcı tarafından özel Token girildi"
                            isTokenFromBuild -> "Derlemeden yapılandırıldı (Gizli Token aktif)"
                            else -> "Cloudflare Worker'ınızda ayarladığınız gizli doğrulama anahtarı"
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Kaydet & Bağlantıyı Test Et Butonları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        R2Settings.setWorkerUrl(context, workerUrlInput)
                        R2Settings.setWorkerToken(context, workerTokenInput)
                        testResultPair = null
                        Toast.makeText(context, "R2 ayarları kaydedildi. ✅", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kaydet")
                }

                OutlinedButton(
                    onClick = {
                        R2Settings.setWorkerUrl(context, workerUrlInput)
                        R2Settings.setWorkerToken(context, workerTokenInput)

                        if (!R2Settings.isR2Configured(context)) {
                            testResultPair = Pair(false, "Lütfen geçerli bir Worker URL ve Token girin (derleme zamanı veya ayarlar üzerinden).")
                            return@OutlinedButton
                        }

                        isTestingConnection = true
                        testResultPair = null

                        coroutineScope.launch {
                            val client = R2Client(context)
                            val result = client.testConnection()
                            isTestingConnection = false
                            when (result) {
                                is R2Result.Success -> {
                                    testResultPair = Pair(true, result.data)
                                    Toast.makeText(context, "R2 bağlantısı doğrulandı! ✅", Toast.LENGTH_SHORT).show()
                                }
                                is R2Result.Error -> {
                                    testResultPair = Pair(false, result.message)
                                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    enabled = !isTestingConnection,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isTestingConnection) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test ediliyor...")
                    } else {
                        Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bağlantıyı Test Et")
                    }
                }
            }

            // Test Sonucu Bildirim Kutusu
            AnimatedVisibility(visible = testResultPair != null) {
                testResultPair?.let { (success, msg) ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (success) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (success) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (success) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. "Resim notlarını R2'ye yükle" Switch'i
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Resim notlarını R2'ye yükle",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Yeni ve düzenlenen resim notları otomatik sıkıştırılıp (maks 1920px, JPEG 85) R2'ye yüklenir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = isUploadNotesEnabled,
                        onCheckedChange = { checked ->
                            isUploadNotesEnabled = checked
                            R2Settings.setImageNotesUploadEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. "Büyük not verilerini R2'ye yedekle" Switch'i
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Büyük not verilerini R2'ye yedekle",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Çizim veya Deneme Analizi verisi 100.000 karakteri aştığında R2'ye (notes/blob-*.json) aktarılır (1 MB Firestore sınırını önler).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = isUploadLargeBlobsEnabled,
                        onCheckedChange = { checked ->
                            isUploadLargeBlobsEnabled = checked
                            R2Settings.setLargeBlobUploadEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. "Mevcut resim notlarını yükle" Butonu ve İlerleme Göstergesi
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mevcut Resim Notlarını R2'ye Aktar",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Cihazda yerel URI olarak kalmış eski resim notlarını R2 bulut depolamaya aktarır.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isUploadingExisting) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            uploadProgress?.let { (curr, total) ->
                                Text(
                                    text = "Yükleniyor: $curr / $total not aktarılıyor...",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val progress = if (total > 0) curr.toFloat() / total.toFloat() else 0f
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } ?: run {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (!R2Settings.isR2Configured(context)) {
                                    Toast.makeText(
                                        context,
                                        "Lütfen önce geçerli bir Worker URL ve Token kaydedin.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }
                                batchUploadStatusMessage = null
                                viewModel.uploadExistingLocalImageNotes { _, _, msg ->
                                    batchUploadStatusMessage = msg
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mevcut Resim Notlarını Yükle")
                        }
                    }

                    AnimatedVisibility(visible = batchUploadStatusMessage != null) {
                        batchUploadStatusMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
