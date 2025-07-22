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

/**
 * Reusable ProfileAvatar component that can display an image or fallback to initials
 * 
 * @param size Size of the circular avatar (default: 72.dp)
 * @param imageResource Optional drawable resource for the avatar image
 * @param fallbackText Text to display when no image is provided (usually initials)
 * @param backgroundColor Background color of the avatar
 * @param contentColor Color of the fallback text
 * @param modifier Optional modifier for styling
 */
@Composable
fun ProfileAvatar(
    size: Dp = 72.dp,
    imageResource: DrawableResource? = null,
    fallbackText: String = "?",
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
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

/**
 * Convenience function to create ProfileAvatar from an expense name initial
 */
@Composable
fun ExpenseProfileAvatar(
    expenseName: String,
    size: Dp = 72.dp,
    imageResource: DrawableResource? = null,
    modifier: Modifier = Modifier
) {
    val initial = if (expenseName.isNotEmpty()) expenseName.first().toString() else "?"
    
    ProfileAvatar(
        size = size,
        imageResource = imageResource,
        fallbackText = initial,
        modifier = modifier
    )
}