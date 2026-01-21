package com.dnovichkov.yadiskgallery.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * A view displaying an empty state with icon and message.
 *
 * @param message Message to display
 * @param modifier Modifier for the container
 * @param icon Optional icon to display
 * @param title Optional title
 */
@Composable
fun EmptyStateView(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Folder,
    title: String? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Empty folder state view.
 */
@Composable
fun EmptyFolderView(modifier: Modifier = Modifier) {
    EmptyStateView(
        title = "Empty Folder",
        message = "This folder doesn't contain any files or subfolders.",
        icon = Icons.Default.Folder,
        modifier = modifier,
    )
}

/**
 * No media files state view.
 */
@Composable
fun NoMediaView(modifier: Modifier = Modifier) {
    EmptyStateView(
        title = "No Media",
        message = "No photos or videos found in this location.",
        icon = Icons.Default.PhotoLibrary,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewPreview() {
    YaDiskGalleryTheme {
        EmptyStateView(
            title = "No Results",
            message = "Try a different search term.",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyFolderViewPreview() {
    YaDiskGalleryTheme {
        EmptyFolderView()
    }
}
