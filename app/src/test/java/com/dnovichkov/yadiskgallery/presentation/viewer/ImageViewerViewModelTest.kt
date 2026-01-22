package com.dnovichkov.yadiskgallery.presentation.viewer

import app.cash.turbine.test
import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.domain.model.MediaType
import com.dnovichkov.yadiskgallery.domain.model.PagedResult
import com.dnovichkov.yadiskgallery.domain.usecase.files.GetFolderContentsUseCase
import com.dnovichkov.yadiskgallery.domain.usecase.files.GetMediaDownloadUrlUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ImageViewerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getFolderContentsUseCase: GetFolderContentsUseCase
    private lateinit var getMediaDownloadUrlUseCase: GetMediaDownloadUrlUseCase
    private lateinit var viewModel: ImageViewerViewModel

    private val testImages =
        listOf(
            createTestMediaFile("1", "image1.jpg", "/photos/image1.jpg"),
            createTestMediaFile("2", "image2.jpg", "/photos/image2.jpg"),
            createTestMediaFile("3", "image3.jpg", "/photos/image3.jpg"),
        )

    private val testDiskItems = testImages.map { DiskItem.File(it) }

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getFolderContentsUseCase = mockk()
        getMediaDownloadUrlUseCase = mockk()
        viewModel =
            ImageViewerViewModel(
                getFolderContentsUseCase = getFolderContentsUseCase,
                getMediaDownloadUrlUseCase = getMediaDownloadUrlUseCase,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("LoadImages")
    inner class LoadImagesTests {
        @Test
        fun `should load images from folder and set initial index`() =
            runTest {
                coEvery {
                    getFolderContentsUseCase(
                        path = "/photos",
                        offset = 0,
                        limit = any(),
                        sortOrder = any(),
                        mediaOnly = true,
                    )
                } returns
                    Result.success(
                        PagedResult(
                            items = testDiskItems,
                            offset = 0,
                            limit = 1000,
                            total = testDiskItems.size,
                            hasMore = false,
                        ),
                    )

                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertTrue(initial.images.isEmpty())

                    viewModel.onEvent(ImageViewerEvent.LoadImages("/photos", 1))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val loading = awaitItem()
                    assertTrue(loading.isLoading)

                    val loaded = awaitItem()
                    assertFalse(loaded.isLoading)
                    assertEquals(3, loaded.images.size)
                    assertEquals(1, loaded.currentIndex)
                    assertEquals("image2.jpg", loaded.currentImage?.name)
                }
            }

        @Test
        fun `should handle error when loading images fails`() =
            runTest {
                coEvery {
                    getFolderContentsUseCase(
                        path = any(),
                        offset = any(),
                        limit = any(),
                        sortOrder = any(),
                        mediaOnly = true,
                    )
                } returns Result.failure(Exception("Network error"))

                viewModel.uiState.test {
                    awaitItem() // initial

                    viewModel.onEvent(ImageViewerEvent.LoadImages("/photos", 0))
                    testDispatcher.scheduler.advanceUntilIdle()

                    awaitItem() // loading
                    val error = awaitItem()

                    assertFalse(error.isLoading)
                    assertNotNull(error.error)
                    assertTrue(error.error!!.contains("Network error"))
                }
            }

        @Test
        fun `should filter only image files`() =
            runTest {
                val mixedItems =
                    listOf(
                        DiskItem.File(createTestMediaFile("1", "image.jpg", "/image.jpg", MediaType.IMAGE)),
                        DiskItem.File(createTestMediaFile("2", "video.mp4", "/video.mp4", MediaType.VIDEO)),
                    )

                coEvery {
                    getFolderContentsUseCase(
                        path = any(),
                        offset = any(),
                        limit = any(),
                        sortOrder = any(),
                        mediaOnly = true,
                    )
                } returns
                    Result.success(
                        PagedResult(
                            items = mixedItems,
                            offset = 0,
                            limit = 1000,
                            total = mixedItems.size,
                            hasMore = false,
                        ),
                    )

                viewModel.uiState.test {
                    awaitItem()
                    viewModel.onEvent(ImageViewerEvent.LoadImages("/", 0))
                    testDispatcher.scheduler.advanceUntilIdle()

                    awaitItem() // loading
                    val loaded = awaitItem()

                    // Should only contain images, not videos
                    assertEquals(1, loaded.images.size)
                    assertEquals(MediaType.IMAGE, loaded.images[0].type)
                }
            }
    }

    @Nested
    @DisplayName("Navigation")
    inner class NavigationTests {
        @BeforeEach
        fun loadImages() =
            runTest {
                coEvery {
                    getFolderContentsUseCase(
                        path = any(),
                        offset = any(),
                        limit = any(),
                        sortOrder = any(),
                        mediaOnly = true,
                    )
                } returns
                    Result.success(
                        PagedResult(
                            items = testDiskItems,
                            offset = 0,
                            limit = 1000,
                            total = testDiskItems.size,
                            hasMore = false,
                        ),
                    )

                viewModel.onEvent(ImageViewerEvent.LoadImages("/photos", 0))
                testDispatcher.scheduler.advanceUntilIdle()
            }

        @Test
        fun `should navigate to next image`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertEquals(0, initial.currentIndex)
                    assertTrue(initial.hasNextImage)

                    viewModel.onEvent(ImageViewerEvent.NextImage)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val next = awaitItem()
                    assertEquals(1, next.currentIndex)
                    assertEquals("image2.jpg", next.currentImage?.name)
                }
            }

        @Test
        fun `should navigate to previous image`() =
            runTest {
                viewModel.onEvent(ImageViewerEvent.GoToImage(2))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertEquals(2, initial.currentIndex)
                    assertTrue(initial.hasPreviousImage)

                    viewModel.onEvent(ImageViewerEvent.PreviousImage)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val prev = awaitItem()
                    assertEquals(1, prev.currentIndex)
                }
            }

        @Test
        fun `should not navigate past last image`() =
            runTest {
                viewModel.onEvent(ImageViewerEvent.GoToImage(2))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertEquals(2, initial.currentIndex)
                    assertFalse(initial.hasNextImage)

                    viewModel.onEvent(ImageViewerEvent.NextImage)
                    testDispatcher.scheduler.advanceUntilIdle()

                    // Should stay at same index
                    expectNoEvents()
                }
            }

        @Test
        fun `should not navigate before first image`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertEquals(0, initial.currentIndex)
                    assertFalse(initial.hasPreviousImage)

                    viewModel.onEvent(ImageViewerEvent.PreviousImage)
                    testDispatcher.scheduler.advanceUntilIdle()

                    // Should stay at same index
                    expectNoEvents()
                }
            }

        @Test
        fun `should go to specific image by index`() =
            runTest {
                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(ImageViewerEvent.GoToImage(2))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val result = awaitItem()
                    assertEquals(2, result.currentIndex)
                    assertEquals("image3.jpg", result.currentImage?.name)
                }
            }
    }

    @Nested
    @DisplayName("Zoom")
    inner class ZoomTests {
        @Test
        fun `should update zoom level`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertEquals(1f, initial.zoomLevel)

                    viewModel.onEvent(ImageViewerEvent.SetZoomLevel(2.5f))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val zoomed = awaitItem()
                    assertEquals(2.5f, zoomed.zoomLevel)
                }
            }

        @Test
        fun `should clamp zoom level to max`() =
            runTest {
                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(ImageViewerEvent.SetZoomLevel(10f))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val zoomed = awaitItem()
                    assertEquals(ImageViewerUiState.MAX_ZOOM, zoomed.zoomLevel)
                }
            }

        @Test
        fun `should clamp zoom level to min`() =
            runTest {
                viewModel.uiState.test {
                    awaitItem()

                    // First set zoom to 2x
                    viewModel.onEvent(ImageViewerEvent.SetZoomLevel(2f))
                    testDispatcher.scheduler.advanceUntilIdle()
                    val zoomed = awaitItem()
                    assertEquals(2f, zoomed.zoomLevel)

                    // Then try to set below min - should clamp to MIN_ZOOM
                    viewModel.onEvent(ImageViewerEvent.SetZoomLevel(0.5f))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val clamped = awaitItem()
                    assertEquals(ImageViewerUiState.MIN_ZOOM, clamped.zoomLevel)
                }
            }

        @Test
        fun `should toggle double tap zoom`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertEquals(1f, initial.zoomLevel)

                    viewModel.onEvent(ImageViewerEvent.ToggleDoubleTapZoom)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val zoomed = awaitItem()
                    assertEquals(ImageViewerUiState.DOUBLE_TAP_ZOOM, zoomed.zoomLevel)

                    viewModel.onEvent(ImageViewerEvent.ToggleDoubleTapZoom)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val unzoomed = awaitItem()
                    assertEquals(ImageViewerUiState.MIN_ZOOM, unzoomed.zoomLevel)
                }
            }
    }

    @Nested
    @DisplayName("Original Image Loading")
    inner class OriginalImageTests {
        @BeforeEach
        fun loadImages() =
            runTest {
                coEvery {
                    getFolderContentsUseCase(
                        path = any(),
                        offset = any(),
                        limit = any(),
                        sortOrder = any(),
                        mediaOnly = true,
                    )
                } returns
                    Result.success(
                        PagedResult(
                            items = testDiskItems,
                            offset = 0,
                            limit = 1000,
                            total = testDiskItems.size,
                            hasMore = false,
                        ),
                    )

                viewModel.onEvent(ImageViewerEvent.LoadImages("/photos", 0))
                testDispatcher.scheduler.advanceUntilIdle()
            }

        @Test
        fun `should load original image URL`() =
            runTest {
                coEvery {
                    getMediaDownloadUrlUseCase.invoke("/photos/image1.jpg")
                } returns Result.success("https://download.url/original.jpg")

                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertNull(initial.originalImageUrl)

                    viewModel.onEvent(ImageViewerEvent.LoadOriginalImage)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val loading = awaitItem()
                    assertTrue(loading.isLoadingOriginal)

                    val loaded = awaitItem()
                    assertFalse(loaded.isLoadingOriginal)
                    assertEquals("https://download.url/original.jpg", loaded.originalImageUrl)
                }
            }

        @Test
        fun `shouldLoadOriginal returns true when zoom exceeds threshold`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertFalse(initial.shouldLoadOriginal)

                    viewModel.onEvent(ImageViewerEvent.SetZoomLevel(2.5f))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val zoomed = awaitItem()
                    assertTrue(zoomed.shouldLoadOriginal)
                }
            }
    }

    @Nested
    @DisplayName("Controls and UI")
    inner class ControlsTests {
        @Test
        fun `should toggle controls visibility`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertTrue(initial.showControls)

                    viewModel.onEvent(ImageViewerEvent.ToggleControls)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val hidden = awaitItem()
                    assertFalse(hidden.showControls)

                    viewModel.onEvent(ImageViewerEvent.ToggleControls)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val shown = awaitItem()
                    assertTrue(shown.showControls)
                }
            }

        @Test
        fun `should show and hide EXIF sheet`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertFalse(initial.showExifSheet)

                    viewModel.onEvent(ImageViewerEvent.ShowExifInfo)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val shown = awaitItem()
                    assertTrue(shown.showExifSheet)

                    viewModel.onEvent(ImageViewerEvent.HideExifInfo)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val hidden = awaitItem()
                    assertFalse(hidden.showExifSheet)
                }
            }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandlingTests {
        @Test
        fun `should clear error`() =
            runTest {
                coEvery {
                    getFolderContentsUseCase(
                        path = any(),
                        offset = any(),
                        limit = any(),
                        sortOrder = any(),
                        mediaOnly = true,
                    )
                } returns Result.failure(Exception("Error"))

                viewModel.onEvent(ImageViewerEvent.LoadImages("/photos", 0))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    val withError = awaitItem()
                    assertNotNull(withError.error)

                    viewModel.onEvent(ImageViewerEvent.ClearError)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val cleared = awaitItem()
                    assertNull(cleared.error)
                }
            }

        @Test
        fun `should retry loading after error`() =
            runTest {
                var callCount = 0
                coEvery {
                    getFolderContentsUseCase(
                        path = any(),
                        offset = any(),
                        limit = any(),
                        sortOrder = any(),
                        mediaOnly = true,
                    )
                } answers {
                    callCount++
                    if (callCount == 1) {
                        Result.failure(Exception("First call fails"))
                    } else {
                        Result.success(
                            PagedResult(
                                items = testDiskItems,
                                offset = 0,
                                limit = 1000,
                                total = testDiskItems.size,
                                hasMore = false,
                            ),
                        )
                    }
                }

                viewModel.uiState.test {
                    awaitItem() // initial

                    viewModel.onEvent(ImageViewerEvent.LoadImages("/photos", 0))
                    testDispatcher.scheduler.advanceUntilIdle()

                    awaitItem() // loading
                    val withError = awaitItem()
                    assertNotNull(withError.error)

                    viewModel.onEvent(ImageViewerEvent.Retry)
                    testDispatcher.scheduler.advanceUntilIdle()

                    awaitItem() // error cleared
                    awaitItem() // loading
                    val success = awaitItem()
                    assertNull(success.error)
                    assertEquals(3, success.images.size)
                }
            }
    }

    private fun createTestMediaFile(
        id: String,
        name: String,
        path: String,
        type: MediaType = MediaType.IMAGE,
    ): MediaFile {
        return MediaFile(
            id = id,
            name = name,
            path = path,
            type = type,
            mimeType = if (type == MediaType.IMAGE) "image/jpeg" else "video/mp4",
            size = 1024L,
            createdAt = Instant.now(),
            modifiedAt = Instant.now(),
            previewUrl = "https://preview.url/$name",
            md5 = "abc123",
        )
    }
}
