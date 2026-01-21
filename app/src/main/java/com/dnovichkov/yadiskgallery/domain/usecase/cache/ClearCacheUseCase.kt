package com.dnovichkov.yadiskgallery.domain.usecase.cache

import com.dnovichkov.yadiskgallery.domain.repository.ICacheRepository
import javax.inject.Inject

/**
 * Use case for clearing the local cache.
 */
class ClearCacheUseCase @Inject constructor(
    private val cacheRepository: ICacheRepository
) {
    /**
     * Clears all cached data.
     *
     * @return Result indicating success or error
     */
    suspend operator fun invoke(): Result<Unit> {
        return cacheRepository.clearCache()
    }

    /**
     * Clears cached data older than the specified time.
     *
     * @param maxAgeMillis Maximum age in milliseconds
     * @return Result with number of items cleared or error
     */
    suspend fun clearOldCache(maxAgeMillis: Long): Result<Int> {
        return cacheRepository.clearOldCache(maxAgeMillis)
    }

    /**
     * Removes a specific file from cache.
     *
     * @param path File path on Yandex.Disk
     * @return Result indicating success or error
     */
    suspend fun removeFromCache(path: String): Result<Unit> {
        return cacheRepository.removeFromCache(path)
    }
}
