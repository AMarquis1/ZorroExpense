package com.marquis.zorroexpense.presentation.screens

import androidx.compose.runtime.Composable
import com.marquis.zorroexpense.presentation.viewmodel.AuthViewModel

/**
 * Web (WASM) implementation of Google Sign-In trigger handler (stub).
 * Web would require platform-specific implementation with web SDKs.
 * For now, this is a no-op.
 */
@Composable
internal actual fun HandleGoogleSignInTrigger(
    trigger: Boolean,
    viewModel: AuthViewModel,
) {
    // Web would require Google Sign-In for Web SDK integration
}
