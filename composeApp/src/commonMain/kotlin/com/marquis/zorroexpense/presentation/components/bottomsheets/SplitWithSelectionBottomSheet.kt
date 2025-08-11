package com.marquis.zorroexpense.presentation.components.bottomsheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.components.ProfileAvatar
import com.marquis.zorroexpense.domain.model.User

/**
 * Bottom sheet for selecting users to split expense with
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitWithSelectionBottomSheet(
    users: List<User>,
    selectedUsers: List<User>,
    paidByUser: User?,
    onUserToggled: (User) -> Unit,
    onDismiss: () -> Unit,
    bottomSheetState: androidx.compose.material3.SheetState,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        ) {
            // Header with Done button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Split With",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                TextButton(
                    onClick = onDismiss,
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text("Done".uppercase())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(users) { user ->
                    val isSelected = selectedUsers.any { it.userId == user.userId }
                    val isPayer = user.userId == paidByUser?.userId

                    SplitWithUserItem(
                        user = user,
                        isSelected = isSelected,
                        isPayer = isPayer,
                        onClick = { onUserToggled(user) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SplitWithUserItem(
    user: User,
    isSelected: Boolean,
    isPayer: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor =
        when {
            isPayer -> MaterialTheme.colorScheme.primaryContainer
            isSelected -> MaterialTheme.colorScheme.secondaryContainer
            else -> Color.Transparent
        }

    val textColor =
        when {
            isPayer -> MaterialTheme.colorScheme.onPrimaryContainer
            isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = !isPayer) { onClick() }
                .padding(vertical = 4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
        shape = RoundedCornerShape(8.dp),
    ) {
        ListItem(
            headlineContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                    )
                    if (isPayer) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(Payer)",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.7f),
                        )
                    }
                }
            },
            leadingContent = {
                ProfileAvatar(
                    name = user.name,
                    size = 40.dp,
                    userProfile = user.profileImage,
                    backgroundColor = if (isPayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isPayer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                )
            },
            trailingContent = {
                if (isSelected) {
                    Icon(
                        Icons.Default.Add, // We could use a checkmark here
                        contentDescription = "Selected",
                        tint = textColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )
    }
}
