package com.marquis.zorroexpense.data.remote

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual class StorageService actual constructor(context: Any?) {
    actual suspend fun uploadProfileImage(
        userId: String,
        imageBytes: ByteArray
    ): Result<String> {
        TODO("Not yet implemented")
    }

    actual suspend fun uploadGroupImage(
        groupId: String,
        imageBytes: ByteArray
    ): Result<String> {
        TODO("Not yet implemented")
    }

    actual suspend fun readImageBytesFromUri(uri: String): Result<ByteArray> {
        TODO("Not yet implemented for wasmJs platform")
    }
}

actual fun createTempFileFromByteArray(fileByteArray: ByteArray): File {
    TODO("Not yet implemented")
}