package com.dnovichkov.yadiskgallery.data.repository

import com.dnovichkov.yadiskgallery.data.api.YandexDiskApi
import com.dnovichkov.yadiskgallery.data.cache.dao.CacheMetadataDao
import com.dnovichkov.yadiskgallery.data.cache.dao.FolderDao
import com.dnovichkov.yadiskgallery.data.cache.dao.MediaDao
import com.dnovichkov.yadiskgallery.data.cache.entity.CacheMetadataEntity
import com.dnovichkov.yadiskgallery.data.cache.entity.FolderEntity
import com.dnovichkov.yadiskgallery.data.cache.entity.MediaFileEntity
import com.dnovichkov.yadiskgallery.data.mapper.ResourceMapper
import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.DomainError
import com.dnovichkov.yadiskgallery.domain.model.Folder
import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.domain.model.MediaType
import com.dnovichkov.yadiskgallery.domain.model.PagedResult
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.repository.DomainException
import com.dnovichkov.yadiskgallery.domain.repository.IFilesRepository
import com.dnovichkov.yadiskgallery.domain.repository.PreviewSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of IFilesRepository.
 * Handles file operations with caching strategy.
 */
@Singleton
class FilesRepositoryImpl @Inject constructor(
    private val api: YandexDiskApi,
    private val mediaDao: MediaDao,
    private val folderDao: FolderDao,
    private val cacheMetadataDao: CacheMetadataDao,
    private val resourceMapper: ResourceMapper
) : IFilesRepository {

    override suspend fun getFolderContents(
        path: String?,
        offset: Int,
        limit: Int,
        sortOrder: SortOrder,
        mediaOnly: Boolean
    ): Result<PagedResult<DiskItem>> {
        return runCatching {
            val folderPath = path ?: "/"

            // Check cache freshness
            val cacheMetadata = cacheMetadataDao.getByFolderPath(folderPath)
            val shouldRefresh = cacheMetadata == null || cacheMetadata.isStale()

            if (shouldRefresh) {
                // Fetch from API
                val response = api.getResource(
                    path = folderPath,
                    limit = limit,
                    offset = offset,
                    sort = toApiSortParam(sortOrder),
                    previewSize = "M"
                )

                if (!response.isSuccessful) {
                    throw mapApiError(response.code(), response.message())
                }

                val resource = response.body()
                    ?: throw DomainException(DomainError.Network.ServerError(0, "Empty response"))

                // Cache the results
                val items = resource.embedded?.items ?: emptyList()
                cacheItems(folderPath, items)

                // Update cache metadata
                cacheMetadataDao.insert(
                    CacheMetadataEntity(
                        folderPath = folderPath,
                        lastSyncedAt = System.currentTimeMillis(),
                        totalItems = resource.embedded?.total
                    )
                )

                // Map to domain models
                val diskItems = resourceMapper.toDiskItems(items, mediaOnly)
                val total = resource.embedded?.total
                val hasMore = total != null && (offset + diskItems.size) < total

                PagedResult(
                    items = diskItems,
                    offset = offset,
                    limit = limit,
                    total = total,
                    hasMore = hasMore
                )
            } else {
                // Return from cache
                val folders = folderDao.getByParentPath(folderPath)
                val mediaFiles = mediaDao.getByParentPath(folderPath)

                val diskItems = buildList {
                    addAll(folders.map { DiskItem.Directory(it.toDomain()) })
                    addAll(mediaFiles.map { DiskItem.File(it.toDomain()) })
                }.sortedWith(getSortComparator(sortOrder))
                    .drop(offset)
                    .take(limit)

                val total = cacheMetadata?.totalItems
                val hasMore = total != null && (offset + diskItems.size) < total

                PagedResult(
                    items = diskItems,
                    offset = offset,
                    limit = limit,
                    total = total,
                    hasMore = hasMore
                )
            }
        }
    }

    override fun observeFolderContents(
        path: String?,
        sortOrder: SortOrder,
        mediaOnly: Boolean
    ): Flow<List<DiskItem>> {
        val folderPath = path ?: "/"

        return combine(
            folderDao.observeByParentPath(folderPath),
            mediaDao.observeByParentPath(folderPath)
        ) { folders, mediaFiles ->
            buildList {
                addAll(folders.map { DiskItem.Directory(it.toDomain()) })
                addAll(mediaFiles.map { DiskItem.File(it.toDomain()) })
            }.sortedWith(getSortComparator(sortOrder))
        }
    }

    override suspend fun getAllMedia(
        offset: Int,
        limit: Int,
        sortOrder: SortOrder
    ): Result<PagedResult<MediaFile>> {
        return runCatching {
            // Fetch from API with media type filter
            val response = api.getAllFiles(
                limit = limit,
                offset = offset,
                mediaType = "image,video",
                sort = toApiSortParam(sortOrder),
                previewSize = "M"
            )

            if (!response.isSuccessful) {
                throw mapApiError(response.code(), response.message())
            }

            val filesResponse = response.body()
                ?: throw DomainException(DomainError.Network.ServerError(0, "Empty response"))

            val mediaFiles = filesResponse.items.map { resourceMapper.toMediaFile(it) }
            val total = filesResponse.limit + filesResponse.offset + mediaFiles.size
            val hasMore = mediaFiles.size >= filesResponse.limit

            PagedResult(
                items = mediaFiles,
                offset = offset,
                limit = limit,
                total = total,
                hasMore = hasMore
            )
        }
    }

    override suspend fun getMediaFile(path: String): Result<MediaFile> {
        return runCatching {
            // Check cache first
            val cached = mediaDao.getByPath(path)
            if (cached != null) {
                return@runCatching cached.toDomain()
            }

            // Fetch from API
            val response = api.getResource(path = path, previewSize = "L")

            if (!response.isSuccessful) {
                throw mapApiError(response.code(), response.message())
            }

            val resource = response.body()
                ?: throw DomainException(DomainError.Network.ServerError(0, "Empty response"))

            resourceMapper.toMediaFile(resource)
        }
    }

    override suspend fun getFolder(path: String): Result<Folder> {
        return runCatching {
            // Check cache first
            val cached = folderDao.getByPath(path)
            if (cached != null) {
                return@runCatching cached.toDomain()
            }

            // Fetch from API
            val response = api.getResource(path = path)

            if (!response.isSuccessful) {
                throw mapApiError(response.code(), response.message())
            }

            val resource = response.body()
                ?: throw DomainException(DomainError.Network.ServerError(0, "Empty response"))

            resourceMapper.toFolder(resource)
        }
    }

    override suspend fun getDownloadUrl(path: String): Result<String> {
        return runCatching {
            val response = api.getDownloadLink(path = path)

            if (!response.isSuccessful) {
                throw mapApiError(response.code(), response.message())
            }

            response.body()?.href
                ?: throw DomainException(DomainError.Network.ServerError(0, "No download URL"))
        }
    }

    override suspend fun getPreviewUrl(path: String, size: PreviewSize): Result<String> {
        return runCatching {
            val response = api.getResource(
                path = path,
                previewSize = size.value
            )

            if (!response.isSuccessful) {
                throw mapApiError(response.code(), response.message())
            }

            response.body()?.preview
                ?: throw DomainException(DomainError.Disk.NotFound(path))
        }
    }

    override suspend fun refreshFolder(path: String?): Result<Unit> {
        return runCatching {
            val folderPath = path ?: "/"

            // Invalidate cache
            cacheMetadataDao.deleteByFolderPath(folderPath)

            // Fetch fresh data
            val response = api.getResource(
                path = folderPath,
                limit = 1000, // Fetch all items
                previewSize = "M"
            )

            if (!response.isSuccessful) {
                throw mapApiError(response.code(), response.message())
            }

            val resource = response.body()
                ?: throw DomainException(DomainError.Network.ServerError(0, "Empty response"))

            // Clear old cache for this folder
            folderDao.deleteByParentPath(folderPath)
            mediaDao.deleteByParentPath(folderPath)

            // Cache new items
            val items = resource.embedded?.items ?: emptyList()
            cacheItems(folderPath, items)

            // Update metadata
            cacheMetadataDao.insert(
                CacheMetadataEntity(
                    folderPath = folderPath,
                    lastSyncedAt = System.currentTimeMillis(),
                    totalItems = resource.embedded?.total
                )
            )
        }
    }

    override suspend fun getPublicFolderContents(
        publicUrl: String,
        path: String?,
        offset: Int,
        limit: Int
    ): Result<PagedResult<DiskItem>> {
        return runCatching {
            val response = api.getPublicResource(
                publicKey = publicUrl,
                path = path,
                limit = limit,
                offset = offset,
                previewSize = "M"
            )

            if (!response.isSuccessful) {
                throw mapApiError(response.code(), response.message())
            }

            val resource = response.body()
                ?: throw DomainException(DomainError.Network.ServerError(0, "Empty response"))

            val items = resource.embedded?.items ?: emptyList()
            val diskItems = resourceMapper.toDiskItems(items)
            val total = resource.embedded?.total
            val hasMore = total != null && (offset + diskItems.size) < total

            PagedResult(
                items = diskItems,
                offset = offset,
                limit = limit,
                total = total,
                hasMore = hasMore
            )
        }
    }

    override suspend fun getPublicDownloadUrl(publicUrl: String, path: String): Result<String> {
        return runCatching {
            val response = api.getPublicDownloadLink(
                publicKey = publicUrl,
                path = path
            )

            if (!response.isSuccessful) {
                throw mapApiError(response.code(), response.message())
            }

            response.body()?.href
                ?: throw DomainException(DomainError.Network.ServerError(0, "No download URL"))
        }
    }

    // ==================== Private Helper Methods ====================

    private suspend fun cacheItems(
        parentPath: String,
        items: List<com.dnovichkov.yadiskgallery.data.api.dto.ResourceDto>
    ) {
        val folders = mutableListOf<FolderEntity>()
        val mediaFiles = mutableListOf<MediaFileEntity>()

        items.forEach { dto ->
            if (dto.isDirectory) {
                folders.add(dto.toFolderEntity(parentPath))
            } else {
                mediaFiles.add(dto.toMediaFileEntity(parentPath))
            }
        }

        if (folders.isNotEmpty()) {
            folderDao.insertAll(folders)
        }
        if (mediaFiles.isNotEmpty()) {
            mediaDao.insertAll(mediaFiles)
        }
    }

    private fun toApiSortParam(sortOrder: SortOrder): String {
        return when (sortOrder) {
            SortOrder.NAME_ASC -> "name"
            SortOrder.NAME_DESC -> "-name"
            SortOrder.DATE_ASC -> "modified"
            SortOrder.DATE_DESC -> "-modified"
            SortOrder.SIZE_ASC -> "size"
            SortOrder.SIZE_DESC -> "-size"
        }
    }

    private fun getSortComparator(sortOrder: SortOrder): Comparator<DiskItem> {
        return when (sortOrder) {
            SortOrder.NAME_ASC -> compareBy { it.name }
            SortOrder.NAME_DESC -> compareByDescending { it.name }
            SortOrder.DATE_ASC -> compareBy { it.modifiedAt }
            SortOrder.DATE_DESC -> compareByDescending { it.modifiedAt }
            SortOrder.SIZE_ASC -> compareBy {
                when (it) {
                    is DiskItem.File -> it.mediaFile.size
                    is DiskItem.Directory -> 0L
                }
            }
            SortOrder.SIZE_DESC -> compareByDescending {
                when (it) {
                    is DiskItem.File -> it.mediaFile.size
                    is DiskItem.Directory -> 0L
                }
            }
        }
    }

    private fun mapApiError(code: Int, message: String): DomainException {
        val error = when (code) {
            401 -> DomainError.Auth.Unauthorized
            403 -> DomainError.Auth.Unauthorized
            404 -> DomainError.Disk.NotFound(message)
            429 -> DomainError.Network.ServerError(code, "Rate limit exceeded")
            in 500..599 -> DomainError.Network.ServerError(code, message)
            else -> DomainError.Network.ServerError(code, message)
        }
        return DomainException(error)
    }
}

// ==================== Extension Functions ====================

private fun FolderEntity.toDomain(): Folder {
    return Folder(
        id = id,
        name = name,
        path = path,
        itemsCount = itemsCount,
        createdAt = createdAt?.let { Instant.ofEpochMilli(it) },
        modifiedAt = modifiedAt?.let { Instant.ofEpochMilli(it) }
    )
}

private fun MediaFileEntity.toDomain(): MediaFile {
    return MediaFile(
        id = id,
        name = name,
        path = path,
        type = MediaType.valueOf(type),
        mimeType = mimeType,
        size = size,
        createdAt = createdAt?.let { Instant.ofEpochMilli(it) },
        modifiedAt = modifiedAt?.let { Instant.ofEpochMilli(it) },
        previewUrl = previewUrl,
        md5 = md5
    )
}

private fun com.dnovichkov.yadiskgallery.data.api.dto.ResourceDto.toFolderEntity(
    parentPath: String
): FolderEntity {
    return FolderEntity(
        id = resourceId ?: path,
        name = name,
        path = path,
        parentPath = parentPath,
        itemsCount = embedded?.total,
        createdAt = parseTimestamp(created),
        modifiedAt = parseTimestamp(modified)
    )
}

private fun com.dnovichkov.yadiskgallery.data.api.dto.ResourceDto.toMediaFileEntity(
    parentPath: String
): MediaFileEntity {
    return MediaFileEntity(
        id = resourceId ?: path,
        name = name,
        path = path,
        parentPath = parentPath,
        type = if (mimeType?.startsWith("video/") == true) "VIDEO" else "IMAGE",
        mimeType = mimeType ?: "application/octet-stream",
        size = size ?: 0L,
        createdAt = parseTimestamp(created),
        modifiedAt = parseTimestamp(modified),
        previewUrl = preview,
        md5 = md5
    )
}

private fun parseTimestamp(dateTime: String?): Long? {
    if (dateTime == null) return null
    return try {
        Instant.parse(dateTime).toEpochMilli()
    } catch (e: Exception) {
        null
    }
}

private val DiskItem.modifiedAt: Instant?
    get() = when (this) {
        is DiskItem.File -> mediaFile.modifiedAt
        is DiskItem.Directory -> folder.modifiedAt
    }
