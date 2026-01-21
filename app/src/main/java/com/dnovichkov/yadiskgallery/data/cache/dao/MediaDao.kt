package com.dnovichkov.yadiskgallery.data.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dnovichkov.yadiskgallery.data.cache.entity.MediaFileEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for media file operations.
 */
@Dao
interface MediaDao {
    // ==================== Queries ====================

    @Query("SELECT * FROM media_files WHERE id = :id")
    suspend fun getById(id: String): MediaFileEntity?

    @Query("SELECT * FROM media_files WHERE path = :path")
    suspend fun getByPath(path: String): MediaFileEntity?

    @Query("SELECT * FROM media_files WHERE parent_path = :parentPath ORDER BY modified_at DESC")
    suspend fun getByParentPath(parentPath: String): List<MediaFileEntity>

    @Query("SELECT * FROM media_files WHERE parent_path = :parentPath ORDER BY modified_at DESC")
    fun observeByParentPath(parentPath: String): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files ORDER BY modified_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<MediaFileEntity>

    @Query("SELECT * FROM media_files ORDER BY modified_at DESC")
    fun observeAll(): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE type = :type ORDER BY modified_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getByType(
        type: String,
        limit: Int,
        offset: Int,
    ): List<MediaFileEntity>

    @Query("SELECT COUNT(*) FROM media_files")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM media_files WHERE parent_path = :parentPath")
    suspend fun getCountByParentPath(parentPath: String): Int

    // ==================== Inserts ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MediaFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MediaFileEntity>)

    // ==================== Deletes ====================

    @Query("DELETE FROM media_files WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM media_files WHERE path = :path")
    suspend fun deleteByPath(path: String)

    @Query("DELETE FROM media_files WHERE parent_path = :parentPath")
    suspend fun deleteByParentPath(parentPath: String)

    @Query("DELETE FROM media_files")
    suspend fun deleteAll()

    @Query("DELETE FROM media_files WHERE cached_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int

    // ==================== Cache Stats ====================

    @Query("SELECT SUM(size) FROM media_files")
    suspend fun getTotalSize(): Long?
}
