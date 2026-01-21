package com.dnovichkov.yadiskgallery.domain.usecase.settings

import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.UserSettings
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import com.dnovichkov.yadiskgallery.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GetSettingsUseCase")
class GetSettingsUseCaseTest {
    private lateinit var useCase: GetSettingsUseCase
    private lateinit var repository: FakeSettingsRepository

    @BeforeEach
    fun setUp() {
        repository = FakeSettingsRepository()
        useCase = GetSettingsUseCase(repository)
    }

    @Nested
    @DisplayName("invoke()")
    inner class InvokeTests {
        @Test
        @DisplayName("should return settings from repository")
        fun `should return settings from repository`() =
            runTest {
                val expectedSettings =
                    UserSettings(
                        publicFolderUrl = "https://disk.yandex.ru/d/abc123",
                        rootFolderPath = "/Photos",
                        isAuthenticated = true,
                        viewMode = ViewMode.GRID,
                        sortOrder = SortOrder.DATE_DESC,
                    )
                repository.settingsToReturn = expectedSettings

                val result = useCase()

                assertEquals(Result.success(expectedSettings), result)
            }

        @Test
        @DisplayName("should return default settings when none set")
        fun `should return default settings when none set`() =
            runTest {
                repository.settingsToReturn = UserSettings.default()

                val result = useCase()

                assertEquals(Result.success(UserSettings.default()), result)
            }

        @Test
        @DisplayName("should propagate errors from repository")
        fun `should propagate errors from repository`() =
            runTest {
                val exception = RuntimeException("Database error")
                repository.errorToThrow = exception

                val result = useCase()

                assertEquals(true, result.isFailure)
                assertEquals(exception, result.exceptionOrNull())
            }
    }

    @Nested
    @DisplayName("observeSettings()")
    inner class ObserveSettingsTests {
        @Test
        @DisplayName("should return flow from repository")
        fun `should return flow from repository`() =
            runTest {
                val expectedSettings =
                    UserSettings(
                        publicFolderUrl = "url",
                        rootFolderPath = "/path",
                        isAuthenticated = false,
                        viewMode = ViewMode.LIST,
                        sortOrder = SortOrder.NAME_ASC,
                    )
                repository.settingsFlowToReturn = flowOf(expectedSettings)

                val result = useCase.observeSettings().first()

                assertEquals(expectedSettings, result)
            }
    }

    private class FakeSettingsRepository : ISettingsRepository {
        var settingsToReturn: UserSettings = UserSettings.default()
        var settingsFlowToReturn: Flow<UserSettings> = flowOf(UserSettings.default())
        var errorToThrow: Throwable? = null

        override fun observeSettings(): Flow<UserSettings> = settingsFlowToReturn

        override suspend fun getSettings(): Result<UserSettings> {
            errorToThrow?.let { return Result.failure(it) }
            return Result.success(settingsToReturn)
        }

        override suspend fun setPublicFolderUrl(url: String?): Result<Unit> = Result.success(Unit)

        override suspend fun setRootFolderPath(path: String?): Result<Unit> = Result.success(Unit)

        override suspend fun setViewMode(viewMode: ViewMode): Result<Unit> = Result.success(Unit)

        override suspend fun setSortOrder(sortOrder: SortOrder): Result<Unit> = Result.success(Unit)

        override suspend fun clearSettings(): Result<Unit> = Result.success(Unit)
    }
}
