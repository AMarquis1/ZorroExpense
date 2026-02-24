package com.marquis.zorroexpense.data.remote

import android.content.Context
import android.util.Log
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.storage
import androidx.core.net.toUri

actual class StorageService actual constructor(context: Any?) {
    private val storage = Firebase.storage("gs://zorro-expense.firebasestorage.app")
    private val androidContext = context as? Context
    private val tag = "StorageService"

    actual suspend fun uploadProfileImage(userId: String, imageBytes: ByteArray): Result<String> =
        try {
            val fileName = "profile_images/$userId.jpg"
            Log.d(tag, "Starting upload to: gs://zorro-expense.firebasestorage.app/$fileName (${imageBytes.size} bytes)")

            val reference = storage.reference.child(fileName)

            // Upload the bytes directly to Firebase Storage
            reference.putData(Data(imageBytes))

            // Get the download URL
            val downloadUrl = reference.getDownloadUrl()
            Log.d(tag, "Download URL obtained: $downloadUrl")

            // Extract token from the download URL if present
            val token = if (downloadUrl.contains("token=")) {
                downloadUrl.substringAfter("token=")
            } else {
                ""
            }

            // Ensure proper URL encoding with %2F for path separators
            val httpsUrl = if (token.isNotEmpty()) {
                "https://firebasestorage.googleapis.com/v0/b/zorro-expense.firebasestorage.app/o/${fileName.replace("/", "%2F")}?alt=media&token=$token"
            } else {
                // Fallback if token extraction fails (for gs:// URLs)
                "https://firebasestorage.googleapis.com/v0/b/zorro-expense.firebasestorage.app/o/${fileName.replace("/", "%2F")}?alt=media"
            }

            Log.d(tag, "Final URL to use: $httpsUrl")
            Result.success(httpsUrl)
        } catch (e: Exception) {
            Log.e(tag, "Upload failed: ${e.message}", e)
            e.printStackTrace()
            Result.failure(e)
        }

    actual suspend fun uploadGroupImage(groupId: String, imageBytes: ByteArray): Result<String> =
        try {
            val fileName = "group_images/$groupId.jpg"
            Log.d(tag, "Starting group image upload to: gs://zorro-expense.firebasestorage.app/$fileName (${imageBytes.size} bytes)")

            val reference = storage.reference.child(fileName)

            // Upload the bytes directly to Firebase Storage
            reference.putData(Data(imageBytes))

            // Get the download URL
            val downloadUrl = reference.getDownloadUrl()
            Log.d(tag, "Download URL obtained: $downloadUrl")

            // Extract token from the download URL if present
            val token = if (downloadUrl.contains("token=")) {
                downloadUrl.substringAfter("token=")
            } else {
                ""
            }

            // Ensure proper URL encoding with %2F for path separators
            val httpsUrl = if (token.isNotEmpty()) {
                "https://firebasestorage.googleapis.com/v0/b/zorro-expense.firebasestorage.app/o/${fileName.replace("/", "%2F")}?alt=media&token=$token"
            } else {
                // Fallback if token extraction fails (for gs:// URLs)
                "https://firebasestorage.googleapis.com/v0/b/zorro-expense.firebasestorage.app/o/${fileName.replace("/", "%2F")}?alt=media"
            }

            Log.d(tag, "Final URL to use: $httpsUrl")
            Result.success(httpsUrl)
        } catch (e: Exception) {
            Log.e(tag, "Group image upload failed: ${e.message}", e)
            e.printStackTrace()
            Result.failure(e)
        }

    actual suspend fun readImageBytesFromUri(uri: String): Result<ByteArray> {
        return try {
            if (androidContext == null) {
                return Result.failure(Exception("Android context not available"))
            }

            // Try file path first (for cropped images from gallery picker)
            if (uri.startsWith("/")) {
                try {
                    Log.d(tag, "Attempting to read file from path: $uri")
                    val file = java.io.File(uri)
                    if (file.exists()) {
                        val bytes = file.readBytes()
                        Log.d(tag, "Successfully read ${bytes.size} bytes from file path")
                        return Result.success(bytes)
                    }
                } catch (e: Exception) {
                    Log.d(tag, "File path read failed, trying fallback: ${e.message}")
                }
            }

            // Fallback: try content resolver for content URIs
            val contentUri = uri.toUri()
            val inputStream = androidContext.contentResolver.openInputStream(contentUri)
                ?: return Result.failure(Exception("Unable to open input stream for URI: $uri"))

            val bytes = inputStream.use { it.readBytes() }
            Log.d(tag, "Successfully read ${bytes.size} bytes from content URI")
            Result.success(bytes)
        } catch (e: Exception) {
            Log.e(tag, "Failed to read image bytes: ${e.message}", e)
            Result.failure(e)
        }
    }
}