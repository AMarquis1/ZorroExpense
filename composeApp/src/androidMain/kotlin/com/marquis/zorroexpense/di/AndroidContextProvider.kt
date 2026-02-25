package com.marquis.zorroexpense.di

import com.marquis.zorroexpense.MainActivity

/**
 * Provide Android context for dependency injection.
 * This is used by services that require Android-specific APIs.
 */
internal actual fun getAndroidContext(): Any? =
    try {
        MainActivity.appContext
    } catch (e: Exception) {
        null
    }
