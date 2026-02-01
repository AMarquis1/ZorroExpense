package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.domain.repository.UserRepository

/**
 * Use case for retrieving multiple users by their IDs.
 * Encapsulates business logic for user data retrieval.
 */
class GetUsersUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(userIds: List<String>): Result<List<User>> =
        userRepository.getUsersByIds(userIds)
}
