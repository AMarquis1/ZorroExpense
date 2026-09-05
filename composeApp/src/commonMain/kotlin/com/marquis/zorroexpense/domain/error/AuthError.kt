package com.marquis.zorroexpense.domain.error

/**
 * Sealed class representing authentication-specific errors.
 * Converts Firebase exceptions to domain-level errors.
 */
sealed class AuthError : Exception() {
    object InvalidEmail : AuthError()

    object InvalidPassword : AuthError()

    object InvalidCredentials : AuthError()

    object EmailAlreadyInUse : AuthError()

    object WeakPassword : AuthError()

    object UserNotFound : AuthError()

    object AccountDisabled : AuthError()

    object NetworkError : AuthError()

    object UnknownError : AuthError()

    object GoogleSignInCancelled : AuthError()

    object GoogleSignInFailed : AuthError()

    object GooglePlayServicesUnavailable : AuthError()

    override val message: String
        get() =
            when (this) {
                InvalidEmail -> "Invalid email address"
                InvalidPassword -> "Invalid password"
                InvalidCredentials -> "Email or password is incorrect"
                EmailAlreadyInUse -> "Email is already registered"
                WeakPassword -> "Password is too weak. Use at least 6 characters"
                UserNotFound -> "User account not found"
                AccountDisabled -> "User account has been disabled"
                NetworkError -> "Network error. Please check your connection"
                UnknownError -> "An unexpected error occurred"
                GoogleSignInCancelled -> "Google sign-in was cancelled"
                GoogleSignInFailed -> "Google sign-in failed. Please try again"
                GooglePlayServicesUnavailable -> "Google Play Services is unavailable"
            }
}

/**
 * Extension function to convert Result exceptions to AuthError
 */
fun Throwable.toAuthError(): AuthError =
    when {
        messageContains("INVALID_EMAIL", "badly formatted", "invalid email") -> AuthError.InvalidEmail
        messageContains("INVALID_PASSWORD") -> AuthError.InvalidPassword
        messageContains("INVALID_LOGIN_CREDENTIALS", "invalid credential", "wrong password") -> AuthError.InvalidCredentials
        messageContains("EMAIL_EXISTS", "already in use") -> AuthError.EmailAlreadyInUse
        messageContains("WEAK_PASSWORD", "password should be at least") -> AuthError.WeakPassword
        messageContains("USER_DISABLED", "has been disabled") -> AuthError.AccountDisabled
        messageContains("network", "offline") -> AuthError.NetworkError
        messageContains("USER_NOT_FOUND", "no user record") -> AuthError.UserNotFound
        else -> AuthError.UnknownError
    }

private fun Throwable.messageContains(vararg values: String): Boolean =
    values.any { value -> message?.contains(value, ignoreCase = true) == true }
