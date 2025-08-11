package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.CategoryDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import com.marquis.zorroexpense.data.remote.dto.UserDto
import com.marquis.zorroexpense.data.remote.dto.WasmExpenseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
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

actual class FirestoreService actual constructor() {
    private val httpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
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

    actual suspend fun getExpenses(): Result<List<ExpenseDto>> =
        try {
            val response =
                httpClient.get("$baseUrl/expense") {
                    headers {
                        append(HttpHeaders.Accept, "application/json")
                    }
                }

            if (response.status == HttpStatusCode.OK) {
                val firestoreResponse: FirestoreResponse = response.body()
                val expenses =
                    firestoreResponse.documents?.mapNotNull { document ->
                        try {
                            val expenseDto =
                                WasmExpenseDto(
                                    description = document.fields.description?.stringValue ?: "",
                                    name = document.fields.name?.stringValue ?: "",
                                    price = document.fields.price?.doubleValue ?: 0.0,
                                    date = document.fields.date?.timestampValue ?: "",
                                    categoryId =
                                        document.fields.category
                                            ?.mapValue
                                            ?.fields
                                            ?.get("name")
                                            ?.stringValue ?: "",
                                    paidById = document.fields.paidBy?.stringValue ?: "",
                                    splitWithIds =
                                        document.fields.splitWith
                                            ?.arrayValue
                                            ?.values
                                            ?.map { it.stringValue } ?: emptyList(),
                                    documentId = "", // Document ID not available in simplified WASM implementation
                                )
                            expenseDto
                        } catch (e: Exception) {
                            println("Error parsing document: ${e.message}")
                            null
                        }
                    } ?: emptyList()

                Result.success(expenses)
            } else {
                Result.failure(Exception("HTTP Error: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getUsers(): Result<List<UserDto>> {
        // TODO: Implement full WASM support for users
        return Result.success(emptyList())
    }

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

    actual suspend fun addExpense(expense: ExpenseDto): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun updateExpense(
        expenseId: String,
        expense: ExpenseDto,
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun deleteExpense(expenseId: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}
