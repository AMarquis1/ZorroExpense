@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.marquis.zorroexpense.data.remote

/**
 * WASM/Web implementation of CrashlyticService.
 * Crashlytics is not natively supported on WASM, so this provides a no-op implementation.
 * For web error tracking, consider using alternative services or custom error reporting.
 */
actual class CrashlyticService actual constructor() {
    actual fun logMessage(message: String) {
        // Web implementation: log to console in development
        console.log("Crashlytics message: $message")
    }

    actual fun logException(throwable: Throwable) {
        // Web implementation: log to console in development
        console.error("Crashlytics exception: ${throwable.message}")
    }

    actual fun setCustomKey(key: String, value: String) {
        // Web implementation: could be extended to send to custom analytics
    }

    actual fun setUserId(userId: String) {
        // Web implementation: could be extended to track user sessions
    }

    actual fun setCrashCollectionEnabled(enabled: Boolean) {
        // Web implementation: no-op
    }
}
