package com.marquis.zorroexpense.domain.model

/**
 * Represents a user's profile stored in Firestore.
 * This is separate from AuthUser which comes from Firebase Auth.
 */
data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val profileImage: String? = null,
    val createdAt: String = ""
)
