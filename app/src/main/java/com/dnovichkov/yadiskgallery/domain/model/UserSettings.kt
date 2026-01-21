package com.dnovichkov.yadiskgallery.domain.model

/**
 * User settings for the application.
 *
 * @property publicFolderUrl URL of the public Yandex.Disk folder (null if not set)
 * @property rootFolderPath Path to the root folder for browsing (null for disk root)
 * @property isAuthenticated Whether the user is authenticated via Yandex OAuth
 * @property viewMode Display mode for the gallery (grid or list)
 * @property sortOrder Sort order for files and folders
 */
data class UserSettings(
    val publicFolderUrl: String?,
    val rootFolderPath: String?,
    val isAuthenticated: Boolean,
    val viewMode: ViewMode,
    val sortOrder: SortOrder,
) {
    companion object {
        /**
         * Creates default settings for a new user.
         */
        fun default() =
            UserSettings(
                publicFolderUrl = null,
                rootFolderPath = null,
                isAuthenticated = false,
                viewMode = ViewMode.GRID,
                sortOrder = SortOrder.DATE_DESC,
            )
    }
}

/**
 * Display mode for the gallery.
 */
enum class ViewMode {
    /** Grid view with thumbnails (3 columns) */
    GRID,

    /** List view with larger previews and details */
    LIST,
}

/**
 * Sort order for files and folders.
 */
enum class SortOrder {
    /** Sort by name ascending (A-Z) */
    NAME_ASC,

    /** Sort by name descending (Z-A) */
    NAME_DESC,

    /** Sort by date ascending (oldest first) */
    DATE_ASC,

    /** Sort by date descending (newest first) */
    DATE_DESC,

    /** Sort by size ascending (smallest first) */
    SIZE_ASC,

    /** Sort by size descending (largest first) */
    SIZE_DESC,
}
