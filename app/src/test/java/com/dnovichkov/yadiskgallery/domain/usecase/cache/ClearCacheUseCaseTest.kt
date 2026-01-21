package com.dnovichkov.yadiskgallery.domain.usecase.cache

import com.dnovichkov.yadiskgallery.domain.repository.CacheStats
import com.dnovichkov.yadiskgallery.domain.repository.ICacheRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ClearCacheUseCase")
class ClearCacheUseCaseTest {

    private lateinit var useCase: ClearCacheUseCase
    private lateinit var repository: FakeCacheRepository

    @BeforeEach
    fun setUp() {
        repository = FakeCacheRepository()
        useCase = ClearCacheUseCase(repository)
    }

    @Nested
    @DisplayName("invoke()")
    inner class InvokeTests {

        @Test
        @DisplayName("should clear all cache")
        fun `should clear all cache`() = runTest {
            val result = useCase()

            assertTrue(result.isSuccess)
            assertTrue(repository.clearCacheCalled)
        }

        @Test
        @DisplayName("should propagate errors")
        fun `should propagate errors`() = runTest {
            repository.clearCacheError = RuntimeException("Clear failed")

            val result = useCase()

            assertTrue(result.isFailure)
        }
    }

    @Nested
    @DisplayName("clearOldCache()")
    inner class ClearOldCacheTests {

        @Test
        @DisplayName("should clear old cache entries")
        fun `should clear old cache entries`() = runTest {
            repository.clearOldCacheResult = Result.success(5)

            val result = useCase.clearOldCache(maxAgeMillis = 3600000L)

            assertTrue(result.isSuccess)
            assertEquals(5, result.getOrNull())
            assertEquals(3600000L, repository.lastMaxAge)
        }
    }

    @Nested
    @DisplayName("removeFromCache()")
    inner class RemoveFromCacheTests {

        @Test
        @DisplayName("should remove specific file from cache")
        fun `should remove specific file from cache`() = runTest {
            val result = useCase.removeFromCache(path = "/photos/file.jpg")

            assertTrue(result.isSuccess)
            assertEquals("/photos/file.jpg", repository.lastRemovedPath)
        }
    }

    private class FakeCacheRepository : ICacheRepository {
        var clearCacheCalled = false
        var clearCacheError: Throwable? = null
        var clearOldCacheResult: Result<Int> = Result.success(0)
        var lastMaxAge: Long = 0
        var lastRemovedPath: String? = null

        override fun observeCacheSize(): Flow<Long> = flowOf(0L)
        override suspend fun getCacheSize(): Long = 0L
        override suspend fun getMaxCacheSize(): Long = 100_000_000L
        override suspend fun setMaxCacheSize(sizeBytes: Long): Result<Unit> = Result.success(Unit)

        override suspend fun clearCache(): Result<Unit> {
            clearCacheCalled = true
            return clearCacheError?.let { Result.failure(it) } ?: Result.success(Unit)
        }

        override suspend fun clearOldCache(maxAgeMillis: Long): Result<Int> {
            lastMaxAge = maxAgeMillis
            return clearOldCacheResult
        }

        override suspend fun isCached(path: String): Boolean = false
        override suspend fun getCachedPath(path: String): String? = null
        override suspend fun cacheFile(path: String): Result<Unit> = Result.success(Unit)

        override suspend fun removeFromCache(path: String): Result<Unit> {
            lastRemovedPath = path
            return Result.success(Unit)
        }

        override suspend fun getCacheStats(): CacheStats = CacheStats(0, 0, 0, 0, 100_000_000L)
    }
}
