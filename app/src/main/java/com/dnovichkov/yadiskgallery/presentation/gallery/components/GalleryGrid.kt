package com.dnovichkov.yadiskgallery.presentation.gallery.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.domain.model.DiskItem
import com.dnovichkov.yadiskgallery.presentation.components.LoadingIndicator

/**
 * Grid view for displaying gallery items.
 */
@Composable
fun GalleryGrid(
    items: List<DiskItem>,
    onFolderClick: (DiskItem.Directory) -> Unit,
    onMediaClick: (DiskItem.File) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    isLoadingMore: Boolean = false,
    hasMoreItems: Boolean = false,
) {
    val gridState = rememberLazyGridState()

    // Detect when we need to load more items
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem =
                gridState.layoutInfo.visibleItemsInfo.lastOrNull()
                    ?: return@derivedStateOf false

            lastVisibleItem.index >= items.size - LOAD_MORE_THRESHOLD
        }
    }

    LaunchedEffect(shouldLoadMore, hasMoreItems, isLoadingMore) {
        if (shouldLoadMore && hasMoreItems && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        modifier = modifier.fillMaxSize(),
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = items,
            key = { it.id },
            contentType = { item ->
                when (item) {
                    is DiskItem.Directory -> "folder"
                    is DiskItem.File -> "media"
                }
            },
        ) { item ->
            when (item) {
                is DiskItem.Directory -> {
                    FolderGridItem(
                        folder = item.folder,
                        onClick = { onFolderClick(item) },
                    )
                }
                is DiskItem.File -> {
                    MediaGridItem(
                        mediaFile = item.mediaFile,
                        onClick = { onMediaClick(item) },
                    )
                }
            }
        }

        // Loading indicator at the bottom
        if (isLoadingMore) {
            item(
                span = { GridItemSpan(GRID_COLUMNS) },
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }
        }
    }
}

private const val GRID_COLUMNS = 3
private const val LOAD_MORE_THRESHOLD = 6
