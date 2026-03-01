@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.marquis.zorroexpense.data.remote

/**
 * Service for crash reporting and error tracking via Firebase Crashlytics.
 * Provides expect/actual implementations for different platforms.
 */
expect class CrashlyticService() {
    /**
     * Log a custom message to Crashlytics
     * @param message The message to log
     */
    fun logMessage(message: String)

    /**
     * Log an exception that is not fatal
     * @param throwable The throwable to log
     */
    fun logException(throwable: Throwable)

    /**
     * Set a custom key-value pair for crash reports
     * @param key The key identifier
     * @param value The value to associate with the key
     */
    fun setCustomKey(key: String, value: String)

    /**
     * Set the user identifier for crash reports
     * @param userId The user ID
     */
    fun setUserId(userId: String)

    /**
     * Enable or disable crash reporting
     * @param enabled Whether crash reporting should be enabled
     */
    fun setCrashCollectionEnabled(enabled: Boolean)
}
