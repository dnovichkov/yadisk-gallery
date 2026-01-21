package com.dnovichkov.yadiskgallery.presentation.settings

import app.cash.turbine.test
import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.UserInfo
import com.dnovichkov.yadiskgallery.domain.model.UserSettings
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import com.dnovichkov.yadiskgallery.domain.usecase.auth.GetAuthStateUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.auth.LogoutUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.cache.ClearCacheUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.cache.GetCacheSizeUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.settings.GetSettingsUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.settings.SaveSettingsUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.settings.ValidatePublicUrlUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("SettingsViewModel")
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getSettingsUseCase: GetSettingsUseCase
    private lateinit var saveSettingsUseCase: SaveSettingsUseCase
    private lateinit var validatePublicUrlUseCase: ValidatePublicUrlUseCase
    private lateinit var getAuthStateUseCase: GetAuthStateUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var getCacheSizeUseCase: GetCacheSizeUseCase
    private lateinit var clearCacheUseCase: ClearCacheUseCase

    private lateinit var viewModel: SettingsViewModel

    private val defaultSettings = UserSettings.default()
    private val testUserInfo =
        UserInfo(
            uid = "123",
            login = "test@yandex.ru",
            displayName = "Test User",
            avatarUrl = "https://avatars.yandex.net/123",
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getSettingsUseCase = mockk()
        saveSettingsUseCase = mockk()
        validatePublicUrlUseCase = mockk()
        getAuthStateUseCase = mockk()
        logoutUseCase = mockk()
        getCacheSizeUseCase = mockk()
        clearCacheUseCase = mockk()

        // Default mock behavior
        every { getSettingsUseCase.observeSettings() } returns flowOf(defaultSettings)
        every { getAuthStateUseCase.observeAuthState() } returns flowOf(AuthState.NotAuthenticated)
        every { getCacheSizeUseCase.observeCacheSize() } returns flowOf(0L)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            getSettingsUseCase = getSettingsUseCase,
            saveSettingsUseCase = saveSettingsUseCase,
            validatePublicUrlUseCase = validatePublicUrlUseCase,
            getAuthStateUseCase = getAuthStateUseCase,
            logoutUseCase = logoutUseCase,
            getCacheSizeUseCase = getCacheSizeUseCase,
            clearCacheUseCase = clearCacheUseCase,
        )
    }

    @Nested
    @DisplayName("Initial State")
    inner class InitialState {
        @Test
        @DisplayName("should have default UI state on creation")
        fun shouldHaveDefaultUiStateOnCreation() =
            runTest {
                viewModel = createViewModel()
                advanceUntilIdle()

                val state = viewModel.uiState.value

                assertNull(state.publicFolderUrl)
                assertNull(state.rootFolderPath)
                assertEquals(ViewMode.GRID, state.viewMode)
                assertEquals(SortOrder.DATE_DESC, state.sortOrder)
                assertFalse(state.isLoading)
                assertNull(state.error)
            }

        @Test
        @DisplayName("should load settings from repository")
        fun shouldLoadSettingsFromRepository() =
            runTest {
                val settings =
                    UserSettings(
                        publicFolderUrl = "https://disk.yandex.ru/d/test123",
                        rootFolderPath = "/Photos",
                        isAuthenticated = false,
                        viewMode = ViewMode.LIST,
                        sortOrder = SortOrder.NAME_ASC,
                    )
                every { getSettingsUseCase.observeSettings() } returns flowOf(settings)

                viewModel = createViewModel()
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertEquals("https://disk.yandex.ru/d/test123", state.publicFolderUrl)
                assertEquals("/Photos", state.rootFolderPath)
                assertEquals(ViewMode.LIST, state.viewMode)
                assertEquals(SortOrder.NAME_ASC, state.sortOrder)
            }

        @Test
        @DisplayName("should load auth state from repository")
        fun shouldLoadAuthStateFromRepository() =
            runTest {
                every { getAuthStateUseCase.observeAuthState() } returns
                    flowOf(
                        AuthState.Authenticated(testUserInfo),
                    )

                viewModel = createViewModel()
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertTrue(state.authState is AuthState.Authenticated)
                assertEquals(testUserInfo, (state.authState as AuthState.Authenticated).userInfo)
            }

        @Test
        @DisplayName("should load cache size from repository")
        fun shouldLoadCacheSizeFromRepository() =
            runTest {
                val cacheSize = 1024L * 1024L * 50L // 50 MB
                every { getCacheSizeUseCase.observeCacheSize() } returns flowOf(cacheSize)

                viewModel = createViewModel()
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertEquals(cacheSize, state.cacheSize)
            }
    }

    @Nested
    @DisplayName("Public URL Handling")
    inner class PublicUrlHandling {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should update public URL input")
        fun shouldUpdatePublicUrlInput() =
            runTest {
                advanceUntilIdle()

                viewModel.onPublicUrlChange("https://disk.yandex.ru/d/abc")

                assertEquals("https://disk.yandex.ru/d/abc", viewModel.uiState.value.publicUrlInput)
            }

        @Test
        @DisplayName("should validate and save valid URL")
        fun shouldValidateAndSaveValidUrl() =
            runTest {
                advanceUntilIdle()
                val url = "https://disk.yandex.ru/d/abc123"
                every { validatePublicUrlUseCase(url) } returns Result.success(url)
                coEvery { saveSettingsUseCase.setPublicFolderUrl(url) } returns Result.success(Unit)

                viewModel.onPublicUrlChange(url)
                viewModel.onSavePublicUrl()
                advanceUntilIdle()

                coVerify { saveSettingsUseCase.setPublicFolderUrl(url) }
                assertNull(viewModel.uiState.value.publicUrlError)
            }

        @Test
        @DisplayName("should show error for invalid URL")
        fun shouldShowErrorForInvalidUrl() =
            runTest {
                advanceUntilIdle()
                val url = "invalid-url"
                every { validatePublicUrlUseCase(url) } returns
                    Result.failure(
                        ValidatePublicUrlUseCase.ValidationException(
                            com.dnovichkov.yadiskgallery.domain.model.DomainError.Disk.InvalidPublicUrl(url),
                        ),
                    )

                viewModel.onPublicUrlChange(url)
                viewModel.onSavePublicUrl()
                advanceUntilIdle()

                assertTrue(viewModel.uiState.value.publicUrlError != null)
            }

        @Test
        @DisplayName("should clear public URL")
        fun shouldClearPublicUrl() =
            runTest {
                advanceUntilIdle()
                coEvery { saveSettingsUseCase.setPublicFolderUrl(null) } returns Result.success(Unit)

                viewModel.onClearPublicUrl()
                advanceUntilIdle()

                coVerify { saveSettingsUseCase.setPublicFolderUrl(null) }
            }
    }

    @Nested
    @DisplayName("View Mode and Sort Order")
    inner class ViewModeAndSortOrder {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should change view mode")
        fun shouldChangeViewMode() =
            runTest {
                advanceUntilIdle()
                coEvery { saveSettingsUseCase.setViewMode(ViewMode.LIST) } returns Result.success(Unit)

                viewModel.onViewModeChange(ViewMode.LIST)
                advanceUntilIdle()

                coVerify { saveSettingsUseCase.setViewMode(ViewMode.LIST) }
            }

        @Test
        @DisplayName("should change sort order")
        fun shouldChangeSortOrder() =
            runTest {
                advanceUntilIdle()
                coEvery { saveSettingsUseCase.setSortOrder(SortOrder.NAME_ASC) } returns Result.success(Unit)

                viewModel.onSortOrderChange(SortOrder.NAME_ASC)
                advanceUntilIdle()

                coVerify { saveSettingsUseCase.setSortOrder(SortOrder.NAME_ASC) }
            }
    }

    @Nested
    @DisplayName("Authentication Actions")
    inner class AuthenticationActions {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should request login")
        fun shouldRequestLogin() =
            runTest {
                advanceUntilIdle()

                viewModel.uiState.test {
                    skipItems(1) // Skip initial state
                    viewModel.onLoginClick()
                    val state = awaitItem()
                    assertTrue(state.showLoginDialog)
                }
            }

        @Test
        @DisplayName("should dismiss login dialog")
        fun shouldDismissLoginDialog() =
            runTest {
                advanceUntilIdle()

                viewModel.onLoginClick()
                viewModel.onDismissLoginDialog()

                assertFalse(viewModel.uiState.value.showLoginDialog)
            }

        @Test
        @DisplayName("should logout user")
        fun shouldLogoutUser() =
            runTest {
                advanceUntilIdle()
                coEvery { logoutUseCase() } returns Result.success(Unit)

                viewModel.onLogoutClick()
                advanceUntilIdle()

                coVerify { logoutUseCase() }
            }

        @Test
        @DisplayName("should show logout confirmation dialog")
        fun shouldShowLogoutConfirmationDialog() =
            runTest {
                advanceUntilIdle()

                viewModel.uiState.test {
                    skipItems(1)
                    viewModel.onLogoutRequest()
                    val state = awaitItem()
                    assertTrue(state.showLogoutConfirmation)
                }
            }

        @Test
        @DisplayName("should dismiss logout confirmation dialog")
        fun shouldDismissLogoutConfirmationDialog() =
            runTest {
                advanceUntilIdle()

                viewModel.onLogoutRequest()
                viewModel.onDismissLogoutConfirmation()

                assertFalse(viewModel.uiState.value.showLogoutConfirmation)
            }
    }

    @Nested
    @DisplayName("Cache Management")
    inner class CacheManagement {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should clear cache")
        fun shouldClearCache() =
            runTest {
                advanceUntilIdle()
                coEvery { clearCacheUseCase() } returns Result.success(Unit)

                viewModel.onClearCache()
                advanceUntilIdle()

                coVerify { clearCacheUseCase() }
            }

        @Test
        @DisplayName("should show cache cleared message")
        fun shouldShowCacheClearedMessage() =
            runTest {
                advanceUntilIdle()
                coEvery { clearCacheUseCase() } returns Result.success(Unit)

                viewModel.onClearCache()
                advanceUntilIdle()

                assertTrue(viewModel.uiState.value.showCacheClearedMessage)
            }

        @Test
        @DisplayName("should dismiss cache cleared message")
        fun shouldDismissCacheClearedMessage() =
            runTest {
                advanceUntilIdle()
                coEvery { clearCacheUseCase() } returns Result.success(Unit)

                viewModel.onClearCache()
                advanceUntilIdle()
                viewModel.onDismissCacheClearedMessage()

                assertFalse(viewModel.uiState.value.showCacheClearedMessage)
            }
    }

    @Nested
    @DisplayName("Root Folder Selection")
    inner class RootFolderSelection {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should update root folder path")
        fun shouldUpdateRootFolderPath() =
            runTest {
                advanceUntilIdle()
                val path = "/Photos/2024"
                coEvery { saveSettingsUseCase.setRootFolderPath(path) } returns Result.success(Unit)

                viewModel.onRootFolderChange(path)
                advanceUntilIdle()

                coVerify { saveSettingsUseCase.setRootFolderPath(path) }
            }

        @Test
        @DisplayName("should clear root folder path")
        fun shouldClearRootFolderPath() =
            runTest {
                advanceUntilIdle()
                coEvery { saveSettingsUseCase.setRootFolderPath(null) } returns Result.success(Unit)

                viewModel.onRootFolderChange(null)
                advanceUntilIdle()

                coVerify { saveSettingsUseCase.setRootFolderPath(null) }
            }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandling {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should handle save settings error")
        fun shouldHandleSaveSettingsError() =
            runTest {
                advanceUntilIdle()
                val url = "https://disk.yandex.ru/d/abc123"
                every { validatePublicUrlUseCase(url) } returns Result.success(url)
                coEvery { saveSettingsUseCase.setPublicFolderUrl(url) } returns
                    Result.failure(
                        Exception("Save failed"),
                    )

                viewModel.onPublicUrlChange(url)
                viewModel.onSavePublicUrl()
                advanceUntilIdle()

                assertTrue(viewModel.uiState.value.error != null)
            }

        @Test
        @DisplayName("should dismiss error")
        fun shouldDismissError() =
            runTest {
                advanceUntilIdle()
                val url = "https://disk.yandex.ru/d/abc123"
                every { validatePublicUrlUseCase(url) } returns Result.success(url)
                coEvery { saveSettingsUseCase.setPublicFolderUrl(url) } returns
                    Result.failure(
                        Exception("Save failed"),
                    )

                viewModel.onPublicUrlChange(url)
                viewModel.onSavePublicUrl()
                advanceUntilIdle()
                viewModel.onDismissError()

                assertNull(viewModel.uiState.value.error)
            }
    }

    @Nested
    @DisplayName("Navigation Events")
    inner class NavigationEvents {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should emit navigate to gallery event when URL saved successfully")
        fun shouldEmitNavigateToGalleryEvent() =
            runTest {
                advanceUntilIdle()
                val url = "https://disk.yandex.ru/d/abc123"
                every { validatePublicUrlUseCase(url) } returns Result.success(url)
                coEvery { saveSettingsUseCase.setPublicFolderUrl(url) } returns Result.success(Unit)

                viewModel.events.test {
                    viewModel.onPublicUrlChange(url)
                    viewModel.onSavePublicUrl()
                    advanceUntilIdle()

                    val event = awaitItem()
                    assertTrue(event is SettingsEvent.NavigateToGallery)
                }
            }

        @Test
        @DisplayName("should emit start login event when login clicked")
        fun shouldEmitStartLoginEvent() =
            runTest {
                advanceUntilIdle()

                viewModel.events.test {
                    viewModel.onConfirmLogin()
                    advanceUntilIdle()

                    val event = awaitItem()
                    assertTrue(event is SettingsEvent.StartYandexLogin)
                }
            }
    }
}
