# Firebase Crashlytics Integration Guide

## Overview

Firebase Crashlytics has been integrated into ZorroExpense to capture crashes, errors, and custom events. This guide explains how to use Crashlytics throughout your app.

## Setup Status

✅ **Android**: Fully implemented via Google Firebase SDK
⚠️ **iOS**: Placeholder implementation (GitLive Firebase Crashlytics not yet integrated)
⚠️ **Web (WASM)**: Placeholder implementation (Crashlytics not supported on WASM)

## Basic Usage

### Using the CrashlyticService

All Crashlytics functionality is accessed through the `CrashlyticService`, available from `AppModule`:

```kotlin
val crashlyticService = AppModule.provideCrashlyticService()
```

### Logging Custom Messages

Log important events or milestones:

```kotlin
crashlyticService.logMessage("User logged in successfully")
crashlyticService.logMessage("Started expense sync")
crashlyticService.logMessage("Payment processed")
```

### Logging Exceptions

Log non-fatal exceptions that don't crash the app:

```kotlin
try {
    // Some operation
    val expenses = getExpenses()
} catch (e: Exception) {
    // Log the exception to Crashlytics
    crashlyticService.logException(e)
    // Handle the error gracefully
    showErrorMessage("Failed to load expenses")
}
```

### Setting Custom Keys

Add custom key-value pairs to include in crash reports:

```kotlin
crashlyticService.setCustomKey("user_action", "adding_expense")
crashlyticService.setCustomKey("expense_category", "groceries")
crashlyticService.setCustomKey("group_id", "group123")
```

### Setting User Identifier

Identify which user experienced a crash:

```kotlin
crashlyticService.setUserId("user_email@example.com")
// Or use:
crashlyticService.setUserId(userId)
```

### Enabling/Disabling Collection

Control whether crashes are collected (useful for testing):

```kotlin
// Enable crash collection (default in production)
crashlyticService.setCrashCollectionEnabled(true)

// Disable for testing
crashlyticService.setCrashCollectionEnabled(false)
```

## Integration in ViewModels

Here's an example of integrating Crashlytics in an expense-related operation:

```kotlin
class ExpenseListViewModel(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val crashlyticService: CrashlyticService
) : ViewModel() {

    fun loadExpenses(listId: String) {
        viewModelScope.launch {
            try {
                crashlyticService.logMessage("Loading expenses for list: $listId")
                crashlyticService.setCustomKey("list_id", listId)

                _uiState.value = ExpenseListUiState.Loading
                val expenses = getExpensesUseCase(listId)

                _uiState.value = ExpenseListUiState.Success(expenses)
                crashlyticService.logMessage("Successfully loaded ${expenses.size} expenses")

            } catch (e: Exception) {
                crashlyticService.logException(e)
                _uiState.value = ExpenseListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

## Integration in Use Cases

Log important business logic milestones:

```kotlin
class AddExpenseUseCase(
    private val repository: ExpenseRepository,
    private val crashlyticService: CrashlyticService
) {
    suspend operator fun invoke(expense: Expense): Result<Unit> {
        return runCatching {
            crashlyticService.logMessage("Adding new expense: ${expense.name}")
            crashlyticService.setCustomKey("category", expense.category.name)
            crashlyticService.setCustomKey("amount", expense.price.toString())

            repository.addExpense(expense)

            crashlyticService.logMessage("Expense added successfully")
        }.onFailure { error ->
            crashlyticService.logException(error)
        }
    }
}
```

## Best Practices

### 1. Log Before Critical Operations
```kotlin
crashlyticService.logMessage("Starting Firebase sync")
performFirebaseSync()
crashlyticService.logMessage("Firebase sync completed")
```

### 2. Use Custom Keys for Context
```kotlin
crashlyticService.setCustomKey("operation", "expense_deletion")
crashlyticService.setCustomKey("expense_id", expenseId)
// If error occurs, Crashlytics will include these keys
```

### 3. Catch and Log Non-Fatal Errors
```kotlin
try {
    validateExpenseData(expense)
} catch (e: ValidationException) {
    // Don't crash, but log for monitoring
    crashlyticService.logException(e)
    showValidationError(e.message)
}
```

### 4. Track User Actions
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val userId = getCurrentUserId()
    AppModule.provideCrashlyticService().setUserId(userId)
}
```

### 5. Performance-Critical Sections
```kotlin
crashlyticService.logMessage("Syncing ${expenses.size} expenses")
try {
    for (expense in expenses) {
        syncExpense(expense)
    }
    crashlyticService.logMessage("Sync completed successfully")
} catch (e: Exception) {
    crashlyticService.logException(e)
    throw e
}
```

## Viewing Crashes in Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your "Zorro Expense" project
3. Navigate to **Crashlytics** in the left menu
4. View crash reports, trends, and custom data

## Testing Crashlytics

### Force a Crash (Testing Only)
```kotlin
// Never use in production!
if (BuildConfig.DEBUG) {
    throw RuntimeException("Testing Crashlytics crash")
}
```

### Log Test Message
```kotlin
AppModule.provideCrashlyticService().logMessage("Test message from app")
```

### Verify in Console
Messages and events should appear in Firebase Crashlytics console within a few minutes.

## Platform-Specific Notes

### Android
- Full Firebase Crashlytics support
- Crashes are automatically captured
- Custom logging and events work fully

### iOS (Future Enhancement)
- Currently uses placeholder implementation
- When GitLive Firebase Crashlytics is available, update `CrashlyticService.ios.kt`
- Manual crash reporting via `recordException()` method

### Web (WASM)
- Firebase Crashlytics not supported on WASM
- Currently logs to browser console
- Consider alternative error tracking for web (e.g., Sentry, custom analytics)

## Troubleshooting

### Crashes Not Appearing in Console
1. Ensure crashes are collection is enabled: `setCrashCollectionEnabled(true)`
2. Give Firebase 5-10 minutes to process
3. Check that google-services.json is included in Android build
4. Verify app has internet connectivity

### Custom Keys Not Showing
1. Ensure keys are set before crash occurs
2. Keep key names and values reasonable (not too long)
3. Use maximum 64 custom keys per crash report

### Testing in Debug Mode
1. Firebase may delay crash reporting in debug builds
2. Always test crash collection in release builds
3. Use `logMessage()` and `logException()` for immediate feedback

## Future Enhancements

- [ ] Integrate GitLive Firebase Crashlytics for iOS
- [ ] Add custom error analytics dashboard
- [ ] Implement crash notification alerts
- [ ] Add performance monitoring integration
- [ ] Set up automated crash remediation workflows

## References

- [Firebase Crashlytics Documentation](https://firebase.google.com/docs/crashlytics)
- [GitLive Firebase Crashlytics](https://github.com/gitlive/firebase-kotlin-sdk)
- [Google Firebase Console](https://console.firebase.google.com/)
