package com.dnovichkov.yadiskgallery.domain.usecase.files

import com.dnovichkov.yadiskgallery.domain.repository.IFilesRepository
import com.dnovichkov.yadiskgallery.domain.repository.PreviewSize
import javax.inject.Inject

/**
 * Use case for getting download and preview URLs for media files.
 */
class GetMediaDownloadUrlUseCase @Inject constructor(
    private val filesRepository: IFilesRepository
) {
    /**
     * Gets the download URL for a media file.
     *
     * @param path Full path to the file
     * @return Result containing download URL or error
     */
    suspend operator fun invoke(path: String): Result<String> {
        return filesRepository.getDownloadUrl(path)
    }

    /**
     * Gets the preview/thumbnail URL for a media file.
     *
     * @param path Full path to the file
     * @param size Desired preview size
     * @return Result containing preview URL or error
     */
    suspend fun getPreviewUrl(path: String, size: PreviewSize = PreviewSize.M): Result<String> {
        return filesRepository.getPreviewUrl(path, size)
    }

    /**
     * Gets the download URL for a file in a public folder.
     *
     * @param publicUrl Public folder URL
     * @param path Path to the file within the public folder
     * @return Result containing download URL or error
     */
    suspend fun getPublicDownloadUrl(publicUrl: String, path: String): Result<String> {
        return filesRepository.getPublicDownloadUrl(publicUrl, path)
    }
}
