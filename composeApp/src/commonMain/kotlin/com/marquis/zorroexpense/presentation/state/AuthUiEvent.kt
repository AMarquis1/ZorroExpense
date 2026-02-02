package com.marquis.zorroexpense.presentation.state

/**
 * Sealed class representing user events in authentication screens.
 */
sealed class AuthUiEvent {
    data class EmailChanged(
        val email: String,
    ) : AuthUiEvent()

    data class PasswordChanged(
        val password: String,
    ) : AuthUiEvent()

    data class DisplayNameChanged(
        val displayName: String,
    ) : AuthUiEvent()

    object LoginClicked : AuthUiEvent()

    object SignUpClicked : AuthUiEvent()

    object ClearError : AuthUiEvent()
}
