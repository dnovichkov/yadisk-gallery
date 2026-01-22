package com.dnovichkov.yadiskgallery.presentation.viewer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Navigation arrows for browsing between images.
 *
 * @param hasPreviousImage Whether there is a previous image to navigate to
 * @param hasNextImage Whether there is a next image to navigate to
 * @param isVisible Whether the arrows are visible
 * @param onPreviousClick Callback when previous arrow is clicked
 * @param onNextClick Callback when next arrow is clicked
 * @param modifier Modifier for the composable
 */
@Composable
fun NavigationArrows(
    hasPreviousImage: Boolean,
    hasNextImage: Boolean,
    isVisible: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Previous arrow (left side)
            if (hasPreviousImage) {
                NavigationArrowButton(
                    onClick = onPreviousClick,
                    contentDescription = "Previous image",
                    isForward = false,
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp),
                )
            }

            // Next arrow (right side)
            if (hasNextImage) {
                NavigationArrowButton(
                    onClick = onNextClick,
                    contentDescription = "Next image",
                    isForward = true,
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun NavigationArrowButton(
    onClick: () -> Unit,
    contentDescription: String,
    isForward: Boolean,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier =
            modifier
                .size(48.dp)
                .alpha(0.7f)
                .background(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = CircleShape,
                ),
    ) {
        Icon(
            imageVector =
                if (isForward) {
                    Icons.AutoMirrored.Filled.ArrowForward
                } else {
                    Icons.AutoMirrored.Filled.ArrowBack
                },
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(32.dp),
        )
    }
}
