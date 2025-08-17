package com.marquis.zorroexpense.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.components.ProfileAvatar
import com.marquis.zorroexpense.domain.model.DebtSummary
import com.marquis.zorroexpense.domain.model.User
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Horizontal bar displaying debt settlements between users
 * Shows who owes whom and how much in a clean, scrollable format
 */
@Composable
fun DebtSummaryBar(
    debtSummaries: List<DebtSummary>,
    modifier: Modifier = Modifier,
) {
    if (debtSummaries.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Text(
                text = "Debt settlements",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Scrollable debt items
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(debtSummaries) { debt ->
                    DebtSummaryItem(debt = debt)
                }
            }
        }
    }
}

/**
 * Individual debt summary item with compact horizontal layout
 */
@Composable
private fun DebtSummaryItem(
    debt: DebtSummary,
    modifier: Modifier = Modifier,
) {
    val formattedAmount = if (debt.amount == debt.amount.toInt().toDouble()) {
        "$${debt.amount.toInt()}"
    } else {
        "$${(kotlin.math.round(debt.amount * 100) / 100.0)}"
    }

    Surface(
        modifier = modifier.widthIn(min = 220.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column (
                verticalArrangement = spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProfileAvatar(
                    name = debt.fromUser.name,
                    userProfile = debt.fromUser.profileImage,
                    size = 40.dp
                )
                Text(
                    text = debt.fromUser.name,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f),
            ) {
                Text(
                    text = "owes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column (
                verticalArrangement = spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProfileAvatar(
                    name = debt.toUser.name,
                    userProfile = debt.toUser.profileImage,
                    size = 40.dp
                )
                Text(
                    text = debt.toUser.name,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Preview function showing a complex debt settlement scenario with 4 users
 * 
 * Scenario:
 * - Alex, Sarah, Mike, and Emma went on a trip together
 * - Multiple expenses were shared differently creating complex debts
 * - The debt algorithm optimizes to minimize total transactions
 */
@Preview
@Composable
fun DebtSummaryBarPreview() {
    // Create 4 users for the complex scenario
    val alex = User(userId = "1", name = "Alex", profileImage = "alex")
    val sarah = User(userId = "2", name = "Sarah", profileImage = "sarah")
    val mike = User(userId = "3", name = "Mike", profileImage = "mike")
    val emma = User(userId = "4", name = "Emma", profileImage = "emma")
    
    // Complex debt scenario result after optimization:
    // - Mike owes $127.50 to Sarah (largest debt)
    // - Emma owes $45.25 to Alex (medium debt)  
    // - Alex owes $23.75 to Sarah (smallest debt)
    //
    // This represents a realistic scenario where:
    // 1. Sarah paid for expensive items (hotel, flights)
    // 2. Mike didn't contribute much but shared expenses
    // 3. Emma and Alex had mixed payments
    // 4. The algorithm minimized from potentially 6 transactions to just 3
    val complexDebtScenario = listOf(
        DebtSummary(
            fromUser = mike,
            toUser = sarah, 
            amount = 127.50
        ),
        DebtSummary(
            fromUser = emma,
            toUser = alex,
            amount = 45.25
        ),
        DebtSummary(
            fromUser = alex,
            toUser = sarah,
            amount = 23.75
        )
    )
    
    MaterialTheme {
        DebtSummaryBar(debtSummaries = complexDebtScenario)
    }
}