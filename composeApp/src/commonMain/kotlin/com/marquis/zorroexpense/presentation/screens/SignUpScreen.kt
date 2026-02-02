package com.marquis.zorroexpense.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.presentation.state.AuthUiEvent
import com.marquis.zorroexpense.presentation.state.AuthUiState
import com.marquis.zorroexpense.presentation.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp),
        )

        OutlinedTextField(
            value = displayName,
            onValueChange = { viewModel.onEvent(AuthUiEvent.DisplayNameChanged(it)) },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is AuthUiState.Loading,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.onEvent(AuthUiEvent.EmailChanged(it)) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is AuthUiState.Loading,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.onEvent(AuthUiEvent.PasswordChanged(it)) },
            label = { Text("Password (min 6 characters)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            enabled = uiState !is AuthUiState.Loading,
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (uiState) {
            is AuthUiState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthUiState.Success -> {
                Text(
                    text = "Account created successfully!",
                    color = MaterialTheme.colorScheme.primary,
                )
                LaunchedEffect(Unit) {
                    onSignUpSuccess()
                }
            }
            is AuthUiState.Error -> {
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
            AuthUiState.Idle -> {}
        }

        Button(
            onClick = { viewModel.onEvent(AuthUiEvent.SignUpClicked) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            enabled =
                email.isNotEmpty() &&
                    password.isNotEmpty() &&
                    displayName.isNotEmpty() &&
                    uiState !is AuthUiState.Loading,
        ) {
            Text("Create Account")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToLogin,
            enabled = uiState !is AuthUiState.Loading,
        ) {
            Text("Already have an account? Sign in")
        }
    }
}
