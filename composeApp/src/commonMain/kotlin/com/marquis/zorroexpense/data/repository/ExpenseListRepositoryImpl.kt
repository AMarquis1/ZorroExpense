package com.marquis.zorroexpense.data.repository

import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.remote.dto.ExpenseListDto
import com.marquis.zorroexpense.data.remote.dto.toDomain
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.repository.ExpenseListRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ExpenseListRepositoryImpl(
    private val firestoreService: FirestoreService,
) : ExpenseListRepository {
    private val mutex = Mutex()
    private var cachedLists: Map<String, List<ExpenseList>> = emptyMap()

    override suspend fun getUserExpenseLists(userId: String): Result<List<ExpenseList>> =
        mutex.withLock {
            firestoreService.getUserExpenseLists(userId)
                .mapCatching { dtos ->
                    dtos.map { it.toDomain(firestoreService) }
                        .also { lists ->
                            cachedLists = cachedLists + (userId to lists)
                        }
                }
        }

    override suspend fun getExpenseListById(listId: String): Result<ExpenseList?> =
        firestoreService.getExpenseListById(listId)
            .mapCatching { it?.toDomain(firestoreService) }

    override suspend fun createExpenseList(list: ExpenseList): Result<String> =
        mutex.withLock {
            val dto = list.toDto()
            firestoreService.createExpenseList(dto)
        }

    override suspend fun updateExpenseList(listId: String, list: ExpenseList): Result<Unit> =
        mutex.withLock {
            val dto = list.toDto()
            firestoreService.updateExpenseList(listId, dto)
        }

    override suspend fun deleteExpenseList(listId: String): Result<Unit> =
        mutex.withLock {
            firestoreService.deleteExpenseList(listId)
                .onSuccess {
                    // Clear cache for all users
                    cachedLists = emptyMap()
                }
        }

    override suspend fun joinExpenseList(userId: String, shareCode: String): Result<ExpenseList> =
        mutex.withLock {
            firestoreService.getExpenseListByShareCode(shareCode)
                .mapCatching { listDto ->
                    listDto ?: throw IllegalArgumentException("List with share code not found")
                }
                .onSuccess { listDto ->
                    // Add user to list members
                    firestoreService.addUserToExpenseListMembers(listDto.listId, userId).getOrThrow()
                    // Add list reference to user
                    firestoreService.addExpenseListReferenceForUser(userId, listDto.listId).getOrThrow()
                    // Clear cache
                    cachedLists = emptyMap()
                }
                .mapCatching { it.toDomain(firestoreService) }
        }

    override suspend fun removeMemberFromList(listId: String, userId: String): Result<Unit> =
        mutex.withLock {
            firestoreService.removeUserFromExpenseListMembers(listId, userId)
                .onSuccess {
                    firestoreService.removeExpenseListReferenceForUser(userId, listId).getOrThrow()
                    // Clear cache
                    cachedLists = emptyMap()
                }
        }

}

expect fun ExpenseList.toDto(): ExpenseListDto
