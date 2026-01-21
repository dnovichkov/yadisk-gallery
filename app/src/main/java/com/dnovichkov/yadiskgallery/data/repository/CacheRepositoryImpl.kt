package com.dnovichkov.yadiskgallery.data.repository

import android.content.Context
import com.dnovichkov.yadiskgallery.data.cache.dao.CacheMetadataDao
import com.dnovichkov.yadiskgallery.data.cache.dao.FolderDao
import com.dnovichkov.yadiskgallery.data.cache.dao.MediaDao
import com.dnovichkov.yadiskgallery.domain.repository.CacheStats
import com.dnovichkov.yadiskgallery.domain.repository.ICacheRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ICacheRepository.
 * Manages local caching of media files and metadata.
 */
@Singleton
class CacheRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaDao: MediaDao,
    private val folderDao: FolderDao,
    private val cacheMetadataDao: CacheMetadataDao
) : ICacheRepository {

    private val cacheDir: File = File(context.cacheDir, CACHE_SUBDIR)
    private var maxCacheSize: Long = DEFAULT_MAX_CACHE_SIZE

    private val _cacheSizeFlow = MutableStateFlow(0L)
    private var hitCount = 0L
    private var missCount = 0L

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    override fun observeCacheSize(): Flow<Long> {
        return _cacheSizeFlow.asStateFlow()
    }

    override suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
        // Calculate database size
        val dbSize = mediaDao.getTotalSize() ?: 0L

        // Calculate file cache size
        val fileSize = calculateDirectorySize(cacheDir)

        val totalSize = dbSize + fileSize
        _cacheSizeFlow.value = totalSize
        totalSize
    }

    override suspend fun getMaxCacheSize(): Long {
        return maxCacheSize
    }

    override suspend fun setMaxCacheSize(sizeBytes: Long): Result<Unit> {
        return runCatching {
            maxCacheSize = sizeBytes
            // Trigger cleanup if current size exceeds new max
            val currentSize = getCacheSize()
            if (currentSize > maxCacheSize) {
                trimCache(maxCacheSize)
            }
        }
    }

    override suspend fun clearCache(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Clear database caches
            mediaDao.deleteAll()
            folderDao.deleteAll()
            cacheMetadataDao.deleteAll()

            // Clear file cache
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()

            _cacheSizeFlow.value = 0L
            hitCount = 0L
            missCount = 0L
        }
    }

    override suspend fun clearOldCache(maxAgeMillis: Long): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val cutoffTime = System.currentTimeMillis() - maxAgeMillis

                // Delete old entries from database
                val deletedMedia = mediaDao.deleteOlderThan(cutoffTime)
                val deletedFolders = folderDao.deleteOlderThan(cutoffTime)
                val deletedMetadata = cacheMetadataDao.deleteOlderThan(cutoffTime)

                // Delete old cached files
                val deletedFiles = deleteOldFiles(cutoffTime)

                // Update cache size
                getCacheSize()

                deletedMedia + deletedFolders + deletedMetadata + deletedFiles
            }
        }

    override suspend fun isCached(path: String): Boolean = withContext(Dispatchers.IO) {
        val cacheFile = getCacheFileForPath(path)
        val exists = cacheFile.exists()
        if (exists) hitCount++ else missCount++
        exists
    }

    override suspend fun getCachedPath(path: String): String? = withContext(Dispatchers.IO) {
        val cacheFile = getCacheFileForPath(path)
        if (cacheFile.exists()) {
            hitCount++
            cacheFile.absolutePath
        } else {
            missCount++
            null
        }
    }

    override suspend fun cacheFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // The actual file download and caching would be handled by
            // the image loading library (Coil) or a dedicated download service.
            // This method marks the file for caching / ensures cache entry exists.

            val cacheFile = getCacheFileForPath(path)
            cacheFile.parentFile?.mkdirs()

            // Trigger cache cleanup if needed
            val currentSize = getCacheSize()
            if (currentSize > maxCacheSize) {
                trimCache(maxCacheSize * 8 / 10) // Trim to 80% of max
            }
        }
    }

    override suspend fun removeFromCache(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cacheFile = getCacheFileForPath(path)
            if (cacheFile.exists()) {
                cacheFile.delete()
            }

            // Also remove from database if applicable
            mediaDao.deleteByPath(path)

            // Update cache size
            getCacheSize()
            Unit
        }
    }

    override suspend fun getCacheStats(): CacheStats = withContext(Dispatchers.IO) {
        val totalSize = getCacheSize()
        val fileCount = mediaDao.getCount() + countCachedFiles()

        CacheStats(
            totalSizeBytes = totalSize,
            fileCount = fileCount,
            hitCount = hitCount,
            missCount = missCount,
            maxSizeBytes = maxCacheSize
        )
    }

    // ==================== Private Helper Methods ====================

    private fun getCacheFileForPath(path: String): File {
        // Create a safe filename from the path
        val safeName = path
            .replace("/", "_")
            .replace("\\", "_")
            .replace(":", "_")
            .hashCode()
            .toString()

        // Preserve extension
        val extension = path.substringAfterLast(".", "")
        val fileName = if (extension.isNotEmpty()) "$safeName.$extension" else safeName

        return File(cacheDir, fileName)
    }

    private fun calculateDirectorySize(dir: File): Long {
        if (!dir.exists()) return 0L

        return dir.walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() }
    }

    private fun countCachedFiles(): Int {
        return cacheDir.listFiles()?.size ?: 0
    }

    private suspend fun deleteOldFiles(cutoffTime: Long): Int {
        var deleted = 0
        cacheDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                if (file.delete()) deleted++
            }
        }
        return deleted
    }

    private suspend fun trimCache(targetSize: Long) {
        // Get all cached files sorted by last modified (oldest first)
        val files = cacheDir.listFiles()
            ?.sortedBy { it.lastModified() }
            ?: return

        var currentSize = getCacheSize()

        for (file in files) {
            if (currentSize <= targetSize) break

            val fileSize = file.length()
            if (file.delete()) {
                currentSize -= fileSize
            }
        }

        _cacheSizeFlow.value = currentSize
    }

    companion object {
        private const val CACHE_SUBDIR = "media_cache"
        private const val DEFAULT_MAX_CACHE_SIZE = 100L * 1024 * 1024 // 100 MB
    }
}
