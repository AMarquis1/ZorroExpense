package com.marquis.zorroexpense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.components.AppHeader
import com.marquis.zorroexpense.components.EmptyState
import com.marquis.zorroexpense.components.ErrorState
import com.marquis.zorroexpense.components.ExpenseCard
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import zorroexpense.composeapp.generated.resources.Res
import zorroexpense.composeapp.generated.resources.sarah


@Composable
@Preview
fun App() {
    MaterialTheme {
        var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()
        
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

        // Auto-load expenses on app launch
        LaunchedEffect(Unit) {
            loadExpenses()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            // Use the reusable AppHeader component
            AppHeader(
                appTitle = "ZorroExpense",
                expenses = expenses,
                isLoading = isLoading
            )
            
            // Content with reusable state components
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
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(expenses) { expense ->
                            ExpenseCard(
                                expense = expense,
                                profileImage = Res.drawable.sarah
                            )
                        }
                    }
                }
            }
        }
    }
}