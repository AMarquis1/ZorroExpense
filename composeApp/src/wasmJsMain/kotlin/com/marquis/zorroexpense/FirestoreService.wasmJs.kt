package com.marquis.zorroexpense

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class FirestoreDocument(
    val fields: FirestoreFields
)

@Serializable
private data class FirestoreFields(
    val description: FirestoreStringValue? = null,
    val name: FirestoreStringValue? = null,
    val price: FirestoreDoubleValue? = null,
    val date: FirestoreTimestampValue? = null
)

@Serializable
private data class FirestoreStringValue(
    val stringValue: String
)

@Serializable
private data class FirestoreDoubleValue(
    val doubleValue: Double
)

@Serializable
private data class FirestoreTimestampValue(
    val timestampValue: String
)

@Serializable
private data class FirestoreResponse(
    val documents: List<FirestoreDocument>? = null
)

actual class FirestoreService actual constructor() {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    // Replace with your Firebase project ID
    private val projectId = "ZorroExpense" // Update this with actual project ID
    private val baseUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"

    actual suspend fun getExpenses(): Result<List<Expense>> {
        return try {
            val response = httpClient.get("$baseUrl/Expense") {
                headers {
                    append(HttpHeaders.Accept, "application/json")
                }
            }
            
            if (response.status == HttpStatusCode.OK) {
                val firestoreResponse: FirestoreResponse = response.body()
                val expenses = firestoreResponse.documents?.mapNotNull { document ->
                    try {
                        Expense(
                            description = document.fields.description?.stringValue ?: "",
                            name = document.fields.name?.stringValue ?: "",
                            price = document.fields.price?.doubleValue ?: 0.0,
                            date = document.fields.date?.timestampValue ?: ""
                        )
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
    }
}