package com.dnovichkov.yadiskgallery.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.domain.model.ViewMode
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * Selector for choosing between grid and list view modes.
 */
@Composable
fun ViewModeSelector(
    selectedMode: ViewMode,
    onModeSelected: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "View mode",
            style = MaterialTheme.typography.titleSmall,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.selectableGroup()) {
            ViewModeOption(
                mode = ViewMode.GRID,
                isSelected = selectedMode == ViewMode.GRID,
                onSelect = { onModeSelected(ViewMode.GRID) },
            )

            ViewModeOption(
                mode = ViewMode.LIST,
                isSelected = selectedMode == ViewMode.LIST,
                onSelect = { onModeSelected(ViewMode.LIST) },
            )
        }
    }
}

@Composable
private fun ViewModeOption(
    mode: ViewMode,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .selectable(
                    selected = isSelected,
                    onClick = onSelect,
                    role = Role.RadioButton,
                )
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector =
                when (mode) {
                    ViewMode.GRID -> Icons.Default.GridView
                    ViewMode.LIST -> Icons.AutoMirrored.Filled.ViewList
                },
            contentDescription = null,
            tint =
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text =
                    when (mode) {
                        ViewMode.GRID -> "Grid"
                        ViewMode.LIST -> "List"
                    },
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text =
                    when (mode) {
                        ViewMode.GRID -> "Show items in a 3-column grid"
                        ViewMode.LIST -> "Show items in a detailed list"
                    },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ViewModeSelectorGridPreview() {
    YaDiskGalleryTheme {
        ViewModeSelector(
            selectedMode = ViewMode.GRID,
            onModeSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ViewModeSelectorListPreview() {
    YaDiskGalleryTheme {
        ViewModeSelector(
            selectedMode = ViewMode.LIST,
            onModeSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
