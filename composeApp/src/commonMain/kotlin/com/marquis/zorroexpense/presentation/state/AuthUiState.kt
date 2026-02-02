package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.AuthUser

/**
 * Sealed class representing the UI state for authentication screens.
 */
sealed class AuthUiState {
    /**
     * Initial state - no operation in progress
     */
    object Idle : AuthUiState()

    /**
     * Loading state - auth operation in progress
     */
    object Loading : AuthUiState()

    /**
     * Success state - auth operation completed
     */
    data class Success(
        val user: AuthUser,
    ) : AuthUiState()

    /**
     * Error state - auth operation failed
     */
    data class Error(
        val message: String,
    ) : AuthUiState()
}
