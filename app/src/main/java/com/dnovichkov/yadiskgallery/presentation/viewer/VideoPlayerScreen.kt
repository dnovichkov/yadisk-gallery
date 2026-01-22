package com.dnovichkov.yadiskgallery.presentation.viewer

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.dnovichkov.yadiskgallery.presentation.viewer.components.VideoBufferingOverlay
import com.dnovichkov.yadiskgallery.presentation.viewer.components.VideoControls
import com.dnovichkov.yadiskgallery.presentation.viewer.components.VideoErrorOverlay
import com.dnovichkov.yadiskgallery.presentation.viewer.components.VideoLoadingOverlay
import com.dnovichkov.yadiskgallery.presentation.viewer.components.VideoPlayer
import kotlinx.coroutines.delay

/**
 * Full-screen video player screen with custom controls.
 *
 * @param filePath Path to the video file
 * @param onNavigateBack Callback to navigate back
 * @param viewModel ViewModel for the screen
 */
@Composable
fun VideoPlayerScreen(
    filePath: String,
    onNavigateBack: () -> Unit,
    viewModel: VideoPlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var seekPosition by remember { mutableLongStateOf(-1L) }

    // Load video on first composition
    LaunchedEffect(filePath) {
        viewModel.onEvent(VideoPlayerEvent.LoadVideo(filePath))
    }

    // Setup immersive mode
    VideoImmersiveMode(enabled = !uiState.showControls)

    // Auto-hide controls after delay
    LaunchedEffect(uiState.showControls, uiState.isPlaying) {
        if (uiState.showControls && uiState.isPlaying) {
            delay(VideoPlayerUiState.CONTROLS_HIDE_DELAY_MS)
            viewModel.onEvent(VideoPlayerEvent.HideControls)
        }
    }

    // Handle back press
    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        containerColor = Color.Black,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.onEvent(VideoPlayerEvent.ToggleControls) },
                    ),
        ) {
            when {
                uiState.isLoading && uiState.videoUrl == null -> {
                    VideoLoadingOverlay(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                uiState.hasError && uiState.videoUrl == null -> {
                    VideoErrorOverlay(
                        errorMessage = uiState.error ?: "Failed to load video",
                        onRetry = { viewModel.onEvent(VideoPlayerEvent.Retry) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                uiState.videoUrl != null -> {
                    VideoPlayer(
                        videoUrl = uiState.videoUrl,
                        isPlaying = uiState.isPlaying,
                        playbackSpeed = uiState.playbackSpeed,
                        volume = uiState.volume,
                        onPlaybackStateChange = { isPlaying ->
                            viewModel.onEvent(VideoPlayerEvent.SetPlaying(isPlaying))
                        },
                        onPositionChange = { position ->
                            viewModel.onEvent(VideoPlayerEvent.UpdatePosition(position))
                        },
                        onDurationChange = { duration ->
                            viewModel.onEvent(VideoPlayerEvent.UpdateDuration(duration))
                        },
                        onBufferedPositionChange = { buffered ->
                            viewModel.onEvent(VideoPlayerEvent.UpdateBufferedPosition(buffered))
                        },
                        onBufferingChange = { isBuffering ->
                            viewModel.onEvent(VideoPlayerEvent.SetBuffering(isBuffering))
                        },
                        onError = { error ->
                            viewModel.onEvent(VideoPlayerEvent.OnError(error))
                        },
                        onPlayerClick = {
                            viewModel.onEvent(VideoPlayerEvent.ToggleControls)
                        },
                        seekToPosition = if (seekPosition >= 0) seekPosition else null,
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Buffering overlay
                    if (uiState.isBuffering && !uiState.isLoading) {
                        VideoBufferingOverlay()
                    }

                    // Error overlay during playback
                    if (uiState.hasError) {
                        VideoErrorOverlay(
                            errorMessage = uiState.error ?: "Playback error",
                            onRetry = { viewModel.onEvent(VideoPlayerEvent.Retry) },
                        )
                    }

                    // Video controls
                    VideoControls(
                        title = extractFileName(filePath),
                        isVisible = uiState.showControls,
                        isPlaying = uiState.isPlaying,
                        currentPosition = uiState.currentPosition,
                        duration = uiState.duration,
                        bufferedPosition = uiState.bufferedPosition,
                        onPlayPauseClick = {
                            viewModel.onEvent(VideoPlayerEvent.TogglePlayPause)
                        },
                        onSeekForward = {
                            viewModel.onEvent(VideoPlayerEvent.SeekForward)
                            seekPosition = uiState.currentPosition + VideoPlayerUiState.SEEK_INCREMENT_MS
                        },
                        onSeekBackward = {
                            viewModel.onEvent(VideoPlayerEvent.SeekBackward)
                            seekPosition =
                                (uiState.currentPosition - VideoPlayerUiState.SEEK_INCREMENT_MS)
                                    .coerceAtLeast(0L)
                        },
                        onSeekTo = { position ->
                            viewModel.onEvent(VideoPlayerEvent.SeekTo(position))
                            seekPosition = position
                        },
                        onBackClick = onNavigateBack,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

/**
 * Controls immersive/fullscreen mode for video playback.
 */
@Composable
private fun VideoImmersiveMode(enabled: Boolean) {
    val view = LocalView.current

    DisposableEffect(enabled) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)

        if (enabled) {
            insetsController.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

/**
 * Extracts the file name from a path.
 */
private fun extractFileName(path: String): String {
    return path.substringAfterLast("/").substringAfterLast("\\")
}
