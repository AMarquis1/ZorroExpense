package com.marquis.zorroexpense.domain.model

data class Category(
    val documentId: String = "",
    val name: String = "",
    val icon: String = "",
    val color: String = "",
    val active: Boolean = true,
)
