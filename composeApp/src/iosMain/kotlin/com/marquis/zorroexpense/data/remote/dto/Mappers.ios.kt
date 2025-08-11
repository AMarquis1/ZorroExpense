package com.marquis.zorroexpense.data.remote.dto

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// iOS-specific timestamp conversion
actual fun Any?.toDateString(): String =
    when (this) {
        is Timestamp -> {
            val instant = Instant.fromEpochMilliseconds(this.seconds * 1000 + this.nanoseconds / 1000000)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            "${dateTime.year}-${dateTime.monthNumber.toString().padStart(2, '0')}-${dateTime.dayOfMonth.toString().padStart(2, '0')}"
        }
        else -> ""
    }

// Platform-specific helper functions for iOS
actual fun Any?.getReferencePath(): String? = (this as? DocumentReference)?.path

actual fun List<Any>.getReferencePaths(): List<String> = this.mapNotNull { (it as? DocumentReference)?.path }
