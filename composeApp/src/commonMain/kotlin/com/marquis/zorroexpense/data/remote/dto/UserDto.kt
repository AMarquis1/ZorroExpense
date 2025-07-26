package com.marquis.zorroexpense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("userId")
    val userId: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("profileImage")
    val profileImage: String = ""
)