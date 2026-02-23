package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.User

sealed class EditProfileUiState {
    data object Loading : EditProfileUiState()

    data class Success(
        val userId: String = "",
        val name: String = "",
        val profileImageUrl: String = "",
        val isUploading: Boolean = false,
        val isSaving: Boolean = false,
    ) : EditProfileUiState()

    data class Error(val message: String) : EditProfileUiState()
}

sealed class EditProfileUiEvent {
    data class NameChanged(val name: String) : EditProfileUiEvent()

    data class ImageSelected(val imageBytes: ByteArray) : EditProfileUiEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ImageSelected) return false
            return imageBytes.contentEquals(other.imageBytes)
        }

        override fun hashCode(): Int {
            return imageBytes.contentHashCode()
        }
    }

    data object SaveProfile : EditProfileUiEvent()
}
