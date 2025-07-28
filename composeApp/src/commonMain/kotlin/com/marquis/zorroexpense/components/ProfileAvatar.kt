package com.marquis.zorroexpense.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import zorroexpense.composeapp.generated.resources.Res
import zorroexpense.composeapp.generated.resources.alex
import zorroexpense.composeapp.generated.resources.sarah


@Composable
fun ProfileAvatar(
    name: String,
    size: Dp = 72.dp,
    userProfile: String,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    val fallbackText = if (name.isNotEmpty()) name.first().toString() else "?"
    val imageResource : DrawableResource? = when (userProfile) {
        "sarah" -> Res.drawable.sarah
        "alex" -> Res.drawable.alex
        else -> null
    }

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (imageResource != null) {
                Image(
                    painter = painterResource(imageResource),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = fallbackText.take(2).uppercase(),
                    style = when {
                        size <= 40.dp -> MaterialTheme.typography.titleMedium
                        size <= 56.dp -> MaterialTheme.typography.headlineSmall
                        else -> MaterialTheme.typography.headlineLarge
                    },
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}