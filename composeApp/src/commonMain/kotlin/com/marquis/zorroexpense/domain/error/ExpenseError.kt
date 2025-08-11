package com.marquis.zorroexpense.domain.error

/**
 * Domain-specific error types for expense operations
 * Provides clean separation between domain errors and infrastructure exceptions
 */
sealed class ExpenseError : Exception() {
    /**
     * Network-related errors
     */
    data class NetworkError(
        override val message: String,
        override val cause: Throwable? = null,
    ) : ExpenseError()

    /**
     * Cache-related errors
     */
    data class CacheError(
        override val message: String,
        override val cause: Throwable? = null,
    ) : ExpenseError()

    /**
     * Data validation errors
     */
    data class ValidationError(
        override val message: String,
        val field: String? = null,
    ) : ExpenseError()

    /**
     * Resource not found errors
     */
    data class NotFoundError(
        val resourceType: String,
        val resourceId: String,
    ) : ExpenseError() {
        override val message: String = "$resourceType with id '$resourceId' not found"
    }

    /**
     * Authentication/Authorization errors
     */
    data class AuthError(
        override val message: String,
    ) : ExpenseError()

    /**
     * Unknown or unexpected errors
     */
    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null,
    ) : ExpenseError()
}

/**
 * Extension function to convert generic exceptions to domain-specific errors
 * Compatible with Kotlin Multiplatform common code
 */
fun Throwable.toExpenseError(): ExpenseError =
    when (this) {
        is ExpenseError -> this
        is IllegalArgumentException -> ExpenseError.ValidationError(message ?: "Invalid input")
        else -> {
            // Check exception message for common network error patterns
            val message = this.message ?: "Unknown error occurred"
            when {
                message.contains("timeout", ignoreCase = true) ->
                    ExpenseError.NetworkError("Request timeout", this)
                message.contains("connection", ignoreCase = true) ||
                    message.contains("network", ignoreCase = true) ->
                    ExpenseError.NetworkError("Network error occurred", this)
                message.contains("auth", ignoreCase = true) ||
                    message.contains("permission", ignoreCase = true) ->
                    ExpenseError.AuthError(message)
                else -> ExpenseError.UnknownError(message, this)
            }
        }
    }
