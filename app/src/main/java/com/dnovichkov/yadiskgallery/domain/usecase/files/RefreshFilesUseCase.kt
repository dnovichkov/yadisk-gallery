package com.dnovichkov.yadiskgallery.domain.usecase.files

import com.dnovichkov.yadiskgallery.domain.repository.IFilesRepository
import javax.inject.Inject

/**
 * Use case for refreshing folder contents from the server.
 */
class RefreshFilesUseCase @Inject constructor(
    private val filesRepository: IFilesRepository
) {
    /**
     * Refreshes the contents of a folder from the server.
     *
     * @param path Folder path (null for root folder)
     * @return Result indicating success or error
     */
    suspend operator fun invoke(path: String? = null): Result<Unit> {
        return filesRepository.refreshFolder(path)
    }
}
