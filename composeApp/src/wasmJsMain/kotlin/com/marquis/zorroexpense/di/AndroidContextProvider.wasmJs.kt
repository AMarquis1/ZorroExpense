package com.marquis.zorroexpense.di

/**
 * Web (WASM) implementation - returns null since web doesn't use Android context.
 */
internal actual fun getAndroidContext(): Any? = null
