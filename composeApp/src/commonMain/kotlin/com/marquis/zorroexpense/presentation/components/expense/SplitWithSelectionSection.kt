package com.marquis.zorroexpense.presentation.components.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.domain.model.SplitMethod
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.presentation.components.expense.AddUserButton
import com.marquis.zorroexpense.presentation.components.expense.UserAvatarWithSplitLabel

/**
 * Section displaying selected users for expense splitting with amounts
 */
@Composable
fun SplitWithSelectionSection(
    title: String,
    selectedUsers: List<User>,
    @Suppress("UNUSED_PARAMETER") splitMethod: SplitMethod,
    percentageSplits: Map<String, Float>,
    numberSplits: Map<String, Float>,
    expenseAmount: Float,
    onAddClick: () -> Unit,
    onRemoveUser: (User) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(selectedUsers) { user ->
                val amount =
                    numberSplits[user.userId] ?: run {
                        // Calculate from percentage if no custom amount is set
                        val percentage = percentageSplits[user.userId] ?: (100f / selectedUsers.size)
                        if (expenseAmount > 0) {
                            (expenseAmount * percentage / 100f)
                        } else {
                            0f
                        }
                    }

                val displayText =
                    if (amount == amount.toInt().toFloat()) {
                        "$${amount.toInt()}"
                    } else {
                        // Format to 2 decimal places
                        val rounded = (amount * 100).toInt() / 100.0
                        "$$rounded"
                    }

                UserAvatarWithSplitLabel(
                    user = user,
                    splitText = displayText,
                    onClick = { onRemoveUser(user) },
                )
            }

            // Add button
            item {
                AddUserButton(
                    onClick = onAddClick,
                    label = "Add New",
                )
            }
        }
    }
}
