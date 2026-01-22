package com.dnovichkov.yadiskgallery.presentation.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * Selector for choosing the root folder path.
 */
@Composable
fun RootFolderSelector(
    currentPath: String?,
    onSelectFolder: () -> Unit,
    onClearFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val folderDescription = currentPath ?: "Disk root"
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Root folder: $folderDescription. Tap to change."
                        role = Role.Button
                    }
                    .clickable(
                        onClick = onSelectFolder,
                        onClickLabel = "Select root folder",
                    )
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Root folder",
                    style = MaterialTheme.typography.titleSmall,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentPath ?: "Disk root",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (currentPath != null) {
                IconButton(onClick = onClearFolder) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear root folder",
                    )
                }
            } else {
                // Decorative icon, card semantics describes the action
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RootFolderSelectorDefaultPreview() {
    YaDiskGalleryTheme {
        RootFolderSelector(
            currentPath = null,
            onSelectFolder = {},
            onClearFolder = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RootFolderSelectorWithPathPreview() {
    YaDiskGalleryTheme {
        RootFolderSelector(
            currentPath = "/Photos/2024",
            onSelectFolder = {},
            onClearFolder = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
