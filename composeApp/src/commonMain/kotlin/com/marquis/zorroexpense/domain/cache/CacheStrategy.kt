package com.marquis.zorroexpense.domain.cache

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Cache strategy configuration for domain entities
 */
data class CacheStrategy(
    val ttl: Duration = 5.minutes,
    val enableOfflineAccess: Boolean = true,
    val maxSize: Int = 1000,
) {
    companion object {
        val DEFAULT = CacheStrategy()
        val NO_CACHE = CacheStrategy(ttl = Duration.ZERO, enableOfflineAccess = false)
        val LONG_CACHE = CacheStrategy(ttl = 30.minutes)
    }
}
