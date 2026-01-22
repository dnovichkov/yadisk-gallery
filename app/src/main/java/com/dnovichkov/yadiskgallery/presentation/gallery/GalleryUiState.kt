package com.dnovichkov.yadiskgallery.presentation.gallery

import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.ViewMode

/**
 * UI state for the gallery screen.
 */
data class GalleryUiState(
    val items: List<DiskItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPath: String? = null,
    val breadcrumbs: List<BreadcrumbItem> = listOf(BreadcrumbItem("Root", null)),
    val viewMode: ViewMode = ViewMode.GRID,
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val hasMoreItems: Boolean = false,
    val error: String? = null,
    val isEmpty: Boolean = false,
    val isOffline: Boolean = false,
)

/**
 * Represents a breadcrumb item for navigation.
 */
data class BreadcrumbItem(
    val name: String,
    val path: String?,
)

/**
 * Events that can be triggered from the gallery UI.
 */
sealed class GalleryEvent {
    /** Load initial content */
    data object LoadContent : GalleryEvent()

    /** Refresh content (pull-to-refresh) */
    data object Refresh : GalleryEvent()

    /** Load more items (infinite scroll) */
    data object LoadMore : GalleryEvent()

    /** Navigate to a folder */
    data class NavigateToFolder(val path: String) : GalleryEvent()

    /** Navigate to a specific breadcrumb or path */
    data class NavigateToPath(val path: String?) : GalleryEvent()

    /** Folder clicked */
    data class FolderClicked(val folder: DiskItem.Directory) : GalleryEvent()

    /** Media file clicked */
    data class MediaClicked(val file: DiskItem.File) : GalleryEvent()

    /** Open a media file */
    data class OpenMedia(val item: DiskItem.File) : GalleryEvent()

    /** Change view mode */
    data class SetViewMode(val viewMode: ViewMode) : GalleryEvent()

    /** Change sort order */
    data class SetSortOrder(val sortOrder: SortOrder) : GalleryEvent()

    /** Navigate to settings */
    data object NavigateToSettings : GalleryEvent()

    /** Retry after error */
    data object Retry : GalleryEvent()

    /** Clear error message */
    data object ClearError : GalleryEvent()
}

/**
 * Navigation events from gallery screen.
 */
sealed class GalleryNavigationEvent {
    /** Navigate to image viewer */
    data class NavigateToImageViewer(val path: String, val index: Int) : GalleryNavigationEvent()

    /** Navigate to video player */
    data class NavigateToVideoPlayer(val path: String) : GalleryNavigationEvent()

    /** Navigate to settings */
    data object NavigateToSettings : GalleryNavigationEvent()
}
