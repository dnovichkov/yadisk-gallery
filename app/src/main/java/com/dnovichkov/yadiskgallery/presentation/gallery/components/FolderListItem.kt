package com.dnovichkov.yadiskgallery.presentation.gallery.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.domain.model.Folder
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * List item for displaying a folder with details.
 */
@Composable
fun FolderListItem(
    folder: Folder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Folder icon
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Folder details
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                folder.itemsCount?.let { count ->
                    Text(
                        text = "$count items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                folder.modifiedAt?.let { date ->
                    Text(
                        text = formatDate(date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Arrow indicator
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open folder",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatDate(instant: java.time.Instant): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    return instant.atZone(ZoneId.systemDefault()).format(formatter)
}
