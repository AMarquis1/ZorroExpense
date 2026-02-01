package com.marquis.zorroexpense.data.repository

import com.marquis.zorroexpense.data.remote.AuthService
import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.remote.dto.toDomain
import com.marquis.zorroexpense.data.remote.dto.toDto
import com.marquis.zorroexpense.domain.error.toAuthError
import com.marquis.zorroexpense.domain.model.AuthUser
import com.marquis.zorroexpense.domain.model.UserProfile
import com.marquis.zorroexpense.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Implementation of AuthRepository with cache-first pattern.
 * Provides reactive auth state management with Firestore user profile creation.
 */
class AuthRepositoryImpl(
    private val authService: AuthService,
    private val firestoreService: FirestoreService
) : AuthRepository {
    private val cacheMutex = Mutex()
    private var cachedUser: AuthUser? = null

    override val currentUser: Flow<AuthUser?> = authService.getAuthStateFlow().map { authUserDto ->
        val user = authUserDto?.toDomain()
        cacheMutex.withLock {
            cachedUser = user
        }
        user
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthUser> = cacheMutex.withLock {
        try {
            // Create user in Firebase Auth
            val authResult = authService.signUp(email, password, displayName)
            if (authResult.isFailure) {
                return authResult.mapCatching { it.toDomain() }
            }

            val authUser = authResult.getOrNull()?.toDomain()
                ?: return Result.failure(Exception("Auth user is null"))

            // Create user profile in Firestore
            val userProfile = UserProfile(
                userId = authUser.userId,
                email = authUser.email,
                name = displayName,
                createdAt = Clock.System.now().toString()
            )

            firestoreService.createUserProfile(authUser.userId, userProfile)
                .onFailure { error ->
                    // Log profile creation failure but don't fail auth
                    println("Failed to create user profile: ${error.message}")
                }

            cachedUser = authUser
            Result.success(authUser)
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<AuthUser> = cacheMutex.withLock {
        try {
            val authResult = authService.signIn(email, password)
            if (authResult.isFailure) {
                return authResult.mapCatching { it.toDomain() }
            }

            val authUser = authResult.getOrNull()?.toDomain()
                ?: return Result.failure(Exception("Auth user is null"))

            cachedUser = authUser
            Result.success(authUser)
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun signOut(): Result<Unit> = cacheMutex.withLock {
        try {
            val result = authService.signOut()
            if (result.isSuccess) {
                cachedUser = null
            }
            result
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun getCurrentUser(): Result<AuthUser?> {
        cacheMutex.withLock {
            cachedUser?.let { return Result.success(it) }
        }

        return try {
            val authResult = authService.getCurrentUser()
            val authUser = authResult.getOrNull()?.toDomain()

            cacheMutex.withLock {
                cachedUser = authUser
            }

            Result.success(authUser)
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        cacheMutex.withLock {
            cachedUser?.let { return true }
        }

        return authService.isAuthenticated()
    }
}
