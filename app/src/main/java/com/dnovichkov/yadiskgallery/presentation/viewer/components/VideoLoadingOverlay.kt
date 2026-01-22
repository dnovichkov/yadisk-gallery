package com.dnovichkov.yadiskgallery.presentation.viewer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * Overlay displayed while video is loading or buffering.
 *
 * @param modifier Modifier for the overlay
 * @param showBackground Whether to show a semi-transparent background
 */
@Composable
fun VideoLoadingOverlay(
    modifier: Modifier = Modifier,
    showBackground: Boolean = true,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .then(
                    if (showBackground) {
                        Modifier.background(Color.Black.copy(alpha = 0.3f))
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            color = Color.White,
            strokeWidth = 4.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        )
    }
}

/**
 * Overlay displayed while buffering during playback.
 * Uses a smaller indicator without background.
 */
@Composable
fun VideoBufferingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = Color.White.copy(alpha = 0.8f),
            strokeWidth = 3.dp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun VideoLoadingOverlayPreview() {
    YaDiskGalleryTheme {
        VideoLoadingOverlay()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun VideoBufferingOverlayPreview() {
    YaDiskGalleryTheme {
        VideoBufferingOverlay()
    }
}
