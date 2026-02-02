package com.marquis.zorroexpense.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.NavController
import androidx.navigation.bindToBrowserNavigation

/**
 * WASM implementation: Binds NavController to browser history.
 * Enables browser back/forward buttons to work with app navigation.
 */
@OptIn(ExperimentalBrowserHistoryApi::class)
@Composable
actual fun BindBrowserNavigation(navController: NavController) {
    LaunchedEffect(navController) {
        navController.bindToBrowserNavigation()
    }
}
