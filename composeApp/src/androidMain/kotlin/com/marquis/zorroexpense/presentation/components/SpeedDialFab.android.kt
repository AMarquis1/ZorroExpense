package com.marquis.zorroexpense.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Android-specific implementation of Speed Dial FAB using Material3 1.5.0 FloatingActionButtonMenu.
 *
 * This provides a native Android Material Design component with built-in animations,
 * proper Material Design spacing, and touch feedback.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun SpeedDialFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<SpeedDialFabItem>,
    mainIcon: ImageVector,
    mainLabel: String,
    fabExpanded: Boolean,
) {
    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            FloatingActionButton(
                onClick = { onExpandedChange(!expanded) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = mainIcon,
                    contentDescription = "Toggle menu",
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            rotationZ = if (expanded) 45f else 0f
                        },
                )
            }
        },
    ) {
        // Add menu items - they appear in order from list
        items.forEach { item ->
            FloatingActionButtonMenuItem(
                onClick = {
                    item.onClick()
                    onExpandedChange(false)
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription,
                        modifier = Modifier.size(20.dp),
                    )
                },
                text = {
                    Text(item.label)
                },
            )
        }
    }
}
