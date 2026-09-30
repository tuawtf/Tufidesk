package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.UserAccount

/**
 * Tuna için profil resmi: https://i.imgur.com/M6BWWpu.jpeg
 */
@Composable
fun BlueLilyFlower(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data("https://i.imgur.com/M6BWWpu.jpeg")
            .crossfade(true)
            .error(R.drawable.profile_tuna)
            .placeholder(R.drawable.profile_tuna)
            .fallback(R.drawable.profile_tuna)
            .build(),
        contentDescription = "Tuna",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

/**
 * Elif için profil resmi: https://i.imgur.com/6ThMMmK.jpeg
 */
@Composable
fun PurpleFlower(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data("https://i.imgur.com/6ThMMmK.jpeg")
            .crossfade(true)
            .error(R.drawable.profile_elif)
            .placeholder(R.drawable.profile_elif)
            .fallback(R.drawable.profile_elif)
            .build(),
        contentDescription = "Elif",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

/**
 * Kullanıcı profil resmi avatarı.
 * Tuna için https://i.imgur.com/M6BWWpu.jpeg, Elif için https://i.imgur.com/6ThMMmK.jpeg
 * yükler.
 */
@Composable
fun UserFlowerAvatar(
    user: UserAccount?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    shape: Shape = CircleShape,
    showBorder: Boolean = true,
    isSelected: Boolean = false
) {
    val isTuna = user?.isTuna ?: true
    val drawableFallback = if (isTuna) R.drawable.profile_tuna else R.drawable.profile_elif
    val imageUrl = user?.photoUrl ?: if (isTuna) "https://i.imgur.com/M6BWWpu.jpeg" else "https://i.imgur.com/6ThMMmK.jpeg"

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else if (isTuna) {
        Color(0xFF2563EB)
    } else {
        Color(0xFF7E22CE)
    }

    val context = LocalContext.current

    Surface(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isSelected) 6.dp else 2.dp,
                shape = shape,
                spotColor = borderColor.copy(alpha = 0.35f)
            ),
        shape = shape,
        color = Color(0xFF1E293B),
        border = if (showBorder) {
            BorderStroke(
                width = if (isSelected) 2.5.dp else 1.5.dp,
                color = borderColor
            )
        } else null
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .error(drawableFallback)
                .placeholder(drawableFallback)
                .fallback(drawableFallback)
                .build(),
            contentDescription = user?.name ?: "Profil Resmi",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
