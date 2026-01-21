package com.dnovichkov.yadiskgallery.domain.usecase.cache

import com.dnovichkov.yadiskgallery.domain.repository.CacheStats
import com.dnovichkov.yadiskgallery.domain.repository.ICacheRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting cache size and statistics.
 */
class GetCacheSizeUseCase
    @Inject
    constructor(
        private val cacheRepository: ICacheRepository,
    ) {
        /**
         * Gets the current cache size in bytes.
         *
         * @return Cache size in bytes
         */
        suspend operator fun invoke(): Long {
            return cacheRepository.getCacheSize()
        }

        /**
         * Observes the cache size changes.
         *
         * @return Flow of cache size in bytes
         */
        fun observeCacheSize(): Flow<Long> {
            return cacheRepository.observeCacheSize()
        }

        /**
         * Gets the maximum allowed cache size.
         *
         * @return Maximum cache size in bytes
         */
        suspend fun getMaxCacheSize(): Long {
            return cacheRepository.getMaxCacheSize()
        }

        /**
         * Sets the maximum allowed cache size.
         *
         * @param sizeBytes Maximum size in bytes
         * @return Result indicating success or error
         */
        suspend fun setMaxCacheSize(sizeBytes: Long): Result<Unit> {
            return cacheRepository.setMaxCacheSize(sizeBytes)
        }

        /**
         * Gets detailed cache statistics.
         *
         * @return Cache statistics
         */
        suspend fun getCacheStats(): CacheStats {
            return cacheRepository.getCacheStats()
        }
    }
