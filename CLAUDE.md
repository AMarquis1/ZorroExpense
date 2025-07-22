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

### Multiplatform Structure

- **commonMain**: Shared business logic, UI components, and data models
- **androidMain**: Android-specific implementations (Firebase integration)
- **iosMain**: iOS-specific implementations (currently placeholder)
- **wasmJsMain**: Web-specific implementations (currently placeholder)

### Key Components

**Data Layer:**
- `Expense` data class with Kotlinx Serialization annotations
- `FirestoreService` expect/actual pattern for platform-specific database implementations
- Android implementation uses Firebase Firestore with coroutines and suspendCancellableCoroutine

**UI Layer:**
- Single `App.kt` composable as main entry point
- Uses Material3 design system
- Implements loading states, error handling, and data display

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

### Known Limitations
- iOS and Web implementations are incomplete (placeholder TODO methods)
- No navigation system implemented yet
- Single screen application currently focused on testing Firestore connectivity
- Web target lacks Firebase support and will require REST API implementation

### Code Patterns
- Uses expect/actual declarations for platform-specific implementations
- Implements Result type for error handling
- Uses Compose state management with remember and mutableStateOf
- Follows Kotlin coroutines patterns with proper error handling

### Testing Strategy
- Basic test structure in place with `ComposeAppCommonTest.kt`
- Android unit testing configured
- No UI tests implemented yet