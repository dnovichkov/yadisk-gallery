package com.dnovichkov.yadiskgallery.presentation.viewer

import app.cash.turbine.test
import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.domain.model.MediaType
import com.dnovichkov.yadiskgallery.domain.repository.IFilesRepository
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
class VideoPlayerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var filesRepository: IFilesRepository
    private lateinit var getMediaDownloadUrlUseCase: GetMediaDownloadUrlUseCase
    private lateinit var viewModel: VideoPlayerViewModel

    private val testVideoFile =
        MediaFile(
            id = "video1",
            name = "test_video.mp4",
            path = "/videos/test_video.mp4",
            type = MediaType.VIDEO,
            mimeType = "video/mp4",
            size = 10_000_000L,
            createdAt = Instant.now(),
            modifiedAt = Instant.now(),
            previewUrl = "https://preview.url/test_video.jpg",
            md5 = "abc123",
        )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        filesRepository = mockk()
        getMediaDownloadUrlUseCase = GetMediaDownloadUrlUseCase(filesRepository)
        viewModel = VideoPlayerViewModel(getMediaDownloadUrlUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("LoadVideo")
    inner class LoadVideoTests {
        @Test
        fun `should load video URL successfully`() =
            runTest {
                coEvery {
                    filesRepository.getDownloadUrl("/videos/test_video.mp4")
                } returns Result.success("https://download.url/test_video.mp4")

                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertNull(initial.videoUrl)
                    assertFalse(initial.isLoading)

                    viewModel.onEvent(VideoPlayerEvent.LoadVideo("/videos/test_video.mp4"))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val loading = awaitItem()
                    assertTrue(loading.isLoading)

                    val loaded = awaitItem()
                    assertFalse(loaded.isLoading)
                    assertEquals("https://download.url/test_video.mp4", loaded.videoUrl)
                    assertNull(loaded.error)
                }
            }

        @Test
        fun `should handle error when loading video fails`() =
            runTest {
                coEvery {
                    filesRepository.getDownloadUrl(any())
                } returns Result.failure(Exception("Network error"))

                viewModel.uiState.test {
                    awaitItem() // initial

                    viewModel.onEvent(VideoPlayerEvent.LoadVideo("/videos/test_video.mp4"))
                    testDispatcher.scheduler.advanceUntilIdle()

                    awaitItem() // loading
                    val error = awaitItem()

                    assertFalse(error.isLoading)
                    assertNotNull(error.error)
                    assertTrue(error.error!!.contains("Network error"))
                }
            }
    }

    @Nested
    @DisplayName("Playback Controls")
    inner class PlaybackControlsTests {
        @Test
        fun `should toggle play pause`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertFalse(initial.isPlaying)

                    viewModel.onEvent(VideoPlayerEvent.TogglePlayPause)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val playing = awaitItem()
                    assertTrue(playing.isPlaying)

                    viewModel.onEvent(VideoPlayerEvent.TogglePlayPause)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val paused = awaitItem()
                    assertFalse(paused.isPlaying)
                }
            }

        @Test
        fun `should play video`() =
            runTest {
                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(VideoPlayerEvent.Play)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val playing = awaitItem()
                    assertTrue(playing.isPlaying)
                }
            }

        @Test
        fun `should pause video`() =
            runTest {
                viewModel.onEvent(VideoPlayerEvent.Play)
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    val playing = awaitItem()
                    assertTrue(playing.isPlaying)

                    viewModel.onEvent(VideoPlayerEvent.Pause)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val paused = awaitItem()
                    assertFalse(paused.isPlaying)
                }
            }
    }

    @Nested
    @DisplayName("Seek")
    inner class SeekTests {
        @BeforeEach
        fun setupWithDuration() =
            runTest {
                viewModel.onEvent(VideoPlayerEvent.UpdateDuration(60_000L))
                testDispatcher.scheduler.advanceUntilIdle()
            }

        @Test
        fun `should seek to specific position`() =
            runTest {
                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(VideoPlayerEvent.SeekTo(30_000L))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val seeked = awaitItem()
                    assertEquals(30_000L, seeked.currentPosition)
                }
            }

        @Test
        fun `should seek forward by increment`() =
            runTest {
                viewModel.onEvent(VideoPlayerEvent.UpdatePosition(20_000L))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(VideoPlayerEvent.SeekForward)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val seeked = awaitItem()
                    assertEquals(30_000L, seeked.currentPosition)
                }
            }

        @Test
        fun `should seek backward by increment`() =
            runTest {
                viewModel.onEvent(VideoPlayerEvent.UpdatePosition(30_000L))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(VideoPlayerEvent.SeekBackward)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val seeked = awaitItem()
                    assertEquals(20_000L, seeked.currentPosition)
                }
            }

        @Test
        fun `should not seek before start`() =
            runTest {
                viewModel.onEvent(VideoPlayerEvent.UpdatePosition(5_000L))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(VideoPlayerEvent.SeekBackward)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val seeked = awaitItem()
                    assertEquals(0L, seeked.currentPosition)
                }
            }

        @Test
        fun `should not seek past duration`() =
            runTest {
                viewModel.onEvent(VideoPlayerEvent.UpdatePosition(55_000L))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(VideoPlayerEvent.SeekForward)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val seeked = awaitItem()
                    assertEquals(60_000L, seeked.currentPosition)
                }
            }
    }

    @Nested
    @DisplayName("Position and Duration Updates")
    inner class PositionDurationTests {
        @Test
        fun `should update current position`() =
            runTest {
                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(VideoPlayerEvent.UpdatePosition(15_000L))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val updated = awaitItem()
                    assertEquals(15_000L, updated.currentPosition)
                }
            }

        @Test
        fun `should update duration`() =
            runTest {
                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(VideoPlayerEvent.UpdateDuration(120_000L))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val updated = awaitItem()
                    assertEquals(120_000L, updated.duration)
                }
            }

        @Test
        fun `should update buffered position`() =
            runTest {
                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(VideoPlayerEvent.UpdateBufferedPosition(45_000L))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val updated = awaitItem()
                    assertEquals(45_000L, updated.bufferedPosition)
                }
            }

        @Test
        fun `should calculate progress correctly`() =
            runTest {
                viewModel.onEvent(VideoPlayerEvent.UpdateDuration(100_000L))
                testDispatcher.scheduler.advanceUntilIdle()
                viewModel.onEvent(VideoPlayerEvent.UpdatePosition(50_000L))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    val state = awaitItem()
                    assertEquals(0.5f, state.progress, 0.01f)
                }
            }

        @Test
        fun `should calculate remaining time correctly`() =
            runTest {
                viewModel.onEvent(VideoPlayerEvent.UpdateDuration(100_000L))
                testDispatcher.scheduler.advanceUntilIdle()
                viewModel.onEvent(VideoPlayerEvent.UpdatePosition(30_000L))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    val state = awaitItem()
                    assertEquals(70_000L, state.remainingTime)
                }
            }
    }

    @Nested
    @DisplayName("Buffering State")
    inner class BufferingTests {
        @Test
        fun `should set buffering state`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertFalse(initial.isBuffering)

                    viewModel.onEvent(VideoPlayerEvent.SetBuffering(true))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val buffering = awaitItem()
                    assertTrue(buffering.isBuffering)

                    viewModel.onEvent(VideoPlayerEvent.SetBuffering(false))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val notBuffering = awaitItem()
                    assertFalse(notBuffering.isBuffering)
                }
            }

        @Test
        fun `should set playing state`() =
            runTest {
                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(VideoPlayerEvent.SetPlaying(true))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val playing = awaitItem()
                    assertTrue(playing.isPlaying)

                    viewModel.onEvent(VideoPlayerEvent.SetPlaying(false))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val notPlaying = awaitItem()
                    assertFalse(notPlaying.isPlaying)
                }
            }
    }

    @Nested
    @DisplayName("Controls Visibility")
    inner class ControlsTests {
        @Test
        fun `should toggle controls visibility`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertTrue(initial.showControls)

                    viewModel.onEvent(VideoPlayerEvent.ToggleControls)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val hidden = awaitItem()
                    assertFalse(hidden.showControls)

                    viewModel.onEvent(VideoPlayerEvent.ToggleControls)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val shown = awaitItem()
                    assertTrue(shown.showControls)
                }
            }

        @Test
        fun `should show controls`() =
            runTest {
                viewModel.onEvent(VideoPlayerEvent.HideControls)
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    val hidden = awaitItem()
                    assertFalse(hidden.showControls)

                    viewModel.onEvent(VideoPlayerEvent.ShowControls)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val shown = awaitItem()
                    assertTrue(shown.showControls)
                }
            }

        @Test
        fun `should hide controls`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertTrue(initial.showControls)

                    viewModel.onEvent(VideoPlayerEvent.HideControls)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val hidden = awaitItem()
                    assertFalse(hidden.showControls)
                }
            }
    }

    @Nested
    @DisplayName("Playback Speed and Volume")
    inner class SpeedVolumeTests {
        @Test
        fun `should set playback speed`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertEquals(1f, initial.playbackSpeed)

                    viewModel.onEvent(VideoPlayerEvent.SetPlaybackSpeed(1.5f))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val updated = awaitItem()
                    assertEquals(1.5f, updated.playbackSpeed)
                }
            }

        @Test
        fun `should set volume`() =
            runTest {
                viewModel.uiState.test {
                    val initial = awaitItem()
                    assertEquals(1f, initial.volume)

                    viewModel.onEvent(VideoPlayerEvent.SetVolume(0.5f))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val updated = awaitItem()
                    assertEquals(0.5f, updated.volume)
                }
            }

        @Test
        fun `should clamp volume to valid range`() =
            runTest {
                // First lower the volume so we can test clamping up
                viewModel.onEvent(VideoPlayerEvent.SetVolume(0.5f))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    val lowered = awaitItem()
                    assertEquals(0.5f, lowered.volume)

                    viewModel.onEvent(VideoPlayerEvent.SetVolume(1.5f))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val clamped = awaitItem()
                    assertEquals(1f, clamped.volume)
                }
            }

        @Test
        fun `should clamp volume to minimum`() =
            runTest {
                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(VideoPlayerEvent.SetVolume(-0.5f))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val clamped = awaitItem()
                    assertEquals(0f, clamped.volume)
                }
            }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandlingTests {
        @Test
        fun `should handle playback error`() =
            runTest {
                viewModel.uiState.test {
                    awaitItem()

                    viewModel.onEvent(VideoPlayerEvent.OnError("Playback failed"))
                    testDispatcher.scheduler.advanceUntilIdle()

                    val withError = awaitItem()
                    assertEquals("Playback failed", withError.error)
                    assertTrue(withError.hasError)
                }
            }

        @Test
        fun `should clear error`() =
            runTest {
                viewModel.onEvent(VideoPlayerEvent.OnError("Some error"))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    val withError = awaitItem()
                    assertNotNull(withError.error)

                    viewModel.onEvent(VideoPlayerEvent.ClearError)
                    testDispatcher.scheduler.advanceUntilIdle()

                    val cleared = awaitItem()
                    assertNull(cleared.error)
                    assertFalse(cleared.hasError)
                }
            }

        @Test
        fun `should retry loading video after error`() =
            runTest {
                var callCount = 0
                coEvery {
                    filesRepository.getDownloadUrl("/videos/test_video.mp4")
                } answers {
                    callCount++
                    if (callCount == 1) {
                        Result.failure(Exception("First call fails"))
                    } else {
                        Result.success("https://download.url/test_video.mp4")
                    }
                }

                viewModel.uiState.test {
                    awaitItem() // initial

                    viewModel.onEvent(VideoPlayerEvent.LoadVideo("/videos/test_video.mp4"))
                    testDispatcher.scheduler.advanceUntilIdle()

                    awaitItem() // loading
                    val withError = awaitItem()
                    assertNotNull(withError.error)

                    viewModel.onEvent(VideoPlayerEvent.Retry)
                    testDispatcher.scheduler.advanceUntilIdle()

                    awaitItem() // error cleared
                    awaitItem() // loading
                    val success = awaitItem()
                    assertNull(success.error)
                    assertEquals("https://download.url/test_video.mp4", success.videoUrl)
                }
            }
    }

    @Nested
    @DisplayName("UiState Computed Properties")
    inner class UiStateTests {
        @Test
        fun `canPlay should be true when video URL is set and no error`() =
            runTest {
                coEvery {
                    filesRepository.getDownloadUrl(any())
                } returns Result.success("https://download.url/test.mp4")

                viewModel.onEvent(VideoPlayerEvent.LoadVideo("/test.mp4"))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    val state = awaitItem()
                    assertTrue(state.canPlay)
                }
            }

        @Test
        fun `canPlay should be false when there is an error`() =
            runTest {
                coEvery {
                    filesRepository.getDownloadUrl(any())
                } returns Result.success("https://download.url/test.mp4")

                viewModel.onEvent(VideoPlayerEvent.LoadVideo("/test.mp4"))
                testDispatcher.scheduler.advanceUntilIdle()
                viewModel.onEvent(VideoPlayerEvent.OnError("Error"))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    val state = awaitItem()
                    assertFalse(state.canPlay)
                }
            }

        @Test
        fun `bufferedProgress should calculate correctly`() =
            runTest {
                viewModel.onEvent(VideoPlayerEvent.UpdateDuration(100_000L))
                testDispatcher.scheduler.advanceUntilIdle()
                viewModel.onEvent(VideoPlayerEvent.UpdateBufferedPosition(75_000L))
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.uiState.test {
                    val state = awaitItem()
                    assertEquals(0.75f, state.bufferedProgress, 0.01f)
                }
            }
    }
}
