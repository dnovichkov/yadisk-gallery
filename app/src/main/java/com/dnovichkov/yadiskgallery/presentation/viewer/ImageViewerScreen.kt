package com.dnovichkov.yadiskgallery.presentation.viewer

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.dnovichkov.yadiskgallery.presentation.components.ErrorView
import com.dnovichkov.yadiskgallery.presentation.components.LoadingIndicator
import com.dnovichkov.yadiskgallery.presentation.viewer.components.ExifInfoSheet
import com.dnovichkov.yadiskgallery.presentation.viewer.components.ImagePager
import com.dnovichkov.yadiskgallery.presentation.viewer.components.ImageViewerTopBar
import com.dnovichkov.yadiskgallery.presentation.viewer.components.NavigationArrows
import kotlinx.coroutines.launch

/**
 * Full-screen image viewer with zoom, swipe, and EXIF info support.
 *
 * @param folderPath Path to the folder containing images
 * @param initialIndex Initial image index to display
 * @param publicFolderUrl Optional public folder URL for public folder mode
 * @param onNavigateBack Callback to navigate back
 * @param viewModel ViewModel for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreen(
    folderPath: String?,
    initialIndex: Int,
    publicFolderUrl: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: ImageViewerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    // Request focus for keyboard navigation
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Load images on first composition
    LaunchedEffect(folderPath, initialIndex, publicFolderUrl) {
        viewModel.onEvent(ImageViewerEvent.LoadImages(folderPath, initialIndex, publicFolderUrl))
    }

    // Setup immersive mode
    ImmersiveMode(enabled = !uiState.showControls)

    // Handle back press
    BackHandler {
        onNavigateBack()
    }

    // Auto-load original when zoomed in
    LaunchedEffect(uiState.shouldLoadOriginal, uiState.currentIndex) {
        if (uiState.shouldLoadOriginal) {
            viewModel.onEvent(ImageViewerEvent.LoadOriginalImage)
        }
    }

    Scaffold(
        containerColor = Color.Black,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
                    .focusRequester(focusRequester)
                    .focusable()
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && uiState.zoomLevel <= 1f) {
                            when (event.key) {
                                Key.DirectionLeft -> {
                                    if (uiState.hasPreviousImage) {
                                        viewModel.onEvent(ImageViewerEvent.PreviousImage)
                                        true
                                    } else {
                                        false
                                    }
                                }
                                Key.DirectionRight -> {
                                    if (uiState.hasNextImage) {
                                        viewModel.onEvent(ImageViewerEvent.NextImage)
                                        true
                                    } else {
                                        false
                                    }
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    },
        ) {
            when {
                uiState.isLoading && uiState.images.isEmpty() -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                uiState.error != null && uiState.images.isEmpty() -> {
                    ErrorView(
                        message = uiState.error ?: "Ошибка загрузки",
                        onRetry = { viewModel.onEvent(ImageViewerEvent.Retry) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                uiState.images.isNotEmpty() -> {
                    ImagePager(
                        images = uiState.images,
                        currentIndex = uiState.currentIndex,
                        zoom = uiState.zoomLevel,
                        onIndexChange = { viewModel.onEvent(ImageViewerEvent.GoToImage(it)) },
                        onZoomChange = { viewModel.onEvent(ImageViewerEvent.SetZoomLevel(it)) },
                        onDoubleTap = { viewModel.onEvent(ImageViewerEvent.ToggleDoubleTapZoom) },
                        onTap = { viewModel.onEvent(ImageViewerEvent.ToggleControls) },
                        getImageUrl = { image ->
                            // Use original URL if available and zoomed in, otherwise preview
                            if (uiState.originalImageUrl != null &&
                                uiState.zoomLevel > ImageViewerUiState.ZOOM_THRESHOLD_FOR_ORIGINAL &&
                                image == uiState.currentImage
                            ) {
                                uiState.originalImageUrl
                            } else {
                                image.previewUrl
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Loading indicator for original image
                    if (uiState.isLoadingOriginal) {
                        LoadingIndicator(
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    // Navigation arrows
                    NavigationArrows(
                        hasPreviousImage = uiState.hasPreviousImage,
                        hasNextImage = uiState.hasNextImage,
                        isVisible = uiState.showControls,
                        onPreviousClick = { viewModel.onEvent(ImageViewerEvent.PreviousImage) },
                        onNextClick = { viewModel.onEvent(ImageViewerEvent.NextImage) },
                    )
                }
            }

            // Top bar with controls
            ImageViewerTopBar(
                title = uiState.currentImage?.name ?: "",
                subtitle =
                    if (uiState.imageCount > 0) {
                        "${uiState.currentIndex + 1} / ${uiState.imageCount}"
                    } else {
                        null
                    },
                isVisible = uiState.showControls,
                onBackClick = onNavigateBack,
                onInfoClick = { viewModel.onEvent(ImageViewerEvent.ShowExifInfo) },
                modifier = Modifier.align(Alignment.TopCenter),
            )

            // EXIF info bottom sheet
            if (uiState.showExifSheet && uiState.currentImage != null) {
                ExifInfoSheet(
                    mediaFile = uiState.currentImage!!,
                    exifData = uiState.exifData,
                    sheetState = sheetState,
                    onDismiss = {
                        scope.launch {
                            sheetState.hide()
                            viewModel.onEvent(ImageViewerEvent.HideExifInfo)
                        }
                    },
                )
            }
        }
    }
}

/**
 * Controls immersive/fullscreen mode.
 */
@Composable
private fun ImmersiveMode(enabled: Boolean) {
    val view = LocalView.current

    DisposableEffect(enabled) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)

        if (enabled) {
            insetsController.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
