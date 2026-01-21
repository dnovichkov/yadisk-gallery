package com.dnovichkov.yadiskgallery.data.cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for cache metadata.
 * Tracks when folders were last synced to determine cache freshness.
 */
@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "folder_path")
    val folderPath: String,
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long,
    @ColumnInfo(name = "total_items")
    val totalItems: Int?,
    @ColumnInfo(name = "etag")
    val etag: String? = null,
) {
    /**
     * Checks if the cache is stale (older than TTL).
     *
     * @param ttlMillis Time-to-live in milliseconds (default: 5 minutes)
     */
    fun isStale(ttlMillis: Long = DEFAULT_TTL_MS): Boolean {
        return System.currentTimeMillis() - lastSyncedAt > ttlMillis
    }

    companion object {
        const val DEFAULT_TTL_MS = 5 * 60 * 1000L // 5 minutes
        const val ROOT_FOLDER_PATH = "/"
    }
}
