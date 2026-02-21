package com.marquis.zorroexpense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val documentId: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("icon")
    val icon: String = "",
    @SerialName("color")
    val color: String = "",
    @SerialName("active")
    val active: Boolean = true,
)
