package com.dnovichkov.yadiskgallery.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for cache management.
 * Handles local caching of media files and metadata.
 */
interface ICacheRepository {

    /**
     * Observes the current cache size in bytes.
     *
     * @return Flow of cache size in bytes
     */
    fun observeCacheSize(): Flow<Long>

    /**
     * Gets the current cache size in bytes.
     *
     * @return Cache size in bytes
     */
    suspend fun getCacheSize(): Long

    /**
     * Gets the maximum allowed cache size in bytes.
     *
     * @return Maximum cache size in bytes
     */
    suspend fun getMaxCacheSize(): Long

    /**
     * Sets the maximum allowed cache size.
     *
     * @param sizeBytes Maximum size in bytes
     * @return Result indicating success or error
     */
    suspend fun setMaxCacheSize(sizeBytes: Long): Result<Unit>

    /**
     * Clears all cached data.
     * Removes cached media files and metadata.
     *
     * @return Result indicating success or error
     */
    suspend fun clearCache(): Result<Unit>

    /**
     * Clears cached data older than the specified time.
     *
     * @param maxAgeMillis Maximum age in milliseconds
     * @return Result with number of items cleared or error
     */
    suspend fun clearOldCache(maxAgeMillis: Long): Result<Int>

    /**
     * Checks if a file is cached.
     *
     * @param path File path on Yandex.Disk
     * @return true if file is cached, false otherwise
     */
    suspend fun isCached(path: String): Boolean

    /**
     * Gets the local cache path for a file.
     *
     * @param path File path on Yandex.Disk
     * @return Local file path if cached, null otherwise
     */
    suspend fun getCachedPath(path: String): String?

    /**
     * Marks a file for caching.
     * The file will be downloaded in the background.
     *
     * @param path File path on Yandex.Disk
     * @return Result indicating success or error
     */
    suspend fun cacheFile(path: String): Result<Unit>

    /**
     * Removes a specific file from cache.
     *
     * @param path File path on Yandex.Disk
     * @return Result indicating success or error
     */
    suspend fun removeFromCache(path: String): Result<Unit>

    /**
     * Gets cache statistics.
     *
     * @return Cache statistics
     */
    suspend fun getCacheStats(): CacheStats
}

/**
 * Statistics about the cache.
 *
 * @property totalSizeBytes Total size of cached files in bytes
 * @property fileCount Number of cached files
 * @property hitCount Number of cache hits
 * @property missCount Number of cache misses
 * @property maxSizeBytes Maximum allowed cache size in bytes
 */
data class CacheStats(
    val totalSizeBytes: Long,
    val fileCount: Int,
    val hitCount: Long,
    val missCount: Long,
    val maxSizeBytes: Long
) {
    /**
     * Cache hit rate as a percentage (0-100).
     */
    val hitRate: Float
        get() {
            val total = hitCount + missCount
            return if (total > 0) (hitCount.toFloat() / total) * 100 else 0f
        }

    /**
     * Cache usage as a percentage (0-100).
     */
    val usagePercent: Float
        get() = if (maxSizeBytes > 0) (totalSizeBytes.toFloat() / maxSizeBytes) * 100 else 0f
}
