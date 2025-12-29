package com.marquis.zorroexpense.di

import com.marquis.zorroexpense.data.cache.CacheConfiguration
import com.marquis.zorroexpense.data.datasource.ExpenseLocalDataSource
import com.marquis.zorroexpense.data.datasource.ExpenseLocalDataSourceImpl
import com.marquis.zorroexpense.data.datasource.ExpenseRemoteDataSource
import com.marquis.zorroexpense.data.datasource.ExpenseRemoteDataSourceImpl
import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.repository.CategoryRepositoryImpl
import com.marquis.zorroexpense.data.repository.ExpenseRepositoryImpl
import com.marquis.zorroexpense.domain.cache.CacheManager
import com.marquis.zorroexpense.domain.cache.InMemoryCacheManager
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.repository.CategoryRepository
import com.marquis.zorroexpense.domain.repository.ExpenseRepository
import com.marquis.zorroexpense.domain.usecase.AddExpenseUseCase
import com.marquis.zorroexpense.domain.usecase.CalculateDebtsUseCase
import com.marquis.zorroexpense.domain.usecase.DeleteExpenseUseCase
import com.marquis.zorroexpense.domain.usecase.GetCategoriesUseCase
import com.marquis.zorroexpense.domain.usecase.GetExpensesUseCase
import com.marquis.zorroexpense.domain.usecase.RefreshExpensesUseCase
import com.marquis.zorroexpense.domain.usecase.UpdateExpenseUseCase
import com.marquis.zorroexpense.presentation.viewmodel.AddExpenseViewModel
import com.marquis.zorroexpense.presentation.viewmodel.ExpenseDetailViewModel
import com.marquis.zorroexpense.presentation.viewmodel.ExpenseListViewModel

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

    private val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(
            remoteDataSource = expenseRemoteDataSource,
            localDataSource = expenseLocalDataSource,
        )
    }

    private val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(firestoreService)
    }

    // =================
    // Use Case Layer
    // =================

    private val getExpensesUseCase: GetExpensesUseCase by lazy {
        GetExpensesUseCase(expenseRepository)
    }

    private val refreshExpensesUseCase: RefreshExpensesUseCase by lazy {
        RefreshExpensesUseCase(expenseRepository)
    }

    private val getCategoriesUseCase: GetCategoriesUseCase by lazy {
        GetCategoriesUseCase(categoryRepository)
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

    // =================
    // Presentation Layer
    // =================

    private var expenseListViewModel: ExpenseListViewModel? = null

    /**
     * Provide ExpenseListViewModel with proper lifecycle management
     * Uses clean dependency injection with interfaces
     */
    fun provideExpenseListViewModel(
        onExpenseClick: (Expense) -> Unit = {},
        onAddExpenseClick: () -> Unit = {},
    ): ExpenseListViewModel {
        // Get or create singleton instance
        val viewModel =
            expenseListViewModel ?: ExpenseListViewModel(
                getExpensesUseCase = getExpensesUseCase,
                getCategoriesUseCase = getCategoriesUseCase,
                refreshExpensesUseCase = refreshExpensesUseCase,
                deleteExpenseUseCase = deleteExpenseUseCase,
                calculateDebtsUseCase = calculateDebtsUseCase,
                onExpenseClick = onExpenseClick,
                onAddExpenseClick = onAddExpenseClick,
            ).also { expenseListViewModel = it }

        // Update callbacks for navigation
        viewModel.updateCallbacks(onExpenseClick, onAddExpenseClick)

        // Note: Data loading is handled by ViewModel's init block
        // No need to call ensureDataLoaded() here - it causes unnecessary refreshes on navigation

        return viewModel
    }

    fun provideAddExpenseViewModel(expenseToEdit: Expense? = null): AddExpenseViewModel =
        AddExpenseViewModel(
            addExpenseUseCase = addExpenseUseCase,
            updateExpenseUseCase = updateExpenseUseCase,
            getCategoriesUseCase = getCategoriesUseCase,
            expenseToEdit = expenseToEdit,
        )

    fun provideExpenseDetailViewModel(expense: Expense): ExpenseDetailViewModel = ExpenseDetailViewModel(expense)

    // =================
    // Public API for Testing and Direct Access
    // =================

    fun provideExpenseRepository(): ExpenseRepository = expenseRepository

    fun provideCategoryRepository(): CategoryRepository = categoryRepository

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
     * Reset all singletons - useful for testing
     */
    fun resetForTesting() {
        expenseListViewModel = null
        clearAllCaches()
    }
}
