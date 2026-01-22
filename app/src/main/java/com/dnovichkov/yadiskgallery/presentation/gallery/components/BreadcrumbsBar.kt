package com.dnovichkov.yadiskgallery.presentation.gallery.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnovichkov.yadiskgallery.presentation.gallery.BreadcrumbItem

/**
 * Horizontal scrolling breadcrumb navigation bar.
 */
@Composable
fun BreadcrumbsBar(
    breadcrumbs: List<BreadcrumbItem>,
    onBreadcrumbClick: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    // Auto-scroll to the end when breadcrumbs change
    LaunchedEffect(breadcrumbs.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier =
                Modifier
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            breadcrumbs.forEachIndexed { index, breadcrumb ->
                val isLast = index == breadcrumbs.lastIndex

                BreadcrumbChip(
                    text = breadcrumb.name,
                    isActive = isLast,
                    onClick = {
                        if (!isLast) {
                            onBreadcrumbClick(breadcrumb.path)
                        }
                    },
                )

                if (!isLast) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(20.dp)
                                .padding(horizontal = 2.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun BreadcrumbChip(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier =
            modifier
                .clickable(enabled = !isActive, onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 2.dp),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
        color =
            if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
