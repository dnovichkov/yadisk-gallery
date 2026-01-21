package com.dnovichkov.yadiskgallery.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dnovichkov.yadiskgallery.domain.model.AuthState
import com.dnovichkov.yadiskgallery.domain.model.UserInfo
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * Card displaying the current authentication status.
 */
@Composable
fun AuthStatusCard(
    authState: AuthState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when (authState) {
                        is AuthState.Authenticated -> MaterialTheme.colorScheme.primaryContainer
                        is AuthState.AuthError -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuthStatusIcon(authState = authState)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getAuthStatusTitle(authState),
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = getAuthStatusDescription(authState),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AuthStatusIcon(authState: AuthState) {
    when (authState) {
        is AuthState.Authenticated -> {
            val userInfo = authState.userInfo
            if (userInfo.avatarUrl != null) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(userInfo.avatarUrl)
                            .crossfade(true)
                            .build(),
                    contentDescription = "User avatar",
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Authenticated",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        is AuthState.Authenticating -> {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
            )
        }
        is AuthState.AuthError -> {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Authentication error",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error,
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Not authenticated",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun getAuthStatusTitle(authState: AuthState): String {
    return when (authState) {
        is AuthState.Authenticated ->
            authState.userInfo.displayName
                ?: authState.userInfo.login
        is AuthState.Authenticating -> "Signing in..."
        is AuthState.PublicAccess -> "Public access"
        is AuthState.AuthError -> "Authentication failed"
        AuthState.NotAuthenticated -> "Not signed in"
    }
}

private fun getAuthStatusDescription(authState: AuthState): String {
    return when (authState) {
        is AuthState.Authenticated -> authState.userInfo.login
        is AuthState.Authenticating -> "Please wait"
        is AuthState.PublicAccess -> "Using public folder"
        is AuthState.AuthError -> authState.message
        AuthState.NotAuthenticated -> "Sign in to access your Yandex.Disk"
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthStatusCardNotAuthenticatedPreview() {
    YaDiskGalleryTheme {
        AuthStatusCard(
            authState = AuthState.NotAuthenticated,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthStatusCardAuthenticatingPreview() {
    YaDiskGalleryTheme {
        AuthStatusCard(
            authState = AuthState.Authenticating,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthStatusCardAuthenticatedPreview() {
    YaDiskGalleryTheme {
        AuthStatusCard(
            authState =
                AuthState.Authenticated(
                    UserInfo(
                        uid = "123",
                        login = "test@yandex.ru",
                        displayName = "Test User",
                        avatarUrl = null,
                    ),
                ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthStatusCardErrorPreview() {
    YaDiskGalleryTheme {
        AuthStatusCard(
            authState = AuthState.AuthError("Invalid credentials"),
            modifier = Modifier.padding(16.dp),
        )
    }
}
