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
            val userIdOnly = userId.substringAfterLast("/")
            if (AppConfig.USE_MOCK_DATA) {
                // Use mock data for development/testing
                Result.success(MockExpenseData.usersMap[userIdOnly])
            } else {
                // Use Firestore for production
                firestoreService
                    .getUserById(userId)
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
                val users = userIds.mapNotNull { userPath ->
                    val userIdOnly = userPath.substringAfterLast("/")
                    MockExpenseData.usersMap[userIdOnly]
                }
                Result.success(users)
            } else {
                // Use Firestore for production
                val users = mutableListOf<User>()
                for (userPath in userIds) {
                    val userIdOnly = userPath.substringAfterLast("/")
                    firestoreService
                        .getUserById(userPath)
                        .onSuccess { userDto ->
                            userDto?.toDomain(userIdOnly)?.let { users.add(it) }
                        }
                        .onFailure { return Result.failure(it) }
                }
                Result.success(users)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}
