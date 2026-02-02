package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.Expense
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.ExperimentalTime

/**
 * iOS-specific implementation to convert Expense domain model to IosExpenseDto
 */
@OptIn(ExperimentalTime::class)
actual fun Expense.toDto(): ExpenseDto {
    val firestore = Firebase.firestore

    return IosExpenseDto(
        documentId = this.documentId,
        name = this.name,
        description = this.description,
        price = this.price,
        isFromRecurring = this.isFromRecurring,
        date =
            try {
                // Parse the date string (YYYY-MM-DD format) to create Firestore Timestamp
                if (this.date.isNotBlank()) {
                    val localDate = LocalDate.parse(this.date)
                    val instant = localDate.atStartOfDayIn(TimeZone.currentSystemDefault())
                    Timestamp(instant.epochSeconds, instant.nanosecondsOfSecond)
                } else {
                    // Use current timestamp if date is empty
                    Timestamp.now()
                }
            } catch (_: Exception) {
                // Fallback to current timestamp if parsing fails
                Timestamp.now()
            },
        categoryRef =
            if (this.category.documentId.isNotBlank()) {
                // Create reference to category document using documentId
                firestore.collection("Categories").document(this.category.documentId)
            } else {
                null
            },
        paidByRef =
            if (this.paidBy.userId.isNotBlank()) {
                // Create reference to user document
                firestore.collection("Users").document(this.paidBy.userId)
            } else {
                null
            },
        splitDetailsDto =
            this.splitDetails.mapNotNull { splitDetail ->
                if (splitDetail.user.userId.isNotBlank()) {
                    IosSplitDetailDto(
                        userRef = firestore.collection("Users").document(splitDetail.user.userId),
                        amount = splitDetail.amount,
                    )
                } else {
                    null
                }
            },
    )
}
