package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.AuthUser

/**
 * Sealed class representing the global authentication state of the app.
 * Used for navigation decisions.
 */
sealed class GlobalAuthState {
    /**
     * User is not authenticated - show login/signup
     */
    object Unauthenticated : GlobalAuthState()

    /**
     * Authentication is in progress
     */
    object Authenticating : GlobalAuthState()

    /**
     * User is authenticated - show main app
     */
    data class Authenticated(
        val user: AuthUser,
    ) : GlobalAuthState()
}
