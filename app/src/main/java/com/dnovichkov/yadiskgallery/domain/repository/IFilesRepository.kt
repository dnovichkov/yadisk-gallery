package com.dnovichkov.yadiskgallery.domain.repository

import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.Folder
import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.domain.model.PagedResult
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for file operations on Yandex.Disk.
 * Handles both authenticated and public folder access.
 */
interface IFilesRepository {

    /**
     * Gets the contents of a folder with pagination.
     *
     * @param path Folder path (null for root folder)
     * @param offset Number of items to skip
     * @param limit Maximum number of items to return
     * @param sortOrder Sort order for items
     * @param mediaOnly If true, only returns media files (images and videos)
     * @return Result containing paged disk items or error
     */
    suspend fun getFolderContents(
        path: String?,
        offset: Int = 0,
        limit: Int = 20,
        sortOrder: SortOrder = SortOrder.DATE_DESC,
        mediaOnly: Boolean = false
    ): Result<PagedResult<DiskItem>>

    /**
     * Observes the contents of a folder.
     * Emits new values when cache is updated.
     *
     * @param path Folder path (null for root folder)
     * @param sortOrder Sort order for items
     * @param mediaOnly If true, only returns media files
     * @return Flow of disk items
     */
    fun observeFolderContents(
        path: String?,
        sortOrder: SortOrder = SortOrder.DATE_DESC,
        mediaOnly: Boolean = false
    ): Flow<List<DiskItem>>

    /**
     * Gets all media files (images and videos) with pagination.
     * Searches recursively through all folders.
     *
     * @param offset Number of items to skip
     * @param limit Maximum number of items to return
     * @param sortOrder Sort order for items
     * @return Result containing paged media files or error
     */
    suspend fun getAllMedia(
        offset: Int = 0,
        limit: Int = 20,
        sortOrder: SortOrder = SortOrder.DATE_DESC
    ): Result<PagedResult<MediaFile>>

    /**
     * Gets a single media file by path.
     *
     * @param path Full path to the file
     * @return Result containing media file or error
     */
    suspend fun getMediaFile(path: String): Result<MediaFile>

    /**
     * Gets a folder by path.
     *
     * @param path Full path to the folder
     * @return Result containing folder or error
     */
    suspend fun getFolder(path: String): Result<Folder>

    /**
     * Gets the download URL for a media file.
     * The URL is temporary and expires after some time.
     *
     * @param path Full path to the file
     * @return Result containing download URL or error
     */
    suspend fun getDownloadUrl(path: String): Result<String>

    /**
     * Gets the preview/thumbnail URL for a media file.
     *
     * @param path Full path to the file
     * @param size Desired preview size (S, M, L, XL, XXL)
     * @return Result containing preview URL or error
     */
    suspend fun getPreviewUrl(path: String, size: PreviewSize = PreviewSize.M): Result<String>

    /**
     * Refreshes the contents of a folder from the server.
     * Updates the local cache.
     *
     * @param path Folder path (null for root folder)
     * @return Result indicating success or error
     */
    suspend fun refreshFolder(path: String?): Result<Unit>

    /**
     * Gets the contents of a public folder.
     *
     * @param publicUrl Public folder URL
     * @param path Path within the public folder (null for root)
     * @param offset Number of items to skip
     * @param limit Maximum number of items to return
     * @return Result containing paged disk items or error
     */
    suspend fun getPublicFolderContents(
        publicUrl: String,
        path: String? = null,
        offset: Int = 0,
        limit: Int = 20
    ): Result<PagedResult<DiskItem>>

    /**
     * Gets the download URL for a file in a public folder.
     *
     * @param publicUrl Public folder URL
     * @param path Path to the file within the public folder
     * @return Result containing download URL or error
     */
    suspend fun getPublicDownloadUrl(publicUrl: String, path: String): Result<String>
}

/**
 * Preview image size options.
 */
enum class PreviewSize(val value: String) {
    /** Small preview (150x150) */
    S("S"),
    /** Medium preview (300x300) */
    M("M"),
    /** Large preview (500x500) */
    L("L"),
    /** Extra large preview (800x800) */
    XL("XL"),
    /** Extra extra large preview (1024x1024) */
    XXL("XXL")
}
