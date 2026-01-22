package com.dnovichkov.yadiskgallery.presentation.viewer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.dnovichkov.yadiskgallery.presentation.viewer.ImageViewerUiState

/**
 * Top app bar for the image viewer with navigation and actions.
 *
 * @param title Title to display (usually image name)
 * @param subtitle Subtitle (e.g., "3 / 10")
 * @param isVisible Whether the bar is visible
 * @param isSlideshowPlaying Whether slideshow is currently playing
 * @param slideshowIntervalMs Current slideshow interval in milliseconds
 * @param onBackClick Callback when back button is clicked
 * @param onSlideshowToggle Callback when slideshow play/pause is clicked
 * @param onIntervalChange Callback when interval is changed
 * @param onInfoClick Callback when info button is clicked
 * @param modifier Modifier for the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerTopBar(
    title: String,
    subtitle: String?,
    isVisible: Boolean,
    isSlideshowPlaying: Boolean,
    slideshowIntervalMs: Long,
    onBackClick: () -> Unit,
    onSlideshowToggle: () -> Unit,
    onIntervalChange: (Long) -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showIntervalMenu by remember { mutableStateOf(false) }
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = modifier,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent,
                                ),
                        ),
                    )
                    .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }

                    // Slideshow play/pause button
                    IconButton(onClick = onSlideshowToggle) {
                        Icon(
                            imageVector =
                                if (isSlideshowPlaying) {
                                    Icons.Filled.Pause
                                } else {
                                    Icons.Filled.PlayArrow
                                },
                            contentDescription =
                                if (isSlideshowPlaying) {
                                    "Pause slideshow"
                                } else {
                                    "Start slideshow"
                                },
                        )
                    }

                    // Interval selector button
                    Box {
                        IconButton(onClick = { showIntervalMenu = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = "Slideshow interval",
                            )
                        }
                        DropdownMenu(
                            expanded = showIntervalMenu,
                            onDismissRequest = { showIntervalMenu = false },
                        ) {
                            IntervalMenuItem(
                                label = "3 сек",
                                intervalMs = ImageViewerUiState.SLIDESHOW_INTERVAL_3S,
                                currentIntervalMs = slideshowIntervalMs,
                                onClick = {
                                    onIntervalChange(ImageViewerUiState.SLIDESHOW_INTERVAL_3S)
                                    showIntervalMenu = false
                                },
                            )
                            IntervalMenuItem(
                                label = "5 сек",
                                intervalMs = ImageViewerUiState.SLIDESHOW_INTERVAL_5S,
                                currentIntervalMs = slideshowIntervalMs,
                                onClick = {
                                    onIntervalChange(ImageViewerUiState.SLIDESHOW_INTERVAL_5S)
                                    showIntervalMenu = false
                                },
                            )
                            IntervalMenuItem(
                                label = "10 сек",
                                intervalMs = ImageViewerUiState.SLIDESHOW_INTERVAL_10S,
                                currentIntervalMs = slideshowIntervalMs,
                                onClick = {
                                    onIntervalChange(ImageViewerUiState.SLIDESHOW_INTERVAL_10S)
                                    showIntervalMenu = false
                                },
                            )
                        }
                    }

                    IconButton(onClick = onInfoClick) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Image information",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White,
                    ),
            )
        }
    }
}

@Composable
private fun IntervalMenuItem(
    label: String,
    intervalMs: Long,
    currentIntervalMs: Long,
    onClick: () -> Unit,
) {
    val isSelected = intervalMs == currentIntervalMs
    DropdownMenuItem(
        text = {
            Text(
                text = if (isSelected) "$label ✓" else label,
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
        },
        onClick = onClick,
    )
}
