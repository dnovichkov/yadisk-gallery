package com.dnovichkov.yadiskgallery.presentation.settings.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.domain.model.SortOrder
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme

/**
 * Selector for choosing the sort order for files.
 */
@Composable
fun SortOrderSelector(
    selectedOrder: SortOrder,
    onOrderSelected: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Sort order",
            style = MaterialTheme.typography.titleSmall,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box {
            OutlinedCard(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = getSortOrderLabel(selectedOrder),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = getSortOrderDescription(selectedOrder),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                SortOrder.entries.forEach { order ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(text = getSortOrderLabel(order))
                                Text(
                                    text = getSortOrderDescription(order),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        onClick = {
                            onOrderSelected(order)
                            expanded = false
                        },
                        leadingIcon = {
                            if (order == selectedOrder) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

private fun getSortOrderLabel(order: SortOrder): String {
    return when (order) {
        SortOrder.NAME_ASC -> "Name (A-Z)"
        SortOrder.NAME_DESC -> "Name (Z-A)"
        SortOrder.DATE_ASC -> "Date (oldest first)"
        SortOrder.DATE_DESC -> "Date (newest first)"
        SortOrder.SIZE_ASC -> "Size (smallest first)"
        SortOrder.SIZE_DESC -> "Size (largest first)"
    }
}

private fun getSortOrderDescription(order: SortOrder): String {
    return when (order) {
        SortOrder.NAME_ASC -> "Sort alphabetically A to Z"
        SortOrder.NAME_DESC -> "Sort alphabetically Z to A"
        SortOrder.DATE_ASC -> "Show oldest files first"
        SortOrder.DATE_DESC -> "Show newest files first"
        SortOrder.SIZE_ASC -> "Show smallest files first"
        SortOrder.SIZE_DESC -> "Show largest files first"
    }
}

@Preview(showBackground = true)
@Composable
private fun SortOrderSelectorPreview() {
    YaDiskGalleryTheme {
        SortOrderSelector(
            selectedOrder = SortOrder.DATE_DESC,
            onOrderSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
