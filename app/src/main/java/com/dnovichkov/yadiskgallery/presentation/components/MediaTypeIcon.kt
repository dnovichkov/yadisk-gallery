package com.dnovichkov.yadiskgallery.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.domain.model.MediaType
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * Icon component for displaying media type.
 *
 * @param mediaType Type of media (IMAGE, VIDEO)
 * @param modifier Modifier for the icon
 * @param size Size of the icon
 * @param tint Icon tint color
 */
@Composable
fun MediaTypeIcon(
    mediaType: MediaType,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val icon =
        when (mediaType) {
            MediaType.IMAGE -> Icons.Default.Image
            MediaType.VIDEO -> Icons.Default.Videocam
        }

    val contentDescription =
        when (mediaType) {
            MediaType.IMAGE -> "Image"
            MediaType.VIDEO -> "Video"
        }

    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}

/**
 * Folder icon component.
 *
 * @param modifier Modifier for the icon
 * @param size Size of the icon
 * @param tint Icon tint color
 */
@Composable
fun FolderIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Icon(
        imageVector = Icons.Default.Folder,
        contentDescription = "Folder",
        modifier = modifier.size(size),
        tint = tint,
    )
}

/**
 * Video play overlay icon for thumbnails.
 *
 * @param modifier Modifier for the icon
 * @param size Size of the icon
 */
@Composable
fun VideoPlayOverlay(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(4.dp),
    ) {
        Icon(
            imageVector = Icons.Default.PlayCircle,
            contentDescription = "Play video",
            modifier = Modifier.size(size - 8.dp),
            tint = Color.White,
        )
    }
}

/**
 * Small media type badge for grid items.
 *
 * @param mediaType Type of media
 * @param modifier Modifier for the badge
 */
@Composable
fun MediaTypeBadge(
    mediaType: MediaType,
    modifier: Modifier = Modifier,
) {
    if (mediaType == MediaType.VIDEO) {
        Box(
            modifier =
                modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = "Video",
                modifier = Modifier.size(16.dp),
                tint = Color.White,
            )
        }
    }
}

/**
 * Returns the appropriate icon for a file or folder.
 */
fun getIconForItem(
    isFolder: Boolean,
    mediaType: MediaType?,
): ImageVector {
    return when {
        isFolder -> Icons.Default.Folder
        mediaType == MediaType.VIDEO -> Icons.Default.Videocam
        else -> Icons.Default.Image
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaTypeIconImagePreview() {
    YaDiskGalleryTheme {
        MediaTypeIcon(mediaType = MediaType.IMAGE)
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaTypeIconVideoPreview() {
    YaDiskGalleryTheme {
        MediaTypeIcon(mediaType = MediaType.VIDEO)
    }
}

@Preview(showBackground = true)
@Composable
private fun FolderIconPreview() {
    YaDiskGalleryTheme {
        FolderIcon()
    }
}

@Preview(showBackground = true)
@Composable
private fun VideoPlayOverlayPreview() {
    YaDiskGalleryTheme {
        VideoPlayOverlay()
    }
}
