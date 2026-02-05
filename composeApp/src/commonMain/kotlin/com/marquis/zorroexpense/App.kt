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
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.navigation.AppDestinations
import com.marquis.zorroexpense.platform.BindBrowserNavigation
import com.marquis.zorroexpense.presentation.screens.AddExpenseScreen
import com.marquis.zorroexpense.presentation.screens.CreateExpenseListScreen
import com.marquis.zorroexpense.presentation.screens.ExpenseDetailScreen
import com.marquis.zorroexpense.presentation.screens.ExpenseListDetailScreen
import com.marquis.zorroexpense.presentation.screens.ExpenseListScreen
import com.marquis.zorroexpense.presentation.screens.ExpenseListsOverviewScreen
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
            val startDestination =
                when (globalAuthState) {
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
                            },
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
                            },
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
                        val viewModel =
                            AppModule.provideExpenseListsViewModel(
                                userId = userId,
                                onListDeleted = { _ ->
                                    // List deleted successfully, no navigation needed
                                    // The UI is already updated in the ViewModel
                                },
                            )
                        ExpenseListsOverviewScreen(
                            viewModel = viewModel,
                            onListSelected = { listId, listName ->
                                navController.navigate(AppDestinations.ExpenseList(listId = listId, listName = listName))
                            },
                            onCreateNewList = {
                                navController.navigate(AppDestinations.CreateExpenseList)
                            },
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
                        val viewModel =
                            AppModule.provideCreateExpenseListViewModel(
                                userId = userId,
                                onListCreated = { listId, listName ->
                                    // Clear the ViewModel cache before navigation
                                    AppModule.clearCreateExpenseListViewModel()
                                    // Navigate directly to the newly created expense list
                                    navController.navigate(AppDestinations.ExpenseList(listId = listId, listName = listName)) {
                                        popUpTo(AppDestinations.CreateExpenseList) { inclusive = true }
                                    }
                                },
                            )
                        CreateExpenseListScreen(
                            viewModel = viewModel,
                            onBackClick = {
                                // Clear cache when user navigates back without creating
                                AppModule.clearCreateExpenseListViewModel()
                                navController.popBackStack()
                            },
                            onListCreated = { listId, listName ->
                                // Clear the ViewModel cache before navigation
                                AppModule.clearCreateExpenseListViewModel()
                                // Navigate directly to the newly created expense list
                                navController.navigate(AppDestinations.ExpenseList(listId = listId, listName = listName)) {
                                    popUpTo(AppDestinations.CreateExpenseList) { inclusive = true }
                                }
                            },
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
                                listName = expenseListRoute.listName,
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
                                            paidByUserName = expense.paidBy.name,
                                            paidByUserProfile = expense.paidBy.profileImage,
                                            splitDetailsJson =
                                                AppDestinations.ExpenseDetail.createSplitDetailsJson(
                                                    expense.splitDetails.map { splitDetail ->
                                                        AppDestinations.SplitDetailNavigation(
                                                            userId = splitDetail.user.userId,
                                                            userName = splitDetail.user.name,
                                                            userProfile = splitDetail.user.profileImage,
                                                            amount = splitDetail.amount,
                                                        )
                                                    },
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
                            onBackPressed = {
                                navController.popBackStack()
                            },
                            onSettingsClick = {
                                val expenseList = viewModel.expenseListMetadata.value
                                if (expenseList != null) {
                                    navController.navigate(
                                        AppDestinations.ExpenseListDetail(
                                            listId = expenseList.listId,
                                            listName = expenseList.name,
                                            shareCode = expenseList.shareCode,
                                            createdBy = expenseList.createdBy,
                                            createdAt = expenseList.createdAt,
                                            lastModified = expenseList.lastModified,
                                            membersJson = AppDestinations.ExpenseListDetail.createMembersJson(
                                                expenseList.members.map { member ->
                                                    AppDestinations.MemberNavigation(
                                                        userId = member.userId,
                                                        name = member.name,
                                                        profileImage = member.profileImage,
                                                    )
                                                },
                                            ),
                                            categoriesJson = AppDestinations.ExpenseListDetail.createCategoriesJson(
                                                expenseList.categories.map { category ->
                                                    AppDestinations.CategoryNavigation(
                                                        documentId = category.documentId,
                                                        name = category.name,
                                                        icon = category.icon,
                                                        color = category.color,
                                                    )
                                                },
                                            ),
                                        ),
                                    )
                                }
                            },
                        )
                    }

                    composable<AppDestinations.ExpenseListDetail> { backStackEntry ->
                        // Auth guard: redirect to login if not authenticated
                        LaunchedEffect(globalAuthState) {
                            if (globalAuthState is GlobalAuthState.Unauthenticated) {
                                navController.navigate(AppDestinations.Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }

                        val listDetailRoute = backStackEntry.toRoute<AppDestinations.ExpenseListDetail>()

                        // Reconstruct ExpenseList from navigation params
                        val expenseList = ExpenseList(
                            listId = listDetailRoute.listId,
                            name = listDetailRoute.listName,
                            shareCode = listDetailRoute.shareCode,
                            createdBy = listDetailRoute.createdBy,
                            createdAt = listDetailRoute.createdAt,
                            lastModified = listDetailRoute.lastModified,
                            members = listDetailRoute.members.map { memberNav ->
                                com.marquis.zorroexpense.domain.model.User(
                                    userId = memberNav.userId,
                                    name = memberNav.name,
                                    profileImage = memberNav.profileImage,
                                )
                            },
                            categories = listDetailRoute.categories.map { categoryNav ->
                                Category(
                                    documentId = categoryNav.documentId,
                                    name = categoryNav.name,
                                    icon = categoryNav.icon,
                                    color = categoryNav.color,
                                )
                            },
                        )

                        val userId = (globalAuthState as? GlobalAuthState.Authenticated)?.user?.userId ?: ""
                        val viewModel = AppModule.provideExpenseListDetailViewModel(
                            listId = listDetailRoute.listId,
                            userId = userId,
                            initialExpenseList = expenseList,
                            onListDeleted = {
                                // Navigate back to the lists overview after deletion
                                navController.popBackStack(AppDestinations.ExpenseLists, inclusive = false)
                            },
                        )

                        ExpenseListDetailScreen(
                            viewModel = viewModel,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onListDeleted = {
                                // Navigate back to the lists overview after deletion
                                navController.popBackStack(AppDestinations.ExpenseLists, inclusive = false)
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
                                    val listViewModel =
                                        AppModule.provideExpenseListViewModel(
                                            userId = userId,
                                            listId = addExpenseRoute.listId,
                                        )
                                    listViewModel.addExpensesLocally(savedExpenses)
                                }
                                navController.popBackStack()
                            },
                        )
                    }

                    composable<AppDestinations.ExpenseDetail> { backStackEntry ->
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
                                listId = expenseDetail.listId,
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
                                paidBy =
                                    com.marquis.zorroexpense.domain.model.User(
                                        userId = expenseDetail.paidByUserId,
                                        name = expenseDetail.paidByUserName,
                                        profileImage = expenseDetail.paidByUserProfile,
                                    ),
                                splitDetails =
                                    expenseDetail.splitDetails.map { splitDetailNav ->
                                        com.marquis.zorroexpense.domain.model.SplitDetail(
                                            user =
                                                com.marquis.zorroexpense.domain.model.User(
                                                    userId = splitDetailNav.userId,
                                                    name = splitDetailNav.userName,
                                                    profileImage = splitDetailNav.userProfile,
                                                ),
                                            amount = splitDetailNav.amount,
                                        )
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
                                val listViewModel =
                                    AppModule.provideExpenseListViewModel(
                                        userId = userId,
                                        listId = expenseDetail.listId,
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
                                        paidByUserName = expenseToEdit.paidBy.name,
                                        paidByUserProfile = expenseToEdit.paidBy.profileImage,
                                        splitDetailsJson =
                                            AppDestinations.EditExpense.createSplitDetailsJson(
                                                expenseToEdit.splitDetails.map { splitDetail ->
                                                    AppDestinations.SplitDetailNavigation(
                                                        userId = splitDetail.user.userId,
                                                        userName = splitDetail.user.name,
                                                        userProfile = splitDetail.user.profileImage,
                                                        amount = splitDetail.amount,
                                                    )
                                                },
                                            ),
                                    ),
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
                                listId = editExpense.listId,
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
                                paidBy =
                                    com.marquis.zorroexpense.domain.model.User(
                                        userId = editExpense.paidByUserId,
                                        name = editExpense.paidByUserName,
                                        profileImage = editExpense.paidByUserProfile,
                                    ),
                                splitDetails =
                                    editExpense.splitDetails.map { splitDetailNav ->
                                        com.marquis.zorroexpense.domain.model.SplitDetail(
                                            user =
                                                com.marquis.zorroexpense.domain.model.User(
                                                    userId = splitDetailNav.userId,
                                                    name = splitDetailNav.userName,
                                                    profileImage = splitDetailNav.userProfile,
                                                ),
                                            amount = splitDetailNav.amount,
                                        )
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
                                    val listViewModel =
                                        AppModule.provideExpenseListViewModel(
                                            userId = userId,
                                            listId = editExpense.listId,
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
