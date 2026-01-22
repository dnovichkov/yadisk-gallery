package com.dnovichkov.yadiskgallery.presentation.viewer.components

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Composable that displays a video player using ExoPlayer.
 *
 * @param videoUrl The URL of the video to play
 * @param isPlaying Whether the video should be playing
 * @param playbackSpeed Current playback speed
 * @param volume Current volume level (0.0 to 1.0)
 * @param onPlaybackStateChange Callback when playback state changes
 * @param onPositionChange Callback when playback position changes
 * @param onDurationChange Callback when video duration is available
 * @param onBufferedPositionChange Callback when buffered position changes
 * @param onBufferingChange Callback when buffering state changes
 * @param onError Callback when an error occurs
 * @param onPlayerClick Callback when player is clicked (for showing/hiding controls)
 * @param seekToPosition Optional position to seek to (in milliseconds)
 * @param modifier Modifier for the player view
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String?,
    isPlaying: Boolean,
    playbackSpeed: Float,
    volume: Float,
    onPlaybackStateChange: (Boolean) -> Unit,
    onPositionChange: (Long) -> Unit,
    onDurationChange: (Long) -> Unit,
    onBufferedPositionChange: (Long) -> Unit,
    onBufferingChange: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onPlayerClick: () -> Unit,
    seekToPosition: Long?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var playerWrapper by remember { mutableStateOf<ExoPlayerWrapper?>(null) }
    var lastSeekPosition by remember { mutableStateOf<Long?>(null) }

    // Create player wrapper
    DisposableEffect(context) {
        val wrapper =
            ExoPlayerWrapper(
                context = context,
                onPlaybackStateChange = onPlaybackStateChange,
                onPositionChange = onPositionChange,
                onDurationChange = onDurationChange,
                onBufferedPositionChange = onBufferedPositionChange,
                onBufferingChange = onBufferingChange,
                onError = onError,
            )
        playerWrapper = wrapper

        onDispose {
            wrapper.release()
            playerWrapper = null
        }
    }

    // Handle lifecycle
    DisposableEffect(lifecycleOwner, playerWrapper) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> playerWrapper?.pause()
                    Lifecycle.Event.ON_RESUME -> {
                        if (isPlaying) {
                            playerWrapper?.play()
                        }
                    }
                    else -> {}
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Load video when URL changes
    LaunchedEffect(videoUrl, playerWrapper) {
        if (videoUrl != null && playerWrapper != null) {
            playerWrapper?.loadVideo(videoUrl)
        }
    }

    // Handle play/pause state
    LaunchedEffect(isPlaying, playerWrapper) {
        playerWrapper?.let { wrapper ->
            if (isPlaying) {
                wrapper.play()
            } else {
                wrapper.pause()
            }
        }
    }

    // Handle playback speed changes
    LaunchedEffect(playbackSpeed, playerWrapper) {
        playerWrapper?.setPlaybackSpeed(playbackSpeed)
    }

    // Handle volume changes
    LaunchedEffect(volume, playerWrapper) {
        playerWrapper?.setVolume(volume)
    }

    // Handle seek requests
    LaunchedEffect(seekToPosition, playerWrapper) {
        if (seekToPosition != null && seekToPosition != lastSeekPosition) {
            playerWrapper?.seekTo(seekToPosition)
            lastSeekPosition = seekToPosition
        }
    }

    // Update position periodically
    LaunchedEffect(playerWrapper) {
        while (isActive) {
            playerWrapper?.updatePositions()
            delay(POSITION_UPDATE_INTERVAL_MS)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                layoutParams =
                    FrameLayout.LayoutParams(
                        MATCH_PARENT,
                        MATCH_PARENT,
                    )
                useController = false // We'll use custom controls
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setOnClickListener { onPlayerClick() }
            }
        },
        update = { playerView ->
            playerView.player = playerWrapper?.getPlayer()
        },
    )
}

private const val POSITION_UPDATE_INTERVAL_MS = 500L
