package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.firebase.SyncState
import com.example.data.model.UserAccount

/**
 * Minimalist, authentic Netflix-style profile selection screen.
 * Displays only the profile logo/avatar and name, with active selection highlight.
 */
@Composable
fun LoginScreen(
    onSelectProfile: (UserAccount) -> Unit,
    modifier: Modifier = Modifier,
    isOnline: Boolean = true,
    syncState: SyncState = SyncState.IDLE
) {
    val profiles = UserAccount.PROFILES
    var selectedProfileId by remember { mutableStateOf("tuna") }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("netflix_profile_selection_screen"),
        color = Color(0xFF141414) // Netflix signature deep black background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Minimal Netflix-style Title
            Text(
                text = "Kimin Masası?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Profile Logos Row (Minimalist Netflix Avatars with Flower icons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profiles_row"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                profiles.forEachIndexed { index, profile ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.width(28.dp))
                    }
                    val isSelected = profile.id == selectedProfileId
                    NetflixProfileLogoItem(
                        profile = profile,
                        isSelected = isSelected,
                        onClick = {
                            selectedProfileId = profile.id
                            onSelectProfile(profile)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NetflixProfileLogoItem(
    profile: UserAccount,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (isSelected) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "profileScale"
    )

    val isTuna = profile.isTuna
    val avatarDrawable = if (isTuna) R.drawable.profile_tuna else R.drawable.profile_elif
    val accentColor = if (isTuna) Color(0xFF3B82F6) else Color(0xFFA855F7)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("profile_avatar_${profile.id}")
    ) {
        // Exact profile picture (Blue for Tuna, Purple for Elif)
        val context = LocalContext.current
        Surface(
            modifier = Modifier
                .size(114.dp)
                .shadow(
                    elevation = if (isSelected) 14.dp else 4.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = accentColor
                ),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(
                width = if (isSelected) 3.5.dp else 2.dp,
                color = if (isSelected) Color.White else accentColor.copy(alpha = 0.6f)
            )
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(profile.photoUrl)
                    .crossfade(true)
                    .error(avatarDrawable)
                    .placeholder(avatarDrawable)
                    .fallback(avatarDrawable)
                    .build(),
                contentDescription = profile.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Profile Name Only
        Text(
            text = profile.name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 17.sp
            ),
            color = if (isSelected) Color.White else Color(0xFFE2E8F0),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Öğrenci",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
    }
}

// Backward compatibility overload for existing call-sites if any
@Composable
fun LoginScreen(
    onLoginAsStudent: (name: String, studentNo: String) -> Unit,
    onLoginAsTeacher: (name: String, passCode: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    LoginScreen(
        onSelectProfile = { profile ->
            if (profile.isTeacher) {
                onLoginAsTeacher(profile.name, "1234")
            } else {
                onLoginAsStudent(profile.name, "")
            }
        },
        modifier = modifier
    )
}
