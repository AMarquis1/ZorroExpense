package com.marquis.zorroexpense.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense

/**
 * Wrapper component that displays date on the left and expense card on the right
 *
 * @param expense The expense data to display
 * @param onCardClick Optional click handler for the card
 * @param sharedTransitionScope Optional shared transition scope for shared element transitions
 * @param animatedContentScope Optional animated content scope for shared element transitions
 * @param modifier Optional modifier for styling
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ExpenseCardWithDate(
    expense: Expense,
    onCardClick: (() -> Unit)? = null,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedContentScope: androidx.compose.animation.AnimatedContentScope? = null,
    isScheduled: Boolean = false, // For future recurring expenses
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Date display on the left
        ExpenseDateDisplay(date = expense.date)

        // Expense card
        ExpenseCard(
            expense = expense,
            onCardClick = onCardClick,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            isScheduled = isScheduled,
        )
    }
}

/**
 * Reusable ExpenseCard component that displays expense information with profile avatar
 * Profile image is automatically determined from the expense.paidBy userId
 *
 * @param expense The expense data to display
 * @param onCardClick Optional click handler for the card
 * @param sharedTransitionScope Optional shared transition scope for shared element transitions
 * @param animatedContentScope Optional animated content scope for shared element transitions
 * @param modifier Optional modifier for styling
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ExpenseCard(
    expense: Expense,
    onCardClick: (() -> Unit)? = null,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedContentScope: androidx.compose.animation.AnimatedContentScope? = null,
    isScheduled: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 16.dp, bottom = 8.dp)
                .let { if (isScheduled) it.graphicsLayer(alpha = 0.6f) else it },
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (isScheduled) 2.dp else 4.dp,
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isScheduled) {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        onClick = { if (!isScheduled) onCardClick?.invoke() },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Category icon on the left with shared element transition
            if (expense.category.icon.isNotEmpty()) {
                val categoryModifier =
                    if (sharedTransitionScope != null && animatedContentScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                sharedContentState =
                                    rememberSharedContentState(
                                        key = "category-${expense.category.name}-${expense.name}-${expense.date}",
                                    ),
                                animatedVisibilityScope = animatedContentScope,
                            )
                        }
                    } else {
                        Modifier
                    }

                CategoryIconCircle(
                    category = expense.category,
                    size = 40.dp,
                    modifier = categoryModifier,
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Content column
            ExpenseCardContent(
                expense = expense,
                isScheduled = isScheduled,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Internal component for the expense card content section
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ExpenseCardContent(
    expense: Expense,
    isScheduled: Boolean = false,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedContentScope: androidx.compose.animation.AnimatedContentScope? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Header row with buyer profile and price
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Expense name on the left
            Text(
                text = expense.name.ifEmpty { "Unnamed Expense" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Right side with price and split users
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                ExpensePriceChip(
                    price = expense.price,
                    categoryColor = expense.category.color,
                    isScheduled = isScheduled,
                )

                // Split users avatars with buyer emphasized
                if (expense.splitWith.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SplitUsersRow(
                        users = expense.splitWith,
                        buyer = expense.paidBy,
                    )
                }
            }
        }
    }
}

/**
 * Date display component that shows date in "JAN 25" format on two lines
 */
@Composable
private fun ExpenseDateDisplay(
    date: String,
    modifier: Modifier = Modifier,
) {
    val formattedDate = formatDateToMonthDay(date)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = formattedDate.first, // Month (e.g., "JAN")
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = formattedDate.second, // Day (e.g., "25")
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Component to display a row of user profile images for split participants
 * The buyer's avatar is displayed larger to emphasize who paid
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SplitUsersRow(
    users: List<com.marquis.zorroexpense.domain.model.User>,
    buyer: com.marquis.zorroexpense.domain.model.User,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ProfileAvatar(
            name = buyer.name,
            size = 32.dp,
            userProfile = buyer.profileImage,
        )

        val otherUsers = users.filter { it.userId != buyer.userId }
        if (otherUsers.isNotEmpty()) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "splits with",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier
                        .size(20.dp)
                        .padding(horizontal = 2.dp),
            )

            otherUsers.forEach { user ->
                ProfileAvatar(
                    name = user.name,
                    size = 20.dp,
                    userProfile = user.profileImage,
                )
            }
        }
    }
}

/**
 * Reusable price chip component with consistent width for 5-digit prices
 * Uses category color for background
 */
@Composable
fun ExpensePriceChip(
    price: Double,
    categoryColor: String = "",
    isScheduled: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        if (categoryColor.isNotEmpty()) {
            parseHexColor(categoryColor)
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }

    // Calculate contrasting text color
    val textColor =
        if (categoryColor.isNotEmpty()) {
            Color.White // Use white text on colored backgrounds
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
        ) {
            Text(
                text = "$${formatPrice(price)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .widthIn(min = 72.dp) // Minimum width to accommodate 5-digit prices
                        .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }

        // Scheduled badge
        if (isScheduled) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = "Scheduled",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }
}

/**
 * Month separator component for grouping expenses by month
 * Clickable to collapse/expand expenses in that month
 */
@Composable
fun MonthSeparator(
    month: String,
    isCollapsed: Boolean = false,
    onToggleCollapsed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Interaction source for press feedback
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate the arrow rotation
    val rotationAngle by animateFloatAsState(
        targetValue = if (isCollapsed) -90f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "arrow_rotation",
    )

    // Animate press scale
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "press_scale",
    )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.clickable(
                    interactionSource = interactionSource,
                    indication = null, // We handle the feedback with scale animation
                ) { onToggleCollapsed() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        // Animated collapse/expand indicator
        Text(
            text = "▼",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier.graphicsLayer {
                    rotationZ = rotationAngle
                },
        )
    }
}

/**
 * Utility function to parse hex color string to Compose Color
 */
private fun parseHexColor(hexColor: String): Color =
    try {
        val cleanHex = hexColor.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        Color(colorInt or 0xFF000000) // Add alpha if not present
    } catch (e: Exception) {
        Color(0xFF6200EE) // Default purple color
    }

/**
 * Utility function to format price with proper decimal places
 * 45.5 -> "45.50", 45.0 -> "45", 45.533 -> "45.50"
 */
private fun formatPrice(price: Double): String {
    // Check if the price is a whole number
    return if (price == price.toInt().toDouble()) {
        // It's a whole number, show without decimals
        price.toInt().toString()
    } else {
        // It has decimals, format to 2 decimal places
        val rounded = (price * 100).toInt() / 100.0
        val decimalPart = ((rounded * 100) % 100).toInt()
        val wholePart = rounded.toInt()
        "$wholePart.${decimalPart.toString().padStart(2, '0')}"
    }
}

/**
 * Utility function to format date into month-day pair (e.g., "JAN" and "25")
 */
private fun formatDateToMonthDay(timestamp: String): Pair<String, String> {
    if (timestamp.isBlank()) {
        return "NOV" to "1"
    }

    try {
        // Extract date part if it's an ISO timestamp
        val dateStr = timestamp.substringBefore("T")

        // Parse date in format YYYY-MM-DD
        val parts = dateStr.split("-")
        if (parts.size >= 3) {
            val month = parts[1].toIntOrNull() ?: 1
            val day = parts[2].toIntOrNull() ?: 1

            val monthNames =
                arrayOf(
                    "JAN",
                    "FEB",
                    "MAR",
                    "APR",
                    "MAY",
                    "JUN",
                    "JUL",
                    "AUG",
                    "SEP",
                    "OCT",
                    "NOV",
                    "DEC",
                )

            val monthName = if (month in 1..12) monthNames[month - 1] else "JAN"
            val dayStr = day.toString()

            return monthName to dayStr
        }
    } catch (_: Exception) {
        // Fall back to default if parsing fails
    }

    return "NOV" to "1"
}

/**
 * Utility function to get full month name and year from timestamp
 */
fun getMonthYear(timestamp: String): String {
    if (timestamp.isBlank()) {
        return "November 2024"
    }

    try {
        val dateStr = timestamp.substringBefore("T")
        val parts = dateStr.split("-")
        if (parts.size >= 3) {
            val year = parts[0]
            val month = parts[1].toIntOrNull() ?: 11

            val monthNames =
                arrayOf(
                    "January",
                    "February",
                    "March",
                    "April",
                    "May",
                    "June",
                    "July",
                    "August",
                    "September",
                    "October",
                    "November",
                    "December",
                )

            val monthName = if (month in 1..12) monthNames[month - 1] else "November"
            return "$monthName $year"
        }
    } catch (_: Exception) {
        // Fall back to default if parsing fails
    }

    return "November 2024"
}

/**
 * Multi-select category filter component using segmented buttons
 */
@Composable
fun CategoryFilterRow(
    categories: List<Category>,
    selectedCategories: Set<Category>,
    onCategoryToggle: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    MultiChoiceSegmentedButtonRow(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    ) {
        categories.forEachIndexed { index, category ->
            val isSelected = selectedCategories.contains(category)

            SegmentedButton(
                shape =
                    SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = categories.size,
                    ),
                checked = isSelected,
                onCheckedChange = { onCategoryToggle(category) },
                colors =
                    SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CategoryIconCircle(
                        category = category,
                        size = 20.dp,
                    )
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

/**
 * Circular category icon component with colored background
 */
@Composable
fun CategoryIconCircle(
    category: Category,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .background(
                    color = parseHexColor(category.color),
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = getCategoryIcon(category.icon),
            contentDescription = category.name,
            tint = Color.White,
            modifier = Modifier.size(size * 0.6f),
        )
    }
}

/**
 * Helper function to get Material Icon based on category icon name
 */
private fun getCategoryIcon(iconName: String): ImageVector =
    when (iconName) {
        "Home" -> Icons.Outlined.Home
        "ShoppingCart" -> Icons.Outlined.ShoppingCart
        "Pets" -> Icons.Outlined.Pets
        else -> Icons.Outlined.Home // Default fallback
    }
