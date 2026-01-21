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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GetFilesUseCase")
class GetFilesUseCaseTest {
    private lateinit var useCase: GetFilesUseCase
    private lateinit var repository: FakeFilesRepository

    @BeforeEach
    fun setUp() {
        repository = FakeFilesRepository()
        useCase = GetFilesUseCase(repository)
    }

    @Nested
    @DisplayName("invoke()")
    inner class InvokeTests {
        @Test
        @DisplayName("should return all media files")
        fun `should return all media files`() =
            runTest {
                val files =
                    listOf(
                        createTestMediaFile(id = "1", name = "photo1.jpg"),
                        createTestMediaFile(id = "2", name = "photo2.jpg"),
                    )
                repository.allMediaResult =
                    Result.success(
                        PagedResult(
                            items = files,
                            offset = 0,
                            limit = 20,
                            total = 2,
                            hasMore = false,
                        ),
                    )

                val result = useCase()

                assertTrue(result.isSuccess)
                assertEquals(2, result.getOrNull()?.items?.size)
            }

        @Test
        @DisplayName("should pass pagination parameters")
        fun `should pass pagination parameters`() =
            runTest {
                repository.allMediaResult =
                    Result.success(
                        PagedResult(emptyList(), 40, 20, 100, true),
                    )

                useCase(offset = 40, limit = 20, sortOrder = SortOrder.NAME_ASC)

                assertEquals(40, repository.lastOffset)
                assertEquals(20, repository.lastLimit)
                assertEquals(SortOrder.NAME_ASC, repository.lastSortOrder)
            }

        @Test
        @DisplayName("should propagate errors")
        fun `should propagate errors`() =
            runTest {
                repository.allMediaResult = Result.failure(RuntimeException("API error"))

                val result = useCase()

                assertTrue(result.isFailure)
            }
    }

    private fun createTestMediaFile(
        id: String = "test-id",
        name: String = "test.jpg",
    ) = MediaFile(
        id = id,
        name = name,
        path = "/$name",
        type = MediaType.IMAGE,
        mimeType = "image/jpeg",
        size = 100L,
        createdAt = null,
        modifiedAt = null,
        previewUrl = null,
        md5 = null,
    )

    private class FakeFilesRepository : IFilesRepository {
        var allMediaResult: Result<PagedResult<MediaFile>> =
            Result.success(
                PagedResult(emptyList(), 0, 20, 0, false),
            )
        var lastOffset: Int = 0
        var lastLimit: Int = 20
        var lastSortOrder: SortOrder = SortOrder.DATE_DESC

        override suspend fun getFolderContents(
            path: String?,
            offset: Int,
            limit: Int,
            sortOrder: SortOrder,
            mediaOnly: Boolean,
        ): Result<PagedResult<DiskItem>> = Result.success(PagedResult(emptyList(), 0, 20, 0, false))

        override fun observeFolderContents(
            path: String?,
            sortOrder: SortOrder,
            mediaOnly: Boolean,
        ): Flow<List<DiskItem>> = flowOf(emptyList())

        override suspend fun getAllMedia(
            offset: Int,
            limit: Int,
            sortOrder: SortOrder,
        ): Result<PagedResult<MediaFile>> {
            lastOffset = offset
            lastLimit = limit
            lastSortOrder = sortOrder
            return allMediaResult
        }

        override suspend fun getMediaFile(path: String): Result<MediaFile> = Result.failure(RuntimeException("Not implemented"))

        override suspend fun getFolder(path: String): Result<Folder> = Result.failure(RuntimeException("Not implemented"))

        override suspend fun getDownloadUrl(path: String): Result<String> = Result.failure(RuntimeException("Not implemented"))

        override suspend fun getPreviewUrl(
            path: String,
            size: PreviewSize,
        ): Result<String> = Result.failure(RuntimeException("Not implemented"))

        override suspend fun refreshFolder(path: String?): Result<Unit> = Result.success(Unit)

        override suspend fun getPublicFolderContents(
            publicUrl: String,
            path: String?,
            offset: Int,
            limit: Int,
        ): Result<PagedResult<DiskItem>> = Result.success(PagedResult(emptyList(), 0, 20, 0, false))

        override suspend fun getPublicDownloadUrl(
            publicUrl: String,
            path: String,
        ): Result<String> = Result.failure(RuntimeException("Not implemented"))
    }
}
