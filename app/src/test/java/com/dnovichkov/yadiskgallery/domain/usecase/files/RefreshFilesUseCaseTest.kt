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

@DisplayName("RefreshFilesUseCase")
class RefreshFilesUseCaseTest {
    private lateinit var useCase: RefreshFilesUseCase
    private lateinit var repository: FakeFilesRepository

    @BeforeEach
    fun setUp() {
        repository = FakeFilesRepository()
        useCase = RefreshFilesUseCase(repository)
    }

    @Nested
    @DisplayName("invoke()")
    inner class InvokeTests {
        @Test
        @DisplayName("should refresh folder contents")
        fun `should refresh folder contents`() =
            runTest {
                val result = useCase(path = "/Photos")

                assertTrue(result.isSuccess)
                assertEquals("/Photos", repository.lastRefreshPath)
            }

        @Test
        @DisplayName("should refresh root folder when path is null")
        fun `should refresh root folder when path is null`() =
            runTest {
                val result = useCase(path = null)

                assertTrue(result.isSuccess)
                assertEquals(null, repository.lastRefreshPath)
            }

        @Test
        @DisplayName("should propagate refresh errors")
        fun `should propagate refresh errors`() =
            runTest {
                repository.refreshError = RuntimeException("Network error")

                val result = useCase(path = "/path")

                assertTrue(result.isFailure)
            }
    }

    private class FakeFilesRepository : IFilesRepository {
        var lastRefreshPath: String? = "initial"
        var refreshError: Throwable? = null

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
        ): Result<PagedResult<MediaFile>> = Result.success(PagedResult(emptyList(), 0, 20, 0, false))

        override suspend fun getMediaFile(path: String): Result<MediaFile> = Result.failure(RuntimeException("Not implemented"))

        override suspend fun getFolder(path: String): Result<Folder> = Result.failure(RuntimeException("Not implemented"))

        override suspend fun getDownloadUrl(path: String): Result<String> = Result.failure(RuntimeException("Not implemented"))

        override suspend fun getPreviewUrl(
            path: String,
            size: PreviewSize,
        ): Result<String> = Result.failure(RuntimeException("Not implemented"))

        override suspend fun refreshFolder(path: String?): Result<Unit> {
            lastRefreshPath = path
            return refreshError?.let { Result.failure(it) } ?: Result.success(Unit)
        }

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
