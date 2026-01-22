package com.dnovichkov.yadiskgallery.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * Section displaying cache information and clear cache button.
 */
@Composable
fun CacheInfoSection(
    cacheSize: Long,
    onClearCache: () -> Unit,
    isClearing: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cache",
                        style = MaterialTheme.typography.titleSmall,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatFileSize(cacheSize),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val buttonDescription =
                if (isClearing) "Clearing cache" else "Clear cache, ${formatFileSize(cacheSize)}"
            OutlinedButton(
                onClick = onClearCache,
                enabled = !isClearing && cacheSize > 0,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = buttonDescription },
            ) {
                if (isClearing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    // Decorative icon, button text describes action
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = if (isClearing) "Clearing..." else "Clear cache",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cached images and data will be deleted. This won't affect your Yandex.Disk files.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Formats a file size in bytes to a human-readable string.
 */
private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()

    return DecimalFormat("#,##0.#")
        .format(bytes / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
}

@Preview(showBackground = true)
@Composable
private fun CacheInfoSectionEmptyPreview() {
    YaDiskGalleryTheme {
        CacheInfoSection(
            cacheSize = 0L,
            onClearCache = {},
            isClearing = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CacheInfoSectionWithDataPreview() {
    YaDiskGalleryTheme {
        CacheInfoSection(
            // 50 MB
            cacheSize = 52_428_800L,
            onClearCache = {},
            isClearing = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CacheInfoSectionClearingPreview() {
    YaDiskGalleryTheme {
        CacheInfoSection(
            cacheSize = 52_428_800L,
            onClearCache = {},
            isClearing = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}
