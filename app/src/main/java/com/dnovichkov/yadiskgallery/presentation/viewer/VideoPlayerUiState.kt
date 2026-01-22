package com.dnovichkov.yadiskgallery.presentation.viewer

import com.dnovichkov.yadiskgallery.domain.model.MediaFile

/**
 * UI state for the video player screen.
 */
data class VideoPlayerUiState(
    val videoFile: MediaFile? = null,
    val videoUrl: String? = null,
    val isLoading: Boolean = false,
    val isBuffering: Boolean = false,
    val isPlaying: Boolean = false,
    val error: String? = null,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L,
    val showControls: Boolean = true,
    val playbackSpeed: Float = 1f,
    val volume: Float = 1f,
) {
    val progress: Float
        get() = if (duration > 0) currentPosition.toFloat() / duration else 0f

    val bufferedProgress: Float
        get() = if (duration > 0) bufferedPosition.toFloat() / duration else 0f

    val remainingTime: Long
        get() = (duration - currentPosition).coerceAtLeast(0)

    val hasError: Boolean
        get() = error != null

    val canPlay: Boolean
        get() = videoUrl != null && !hasError

    companion object {
        val PLAYBACK_SPEEDS = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
        const val DEFAULT_PLAYBACK_SPEED = 1f
        const val SEEK_INCREMENT_MS = 10_000L
        const val CONTROLS_HIDE_DELAY_MS = 3000L
    }
}

/**
 * Events that can be triggered from the video player UI.
 */
sealed class VideoPlayerEvent {
    /** Load video for the given file path */
    data class LoadVideo(val filePath: String) : VideoPlayerEvent()

    /** Toggle play/pause */
    data object TogglePlayPause : VideoPlayerEvent()

    /** Play video */
    data object Play : VideoPlayerEvent()

    /** Pause video */
    data object Pause : VideoPlayerEvent()

    /** Seek to specific position */
    data class SeekTo(val positionMs: Long) : VideoPlayerEvent()

    /** Seek forward by SEEK_INCREMENT_MS */
    data object SeekForward : VideoPlayerEvent()

    /** Seek backward by SEEK_INCREMENT_MS */
    data object SeekBackward : VideoPlayerEvent()

    /** Update current playback position */
    data class UpdatePosition(val positionMs: Long) : VideoPlayerEvent()

    /** Update duration */
    data class UpdateDuration(val durationMs: Long) : VideoPlayerEvent()

    /** Update buffered position */
    data class UpdateBufferedPosition(val positionMs: Long) : VideoPlayerEvent()

    /** Set buffering state */
    data class SetBuffering(val isBuffering: Boolean) : VideoPlayerEvent()

    /** Set playing state */
    data class SetPlaying(val isPlaying: Boolean) : VideoPlayerEvent()

    /** Toggle controls visibility */
    data object ToggleControls : VideoPlayerEvent()

    /** Show controls */
    data object ShowControls : VideoPlayerEvent()

    /** Hide controls */
    data object HideControls : VideoPlayerEvent()

    /** Set playback speed */
    data class SetPlaybackSpeed(val speed: Float) : VideoPlayerEvent()

    /** Set volume */
    data class SetVolume(val volume: Float) : VideoPlayerEvent()

    /** Handle playback error */
    data class OnError(val message: String) : VideoPlayerEvent()

    /** Clear error */
    data object ClearError : VideoPlayerEvent()

    /** Retry loading video */
    data object Retry : VideoPlayerEvent()
}
