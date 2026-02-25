package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.AuthUserDto
import kotlinx.coroutines.flow.Flow

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AuthService {
    actual suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
    ): Result<AuthUserDto> {
        TODO("Not yet implemented")
    }

    actual suspend fun signIn(
        email: String,
        password: String,
    ): Result<AuthUserDto> {
        TODO("Not yet implemented")
    }

    actual suspend fun signOut(): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun getCurrentUser(): Result<AuthUserDto?> {
        TODO("Not yet implemented")
    }

    actual fun getAuthStateFlow(): Flow<AuthUserDto?> {
        TODO("Not yet implemented")
    }

    actual suspend fun isAuthenticated(): Boolean {
        TODO("Not yet implemented")
    }

    actual suspend fun signInWithGoogle(idToken: String): Result<AuthUserDto> {
        TODO("Not yet implemented")
    }

    actual companion object {
        actual fun create(): AuthService {
            TODO("Not yet implemented")
        }
    }
}
