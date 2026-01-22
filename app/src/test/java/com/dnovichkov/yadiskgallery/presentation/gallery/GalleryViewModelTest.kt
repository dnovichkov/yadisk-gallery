package com.dnovichkov.yadiskgallery.presentation.gallery

import app.cash.turbine.test
import com.dnovichkov.yadiskgallery.data.network.NetworkMonitor
import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.Folder
import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.domain.model.MediaType
import com.dnovichkov.yadiskgallery.domain.model.PagedResult
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.UserSettings
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import com.dnovichkov.yadiskgallery.domain.usecase.files.GetFolderContentsUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.files.RefreshFilesUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.settings.GetSettingsUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.settings.SaveSettingsUseCase
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
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("GalleryViewModel")
class GalleryViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getFolderContentsUseCase: GetFolderContentsUseCase
    private lateinit var refreshFilesUseCase: RefreshFilesUseCase
    private lateinit var getSettingsUseCase: GetSettingsUseCase
    private lateinit var saveSettingsUseCase: SaveSettingsUseCase
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var viewModel: GalleryViewModel

    private val defaultSettings = UserSettings.default()

    private val testFolder =
        Folder(
            id = "folder1",
            name = "Photos",
            path = "/Photos",
            itemsCount = 10,
            createdAt = Instant.now(),
            modifiedAt = Instant.now(),
        )

    private val testMediaFile =
        MediaFile(
            id = "file1",
            name = "photo.jpg",
            path = "/Photos/photo.jpg",
            type = MediaType.IMAGE,
            mimeType = "image/jpeg",
            size = 1024L,
            createdAt = Instant.now(),
            modifiedAt = Instant.now(),
            previewUrl = "https://preview.url/photo.jpg",
            md5 = "abc123",
        )

    private val testItems =
        listOf(
            DiskItem.Directory(testFolder),
            DiskItem.File(testMediaFile),
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getFolderContentsUseCase = mockk()
        refreshFilesUseCase = mockk()
        getSettingsUseCase = mockk()
        saveSettingsUseCase = mockk()
        networkMonitor = mockk(relaxed = true)

        // Default mock behavior
        every { getSettingsUseCase.observeSettings() } returns flowOf(defaultSettings)
        every { networkMonitor.isOnline } returns flowOf(true)
        coEvery {
            getFolderContentsUseCase(
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns Result.success(PagedResult(testItems, 0, 20, 2, false))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): GalleryViewModel =
        GalleryViewModel(
            getFolderContentsUseCase = getFolderContentsUseCase,
            refreshFilesUseCase = refreshFilesUseCase,
            getSettingsUseCase = getSettingsUseCase,
            saveSettingsUseCase = saveSettingsUseCase,
            networkMonitor = networkMonitor,
        )

    @Nested
    @DisplayName("Initial State")
    inner class InitialState {
        @Test
        @DisplayName("should have default UI state on creation")
        fun shouldHaveDefaultUiStateOnCreation() =
            runTest {
                viewModel = createViewModel()

                val state = viewModel.uiState.value

                assertTrue(state.items.isEmpty())
                assertFalse(state.isLoading)
                assertFalse(state.isRefreshing)
                assertNull(state.currentPath)
                assertEquals(ViewMode.GRID, state.viewMode)
                assertEquals(SortOrder.DATE_DESC, state.sortOrder)
            }

        @Test
        @DisplayName("should load settings on creation")
        fun shouldLoadSettingsOnCreation() =
            runTest {
                val settings =
                    UserSettings(
                        publicFolderUrl = null,
                        rootFolderPath = "/MyPhotos",
                        isAuthenticated = true,
                        viewMode = ViewMode.LIST,
                        sortOrder = SortOrder.NAME_ASC,
                    )
                every { getSettingsUseCase.observeSettings() } returns flowOf(settings)

                viewModel = createViewModel()
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertEquals(ViewMode.LIST, state.viewMode)
                assertEquals(SortOrder.NAME_ASC, state.sortOrder)
            }
    }

    @Nested
    @DisplayName("Loading Content")
    inner class LoadingContent {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should load folder contents")
        fun shouldLoadFolderContents() =
            runTest {
                advanceUntilIdle()

                viewModel.onEvent(GalleryEvent.LoadContent)
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertEquals(2, state.items.size)
                assertFalse(state.isLoading)
            }

        @Test
        @DisplayName("should show loading state while fetching")
        fun shouldShowLoadingStateWhileFetching() =
            runTest {
                viewModel.uiState.test {
                    skipItems(1) // Skip initial state
                    viewModel.onEvent(GalleryEvent.LoadContent)

                    val loadingState = awaitItem()
                    assertTrue(loadingState.isLoading)

                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        @DisplayName("should handle error when loading fails")
        fun shouldHandleErrorWhenLoadingFails() =
            runTest {
                coEvery {
                    getFolderContentsUseCase(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns Result.failure(RuntimeException("Network error"))

                advanceUntilIdle()

                viewModel.onEvent(GalleryEvent.LoadContent)
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertFalse(state.isLoading)
                assertTrue(state.error != null)
            }
    }

    @Nested
    @DisplayName("Refresh")
    inner class Refresh {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should refresh content")
        fun shouldRefreshContent() =
            runTest {
                coEvery { refreshFilesUseCase(any()) } returns Result.success(Unit)
                advanceUntilIdle()

                viewModel.onEvent(GalleryEvent.Refresh)
                advanceUntilIdle()

                coVerify { refreshFilesUseCase(any()) }
                assertFalse(viewModel.uiState.value.isRefreshing)
            }

        @Test
        @DisplayName("should show refreshing state")
        fun shouldShowRefreshingState() =
            runTest {
                coEvery { refreshFilesUseCase(any()) } returns Result.success(Unit)
                advanceUntilIdle()

                viewModel.uiState.test {
                    skipItems(1)
                    viewModel.onEvent(GalleryEvent.Refresh)

                    val refreshingState = awaitItem()
                    assertTrue(refreshingState.isRefreshing)

                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Pagination")
    inner class Pagination {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should load more items")
        fun shouldLoadMoreItems() =
            runTest {
                // First load
                coEvery {
                    getFolderContentsUseCase(
                        any(),
                        eq(0),
                        any(),
                        any(),
                        any(),
                    )
                } returns Result.success(PagedResult(testItems, 0, 20, 2, true))

                advanceUntilIdle()
                viewModel.onEvent(GalleryEvent.LoadContent)
                advanceUntilIdle()

                // Load more
                val moreItems =
                    listOf(
                        DiskItem.File(testMediaFile.copy(id = "file2", name = "photo2.jpg")),
                    )
                coEvery {
                    getFolderContentsUseCase(
                        any(),
                        eq(2),
                        any(),
                        any(),
                        any(),
                    )
                } returns Result.success(PagedResult(moreItems, 2, 20, 3, false))

                viewModel.onEvent(GalleryEvent.LoadMore)
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertEquals(3, state.items.size)
                assertFalse(state.hasMoreItems)
            }

        @Test
        @DisplayName("should not load more when already loading")
        fun shouldNotLoadMoreWhenAlreadyLoading() =
            runTest {
                coEvery {
                    getFolderContentsUseCase(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns Result.success(PagedResult(testItems, 0, 20, 2, true))

                advanceUntilIdle()
                viewModel.onEvent(GalleryEvent.LoadContent)
                // Don't wait for completion

                viewModel.onEvent(GalleryEvent.LoadMore)
                viewModel.onEvent(GalleryEvent.LoadMore)
                advanceUntilIdle()

                // Should only call once for initial load (LoadMore is ignored while loading)
                coVerify(atMost = 2) { getFolderContentsUseCase(any(), any(), any(), any(), any()) }
            }
    }

    @Nested
    @DisplayName("Navigation")
    inner class Navigation {
        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        @DisplayName("should navigate to folder")
        fun shouldNavigateToFolder() =
            runTest {
                advanceUntilIdle()

                viewModel.onEvent(GalleryEvent.NavigateToFolder("/Photos"))
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertEquals("/Photos", state.currentPath)
            }

        @Test
        @DisplayName("should update breadcrumbs when navigating")
        fun shouldUpdateBreadcrumbsWhenNavigating() =
            runTest {
                advanceUntilIdle()

                viewModel.onEvent(GalleryEvent.NavigateToFolder("/Photos"))
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertEquals(2, state.breadcrumbs.size)
                assertEquals("Root", state.breadcrumbs[0].name)
                assertEquals("Photos", state.breadcrumbs[1].name)
            }

        @Test
        @DisplayName("should navigate to breadcrumb")
        fun shouldNavigateToBreadcrumb() =
            runTest {
                advanceUntilIdle()

                // Navigate deep
                viewModel.onEvent(GalleryEvent.NavigateToFolder("/Photos/2024"))
                advanceUntilIdle()

                // Navigate back to Photos
                viewModel.onEvent(GalleryEvent.NavigateToPath("/Photos"))
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertEquals("/Photos", state.currentPath)
            }

        @Test
        @DisplayName("should emit navigation event for media file")
        fun shouldEmitNavigationEventForMediaFile() =
            runTest {
                advanceUntilIdle()
                viewModel.onEvent(GalleryEvent.LoadContent)
                advanceUntilIdle()

                viewModel.navigationEvents.test {
                    viewModel.onEvent(GalleryEvent.OpenMedia(DiskItem.File(testMediaFile)))

                    val event = awaitItem()
                    assertTrue(event is GalleryNavigationEvent.NavigateToImageViewer)
                }
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
                coEvery { saveSettingsUseCase.setViewMode(ViewMode.LIST) } returns Result.success(Unit)
                advanceUntilIdle()

                viewModel.onEvent(GalleryEvent.SetViewMode(ViewMode.LIST))
                advanceUntilIdle()

                coVerify { saveSettingsUseCase.setViewMode(ViewMode.LIST) }
            }

        @Test
        @DisplayName("should change sort order and reload")
        fun shouldChangeSortOrderAndReload() =
            runTest {
                coEvery { saveSettingsUseCase.setSortOrder(SortOrder.NAME_ASC) } returns Result.success(Unit)
                advanceUntilIdle()

                viewModel.onEvent(GalleryEvent.SetSortOrder(SortOrder.NAME_ASC))
                advanceUntilIdle()

                coVerify { saveSettingsUseCase.setSortOrder(SortOrder.NAME_ASC) }
                // Should reload with new sort order
                coVerify(atLeast = 1) { getFolderContentsUseCase(any(), any(), any(), any(), any()) }
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
        @DisplayName("should clear error")
        fun shouldClearError() =
            runTest {
                coEvery {
                    getFolderContentsUseCase(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns Result.failure(RuntimeException("Error"))

                advanceUntilIdle()
                viewModel.onEvent(GalleryEvent.LoadContent)
                advanceUntilIdle()

                assertTrue(viewModel.uiState.value.error != null)

                viewModel.onEvent(GalleryEvent.ClearError)

                assertNull(viewModel.uiState.value.error)
            }

        @Test
        @DisplayName("should retry on error")
        fun shouldRetryOnError() =
            runTest {
                coEvery {
                    getFolderContentsUseCase(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns Result.failure(RuntimeException("Error"))

                advanceUntilIdle()
                viewModel.onEvent(GalleryEvent.LoadContent)
                advanceUntilIdle()

                // Fix the error
                coEvery {
                    getFolderContentsUseCase(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns Result.success(PagedResult(testItems, 0, 20, 2, false))

                viewModel.onEvent(GalleryEvent.Retry)
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertNull(state.error)
                assertEquals(2, state.items.size)
            }
    }
}
