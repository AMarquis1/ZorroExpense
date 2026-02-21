package com.marquis.zorroexpense.data.repository

import com.marquis.zorroexpense.data.remote.dto.GroupDto
import com.marquis.zorroexpense.data.remote.dto.WasmGroupDto
import com.marquis.zorroexpense.domain.model.Group

actual fun Group.toDto(): GroupDto =
    WasmGroupDto(
        groupId = listId,
        name = name,
        createdBy = createdBy,
        memberIds = members,
        shareCode = shareCode,
        createdAt = createdAt,
        lastModified = lastModified,
    )
