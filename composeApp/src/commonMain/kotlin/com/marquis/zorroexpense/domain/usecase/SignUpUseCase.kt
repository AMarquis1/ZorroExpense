package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.error.AuthError
import com.marquis.zorroexpense.domain.model.AuthUser
import com.marquis.zorroexpense.domain.repository.AuthRepository
import com.marquis.zorroexpense.domain.util.ValidationUtil

/**
 * Use case for signing up a new user.
 * Validates input and delegates to AuthRepository.
 */
class SignUpUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String,
    ): Result<AuthUser> {
        // Validate email
        if (!ValidationUtil.isValidEmail(email)) {
            return Result.failure(AuthError.InvalidEmail)
        }

        // Validate password
        if (password.length < 6) {
            return Result.failure(AuthError.WeakPassword)
        }

        // Validate display name
        if (displayName.isEmpty()) {
            return Result.failure(AuthError.UnknownError)
        }

        return authRepository.signUp(email.trim(), password, displayName.trim())
    }
}
