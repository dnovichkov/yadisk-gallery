package com.dnovichkov.yadiskgallery.presentation.gallery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.domain.model.MediaType
import com.dnovichkov.yadiskgallery.presentation.components.LoadingIndicator

/**
 * Grid item for displaying a media file (image or video) thumbnail.
 */
@Composable
fun MediaGridItem(
    mediaFile: MediaFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typeDescription = if (mediaFile.type == MediaType.VIDEO) "video" else "image"
    val actionDescription = "Open $typeDescription ${mediaFile.name}"

    Card(
        modifier =
            modifier
                .aspectRatio(1f)
                .semantics {
                    contentDescription = actionDescription
                    role = Role.Button
                }
                .clickable(
                    onClick = onClick,
                    onClickLabel = "Open $typeDescription",
                ),
        shape = RoundedCornerShape(8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // Thumbnail image
            SubcomposeAsyncImage(
                model =
                    ImageRequest.Builder(LocalContext.current)
                        .data(mediaFile.previewUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = mediaFile.name,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
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
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )

            // Video play indicator
            if (mediaFile.type == MediaType.VIDEO) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    // Decorative icon, described by card semantics
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White,
                    )
                }
            }

            // Duration badge for videos (if available)
            if (mediaFile.type == MediaType.VIDEO) {
                VideoDurationBadge(
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                )
            }
        }
    }
}

@Composable
private fun VideoDurationBadge(modifier: Modifier = Modifier) {
    // Placeholder for duration badge
    // In a full implementation, this would show the video duration
}
