package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.repository.AuthRepository

/**
 * Use case for logging out the current user.
 */
class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.signOut()
    }
}
