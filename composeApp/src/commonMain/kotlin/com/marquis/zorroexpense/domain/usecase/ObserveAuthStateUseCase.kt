package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.AuthUser
import com.marquis.zorroexpense.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for reactively observing authentication state changes.
 * Emits null when unauthenticated, AuthUser when authenticated.
 */
class ObserveAuthStateUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<AuthUser?> {
        return authRepository.currentUser
    }
}
