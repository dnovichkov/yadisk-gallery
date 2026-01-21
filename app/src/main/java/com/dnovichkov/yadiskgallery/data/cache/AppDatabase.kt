package com.dnovichkov.yadiskgallery.data.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dnovichkov.yadiskgallery.data.cache.dao.CacheMetadataDao
import com.dnovichkov.yadiskgallery.data.cache.dao.FolderDao
import com.dnovichkov.yadiskgallery.data.cache.dao.MediaDao
import com.dnovichkov.yadiskgallery.data.cache.entity.CacheMetadataEntity
import com.dnovichkov.yadiskgallery.data.cache.entity.FolderEntity
import com.dnovichkov.yadiskgallery.data.cache.entity.MediaFileEntity

/**
 * Room database for caching Yandex.Disk data.
 */
@Database(
    entities = [
        MediaFileEntity::class,
        FolderEntity::class,
        CacheMetadataEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

    abstract fun folderDao(): FolderDao

    abstract fun cacheMetadataDao(): CacheMetadataDao

    companion object {
        const val DATABASE_NAME = "yadisk_gallery_db"
    }
}
