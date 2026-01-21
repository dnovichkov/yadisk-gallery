package com.dnovichkov.yadiskgallery.data.cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for cached folders.
 */
@Entity(
    tableName = "folders",
    indices = [
        Index(value = ["path"], unique = true),
        Index(value = ["parent_path"]),
    ],
)
data class FolderEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "path")
    val path: String,
    @ColumnInfo(name = "parent_path")
    val parentPath: String?,
    @ColumnInfo(name = "items_count")
    val itemsCount: Int?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long?,
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long?,
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),
)
