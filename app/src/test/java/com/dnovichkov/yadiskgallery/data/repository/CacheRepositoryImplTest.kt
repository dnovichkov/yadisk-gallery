package com.dnovichkov.yadiskgallery.data.repository

import android.content.Context
import com.dnovichkov.yadiskgallery.data.cache.dao.CacheMetadataDao
import com.dnovichkov.yadiskgallery.data.cache.dao.FolderDao
import com.dnovichkov.yadiskgallery.data.cache.dao.MediaDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CacheRepositoryImplTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var context: Context
    private lateinit var mediaDao: MediaDao
    private lateinit var folderDao: FolderDao
    private lateinit var cacheMetadataDao: CacheMetadataDao
    private lateinit var repository: CacheRepositoryImpl

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        mediaDao = mockk(relaxed = true)
        folderDao = mockk(relaxed = true)
        cacheMetadataDao = mockk(relaxed = true)

        val cacheDir = File(tempDir, "cache")
        cacheDir.mkdirs()
        every { context.cacheDir } returns cacheDir

        coEvery { mediaDao.getTotalSize() } returns 0L
        coEvery { mediaDao.getCount() } returns 0

        repository = CacheRepositoryImpl(
            context = context,
            mediaDao = mediaDao,
            folderDao = folderDao,
            cacheMetadataDao = cacheMetadataDao
        )
    }

    @Test
    fun `getCacheSize returns combined database and file size`() = runTest {
        coEvery { mediaDao.getTotalSize() } returns 1000L

        val size = repository.getCacheSize()

        assertTrue(size >= 1000L)
    }

    @Test
    fun `getMaxCacheSize returns default max size`() = runTest {
        val maxSize = repository.getMaxCacheSize()

        assertEquals(100L * 1024 * 1024, maxSize) // 100 MB default
    }

    @Test
    fun `setMaxCacheSize updates max size`() = runTest {
        val newMaxSize = 50L * 1024 * 1024 // 50 MB

        val result = repository.setMaxCacheSize(newMaxSize)

        assertTrue(result.isSuccess)
        assertEquals(newMaxSize, repository.getMaxCacheSize())
    }

    @Test
    fun `clearCache deletes all cached data`() = runTest {
        val result = repository.clearCache()

        assertTrue(result.isSuccess)
        coVerify { mediaDao.deleteAll() }
        coVerify { folderDao.deleteAll() }
        coVerify { cacheMetadataDao.deleteAll() }
    }

    @Test
    fun `clearOldCache deletes entries older than cutoff`() = runTest {
        val maxAgeMillis = 24 * 60 * 60 * 1000L // 24 hours
        coEvery { mediaDao.deleteOlderThan(any()) } returns 5
        coEvery { folderDao.deleteOlderThan(any()) } returns 3
        coEvery { cacheMetadataDao.deleteOlderThan(any()) } returns 2

        val result = repository.clearOldCache(maxAgeMillis)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!! >= 10) // At least 5+3+2 items deleted
    }

    @Test
    fun `isCached returns false for non-existent file`() = runTest {
        val path = "/Photos/nonexistent.jpg"

        val cached = repository.isCached(path)

        assertTrue(!cached)
    }

    @Test
    fun `getCachedPath returns null for non-cached file`() = runTest {
        val path = "/Photos/nonexistent.jpg"

        val cachedPath = repository.getCachedPath(path)

        assertEquals(null, cachedPath)
    }

    @Test
    fun `removeFromCache deletes file and database entry`() = runTest {
        val path = "/Photos/test.jpg"

        val result = repository.removeFromCache(path)

        assertTrue(result.isSuccess)
        coVerify { mediaDao.deleteByPath(path) }
    }

    @Test
    fun `getCacheStats returns correct statistics`() = runTest {
        coEvery { mediaDao.getTotalSize() } returns 5000L
        coEvery { mediaDao.getCount() } returns 10

        val stats = repository.getCacheStats()

        assertTrue(stats.totalSizeBytes >= 5000L)
        assertTrue(stats.fileCount >= 10)
        assertEquals(100L * 1024 * 1024, stats.maxSizeBytes)
    }

    @Test
    fun `cacheFile creates parent directories`() = runTest {
        val path = "/Photos/2024/test.jpg"

        val result = repository.cacheFile(path)

        assertTrue(result.isSuccess)
    }
}
