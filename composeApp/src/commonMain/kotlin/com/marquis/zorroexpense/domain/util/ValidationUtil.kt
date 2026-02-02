package com.marquis.zorroexpense.domain.util

/**
 * Utility functions for input validation
 */
object ValidationUtil {
    /**
     * Simple email validation that works on all platforms
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isEmpty()) return false

        // Simple regex pattern that works cross-platform
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(Regex(emailPattern))
    }
}
