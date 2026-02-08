package com.marquis.zorroexpense.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import zorroexpense.composeapp.generated.resources.Res
import com.marquis.zorroexpense.presentation.state.AuthUiEvent
import com.marquis.zorroexpense.presentation.state.AuthUiState
import com.marquis.zorroexpense.presentation.viewmodel.AuthViewModel
import zorroexpense.composeapp.generated.resources.google
import zorroexpense.composeapp.generated.resources.zorro2

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToSignUp: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val googleSignInTrigger by viewModel.googleSignInTrigger.collectAsState()

    HandleGoogleSignInTrigger(googleSignInTrigger, viewModel)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Image(
            painter = painterResource(Res.drawable.zorro2),
            contentDescription = "Zorro Header",
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
        )

        Text(
            text = "Sign In",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )

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
            label = { Text("Password") },
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
                    text = "Login successful!",
                    color = MaterialTheme.colorScheme.primary,
                )
                LaunchedEffect(Unit) {
                    onLoginSuccess()
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
            onClick = { viewModel.onEvent(AuthUiEvent.LoginClicked) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty() && uiState !is AuthUiState.Loading,
        ) {
            Text("Sign In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider with "OR" text
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Google Sign-In button
        OutlinedButton(
            onClick = { viewModel.onEvent(AuthUiEvent.GoogleSignInClicked) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            enabled = uiState !is AuthUiState.Loading,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.google),
                    contentDescription = "Google Logo",
                    modifier = Modifier.height(24.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToSignUp,
            enabled = uiState !is AuthUiState.Loading,
        ) {
            Text("Don't have an account? Sign up")
        }
    }
}

/**
 * Platform-specific handler for Google Sign-In trigger.
 * Implemented separately for Android and other platforms.
 */
@Composable
internal expect fun HandleGoogleSignInTrigger(
    trigger: Boolean,
    viewModel: AuthViewModel,
)
