package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.TufiAIService
import kotlinx.coroutines.launch

/**
 * Tufi AI & API Anahtarı Yapılandırma Penceresi:
 * - Kullanıcının DeepSeek veya Google Gemini API anahtarını doğrudan girmesini,
 *   test etmesini ve kalıcı olarak kaydetmesini sağlar.
 * - GitHub üzerinden indirilen projelerde API anahtarı repo içinde olmadığında
 *   kullanıcının uygulamayı canlı olarak kullanabilmesi için esnek ve güvenli çözüm sunar.
 */
@Composable
fun TufiAISettingsDialog(
    onDismiss: () -> Unit,
    onKeyUpdated: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val initialKey = remember { TufiAIService.getApiKey(context) }
    var inputKey by remember { mutableStateOf(initialKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val isConfigured = inputKey.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Tufi AI & API Ayarları",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "DeepSeek & Gemini Canlı Bağlantı",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mevcut Durum Rozeti
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isConfigured) {
                        Color(0xFF10B981).copy(alpha = 0.12f)
                    } else {
                        Color(0xFFF59E0B).copy(alpha = 0.14f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (isConfigured) Color(0xFF10B981).copy(alpha = 0.40f) else Color(0xFFF59E0B).copy(alpha = 0.40f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isConfigured) Color(0xFF059669) else Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isConfigured) {
                                val masked = if (inputKey.length > 8) "${inputKey.take(6)}...${inputKey.takeLast(4)}" else inputKey
                                "Bağlantı Hazır: $masked"
                            } else {
                                "API Anahtarı Yapılandırılmadı (Çevrimdışı Mod)"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = if (isConfigured) Color(0xFF065F46) else Color(0xFF92400E)
                        )
                    }
                }

                Text(
                    text = "GitHub'dan indirilen projelerde güvenlik gereği API anahtarları repo içerisinde yer almaz. Buraya kendi DeepSeek (sk-...) veya Google Gemini (AIzaSy...) anahtarınızı girerek canlı modeli aktif edebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                // API Anahtarı Giriş Alanı
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = {
                        inputKey = it
                        testResult = null
                    },
                    label = { Text("API Anahtarı") },
                    placeholder = { Text("sk-... veya AIzaSy...") },
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isKeyVisible) "Gizle" else "Göster",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Test Sonucu Bildirimi
                if (testResult != null) {
                    val (success, message) = testResult!!
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (success) Color(0xFF10B981).copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        border = BorderStroke(
                            0.8.dp,
                            if (success) Color(0xFF10B981).copy(alpha = 0.50f) else MaterialTheme.colorScheme.error.copy(alpha = 0.40f)
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
                                tint = if (success) Color(0xFF059669) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (success) Color(0xFF065F46) else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Bağlantıyı Test Et Butonu
                OutlinedButton(
                    onClick = {
                        if (inputKey.isNotBlank()) {
                            isTesting = true
                            testResult = null
                            coroutineScope.launch {
                                val res = TufiAIService.testConnection(context, inputKey)
                                testResult = res
                                isTesting = false
                            }
                        } else {
                            Toast.makeText(context, "Lütfen önce bir API anahtarı girin.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isTesting && inputKey.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bağlantı Test Ediliyor...")
                    } else {
                        Text("⚡ Bağlantıyı Test Et")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    TufiAIService.saveCustomApiKey(context, inputKey.trim())
                    Toast.makeText(context, "API anahtarı başarıyla kaydedildi! 🚀", Toast.LENGTH_SHORT).show()
                    onKeyUpdated()
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            Row {
                if (initialKey.isNotBlank()) {
                    TextButton(
                        onClick = {
                            TufiAIService.saveCustomApiKey(context, "")
                            inputKey = ""
                            testResult = null
                            Toast.makeText(context, "API anahtarı sıfırlandı.", Toast.LENGTH_SHORT).show()
                            onKeyUpdated()
                        }
                    ) {
                        Text("Sıfırla", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Kapat")
                }
            }
        }
    )
}
