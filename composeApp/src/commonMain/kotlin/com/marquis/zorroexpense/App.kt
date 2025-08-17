package com.marquis.zorroexpense

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.marquis.zorroexpense.di.AppModule
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.navigation.AppDestinations
import com.marquis.zorroexpense.presentation.constants.DeleteConstants
import com.marquis.zorroexpense.presentation.screens.AddExpenseScreen
import com.marquis.zorroexpense.presentation.screens.ExpenseDetailScreen
import com.marquis.zorroexpense.presentation.screens.ExpenseListScreen
import com.marquis.zorroexpense.presentation.state.ExpenseListUiEvent
import com.marquis.zorroexpense.ui.theme.ZorroExpenseTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Preview
fun App() {
    ZorroExpenseTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            var deletedExpenseName by remember { mutableStateOf<String?>(null) }
            var deletedExpenseId by remember { mutableStateOf<String?>(null) }

            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = AppDestinations.ExpenseList,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable<AppDestinations.ExpenseList> {
                        val viewModel =
                            AppModule.provideExpenseListViewModel(
                                onExpenseClick = { expense ->
                                    navController.navigate(
                                        AppDestinations.ExpenseDetail(
                                            expenseId = expense.documentId,
                                            expenseName = expense.name,
                                            expenseDescription = expense.description,
                                            expensePrice = expense.price,
                                            expenseDate = expense.date,
                                            categoryName = expense.category.name,
                                            categoryIcon = expense.category.icon,
                                            categoryColor = expense.category.color,
                                            paidByUserId = expense.paidBy.userId,
                                            splitDetailsJson = AppDestinations.ExpenseDetail.createSplitDetailsJson(
                                                expense.splitDetails.map { splitDetail ->
                                                    AppDestinations.SplitDetailNavigation(
                                                        userId = splitDetail.user.userId,
                                                        amount = splitDetail.amount
                                                    )
                                                }
                                            ),
                                        ),
                                    )
                                },
                                onAddExpenseClick = {
                                    navController.navigate(AppDestinations.AddExpense)
                                },
                            )
                        ExpenseListScreen(
                            viewModel = viewModel,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this,
                            deletedExpenseName = deletedExpenseName,
                            onUndoDelete = {
                                deletedExpenseId?.let { expenseId ->
                                    viewModel.onEvent(ExpenseListUiEvent.UndoDeleteExpense(expenseId))
                                    deletedExpenseName = null
                                    deletedExpenseId = null
                                }
                            },
                        )

                        // Auto-dismiss after delay - confirm the actual deletion
                        // This will be cancelled when deletedExpenseName/Id becomes null (undo case)
                        LaunchedEffect(deletedExpenseId) {
                            deletedExpenseId?.let { expenseId ->
                                kotlinx.coroutines.delay(DeleteConstants.AUTO_DELETE_DELAY_MS)
                                if (deletedExpenseId == expenseId) {
                                    viewModel.onEvent(ExpenseListUiEvent.ConfirmDeleteExpense(expenseId))
                                    deletedExpenseName = null
                                    deletedExpenseId = null
                                }
                            }
                        }
                    }

                    composable<AppDestinations.AddExpense> {
                        val viewModel = AppModule.provideAddExpenseViewModel()
                        AddExpenseScreen(
                            viewModel = viewModel,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onExpenseSaved = {
                                navController.popBackStack()
                            },
                        )
                    }

                    composable<AppDestinations.ExpenseDetail> { backStackEntry ->
                        val expenseDetail = backStackEntry.toRoute<AppDestinations.ExpenseDetail>()
                        val expense =
                            Expense(
                                documentId = expenseDetail.expenseId,
                                name = expenseDetail.expenseName,
                                description = expenseDetail.expenseDescription,
                                price = expenseDetail.expensePrice,
                                date = expenseDetail.expenseDate,
                                category =
                                    Category(
                                        name = expenseDetail.categoryName,
                                        icon = expenseDetail.categoryIcon,
                                        color = expenseDetail.categoryColor,
                                    ),
                                paidBy = MockExpenseData.usersMap[expenseDetail.paidByUserId] ?: User(),
                                splitDetails = expenseDetail.splitDetails.mapNotNull { splitDetailNav ->
                                    val user = MockExpenseData.usersMap[splitDetailNav.userId]
                                    if (user != null) {
                                        com.marquis.zorroexpense.domain.model.SplitDetail(user = user, amount = splitDetailNav.amount)
                                    } else {
                                        null
                                    }
                                },
                            )

                        ExpenseDetailScreen(
                            expense = expense,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onExpenseDeleted = { expenseName ->
                                // Set the deleted expense info for snackbar display and undo functionality
                                deletedExpenseName = expenseName
                                deletedExpenseId = expense.documentId

                                // Use the singleton ViewModel instance
                                val listViewModel = AppModule.provideExpenseListViewModel()
                                listViewModel.onEvent(ExpenseListUiEvent.PendingDeleteExpense(expense.documentId))

                                // Navigate back to show snackbar
                                navController.popBackStack()
                            },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this,
                        )
                    }
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
        val gradient =
            Brush.verticalGradient(
                colors =
                    listOf(
                        color.copy(alpha = 1f),
                        color.copy(alpha = .8f),
                        Color.Transparent,
                    ),
                startY = 0f,
                endY = calculatedHeight,
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
