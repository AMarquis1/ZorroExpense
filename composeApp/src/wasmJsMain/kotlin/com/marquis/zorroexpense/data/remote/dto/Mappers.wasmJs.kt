package com.marquis.zorroexpense.data.remote.dto

// WASM-specific timestamp conversion (simplified for now)
actual fun Any?.toDateString(): String =
    when (this) {
        is String -> this // For WASM, timestamps might come as strings
        else -> ""
    }

// Platform-specific helper functions for WASM
actual fun Any?.getReferencePath(): String? {
    return this as? String // For WASM, references are just strings (IDs)
}

actual fun List<Any>.getReferencePaths(): List<String> = this.mapNotNull { it as? String }

actual fun List<Any>.getSplitDetailData(): List<Pair<String, Double>> = this.mapNotNull { item ->
    (item as? WasmSplitDetailDto)?.let { splitDetail ->
        if (splitDetail.userId.isNotBlank()) {
            Pair("Users/${splitDetail.userId}", splitDetail.amount)
        } else {
            null
        }
    }
}

actual fun List<Any>.getCategoryPaths(): List<String> =
    filterIsInstance<String>()
