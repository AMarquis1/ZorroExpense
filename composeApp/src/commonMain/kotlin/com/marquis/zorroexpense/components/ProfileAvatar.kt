package com.marquis.zorroexpense.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ProfileAvatar(
    name: String,
    size: Dp = 72.dp,
    userProfile: String,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
) {
    val fallbackText = if (name.isNotEmpty()) name.first().toString() else "?"
    val isLoadingImage = remember { mutableStateOf(false) }
    val imageError = remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = backgroundColor,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                userProfile.isNotEmpty() && (userProfile.startsWith("http://") || userProfile.startsWith("https://") || userProfile.startsWith("gs://")) -> {
                    // Load remote image from Firebase Storage URL or other HTTP(S) URL
                    AsyncImage(
                        model = userProfile,
                        contentDescription = "Profile picture",
                        modifier =
                            Modifier
                                .size(size)
                                .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        onLoading = {
                            isLoadingImage.value = true
                            imageError.value = null
                        },
                        onSuccess = {
                            isLoadingImage.value = false
                            imageError.value = null
                        },
                        onError = { painter ->
                            isLoadingImage.value = false
                            imageError.value = "Load failed"
                        },
                    )

                    // Show loading indicator while fetching
                    if (isLoadingImage.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(size * 0.4f),
                            color = contentColor,
                            strokeWidth = 2.dp,
                        )
                    }

                    // Show fallback text if image failed to load
                    if (imageError.value != null) {
                        Text(
                            text = fallbackText.take(2).uppercase(),
                            style =
                                when {
                                    size <= 40.dp -> MaterialTheme.typography.titleMedium
                                    size <= 56.dp -> MaterialTheme.typography.headlineSmall
                                    else -> MaterialTheme.typography.headlineLarge
                                },
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> {
                    // Show initials as fallback when no image provided
                    Text(
                        text = fallbackText.take(2).uppercase(),
                        style =
                            when {
                                size <= 40.dp -> MaterialTheme.typography.titleMedium
                                size <= 56.dp -> MaterialTheme.typography.headlineSmall
                                else -> MaterialTheme.typography.headlineLarge
                            },
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
