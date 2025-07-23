package com.marquis.zorroexpense.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.AppConfig
import com.marquis.zorroexpense.Expense
import com.marquis.zorroexpense.FirestoreService
import com.marquis.zorroexpense.MockExpenseData
import com.marquis.zorroexpense.components.AppHeader
import com.marquis.zorroexpense.components.EmptyState
import com.marquis.zorroexpense.components.ErrorState
import com.marquis.zorroexpense.components.ExpenseCard
import zorroexpense.composeapp.generated.resources.Res
import zorroexpense.composeapp.generated.resources.sarah

@Composable
fun ExpenseListScreen(
    onExpenseClick: (Expense) -> Unit
) {
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val loadExpenses: suspend () -> Unit = {
        isLoading = true
        errorMessage = null
        
        if (AppConfig.USE_MOCK_DATA) {
            MockExpenseData.getMockExpenses()
                .onSuccess { expenseList ->
                    expenses = expenseList
                }
                .onFailure { exception ->
                    errorMessage = exception.message ?: "Unknown error occurred"
                }
        } else {
            FirestoreService().getExpenses()
                .onSuccess { expenseList ->
                    expenses = expenseList
                }
                .onFailure { exception ->
                    errorMessage = exception.message ?: "Unknown error occurred"
                }
        }
        
        isLoading = false
    }

    LaunchedEffect(Unit) {
        loadExpenses()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(
                appTitle = "ZorroExpense",
                expenses = expenses,
                isLoading = isLoading
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    errorMessage != null -> {
                        ErrorState(
                            title = "Error loading expenses",
                            message = errorMessage!!
                        )
                    }
                    
                    expenses.isEmpty() && !isLoading -> {
                        EmptyState(
                            icon = "💸",
                            title = "No expenses found",
                            description = "Start tracking your expenses by adding some data to your Firestore collection."
                        )
                    }
                    
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = with(LocalDensity.current) { 
                                    WindowInsets.navigationBars.getBottom(this).toDp() + 8.dp
                                }
                            )
                        ) {
                            items(expenses) { expense ->
                                ExpenseCard(
                                    expense = expense,
                                    profileImage = Res.drawable.sarah,
                                    onCardClick = { onExpenseClick(expense) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}