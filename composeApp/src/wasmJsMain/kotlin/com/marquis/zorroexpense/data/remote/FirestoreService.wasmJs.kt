package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.CategoryDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseListDto
import com.marquis.zorroexpense.data.remote.dto.UserDto
import com.marquis.zorroexpense.domain.model.UserProfile
import io.ktor.client.HttpClient
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class FirestoreDocument(
    val fields: FirestoreFields,
)

@Serializable
private data class FirestoreFields(
    val description: FirestoreStringValue? = null,
    val name: FirestoreStringValue? = null,
    val price: FirestoreDoubleValue? = null,
    val date: FirestoreTimestampValue? = null,
    val category: FirestoreCategoryValue? = null,
    val paidBy: FirestoreStringValue? = null,
    val splitWith: FirestoreArrayValue? = null,
)

@Serializable
private data class FirestoreStringValue(
    val stringValue: String,
)

@Serializable
private data class FirestoreDoubleValue(
    val doubleValue: Double,
)

@Serializable
private data class FirestoreTimestampValue(
    val timestampValue: String,
)

@Serializable
private data class FirestoreCategoryValue(
    val mapValue: FirestoreCategoryFields,
)

@Serializable
private data class FirestoreCategoryFields(
    val fields: Map<String, FirestoreStringValue>,
)

@Serializable
private data class FirestoreArrayValue(
    val arrayValue: FirestoreArrayFields,
)

@Serializable
private data class FirestoreArrayFields(
    val values: List<FirestoreStringValue>,
)

@Serializable
private data class FirestoreResponse(
    val documents: List<FirestoreDocument>? = null,
)

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FirestoreService actual constructor() {
    private val httpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true // Handle null values gracefully (Ktor 3.3.3+)
                        explicitNulls = false // Don't serialize null values
                    },
                )
            }

            install(HttpCache) {
                // Configure HTTP caching for WASM
                // Cache responses for 5 minutes to survive navigation
            }

            install(Logging) {
                level = LogLevel.INFO
            }
        }

    // Replace with your Firebase project ID
    private val projectId = "ZorroExpense" // Update this with actual project ID
    private val baseUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"

//    actual suspend fun getExpenses(): Result<List<ExpenseDto>> =
//        try {
//            val response =
//                httpClient.get("$baseUrl/expense") {
//                    headers {
//                        append(HttpHeaders.Accept, "application/json")
//                    }
//                }
//
//            if (response.status == HttpStatusCode.OK) {
//                val firestoreResponse: FirestoreResponse = response.body()
//                val expenses =
//                    firestoreResponse.documents?.mapNotNull { document ->
//                        try {
//                            val expenseDto =
//                                WasmExpenseDto(
//                                    description = document.fields.description?.stringValue ?: "",
//                                    name = document.fields.name?.stringValue ?: "",
//                                    price = document.fields.price?.doubleValue ?: 0.0,
//                                    date = document.fields.date?.timestampValue ?: "",
//                                    categoryId =
//                                        document.fields.category
//                                            ?.mapValue
//                                            ?.fields
//                                            ?.get("name")
//                                            ?.stringValue ?: "",
//                                    paidById = document.fields.paidBy?.stringValue ?: "",
//                                    splitWithIds =
//                                        document.fields.splitWith
//                                            ?.arrayValue
//                                            ?.values
//                                            ?.map { it.stringValue } ?: emptyList(),
//                                    documentId = "", // Document ID not available in simplified WASM implementation
//                                )
//                            expenseDto
//                        } catch (e: Exception) {
//                            println("Error parsing document: ${e.message}")
//                            null
//                        }
//                    } ?: emptyList()
//
//                Result.success(expenses)
//            } else {
//                Result.failure(Exception("HTTP Error: ${response.status}"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//
//    actual suspend fun getUsers(): Result<List<UserDto>> {
//        // TODO: Implement full WASM support for users
//        return Result.success(emptyList())
//    }

    actual suspend fun getCategories(): Result<List<CategoryDto>> {
        // TODO: Implement full WASM support for categories
        return Result.success(emptyList())
    }

    actual suspend fun getUserById(userId: String): Result<UserDto?> {
        // TODO: Implement full WASM support for user by ID
        return Result.success(null)
    }

    actual suspend fun getCategoryById(categoryId: String): Result<CategoryDto?> {
        // TODO: Implement full WASM support for category by ID
        return Result.success(null)
    }

    actual suspend fun createUserProfile(
        userId: String,
        profile: UserProfile,
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun getUserExpenseLists(userId: String): Result<List<ExpenseListDto>> {
        TODO("Not yet implemented")
    }

    actual suspend fun getExpenseListById(listId: String): Result<ExpenseListDto?> {
        TODO("Not yet implemented")
    }

    actual suspend fun createExpenseList(list: ExpenseListDto): Result<String> {
        TODO("Not yet implemented")
    }

    actual suspend fun updateExpenseList(
        listId: String,
        list: ExpenseListDto,
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun deleteExpenseList(listId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun getExpenseListByShareCode(shareCode: String): Result<ExpenseListDto?> {
        TODO("Not yet implemented")
    }

    actual suspend fun addUserToExpenseListMembers(
        listId: String,
        userId: String,
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun removeUserFromExpenseListMembers(
        listId: String,
        userId: String,
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun addExpenseListReferenceForUser(
        userId: String,
        listId: String,
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun removeExpenseListReferenceForUser(
        userId: String,
        listId: String,
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun getExpensesByListId(listId: String): Result<List<ExpenseDto>> {
        TODO("Not yet implemented")
    }

    actual suspend fun addExpenseToList(
        listId: String,
        expense: ExpenseDto,
    ): Result<String> {
        TODO("Not yet implemented")
    }

    actual suspend fun updateExpenseInList(
        listId: String,
        expenseId: String,
        expense: ExpenseDto,
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun deleteExpenseFromList(
        listId: String,
        expenseId: String,
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun updateExpenseListLastModified(listId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun getGroupCategories(listId: String): Result<List<CategoryDto>> =
        Result.success(emptyList())

    actual suspend fun setGroupCategories(
        listId: String,
        categories: List<CategoryDto>,
    ): Result<Unit> =
        Result.success(Unit)

    actual suspend fun deleteGroupCategory(
        listId: String,
        categoryId: String,
    ): Result<Unit> =
        Result.success(Unit)
}
