@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.marquis.zorroexpense.data.remote

import com.google.firebase.crashlytics.FirebaseCrashlytics

actual class CrashlyticService actual constructor() {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    actual fun logMessage(message: String) {
        crashlytics.log(message)
    }

    actual fun logException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    actual fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    actual fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    actual fun setCrashCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }
}
