# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ZorroExpense is a Kotlin Multiplatform expense tracking application targeting Android, iOS, and Web (WASM). The app uses Jetpack Compose Multiplatform for UI and Firebase Firestore for data persistence on Android.

## Development Commands

### Building and Running

```bash
# Run Android application
./gradlew :composeApp:installDebug

# Run web application in development mode
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Build all targets
./gradlew build

# Clean build
./gradlew clean
```

### Testing

```bash
# Run common tests
./gradlew :composeApp:commonMainTest

# Run Android tests
./gradlew :composeApp:testDebugUnitTest

# Run all tests
./gradlew test
```

### Platform-Specific Commands

```bash
# Generate iOS framework
./gradlew :composeApp:assembleXCFramework

# Build release APK
./gradlew :composeApp:assembleRelease

# Bundle for web deployment
./gradlew :composeApp:wasmJsBrowserDistribution
```

## Architecture

The project follows **MVVM (Model-View-ViewModel)** architecture with **Clean Architecture** principles, providing a scalable and maintainable codebase.

### Architecture Layers

#### 📱 **Presentation Layer** (`presentation/`)
- **ViewModels**: State management with StateFlow and UI event handling
- **UI States**: Sealed classes defining screen states (Loading, Success, Error)
- **UI Events**: Sealed classes for user interactions
- **Screens**: Jetpack Compose UI components that observe ViewModel state

```kotlin
// Example: ExpenseListViewModel manages state and handles events
class ExpenseListViewModel(private val getExpensesUseCase: GetExpensesUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<ExpenseListUiState>(ExpenseListUiState.Loading)
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()
    
    fun onEvent(event: ExpenseListUiEvent) { /* Handle UI events */ }
}
```

#### 💼 **Domain Layer** (`domain/`)
- **Models**: Clean business entities (Category, Expense, User)
- **Repository Interfaces**: Abstract data access contracts
- **Use Cases**: Business logic encapsulation

```kotlin
// Example: Clean domain model without framework dependencies
data class Expense(
    val description: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val date: String = "",
    val category: Category = Category(),
    val paidBy: String = "",
    val splitWith: List<String> = emptyList()
)
```

#### 💾 **Data Layer** (`data/`)
- **Repository Implementations**: Concrete data access implementations
- **DTOs**: Data Transfer Objects with serialization annotations
- **Data Sources**: Platform-specific data access (FirestoreService)
- **Mappers**: Convert between DTOs and Domain models

```kotlin
// Example: Repository handles data source selection (Mock vs Firestore)
class ExpenseRepositoryImpl(private val firestoreService: FirestoreService) : ExpenseRepository {
    override suspend fun getExpenses(): Result<List<Expense>> {
        return if (AppConfig.USE_MOCK_DATA) {
            MockExpenseData.getMockExpenses()
        } else {
            firestoreService.getExpenses().mapCatching { dtos -> dtos.map { it.toDomain() } }
        }
    }
}
```

#### 🔧 **Dependency Injection** (`di/`)
- **AppModule**: Manual dependency injection providing ViewModels and dependencies
- **Singleton Pattern**: Ensures single instances of repositories and services

### Reactive State Management

The app uses **StateFlow** for reactive state management:

```kotlin
// Screen observes ViewModel state reactively
@Composable
fun ExpenseListScreen(viewModel: ExpenseListViewModel, ...) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState) {
        is ExpenseListUiState.Loading -> LoadingUI()
        is ExpenseListUiState.Success -> ExpenseListUI(uiState.expenses)
        is ExpenseListUiState.Error -> ErrorUI(uiState.message)
    }
}
```

### Navigation Integration

Type-safe navigation with ViewModel integration:

```kotlin
// App.kt provides ViewModels through dependency injection
composable<AppDestinations.ExpenseList> {
    val viewModel = AppModule.provideExpenseListViewModel()
    ExpenseListScreen(viewModel = viewModel, ...)
}
```

### Multiplatform Structure

- **commonMain**: Shared MVVM architecture, business logic, and UI
- **androidMain**: Android-specific implementations (Firebase integration)
- **iosMain**: iOS-specific implementations (currently placeholder)
- **wasmJsMain**: Web-specific implementations (currently placeholder)

### Platform-Specific Implementations

**Android & iOS:**
- Uses GitLive Firebase Multiplatform SDK (`dev.gitlive.firebase`)
- Consistent API across both platforms with proper error handling
- Collection name: "Expense"
- Handles data parsing with robust type conversion

**Web (wasmJs):**
- Uses Ktor HTTP client for Firebase REST API calls
- Direct HTTP calls to `https://firestore.googleapis.com/v1/projects/{projectId}/databases/(default)/documents`
- Parses Firestore's JSON format into `Expense` objects
- No Firebase SDK dependencies (not supported on WASM)

### Dependencies

**Core:**
- Kotlin Multiplatform 2.2.0
- Compose Multiplatform 1.8.2
- Kotlinx Serialization
- Ktor client for networking
- Coil for image loading

**Platform-Specific:**
- GitLive Firebase Multiplatform (`firebase-common-ktx`, `firebase-firestore-ktx`) for Android/iOS
- Ktor Darwin client for iOS networking
- Ktor Core client for Web networking
- Jetpack Compose Activity (Android only)

**Build Configuration:**
- Gradle Version Catalogs (libs.versions.toml)
- KSP for annotation processing
- JVM Target 17
- Android SDK 36 (compile/target), minimum SDK 24

## Firebase Configuration

The project includes Firebase configuration files:
- `composeApp/google-services.json` for Android
- `iosApp/GoogleService-Info.plist` for iOS

Database structure expects documents in "Expense" collection with fields:
- `description` (String)
- `name` (String) 
- `price` (Double)
- `date` (Timestamp)

## Development Notes

### Architecture Benefits
- **Separation of Concerns**: Clear separation between UI, business logic, and data
- **Testability**: Each layer can be tested independently
- **Scalability**: Easy to add new features and screens following established patterns
- **Maintainability**: Clean dependencies and single responsibility principle
- **Reactive UI**: StateFlow ensures UI automatically updates when data changes

### Development Configuration

**Mock Data vs Production Data:**
```kotlin
// AppConfig.kt controls data source
object AppConfig {
    const val USE_MOCK_DATA = true  // Set to false for Firestore
}
```

When `USE_MOCK_DATA = true`:
- Uses `MockExpenseData.getMockExpenses()` with sample data
- No network calls or Firebase dependencies required
- Instant data loading for development

When `USE_MOCK_DATA = false`:
- Uses Firestore through `FirestoreService` expect/actual implementation
- Requires proper Firebase configuration and network connectivity

### File Organization

```
src/commonMain/kotlin/com/marquis/zorroexpense/
├── di/                          # Dependency Injection
│   └── AppModule.kt            # Manual DI container
├── domain/                      # Business Logic Layer
│   ├── model/                  # Domain models (Expense, Category, User)
│   ├── repository/             # Repository interfaces
│   └── usecase/               # Business use cases
├── data/                       # Data Access Layer
│   ├── remote/                # External data sources
│   │   ├── dto/              # Data Transfer Objects
│   │   └── FirestoreService.kt
│   └── repository/           # Repository implementations
├── presentation/               # UI Layer
│   ├── viewmodel/            # ViewModels for state management
│   ├── state/                # UI states and events
│   └── screens/              # Compose UI screens
├── components/                # Reusable UI components
├── navigation/                # Type-safe navigation
├── ui/theme/                  # Material3 theming
├── AppConfig.kt              # Application configuration
└── MockExpenseData.kt        # Development mock data
```

### Key Implementation Patterns

**ViewModel Pattern:**
```kotlin
// ViewModels expose StateFlow for reactive UI updates
class ExpenseListViewModel(private val getExpensesUseCase: GetExpensesUseCase) {
    private val _uiState = MutableStateFlow<ExpenseListUiState>(ExpenseListUiState.Loading)
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()
    
    // Handle all UI events through a single entry point
    fun onEvent(event: ExpenseListUiEvent) { /* ... */ }
}
```

**Repository Pattern:**
```kotlin
// Abstract interface in domain layer
interface ExpenseRepository {
    suspend fun getExpenses(): Result<List<Expense>>
}

// Concrete implementation in data layer
class ExpenseRepositoryImpl(private val firestoreService: FirestoreService) : ExpenseRepository
```

**Use Case Pattern:**
```kotlin
// Encapsulates business logic
class GetExpensesUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(): Result<List<Expense>> = repository.getExpenses()
}
```

### Code Patterns
- **MVVM Architecture**: Clear separation of presentation, business, and data layers
- **StateFlow**: Reactive state management with automatic UI updates
- **Sealed Classes**: Type-safe state and event handling
- **Result Type**: Functional error handling throughout the app
- **Repository Pattern**: Abstract data access with multiple implementations
- **Use Cases**: Single-responsibility business logic encapsulation
- **Dependency Injection**: Manual DI with singleton pattern
- **Clean Architecture**: Dependencies point inward (Presentation → Domain ← Data)

### Testing Strategy
- **Unit Tests**: Test ViewModels, Use Cases, and Repository implementations independently
- **UI Tests**: Test Compose screens with ViewModel integration
- **Mock Data**: Comprehensive mock data for development and testing
- **Layer Testing**: Each architecture layer can be tested in isolation

## Current Implementation Status

### ✅ Completed Features
- **MVVM Architecture**: Full implementation with Clean Architecture principles
- **ExpenseListScreen**: Complete with reactive state management, search, filtering, and sorting
- **Mock Data Integration**: Rich sample data for development and testing
- **Type-Safe Navigation**: Compose Navigation with proper ViewModel integration
- **Reactive UI**: StateFlow-based state management with automatic UI updates
- **Material3 Design**: Modern UI with proper theming and components

### 🚧 In Progress
- **AddExpenseScreen**: Functional but could be enhanced with ViewModel pattern
- **ExpenseDetailScreen**: Functional but could be enhanced with ViewModel pattern

### 📋 Future Enhancements
- **Firestore Integration**: Complete Firebase implementation for production
- **iOS Platform**: Native iOS implementations for FirestoreService
- **Web Platform**: WASM-specific implementations and Firebase REST API
- **Additional Features**: Edit expenses, categories management, user profiles
- **Testing**: Comprehensive test coverage for all layers

## Development Best Practices

### When Working with ZorroExpense

#### Code Quality Standards
- Follow MVVM pattern for new features - always create ViewModels with StateFlow
- Maintain Clean Architecture principles - dependencies flow inward
- Use sealed classes for UI states and events for type safety
- Implement proper error handling with Result<T> throughout the app
- Write unit tests for ViewModels and Use Cases when adding features

#### Platform-Specific Development
- **Android**: Use GitLive Firebase SDK, test with real Firestore data
- **iOS**: Implement expect/actual patterns, ensure proper iOS lifecycle handling
- **Web**: Use Ktor client for Firebase REST API, handle WASM-specific constraints
- Always test mock data mode before implementing real data sources

#### Performance Guidelines
- Use `collectAsState()` for efficient Compose recomposition
- Implement proper loading states to prevent UI blocking
- Cache expensive operations in ViewModels
- Use LazyColumn for large expense lists with proper keys

#### Common Development Tasks
- **Adding new screens**: Create ViewModel + UiState + UiEvent + Screen composable
- **New data sources**: Implement Repository interface in data layer first
- **UI components**: Place reusable components in `components/` package
- **Navigation**: Use type-safe navigation with proper ViewModel injection

#### Debugging Tips
- Toggle `AppConfig.USE_MOCK_DATA` for quick testing without network
- Use Android Studio's Compose Preview for UI development
- Check Firestore console for data structure validation
- Run `./gradlew clean` when switching between mock and real data modes

#### Testing Strategy
- **Unit Tests**: Focus on ViewModels and Use Cases with mock repositories
- **UI Tests**: Test Compose screens with fake ViewModels
- **Integration Tests**: Test Repository implementations with real/mock data sources
- **E2E Tests**: Test complete user flows across navigation

## AI Assistant Guidelines

### When Adding New Features
1. **Always follow MVVM pattern**: Create ViewModel with StateFlow, UiState sealed class, and UiEvent sealed class
2. **Update dependency injection**: Add new dependencies to AppModule
3. **Maintain architecture layers**: Keep domain models clean, implement repository pattern
4. **Add navigation support**: Update AppDestinations and navigation graph
5. **Consider all platforms**: Ensure new features work across Android, iOS, and Web

### When Debugging Issues
1. **Check AppConfig.USE_MOCK_DATA**: Verify data source configuration
2. **Examine StateFlow emissions**: Look for proper state transitions in ViewModels
3. **Validate repository implementations**: Ensure expect/actual implementations are correct
4. **Test platform-specific code**: Check FirestoreService implementations per platform

### When Refactoring
1. **Preserve MVVM structure**: Don't break existing ViewModel patterns
2. **Maintain test compatibility**: Ensure existing tests continue to work
3. **Update all platforms**: Apply changes consistently across commonMain/androidMain/iosMain
4. **Validate with both data modes**: Test with mock data and real Firestore data

### Code Generation Guidelines
- Generate ViewModels with proper StateFlow patterns
- Create sealed classes for states and events
- Follow existing naming conventions (ExpenseListViewModel, ExpenseListUiState, etc.)
- Include proper error handling and loading states
- Add Compose previews for new UI components