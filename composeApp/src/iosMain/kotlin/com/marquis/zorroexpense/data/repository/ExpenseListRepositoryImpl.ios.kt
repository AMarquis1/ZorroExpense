package com.marquis.zorroexpense.data.repository

import com.marquis.zorroexpense.data.remote.dto.ExpenseListDto
import com.marquis.zorroexpense.data.remote.dto.IosExpenseListDto
import com.marquis.zorroexpense.domain.model.Group
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

actual fun Group.toDto(): ExpenseListDto {
    val firestore = Firebase.firestore
    val memberRefs =
        members.map { memberId ->
            firestore.collection("Users").document(memberId)
        }

    val categoryRefs =
        categories.map { category ->
            firestore.collection("Categories").document(category.documentId)
        }

    return IosExpenseListDto(
        listId = listId,
        name = name,
        createdBy = createdBy,
        memberRefs = memberRefs,
        shareCode = shareCode,
        createdAt = createdAt,
        lastModified = lastModified,
        categoryRefs = categoryRefs,
    )
}
