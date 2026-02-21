package com.marquis.zorroexpense.data.repository

import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.remote.dto.ExpenseListDto
import com.marquis.zorroexpense.data.remote.dto.toDomain
import com.marquis.zorroexpense.data.remote.dto.toDto
import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.domain.repository.GroupRepository
import com.marquis.zorroexpense.domain.repository.UserRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GroupRepositoryImpl(
    private val firestoreService: FirestoreService,
    private val userRepository: UserRepository,
) : GroupRepository {
    private val mutex = Mutex()
    private var cachedLists: Map<String, List<Group>> = emptyMap()

    /**
     * Enrich expense list members with real user data (names, profile images)
     */
    private suspend fun enrichListMembers(list: Group): Group {
        val userIds = list.members.map { it.userId }.distinct()
        if (userIds.isEmpty()) return list

        return userRepository.getUsersByIds(userIds).getOrNull()?.let { users ->
            val userMap = users.associateBy { it.userId }
            val enrichedMembers = list.members.map { member ->
                userMap[member.userId]?.let {
                    member.copy(name = it.name, profileImage = it.profileImage)
                } ?: member
            }
            list.copy(members = enrichedMembers)
        } ?: list
    }

    override suspend fun refreshUserGroups(userId: String): Result<List<Group>> =
        mutex.withLock {
            firestoreService
                .getUserExpenseLists(userId)
                .mapCatching { dtos ->
                    dtos
                        .map { it.toDomain() }
                        .map { enrichListMembers(it) }
                        .also { lists ->
                            cachedLists = cachedLists + (userId to lists)
                        }
                }
        }

    override suspend fun getUserGroups(userId: String): Result<List<Group>> =
        mutex.withLock {
            firestoreService
                .getUserExpenseLists(userId)
                .mapCatching { dtos ->
                    dtos
                        .map { it.toDomain() }
                        .map { enrichListMembers(it) }
                        .also { lists ->
                            cachedLists = cachedLists + (userId to lists)
                        }
                }
        }

    override suspend fun getGroup(listId: String): Result<Group?> =
        firestoreService
            .getExpenseListById(listId)
            .mapCatching { dto ->
                dto?.let {
                    val categories = firestoreService.getGroupCategories(listId).getOrElse { emptyList() }
                    it.toDomain(categories)
                }
            }
            .mapCatching { it?.let { enrichListMembers(it) } }

    override suspend fun createGroup(list: Group): Result<String> =
        mutex.withLock {
            val dto = list.toDto()
            firestoreService.createExpenseList(dto).mapCatching { listId ->
                val categoryDtos = list.categories.map { it.toDto() }
                firestoreService.addGroupToUser(list.members.first().userId, listId)
                firestoreService.setGroupCategories(listId, categoryDtos)
                listId
            }
        }

    override suspend fun updateGroup(
        listId: String,
        list: Group,
    ): Result<Unit> =
        mutex.withLock {
            val dto = list.toDto()
            firestoreService.updateExpenseList(listId, dto).mapCatching {
                // Sync categories subcollection
                val currentCategories = firestoreService.getGroupCategories(listId).getOrElse { emptyList() }
                val newCategoryIds = list.categories.map { it.documentId }.toSet()
                val currentCategoryIds = currentCategories.map { it.documentId }.toSet()

                // Delete removed categories
                (currentCategoryIds - newCategoryIds).forEach { removedId ->
                    firestoreService.deleteGroupCategory(listId, removedId).getOrThrow()
                }

                // Add/update categories
                val categoryDtos = list.categories.map { it.toDto() }
                firestoreService.setGroupCategories(listId, categoryDtos).getOrThrow()
            }
        }

    override suspend fun deleteGroup(listId: String): Result<Unit> =
        mutex.withLock {
            firestoreService
                .deleteExpenseList(listId)
                .onSuccess {
                    // Clear cache for all users
                    cachedLists = emptyMap()
                }
        }

    override suspend fun joinGroup(
        userId: String,
        shareCode: String,
    ): Result<Group> =
        mutex.withLock {
            firestoreService
                .getExpenseListByShareCode(shareCode)
                .mapCatching { listDto ->
                    listDto ?: throw IllegalArgumentException("List with share code not found")
                }.onSuccess { listDto ->
                    // Add user to list members
                    firestoreService.addUserToExpenseListMembers(listDto.listId, userId).getOrThrow()
                    // Add list reference to user
                    firestoreService.addExpenseListReferenceForUser(userId, listDto.listId).getOrThrow()
                    // Clear cache
                    cachedLists = emptyMap()
                }.mapCatching { dto ->
                    val categories = firestoreService.getGroupCategories(dto.listId).getOrElse { emptyList() }
                    dto.toDomain(categories)
                }
                .mapCatching { enrichListMembers(it) }
        }

    override suspend fun removeMemberFromGroup(
        listId: String,
        userId: String,
    ): Result<Unit> =
        mutex.withLock {
            firestoreService
                .removeUserFromExpenseListMembers(listId, userId)
                .onSuccess {
                    firestoreService.removeExpenseListReferenceForUser(userId, listId).getOrThrow()
                    // Clear cache
                    cachedLists = emptyMap()
                }
        }
}

expect fun Group.toDto(): ExpenseListDto
