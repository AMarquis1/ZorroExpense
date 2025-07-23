package com.marquis.zorroexpense

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.marquis.zorroexpense.navigation.AppDestinations
import com.marquis.zorroexpense.screens.AddExpenseScreen
import com.marquis.zorroexpense.screens.ExpenseDetailScreen
import com.marquis.zorroexpense.screens.ExpenseListScreen
import com.marquis.zorroexpense.ui.theme.ZorroExpenseTheme
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {
    ZorroExpenseTheme {
        val navController = rememberNavController()
        
        NavHost(
            navController = navController,
            startDestination = AppDestinations.ExpenseList,
            modifier = Modifier.fillMaxSize()
        ) {
            composable<AppDestinations.ExpenseList> {
                ExpenseListScreen(
                    onExpenseClick = { expense ->
                        navController.navigate(
                            AppDestinations.ExpenseDetail(
                                expenseName = expense.name,
                                expenseDescription = expense.description,
                                expensePrice = expense.price,
                                expenseDate = expense.date
                            )
                        )
                    },
                    onAddExpense = {
                        navController.navigate(AppDestinations.AddExpense)
                    }
                )
            }
            
            composable<AppDestinations.AddExpense> {
                AddExpenseScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onExpenseSaved = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable<AppDestinations.ExpenseDetail> { backStackEntry ->
                val expenseDetail = backStackEntry.toRoute<AppDestinations.ExpenseDetail>()
                val expense = Expense(
                    name = expenseDetail.expenseName,
                    description = expenseDetail.expenseDescription,
                    price = expenseDetail.expensePrice,
                    date = expenseDetail.expenseDate
                )
                
                ExpenseDetailScreen(
                    expense = expense,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}