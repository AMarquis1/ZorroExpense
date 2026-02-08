package com.marquis.zorroexpense.data.repository

import com.marquis.zorroexpense.data.remote.dto.AndroidExpenseListDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseListDto
import com.marquis.zorroexpense.domain.model.Group
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.datetime.Clock

actual fun Group.toDto(): ExpenseListDto {
    val firestore = Firebase.firestore
    val memberRefs =
        members.map { member ->
            firestore.collection("Users").document(member.userId)
        }

    val categoryRefs =
        categories.map { category ->
            firestore.collection("Categories").document(category.documentId)
        }

    val now = Clock.System.now()
    val lastModifiedTimestamp = Timestamp(now.epochSeconds, now.nanosecondsOfSecond)

    return AndroidExpenseListDto(
        listId = listId,
        name = name,
        createdBy = createdBy,
        memberRefs = memberRefs,
        shareCode = shareCode,
        createdAt = createdAt,
        lastModified = lastModifiedTimestamp,
        categoriesRef = categoryRefs,
    )
}
