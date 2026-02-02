package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.AuthUser
import com.marquis.zorroexpense.domain.repository.AuthRepository

/**
 * Use case for getting the current user one-time.
 * For reactive auth state, use ObserveAuthStateUseCase instead.
 */
class GetCurrentUserUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<AuthUser?> = authRepository.getCurrentUser()
}
