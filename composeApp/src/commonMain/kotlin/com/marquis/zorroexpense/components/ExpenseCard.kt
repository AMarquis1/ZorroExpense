package com.marquis.zorroexpense.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                size = 72.dp
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
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            ExpensePriceChip(price = expense.price)
        }
        
        // Description
        if (expense.description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = expense.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Date badge
        ExpenseDateChip(date = expense.date)
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
            style = MaterialTheme.typography.titleMedium,
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