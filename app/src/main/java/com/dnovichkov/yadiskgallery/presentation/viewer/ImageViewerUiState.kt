package com.dnovichkov.yadiskgallery.presentation.viewer

import com.dnovichkov.yadiskgallery.domain.model.MediaFile

/**
 * UI state for the image viewer screen.
 */
data class ImageViewerUiState(
    val images: List<MediaFile> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingOriginal: Boolean = false,
    val error: String? = null,
    val currentImageUrl: String? = null,
    val originalImageUrl: String? = null,
    val zoomLevel: Float = 1f,
    val showControls: Boolean = true,
    val exifData: ExifData? = null,
    val showExifSheet: Boolean = false,
) {
    val currentImage: MediaFile?
        get() = images.getOrNull(currentIndex)

    val hasNextImage: Boolean
        get() = currentIndex < images.size - 1

    val hasPreviousImage: Boolean
        get() = currentIndex > 0

    val imageCount: Int
        get() = images.size

    val shouldLoadOriginal: Boolean
        get() = zoomLevel > ZOOM_THRESHOLD_FOR_ORIGINAL && originalImageUrl == null

    companion object {
        const val ZOOM_THRESHOLD_FOR_ORIGINAL = 2f
        const val MIN_ZOOM = 1f
        const val MAX_ZOOM = 5f
        const val DOUBLE_TAP_ZOOM = 2f
    }
}

/**
 * EXIF metadata for an image.
 */
data class ExifData(
    val width: Int? = null,
    val height: Int? = null,
    val dateTime: String? = null,
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val focalLength: String? = null,
    val aperture: String? = null,
    val iso: String? = null,
    val exposureTime: String? = null,
    val gpsLatitude: Double? = null,
    val gpsLongitude: Double? = null,
)

/**
 * Events that can be triggered from the image viewer UI.
 */
sealed class ImageViewerEvent {
    /** Load images for the given folder path */
    data class LoadImages(val folderPath: String?, val initialIndex: Int) : ImageViewerEvent()

    /** Navigate to next image */
    data object NextImage : ImageViewerEvent()

    /** Navigate to previous image */
    data object PreviousImage : ImageViewerEvent()

    /** Navigate to specific image by index */
    data class GoToImage(val index: Int) : ImageViewerEvent()

    /** Update zoom level */
    data class SetZoomLevel(val zoom: Float) : ImageViewerEvent()

    /** Toggle double-tap zoom */
    data object ToggleDoubleTapZoom : ImageViewerEvent()

    /** Toggle controls visibility */
    data object ToggleControls : ImageViewerEvent()

    /** Show EXIF info sheet */
    data object ShowExifInfo : ImageViewerEvent()

    /** Hide EXIF info sheet */
    data object HideExifInfo : ImageViewerEvent()

    /** Load original quality image */
    data object LoadOriginalImage : ImageViewerEvent()

    /** Retry after error */
    data object Retry : ImageViewerEvent()

    /** Clear error */
    data object ClearError : ImageViewerEvent()
}
