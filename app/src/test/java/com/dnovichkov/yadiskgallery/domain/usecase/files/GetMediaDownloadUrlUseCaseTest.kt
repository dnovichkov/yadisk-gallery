package com.dnovichkov.yadiskgallery.domain.usecase.files

import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.Folder
import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.domain.model.PagedResult
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.repository.IFilesRepository
import com.dnovichkov.yadiskgallery.domain.repository.PreviewSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GetMediaDownloadUrlUseCase")
class GetMediaDownloadUrlUseCaseTest {

    private lateinit var useCase: GetMediaDownloadUrlUseCase
    private lateinit var repository: FakeFilesRepository

    @BeforeEach
    fun setUp() {
        repository = FakeFilesRepository()
        useCase = GetMediaDownloadUrlUseCase(repository)
    }

    @Nested
    @DisplayName("invoke()")
    inner class InvokeTests {

        @Test
        @DisplayName("should return download URL")
        fun `should return download URL`() = runTest {
            repository.downloadUrlResult = Result.success("https://download.url/file.jpg")

            val result = useCase(path = "/photos/file.jpg")

            assertTrue(result.isSuccess)
            assertEquals("https://download.url/file.jpg", result.getOrNull())
            assertEquals("/photos/file.jpg", repository.lastDownloadPath)
        }

        @Test
        @DisplayName("should propagate errors")
        fun `should propagate errors`() = runTest {
            repository.downloadUrlResult = Result.failure(RuntimeException("Not found"))

            val result = useCase(path = "/missing.jpg")

            assertTrue(result.isFailure)
        }
    }

    @Nested
    @DisplayName("getPreviewUrl()")
    inner class GetPreviewUrlTests {

        @Test
        @DisplayName("should return preview URL with default size")
        fun `should return preview URL with default size`() = runTest {
            repository.previewUrlResult = Result.success("https://preview.url/thumb.jpg")

            val result = useCase.getPreviewUrl(path = "/photo.jpg")

            assertTrue(result.isSuccess)
            assertEquals(PreviewSize.M, repository.lastPreviewSize)
        }

        @Test
        @DisplayName("should return preview URL with specified size")
        fun `should return preview URL with specified size`() = runTest {
            repository.previewUrlResult = Result.success("https://preview.url/thumb_xl.jpg")

            val result = useCase.getPreviewUrl(path = "/photo.jpg", size = PreviewSize.XL)

            assertTrue(result.isSuccess)
            assertEquals(PreviewSize.XL, repository.lastPreviewSize)
        }
    }

    @Nested
    @DisplayName("getPublicDownloadUrl()")
    inner class GetPublicDownloadUrlTests {

        @Test
        @DisplayName("should return public download URL")
        fun `should return public download URL`() = runTest {
            repository.publicDownloadUrlResult = Result.success("https://public.download/file.jpg")

            val result = useCase.getPublicDownloadUrl(
                publicUrl = "https://disk.yandex.ru/d/abc",
                path = "/file.jpg"
            )

            assertTrue(result.isSuccess)
            assertEquals("https://disk.yandex.ru/d/abc", repository.lastPublicUrl)
        }
    }

    private class FakeFilesRepository : IFilesRepository {
        var downloadUrlResult: Result<String> = Result.success("")
        var previewUrlResult: Result<String> = Result.success("")
        var publicDownloadUrlResult: Result<String> = Result.success("")
        var lastDownloadPath: String? = null
        var lastPreviewSize: PreviewSize = PreviewSize.M
        var lastPublicUrl: String? = null

        override suspend fun getFolderContents(
            path: String?,
            offset: Int,
            limit: Int,
            sortOrder: SortOrder,
            mediaOnly: Boolean
        ): Result<PagedResult<DiskItem>> = Result.success(PagedResult(emptyList(), 0, 20, 0, false))

        override fun observeFolderContents(
            path: String?,
            sortOrder: SortOrder,
            mediaOnly: Boolean
        ): Flow<List<DiskItem>> = flowOf(emptyList())

        override suspend fun getAllMedia(
            offset: Int,
            limit: Int,
            sortOrder: SortOrder
        ): Result<PagedResult<MediaFile>> = Result.success(PagedResult(emptyList(), 0, 20, 0, false))

        override suspend fun getMediaFile(path: String): Result<MediaFile> =
            Result.failure(RuntimeException("Not implemented"))

        override suspend fun getFolder(path: String): Result<Folder> =
            Result.failure(RuntimeException("Not implemented"))

        override suspend fun getDownloadUrl(path: String): Result<String> {
            lastDownloadPath = path
            return downloadUrlResult
        }

        override suspend fun getPreviewUrl(path: String, size: PreviewSize): Result<String> {
            lastPreviewSize = size
            return previewUrlResult
        }

        override suspend fun refreshFolder(path: String?): Result<Unit> = Result.success(Unit)

        override suspend fun getPublicFolderContents(
            publicUrl: String,
            path: String?,
            offset: Int,
            limit: Int
        ): Result<PagedResult<DiskItem>> = Result.success(PagedResult(emptyList(), 0, 20, 0, false))

        override suspend fun getPublicDownloadUrl(publicUrl: String, path: String): Result<String> {
            lastPublicUrl = publicUrl
            return publicDownloadUrlResult
        }
    }
}
