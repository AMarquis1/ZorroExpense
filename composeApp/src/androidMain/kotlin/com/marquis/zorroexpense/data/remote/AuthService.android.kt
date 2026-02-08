package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.AuthUserDto
import com.marquis.zorroexpense.domain.error.AuthError
import com.marquis.zorroexpense.domain.error.toAuthError
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Android implementation of AuthService using Firebase Auth (GitLive SDK).
 */
actual class AuthService {
    private val firebaseAuth = Firebase.auth

    actual companion object {
        actual fun create(): AuthService = AuthService()
    }

    actual suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
    ): Result<AuthUserDto> =
        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)

            // Update display name
            authResult.user?.updateProfile(displayName = displayName)

            val user = authResult.user ?: return Result.failure(AuthError.UnknownError)
            Result.success(user.toAuthUserDto())
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }

    actual suspend fun signIn(
        email: String,
        password: String,
    ): Result<AuthUserDto> =
        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password)
            val user = authResult.user ?: return Result.failure(AuthError.UnknownError)
            Result.success(user.toAuthUserDto())
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }

    actual suspend fun signOut(): Result<Unit> =
        try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }

    actual suspend fun getCurrentUser(): Result<AuthUserDto?> =
        try {
            val user = firebaseAuth.currentUser
            Result.success(user?.toAuthUserDto())
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }

    actual fun getAuthStateFlow(): Flow<AuthUserDto?> =
        firebaseAuth.authStateChanged.map { user ->
            user?.toAuthUserDto()
        }

    actual suspend fun isAuthenticated(): Boolean = firebaseAuth.currentUser != null

    actual suspend fun signInWithGoogle(idToken: String): Result<AuthUserDto> =
        try {
            // Create Google credential from ID token
            val credential = GoogleAuthProvider.credential(idToken, null)
            // Sign in using GitLive SDK's native credential method
            firebaseAuth.signInWithCredential(credential)
            // Get the signed-in user from GitLive auth
            val user = firebaseAuth.currentUser ?: return Result.failure(AuthError.UnknownError)
            Result.success(user.toAuthUserDto())
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }

    private fun FirebaseUser.toAuthUserDto(): AuthUserDto =
        AuthUserDto(
            userId = uid,
            email = email ?: "",
            displayName = displayName,
            isEmailVerified = isEmailVerified,
        )
}
