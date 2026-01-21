package com.dnovichkov.yadiskgallery.domain.usecase.cache

import com.dnovichkov.yadiskgallery.domain.repository.CacheStats
import com.dnovichkov.yadiskgallery.domain.repository.ICacheRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GetCacheSizeUseCase")
class GetCacheSizeUseCaseTest {

    private lateinit var useCase: GetCacheSizeUseCase
    private lateinit var repository: FakeCacheRepository

    @BeforeEach
    fun setUp() {
        repository = FakeCacheRepository()
        useCase = GetCacheSizeUseCase(repository)
    }

    @Nested
    @DisplayName("invoke()")
    inner class InvokeTests {

        @Test
        @DisplayName("should return cache size in bytes")
        fun `should return cache size in bytes`() = runTest {
            repository.cacheSize = 1024 * 1024 * 50L // 50 MB

            val result = useCase()

            assertEquals(1024 * 1024 * 50L, result)
        }

        @Test
        @DisplayName("should return zero for empty cache")
        fun `should return zero for empty cache`() = runTest {
            repository.cacheSize = 0L

            val result = useCase()

            assertEquals(0L, result)
        }
    }

    @Nested
    @DisplayName("observeCacheSize()")
    inner class ObserveCacheSizeTests {

        @Test
        @DisplayName("should observe cache size changes")
        fun `should observe cache size changes`() = runTest {
            repository.cacheSizeFlow = flowOf(1024L * 1024 * 100) // 100 MB

            val result = useCase.observeCacheSize().first()

            assertEquals(1024L * 1024 * 100, result)
        }
    }

    @Nested
    @DisplayName("getMaxCacheSize()")
    inner class GetMaxCacheSizeTests {

        @Test
        @DisplayName("should return max cache size")
        fun `should return max cache size`() = runTest {
            repository.maxCacheSize = 1024L * 1024 * 500 // 500 MB

            val result = useCase.getMaxCacheSize()

            assertEquals(1024L * 1024 * 500, result)
        }
    }

    @Nested
    @DisplayName("setMaxCacheSize()")
    inner class SetMaxCacheSizeTests {

        @Test
        @DisplayName("should set max cache size")
        fun `should set max cache size`() = runTest {
            val result = useCase.setMaxCacheSize(1024L * 1024 * 200)

            assertTrue(result.isSuccess)
            assertEquals(1024L * 1024 * 200, repository.savedMaxCacheSize)
        }
    }

    @Nested
    @DisplayName("getCacheStats()")
    inner class GetCacheStatsTests {

        @Test
        @DisplayName("should return cache statistics")
        fun `should return cache statistics`() = runTest {
            repository.stats = CacheStats(
                totalSizeBytes = 1024L * 1024 * 50,
                fileCount = 100,
                hitCount = 500,
                missCount = 50,
                maxSizeBytes = 1024L * 1024 * 500
            )

            val result = useCase.getCacheStats()

            assertEquals(100, result.fileCount)
            assertEquals(500L, result.hitCount)
            assertEquals(50L, result.missCount)
        }
    }

    private class FakeCacheRepository : ICacheRepository {
        var cacheSize: Long = 0L
        var cacheSizeFlow: Flow<Long> = flowOf(0L)
        var maxCacheSize: Long = 100_000_000L
        var savedMaxCacheSize: Long = 0L
        var stats: CacheStats = CacheStats(0, 0, 0, 0, 100_000_000L)

        override fun observeCacheSize(): Flow<Long> = cacheSizeFlow
        override suspend fun getCacheSize(): Long = cacheSize
        override suspend fun getMaxCacheSize(): Long = maxCacheSize

        override suspend fun setMaxCacheSize(sizeBytes: Long): Result<Unit> {
            savedMaxCacheSize = sizeBytes
            return Result.success(Unit)
        }

        override suspend fun clearCache(): Result<Unit> = Result.success(Unit)
        override suspend fun clearOldCache(maxAgeMillis: Long): Result<Int> = Result.success(0)
        override suspend fun isCached(path: String): Boolean = false
        override suspend fun getCachedPath(path: String): String? = null
        override suspend fun cacheFile(path: String): Result<Unit> = Result.success(Unit)
        override suspend fun removeFromCache(path: String): Result<Unit> = Result.success(Unit)
        override suspend fun getCacheStats(): CacheStats = stats
    }
}
