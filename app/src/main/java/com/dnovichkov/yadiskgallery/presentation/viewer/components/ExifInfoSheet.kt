package com.dnovichkov.yadiskgallery.presentation.viewer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ImageAspectRatio
import androidx.compose.material.icons.outlined.Iso
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ShutterSpeed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.domain.model.MediaFile
import com.dnovichkov.yadiskgallery.presentation.viewer.ExifData
import java.time.format.DateTimeFormatter

/**
 * Bottom sheet displaying EXIF information for an image.
 *
 * @param mediaFile The media file to display info for
 * @param exifData Optional EXIF metadata
 * @param sheetState Sheet state for controlling visibility
 * @param onDismiss Callback when sheet is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExifInfoSheet(
    mediaFile: MediaFile,
    exifData: ExifData?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
        ) {
            Text(
                text = mediaFile.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // File info
            InfoRow(
                icon = Icons.Outlined.ImageAspectRatio,
                label = "Размер",
                value = formatFileSize(mediaFile.size),
            )

            mediaFile.createdAt?.let { created ->
                InfoRow(
                    icon = Icons.Outlined.CalendarToday,
                    label = "Дата создания",
                    value =
                        created.atZone(java.time.ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                )
            }

            // EXIF data
            exifData?.let { exif ->
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "EXIF данные",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                if (exif.width != null && exif.height != null) {
                    InfoRow(
                        icon = Icons.Outlined.ImageAspectRatio,
                        label = "Разрешение",
                        value = "${exif.width} × ${exif.height}",
                    )
                }

                if (exif.cameraMake != null || exif.cameraModel != null) {
                    InfoRow(
                        icon = Icons.Outlined.CameraAlt,
                        label = "Камера",
                        value = listOfNotNull(exif.cameraMake, exif.cameraModel).joinToString(" "),
                    )
                }

                exif.focalLength?.let {
                    InfoRow(
                        icon = Icons.Outlined.Camera,
                        label = "Фокусное расстояние",
                        value = it,
                    )
                }

                exif.aperture?.let {
                    InfoRow(
                        icon = Icons.Outlined.Camera,
                        label = "Диафрагма",
                        value = "f/$it",
                    )
                }

                exif.exposureTime?.let {
                    InfoRow(
                        icon = Icons.Outlined.ShutterSpeed,
                        label = "Выдержка",
                        value = it,
                    )
                }

                exif.iso?.let {
                    InfoRow(
                        icon = Icons.Outlined.Iso,
                        label = "ISO",
                        value = it,
                    )
                }

                if (exif.gpsLatitude != null && exif.gpsLongitude != null) {
                    InfoRow(
                        icon = Icons.Outlined.LocationOn,
                        label = "Координаты",
                        value = "%.6f, %.6f".format(exif.gpsLatitude, exif.gpsLongitude),
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        else -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
