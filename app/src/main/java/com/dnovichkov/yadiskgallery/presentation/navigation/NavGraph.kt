package com.dnovichkov.yadiskgallery.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import java.net.URLDecoder

/**
 * Main navigation graph for the application.
 * Defines all navigation destinations and their arguments.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Settings.route,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // Settings Screen
        composable(route = Screen.Settings.route) {
            // TODO: Replace with SettingsScreen when implemented
            PlaceholderScreen("Settings")
        }

        // Gallery Screen
        composable(
            route = Screen.Gallery.route,
            arguments =
                listOf(
                    navArgument(Screen.Gallery.PATH_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString(Screen.Gallery.PATH_ARG)
            // TODO: Replace with GalleryScreen when implemented
            PlaceholderScreen("Gallery: ${path ?: "Root"}")
        }

        // Image Viewer Screen
        composable(
            route = Screen.ImageViewer.route,
            arguments =
                listOf(
                    navArgument(Screen.ImageViewer.PATH_ARG) {
                        type = NavType.StringType
                    },
                    navArgument(Screen.ImageViewer.INDEX_ARG) {
                        type = NavType.IntType
                        defaultValue = 0
                    },
                ),
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString(Screen.ImageViewer.PATH_ARG) ?: ""
            val path = URLDecoder.decode(encodedPath, "UTF-8")
            val index = backStackEntry.arguments?.getInt(Screen.ImageViewer.INDEX_ARG) ?: 0
            // TODO: Replace with ImageViewerScreen when implemented
            PlaceholderScreen("Image Viewer: $path (index: $index)")
        }

        // Video Player Screen
        composable(
            route = Screen.VideoPlayer.route,
            arguments =
                listOf(
                    navArgument(Screen.VideoPlayer.PATH_ARG) {
                        type = NavType.StringType
                    },
                ),
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString(Screen.VideoPlayer.PATH_ARG) ?: ""
            val path = URLDecoder.decode(encodedPath, "UTF-8")
            // TODO: Replace with VideoPlayerScreen when implemented
            PlaceholderScreen("Video Player: $path")
        }
    }
}

/**
 * Placeholder screen for destinations not yet implemented.
 */
@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = name)
    }
}

/**
 * Navigation actions helper class.
 * Provides type-safe navigation methods.
 */
class NavigationActions(private val navController: NavHostController) {
    fun navigateToSettings() {
        navController.navigate(Screen.Settings.route) {
            launchSingleTop = true
        }
    }

    fun navigateToGallery(path: String? = null) {
        navController.navigate(Screen.Gallery.createRoute(path)) {
            launchSingleTop = true
        }
    }

    fun navigateToImageViewer(
        path: String,
        index: Int = 0,
    ) {
        navController.navigate(Screen.ImageViewer.createRoute(path, index))
    }

    fun navigateToVideoPlayer(path: String) {
        navController.navigate(Screen.VideoPlayer.createRoute(path))
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    fun navigateBackToGallery() {
        navController.popBackStack(Screen.Gallery.route, inclusive = false)
    }
}
