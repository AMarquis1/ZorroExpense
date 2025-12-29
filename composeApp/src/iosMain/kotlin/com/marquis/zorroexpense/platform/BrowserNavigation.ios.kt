package com.marquis.zorroexpense.platform

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/**
 * iOS implementation: No-op since browser navigation only applies to web.
 */
@Composable
actual fun BindBrowserNavigation(navController: NavController) {
    // No-op: Browser navigation binding only applies to WASM/JS targets
}
