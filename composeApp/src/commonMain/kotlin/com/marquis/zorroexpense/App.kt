package com.marquis.zorroexpense

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
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
        Box(modifier = Modifier.fillMaxSize()) {
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
            StatusBarProtection()
        }
    }
}

@Composable
private fun StatusBarProtection(
    color: Color = MaterialTheme.colorScheme.surface,
    heightProvider: () -> Float = calculateGradientHeight(),
) {
    Canvas(Modifier.fillMaxSize()) {
        val calculatedHeight = heightProvider()
        val gradient = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 1f),
                color.copy(alpha = .8f),
                Color.Transparent
            ),
            startY = 0f,
            endY = calculatedHeight
        )
        drawRect(
            brush = gradient,
            size = Size(size.width, calculatedHeight),
        )
    }
}

@Composable
fun calculateGradientHeight(): () -> Float {
    val statusBars = WindowInsets.statusBars
    val density = LocalDensity.current
    return { statusBars.getTop(density).times(1.2f) }
}