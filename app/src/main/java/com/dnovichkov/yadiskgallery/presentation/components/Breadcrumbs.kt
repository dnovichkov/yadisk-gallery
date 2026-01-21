package com.dnovichkov.yadiskgallery.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * Data class representing a single breadcrumb item.
 *
 * @param name Display name of the breadcrumb
 * @param path Full path to this location
 */
data class BreadcrumbItem(
    val name: String,
    val path: String,
)

/**
 * Breadcrumbs navigation component.
 *
 * @param items List of breadcrumb items from root to current
 * @param onItemClick Callback when a breadcrumb item is clicked
 * @param modifier Modifier for the component
 */
@Composable
fun Breadcrumbs(
    items: List<BreadcrumbItem>,
    onItemClick: (BreadcrumbItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    // Auto-scroll to end when items change
    LaunchedEffect(items) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier =
            modifier
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Home icon for root
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Root",
            tint =
                if (items.isEmpty()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            modifier =
                Modifier
                    .clickable {
                        onItemClick(BreadcrumbItem(name = "Root", path = "/"))
                    }
                    .padding(8.dp),
        )

        items.forEachIndexed { index, item ->
            // Separator
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )

            // Breadcrumb item
            val isLast = index == items.lastIndex
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (isLast) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .clickable(enabled = !isLast) { onItemClick(item) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

/**
 * Creates breadcrumb items from a path string.
 *
 * @param path Full path string (e.g., "/Photos/2024/January")
 * @return List of BreadcrumbItem representing each path segment
 */
fun createBreadcrumbsFromPath(path: String): List<BreadcrumbItem> {
    if (path.isBlank() || path == "/") return emptyList()

    val segments = path.trim('/').split('/')
    val items = mutableListOf<BreadcrumbItem>()
    var currentPath = ""

    segments.forEach { segment ->
        if (segment.isNotBlank()) {
            currentPath += "/$segment"
            items.add(BreadcrumbItem(name = segment, path = currentPath))
        }
    }

    return items
}

@Preview(showBackground = true)
@Composable
private fun BreadcrumbsPreview() {
    YaDiskGalleryTheme {
        Breadcrumbs(
            items =
                listOf(
                    BreadcrumbItem("Photos", "/Photos"),
                    BreadcrumbItem("2024", "/Photos/2024"),
                    BreadcrumbItem("January", "/Photos/2024/January"),
                ),
            onItemClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BreadcrumbsRootPreview() {
    YaDiskGalleryTheme {
        Breadcrumbs(
            items = emptyList(),
            onItemClick = {},
        )
    }
}
