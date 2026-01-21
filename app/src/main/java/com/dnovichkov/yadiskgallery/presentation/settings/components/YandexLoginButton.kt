package com.dnovichkov.yadiskgallery.presentation.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

// Yandex brand color
private val YandexRed = Color(0xFFFC3F1D)

/**
 * Button to initiate Yandex OAuth login.
 */
@Composable
fun YandexLoginButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = YandexRed,
                contentColor = Color.White,
            ),
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
        } else {
            Text(
                text = "Sign in with Yandex",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun YandexLoginButtonPreview() {
    YaDiskGalleryTheme {
        YandexLoginButton(
            onClick = {},
            isLoading = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun YandexLoginButtonLoadingPreview() {
    YaDiskGalleryTheme {
        YandexLoginButton(
            onClick = {},
            isLoading = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}
