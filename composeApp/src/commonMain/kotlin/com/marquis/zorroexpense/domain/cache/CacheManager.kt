package com.marquis.zorroexpense.domain.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlin.time.Duration

/**
 * Generic cache manager for domain entities
 * Provides thread-safe caching with configurable strategies
 */
interface CacheManager<K, V> {
    suspend fun get(key: K): V?
    suspend fun put(key: K, value: V)
    suspend fun invalidate(key: K)
    suspend fun clear()
    suspend fun isValid(key: K): Boolean
}

/**
 * In-memory implementation of CacheManager
 */
class InMemoryCacheManager<K, V>(
    private val strategy: CacheStrategy
) : CacheManager<K, V> {
    
    private data class CacheEntry<V>(
        val value: V,
        val timestamp: Long
    )
    
    private val cache = mutableMapOf<K, CacheEntry<V>>()
    private val mutex = Mutex()
    
    override suspend fun get(key: K): V? = mutex.withLock {
        val entry = cache[key] ?: return@withLock null
        
        if (isEntryValid(entry)) {
            entry.value
        } else {
            cache.remove(key)
            null
        }
    }
    
    override suspend fun put(key: K, value: V) = mutex.withLock {
        // Implement simple LRU if cache is full
        if (cache.size >= strategy.maxSize && !cache.containsKey(key)) {
            val oldestKey = cache.entries.minByOrNull { it.value.timestamp }?.key
            oldestKey?.let { cache.remove(it) }
        }
        
        cache[key] = CacheEntry(
            value = value,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
    }
    
    override suspend fun invalidate(key: K): Unit = mutex.withLock {
        cache.remove(key)
        Unit
    }
    
    override suspend fun clear() = mutex.withLock {
        cache.clear()
    }
    
    override suspend fun isValid(key: K): Boolean = mutex.withLock {
        val entry = cache[key] ?: return@withLock false
        isEntryValid(entry)
    }
    
    private fun isEntryValid(entry: CacheEntry<V>): Boolean {
        if (strategy.ttl == Duration.ZERO) return false
        
        val currentTime = Clock.System.now().toEpochMilliseconds()
        return (currentTime - entry.timestamp) < strategy.ttl.inWholeMilliseconds
    }
}