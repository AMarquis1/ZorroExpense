package com.marquis.zorroexpense.data.remote

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.storage

actual class StorageService actual constructor(context: Any?) {
    private val storage = Firebase.storage

    actual suspend fun uploadProfileImage(userId: String, imageBytes: ByteArray): Result<String> =
        try {
            val fileName = "profile_images/$userId.jpg"
            val reference = storage.reference.child(fileName)

            // Upload the bytes directly to Firebase Storage
            reference.putData(imageBytes)

            // Get the download URL
            val downloadUrl = reference.getDownloadUrl()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
}