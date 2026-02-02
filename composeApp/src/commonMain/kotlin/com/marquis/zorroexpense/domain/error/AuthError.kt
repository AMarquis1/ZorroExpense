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
            }
}

/**
 * Extension function to convert Result exceptions to AuthError
 */
fun Throwable.toAuthError(): AuthError =
    when {
        this.message?.contains("INVALID_EMAIL", ignoreCase = true) == true -> AuthError.InvalidEmail
        this.message?.contains("INVALID_PASSWORD", ignoreCase = true) == true -> AuthError.InvalidPassword
        this.message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true -> AuthError.InvalidCredentials
        this.message?.contains("EMAIL_EXISTS", ignoreCase = true) == true -> AuthError.EmailAlreadyInUse
        this.message?.contains("WEAK_PASSWORD", ignoreCase = true) == true -> AuthError.WeakPassword
        this.message?.contains("USER_DISABLED", ignoreCase = true) == true -> AuthError.AccountDisabled
        this.message?.contains("network", ignoreCase = true) == true -> AuthError.NetworkError
        this.message?.contains("USER_NOT_FOUND", ignoreCase = true) == true -> AuthError.UserNotFound
        else -> AuthError.UnknownError
    }
