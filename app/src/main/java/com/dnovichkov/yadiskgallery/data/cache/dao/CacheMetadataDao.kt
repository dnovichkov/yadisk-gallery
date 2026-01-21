package com.dnovichkov.yadiskgallery.data.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dnovichkov.yadiskgallery.data.cache.entity.CacheMetadataEntity

/**
 * DAO for cache metadata operations.
 */
@Dao
interface CacheMetadataDao {

    @Query("SELECT * FROM cache_metadata WHERE folder_path = :folderPath")
    suspend fun getByFolderPath(folderPath: String): CacheMetadataEntity?

    @Query("SELECT * FROM cache_metadata")
    suspend fun getAll(): List<CacheMetadataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CacheMetadataEntity)

    @Query("DELETE FROM cache_metadata WHERE folder_path = :folderPath")
    suspend fun deleteByFolderPath(folderPath: String)

    @Query("DELETE FROM cache_metadata")
    suspend fun deleteAll()

    @Query("DELETE FROM cache_metadata WHERE last_synced_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int
}
