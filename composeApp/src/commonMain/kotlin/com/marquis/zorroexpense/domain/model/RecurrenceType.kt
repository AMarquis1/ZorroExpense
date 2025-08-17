package com.marquis.zorroexpense.domain.model

enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    ;

    fun getDisplayName(): String =
        when (this) {
            NONE -> "No recurrence"
            DAILY -> "Daily"
            WEEKLY -> "Weekly"
            MONTHLY -> "Monthly"
            YEARLY -> "Yearly"
        }

    fun getPreviewText(dayOfMonth: Int? = null): String =
        when (this) {
            NONE -> "This is a one-time expense"
            DAILY -> "This expense will repeat every day"
            WEEKLY -> "This expense will repeat every week"
            MONTHLY -> {
                val day = dayOfMonth ?: 1
                val suffix =
                    when {
                        day in 11..13 -> "th"
                        day % 10 == 1 -> "st"
                        day % 10 == 2 -> "nd"
                        day % 10 == 3 -> "rd"
                        else -> "th"
                    }
                "This expense will repeat on the $day$suffix of every month"
            }
            YEARLY -> "This expense will repeat every year"
        }
}
