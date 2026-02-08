package com.marquis.zorroexpense.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing a single menu item in the speed dial FAB
 *
 * @param icon The icon to display for this menu item
 * @param label The label text for this menu item
 * @param contentDescription Accessibility description for the icon
 * @param onClick Callback when this menu item is clicked
 */
data class SpeedDialFabItem(
    val icon: ImageVector,
    val label: String,
    val contentDescription: String,
    val onClick: () -> Unit,
)

/**
 * Speed Dial FAB component with Material Design 3 pattern.
 *
 * Platform-specific implementation:
 * - Android: Uses native FloatingActionButtonMenu from Material3 1.5.0
 * - iOS/Web: Uses custom Speed Dial component with animations
 *
 * Features:
 * - Main FAB rotates 45° when expanded
 * - Mini FABs slide up with stagger animation
 * - Semi-transparent scrim overlay appears to dismiss menu
 * - Automatically closes when scrolling
 *
 * @param expanded Controls whether the menu is visible
 * @param onExpandedChange Callback when expand state changes
 * @param mainIcon Icon for the main FAB button
 * @param mainLabel Label for the main FAB button
 * @param items List of menu items to display
 * @param fabExpanded Controls scroll-based FAB text visibility (extended vs icon-only)
 */
@Composable
expect fun SpeedDialFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<SpeedDialFabItem>,
    mainIcon: ImageVector = Icons.Default.Add,
    mainLabel: String = "Add",
    fabExpanded: Boolean = true,
)
