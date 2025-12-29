package com.marquis.zorroexpense.presentation.constants

import androidx.compose.material3.SnackbarDuration

/**
 * Constants for delete functionality configuration
 */
object DeleteConstants {
    /**
     * Duration for delete confirmation snackbar.
     * Using Indefinite so the snackbar stays visible during the undo window.
     * The snackbar will be programmatically dismissed after auto-delete completes.
     */
    val SNACKBAR_DURATION = SnackbarDuration.Indefinite

    /**
     * Auto-delete delay in milliseconds (3 seconds for more responsive UX)
     */
    const val AUTO_DELETE_DELAY_MS = 3000L

    /**
     * Delete confirmation dialog text
     */
    const val DELETE_CONFIRMATION_TITLE = "Delete Expense"
    const val DELETE_CONFIRMATION_MESSAGE = "Are you sure you want to delete this expense? This action cannot be undone."

    /**
     * Snackbar message template for deleted expenses
     */
    const val DELETED_MESSAGE_TEMPLATE = "Expense \"%s\" has been deleted"

    /**
     * Undo button text
     */
    const val UNDO_BUTTON_TEXT = "UNDO"
}
