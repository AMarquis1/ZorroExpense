package com.marquis.zorroexpense.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.marquis.zorroexpense.components.ProfileAvatar
import com.marquis.zorroexpense.presentation.state.EditProfileUiEvent
import com.marquis.zorroexpense.presentation.state.EditProfileUiState
import com.marquis.zorroexpense.presentation.viewmodel.EditProfileViewModel
import io.github.ismoy.imagepickerkmp.domain.config.CameraCaptureConfig
import io.github.ismoy.imagepickerkmp.domain.config.CropConfig
import io.github.ismoy.imagepickerkmp.domain.models.CompressionLevel
import io.github.ismoy.imagepickerkmp.domain.models.MimeType
import io.github.ismoy.imagepickerkmp.presentation.ui.components.GalleryPickerLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var showGalleryPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (!showGalleryPicker) {
                TopAppBar(
                    title = { Text("Edit Profile") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is EditProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is EditProfileUiState.Success -> {
                EditProfileContent(
                    state = state,
                    viewModel = viewModel,
                    showGalleryPicker = showGalleryPicker,
                    onShowGalleryPickerChanged = { show -> showGalleryPicker = show },
                    modifier = Modifier.padding(paddingValues),
                )
            }

            is EditProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun EditProfileContent(
    state: EditProfileUiState.Success,
    viewModel: EditProfileViewModel,
    showGalleryPicker: Boolean,
    onShowGalleryPickerChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Profile Avatar with Edit Icon
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            ProfileAvatar(
                name = state.name,
                userProfile = state.profileImageUrl,
                size = 120.dp,
            )

            // Edit Icon (clickable to open image picker)
            IconButton(
                onClick = {
                    if (!state.isUploading) {
                        onShowGalleryPickerChanged(true)
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .zIndex(1f),
                enabled = !state.isUploading,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (state.isUploading) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.primary,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change avatar",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Name Field
        OutlinedTextField(
            value = state.name,
            onValueChange = { newName ->
                viewModel.onEvent(EditProfileUiEvent.NameChanged(newName))
            },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving && !state.isUploading,
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                viewModel.onEvent(EditProfileUiEvent.SaveProfile)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !state.isSaving && !state.isUploading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Save Profile")
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize().padding(top = 24.dp)) {
        if (showGalleryPicker) {
            GalleryPickerLauncher(
                allowMultiple = false,
                mimeTypes = listOf(MimeType.IMAGE_JPEG, MimeType.IMAGE_PNG),
                cameraCaptureConfig = CameraCaptureConfig(
                    compressionLevel = CompressionLevel.HIGH,
                    cropConfig = CropConfig(
                        enabled = true,
                        squareCrop = false
                    )
                ),
                onPhotosSelected = { photos ->
                    if (photos.isNotEmpty()) {
                        val photo = photos.first()
                        viewModel.onPhotoSelected(photo)
                    }
                    onShowGalleryPickerChanged(false)
                },
                onError = {
                    onShowGalleryPickerChanged(false)
                },
                onDismiss = {
                    onShowGalleryPickerChanged(false)
                }
            )
        }
    }
}
