package com.dnovichkov.yadiskgallery.presentation.gallery.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
 * List view for displaying gallery items.
 */
@Composable
fun GalleryList(
    items: List<DiskItem>,
    onFolderClick: (DiskItem.Directory) -> Unit,
    onMediaClick: (DiskItem.File) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    isLoadingMore: Boolean = false,
    hasMoreItems: Boolean = false,
) {
    val listState = rememberLazyListState()

    // Detect when we need to load more items
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()
                    ?: return@derivedStateOf false

            lastVisibleItem.index >= items.size - LOAD_MORE_THRESHOLD
        }
    }

    LaunchedEffect(shouldLoadMore, hasMoreItems, isLoadingMore) {
        if (shouldLoadMore && hasMoreItems && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(8.dp),
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
                    FolderListItem(
                        folder = item.folder,
                        onClick = { onFolderClick(item) },
                    )
                }
                is DiskItem.File -> {
                    MediaListItem(
                        mediaFile = item.mediaFile,
                        onClick = { onMediaClick(item) },
                    )
                }
            }
        }

        // Loading indicator at the bottom
        if (isLoadingMore) {
            item {
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

private const val LOAD_MORE_THRESHOLD = 5
