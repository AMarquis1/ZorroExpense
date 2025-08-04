package com.marquis.zorroexpense.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    // For WASM, just wrap the content without pull-to-refresh for now
    // Could be implemented with web-specific gestures later
    Box(modifier = modifier) {
        content()
    }
}