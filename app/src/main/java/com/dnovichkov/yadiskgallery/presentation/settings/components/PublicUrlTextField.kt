package com.dnovichkov.yadiskgallery.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * Text field for entering Yandex.Disk public folder URL.
 */
@Composable
fun PublicUrlTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    error: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Public folder URL") },
            placeholder = { Text("https://disk.yandex.ru/d/...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear URL",
                        )
                    }
                }
            },
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Text(
                        text = "Enter the URL of a public Yandex.Disk folder",
                    )
                }
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = { onSave() },
                ),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSave,
                enabled = value.isNotBlank() && !isLoading,
            ) {
                Text("Open Gallery")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PublicUrlTextFieldPreview() {
    YaDiskGalleryTheme {
        PublicUrlTextField(
            value = "https://disk.yandex.ru/d/test123",
            onValueChange = {},
            onSave = {},
            onClear = {},
            error = null,
            isLoading = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PublicUrlTextFieldErrorPreview() {
    YaDiskGalleryTheme {
        PublicUrlTextField(
            value = "invalid-url",
            onValueChange = {},
            onSave = {},
            onClear = {},
            error = "Invalid Yandex.Disk URL format",
            isLoading = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PublicUrlTextFieldEmptyPreview() {
    YaDiskGalleryTheme {
        PublicUrlTextField(
            value = "",
            onValueChange = {},
            onSave = {},
            onClear = {},
            error = null,
            isLoading = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}
