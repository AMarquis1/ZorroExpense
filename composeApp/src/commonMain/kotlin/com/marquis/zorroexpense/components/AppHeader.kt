package com.marquis.zorroexpense.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.marquis.zorroexpense.domain.model.Expense

/**
 * Reusable AppHeader component that displays app title and expense summary with Zorro header image background
 * Designed to work with edge-to-edge layout extending behind status bar
 * 
 * @param appTitle The main title of the app
 * @param expenses List of expenses to calculate totals
 * @param isLoading Loading state
 * @param modifier Optional modifier for styling (should include status bar padding)
 */
/*@Composable
fun AppHeader(
    appTitle: String = "ZorroExpense",
    expenses: List<Expense> = emptyList(),
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.zorro_header),
            contentDescription = "Zorro header background",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp)
        ) {
            // Centered content
            Column(
                modifier = Modifier.fillMaxWidth(),
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
}*/

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
        verticalArrangement = Arrangement.Bottom,
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

