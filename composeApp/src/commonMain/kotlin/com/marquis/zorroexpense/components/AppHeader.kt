package com.marquis.zorroexpense.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.Expense

/**
 * Reusable AppHeader component that displays app title and expense summary
 * 
 * @param appTitle The main title of the app
 * @param expenses List of expenses to calculate totals
 * @param isLoading Loading state
 * @param modifier Optional modifier for styling
 */
@Composable
fun AppHeader(
    appTitle: String = "ZorroExpense",
    expenses: List<Expense> = emptyList(),
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Centered content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppTitle(title = appTitle)
                
                if (expenses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ExpenseSummary(expenses = expenses)
                } else if (!isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AppSubtitle(subtitle = "Suivi des dépenses")
                }
                
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * App title component
 */
@Composable
private fun AppTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/**
 * App subtitle component
 */
@Composable
private fun AppSubtitle(
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/**
 * Expense summary component showing total amount and count
 */
@Composable
private fun ExpenseSummary(
    expenses: List<Expense>,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val totalAmount = expenses.sumOf { it.price }
        
        Text(
            text = "$$totalAmount",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "${expenses.size} dépenses pour les criss",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Standalone expense summary card for use in other places
 */
@Composable
fun ExpenseSummaryCard(
    expenses: List<Expense>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        ExpenseSummary(
            expenses = expenses,
            modifier = Modifier.padding(16.dp)
        )
    }
}