package com.marquis.zorroexpense.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import com.marquis.zorroexpense.domain.error.AuthError
import com.marquis.zorroexpense.presentation.viewmodel.AuthViewModel

@Composable
internal actual fun HandleGoogleSignInTrigger(
    trigger: Boolean,
    viewModel: AuthViewModel,
) {
    val notificationCenter = remember { NSNotificationCenter.defaultCenter }

    DisposableEffect(notificationCenter) {
        val tokenObserver = notificationCenter.addObserverForName(
            name = GoogleSignInTokenNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { notification ->
            val idToken = notification?.userInfo?.get(GoogleSignInTokenKey) as? String
            if (idToken == null) {
                viewModel.handleGoogleSignInFailure(AuthError.GoogleSignInFailed)
            } else {
                viewModel.handleGoogleSignInResult(idToken)
            }
        }
        val failureObserver = notificationCenter.addObserverForName(
            name = GoogleSignInFailureNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { notification ->
            val cancelled = notification?.userInfo?.get(GoogleSignInCancelledKey) as? Boolean ?: false
            viewModel.handleGoogleSignInFailure(
                if (cancelled) AuthError.GoogleSignInCancelled else AuthError.GoogleSignInFailed,
            )
        }
        onDispose {
            notificationCenter.removeObserver(tokenObserver)
            notificationCenter.removeObserver(failureObserver)
        }
    }

    LaunchedEffect(trigger) {
        if (trigger) {
            notificationCenter.postNotificationName(GoogleSignInStartNotification, null)
        }
    }
}

private const val GoogleSignInStartNotification = "com.marquis.zorroexpense.googleSignIn.start"
private const val GoogleSignInTokenNotification = "com.marquis.zorroexpense.googleSignIn.token"
private const val GoogleSignInFailureNotification = "com.marquis.zorroexpense.googleSignIn.failure"
private const val GoogleSignInTokenKey = "idToken"
private const val GoogleSignInCancelledKey = "cancelled"
