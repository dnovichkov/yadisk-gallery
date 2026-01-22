package com.dnovichkov.yadiskgallery.presentation.gallery.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.domain.model.MediaType
import com.dnovichkov.yadiskgallery.presentation.components.LoadingIndicator
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * List item for displaying a media file with details.
 */
@Composable
fun MediaListItem(
    mediaFile: MediaFile,
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
                    .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thumbnail
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                SubcomposeAsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(mediaFile.previewUrl)
                            .crossfade(true)
                            .build(),
                    contentDescription = mediaFile.name,
                    modifier =
                        Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        LoadingIndicator(
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    error = {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = "Failed to load",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )

                // Video play indicator overlay
                if (mediaFile.type == MediaType.VIDEO) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Video",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File details
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = mediaFile.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = formatFileSize(mediaFile.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                mediaFile.modifiedAt?.let { date ->
                    Text(
                        text = formatDate(date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Type icon
            Icon(
                imageVector =
                    if (mediaFile.type == MediaType.IMAGE) {
                        Icons.Default.Image
                    } else {
                        Icons.Default.VideoFile
                    },
                contentDescription = if (mediaFile.type == MediaType.IMAGE) "Image" else "Video",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

private fun formatDate(instant: java.time.Instant): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    return instant.atZone(ZoneId.systemDefault()).format(formatter)
}
