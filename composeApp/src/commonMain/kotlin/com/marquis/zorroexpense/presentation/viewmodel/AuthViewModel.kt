package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.error.AuthError
import com.marquis.zorroexpense.domain.usecase.GetCurrentUserUseCase
import com.marquis.zorroexpense.domain.usecase.LoginUseCase
import com.marquis.zorroexpense.domain.usecase.LogoutUseCase
import com.marquis.zorroexpense.domain.usecase.ObserveAuthStateUseCase
import com.marquis.zorroexpense.domain.usecase.SignUpUseCase
import com.marquis.zorroexpense.presentation.state.AuthUiEvent
import com.marquis.zorroexpense.presentation.state.AuthUiState
import com.marquis.zorroexpense.presentation.state.GlobalAuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication operations.
 * Manages login, signup, and logout flows with reactive state.
 */
class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase
) : ViewModel() {

    // Form state
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    // UI state
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Global auth state for navigation
    private val _globalAuthState = MutableStateFlow<GlobalAuthState>(GlobalAuthState.Authenticating)
    val globalAuthState: StateFlow<GlobalAuthState> = _globalAuthState.asStateFlow()

    init {
        // Observe auth state changes
        viewModelScope.launch {
            observeAuthStateUseCase().collect { authUser ->
                if (authUser != null) {
                    _globalAuthState.value = GlobalAuthState.Authenticated(authUser)
                    _uiState.value = AuthUiState.Success(authUser)
                } else {
                    _globalAuthState.value = GlobalAuthState.Unauthenticated
                }
            }
        }

        // Check if user is already authenticated on init
        viewModelScope.launch {
            getCurrentUserUseCase().onSuccess { user ->
                if (user != null) {
                    _globalAuthState.value = GlobalAuthState.Authenticated(user)
                } else {
                    _globalAuthState.value = GlobalAuthState.Unauthenticated
                }
            }.onFailure {
                _globalAuthState.value = GlobalAuthState.Unauthenticated
            }
        }
    }

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.EmailChanged -> _email.value = event.email
            is AuthUiEvent.PasswordChanged -> _password.value = event.password
            is AuthUiEvent.DisplayNameChanged -> _displayName.value = event.displayName
            AuthUiEvent.LoginClicked -> login()
            AuthUiEvent.SignUpClicked -> signUp()
            AuthUiEvent.ClearError -> _uiState.value = AuthUiState.Idle
        }
    }

    private fun login() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = loginUseCase(
                email = _email.value,
                password = _password.value
            )

            result
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Success(user)
                    clearForm()
                }
                .onFailure { error ->
                    val errorMessage = when (error) {
                        is AuthError -> error.message
                        else -> "Login failed"
                    }
                    _uiState.value = AuthUiState.Error(errorMessage)
                }
        }
    }

    private fun signUp() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = signUpUseCase(
                email = _email.value,
                password = _password.value,
                displayName = _displayName.value
            )

            result
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Success(user)
                    clearForm()
                }
                .onFailure { error ->
                    val errorMessage = when (error) {
                        is AuthError -> error.message
                        else -> "Sign up failed"
                    }
                    _uiState.value = AuthUiState.Error(errorMessage)
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
                .onSuccess {
                    _globalAuthState.value = GlobalAuthState.Unauthenticated
                    clearForm()
                    _uiState.value = AuthUiState.Idle
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error("Logout failed")
                }
        }
    }

    private fun clearForm() {
        _email.value = ""
        _password.value = ""
        _displayName.value = ""
    }
}
