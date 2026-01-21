package com.dnovichkov.yadiskgallery.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * View mode for gallery display.
 */
enum class ViewMode {
    GRID,
    LIST,
}

/**
 * Gallery top app bar with navigation, view mode toggle, and sort options.
 *
 * @param title Title to display
 * @param onNavigationClick Callback for navigation icon click
 * @param viewMode Current view mode
 * @param onViewModeChange Callback when view mode changes
 * @param sortOrder Current sort order
 * @param onSortOrderChange Callback when sort order changes
 * @param onSettingsClick Callback for settings click
 * @param canNavigateBack Whether back navigation is available
 * @param modifier Modifier for the app bar
 * @param scrollBehavior Optional scroll behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryTopAppBar(
    title: String,
    onNavigationClick: () -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        },
        actions = {
            // View mode toggle
            IconButton(onClick = {
                val newMode = if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                onViewModeChange(newMode)
            }) {
                Icon(
                    imageVector =
                        if (viewMode == ViewMode.GRID) {
                            Icons.AutoMirrored.Filled.ViewList
                        } else {
                            Icons.Default.GridView
                        },
                    contentDescription =
                        if (viewMode == ViewMode.GRID) {
                            "Switch to list view"
                        } else {
                            "Switch to grid view"
                        },
                )
            }

            // Sort menu
            IconButton(onClick = { showSortMenu = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort",
                )
            }
            SortDropdownMenu(
                expanded = showSortMenu,
                currentSortOrder = sortOrder,
                onDismiss = { showSortMenu = false },
                onSortOrderSelected = {
                    onSortOrderChange(it)
                    showSortMenu = false
                },
            )

            // More menu
            IconButton(onClick = { showMoreMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                )
            }
            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = {
                        showMoreMenu = false
                        onSettingsClick()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    },
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    )
}

/**
 * Sort order dropdown menu.
 */
@Composable
private fun SortDropdownMenu(
    expanded: Boolean,
    currentSortOrder: SortOrder,
    onDismiss: () -> Unit,
    onSortOrderSelected: (SortOrder) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        SortOrder.entries.forEach { sortOrder ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = sortOrder.displayName,
                        color =
                            if (sortOrder == currentSortOrder) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                },
                onClick = { onSortOrderSelected(sortOrder) },
            )
        }
    }
}

/**
 * Display name for sort orders.
 */
private val SortOrder.displayName: String
    get() =
        when (this) {
            SortOrder.NAME_ASC -> "Name (A-Z)"
            SortOrder.NAME_DESC -> "Name (Z-A)"
            SortOrder.DATE_ASC -> "Date (oldest)"
            SortOrder.DATE_DESC -> "Date (newest)"
            SortOrder.SIZE_ASC -> "Size (smallest)"
            SortOrder.SIZE_DESC -> "Size (largest)"
        }

/**
 * Simple top app bar for viewer screens.
 *
 * @param title Title to display
 * @param onNavigationClick Callback for navigation icon click
 * @param modifier Modifier for the app bar
 * @param actions Optional composable for action icons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerTopAppBar(
    title: String,
    onNavigationClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        actions = { actions() },
        colors =
            TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun GalleryTopAppBarPreview() {
    YaDiskGalleryTheme {
        GalleryTopAppBar(
            title = "Gallery",
            onNavigationClick = {},
            viewMode = ViewMode.GRID,
            onViewModeChange = {},
            sortOrder = SortOrder.DATE_DESC,
            onSortOrderChange = {},
            onSettingsClick = {},
            canNavigateBack = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ViewerTopAppBarPreview() {
    YaDiskGalleryTheme {
        ViewerTopAppBar(
            title = "photo.jpg",
            onNavigationClick = {},
        )
    }
}
