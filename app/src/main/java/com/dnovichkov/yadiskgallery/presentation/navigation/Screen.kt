package com.dnovichkov.yadiskgallery.presentation.navigation

/**
 * Sealed class representing all navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    /**
     * Settings screen - app configuration and authentication.
     */
    data object Settings : Screen("settings")

    /**
     * Gallery screen - displays files and folders.
     * @param path optional folder path to display (null for root)
     */
    data object Gallery : Screen("gallery?path={path}") {
        const val PATH_ARG = "path"

        fun createRoute(path: String? = null): String {
            return if (path != null) {
                "gallery?path=$path"
            } else {
                "gallery"
            }
        }
    }

    /**
     * Image viewer screen - fullscreen image viewing with zoom.
     * @param path file path
     * @param index current index in the list
     * @param publicUrl optional public folder URL for public folder mode
     */
    data object ImageViewer : Screen("image_viewer/{path}?index={index}&publicUrl={publicUrl}") {
        const val PATH_ARG = "path"
        const val INDEX_ARG = "index"
        const val PUBLIC_URL_ARG = "publicUrl"

        fun createRoute(
            path: String,
            index: Int = 0,
            publicUrl: String? = null,
        ): String {
            val encodedPath = java.net.URLEncoder.encode(path.ifEmpty { " " }, "UTF-8")
            val encodedPublicUrl = publicUrl?.let { java.net.URLEncoder.encode(it, "UTF-8") } ?: ""
            return "image_viewer/$encodedPath?index=$index&publicUrl=$encodedPublicUrl"
        }
    }

    /**
     * Video player screen - video playback with controls.
     * @param path file path
     */
    data object VideoPlayer : Screen("video_player/{path}") {
        const val PATH_ARG = "path"

        fun createRoute(path: String): String {
            return "video_player/${java.net.URLEncoder.encode(path, "UTF-8")}"
        }
    }
}
