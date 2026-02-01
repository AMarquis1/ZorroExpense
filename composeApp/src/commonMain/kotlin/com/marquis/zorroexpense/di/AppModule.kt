package com.marquis.zorroexpense.di

import com.marquis.zorroexpense.data.cache.CacheConfiguration
import com.marquis.zorroexpense.data.datasource.ExpenseLocalDataSource
import com.marquis.zorroexpense.data.datasource.ExpenseLocalDataSourceImpl
import com.marquis.zorroexpense.data.datasource.ExpenseRemoteDataSource
import com.marquis.zorroexpense.data.datasource.ExpenseRemoteDataSourceImpl
import com.marquis.zorroexpense.data.remote.AuthService
import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.repository.AuthRepositoryImpl
import com.marquis.zorroexpense.data.repository.CategoryRepositoryImpl
import com.marquis.zorroexpense.data.repository.ExpenseListRepositoryImpl
import com.marquis.zorroexpense.data.repository.ExpenseRepositoryImpl
import com.marquis.zorroexpense.data.repository.UserRepositoryImpl
import com.marquis.zorroexpense.domain.cache.CacheManager
import com.marquis.zorroexpense.domain.cache.InMemoryCacheManager
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.repository.AuthRepository
import com.marquis.zorroexpense.domain.repository.CategoryRepository
import com.marquis.zorroexpense.domain.repository.ExpenseListRepository
import com.marquis.zorroexpense.domain.repository.ExpenseRepository
import com.marquis.zorroexpense.domain.repository.UserRepository
import com.marquis.zorroexpense.domain.usecase.AddExpenseUseCase
import com.marquis.zorroexpense.domain.usecase.CalculateDebtsUseCase
import com.marquis.zorroexpense.domain.usecase.CreateExpenseListUseCase
import com.marquis.zorroexpense.domain.usecase.DeleteExpenseUseCase
import com.marquis.zorroexpense.domain.usecase.GetCategoriesUseCase
import com.marquis.zorroexpense.domain.usecase.GetCurrentUserUseCase
import com.marquis.zorroexpense.domain.usecase.GetExpensesByListIdUseCase
import com.marquis.zorroexpense.domain.usecase.GetExpensesUseCase
import com.marquis.zorroexpense.domain.usecase.GetUsersUseCase
import com.marquis.zorroexpense.domain.usecase.GetUserExpenseListsUseCase
import com.marquis.zorroexpense.domain.usecase.JoinExpenseListUseCase
import com.marquis.zorroexpense.domain.usecase.LoginUseCase
import com.marquis.zorroexpense.domain.usecase.LogoutUseCase
import com.marquis.zorroexpense.domain.usecase.ObserveAuthStateUseCase
import com.marquis.zorroexpense.domain.usecase.RefreshExpensesUseCase
import com.marquis.zorroexpense.domain.usecase.SignUpUseCase
import com.marquis.zorroexpense.domain.usecase.UpdateExpenseUseCase
import com.marquis.zorroexpense.presentation.viewmodel.AddExpenseViewModel
import com.marquis.zorroexpense.presentation.viewmodel.AuthViewModel
import com.marquis.zorroexpense.presentation.viewmodel.CreateExpenseListViewModel
import com.marquis.zorroexpense.presentation.viewmodel.ExpenseDetailViewModel
import com.marquis.zorroexpense.presentation.viewmodel.ExpenseListViewModel
import com.marquis.zorroexpense.presentation.viewmodel.ExpenseListsViewModel

/**
 * Clean dependency injection module following KMP and Clean Architecture standards
 *
 * Features:
 * - Proper abstraction layers
 * - Clean dependency graph
 * - Configurable cache strategies
 * - Thread-safe singleton management
 * - Easy testing and mocking
 */
object AppModule {
    // =================
    // Infrastructure Layer
    // =================

    private val authService: AuthService by lazy {
        AuthService.create()
    }

    private val firestoreService: FirestoreService by lazy {
        FirestoreService()
    }

    // =================
    // Cache Layer
    // =================

    private val expensesCacheManager: CacheManager<String, List<Expense>> by lazy {
        InMemoryCacheManager(CacheConfiguration.expensesCacheStrategy())
    }

    // =================
    // Data Sources
    // =================

    private val expenseRemoteDataSource: ExpenseRemoteDataSource by lazy {
        ExpenseRemoteDataSourceImpl(firestoreService)
    }

    private val expenseLocalDataSource: ExpenseLocalDataSource by lazy {
        ExpenseLocalDataSourceImpl(expensesCacheManager)
    }

    // =================
    // Repository Layer
    // =================

    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authService, firestoreService)
    }

    private val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(
            remoteDataSource = expenseRemoteDataSource,
            localDataSource = expenseLocalDataSource,
        )
    }

    private val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(firestoreService)
    }

    private val expenseListRepository: ExpenseListRepository by lazy {
        ExpenseListRepositoryImpl(firestoreService)
    }

    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(firestoreService)
    }

    // =================
    // Use Case Layer
    // =================

    // Auth Use Cases
    private val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(authRepository)
    }

    private val signUpUseCase: SignUpUseCase by lazy {
        SignUpUseCase(authRepository)
    }

    private val logoutUseCase: LogoutUseCase by lazy {
        LogoutUseCase(authRepository)
    }

    private val getCurrentUserUseCase: GetCurrentUserUseCase by lazy {
        GetCurrentUserUseCase(authRepository)
    }

    private val observeAuthStateUseCase: ObserveAuthStateUseCase by lazy {
        ObserveAuthStateUseCase(authRepository)
    }

    // Expense Use Cases
    private val getExpensesUseCase: GetExpensesUseCase by lazy {
        GetExpensesUseCase(expenseRepository)
    }

    private val getExpensesByListIdUseCase: GetExpensesByListIdUseCase by lazy {
        GetExpensesByListIdUseCase(expenseRepository)
    }

    private val refreshExpensesUseCase: RefreshExpensesUseCase by lazy {
        RefreshExpensesUseCase(expenseRepository)
    }

    private val getCategoriesUseCase: GetCategoriesUseCase by lazy {
        GetCategoriesUseCase(categoryRepository)
    }

    private val getUsersUseCase: GetUsersUseCase by lazy {
        GetUsersUseCase(userRepository)
    }

    private val addExpenseUseCase: AddExpenseUseCase by lazy {
        AddExpenseUseCase(expenseRepository)
    }

    private val updateExpenseUseCase: UpdateExpenseUseCase by lazy {
        UpdateExpenseUseCase(expenseRepository)
    }

    private val deleteExpenseUseCase: DeleteExpenseUseCase by lazy {
        DeleteExpenseUseCase(expenseRepository)
    }

    private val calculateDebtsUseCase: CalculateDebtsUseCase by lazy {
        CalculateDebtsUseCase()
    }

    // List-based Use Cases
    private val getUserExpenseListsUseCase: GetUserExpenseListsUseCase by lazy {
        GetUserExpenseListsUseCase(expenseListRepository)
    }

    private val createExpenseListUseCase: CreateExpenseListUseCase by lazy {
        CreateExpenseListUseCase(expenseListRepository, firestoreService)
    }

    private val joinExpenseListUseCase: JoinExpenseListUseCase by lazy {
        JoinExpenseListUseCase(expenseListRepository)
    }

    // =================
    // Presentation Layer
    // =================

    private var authViewModel: AuthViewModel? = null

    /**
     * Provide AuthViewModel as singleton for app-wide auth state
     */
    fun provideAuthViewModel(): AuthViewModel {
        val viewModel = authViewModel ?: AuthViewModel(
            loginUseCase = loginUseCase,
            signUpUseCase = signUpUseCase,
            logoutUseCase = logoutUseCase,
            getCurrentUserUseCase = getCurrentUserUseCase,
            observeAuthStateUseCase = observeAuthStateUseCase
        ).also { authViewModel = it }

        return viewModel
    }

    /**
     * Provide ExpenseListsViewModel for list selection
     */
    fun provideExpenseListsViewModel(
        userId: String,
        onListSelected: (listId: String, listName: String) -> Unit = { _, _ -> },
    ): ExpenseListsViewModel =
        ExpenseListsViewModel(
            userId = userId,
            getUserExpenseListsUseCase = getUserExpenseListsUseCase,
            joinExpenseListUseCase = joinExpenseListUseCase,
            onListSelected = onListSelected,
        )

    fun provideCreateExpenseListViewModel(
        userId: String,
        onListCreated: (listId: String, listName: String) -> Unit = { _, _ -> },
    ): CreateExpenseListViewModel =
        CreateExpenseListViewModel(
            userId = userId,
            createExpenseListUseCase = createExpenseListUseCase,
            getCategoriesUseCase = getCategoriesUseCase,
            onListCreated = onListCreated,
        )

    private var expenseListViewModel: ExpenseListViewModel? = null

    /**
     * Provide ExpenseListViewModel with proper lifecycle management
     * Uses clean dependency injection with interfaces
     */
    fun provideExpenseListViewModel(
        userId: String,
        listId: String,
        onExpenseClick: (Expense) -> Unit = {},
        onAddExpenseClick: () -> Unit = {},
    ): ExpenseListViewModel {
        // Get or create singleton instance - note: create new instance per userId for proper data isolation
        val viewModel =
            ExpenseListViewModel(
                userId = userId,
                listId = listId,
                getExpensesByListIdUseCase = getExpensesByListIdUseCase,
                getCategoriesUseCase = getCategoriesUseCase,
                deleteExpenseUseCase = deleteExpenseUseCase,
                calculateDebtsUseCase = calculateDebtsUseCase,
                onExpenseClick = onExpenseClick,
                onAddExpenseClick = onAddExpenseClick,
            )

        // Update callbacks for navigation
        viewModel.updateCallbacks(onExpenseClick, onAddExpenseClick)

        // Note: Data loading is handled by ViewModel's init block
        // No need to call ensureDataLoaded() here - it causes unnecessary refreshes on navigation

        return viewModel
    }

    fun provideAddExpenseViewModel(userId: String, listId: String, expenseToEdit: Expense? = null): AddExpenseViewModel =
        AddExpenseViewModel(
            userId = userId,
            listId = listId,
            addExpenseUseCase = addExpenseUseCase,
            updateExpenseUseCase = updateExpenseUseCase,
            getCategoriesUseCase = getCategoriesUseCase,
            getUsersUseCase = getUsersUseCase,
            expenseListRepository = expenseListRepository,
            expenseToEdit = expenseToEdit,
        )

    fun provideExpenseDetailViewModel(expense: Expense): ExpenseDetailViewModel = ExpenseDetailViewModel(expense)

    // =================
    // Public API for Testing and Direct Access
    // =================

    fun provideExpenseRepository(): ExpenseRepository = expenseRepository

    fun provideCategoryRepository(): CategoryRepository = categoryRepository

    fun provideExpenseListRepository(): ExpenseListRepository = expenseListRepository

    fun provideGetExpensesUseCase(): GetExpensesUseCase = getExpensesUseCase

    fun provideRefreshExpensesUseCase(): RefreshExpensesUseCase = refreshExpensesUseCase

    fun provideGetCategoriesUseCase(): GetCategoriesUseCase = getCategoriesUseCase

    fun provideAddExpenseUseCase(): AddExpenseUseCase = addExpenseUseCase

    fun provideUpdateExpenseUseCase(): UpdateExpenseUseCase = updateExpenseUseCase

    fun provideDeleteExpenseUseCase(): DeleteExpenseUseCase = deleteExpenseUseCase

    /**
     * Clear all caches - useful for testing or logout scenarios
     */
    fun clearAllCaches() {
        expensesCacheManager.runCatching {
            // Use a coroutine scope in real implementation
            // For now, this is a placeholder
        }
    }

    /**
     * Clear authenticated user data - useful for logout
     */
    fun clearAuthenticatedData() {
        authViewModel = null
        expenseListViewModel = null
        clearAllCaches()
    }

    /**
     * Reset all singletons - useful for testing
     */
    fun resetForTesting() {
        authViewModel = null
        expenseListViewModel = null
        clearAllCaches()
    }
}
