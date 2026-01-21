package com.dnovichkov.yadiskgallery.data.cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for cached media files.
 */
@Entity(
    tableName = "media_files",
    indices = [
        Index(value = ["path"], unique = true),
        Index(value = ["parent_path"]),
        Index(value = ["type"]),
        Index(value = ["modified_at"]),
    ],
)
data class MediaFileEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "path")
    val path: String,
    @ColumnInfo(name = "parent_path")
    val parentPath: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    @ColumnInfo(name = "size")
    val size: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long?,
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long?,
    @ColumnInfo(name = "preview_url")
    val previewUrl: String?,
    @ColumnInfo(name = "md5")
    val md5: String?,
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val TYPE_IMAGE = "IMAGE"
        const val TYPE_VIDEO = "VIDEO"
    }
}
