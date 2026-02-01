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
import androidx.compose.runtime.collectAsState
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
import com.marquis.zorroexpense.platform.BindBrowserNavigation
import com.marquis.zorroexpense.presentation.screens.AddExpenseScreen
import com.marquis.zorroexpense.presentation.screens.CreateExpenseListScreen
import com.marquis.zorroexpense.presentation.screens.ExpenseDetailScreen
import com.marquis.zorroexpense.presentation.screens.ExpenseListScreen
import com.marquis.zorroexpense.presentation.screens.ExpenseListsScreen
import com.marquis.zorroexpense.presentation.screens.LoginScreen
import com.marquis.zorroexpense.presentation.screens.SignUpScreen
import com.marquis.zorroexpense.presentation.state.ExpenseListUiEvent
import com.marquis.zorroexpense.presentation.state.GlobalAuthState
import com.marquis.zorroexpense.ui.theme.ZorroExpenseTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Preview
fun App() {
    ZorroExpenseTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            val authViewModel = AppModule.provideAuthViewModel()
            val globalAuthState by authViewModel.globalAuthState.collectAsState()

            var deletedExpenseName by remember { mutableStateOf<String?>(null) }
            var deletedExpenseId by remember { mutableStateOf<String?>(null) }
            var updatedExpenseName by remember { mutableStateOf<String?>(null) }

            // Bind browser navigation for WASM back/forward button support
            BindBrowserNavigation(navController)

            // Determine start destination based on auth state
            val startDestination = when (globalAuthState) {
                GlobalAuthState.Unauthenticated -> AppDestinations.Login
                GlobalAuthState.Authenticating -> AppDestinations.Login
                is GlobalAuthState.Authenticated -> AppDestinations.ExpenseLists
            }

            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable<AppDestinations.Login> {
                        LoginScreen(
                            viewModel = authViewModel,
                            onNavigateToSignUp = {
                                navController.navigate(AppDestinations.SignUp)
                            },
                            onLoginSuccess = {
                                // Navigate to ExpenseLists and clear login/signup from back stack
                                navController.navigate(AppDestinations.ExpenseLists) {
                                    popUpTo(AppDestinations.Login) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable<AppDestinations.SignUp> {
                        SignUpScreen(
                            viewModel = authViewModel,
                            onNavigateToLogin = {
                                navController.popBackStack()
                            },
                            onSignUpSuccess = {
                                // Navigate to ExpenseLists and clear login/signup from back stack
                                navController.navigate(AppDestinations.ExpenseLists) {
                                    popUpTo(AppDestinations.Login) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable<AppDestinations.ExpenseLists> {
                        // Auth guard: redirect to login if not authenticated
                        LaunchedEffect(globalAuthState) {
                            if (globalAuthState is GlobalAuthState.Unauthenticated) {
                                navController.navigate(AppDestinations.Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }

                        val userId = (globalAuthState as? GlobalAuthState.Authenticated)?.user?.userId ?: ""
                        val viewModel = AppModule.provideExpenseListsViewModel(
                            userId = userId,
                            onListSelected = { listId, _ ->
                                navController.navigate(AppDestinations.ExpenseList(listId = listId))
                            }
                        )
                        ExpenseListsScreen(
                            viewModel = viewModel,
                            onListSelected = { listId ->
                                navController.navigate(AppDestinations.ExpenseList(listId = listId))
                            },
                            onCreateNewList = {
                                navController.navigate(AppDestinations.CreateExpenseList)
                            }
                        )
                    }

                    composable<AppDestinations.CreateExpenseList> {
                        // Auth guard: redirect to login if not authenticated
                        LaunchedEffect(globalAuthState) {
                            if (globalAuthState is GlobalAuthState.Unauthenticated) {
                                navController.navigate(AppDestinations.Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }

                        val userId = (globalAuthState as? GlobalAuthState.Authenticated)?.user?.userId ?: ""
                        val viewModel = AppModule.provideCreateExpenseListViewModel(
                            userId = userId,
                            onListCreated = { _, _ ->
                                // Navigate back to ExpenseLists which will reload and show the new list
                                navController.popBackStack(AppDestinations.ExpenseLists, inclusive = false)
                            }
                        )
                        CreateExpenseListScreen(
                            viewModel = viewModel,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onListCreated = { _, _ ->
                                // Navigate back to ExpenseLists which will reload and show the new list
                                navController.popBackStack(AppDestinations.ExpenseLists, inclusive = false)
                            }
                        )
                    }

                    composable<AppDestinations.ExpenseList> { backStackEntry ->
                        // Auth guard: redirect to login if not authenticated
                        LaunchedEffect(globalAuthState) {
                            if (globalAuthState is GlobalAuthState.Unauthenticated) {
                                navController.navigate(AppDestinations.Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }

                        val expenseListRoute = backStackEntry.toRoute<AppDestinations.ExpenseList>()
                        val userId = (globalAuthState as? GlobalAuthState.Authenticated)?.user?.userId ?: ""
                        val viewModel =
                            AppModule.provideExpenseListViewModel(
                                userId = userId,
                                listId = expenseListRoute.listId,
                                onExpenseClick = { expense ->
                                    navController.navigate(
                                        AppDestinations.ExpenseDetail(
                                            listId = expenseListRoute.listId,
                                            expenseId = expense.documentId,
                                            expenseName = expense.name,
                                            expenseDescription = expense.description,
                                            expensePrice = expense.price,
                                            expenseDate = expense.date,
                                            categoryDocumentId = expense.category.documentId,
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
                                    navController.navigate(AppDestinations.AddExpense(listId = expenseListRoute.listId))
                                },
                            )
                        ExpenseListScreen(
                            viewModel = viewModel,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this,
                            deletedExpenseName = deletedExpenseName,
                            onUndoDelete = {
                                // User clicked UNDO - restore the expense
                                deletedExpenseId?.let { expenseId ->
                                    viewModel.onEvent(ExpenseListUiEvent.UndoDeleteExpense(expenseId))
                                }
                            },
                            onConfirmDelete = {
                                // Timer expired or snackbar dismissed - confirm deletion
                                deletedExpenseId?.let { expenseId ->
                                    viewModel.onEvent(ExpenseListUiEvent.ConfirmDeleteExpense(expenseId))
                                }
                            },
                            onDeleteFlowComplete = {
                                // Clear delete state after flow completes (undo or confirm)
                                deletedExpenseName = null
                                deletedExpenseId = null
                            },
                            updatedExpenseName = updatedExpenseName,
                            onUpdateFlowComplete = {
                                // Clear update state after snackbar is shown
                                updatedExpenseName = null
                            },
                        )
                    }

                    composable<AppDestinations.AddExpense> { backStackEntry ->
                        // Auth guard: redirect to login if not authenticated
                        LaunchedEffect(globalAuthState) {
                            if (globalAuthState is GlobalAuthState.Unauthenticated) {
                                navController.navigate(AppDestinations.Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }

                        val addExpenseRoute = backStackEntry.toRoute<AppDestinations.AddExpense>()
                        val userId = (globalAuthState as? GlobalAuthState.Authenticated)?.user?.userId ?: ""
                        val addExpenseViewModel = AppModule.provideAddExpenseViewModel(userId = userId, listId = addExpenseRoute.listId)
                        AddExpenseScreen(
                            viewModel = addExpenseViewModel,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onExpenseSaved = { savedExpenses ->
                                // Add saved expenses to list immediately (no network refresh needed)
                                if (savedExpenses.isNotEmpty()) {
                                    val listViewModel = AppModule.provideExpenseListViewModel(
                                        userId = userId,
                                        listId = addExpenseRoute.listId
                                    )
                                    listViewModel.addExpensesLocally(savedExpenses)
                                }
                                navController.popBackStack()
                            },
                        )
                    }

                    composable<AppDestinations.ExpenseDetail> { backStackEntry ->
                        // Auth guard: redirect to login if not authenticated
                        LaunchedEffect(globalAuthState) {
                            if (globalAuthState is GlobalAuthState.Unauthenticated) {
                                navController.navigate(AppDestinations.Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }

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
                                        documentId = expenseDetail.categoryDocumentId,
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
                                val userId = (globalAuthState as? GlobalAuthState.Authenticated)?.user?.userId ?: ""
                                val listViewModel = AppModule.provideExpenseListViewModel(
                                    userId = userId,
                                    listId = expenseDetail.listId
                                )
                                listViewModel.onEvent(ExpenseListUiEvent.PendingDeleteExpense(expense.documentId))

                                // Navigate back to show snackbar
                                navController.popBackStack()
                            },
                            onEditExpense = { expenseToEdit ->
                                navController.navigate(
                                    AppDestinations.EditExpense(
                                        listId = expenseDetail.listId,
                                        expenseId = expenseToEdit.documentId,
                                        expenseName = expenseToEdit.name,
                                        expenseDescription = expenseToEdit.description,
                                        expensePrice = expenseToEdit.price,
                                        expenseDate = expenseToEdit.date,
                                        categoryDocumentId = expenseToEdit.category.documentId,
                                        categoryName = expenseToEdit.category.name,
                                        categoryIcon = expenseToEdit.category.icon,
                                        categoryColor = expenseToEdit.category.color,
                                        paidByUserId = expenseToEdit.paidBy.userId,
                                        splitDetailsJson = AppDestinations.ExpenseDetail.createSplitDetailsJson(
                                            expenseToEdit.splitDetails.map { splitDetail ->
                                                AppDestinations.SplitDetailNavigation(
                                                    userId = splitDetail.user.userId,
                                                    amount = splitDetail.amount
                                                )
                                            }
                                        ),
                                    )
                                )
                            },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this,
                        )
                    }

                    composable<AppDestinations.EditExpense> { backStackEntry ->
                        // Auth guard: redirect to login if not authenticated
                        LaunchedEffect(globalAuthState) {
                            if (globalAuthState is GlobalAuthState.Unauthenticated) {
                                navController.navigate(AppDestinations.Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }

                        val editExpense = backStackEntry.toRoute<AppDestinations.EditExpense>()
                        val expense =
                            Expense(
                                documentId = editExpense.expenseId,
                                name = editExpense.expenseName,
                                description = editExpense.expenseDescription,
                                price = editExpense.expensePrice,
                                date = editExpense.expenseDate,
                                category =
                                    Category(
                                        documentId = editExpense.categoryDocumentId,
                                        name = editExpense.categoryName,
                                        icon = editExpense.categoryIcon,
                                        color = editExpense.categoryColor,
                                    ),
                                paidBy = MockExpenseData.usersMap[editExpense.paidByUserId] ?: User(),
                                splitDetails = editExpense.splitDetails.mapNotNull { splitDetailNav ->
                                    val user = MockExpenseData.usersMap[splitDetailNav.userId]
                                    if (user != null) {
                                        com.marquis.zorroexpense.domain.model.SplitDetail(user = user, amount = splitDetailNav.amount)
                                    } else {
                                        null
                                    }
                                },
                            )

                        val userId = (globalAuthState as? GlobalAuthState.Authenticated)?.user?.userId ?: ""
                        val editViewModel = AppModule.provideAddExpenseViewModel(userId = userId, listId = editExpense.listId, expenseToEdit = expense)
                        AddExpenseScreen(
                            viewModel = editViewModel,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onExpenseSaved = { savedExpenses ->
                                // Update the expense in the list and show snackbar
                                if (savedExpenses.isNotEmpty()) {
                                    val savedExpense = savedExpenses.first()
                                    val listViewModel = AppModule.provideExpenseListViewModel(
                                        userId = userId,
                                        listId = editExpense.listId
                                    )
                                    listViewModel.updateExpenseLocally(savedExpense)
                                    // Set the name to trigger snackbar on ExpenseListScreen
                                    updatedExpenseName = savedExpense.name
                                }
                                // Navigate back to ExpenseList (pop both EditExpense and ExpenseDetail)
                                navController.popBackStack(AppDestinations.ExpenseList(listId = editExpense.listId), inclusive = false)
                            },
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
