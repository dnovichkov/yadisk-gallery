package com.dnovichkov.yadiskgallery.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * A centered loading indicator with optional message.
 *
 * @param modifier Modifier for the container
 * @param message Optional message to display below the indicator
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
            )
            if (message != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * A small inline loading indicator.
 *
 * @param modifier Modifier for the indicator
 */
@Composable
fun SmallLoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier.size(24.dp),
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 2.dp,
    )
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    YaDiskGalleryTheme {
        LoadingIndicator(message = "Loading...")
    }
}

@Preview(showBackground = true)
@Composable
private fun SmallLoadingIndicatorPreview() {
    YaDiskGalleryTheme {
        SmallLoadingIndicator()
    }
}
