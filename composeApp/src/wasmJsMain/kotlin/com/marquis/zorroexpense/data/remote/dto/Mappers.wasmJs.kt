package com.marquis.zorroexpense.data.remote.dto

// WASM-specific timestamp conversion (simplified for now)
actual fun Any?.toDateString(): String {
    return when (this) {
        is String -> this // For WASM, timestamps might come as strings
        else -> ""
    }
}

// Platform-specific helper functions for WASM
actual fun Any?.getReferencePath(): String? {
    return this as? String // For WASM, references are just strings (IDs)
}

actual fun List<Any>.getReferencePaths(): List<String> {
    return this.mapNotNull { it as? String }
}