package com.dnovichkov.yadiskgallery.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * A view displaying an error state with retry button.
 *
 * @param message Error message to display
 * @param modifier Modifier for the container
 * @param title Optional title for the error
 * @param onRetry Optional callback for retry button click
 */
@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Error",
    onRetry: (() -> Unit)? = null,
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
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}

/**
 * Network error view with specific styling.
 */
@Composable
fun NetworkErrorView(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    ErrorView(
        title = "No Connection",
        message = "Please check your internet connection and try again.",
        modifier = modifier,
        onRetry = onRetry,
    )
}

/**
 * Not found error view.
 */
@Composable
fun NotFoundView(
    message: String = "The requested content could not be found.",
    modifier: Modifier = Modifier,
) {
    ErrorView(
        title = "Not Found",
        message = message,
        modifier = modifier,
        onRetry = null,
    )
}

@Preview(showBackground = true)
@Composable
private fun ErrorViewPreview() {
    YaDiskGalleryTheme {
        ErrorView(
            message = "Something went wrong. Please try again.",
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NetworkErrorViewPreview() {
    YaDiskGalleryTheme {
        NetworkErrorView(onRetry = {})
    }
}
