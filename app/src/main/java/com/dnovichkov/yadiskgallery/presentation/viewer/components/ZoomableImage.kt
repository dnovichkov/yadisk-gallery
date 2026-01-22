package com.dnovichkov.yadiskgallery.presentation.viewer.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dnovichkov.yadiskgallery.presentation.components.LoadingIndicator
import com.dnovichkov.yadiskgallery.presentation.viewer.ImageViewerUiState

/**
 * A composable that displays an image with zoom and pan support.
 *
 * @param imageUrl URL of the image to display
 * @param contentDescription Accessibility description
 * @param zoom Current zoom level
 * @param onZoomChange Callback when zoom level changes
 * @param onDoubleTap Callback when user double-taps the image
 * @param onTap Callback when user taps the image
 * @param modifier Modifier for the composable
 */
@Composable
fun ZoomableImage(
    imageUrl: String?,
    contentDescription: String?,
    zoom: Float,
    onZoomChange: (Float) -> Unit,
    onDoubleTap: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastTapTime by remember { mutableStateOf(0L) }

    val clampedZoom = zoom.coerceIn(ImageViewerUiState.MIN_ZOOM, ImageViewerUiState.MAX_ZOOM)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val firstDown = awaitFirstDown(requireUnconsumed = false)
                        val firstDownTime = System.currentTimeMillis()

                        var zoom1 = clampedZoom
                        var pan = offset
                        var pastTouchSlop = false
                        val touchSlop = viewConfiguration.touchSlop
                        var lockedToPanZoom = false

                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }

                            if (!canceled) {
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                val centroidSize = event.calculateCentroidSize(useCurrent = false)

                                if (!pastTouchSlop) {
                                    val distance = (event.calculateCentroid() - firstDown.position).getDistance()
                                    pastTouchSlop = distance > touchSlop || centroidSize > touchSlop
                                    lockedToPanZoom = pastTouchSlop
                                }

                                if (pastTouchSlop) {
                                    // Update zoom
                                    val newZoom =
                                        (zoom1 * zoomChange).coerceIn(
                                            ImageViewerUiState.MIN_ZOOM,
                                            ImageViewerUiState.MAX_ZOOM,
                                        )

                                    if (newZoom != zoom1) {
                                        zoom1 = newZoom
                                        onZoomChange(newZoom)
                                    }

                                    // Update pan only when zoomed in
                                    if (zoom1 > ImageViewerUiState.MIN_ZOOM) {
                                        pan += panChange * zoom1
                                        offset = constrainOffset(pan, zoom1, size)
                                    } else {
                                        pan = Offset.Zero
                                        offset = Offset.Zero
                                    }

                                    event.changes.forEach {
                                        if (it.positionChanged()) {
                                            it.consume()
                                        }
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        // Handle tap and double-tap
                        if (!lockedToPanZoom) {
                            val tapTime = System.currentTimeMillis()
                            val timeSinceLastTap = tapTime - lastTapTime

                            if (timeSinceLastTap < DOUBLE_TAP_TIMEOUT_MS) {
                                // Double tap
                                onDoubleTap()
                                offset = Offset.Zero
                                lastTapTime = 0L
                            } else {
                                // Single tap
                                onTap()
                                lastTapTime = tapTime
                            }
                        }
                    }
                }
                .graphicsLayer {
                    scaleX = clampedZoom
                    scaleY = clampedZoom
                    translationX = offset.x
                    translationY = offset.y
                },
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = contentDescription,
            loading = {
                LoadingIndicator()
            },
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Constrains the offset to keep the image within bounds when zoomed.
 */
private fun constrainOffset(
    offset: Offset,
    zoom: Float,
    size: androidx.compose.ui.unit.IntSize,
): Offset {
    val maxX = (size.width * (zoom - 1) / 2f).coerceAtLeast(0f)
    val maxY = (size.height * (zoom - 1) / 2f).coerceAtLeast(0f)

    return Offset(
        x = offset.x.coerceIn(-maxX, maxX),
        y = offset.y.coerceIn(-maxY, maxY),
    )
}

private const val DOUBLE_TAP_TIMEOUT_MS = 300L
