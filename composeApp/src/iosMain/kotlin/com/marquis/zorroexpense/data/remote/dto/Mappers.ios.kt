package com.marquis.zorroexpense.data.remote.dto

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// iOS-specific timestamp conversion
@OptIn(ExperimentalTime::class)
actual fun Any?.toDateString(): String =
    when (this) {
        is Timestamp -> {
            val instant = Instant.fromEpochMilliseconds(this.seconds * 1000 + this.nanoseconds / 1000000)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            "${dateTime.year}-${dateTime.month.number.toString().padStart(2, '0')}-${dateTime.day.toString().padStart(2, '0')}"
        }
        else -> ""
    }

// Platform-specific helper functions for iOS
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
        (item as? IosSplitDetailDto)?.let { splitDetail ->
            splitDetail.userRef?.path?.let { path ->
                Pair(path, splitDetail.amount)
            }
        }
    }
