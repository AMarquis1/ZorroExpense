package com.marquis.zorroexpense.data.remote

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual class StorageService actual constructor(context: Any?) {
    actual suspend fun uploadProfileImage(
        userId: String,
        imageBytes: ByteArray
    ): Result<String> {
        TODO("Not yet implemented")
    }
}

actual fun createTempFileFromByteArray(fileByteArray: ByteArray): File {
    TODO("Not yet implemented")
}