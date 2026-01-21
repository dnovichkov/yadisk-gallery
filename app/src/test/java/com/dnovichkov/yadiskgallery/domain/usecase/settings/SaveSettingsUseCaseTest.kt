package com.dnovichkov.yadiskgallery.domain.usecase.settings

import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.UserSettings
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import com.dnovichkov.yadiskgallery.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SaveSettingsUseCase")
class SaveSettingsUseCaseTest {
    private lateinit var useCase: SaveSettingsUseCase
    private lateinit var repository: FakeSettingsRepository

    @BeforeEach
    fun setUp() {
        repository = FakeSettingsRepository()
        useCase = SaveSettingsUseCase(repository)
    }

    @Nested
    @DisplayName("setPublicFolderUrl()")
    inner class SetPublicFolderUrlTests {
        @Test
        @DisplayName("should save public folder URL")
        fun `should save public folder URL`() =
            runTest {
                val url = "https://disk.yandex.ru/d/abc123"

                val result = useCase.setPublicFolderUrl(url)

                assertTrue(result.isSuccess)
                assertEquals(url, repository.savedPublicFolderUrl)
            }

        @Test
        @DisplayName("should allow null URL to clear setting")
        fun `should allow null URL to clear setting`() =
            runTest {
                val result = useCase.setPublicFolderUrl(null)

                assertTrue(result.isSuccess)
                assertEquals(null, repository.savedPublicFolderUrl)
            }

        @Test
        @DisplayName("should propagate errors")
        fun `should propagate errors`() =
            runTest {
                repository.errorToThrow = RuntimeException("Save failed")

                val result = useCase.setPublicFolderUrl("url")

                assertTrue(result.isFailure)
            }
    }

    @Nested
    @DisplayName("setRootFolderPath()")
    inner class SetRootFolderPathTests {
        @Test
        @DisplayName("should save root folder path")
        fun `should save root folder path`() =
            runTest {
                val path = "/Photos/2024"

                val result = useCase.setRootFolderPath(path)

                assertTrue(result.isSuccess)
                assertEquals(path, repository.savedRootFolderPath)
            }

        @Test
        @DisplayName("should allow null path for disk root")
        fun `should allow null path for disk root`() =
            runTest {
                val result = useCase.setRootFolderPath(null)

                assertTrue(result.isSuccess)
                assertEquals(null, repository.savedRootFolderPath)
            }
    }

    @Nested
    @DisplayName("setViewMode()")
    inner class SetViewModeTests {
        @Test
        @DisplayName("should save GRID view mode")
        fun `should save GRID view mode`() =
            runTest {
                val result = useCase.setViewMode(ViewMode.GRID)

                assertTrue(result.isSuccess)
                assertEquals(ViewMode.GRID, repository.savedViewMode)
            }

        @Test
        @DisplayName("should save LIST view mode")
        fun `should save LIST view mode`() =
            runTest {
                val result = useCase.setViewMode(ViewMode.LIST)

                assertTrue(result.isSuccess)
                assertEquals(ViewMode.LIST, repository.savedViewMode)
            }
    }

    @Nested
    @DisplayName("setSortOrder()")
    inner class SetSortOrderTests {
        @Test
        @DisplayName("should save DATE_DESC sort order")
        fun `should save DATE_DESC sort order`() =
            runTest {
                val result = useCase.setSortOrder(SortOrder.DATE_DESC)

                assertTrue(result.isSuccess)
                assertEquals(SortOrder.DATE_DESC, repository.savedSortOrder)
            }

        @Test
        @DisplayName("should save NAME_ASC sort order")
        fun `should save NAME_ASC sort order`() =
            runTest {
                val result = useCase.setSortOrder(SortOrder.NAME_ASC)

                assertTrue(result.isSuccess)
                assertEquals(SortOrder.NAME_ASC, repository.savedSortOrder)
            }
    }

    @Nested
    @DisplayName("clearSettings()")
    inner class ClearSettingsTests {
        @Test
        @DisplayName("should clear all settings")
        fun `should clear all settings`() =
            runTest {
                val result = useCase.clearSettings()

                assertTrue(result.isSuccess)
                assertTrue(repository.clearSettingsCalled)
            }
    }

    private class FakeSettingsRepository : ISettingsRepository {
        var savedPublicFolderUrl: String? = "initial"
        var savedRootFolderPath: String? = "initial"
        var savedViewMode: ViewMode? = null
        var savedSortOrder: SortOrder? = null
        var clearSettingsCalled = false
        var errorToThrow: Throwable? = null

        override fun observeSettings(): Flow<UserSettings> = flowOf(UserSettings.default())

        override suspend fun getSettings(): Result<UserSettings> = Result.success(UserSettings.default())

        override suspend fun setPublicFolderUrl(url: String?): Result<Unit> {
            errorToThrow?.let { return Result.failure(it) }
            savedPublicFolderUrl = url
            return Result.success(Unit)
        }

        override suspend fun setRootFolderPath(path: String?): Result<Unit> {
            errorToThrow?.let { return Result.failure(it) }
            savedRootFolderPath = path
            return Result.success(Unit)
        }

        override suspend fun setViewMode(viewMode: ViewMode): Result<Unit> {
            errorToThrow?.let { return Result.failure(it) }
            savedViewMode = viewMode
            return Result.success(Unit)
        }

        override suspend fun setSortOrder(sortOrder: SortOrder): Result<Unit> {
            errorToThrow?.let { return Result.failure(it) }
            savedSortOrder = sortOrder
            return Result.success(Unit)
        }

        override suspend fun clearSettings(): Result<Unit> {
            errorToThrow?.let { return Result.failure(it) }
            clearSettingsCalled = true
            return Result.success(Unit)
        }
    }
}
