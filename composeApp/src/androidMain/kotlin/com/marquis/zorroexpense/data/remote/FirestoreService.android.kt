package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.AndroidExpenseDto
import com.marquis.zorroexpense.data.remote.dto.AndroidGroupDto
import com.marquis.zorroexpense.data.remote.dto.CategoryDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import com.marquis.zorroexpense.data.remote.dto.GroupDto
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

    actual suspend fun getUserGroups(userId: String): Result<List<GroupDto>> =
        try {
            val userSnapshot =
                firestore
                    .collection("Users")
                    .document(userId)
                    .get()

            val expenseListReferences =
                (userSnapshot.get("ExpenseListReferences") as? List<DocumentReference>)
                    ?: emptyList()

            val lists = mutableListOf<GroupDto>()
            for (reference in expenseListReferences) {
                try {
                    val listSnapshot = firestore.document(reference.path).get()
                    if (listSnapshot.exists) {
                        val listDto = listSnapshot.data<AndroidGroupDto>()
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

    actual suspend fun getGroupById(listId: String): Result<GroupDto?> =
        try {
            val snapshot =
                firestore
                    .collection("ExpenseLists")
                    .document(listId)
                    .get()
            val list = if (snapshot.exists) snapshot.data<AndroidGroupDto>() else null
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun createGroup(group: GroupDto): Result<String> =
        try {
            val androidExpenseListDto = group as AndroidGroupDto
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

    actual suspend fun updateGroup(
        listId: String,
        list: GroupDto,
    ): Result<Unit> =
        try {
            val androidExpenseListDto = list as AndroidGroupDto
            firestore
                .collection("ExpenseLists")
                .document(listId)
                .set(androidExpenseListDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun deleteGroup(groupId: String): Result<Unit> =
        try {
            firestore
                .collection("ExpenseLists")
                .document(groupId)
                .delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getGroupByShareCode(shareCode: String): Result<GroupDto?> =
        try {
            val snapshot =
                firestore
                    .collection("ExpenseLists")
                    .where { "shareCode" equalTo shareCode }
                    .get()
            val list =
                if (snapshot.documents.isNotEmpty()) {
                    snapshot.documents.first().data<AndroidGroupDto>()
                } else {
                    null
                }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun addUserToExpenseListMembers(
        groupId: String,
        userId: String,
    ): Result<Unit> =
        try {
            val listSnapshot =
                firestore
                    .collection("ExpenseLists")
                    .document(groupId)
                    .get()
            val list = listSnapshot.data<AndroidGroupDto>()
            val userRef = firestore.collection("Users").document(userId)
            val updatedMembers = (list.memberRefs + userRef).distinctBy { it.path }
            firestore
                .collection("ExpenseLists")
                .document(groupId)
                .update("members" to updatedMembers)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun addGroupToUser(
        userId: String,
        groupId: String,
    ): Result<Unit> =
        try {
            val userDoc = firestore.collection("Users").document(userId)
            val userSnapshot = userDoc.get()

            @Suppress("UNCHECKED_CAST")
            val currentReferences =
                (userSnapshot.get("ExpenseListReferences") as? List<DocumentReference>)
                    ?.toMutableList() ?: mutableListOf()

            val newReference = firestore.collection("ExpenseLists").document(groupId)
            val alreadyExists = currentReferences.any { ref ->
                ref.path.endsWith(groupId)
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

    actual suspend fun removeUserFromExpenseListMembers(
        groupId: String,
        userId: String,
    ): Result<Unit> =
        try {
            val listSnapshot =
                firestore
                    .collection("ExpenseLists")
                    .document(groupId)
                    .get()
            val list = listSnapshot.data<AndroidGroupDto>()
            val updatedMembers =
                list.memberRefs.filter { ref ->
                    !ref.path.endsWith(userId)
                }
            firestore
                .collection("ExpenseLists")
                .document(groupId)
                .update("members" to updatedMembers)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun addExpenseListReferenceForUser(
        userId: String,
        groupId: String,
    ): Result<Unit> =
        try {
            val userDoc = firestore.collection("Users").document(userId)
            val userSnapshot = userDoc.get()

            @Suppress("UNCHECKED_CAST")
            val currentReferences =
                (userSnapshot.get("ExpenseListReferences") as? List<DocumentReference>)
                    ?.toMutableList() ?: mutableListOf()

            val newReference = firestore.collection("ExpenseLists").document(groupId)
            val alreadyExists = currentReferences.any { ref ->
                ref.path.endsWith(groupId)
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
        groupId: String,
    ): Result<Unit> =
        try {
            val userDoc = firestore.collection("Users").document(userId)
            val userSnapshot = userDoc.get()

            val currentReferences =
                (userSnapshot.get("ExpenseListReferences") as? List<DocumentReference>)
                    ?.toMutableList() ?: mutableListOf()

            val removed = currentReferences.removeAll { ref ->
                ref.path.endsWith(groupId)
            }

            if (removed) {
                @Suppress("DEPRECATION")
                userDoc.update("ExpenseListReferences" to currentReferences)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getExpensesByListId(groupId: String): Result<List<ExpenseDto>> =
        try {
            val snapshot =
                firestore
                    .collection("ExpenseLists")
                    .document(groupId)
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
        groupId: String,
        expenseId: String,
    ): Result<ExpenseDto?> =
        try {
            val snapshot =
                firestore
                    .collection("ExpenseLists")
                    .document(groupId)
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
        groupId: String,
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
        groupId: String,
        expenseId: String,
        expense: ExpenseDto,
    ): Result<Unit> =
        try {
            val androidExpenseDto = expense as AndroidExpenseDto
            firestore
                .collection("ExpenseLists")
                .document(groupId)
                .collection("Expenses")
                .document(expenseId)
                .set(androidExpenseDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun deleteExpenseFromList(
        groupId: String,
        expenseId: String,
    ): Result<Unit> =
        try {
            firestore
                .collection("ExpenseLists")
                .document(groupId)
                .collection("Expenses")
                .document(expenseId)
                .delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun updateExpenseListLastModified(groupId: String): Result<Unit> =
        try {
            firestore
                .collection("ExpenseLists")
                .document(groupId)
                .update("lastModified" to dev.gitlive.firebase.firestore.Timestamp.now())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getGroupCategories(groupId: String): Result<List<CategoryDto>> =
        try {
            val snapshot =
                firestore
                    .collection("ExpenseLists")
                    .document(groupId)
                    .collection("categories")
                    .get()
            val categories =
                snapshot.documents.map { document ->
                    document.data<CategoryDto>().copy(documentId = document.id)
                }

            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun setGroupCategories(
        listId: String,
        categories: List<CategoryDto>,
    ): Result<Unit> =
        try {
            for (category in categories) {
                firestore
                    .collection("ExpenseLists")
                    .document(listId)
                    .collection("categories")
                    .document(category.documentId)
                    .set(category)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun deleteGroupCategory(
        listId: String,
        categoryId: String,
    ): Result<Unit> =
        try {
            firestore
                .collection("ExpenseLists")
                .document(listId)
                .collection("categories")
                .document(categoryId)
                .delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun createCategory(
        groupId: String,
        category: CategoryDto
    ): Result<String> =
        try {
            val docRef = firestore
                .collection("ExpenseLists")
                .document(groupId)
                .collection("categories")
                .add(category)
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun updateCategory(
        groupId: String,
        category: CategoryDto
    ): Result<Unit> =
        try {
            firestore
                .collection("ExpenseLists")
                .document(groupId)
                .collection("categories")
                .document(category.documentId)
                .set(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun deleteCategory(
        groupId: String,
        categoryId: String
    ): Result<Unit> =
        try {
            firestore
                .collection("ExpenseLists")
                .document(groupId)
                .collection("categories")
                .document(categoryId)
                .delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
