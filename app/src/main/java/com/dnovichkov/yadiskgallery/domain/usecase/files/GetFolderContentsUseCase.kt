package com.dnovichkov.yadiskgallery.domain.usecase.files

import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.PagedResult
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.repository.IFilesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting the contents of a folder.
 */
class GetFolderContentsUseCase @Inject constructor(
    private val filesRepository: IFilesRepository
) {
    /**
     * Gets the contents of a folder with pagination.
     *
     * @param path Folder path (null for root folder)
     * @param offset Number of items to skip
     * @param limit Maximum number of items to return
     * @param sortOrder Sort order for items
     * @param mediaOnly If true, only returns media files
     * @return Result containing paged disk items or error
     */
    suspend operator fun invoke(
        path: String? = null,
        offset: Int = 0,
        limit: Int = 20,
        sortOrder: SortOrder = SortOrder.DATE_DESC,
        mediaOnly: Boolean = false
    ): Result<PagedResult<DiskItem>> {
        return filesRepository.getFolderContents(path, offset, limit, sortOrder, mediaOnly)
    }

    /**
     * Observes the contents of a folder.
     *
     * @param path Folder path (null for root folder)
     * @param sortOrder Sort order for items
     * @param mediaOnly If true, only returns media files
     * @return Flow of disk items
     */
    fun observeFolderContents(
        path: String? = null,
        sortOrder: SortOrder = SortOrder.DATE_DESC,
        mediaOnly: Boolean = false
    ): Flow<List<DiskItem>> {
        return filesRepository.observeFolderContents(path, sortOrder, mediaOnly)
    }

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
    ): Result<PagedResult<DiskItem>> {
        return filesRepository.getPublicFolderContents(publicUrl, path, offset, limit)
    }
}
