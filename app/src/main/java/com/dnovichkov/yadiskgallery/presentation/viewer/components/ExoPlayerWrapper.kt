package com.dnovichkov.yadiskgallery.presentation.viewer.components

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

/**
 * Lifecycle-aware wrapper for ExoPlayer.
 * Automatically handles player lifecycle and provides callbacks for player events.
 */
class ExoPlayerWrapper(
    context: Context,
    private val onPlaybackStateChange: (isPlaying: Boolean) -> Unit = {},
    private val onPositionChange: (positionMs: Long) -> Unit = {},
    private val onDurationChange: (durationMs: Long) -> Unit = {},
    private val onBufferedPositionChange: (positionMs: Long) -> Unit = {},
    private val onBufferingChange: (isBuffering: Boolean) -> Unit = {},
    private val onError: (String) -> Unit = {},
) : DefaultLifecycleObserver {
    private var exoPlayer: ExoPlayer? = null
    private var playWhenReady = false
    private var currentPosition = 0L
    private var currentMediaUrl: String? = null

    private val playerListener =
        object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        onBufferingChange(true)
                    }
                    Player.STATE_READY -> {
                        onBufferingChange(false)
                        exoPlayer?.duration?.let { duration ->
                            if (duration > 0) {
                                onDurationChange(duration)
                            }
                        }
                    }
                    Player.STATE_ENDED -> {
                        onPlaybackStateChange(false)
                    }
                    Player.STATE_IDLE -> {
                        // Player is idle
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onPlaybackStateChange(isPlaying)
            }

            override fun onPlayerError(error: PlaybackException) {
                val errorMessage =
                    when (error.errorCode) {
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                            "Network connection failed"
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                            "Network timeout"
                        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ->
                            "Failed to initialize decoder"
                        PlaybackException.ERROR_CODE_DECODING_FAILED ->
                            "Decoding failed"
                        PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED ->
                            "Audio track initialization failed"
                        else -> error.message ?: "Unknown playback error"
                    }
                onError(errorMessage)
            }
        }

    init {
        exoPlayer =
            ExoPlayer.Builder(context).build().apply {
                addListener(playerListener)
            }
    }

    /**
     * Loads and prepares a video from the given URL.
     */
    @OptIn(UnstableApi::class)
    fun loadVideo(url: String) {
        currentMediaUrl = url
        exoPlayer?.let { player ->
            val mediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            player.prepare()
        }
    }

    /**
     * Starts or resumes playback.
     */
    fun play() {
        exoPlayer?.play()
    }

    /**
     * Pauses playback.
     */
    fun pause() {
        exoPlayer?.pause()
    }

    /**
     * Seeks to the specified position.
     */
    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    /**
     * Sets the playback speed.
     */
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
    }

    /**
     * Sets the audio volume (0.0 to 1.0).
     */
    fun setVolume(volume: Float) {
        exoPlayer?.volume = volume.coerceIn(0f, 1f)
    }

    /**
     * Gets the current playback position.
     */
    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0L

    /**
     * Gets the total duration.
     */
    fun getDuration(): Long = exoPlayer?.duration ?: 0L

    /**
     * Gets the buffered position.
     */
    fun getBufferedPosition(): Long = exoPlayer?.bufferedPosition ?: 0L

    /**
     * Returns whether the player is currently playing.
     */
    fun isPlaying(): Boolean = exoPlayer?.isPlaying == true

    /**
     * Returns the underlying ExoPlayer instance for use with PlayerView.
     */
    fun getPlayer(): ExoPlayer? = exoPlayer

    /**
     * Updates position and buffered position - should be called periodically.
     */
    fun updatePositions() {
        exoPlayer?.let { player ->
            onPositionChange(player.currentPosition)
            onBufferedPositionChange(player.bufferedPosition)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        exoPlayer?.let { player ->
            player.seekTo(currentPosition)
            player.playWhenReady = playWhenReady
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        exoPlayer?.let { player ->
            playWhenReady = player.playWhenReady
            currentPosition = player.currentPosition
            player.pause()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        release()
    }

    /**
     * Releases player resources.
     */
    fun release() {
        exoPlayer?.let { player ->
            currentPosition = player.currentPosition
            playWhenReady = player.playWhenReady
            player.removeListener(playerListener)
            player.release()
        }
        exoPlayer = null
    }

    /**
     * Saves the current playback state for restoration.
     */
    fun saveState(): PlaybackState {
        return PlaybackState(
            position = getCurrentPosition(),
            playWhenReady = exoPlayer?.playWhenReady == true,
        )
    }

    /**
     * Restores playback state.
     */
    fun restoreState(state: PlaybackState) {
        currentPosition = state.position
        playWhenReady = state.playWhenReady
        exoPlayer?.seekTo(state.position)
        if (state.playWhenReady) {
            exoPlayer?.play()
        }
    }

    /**
     * Data class to hold playback state.
     */
    data class PlaybackState(
        val position: Long = 0L,
        val playWhenReady: Boolean = false,
    )
}
