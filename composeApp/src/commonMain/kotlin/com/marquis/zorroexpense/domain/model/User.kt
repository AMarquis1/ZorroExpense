package com.marquis.zorroexpense.domain.model

data class User(
    val userId: String = "",
    val name: String = "",
    val profileImage: String = "", // Will store resource name like "sarah" or "alex"
)
