package com.dnovichkov.yadiskgallery.presentation.gallery.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.domain.model.ViewMode

/**
 * Top app bar for the gallery screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryTopBar(
    title: String,
    viewMode: ViewMode,
    sortOrder: SortOrder,
    onViewModeChange: (ViewMode) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSortMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        actions = {
            // View mode toggle
            IconButton(
                onClick = {
                    val newMode = if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                    onViewModeChange(newMode)
                },
            ) {
                Icon(
                    imageVector =
                        if (viewMode == ViewMode.GRID) {
                            Icons.AutoMirrored.Filled.List
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
            Row {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort options",
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                ) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = getSortOrderLabel(order),
                                    style =
                                        if (order == sortOrder) {
                                            MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        } else {
                                            MaterialTheme.typography.bodyMedium
                                        },
                                )
                            },
                            onClick = {
                                onSortOrderChange(order)
                                showSortMenu = false
                            },
                        )
                    }
                }
            }

            // Settings
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    )
}

private fun getSortOrderLabel(sortOrder: SortOrder): String =
    when (sortOrder) {
        SortOrder.NAME_ASC -> "Name (A-Z)"
        SortOrder.NAME_DESC -> "Name (Z-A)"
        SortOrder.DATE_ASC -> "Date (oldest)"
        SortOrder.DATE_DESC -> "Date (newest)"
        SortOrder.SIZE_ASC -> "Size (smallest)"
        SortOrder.SIZE_DESC -> "Size (largest)"
    }
