package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.Expense
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.datetime.Instant

/**
 * Android-specific implementation to convert Expense domain model to AndroidExpenseDto
 */
actual fun Expense.toDto(): ExpenseDto {
    val firestore = Firebase.firestore
    
    return AndroidExpenseDto(
        documentId = this.documentId,
        name = this.name,
        description = this.description,
        price = this.price,
        date = try {
            // Parse the date string to create Firestore Timestamp
            if (this.date.isNotBlank()) {
                val instant = Instant.parse(this.date)
                Timestamp(instant.epochSeconds, instant.nanosecondsOfSecond.toInt())
            } else {
                // Use current timestamp if date is empty
                Timestamp.now()
            }
        } catch (e: Exception) {
            // Fallback to current timestamp if parsing fails
            Timestamp.now()
        },
        categoryRef = if (this.category.documentId.isNotBlank()) {
            // Create reference to category document using documentId
            firestore.collection("Categories").document(this.category.documentId)
        } else null,
        paidByRef = if (this.paidBy.userId.isNotBlank()) {
            // Create reference to user document
            firestore.collection("Users").document(this.paidBy.userId)
        } else null,
        splitWithRefs = this.splitWith.mapNotNull { user ->
            if (user.userId.isNotBlank()) {
                firestore.collection("Users").document(user.userId)
            } else null
        }
    )
}