package com.marquis.zorroexpense.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.Expense
import org.jetbrains.compose.resources.DrawableResource
import zorroexpense.composeapp.generated.resources.Res
import zorroexpense.composeapp.generated.resources.sarah

/**
 * Wrapper component that displays date on the left and expense card on the right
 * 
 * @param expense The expense data to display
 * @param profileImage Optional drawable resource for the profile picture
 * @param onCardClick Optional click handler for the card
 * @param modifier Optional modifier for styling
 */
@Composable
fun ExpenseCardWithDate(
    expense: Expense,
    profileImage: DrawableResource? = Res.drawable.sarah,
    onCardClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date display on the left
        ExpenseDateDisplay(date = expense.date)

        // Expense card
        ExpenseCard(
            expense = expense,
            profileImage = profileImage,
            onCardClick = onCardClick,
        )
    }
}

/**
 * Reusable ExpenseCard component that displays expense information with profile avatar
 * 
 * @param expense The expense data to display
 * @param profileImage Optional drawable resource for the profile picture
 * @param onCardClick Optional click handler for the card
 * @param modifier Optional modifier for styling
 */
@Composable
fun ExpenseCard(
    expense: Expense,
    profileImage: DrawableResource? = Res.drawable.sarah, // Default for now
    onCardClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        onClick = { onCardClick?.invoke() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile avatar
            ExpenseProfileAvatar(
                expenseName = expense.name,
                imageResource = profileImage,
                size = 40.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content column
            ExpenseCardContent(
                expense = expense,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Internal component for the expense card content section
 */
@Composable
private fun ExpenseCardContent(
    expense: Expense,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header row with name and amount
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = expense.name.ifEmpty { "Unnamed Expense" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            ExpensePriceChip(price = expense.price)
        }
        
    }
}

/**
 * Date display component that shows date in "JAN 25" format on two lines
 */
@Composable
private fun ExpenseDateDisplay(
    date: String,
    modifier: Modifier = Modifier
) {
    val formattedDate = formatDateToMonthDay(date)
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formattedDate.first, // Month (e.g., "JAN")
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = formattedDate.second, // Day (e.g., "25")
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Reusable price chip component
 */
@Composable
fun ExpensePriceChip(
    price: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = "$$price",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Reusable date chip component
 */
@Composable
fun ExpenseDateChip(
    date: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = formatTimestamp(date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Utility function to format timestamps (moved from App.kt)
 */
private fun formatTimestamp(timestamp: String): String {
    return if (timestamp.isBlank()) {
        "No date"
    } else {
        timestamp.substringBefore("T").takeIf { it.isNotBlank() } ?: timestamp
    }
}

/**
 * Month separator component for grouping expenses by month
 */
@Composable
fun MonthSeparator(
    month: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = month,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    )
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
            
            val monthNames = arrayOf(
                "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
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
            
            val monthNames = arrayOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            
            val monthName = if (month in 1..12) monthNames[month - 1] else "November"
            return "$monthName $year"
        }
    } catch (_: Exception) {
        // Fall back to default if parsing fails
    }
    
    return "November 2024"
}