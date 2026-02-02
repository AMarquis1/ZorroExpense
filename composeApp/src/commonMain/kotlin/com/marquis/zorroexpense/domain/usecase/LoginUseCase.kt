package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.error.AuthError
import com.marquis.zorroexpense.domain.model.AuthUser
import com.marquis.zorroexpense.domain.repository.AuthRepository
import com.marquis.zorroexpense.domain.util.ValidationUtil

/**
 * Use case for logging in a user.
 * Validates input and delegates to AuthRepository.
 */
class LoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): Result<AuthUser> {
        // Validate email
        if (!ValidationUtil.isValidEmail(email)) {
            return Result.failure(AuthError.InvalidEmail)
        }

        // Validate password
        if (password.isEmpty()) {
            return Result.failure(AuthError.InvalidPassword)
        }

        return authRepository.signIn(email.trim(), password)
    }
}
