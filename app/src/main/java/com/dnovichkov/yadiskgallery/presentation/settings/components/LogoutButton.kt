package com.dnovichkov.yadiskgallery.presentation.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * Button to logout from Yandex account.
 */
@Composable
fun LogoutButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
        )
        Text(
            text = "Sign out",
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/**
 * Confirmation dialog for logout action.
 */
@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = {
            Text(text = "Sign out?")
        },
        text = {
            Text(text = "You will need to sign in again to access your Yandex.Disk files.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Sign out",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun LogoutButtonPreview() {
    YaDiskGalleryTheme {
        LogoutButton(
            onClick = {},
            enabled = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogoutButtonDisabledPreview() {
    YaDiskGalleryTheme {
        LogoutButton(
            onClick = {},
            enabled = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogoutConfirmationDialogPreview() {
    YaDiskGalleryTheme {
        LogoutConfirmationDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}
