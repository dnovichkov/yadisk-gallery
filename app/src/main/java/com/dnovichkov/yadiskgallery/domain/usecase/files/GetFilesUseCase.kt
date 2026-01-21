package com.dnovichkov.yadiskgallery.domain.usecase.files

import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.domain.model.PagedResult
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.repository.IFilesRepository
import javax.inject.Inject

/**
 * Use case for getting all media files from Yandex.Disk.
 */
class GetFilesUseCase
    @Inject
    constructor(
        private val filesRepository: IFilesRepository,
    ) {
        /**
         * Gets all media files with pagination.
         *
         * @param offset Number of items to skip
         * @param limit Maximum number of items to return
         * @param sortOrder Sort order for items
         * @return Result containing paged media files or error
         */
        suspend operator fun invoke(
            offset: Int = 0,
            limit: Int = 20,
            sortOrder: SortOrder = SortOrder.DATE_DESC,
        ): Result<PagedResult<MediaFile>> {
            return filesRepository.getAllMedia(offset, limit, sortOrder)
        }
    }
