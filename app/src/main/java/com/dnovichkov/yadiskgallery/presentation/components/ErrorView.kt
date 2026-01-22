package com.dnovichkov.yadiskgallery.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.domain.model.DomainError
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
    modifier: Modifier = Modifier,
    message: String = "The requested content could not be found.",
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

/**
 * Error view that displays appropriate UI based on DomainError type.
 *
 * @param error The domain error to display
 * @param modifier Modifier for the container
 * @param onRetry Optional callback for retry button click
 */
@Composable
fun DomainErrorView(
    error: DomainError,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val (icon, title, message, showRetry) = getErrorDisplayInfo(error)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
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

        if (showRetry && onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}

/**
 * Returns display information for a DomainError.
 */
private fun getErrorDisplayInfo(error: DomainError): ErrorDisplayInfo {
    return when (error) {
        is DomainError.Network.NoConnection ->
            ErrorDisplayInfo(
                icon = Icons.Default.WifiOff,
                title = "No Connection",
                message = "Please check your internet connection and try again.",
                showRetry = true,
            )
        is DomainError.Network.Timeout ->
            ErrorDisplayInfo(
                icon = Icons.Default.CloudOff,
                title = "Request Timeout",
                message = "The server took too long to respond. Please try again.",
                showRetry = true,
            )
        is DomainError.Network.ServerError ->
            ErrorDisplayInfo(
                icon = Icons.Default.Error,
                title = "Server Error",
                message = error.serverMessage,
                showRetry = true,
            )
        is DomainError.Network.Unknown ->
            ErrorDisplayInfo(
                icon = Icons.Default.Warning,
                title = "Network Error",
                message = error.message,
                showRetry = true,
            )
        is DomainError.Auth.Unauthorized ->
            ErrorDisplayInfo(
                icon = Icons.Default.Lock,
                title = "Authentication Required",
                message = "Please log in to access this content.",
                showRetry = false,
            )
        is DomainError.Auth.TokenExpired ->
            ErrorDisplayInfo(
                icon = Icons.Default.Lock,
                title = "Session Expired",
                message = "Your session has expired. Please log in again.",
                showRetry = false,
            )
        is DomainError.Auth.InvalidCredentials ->
            ErrorDisplayInfo(
                icon = Icons.Default.Lock,
                title = "Invalid Credentials",
                message = "The provided credentials are incorrect.",
                showRetry = false,
            )
        is DomainError.Auth.AuthCancelled ->
            ErrorDisplayInfo(
                icon = Icons.Default.Warning,
                title = "Authentication Cancelled",
                message = "You cancelled the authentication process.",
                showRetry = false,
            )
        is DomainError.Disk.NotFound ->
            ErrorDisplayInfo(
                icon = Icons.Default.SearchOff,
                title = "Not Found",
                message = "The requested file or folder could not be found.",
                showRetry = false,
            )
        is DomainError.Disk.AccessDenied ->
            ErrorDisplayInfo(
                icon = Icons.Default.Lock,
                title = "Access Denied",
                message = "You don't have permission to access this content.",
                showRetry = false,
            )
        is DomainError.Disk.QuotaExceeded ->
            ErrorDisplayInfo(
                icon = Icons.Default.Warning,
                title = "Storage Full",
                message = "Your Yandex.Disk storage quota has been exceeded.",
                showRetry = false,
            )
        is DomainError.Disk.InvalidPublicUrl ->
            ErrorDisplayInfo(
                icon = Icons.Default.Warning,
                title = "Invalid Link",
                message = "The public folder link is invalid.",
                showRetry = false,
            )
        is DomainError.Disk.PublicLinkExpired ->
            ErrorDisplayInfo(
                icon = Icons.Default.Warning,
                title = "Link Expired",
                message = "This public link has expired or been revoked.",
                showRetry = false,
            )
        is DomainError.Cache.ReadError,
        is DomainError.Cache.WriteError,
        ->
            ErrorDisplayInfo(
                icon = Icons.Default.Warning,
                title = "Cache Error",
                message = error.message,
                showRetry = true,
            )
        is DomainError.Validation.InvalidUrl,
        is DomainError.Validation.EmptyField,
        ->
            ErrorDisplayInfo(
                icon = Icons.Default.Warning,
                title = "Validation Error",
                message = error.message,
                showRetry = false,
            )
        is DomainError.Unknown ->
            ErrorDisplayInfo(
                icon = Icons.Default.Warning,
                title = "Error",
                message = error.message,
                showRetry = true,
            )
    }
}

/**
 * Data class holding display information for an error.
 */
private data class ErrorDisplayInfo(
    val icon: ImageVector,
    val title: String,
    val message: String,
    val showRetry: Boolean,
)

/**
 * Auth error view prompting user to log in.
 */
@Composable
fun AuthRequiredView(
    modifier: Modifier = Modifier,
    onLogin: (() -> Unit)? = null,
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
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Authentication Required",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please log in to access your Yandex.Disk files.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (onLogin != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onLogin) {
                Text(text = "Log In")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NetworkErrorViewPreview() {
    YaDiskGalleryTheme {
        NetworkErrorView(onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DomainErrorViewPreview() {
    YaDiskGalleryTheme {
        DomainErrorView(
            error = DomainError.Network.NoConnection,
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthRequiredViewPreview() {
    YaDiskGalleryTheme {
        AuthRequiredView(onLogin = {})
    }
}
