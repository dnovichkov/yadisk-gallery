package com.dnovichkov.yadiskgallery.presentation.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnovichkov.yadiskgallery.domain.usecase.files.GetMediaDownloadUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the video player screen.
 * Handles video loading, playback state, and controls.
 */
@HiltViewModel
class VideoPlayerViewModel
    @Inject
    constructor(
        private val getMediaDownloadUrlUseCase: GetMediaDownloadUrlUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(VideoPlayerUiState())
        val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()

        private var currentFilePath: String? = null

        /**
         * Handles UI events.
         */
        fun onEvent(event: VideoPlayerEvent) {
            when (event) {
                is VideoPlayerEvent.LoadVideo -> loadVideo(event.filePath)
                is VideoPlayerEvent.TogglePlayPause -> togglePlayPause()
                is VideoPlayerEvent.Play -> play()
                is VideoPlayerEvent.Pause -> pause()
                is VideoPlayerEvent.SeekTo -> seekTo(event.positionMs)
                is VideoPlayerEvent.SeekForward -> seekForward()
                is VideoPlayerEvent.SeekBackward -> seekBackward()
                is VideoPlayerEvent.UpdatePosition -> updatePosition(event.positionMs)
                is VideoPlayerEvent.UpdateDuration -> updateDuration(event.durationMs)
                is VideoPlayerEvent.UpdateBufferedPosition -> updateBufferedPosition(event.positionMs)
                is VideoPlayerEvent.SetBuffering -> setBuffering(event.isBuffering)
                is VideoPlayerEvent.SetPlaying -> setPlaying(event.isPlaying)
                is VideoPlayerEvent.ToggleControls -> toggleControls()
                is VideoPlayerEvent.ShowControls -> showControls()
                is VideoPlayerEvent.HideControls -> hideControls()
                is VideoPlayerEvent.SetPlaybackSpeed -> setPlaybackSpeed(event.speed)
                is VideoPlayerEvent.SetVolume -> setVolume(event.volume)
                is VideoPlayerEvent.OnError -> onError(event.message)
                is VideoPlayerEvent.ClearError -> clearError()
                is VideoPlayerEvent.Retry -> retry()
            }
        }

        private fun loadVideo(filePath: String) {
            currentFilePath = filePath
            _uiState.update { it.copy(isLoading = true, error = null) }

            viewModelScope.launch {
                getMediaDownloadUrlUseCase(filePath)
                    .onSuccess { url ->
                        _uiState.update {
                            it.copy(
                                videoUrl = url,
                                isLoading = false,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load video",
                            )
                        }
                    }
            }
        }

        private fun togglePlayPause() {
            _uiState.update { it.copy(isPlaying = !it.isPlaying) }
        }

        private fun play() {
            _uiState.update { it.copy(isPlaying = true) }
        }

        private fun pause() {
            _uiState.update { it.copy(isPlaying = false) }
        }

        private fun seekTo(positionMs: Long) {
            val duration = _uiState.value.duration
            val clampedPosition = positionMs.coerceIn(0L, duration.coerceAtLeast(0L))
            _uiState.update { it.copy(currentPosition = clampedPosition) }
        }

        private fun seekForward() {
            val currentState = _uiState.value
            val newPosition =
                (currentState.currentPosition + VideoPlayerUiState.SEEK_INCREMENT_MS)
                    .coerceAtMost(currentState.duration)
            _uiState.update { it.copy(currentPosition = newPosition) }
        }

        private fun seekBackward() {
            val currentState = _uiState.value
            val newPosition =
                (currentState.currentPosition - VideoPlayerUiState.SEEK_INCREMENT_MS)
                    .coerceAtLeast(0L)
            _uiState.update { it.copy(currentPosition = newPosition) }
        }

        private fun updatePosition(positionMs: Long) {
            _uiState.update { it.copy(currentPosition = positionMs) }
        }

        private fun updateDuration(durationMs: Long) {
            _uiState.update { it.copy(duration = durationMs) }
        }

        private fun updateBufferedPosition(positionMs: Long) {
            _uiState.update { it.copy(bufferedPosition = positionMs) }
        }

        private fun setBuffering(isBuffering: Boolean) {
            _uiState.update { it.copy(isBuffering = isBuffering) }
        }

        private fun setPlaying(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        private fun toggleControls() {
            _uiState.update { it.copy(showControls = !it.showControls) }
        }

        private fun showControls() {
            _uiState.update { it.copy(showControls = true) }
        }

        private fun hideControls() {
            _uiState.update { it.copy(showControls = false) }
        }

        private fun setPlaybackSpeed(speed: Float) {
            _uiState.update { it.copy(playbackSpeed = speed) }
        }

        private fun setVolume(volume: Float) {
            val clampedVolume = volume.coerceIn(0f, 1f)
            _uiState.update { it.copy(volume = clampedVolume) }
        }

        private fun onError(message: String) {
            _uiState.update { it.copy(error = message, isPlaying = false) }
        }

        private fun clearError() {
            _uiState.update { it.copy(error = null) }
        }

        private fun retry() {
            _uiState.update { it.copy(error = null) }
            currentFilePath?.let { loadVideo(it) }
        }

        /**
         * Saves the current playback position for resuming later.
         * @return The current playback position in milliseconds
         */
        fun savePlaybackPosition(): Long {
            return _uiState.value.currentPosition
        }

        /**
         * Restores playback to a previously saved position.
         * @param positionMs The position to restore to
         */
        fun restorePlaybackPosition(positionMs: Long) {
            _uiState.update { it.copy(currentPosition = positionMs) }
        }
    }
