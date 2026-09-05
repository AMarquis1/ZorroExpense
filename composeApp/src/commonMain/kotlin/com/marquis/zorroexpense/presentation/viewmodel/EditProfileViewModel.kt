package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.data.remote.StorageService
import com.marquis.zorroexpense.domain.repository.UserRepository
import com.marquis.zorroexpense.domain.usecase.GetCurrentUserUseCase
import com.marquis.zorroexpense.presentation.state.EditProfileUiEvent
import com.marquis.zorroexpense.presentation.state.EditProfileUiState
import io.github.ismoy.imagepickerkmp.picker.GalleryPhotoResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val userRepository: UserRepository,
    private val storageService: StorageService,
) : ViewModel() {
    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var currentUserId: String = ""
    private var currentImageUrl: String = ""

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase()
                .onSuccess { authUser ->
                    if (authUser != null) {
                        currentUserId = authUser.userId
                        // Fetch the full user profile from the repository
                        userRepository
                            .getUserById("Users/${authUser.userId}")
                            .onSuccess { userProfile ->
                                if (userProfile != null) {
                                    currentImageUrl = userProfile.profileImage
                                    _uiState.value =
                                        EditProfileUiState.Success(
                                            userId = authUser.userId,
                                            name = userProfile.name,
                                            profileImageUrl = userProfile.profileImage,
                                        )
                                } else {
                                    // Use auth user display name if profile not found
                                    _uiState.value =
                                        EditProfileUiState.Success(
                                            userId = authUser.userId,
                                            name = authUser.displayName ?: authUser.email,
                                            profileImageUrl = "",
                                        )
                                }
                            }.onFailure { error ->
                                // Fallback to auth user data if profile fetch fails
                                _uiState.value =
                                    EditProfileUiState.Success(
                                        userId = authUser.userId,
                                        name = authUser.displayName ?: authUser.email,
                                        profileImageUrl = "",
                                    )
                            }
                    } else {
                        _uiState.value = EditProfileUiState.Error("Could not load current user")
                    }
                }.onFailure { error ->
                    _uiState.value = EditProfileUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun onEvent(event: EditProfileUiEvent) {
        when (event) {
            is EditProfileUiEvent.NameChanged -> {
                updateName(event.name)
            }

            is EditProfileUiEvent.ImageSelected -> {
                uploadImage(event.imageBytes)
            }

            is EditProfileUiEvent.SaveProfile -> {
                saveProfile()
            }
        }
    }

    fun onPhotoSelected(photo: GalleryPhotoResult) {
        // Read bytes from the photo URI and upload
        viewModelScope.launch {
            storageService
                .readImageBytesFromUri(photo.uri)
                .onSuccess { imageBytes ->
                    uploadImage(imageBytes)
                }.onFailure { error ->
                    _uiState.value =
                        EditProfileUiState.Error(
                            error.message ?: "Failed to read image",
                        )
                }
        }
    }

    private fun updateName(name: String) {
        val currentState = _uiState.value as? EditProfileUiState.Success ?: return
        _uiState.value = currentState.copy(name = name)
    }

    private fun uploadImage(imageBytes: ByteArray) {
        val currentState = _uiState.value as? EditProfileUiState.Success ?: return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isUploading = true)

            storageService
                .uploadProfileImage(currentUserId, imageBytes)
                .onSuccess { downloadUrl ->
                    currentImageUrl = downloadUrl
                    _uiState.value =
                        currentState.copy(
                            profileImageUrl = downloadUrl,
                            isUploading = false,
                        )
                }.onFailure { error ->
                    _uiState.value =
                        EditProfileUiState.Error(
                            error.message ?: "Failed to upload image",
                        )
                }
        }
    }

    private fun saveProfile() {
        val currentState = _uiState.value as? EditProfileUiState.Success ?: return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSaving = true)

            userRepository
                .updateProfile(
                    userId = currentUserId,
                    name = currentState.name,
                    profileImageUrl = currentState.profileImageUrl,
                ).onSuccess {
                    _uiState.value = EditProfileUiState.Saved
                }.onFailure { error ->
                    _uiState.value =
                        EditProfileUiState.Error(
                            error.message ?: "Failed to save profile",
                        )
                }
        }
    }
}
