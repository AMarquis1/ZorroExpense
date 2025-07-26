package com.marquis.zorroexpense.di

import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.repository.ExpenseRepositoryImpl
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.repository.ExpenseRepository
import com.marquis.zorroexpense.domain.usecase.AddExpenseUseCase
import com.marquis.zorroexpense.domain.usecase.DeleteExpenseUseCase
import com.marquis.zorroexpense.domain.usecase.GetExpensesUseCase
import com.marquis.zorroexpense.domain.usecase.UpdateExpenseUseCase
import com.marquis.zorroexpense.presentation.viewmodel.AddExpenseViewModel
import com.marquis.zorroexpense.presentation.viewmodel.ExpenseDetailViewModel
import com.marquis.zorroexpense.presentation.viewmodel.ExpenseListViewModel

object AppModule {
    
    // Data Layer
    private val firestoreService: FirestoreService by lazy {
        FirestoreService()
    }
    
    private val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(firestoreService)
    }
    
    // Domain Layer (Use Cases)
    private val getExpensesUseCase: GetExpensesUseCase by lazy {
        GetExpensesUseCase(expenseRepository)
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
    
    // Presentation Layer (ViewModels)
    fun provideExpenseListViewModel(
        onExpenseClick: (Expense) -> Unit = {},
        onAddExpenseClick: () -> Unit = {}
    ) = ExpenseListViewModel(
            getExpensesUseCase = getExpensesUseCase,
            onExpenseClick = onExpenseClick,
            onAddExpenseClick = onAddExpenseClick
        )
    
    fun provideAddExpenseViewModel(): AddExpenseViewModel {
        return AddExpenseViewModel(addExpenseUseCase)
    }
    
    fun provideExpenseDetailViewModel(expense: Expense): ExpenseDetailViewModel {
        return ExpenseDetailViewModel(expense)
    }
    
    // Use Cases (for direct injection if needed)
    fun provideGetExpensesUseCase(): GetExpensesUseCase = getExpensesUseCase
    fun provideAddExpenseUseCase(): AddExpenseUseCase = addExpenseUseCase
    fun provideUpdateExpenseUseCase(): UpdateExpenseUseCase = updateExpenseUseCase
    fun provideDeleteExpenseUseCase(): DeleteExpenseUseCase = deleteExpenseUseCase
    
    // Repository (for direct injection if needed)
    fun provideExpenseRepository(): ExpenseRepository = expenseRepository
}