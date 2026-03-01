package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.data.remote.StorageService
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.usecase.CreateCategoryUseCase
import com.marquis.zorroexpense.domain.usecase.GetCategoriesUseCase
import com.marquis.zorroexpense.domain.usecase.GetGroupByIdUseCase
import com.marquis.zorroexpense.domain.usecase.UpdateGroupUseCase
import com.marquis.zorroexpense.presentation.state.CategoryManagementUiEvent
import com.marquis.zorroexpense.presentation.state.CategoryManagementUiState
import io.github.ismoy.imagepickerkmp.domain.models.GalleryPhotoResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryManagementViewModel(
    private val groupId: String,
    private val groupName: String,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val storageService: StorageService,
    private val onCategoriesSaved: (groupId: String, groupName: String) -> Unit = { _, _ -> },
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<CategoryManagementUiState>(
            CategoryManagementUiState.Loading,
        )
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    val allCategories: StateFlow<List<Category>> = _allCategories.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: CategoryManagementUiEvent) {
        when (event) {
            is CategoryManagementUiEvent.CategoryToggled -> toggleCategory(event.category)
            is CategoryManagementUiEvent.RemoveCategory -> removeCategory(event.category)
            CategoryManagementUiEvent.SaveChanges -> saveChanges()
            CategoryManagementUiEvent.CancelClicked -> {} // Handle in screen
            CategoryManagementUiEvent.AddCategoryClicked -> {} // Handled in screen
            CategoryManagementUiEvent.DismissCategoryBottomSheet -> {} // Handled in screen
            is CategoryManagementUiEvent.PhotoSelected -> onPhotoSelected(event.photo)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            // Load all available categories
            getCategoriesUseCase().fold(
                onSuccess = { categories ->
                    _allCategories.value = categories

                    // By default, select the first 3 categories as active for new groups
                    val defaultCategories =
                        categories.map { category ->
                            category.copy(active = true)
                        }

                    _uiState.value =
                        CategoryManagementUiState.Success(
                            groupId = groupId,
                            groupName = groupName,
                            categories = defaultCategories,
                            imageUrl = "",
                            isUploading = false,
                        )
                },
                onFailure = { error ->
                    _uiState.value =
                        CategoryManagementUiState.Error(
                            error.message ?: "Failed to load categories",
                        )
                },
            )
        }
    }

    fun refreshCategories() {
        loadData()
    }

    private fun toggleCategory(category: Category) {
        val currentState = _uiState.value
        if (currentState is CategoryManagementUiState.Success) {
            val isAlreadySelected =
                currentState.categories.any {
                    it.documentId == category.documentId && it.active
                }

            val updatedCategories =
                if (isAlreadySelected) {
                    // Deactivate the category
                    currentState.categories.map {
                        if (it.documentId == category.documentId) it.copy(active = false) else it
                    }
                } else {
                    // Reactivate or add the category
                    val existingIndex =
                        currentState.categories.indexOfFirst {
                            it.documentId == category.documentId
                        }
                    if (existingIndex >= 0) {
                        currentState.categories.toMutableList().apply {
                            set(existingIndex, this[existingIndex].copy(active = true))
                        }
                    } else {
                        currentState.categories + category.copy(active = true)
                    }
                }

            _uiState.update {
                currentState.copy(categories = updatedCategories)
            }
        }
    }

    private fun removeCategory(category: Category) {
        val currentState = _uiState.value
        if (currentState is CategoryManagementUiState.Success) {
            // Remove from the selected categories (it will be excluded when we save)
            _uiState.update {
                currentState.copy(
                    categories =
                        currentState.categories.filter {
                            it.documentId != category.documentId
                        },
                )
            }

            // Also remove from all categories display
            _allCategories.value =
                _allCategories.value.filter {
                    it.documentId != category.documentId
                }
        }
    }

    private fun saveChanges() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is CategoryManagementUiState.Success) {
                _uiState.update { currentState.copy(isSaving = true) }

                // Separate new categories (temporary IDs starting with "temp_") from existing ones
                val existingCategories = currentState.categories.filter { !it.documentId.startsWith("temp_") }
                val newCategories = currentState.categories.filter { it.documentId.startsWith("temp_") }

                // First, create any new categories that don't have IDs yet
                val createdCategories = mutableListOf<Category>()
                var creationFailed = false

                for (newCategory in newCategories) {
                    createCategoryUseCase(groupId, newCategory).fold(
                        onSuccess = { categoryId ->
                            createdCategories.add(newCategory.copy(documentId = categoryId))
                        },
                        onFailure = { error ->
                            _uiState.value =
                                CategoryManagementUiState.Error(
                                    error.message ?: "Failed to create category",
                                )
                            creationFailed = true
                        },
                    )
                }

                if (creationFailed) return@launch

                // Combine existing and newly created categories
                val allActiveCategories =
                    (existingCategories + createdCategories)
                        .filter { it.active }
                        .map { it.copy(active = true) }

                // Now save the group with all categories
                getGroupByIdUseCase(groupId).fold(
                    onSuccess = { group ->
                        if (group != null) {
                            val updatedGroup =
                                group.copy(
                                    categories = allActiveCategories,
                                )

                            updateGroupUseCase(groupId, updatedGroup).fold(
                                onSuccess = {
                                    _uiState.update {
                                        currentState.copy(isSaving = false)
                                    }
                                    onCategoriesSaved(groupId, groupName)
                                },
                                onFailure = { error ->
                                    _uiState.value =
                                        CategoryManagementUiState.Error(
                                            error.message ?: "Failed to save categories",
                                        )
                                },
                            )
                        } else {
                            _uiState.value =
                                CategoryManagementUiState.Error(
                                    "Failed to load group",
                                )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value =
                            CategoryManagementUiState.Error(
                                error.message ?: "Failed to load group",
                            )
                    },
                )
            }
        }
    }

    fun addOrUpdateCategory(category: Category) {
        val currentState = _uiState.value
        if (currentState is CategoryManagementUiState.Success) {
            // Add to allCategories if not already there
            val updatedAllCategories =
                _allCategories.value.let { categories ->
                    val filtered = categories.filter { it.documentId != category.documentId }
                    filtered + category
                }
            _allCategories.value = updatedAllCategories

            // Check if this is a new category (not in current selected list)
            val isNewCategory =
                currentState.categories.none {
                    it.documentId == category.documentId
                }

            // Add to the selected categories with active = true
            val updatedCategories =
                if (isNewCategory) {
                    currentState.categories + category.copy(active = true)
                } else {
                    currentState.categories.map {
                        if (it.documentId == category.documentId) category.copy(active = true) else it
                    }
                }

            _uiState.update {
                currentState.copy(categories = updatedCategories)
            }
        }
    }

    fun onPhotoSelected(photo: GalleryPhotoResult) {
        // Read bytes from the photo URI and upload
        viewModelScope.launch {
            storageService
                .readImageBytesFromUri(photo.uri)
                .onSuccess { imageBytes ->
                    uploadGroupImage(imageBytes)
                }.onFailure { error ->
                    // Error handled silently - image upload is optional
                }
        }
    }

    private fun uploadGroupImage(imageBytes: ByteArray) {
        val currentState = _uiState.value as? CategoryManagementUiState.Success ?: return

        viewModelScope.launch {
            _uiState.update { currentState.copy(isUploading = true) }

            storageService
                .uploadGroupImage(groupId, imageBytes)
                .onSuccess { downloadUrl ->
                    _uiState.update {
                        currentState.copy(
                            imageUrl = downloadUrl,
                            isUploading = false,
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { currentState.copy(isUploading = false) }
                }
        }
    }
}
