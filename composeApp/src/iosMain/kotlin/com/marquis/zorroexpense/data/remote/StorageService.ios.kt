package com.marquis.zorroexpense.data.remote

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.storage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLFileScheme

actual class StorageService actual constructor(context: Any?) {
    private val storage = Firebase.storage("gs://zorro-expense.firebasestorage.app")

    actual suspend fun uploadProfileImage(userId: String, imageBytes: ByteArray): Result<String> =
        try {
            val fileName = "profile_images/$userId.jpg"
            val reference = storage.reference.child(fileName)

            // Upload the bytes directly to Firebase Storage
            reference.putBytes(imageBytes)

            // Get the download URL
            val downloadUrl = reference.getDownloadUrl()

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

            Result.success(httpsUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun uploadGroupImage(groupId: String, imageBytes: ByteArray): Result<String> =
        try {
            val fileName = "group_images/$groupId.jpg"
            val reference = storage.reference.child(fileName)

            // Upload the bytes directly to Firebase Storage
            reference.putBytes(imageBytes)

            // Get the download URL
            val downloadUrl = reference.getDownloadUrl()

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

            Result.success(httpsUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun readImageBytesFromUri(uri: String): Result<ByteArray> =
        try {
            val fileUrl = NSURL(string = uri)
                ?: return Result.failure(Exception("Invalid file URI: $uri"))

            val fileManager = NSFileManager.defaultManager
            val path = fileUrl.path ?: return Result.failure(Exception("Cannot get file path from URI"))

            val bytes = fileManager.contentsAtPath(path)
                ?: return Result.failure(Exception("Cannot read file at path: $path"))

            Result.success(bytes.readBytes(bytes.length.toInt()))
        } catch (e: Exception) {
            Result.failure(e)
        }
}