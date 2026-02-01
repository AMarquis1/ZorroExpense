package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.CategoryDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseListDto
import com.marquis.zorroexpense.data.remote.dto.IosExpenseDto
import com.marquis.zorroexpense.data.remote.dto.IosExpenseListDto
import com.marquis.zorroexpense.data.remote.dto.UserDto
import com.marquis.zorroexpense.data.remote.dto.UserProfileDto
import com.marquis.zorroexpense.data.remote.dto.toDto
import com.marquis.zorroexpense.domain.model.UserProfile
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.firestoreSettings

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FirestoreService {
    private val firestore =
        Firebase.firestore.apply {
            // Configure Firestore with offline persistence for iOS using GitLive API
            try {
                settings =
                    firestoreSettings {
                        sslEnabled = true
                        // Note: GitLive Firebase SDK enables offline persistence by default on iOS
                        // The persistent cache will be configured automatically
                    }
            } catch (e: Exception) {
                // Settings might already be configured, that's okay
                println("FirestoreService iOS: Settings already configured or error: ${e.message}")
            }
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

            // Get the ExpenseListReferences array field from the user document
            @Suppress("UNCHECKED_CAST")
            val expenseListReferences = (userSnapshot.get("ExpenseListReferences") as? List<Any>)
                ?: emptyList()

            val lists = mutableListOf<ExpenseListDto>()
            for (reference in expenseListReferences) {
                // Extract document ID from reference path
                // Path format: "projects/zorro-expense/databases/(default)/documents/ExpenseLists/FXeLk3GspKd1fDqWJM8b"
                val referenceString = reference.toString()
                val listId = referenceString.substringAfterLast("/")

                val listSnapshot = firestore
                    .collection("ExpenseLists")
                    .document(listId)
                    .get()
                if (listSnapshot.exists) {
                    val listDto = listSnapshot.data<IosExpenseListDto>()
                    lists.add(listDto)
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
            val list = if (snapshot.exists) snapshot.data<IosExpenseListDto>() else null
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun createExpenseList(list: ExpenseListDto): Result<String> =
        try {
            val iosExpenseListDto = list as IosExpenseListDto
            val docRef = firestore
                .collection("ExpenseLists")
                .add(iosExpenseListDto)

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
            val iosExpenseListDto = list as IosExpenseListDto
            firestore
                .collection("ExpenseLists")
                .document(listId)
                .set(iosExpenseListDto)
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
                snapshot.documents.first().data<IosExpenseListDto>()
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
            val list = listSnapshot.data<IosExpenseListDto>()
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

    actual suspend fun removeUserFromExpenseListMembers(listId: String, userId: String): Result<Unit> =
        try {
            val listSnapshot = firestore
                .collection("ExpenseLists")
                .document(listId)
                .get()
            val list = listSnapshot.data<IosExpenseListDto>()
            val updatedMembers = list.memberRefs.filter { ref ->
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

    actual suspend fun addExpenseListReferenceForUser(userId: String, listId: String): Result<Unit> =
        try {
            val userDoc = firestore.collection("Users").document(userId)
            val userSnapshot = userDoc.get()

            @Suppress("UNCHECKED_CAST")
            val currentReferences = (userSnapshot.get("ExpenseListReferences") as? MutableList<Any>)
                ?.toMutableList() ?: mutableListOf()

            // Add the reference if not already present
            val alreadyExists = currentReferences.any {
                it.toString().substringAfterLast("/") == listId
            }
            if (!alreadyExists) {
                val newReference = firestore.collection("ExpenseLists").document(listId)
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
                document.data<IosExpenseDto>().copy(documentId = document.id)
            }
            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun addExpenseToList(listId: String, expense: ExpenseDto): Result<String> =
        try {
            val iosExpenseDto = expense as IosExpenseDto
            val docRef = firestore
                .collection("Expenses")
                .add(iosExpenseDto)
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun updateExpenseInList(listId: String, expenseId: String, expense: ExpenseDto): Result<Unit> =
        try {
            val iosExpenseDto = expense as IosExpenseDto
            firestore
                .collection("Expenses")
                .document(expenseId)
                .set(iosExpenseDto)
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
