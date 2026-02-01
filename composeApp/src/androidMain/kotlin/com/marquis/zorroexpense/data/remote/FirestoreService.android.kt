package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.AndroidExpenseDto
import com.marquis.zorroexpense.data.remote.dto.CategoryDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseListDto
import com.marquis.zorroexpense.data.remote.dto.UserDto
import com.marquis.zorroexpense.data.remote.dto.toDto
import com.marquis.zorroexpense.domain.model.UserProfile
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.firestore

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FirestoreService {
    private val firestore = Firebase.firestore

    actual suspend fun getExpenses(userId: String): Result<List<ExpenseDto>> =
        try {
            val snapshot = firestore
                .collection("Users")
                .document(userId)
                .collection("Expenses")
                .get()
            val expenses =
                snapshot.documents.map { document ->
                    document.data<AndroidExpenseDto>().copy(documentId = document.id)
                }

            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun addExpense(userId: String, expense: ExpenseDto): Result<Unit> =
        try {
            // Cast to AndroidExpenseDto for platform-specific implementation
            val androidExpenseDto = expense as AndroidExpenseDto

            // Add the expense to Firestore
            firestore
                .collection("Users")
                .document(userId)
                .collection("Expenses")
                .add(androidExpenseDto)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun updateExpense(
        userId: String,
        expenseId: String,
        expense: ExpenseDto,
    ): Result<Unit> =
        try {
            val androidExpenseDto = expense as AndroidExpenseDto

            firestore
                .collection("Users")
                .document(userId)
                .collection("Expenses")
                .document(expenseId)
                .set(androidExpenseDto)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> =
        try {
            firestore
                .collection("Users")
                .document(userId)
                .collection("Expenses")
                .document(expenseId)
                .delete()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getUsers(): Result<List<UserDto>> =
        try {
            val snapshot = firestore.collection("Users").get()
            val users =
                snapshot.documents.map { document ->
                    val userData = document.data<UserDto>()
                    userData.copy(documentId = document.id) // Set the document ID
                }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getCategories(): Result<List<CategoryDto>> =
        try {
            val snapshot = firestore.collection("Categories").get()
            val categories =
                snapshot.documents.map { document ->
                    val categoryDto = document.data<CategoryDto>()
                    categoryDto.copy(documentId = document.id)
                }

            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getUserById(userId: String): Result<UserDto?> =
        try {
            val document = firestore.document(userId).get()
            val user =
                if (document.exists) {
                    document.data<UserDto>()
                } else {
                    null
                }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getCategoryById(categoryId: String): Result<CategoryDto?> =
        try {
            val document = firestore.document(categoryId).get()
            val category =
                if (document.exists) {
                    val categoryDto = document.data<CategoryDto>()
                    categoryDto.copy(documentId = document.id)
                } else {
                    null
                }

            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun createUserProfile(userId: String, profile: UserProfile): Result<Unit> =
        try {
            val profileDto = profile.toDto()
            firestore.collection("Users").document(userId).set(profileDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getUserExpenseLists(userId: String): Result<List<ExpenseListDto>> =
        try {
            val userSnapshot = firestore
                .collection("Users")
                .document(userId)
                .get()

            val expenseListReferences = (userSnapshot.get("ExpenseListReferences") as? List<DocumentReference>)
                ?: emptyList()

            val lists = mutableListOf<ExpenseListDto>()
            for (reference in expenseListReferences) {
                val expensePath = reference.path.substringAfterLast("/")
                try {
                    val listSnapshot = firestore
                        .collection("ExpenseLists")
                        .document(expensePath)
                        .get()
                    if (listSnapshot.exists) {
                        val listDto = listSnapshot.data<ExpenseListDto>()
                        lists.add(listDto)
                    }
                } catch (_: Exception) {
                    continue
                }
            }

            Result.success(lists)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getExpenseListById(listId: String): Result<ExpenseListDto?> =
        try {
            val snapshot = firestore
                .collection("ExpenseLists")
                .document(listId)
                .get()
            val list = if (snapshot.exists) snapshot.data<ExpenseListDto>() else null
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun createExpenseList(list: ExpenseListDto): Result<String> =
        try {
            val docRef = firestore
                .collection("ExpenseLists")
                .add(list)

            // Update the document to set the listId to the auto-generated document ID
            firestore
                .collection("ExpenseLists")
                .document(docRef.id)
                .update("listId" to docRef.id)

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun updateExpenseList(listId: String, list: ExpenseListDto): Result<Unit> =
        try {
            firestore
                .collection("ExpenseLists")
                .document(listId)
                .set(list)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun deleteExpenseList(listId: String): Result<Unit> =
        try {
            firestore
                .collection("ExpenseLists")
                .document(listId)
                .delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getExpenseListByShareCode(shareCode: String): Result<ExpenseListDto?> =
        try {
            val snapshot = firestore
                .collection("ExpenseLists")
                .where { "shareCode" equalTo shareCode }
                .get()
            val list = if (snapshot.documents.isNotEmpty()) {
                snapshot.documents.first().data<ExpenseListDto>()
            } else {
                null
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun addUserToExpenseListMembers(listId: String, userId: String): Result<Unit> =
        try {
            val listSnapshot = firestore
                .collection("ExpenseLists")
                .document(listId)
                .get()
            val list = listSnapshot.data<ExpenseListDto>()
            val updatedMembers = (list.members + userId).distinct()
            firestore
                .collection("ExpenseLists")
                .document(listId)
                .update("members" to updatedMembers)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun removeUserFromExpenseListMembers(listId: String, userId: String): Result<Unit> =
        try {
            val listSnapshot = firestore
                .collection("ExpenseLists")
                .document(listId)
                .get()
            val list = listSnapshot.data<ExpenseListDto>()
            val updatedMembers = list.members.filter { it != userId }
            firestore
                .collection("ExpenseLists")
                .document(listId)
                .update("members" to updatedMembers)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun addExpenseListReferenceForUser(userId: String, listId: String): Result<Unit> =
        try {
            val userDoc = firestore.collection("Users").document(userId)
            val userSnapshot = userDoc.get()

            @Suppress("UNCHECKED_CAST")
            val currentReferences = (userSnapshot.get("ExpenseListReferences") as? MutableList<Any>)
                ?.toMutableList() ?: mutableListOf()

            // Add the reference if not already present
            val newReference = firestore.collection("ExpenseLists").document(listId)
            val alreadyExists = currentReferences.any {
                it.toString().substringAfterLast("/") == listId
            }
            if (!alreadyExists) {
                currentReferences.add(newReference)
            }

            userDoc.update("ExpenseListReferences" to currentReferences)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun removeExpenseListReferenceForUser(userId: String, listId: String): Result<Unit> =
        try {
            val userDoc = firestore.collection("Users").document(userId)
            val userSnapshot = userDoc.get()

            @Suppress("UNCHECKED_CAST")
            val currentReferences = (userSnapshot.get("ExpenseListReferences") as? MutableList<Any>)
                ?.toMutableList() ?: mutableListOf()

            // Remove the reference with matching listId
            currentReferences.removeAll { reference ->
                reference.toString().substringAfterLast("/") == listId
            }

            userDoc.update("ExpenseListReferences" to currentReferences)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getExpensesByListId(listId: String): Result<List<ExpenseDto>> =
        try {
            val snapshot = firestore
                .collection("Expenses")
                .where { "listId" equalTo listId }
                .get()
            val expenses = snapshot.documents.map { document ->
                document.data<AndroidExpenseDto>().copy(documentId = document.id)
            }
            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun addExpenseToList(listId: String, expense: ExpenseDto): Result<String> =
        try {
            val androidExpenseDto = expense as AndroidExpenseDto
            val docRef = firestore
                .collection("Expenses")
                .add(androidExpenseDto)
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun updateExpenseInList(listId: String, expenseId: String, expense: ExpenseDto): Result<Unit> =
        try {
            val androidExpenseDto = expense as AndroidExpenseDto
            firestore
                .collection("Expenses")
                .document(expenseId)
                .set(androidExpenseDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun deleteExpenseFromList(listId: String, expenseId: String): Result<Unit> =
        try {
            firestore
                .collection("Expenses")
                .document(expenseId)
                .delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
