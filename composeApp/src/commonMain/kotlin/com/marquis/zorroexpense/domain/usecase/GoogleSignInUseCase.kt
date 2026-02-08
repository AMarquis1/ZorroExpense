package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.error.AuthError
import com.marquis.zorroexpense.domain.model.AuthUser
import com.marquis.zorroexpense.domain.repository.AuthRepository

/**
 * Use case for signing in with Google ID token.
 * Exchanges Google credential for Firebase authentication.
 */
class GoogleSignInUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<AuthUser> {
        if (idToken.isEmpty()) {
            return Result.failure(AuthError.GoogleSignInFailed)
        }
        return authRepository.signInWithGoogle(idToken)
    }
}
