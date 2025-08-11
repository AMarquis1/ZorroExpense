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

actual fun List<Any>.getReferencePaths(): List<String> = this.mapNotNull { (it as? DocumentReference)?.path }
