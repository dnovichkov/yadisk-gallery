package com.dnovichkov.yadiskgallery.data.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dnovichkov.yadiskgallery.data.cache.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for folder operations.
 */
@Dao
interface FolderDao {
    // ==================== Queries ====================

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getById(id: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE path = :path")
    suspend fun getByPath(path: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE parent_path = :parentPath ORDER BY name ASC")
    suspend fun getByParentPath(parentPath: String): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE parent_path = :parentPath ORDER BY name ASC")
    fun observeByParentPath(parentPath: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders ORDER BY name ASC")
    suspend fun getAll(): List<FolderEntity>

    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun observeAll(): Flow<List<FolderEntity>>

    @Query("SELECT COUNT(*) FROM folders")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM folders WHERE parent_path = :parentPath")
    suspend fun getCountByParentPath(parentPath: String): Int

    // ==================== Inserts ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FolderEntity>)

    // ==================== Deletes ====================

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM folders WHERE path = :path")
    suspend fun deleteByPath(path: String)

    @Query("DELETE FROM folders WHERE parent_path = :parentPath")
    suspend fun deleteByParentPath(parentPath: String)

    @Query("DELETE FROM folders")
    suspend fun deleteAll()

    @Query("DELETE FROM folders WHERE cached_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int
}
