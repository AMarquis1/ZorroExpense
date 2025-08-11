# ZorroExpense

A modern expense tracking application built with Kotlin Multiplatform and Jetpack Compose.

<div align="center">

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-2.2.0-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.8.2-4285F4?style=for-the-badge&logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore-FFA000?style=for-the-badge&logo=firebase)](https://firebase.google.com/)

**Android • iOS • Web**
</div>

## Overview

ZorroExpense is a cross-platform expense tracking application that demonstrates modern mobile development practices. Built with Kotlin Multiplatform and Jetpack Compose, it provides a consistent user experience across Android, iOS, and web platforms while maintaining native performance and platform-specific optimizations.

### Features

- **Material 3 Design** - Modern interface with platform-adaptive components
- **Reactive UI** - StateFlow-based state management with automatic UI updates
- **Firebase Integration** - Cloud synchronization using Firestore database
- **Expense Management** - Create, view, search, filter, and sort expense records
- **Clean Architecture** - MVVM pattern with clear separation of concerns
- **Development Support** - Mock data mode for offline development and testing

## Quick Start

### Prerequisites
- Android Studio Iguana or later
- JDK 17+
- Xcode 15+ (for iOS development)

### Get Up and Running

```bash
# Clone the repository
git clone https://github.com/yourusername/ZorroExpense.git
cd ZorroExpense

# Run on Android
./gradlew :composeApp:installDebug

# Run on Web
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Build everything
./gradlew build
```

### Development Mode

ZorroExpense includes mock data for development without requiring Firebase setup:

```kotlin
// In AppConfig.kt
object AppConfig {
    const val USE_MOCK_DATA = true  // Enable mock data mode
}
```

## Architecture

The application follows Clean Architecture principles with clear separation of concerns:

```
Presentation Layer          →  Jetpack Compose UI + ViewModels
Domain Layer               →  Pure business logic + Use cases  
Data Layer                 →  Repository pattern + Firebase
Dependency Injection       →  Clean, testable dependencies
```

### State Management

The application uses a reactive architecture pattern:

```kotlin
// ViewModels manage state reactively
class ExpenseListViewModel {
    val uiState: StateFlow<ExpenseListUiState>  // Observable state
    fun onEvent(event: ExpenseListUiEvent)      // Single entry point
}

// UI observes state changes automatically
@Composable
fun ExpenseListScreen(viewModel: ExpenseListViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState) {
        is Loading -> ShowLoadingAnimation()
        is Success -> ShowExpenseList(uiState.expenses)
        is Error -> ShowErrorMessage(uiState.message)
    }
}
```

## Implementation Status

### Current Features
- **Expense List** - View, search, filter, and sort expenses
- **Add Expenses** - Create new expense entries
- **Expense Details** - View detailed expense information
- **Material 3 UI** - Modern design system implementation
- **Type-Safe Navigation** - Compose Navigation integration

### Planned Features
- **Edit Expenses** - Modify existing entries
- **Categories Management** - Custom expense categories
- **User Profiles** - Multi-user support
- **Analytics** - Spending insights and reports
- **Offline Support** - Local data caching

## Platform Implementation

### Android
- Firebase SDK integration via GitLive Firebase Multiplatform
- Material 3 design system components
- Native Android lifecycle handling

### iOS
- Firebase SDK integration via GitLive Firebase Multiplatform
- Native iOS performance and feel
- iOS-specific UI adaptations

### Web
- Firebase REST API integration using Ktor HTTP client
- WebAssembly (WASM) compilation target
- Responsive web design

## Development

### Building
```bash
# Android development
./gradlew :composeApp:installDebug

# Web development
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Run tests
./gradlew test

# Production builds
./gradlew :composeApp:assembleRelease                    # Android APK
./gradlew :composeApp:assembleXCFramework                # iOS Framework  
./gradlew :composeApp:wasmJsBrowserDistribution          # Web distribution
```

### Testing
- Unit tests for ViewModels and business logic
- UI tests for Compose screens
- Integration tests for repository implementations
- Mock data available for offline development

## Contributing

Contributions are welcome. Please fork the repository and submit pull requests with:

1. Clear description of changes
2. Appropriate tests
3. Updated documentation if needed

## License

ZorroExpense is open source and available under the [MIT License](LICENSE).

## Built With

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) - Cross-platform development
- [Jetpack Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) - Declarative UI framework
- [Firebase](https://firebase.google.com/) - Backend services and database
- [Material Design 3](https://m3.material.io/) - Design system