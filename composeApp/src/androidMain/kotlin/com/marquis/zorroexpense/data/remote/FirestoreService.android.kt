package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.AndroidExpenseDto
import com.marquis.zorroexpense.data.remote.dto.AndroidExpenseListDto
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

    actual suspend fun createUserProfile(
        userId: String,
        profile: UserProfile,
    ): Result<Unit> =
        try {
            val profileDto = profile.toDto()
            firestore.collection("Users").document(userId).set(profileDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getUserExpenseLists(userId: String): Result<List<ExpenseListDto>> =
        try {
            val userSnapshot =
                firestore
                    .collection("Users")
                    .document(userId)
                    .get()

            val expenseListReferences =
                (userSnapshot.get("ExpenseListReferences") as? List<DocumentReference>)
                    ?: emptyList()

            val lists = mutableListOf<ExpenseListDto>()
            for (reference in expenseListReferences) {
                try {
                    val listSnapshot = firestore.document(reference.path).get()
                    if (listSnapshot.exists) {
                        val listDto = listSnapshot.data<AndroidExpenseListDto>()
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
            val snapshot =
                firestore
                    .collection("ExpenseLists")
                    .document(listId)
                    .get()
            val list = if (snapshot.exists) snapshot.data<AndroidExpenseListDto>() else null
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun createExpenseList(list: ExpenseListDto): Result<String> =
        try {
            val androidExpenseListDto = list as AndroidExpenseListDto
            val docRef =
                firestore
                    .collection("ExpenseLists")
                    .add(androidExpenseListDto)

            // Update the document to set the listId to the auto-generated document ID
            firestore
                .collection("ExpenseLists")
                .document(docRef.id)
                .update("listId" to docRef.id)

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun updateExpenseList(
        listId: String,
        list: ExpenseListDto,
    ): Result<Unit> =
        try {
            val androidExpenseListDto = list as AndroidExpenseListDto
            firestore
                .collection("ExpenseLists")
                .document(listId)
                .set(androidExpenseListDto)
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
            val snapshot =
                firestore
                    .collection("ExpenseLists")
                    .where { "shareCode" equalTo shareCode }
                    .get()
            val list =
                if (snapshot.documents.isNotEmpty()) {
                    snapshot.documents.first().data<AndroidExpenseListDto>()
                } else {
                    null
                }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun addUserToExpenseListMembers(
        listId: String,
        userId: String,
    ): Result<Unit> =
        try {
            val listSnapshot =
                firestore
                    .collection("ExpenseLists")
                    .document(listId)
                    .get()
            val list = listSnapshot.data<AndroidExpenseListDto>()
            val userRef = firestore.collection("Users").document(userId)
            val updatedMembers = (list.memberRefs + userRef).distinctBy { it.path }
            firestore
                .collection("ExpenseLists")
                .document(listId)
                .update("members" to updatedMembers)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun removeUserFromExpenseListMembers(
        listId: String,
        userId: String,
    ): Result<Unit> =
        try {
            val listSnapshot =
                firestore
                    .collection("ExpenseLists")
                    .document(listId)
                    .get()
            val list = listSnapshot.data<AndroidExpenseListDto>()
            val updatedMembers =
                list.memberRefs.filter { ref ->
                    !ref.path.endsWith(userId)
                }
            firestore
                .collection("ExpenseLists")
                .document(listId)
                .update("members" to updatedMembers)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun addExpenseListReferenceForUser(
        userId: String,
        listId: String,
    ): Result<Unit> =
        try {
            val userDoc = firestore.collection("Users").document(userId)
            val userSnapshot = userDoc.get()

            @Suppress("UNCHECKED_CAST")
            val currentReferences =
                (userSnapshot.get("ExpenseListReferences") as? List<DocumentReference>)
                    ?.toMutableList() ?: mutableListOf()

            val newReference = firestore.collection("ExpenseLists").document(listId)
            val alreadyExists = currentReferences.any { ref ->
                ref.path.endsWith(listId)
            }

            if (!alreadyExists) {
                currentReferences.add(newReference)
                @Suppress("DEPRECATION")
                userDoc.update("ExpenseListReferences" to currentReferences)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun removeExpenseListReferenceForUser(
        userId: String,
        listId: String,
    ): Result<Unit> =
        try {
            val userDoc = firestore.collection("Users").document(userId)
            val userSnapshot = userDoc.get()

            val currentReferences =
                (userSnapshot.get("ExpenseListReferences") as? List<DocumentReference>)
                    ?.toMutableList() ?: mutableListOf()

            val removed = currentReferences.removeAll { ref ->
                ref.path.endsWith(listId)
            }

            if (removed) {
                @Suppress("DEPRECATION")
                userDoc.update("ExpenseListReferences" to currentReferences)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getExpensesByListId(listId: String): Result<List<ExpenseDto>> =
        try {
            val snapshot =
                firestore
                    .collection("ExpenseLists")
                    .document(listId)
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

    actual suspend fun getExpenseById(
        listId: String,
        expenseId: String,
    ): Result<ExpenseDto?> =
        try {
            val snapshot =
                firestore
                    .collection("ExpenseLists")
                    .document(listId)
                    .collection("Expenses")
                    .document(expenseId)
                    .get()
            val expense =
                if (snapshot.exists) {
                    snapshot.data<AndroidExpenseDto>().copy(documentId = snapshot.id)
                } else {
                    null
                }
            Result.success(expense)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun addExpenseToList(
        listId: String,
        expense: ExpenseDto,
    ): Result<String> =
        try {
            val androidExpenseDto = expense as AndroidExpenseDto
            val expensePath = androidExpenseDto.listId.path.substringAfterLast("/")
            val docRef =
                firestore
                    .collection("ExpenseLists")
                    .document(expensePath)
                    .collection("Expenses")
                    .add(androidExpenseDto)
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun updateExpenseInList(
        listId: String,
        expenseId: String,
        expense: ExpenseDto,
    ): Result<Unit> =
        try {
            val androidExpenseDto = expense as AndroidExpenseDto
            firestore
                .collection("ExpenseLists")
                .document(listId)
                .collection("Expenses")
                .document(expenseId)
                .set(androidExpenseDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun deleteExpenseFromList(
        listId: String,
        expenseId: String,
    ): Result<Unit> =
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
