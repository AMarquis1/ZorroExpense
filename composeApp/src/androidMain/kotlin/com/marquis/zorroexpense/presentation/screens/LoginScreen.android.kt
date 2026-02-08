package com.marquis.zorroexpense.presentation.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.marquis.zorroexpense.presentation.viewmodel.AuthViewModel

private const val TAG = "GoogleSignIn"

/**
 * Android implementation of Google Sign-In trigger handler.
 * Launches Google Sign-In intent and handles the result.
 */
@Composable
internal actual fun HandleGoogleSignInTrigger(
    trigger: Boolean,
    viewModel: AuthViewModel,
) {
    val context = LocalContext.current

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google Sign-In result: resultCode=${result.resultCode}, RESULT_OK=${Activity.RESULT_OK}")

        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Result OK - processing sign-in")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "Got account: ${account.email}")
                account.idToken?.let { idToken ->
                    Log.d(TAG, "Got ID token: ${idToken.take(20)}...")
                    viewModel.handleGoogleSignInResult(idToken)
                } ?: run {
                    Log.e(TAG, "ID token is null")
                    viewModel.resetGoogleSignInTrigger()
                }
            } catch (e: ApiException) {
                Log.e(TAG, "ApiException: ${e.statusCode} - ${e.message}", e)
                viewModel.resetGoogleSignInTrigger()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected exception", e)
                viewModel.resetGoogleSignInTrigger()
            }
        } else {
            Log.d(TAG, "Result not OK (probably cancelled or error)")
            viewModel.resetGoogleSignInTrigger()
        }
    }

    // Launch Google Sign-In intent when triggered
    LaunchedEffect(trigger) {
        if (trigger) {
            Log.d(TAG, "Launching Google Sign-In")
            try {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("10827675040-ttg79ck4gimf44inh8o5ftvh8a96l057.apps.googleusercontent.com")
                    .requestEmail()
                    .requestProfile()
                    .build()

                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                Log.d(TAG, "Launching sign-in intent")
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error launching Google Sign-In", e)
                viewModel.resetGoogleSignInTrigger()
            }
        }
    }
}
