package com.dnovichkov.yadiskgallery.presentation.gallery.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Skeleton loading placeholder for the gallery grid.
 * Displays shimmer animation while content is loading.
 */
@Composable
fun SkeletonGrid(
    modifier: Modifier = Modifier,
    itemCount: Int = SKELETON_ITEM_COUNT,
) {
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
                        durationMillis = 1200,
                        easing = LinearEasing,
                    ),
                repeatMode = RepeatMode.Restart,
            ),
        label = "shimmer_translate",
    )

    val brush =
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnimation - 500f, translateAnimation - 500f),
            end = Offset(translateAnimation, translateAnimation),
        )

    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false,
    ) {
        items(itemCount) {
            SkeletonGridItem(brush = brush)
        }
    }
}

@Composable
private fun SkeletonGridItem(
    brush: Brush,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface),
    ) {
        // Thumbnail placeholder
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(brush),
        )

        // Text placeholder
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush),
        )
    }
}

private const val GRID_COLUMNS = 3
private const val SKELETON_ITEM_COUNT = 12
