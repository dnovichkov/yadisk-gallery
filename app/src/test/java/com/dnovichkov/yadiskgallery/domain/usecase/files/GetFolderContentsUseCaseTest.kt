package com.dnovichkov.yadiskgallery.domain.usecase.files

import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.Folder
import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.domain.model.MediaType
import com.dnovichkov.yadiskgallery.domain.model.PagedResult
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.repository.IFilesRepository
import com.dnovichkov.yadiskgallery.domain.repository.PreviewSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GetFolderContentsUseCase")
class GetFolderContentsUseCaseTest {

    private lateinit var useCase: GetFolderContentsUseCase
    private lateinit var repository: FakeFilesRepository

    @BeforeEach
    fun setUp() {
        repository = FakeFilesRepository()
        useCase = GetFolderContentsUseCase(repository)
    }

    @Nested
    @DisplayName("invoke()")
    inner class InvokeTests {

        @Test
        @DisplayName("should return folder contents")
        fun `should return folder contents`() = runTest {
            val items = listOf(
                DiskItem.Directory(createTestFolder("1", "Photos")),
                DiskItem.File(createTestMediaFile("2", "photo.jpg"))
            )
            repository.folderContentsResult = Result.success(
                PagedResult(items, 0, 20, 2, false)
            )

            val result = useCase(path = "/root")

            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrNull()?.items?.size)
        }

        @Test
        @DisplayName("should pass all parameters to repository")
        fun `should pass all parameters to repository`() = runTest {
            repository.folderContentsResult = Result.success(
                PagedResult(emptyList(), 0, 20, 0, false)
            )

            useCase(
                path = "/Photos",
                offset = 20,
                limit = 50,
                sortOrder = SortOrder.SIZE_DESC,
                mediaOnly = true
            )

            assertEquals("/Photos", repository.lastPath)
            assertEquals(20, repository.lastOffset)
            assertEquals(50, repository.lastLimit)
            assertEquals(SortOrder.SIZE_DESC, repository.lastSortOrder)
            assertEquals(true, repository.lastMediaOnly)
        }

        @Test
        @DisplayName("should use null path for root folder")
        fun `should use null path for root folder`() = runTest {
            repository.folderContentsResult = Result.success(
                PagedResult(emptyList(), 0, 20, 0, false)
            )

            useCase(path = null)

            assertEquals(null, repository.lastPath)
        }
    }

    @Nested
    @DisplayName("observeFolderContents()")
    inner class ObserveFolderContentsTests {

        @Test
        @DisplayName("should observe folder contents")
        fun `should observe folder contents`() = runTest {
            val items = listOf(
                DiskItem.File(createTestMediaFile("1", "test.jpg"))
            )
            repository.observeResult = flowOf(items)

            val result = useCase.observeFolderContents("/path").first()

            assertEquals(1, result.size)
        }
    }

    @Nested
    @DisplayName("getPublicFolderContents()")
    inner class GetPublicFolderContentsTests {

        @Test
        @DisplayName("should get public folder contents")
        fun `should get public folder contents`() = runTest {
            val items = listOf(
                DiskItem.File(createTestMediaFile("1", "public.jpg"))
            )
            repository.publicFolderResult = Result.success(
                PagedResult(items, 0, 20, 1, false)
            )

            val result = useCase.getPublicFolderContents(
                publicUrl = "https://disk.yandex.ru/d/abc123",
                path = "/subfolder"
            )

            assertTrue(result.isSuccess)
            assertEquals("https://disk.yandex.ru/d/abc123", repository.lastPublicUrl)
        }
    }

    private fun createTestMediaFile(id: String, name: String) = MediaFile(
        id = id,
        name = name,
        path = "/$name",
        type = MediaType.IMAGE,
        mimeType = "image/jpeg",
        size = 100L,
        createdAt = null,
        modifiedAt = null,
        previewUrl = null,
        md5 = null
    )

    private fun createTestFolder(id: String, name: String) = Folder(
        id = id,
        name = name,
        path = "/$name",
        itemsCount = null,
        createdAt = null,
        modifiedAt = null
    )

    private class FakeFilesRepository : IFilesRepository {
        var folderContentsResult: Result<PagedResult<DiskItem>> = Result.success(
            PagedResult(emptyList(), 0, 20, 0, false)
        )
        var observeResult: Flow<List<DiskItem>> = flowOf(emptyList())
        var publicFolderResult: Result<PagedResult<DiskItem>> = Result.success(
            PagedResult(emptyList(), 0, 20, 0, false)
        )
        var lastPath: String? = null
        var lastOffset: Int = 0
        var lastLimit: Int = 20
        var lastSortOrder: SortOrder = SortOrder.DATE_DESC
        var lastMediaOnly: Boolean = false
        var lastPublicUrl: String? = null

        override suspend fun getFolderContents(
            path: String?,
            offset: Int,
            limit: Int,
            sortOrder: SortOrder,
            mediaOnly: Boolean
        ): Result<PagedResult<DiskItem>> {
            lastPath = path
            lastOffset = offset
            lastLimit = limit
            lastSortOrder = sortOrder
            lastMediaOnly = mediaOnly
            return folderContentsResult
        }

        override fun observeFolderContents(
            path: String?,
            sortOrder: SortOrder,
            mediaOnly: Boolean
        ): Flow<List<DiskItem>> = observeResult

        override suspend fun getAllMedia(
            offset: Int,
            limit: Int,
            sortOrder: SortOrder
        ): Result<PagedResult<MediaFile>> = Result.success(PagedResult(emptyList(), 0, 20, 0, false))

        override suspend fun getMediaFile(path: String): Result<MediaFile> =
            Result.failure(RuntimeException("Not implemented"))

        override suspend fun getFolder(path: String): Result<Folder> =
            Result.failure(RuntimeException("Not implemented"))

        override suspend fun getDownloadUrl(path: String): Result<String> =
            Result.failure(RuntimeException("Not implemented"))

        override suspend fun getPreviewUrl(path: String, size: PreviewSize): Result<String> =
            Result.failure(RuntimeException("Not implemented"))

        override suspend fun refreshFolder(path: String?): Result<Unit> = Result.success(Unit)

        override suspend fun getPublicFolderContents(
            publicUrl: String,
            path: String?,
            offset: Int,
            limit: Int
        ): Result<PagedResult<DiskItem>> {
            lastPublicUrl = publicUrl
            return publicFolderResult
        }

        override suspend fun getPublicDownloadUrl(publicUrl: String, path: String): Result<String> =
            Result.failure(RuntimeException("Not implemented"))
    }
}
