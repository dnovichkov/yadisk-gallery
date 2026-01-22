package com.dnovichkov.yadiskgallery.presentation.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import com.dnovichkov.yadiskgallery.presentation.components.EmptyStateView
import com.dnovichkov.yadiskgallery.presentation.components.ErrorView
import com.dnovichkov.yadiskgallery.presentation.components.OfflineBanner
import com.dnovichkov.yadiskgallery.presentation.gallery.components.BreadcrumbsBar
import com.dnovichkov.yadiskgallery.presentation.gallery.components.GalleryGrid
import com.dnovichkov.yadiskgallery.presentation.gallery.components.GalleryList
import com.dnovichkov.yadiskgallery.presentation.gallery.components.GalleryTopBar
import com.dnovichkov.yadiskgallery.presentation.gallery.components.SkeletonGrid

/**
 * Main gallery screen displaying folders and media files.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToImageViewer: (path: String, index: Int) -> Unit,
    onNavigateToVideoPlayer: (path: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is GalleryNavigationEvent.NavigateToImageViewer -> {
                    onNavigateToImageViewer(event.path, event.index)
                }
                is GalleryNavigationEvent.NavigateToVideoPlayer -> {
                    onNavigateToVideoPlayer(event.path)
                }
                is GalleryNavigationEvent.NavigateToSettings -> {
                    onNavigateToSettings()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            GalleryTopBar(
                title = "YaDisk Gallery",
                viewMode = uiState.viewMode,
                sortOrder = uiState.sortOrder,
                onViewModeChange = { viewModel.onEvent(GalleryEvent.SetViewMode(it)) },
                onSortOrderChange = { viewModel.onEvent(GalleryEvent.SetSortOrder(it)) },
                onSettingsClick = { viewModel.onEvent(GalleryEvent.NavigateToSettings) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Offline banner
            OfflineBanner(isOffline = uiState.isOffline)

            // Breadcrumbs navigation
            if (uiState.breadcrumbs.size > 1 || uiState.currentPath != null) {
                BreadcrumbsBar(
                    breadcrumbs = uiState.breadcrumbs,
                    onBreadcrumbClick = { path ->
                        viewModel.onEvent(GalleryEvent.NavigateToPath(path))
                    },
                )
            }

            // Content area
            GalleryContent(
                uiState = uiState,
                onRefresh = { viewModel.onEvent(GalleryEvent.Refresh) },
                onRetry = { viewModel.onEvent(GalleryEvent.Retry) },
                onFolderClick = { folder ->
                    viewModel.onEvent(GalleryEvent.FolderClicked(folder))
                },
                onMediaClick = { file ->
                    viewModel.onEvent(GalleryEvent.MediaClicked(file))
                },
                onLoadMore = { viewModel.onEvent(GalleryEvent.LoadMore) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryContent(
    uiState: GalleryUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onFolderClick: (DiskItem.Directory) -> Unit,
    onMediaClick: (DiskItem.File) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            // Initial loading
            uiState.isLoading && uiState.items.isEmpty() -> {
                SkeletonGrid()
            }

            // Error state with no data
            uiState.error != null && uiState.items.isEmpty() -> {
                ErrorView(
                    message = uiState.error,
                    onRetry = onRetry,
                )
            }

            // Empty state
            uiState.isEmpty -> {
                EmptyStateView(
                    title = "No items found",
                    message = "This folder is empty",
                )
            }

            // Content with pull-to-refresh
            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (uiState.viewMode) {
                        ViewMode.GRID -> {
                            GalleryGrid(
                                items = uiState.items,
                                onFolderClick = onFolderClick,
                                onMediaClick = onMediaClick,
                                onLoadMore = onLoadMore,
                                isLoadingMore = uiState.isLoadingMore,
                                hasMoreItems = uiState.hasMoreItems,
                            )
                        }
                        ViewMode.LIST -> {
                            GalleryList(
                                items = uiState.items,
                                onFolderClick = onFolderClick,
                                onMediaClick = onMediaClick,
                                onLoadMore = onLoadMore,
                                isLoadingMore = uiState.isLoadingMore,
                                hasMoreItems = uiState.hasMoreItems,
                            )
                        }
                    }
                }
            }
        }
    }
}
