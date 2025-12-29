package com.marquis.zorroexpense.platform

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/**
 * Binds navigation controller to browser history for back/forward button support.
 * Only has effect on WASM/JS targets - no-op on Android and iOS.
 */
@Composable
expect fun BindBrowserNavigation(navController: NavController)
