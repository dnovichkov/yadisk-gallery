package com.dnovichkov.yadiskgallery

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * Main Application class for YaDisk Gallery.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class YaDiskGalleryApp : Application(), ImageLoaderFactory {
    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(MEMORY_CACHE_PERCENT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve(DISK_CACHE_DIRECTORY))
                    .maxSizeBytes(DISK_CACHE_SIZE_BYTES)
                    .build()
            }
            .respectCacheHeaders(false)
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }

    companion object {
        private const val MEMORY_CACHE_PERCENT = 0.25
        private const val DISK_CACHE_DIRECTORY = "image_cache"
        private const val DISK_CACHE_SIZE_BYTES = 512L * 1024 * 1024 // 512 MB
    }
}
