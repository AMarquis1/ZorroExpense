package com.marquis.zorroexpense.data.repository

import com.marquis.zorroexpense.data.remote.dto.GroupDto
import com.marquis.zorroexpense.data.remote.dto.IosGroupDto
import com.marquis.zorroexpense.domain.model.Group
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

actual fun Group.toDto(): GroupDto {
    val firestore = Firebase.firestore
    val memberRefs =
        members.map { memberId ->
            firestore.collection("Users").document(memberId)
        }

    return IosGroupDto(
        groupId = listId,
        name = name,
        createdBy = createdBy,
        memberRefs = memberRefs,
        shareCode = shareCode,
        createdAt = createdAt,
        lastModified = lastModified,
    )
}
