package com.marquis.zorroexpense.data.remote.dto

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Android-specific timestamp conversion
actual fun Any?.toDateString(): String =
    when (this) {
        is Timestamp -> {
            val date = Date(this.seconds * 1000 + this.nanoseconds / 1000000)
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.format(date)
        }
        else -> ""
    }

// Platform-specific helper functions for Android
actual fun Any?.getReferencePath(): String? = (this as? DocumentReference)?.path

actual fun Any?.getListIdPath(): String =
    when (this) {
        is DocumentReference -> this.path
        is String -> this
        else -> ""
    }

actual fun List<Any>.getReferencePaths(): List<String> = this.mapNotNull { (it as? DocumentReference)?.path }

actual fun List<Any>.getSplitDetailData(): List<Pair<String, Double>> =
    this.mapNotNull { item ->
        (item as? SplitDetailDto)?.let { splitDetail ->
            splitDetail.userRef?.path?.let { path ->
                Pair(path, splitDetail.amount)
            }
        }
    }

actual fun List<Any>.getCategoryPaths(): List<String> = filterIsInstance<DocumentReference>().map { ref -> ref.path }
