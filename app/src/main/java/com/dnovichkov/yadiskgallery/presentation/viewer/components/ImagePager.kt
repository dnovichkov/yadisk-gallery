package com.dnovichkov.yadiskgallery.presentation.viewer.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.dnovichkov.yadiskgallery.domain.model.MediaFile

/**
 * A horizontally swipeable image pager.
 *
 * @param images List of images to display
 * @param currentIndex Currently displayed image index
 * @param zoom Current zoom level
 * @param onIndexChange Callback when user swipes to a different image
 * @param onZoomChange Callback when zoom level changes
 * @param onDoubleTap Callback when user double-taps
 * @param onTap Callback when user taps
 * @param getImageUrl Function to get the URL for an image (supports original vs preview)
 * @param modifier Modifier for the composable
 */
@Composable
fun ImagePager(
    images: List<MediaFile>,
    currentIndex: Int,
    zoom: Float,
    onIndexChange: (Int) -> Unit,
    onZoomChange: (Float) -> Unit,
    onDoubleTap: () -> Unit,
    onTap: () -> Unit,
    getImageUrl: (MediaFile) -> String?,
    modifier: Modifier = Modifier,
) {
    val pagerState =
        rememberPagerState(
            initialPage = currentIndex,
            pageCount = { images.size },
        )

    // Sync pager state with ViewModel
    LaunchedEffect(currentIndex) {
        if (pagerState.currentPage != currentIndex) {
            pagerState.animateScrollToPage(currentIndex)
        }
    }

    // Keep currentIndex updated for use in long-running LaunchedEffect
    val currentIndexUpdated by rememberUpdatedState(currentIndex)

    // Notify ViewModel when page changes (only for user swipes, not programmatic changes)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { page ->
                if (page != currentIndexUpdated) {
                    onIndexChange(page)
                }
            }
    }

    // Disable swipe when zoomed in
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        beyondViewportPageCount = 1,
        userScrollEnabled = zoom <= 1f,
        key = { index -> images.getOrNull(index)?.id ?: index },
    ) { page ->
        val image = images.getOrNull(page)
        if (image != null) {
            ZoomableImage(
                imageUrl = getImageUrl(image),
                contentDescription = image.name,
                zoom = if (page == pagerState.currentPage) zoom else 1f,
                onZoomChange = { if (page == pagerState.currentPage) onZoomChange(it) },
                onDoubleTap = { if (page == pagerState.currentPage) onDoubleTap() },
                onTap = onTap,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Image page indicator showing current position.
 *
 * @param currentIndex Current image index (0-based)
 * @param totalCount Total number of images
 * @param modifier Modifier for the composable
 */
@Composable
fun ImagePageIndicator(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Text(
        text = "${currentIndex + 1} / $totalCount",
        modifier = modifier,
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
    )
}
