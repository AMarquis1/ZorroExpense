package com.marquis.zorroexpense.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.components.CategoryIconCircle
import com.marquis.zorroexpense.presentation.state.CategoryDetailUiEvent
import com.marquis.zorroexpense.presentation.state.CategoryDetailUiState
import com.marquis.zorroexpense.presentation.state.CategoryDetailMode
import com.marquis.zorroexpense.presentation.viewmodel.CategoryDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    viewModel: CategoryDetailViewModel,
    onBackClick: () -> Unit,
    onCategoryDeleted: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    val title = when (uiState) {
                        is CategoryDetailUiState.Success -> {
                            when ((uiState as CategoryDetailUiState.Success).mode) {
                                CategoryDetailMode.VIEW -> "Category"
                                CategoryDetailMode.EDIT -> "Edit Category"
                                CategoryDetailMode.ADD -> "New Category"
                            }
                        }
                        else -> "Category"
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val currentState = uiState
                        if (currentState is CategoryDetailUiState.Success &&
                            currentState.mode != CategoryDetailMode.VIEW
                        ) {
                            viewModel.onEvent(CategoryDetailUiEvent.CancelEdit)
                        } else {
                            onBackClick()
                        }
                    }) {
                        val icon = if (uiState is CategoryDetailUiState.Success &&
                            (uiState as CategoryDetailUiState.Success).mode != CategoryDetailMode.VIEW
                        ) {
                            Icons.Default.Close
                        } else {
                            Icons.AutoMirrored.Filled.ArrowBack
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (uiState is CategoryDetailUiState.Success) {
                        val successState = uiState as CategoryDetailUiState.Success

                        when (successState.mode) {
                            CategoryDetailMode.VIEW -> {
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(CategoryDetailUiEvent.EnterEditMode)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit",
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(CategoryDetailUiEvent.DeleteCategory)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                    )
                                }
                            }
                            CategoryDetailMode.EDIT, CategoryDetailMode.ADD -> {
                                if (successState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(12.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            viewModel.onEvent(CategoryDetailUiEvent.SaveChanges)
                                        },
                                        enabled = successState.editedName.isNotBlank(),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Save",
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { paddingValues ->
        when (uiState) {
            is CategoryDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is CategoryDetailUiState.Success -> {
                val successState = uiState as CategoryDetailUiState.Success
                CategoryDetailContent(
                    category = successState.category,
                    mode = successState.mode,
                    editedName = successState.editedName,
                    editedIcon = successState.editedIcon,
                    editedColor = successState.editedColor,
                    onNameChange = { viewModel.onEvent(CategoryDetailUiEvent.UpdateName(it)) },
                    onIconChange = { viewModel.onEvent(CategoryDetailUiEvent.UpdateIcon(it)) },
                    onColorChange = { viewModel.onEvent(CategoryDetailUiEvent.UpdateColor(it)) },
                    modifier = Modifier.padding(paddingValues),
                )

                if (successState.showDeleteDialog) {
                    DeleteCategoryConfirmationDialog(
                        categoryName = successState.category.name,
                        onConfirm = { viewModel.onEvent(CategoryDetailUiEvent.ConfirmDelete) },
                        onDismiss = { viewModel.onEvent(CategoryDetailUiEvent.CancelDelete) },
                    )
                }
            }
            is CategoryDetailUiState.Deleted -> {
                onCategoryDeleted()
            }
            is CategoryDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (uiState as CategoryDetailUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun hexStringToColor(colorHex: String): Color {
    val hexString = colorHex.removePrefix("#")
    val long = hexString.toLongOrNull(16) ?: 0xFFFFFFFF
    return Color(0xFF000000 or long)
}

private fun getIconByName(iconName: String) = when (iconName) {
    "Others" -> Icons.Filled.QuestionMark
    "ShoppingCart" -> Icons.Filled.ShoppingCart
    "Pets" -> Icons.Filled.Pets
    "Restaurant" -> Icons.Filled.Restaurant
    "DirectionsCar" -> Icons.Filled.DirectionsCar
    "Work" -> Icons.Filled.Work
    "Movie" -> Icons.Filled.Movie
    "FitnessCenter" -> Icons.Filled.FitnessCenter
    "LocalDining" -> Icons.Filled.LocalDining
    "School" -> Icons.Filled.School
    "LocalCafe" -> Icons.Filled.LocalCafe
    "Book" -> Icons.Filled.Book
    "MusicNote" -> Icons.Filled.MusicNote
    "LocalMovies" -> Icons.Filled.LocalMovies
    "Sports" -> Icons.Filled.Sports
    "FlightTakeoff" -> Icons.Filled.FlightTakeoff
    "LocalParking" -> Icons.Filled.LocalParking
    "LocalGasStation" -> Icons.Filled.LocalGasStation
    "LocalPharmacy" -> Icons.Filled.LocalPharmacy
    "LocalHospital" -> Icons.Filled.LocalHospital
    "LocalFlorist" -> Icons.Filled.LocalFlorist
    "LocalLaundryService" -> Icons.Filled.LocalLaundryService
    "LocalShipping" -> Icons.Filled.LocalShipping
    "LocalOffer" -> Icons.Filled.LocalOffer
    else -> Icons.Filled.QuestionMark
}

@Composable
private fun CategoryDetailContent(
    category: com.marquis.zorroexpense.domain.model.Category,
    mode: CategoryDetailMode,
    editedName: String,
    editedIcon: String,
    editedColor: String,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val icons = listOf(
        "Others",
        "ShoppingCart",
        "Pets",
        "Restaurant",
        "DirectionsCar",
        "Work",
        "Movie",
        "FitnessCenter",
        "LocalDining",
        "School",
        "LocalCafe",
        "Book",
        "MusicNote",
        "LocalMovies",
        "Sports",
        "FlightTakeoff",
        "LocalParking",
        "LocalGasStation",
        "LocalPharmacy",
        "LocalHospital",
        "LocalFlorist",
        "LocalLaundryService",
        "LocalShipping",
        "LocalOffer",
    )
    val colors = listOf(
        "#FF5722",
        "#2196F3",
        "#4CAF50",
        "#FF9800",
        "#9C27B0",
        "#F44336",
        "#009688",
        "#607D8B",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Preview card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            CategoryIconCircle(
                category = com.marquis.zorroexpense.domain.model.Category(
                    documentId = category.documentId,
                    name = editedName,
                    icon = editedIcon,
                    color = editedColor,
                ),
                size = 80.dp,
            )
        }

        // Name field
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Name",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (mode == CategoryDetailMode.VIEW) {
                Text(
                    text = editedName,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter category name") },
                    singleLine = true,
                )
            }
        }

        // Icon picker
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Icon",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            // Grid layout with 4 columns
            icons.chunked(6).forEach { rowIcons ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowIcons.forEach { icon ->
                        Box(modifier = Modifier.weight(1f)) {
                            IconChip(
                                label = icon,
                                isSelected = editedIcon == icon,
                                enabled = mode != CategoryDetailMode.VIEW,
                                onClick = { onIconChange(icon) },
                            )
                        }
                    }
                    // Add spacers for the last row if it has fewer than 4 items
                    repeat(6 - rowIcons.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Color picker
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Color",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(colors) { color ->
                    ColorSwatch(
                        color = hexStringToColor(color),
                        isSelected = editedColor == color,
                        enabled = mode != CategoryDetailMode.VIEW,
                        onClick = { onColorChange(color) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun IconChip(
    label: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp),
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = getIconByName(label),
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(color, CircleShape)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = CircleShape,
            )
            .clickable(enabled = enabled) { onClick() },
    )
}

@Composable
private fun DeleteCategoryConfirmationDialog(
    categoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Category?") },
        text = {
            Text(
                "Are you sure you want to delete \"$categoryName\"? " +
                    "This action cannot be undone.",
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
