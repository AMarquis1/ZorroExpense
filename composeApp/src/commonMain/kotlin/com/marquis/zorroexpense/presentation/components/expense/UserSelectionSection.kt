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
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.presentation.components.expense.UserAvatarWithLabel
import com.marquis.zorroexpense.presentation.components.expense.AddUserButton

@Composable
fun UserSelectionSection(
    title: String,
    selectedUser: User?,
    onAddClick: () -> Unit,
    showError: Boolean = false,
    errorMessage: String = ""
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Show selected user or add button
            if (selectedUser != null) {
                item {
                    UserAvatarWithLabel(
                        user = selectedUser,
                        onClick = onAddClick
                    )
                }
            } else {
                item {
                    AddUserButton(
                        onClick = onAddClick,
                        label = "Add Buyer"
                    )
                }
            }
        }
        
        // Error message
        if (showError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}