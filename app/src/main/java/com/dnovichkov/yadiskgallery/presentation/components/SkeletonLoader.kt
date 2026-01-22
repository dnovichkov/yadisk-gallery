package com.dnovichkov.yadiskgallery.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * Creates a shimmer effect brush for skeleton loading.
 */
@Composable
private fun shimmerBrush(): Brush {
    val shimmerColors =
        listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 1000,
                        easing = FastOutSlowInEasing,
                    ),
                repeatMode = RepeatMode.Restart,
            ),
        label = "shimmer_translate",
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation - 500f, translateAnimation - 500f),
        end = Offset(translateAnimation, translateAnimation),
    )
}

/**
 * A box with shimmer effect for skeleton loading.
 *
 * @param modifier Modifier for the box
 */
@Composable
fun SkeletonBox(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush()),
    )
}

/**
 * Skeleton loader for a grid item (thumbnail).
 */
@Composable
fun SkeletonGridItem(modifier: Modifier = Modifier) {
    SkeletonBox(
        modifier =
            modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
    )
}

/**
 * Skeleton loader for a list item.
 */
@Composable
fun SkeletonListItem(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Thumbnail placeholder
        SkeletonBox(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Title placeholder
            SkeletonBox(
                modifier =
                    Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle placeholder
            SkeletonBox(
                modifier =
                    Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp),
            )
        }
    }
}

/**
 * Skeleton loader for a full gallery grid.
 *
 * @param itemCount Number of skeleton items to display
 * @param columns Number of grid columns
 * @param modifier Modifier for the grid
 */
@Composable
fun SkeletonGrid(
    modifier: Modifier = Modifier,
    itemCount: Int = 12,
    columns: Int = 3,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier =
            modifier
                .padding(4.dp)
                .semantics { contentDescription = "Loading content" },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(itemCount) {
            SkeletonGridItem()
        }
    }
}

/**
 * Skeleton loader for a user avatar.
 */
@Composable
fun SkeletonAvatar(
    modifier: Modifier = Modifier,
    size: Int = 40,
) {
    SkeletonBox(
        modifier =
            modifier
                .size(size.dp)
                .clip(CircleShape),
    )
}

@Preview(showBackground = true)
@Composable
private fun SkeletonGridItemPreview() {
    YaDiskGalleryTheme {
        SkeletonGridItem(modifier = Modifier.size(100.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun SkeletonListItemPreview() {
    YaDiskGalleryTheme {
        SkeletonListItem()
    }
}

@Preview(showBackground = true)
@Composable
private fun SkeletonGridPreview() {
    YaDiskGalleryTheme {
        SkeletonGrid(itemCount = 9, columns = 3)
    }
}
