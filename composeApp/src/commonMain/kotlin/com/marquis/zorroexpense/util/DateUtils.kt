package com.marquis.zorroexpense.util

import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Format a timestamp in milliseconds to "d MMMM yyyy" format (e.g., "4 February 2026")
 */
fun formatTimestamp(timestampMillis: Long): String {
    return try {
        val date = Date(timestampMillis)
        val formatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        "Unknown"
    }
}
