package com.marquis.zorroexpense.data.repository

import com.marquis.zorroexpense.AppConfig
import com.marquis.zorroexpense.MockExpenseData
import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.remote.dto.toDomain
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.domain.repository.UserRepository

/**
 * Repository implementation for user-related operations.
 * Handles switching between mock data and Firestore based on configuration.
 */
class UserRepositoryImpl(
    private val firestoreService: FirestoreService,
) : UserRepository {
    override suspend fun getUserById(userId: String): Result<User?> =
        try {
            // Extract just the userId (remove "Users/" prefix if present)
            val userIdOnly = if (userId.startsWith("Users/")) userId.substringAfterLast("/") else userId
            if (AppConfig.USE_MOCK_DATA) {
                // Use mock data for development/testing
                Result.success(MockExpenseData.usersMap[userIdOnly])
            } else {
                // Use Firestore for production
                firestoreService
                    .getUserById(userIdOnly)
                    .mapCatching { userDto ->
                        userDto?.toDomain(userIdOnly)
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getUsersByIds(userIds: List<String>): Result<List<User>> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                // Use mock data for development/testing
                val users =
                    userIds.mapNotNull { userPath ->
                        // Normalize path: if it doesn't start with "Users/", add it
                        val normalizedPath = if (userPath.startsWith("Users/")) userPath else "Users/$userPath"
                        val userIdOnly = normalizedPath.substringAfterLast("/")
                        MockExpenseData.usersMap[userIdOnly]
                    }
                Result.success(users)
            } else {
                // Use Firestore for production
                val users = mutableListOf<User>()
                for (userPath in userIds) {
                    // Extract just the userId (remove "Users/" prefix if present)
                    val userIdOnly = if (userPath.startsWith("Users/")) userPath.substringAfterLast("/") else userPath
                    firestoreService
                        .getUserById(userIdOnly)
                        .onSuccess { userDto ->
                            userDto?.toDomain(userIdOnly)?.let { users.add(it) }
                        }.onFailure { return Result.failure(it) }
                }
                Result.success(users)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun updateProfile(
        userId: String,
        name: String,
        profileImageUrl: String?,
    ): Result<Unit> =
        try {
            // Extract just the userId (remove "Users/" prefix if present)
            val userIdOnly = if (userId.startsWith("Users/")) userId.substringAfterLast("/") else userId
            if (AppConfig.USE_MOCK_DATA) {
                // Update mock data in memory
                val existingUser = MockExpenseData.usersMap[userIdOnly]
                if (existingUser != null) {
                    MockExpenseData.usersMap[userIdOnly] =
                        existingUser.copy(
                            name = name,
                            profileImage = profileImageUrl ?: existingUser.profileImage,
                        )
                }
                Result.success(Unit)
            } else {
                // Use Firestore for production - pass full path to updateUserProfile
                val fullPath = "Users/$userIdOnly"
                firestoreService.updateUserProfile(fullPath, name, profileImageUrl)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}
