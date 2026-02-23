package com.marquis.zorroexpense.data.remote

// Platform-specific constructor parameters are handled in actual implementations
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class StorageService(context: Any? = null) {
    /**
     * Upload a profile image for a user to Firebase Storage.
     *
     * @param userId The ID of the user
     * @param imageBytes The image data to upload (JPEG format)
     * @return A Result containing the Firebase Storage download URL on success, or an error on failure
     */
    suspend fun uploadProfileImage(userId: String, imageBytes: ByteArray): Result<String>
}
