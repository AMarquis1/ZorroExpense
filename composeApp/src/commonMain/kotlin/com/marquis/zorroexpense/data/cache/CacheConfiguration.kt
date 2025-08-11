package com.marquis.zorroexpense.data.cache

import com.marquis.zorroexpense.AppConfig
import com.marquis.zorroexpense.domain.cache.CacheStrategy
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Cache configuration for different environments and data types
 * Centralizes cache behavior configuration following KMP standards
 */
object CacheConfiguration {
    
    /**
     * Get cache strategy for expenses based on current configuration
     */
    fun expensesCacheStrategy(): CacheStrategy {
        return if (AppConfig.USE_MOCK_DATA) {
            // No cache for mock data to allow rapid development
            CacheStrategy.NO_CACHE
        } else {
            // 5-minute cache for production with offline access
            CacheStrategy(
                ttl = 5.minutes,
                enableOfflineAccess = true,
                maxSize = 1000
            )
        }
    }
    
    /**
     * Get cache strategy for categories
     */
    fun categoriesCacheStrategy(): CacheStrategy {
        return if (AppConfig.USE_MOCK_DATA) {
            CacheStrategy.NO_CACHE
        } else {
            // Categories change less frequently, cache for 30 minutes
            CacheStrategy(
                ttl = 1.hours,
                enableOfflineAccess = true,
                maxSize = 100
            )
        }
    }
    
    /**
     * Get cache strategy for user data
     */
    fun userCacheStrategy(): CacheStrategy {
        return if (AppConfig.USE_MOCK_DATA) {
            CacheStrategy.NO_CACHE
        } else {
            // User data is fairly static, cache for 1 hour
            CacheStrategy(
                ttl = 60.minutes,
                enableOfflineAccess = true,
                maxSize = 50
            )
        }
    }
}