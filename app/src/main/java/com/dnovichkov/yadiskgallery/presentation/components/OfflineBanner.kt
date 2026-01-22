package com.dnovichkov.yadiskgallery.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.data.network.ConnectionType
import com.dnovichkov.yadiskgallery.data.network.ConnectivityState
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * Banner displayed when the device is offline.
 *
 * @param isOffline Whether the device is offline
 * @param modifier Modifier for the banner
 */
@Composable
fun OfflineBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = "No internet connection",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

/**
 * Banner that shows connectivity state with more details.
 *
 * @param connectivityState Current connectivity state
 * @param modifier Modifier for the banner
 */
@Composable
fun ConnectivityBanner(
    connectivityState: ConnectivityState,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = connectivityState.isOffline,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = "Offline mode - showing cached data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

/**
 * Compact connection status indicator.
 *
 * @param connectivityState Current connectivity state
 * @param modifier Modifier for the indicator
 */
@Composable
fun ConnectionIndicator(
    connectivityState: ConnectivityState,
    modifier: Modifier = Modifier,
) {
    val (icon, color) =
        when (connectivityState) {
            is ConnectivityState.Connected -> {
                when (connectivityState.connectionType) {
                    ConnectionType.WIFI -> Icons.Default.Wifi to Color.Green
                    else -> Icons.Default.Wifi to Color.Green
                }
            }
            is ConnectivityState.Disconnected -> Icons.Default.WifiOff to Color.Red
            is ConnectivityState.Unknown -> Icons.Default.Wifi to Color.Gray
        }

    Icon(
        imageVector = icon,
        contentDescription =
            if (connectivityState.isOnline) {
                "Connected"
            } else {
                "Disconnected"
            },
        modifier = modifier.size(20.dp),
        tint = color,
    )
}

@Preview(showBackground = true)
@Composable
private fun OfflineBannerPreview() {
    YaDiskGalleryTheme {
        OfflineBanner(isOffline = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectivityBannerPreview() {
    YaDiskGalleryTheme {
        ConnectivityBanner(connectivityState = ConnectivityState.Disconnected)
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectionIndicatorPreview() {
    YaDiskGalleryTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ConnectionIndicator(
                connectivityState = ConnectivityState.Connected(ConnectionType.WIFI),
            )
            ConnectionIndicator(connectivityState = ConnectivityState.Disconnected)
        }
    }
}
