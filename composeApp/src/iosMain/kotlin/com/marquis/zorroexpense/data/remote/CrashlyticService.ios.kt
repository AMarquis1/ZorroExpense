@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.marquis.zorroexpense.data.remote

/**
 * iOS implementation of CrashlyticService using GitLive Firebase Crashlytics.
 * Note: GitLive Firebase Crashlytics support on iOS requires manual integration.
 */
actual class CrashlyticService actual constructor() {
    actual fun logMessage(message: String) {
        // iOS implementation would use GitLive Firebase if available
        // For now, this is a placeholder that can be enhanced when needed
        println("Crashlytics message: $message")
    }

    actual fun logException(throwable: Throwable) {
        // iOS implementation would use GitLive Firebase if available
        println("Crashlytics exception: ${throwable.message}")
    }

    actual fun setCustomKey(key: String, value: String) {
        // iOS implementation would use GitLive Firebase if available
        println("Crashlytics custom key: $key = $value")
    }

    actual fun setUserId(userId: String) {
        // iOS implementation would use GitLive Firebase if available
        println("Crashlytics user ID: $userId")
    }

    actual fun setCrashCollectionEnabled(enabled: Boolean) {
        // iOS implementation would use GitLive Firebase if available
        println("Crashlytics collection enabled: $enabled")
    }
}
