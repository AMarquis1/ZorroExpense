package com.marquis.zorroexpense.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.components.CategoryIconCircle
import com.marquis.zorroexpense.components.ProfileAvatar
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.presentation.components.AddCategoryButton
import com.marquis.zorroexpense.presentation.state.CategoryManagementUiEvent
import com.marquis.zorroexpense.presentation.state.CategoryManagementUiState
import com.marquis.zorroexpense.presentation.viewmodel.CategoryManagementViewModel
import io.github.ismoy.imagepickerkmp.config.CameraCaptureConfig
import io.github.ismoy.imagepickerkmp.config.CropConfig
import io.github.ismoy.imagepickerkmp.config.GalleryConfig
import io.github.ismoy.imagepickerkmp.picker.CompressionLevel
import io.github.ismoy.imagepickerkmp.picker.ImagePickerKMPConfig
import io.github.ismoy.imagepickerkmp.picker.ImagePickerResult
import io.github.ismoy.imagepickerkmp.picker.MimeType
import io.github.ismoy.imagepickerkmp.picker.rememberImagePickerKMP

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryManagementScreen(
    viewModel: CategoryManagementViewModel,
    onBackClick: () -> Unit,
    onCreateCategoryClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val imagePicker =
        rememberImagePickerKMP(
            ImagePickerKMPConfig(
                galleryConfig =
                    GalleryConfig(
                        mimeTypes = listOf(MimeType.IMAGE_JPEG, MimeType.IMAGE_PNG),
                    ),
                cropConfig = CropConfig(enabled = true, squareCrop = false),
            ),
        )

    when (val result = imagePicker.result) {
        is ImagePickerResult.Success -> {
            result.first?.let { photo ->
                viewModel.onEvent(CategoryManagementUiEvent.PhotoSelected(photo))
            }
            imagePicker.reset()
        }
        is ImagePickerResult.Error,
        ImagePickerResult.Dismissed,
        ImagePickerResult.Idle,
        ImagePickerResult.Loading,
        -> Unit
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Categories",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (uiState is CategoryManagementUiState.Success) {
                        if ((uiState as CategoryManagementUiState.Success).isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(12.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    viewModel.onEvent(CategoryManagementUiEvent.SaveChanges)
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Save",
                                )
                            }
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
            )
        },
    ) { paddingValues ->
        when (uiState) {
            is CategoryManagementUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is CategoryManagementUiState.Success -> {
                val successState = uiState as CategoryManagementUiState.Success
                CategoryManagementContent(
                    groupName = successState.groupName,
                    selectedCategories = successState.categories,
                    allCategories = allCategories,
                    imageUrl = successState.imageUrl,
                    isUploading = successState.isUploading,
                    onAddCategoryClick = { onCreateCategoryClick() },
                    onRemoveCategory = { viewModel.onEvent(CategoryManagementUiEvent.RemoveCategory(it)) },
                    onReactivateCategory = { viewModel.onEvent(CategoryManagementUiEvent.CategoryToggled(it)) },
                    onShowGalleryPicker = {
                        imagePicker.launchGallery(
                            cameraCaptureConfig = CameraCaptureConfig(compressionLevel = CompressionLevel.HIGH),
                        )
                    },
                    modifier = Modifier.padding(paddingValues),
                )
            }
            is CategoryManagementUiState.Error -> {
                val errorState = uiState as CategoryManagementUiState.Error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Error: ${errorState.message}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryManagementContent(
    groupName: String,
    selectedCategories: List<Category>,
    allCategories: List<Category>,
    imageUrl: String,
    isUploading: Boolean,
    onAddCategoryClick: () -> Unit,
    onRemoveCategory: (Category) -> Unit,
    onReactivateCategory: (Category) -> Unit,
    onShowGalleryPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // In new group workflow, all categories are active (no inactive state)
    val activeCategories = selectedCategories

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
    ) {
        // Group image section
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd,
            ) {
                ProfileAvatar(
                    name = groupName,
                    size = 120.dp,
                    userProfile = imageUrl,
                )

                // Camera icon overlay for uploading
                Card(
                    modifier =
                        Modifier
                            .size(50.dp)
                            .padding(4.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    shape = CircleShape,
                    onClick = if (!isUploading) ({ onShowGalleryPicker() }) else ({}),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change group image",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Select categories for \"$groupName\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                if (activeCategories.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp),
                    ) {
                        activeCategories.forEach { category ->
                            Box {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    CategoryIconCircle(
                                        category = category,
                                        size = 48.dp,
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                    )
                                }

                                // Remove (X) badge
                                Card(
                                    modifier =
                                        Modifier
                                            .size(20.dp)
                                            .align(Alignment.TopEnd),
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                        ),
                                    shape = CircleShape,
                                    onClick = { onRemoveCategory(category) },
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove ${category.name}",
                                            tint = MaterialTheme.colorScheme.onError,
                                            modifier = Modifier.size(12.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No categories added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }

                // Create New button
                AddCategoryButton(
                    onClick = { onAddCategoryClick() },
                    label = "Create New",
                    size = 48.dp,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Available categories will appear here once you create them.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
