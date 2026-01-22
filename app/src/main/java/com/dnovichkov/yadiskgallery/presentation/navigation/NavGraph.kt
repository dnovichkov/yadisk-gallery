package com.dnovichkov.yadiskgallery.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dnovichkov.yadiskgallery.presentation.gallery.GalleryScreen
import com.dnovichkov.yadiskgallery.presentation.settings.SettingsScreen
import com.dnovichkov.yadiskgallery.presentation.viewer.ImageViewerScreen
import com.dnovichkov.yadiskgallery.presentation.viewer.VideoPlayerScreen
import java.net.URLDecoder

/**
 * Main navigation graph for the application.
 * Defines all navigation destinations and their arguments.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onStartYandexLogin: () -> Unit = {},
    startDestination: String = Screen.Settings.route,
) {
    val navigationActions = remember(navController) { NavigationActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // Settings Screen
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateToGallery = { navigationActions.navigateToGallery() },
                onStartYandexLogin = onStartYandexLogin,
            )
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
        ) {
            GalleryScreen(
                onNavigateToSettings = { navigationActions.navigateToSettings() },
                onNavigateToImageViewer = { path, index ->
                    navigationActions.navigateToImageViewer(path, index)
                },
                onNavigateToVideoPlayer = { path ->
                    navigationActions.navigateToVideoPlayer(path)
                },
            )
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
            val folderPath = URLDecoder.decode(encodedPath, "UTF-8").ifEmpty { null }
            val index = backStackEntry.arguments?.getInt(Screen.ImageViewer.INDEX_ARG) ?: 0

            ImageViewerScreen(
                folderPath = folderPath,
                initialIndex = index,
                onNavigateBack = { navigationActions.navigateBack() },
            )
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

            VideoPlayerScreen(
                filePath = path,
                onNavigateBack = { navigationActions.navigateBack() },
            )
        }
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
