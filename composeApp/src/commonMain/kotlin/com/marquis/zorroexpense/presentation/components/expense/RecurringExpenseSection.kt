package com.marquis.zorroexpense.presentation.components.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.domain.model.RecurrenceType

@Composable
fun RecurringExpenseSection(
    isRecurring: Boolean,
    recurrenceType: RecurrenceType,
    recurrenceDay: Int?,
    recurrenceLimit: Int?,
    onRecurringToggled: (Boolean) -> Unit,
    onRecurrenceTypeClick: () -> Unit,
    onRecurrenceLimitChanged: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Toggle Switch Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Recurring Expense",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = if (isRecurring) "This expense will repeat automatically" else "Make this a one-time expense",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }

            Switch(
                checked = isRecurring,
                onCheckedChange = onRecurringToggled,
            )
        }

        // Recurring Options (shown only when toggle is on)
        if (isRecurring) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Recurrence Type Selection
                    Text(
                        text = "Repeat frequency",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                    )

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onRecurrenceTypeClick() },
                    ) {
                        OutlinedTextField(
                            value = recurrenceType.getDisplayName(),
                            onValueChange = { },
                            label = { Text("Frequency") },
                            placeholder = { Text("Select frequency") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select frequency",
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false,
                            colors =
                                androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                        )
                    }

                    // Number of occurrences (always required)
                    Text(
                        text = "Number of occurrences",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                    )

                    // Quick selection buttons
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val quickOptions = listOf(2, 3, 5, 11, 12)
                        items(quickOptions) { option ->
                            Card(
                                modifier =
                                    Modifier.clickable {
                                        onRecurrenceLimitChanged(option)
                                    },
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            if (recurrenceLimit == option) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            },
                                    ),
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Text(
                                    text = option.toString(),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color =
                                        if (recurrenceLimit == option) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }

                    // Custom input field
                    OutlinedTextField(
                        value = recurrenceLimit?.toString() ?: "",
                        onValueChange = { value ->
                            val number = value.toIntOrNull()
                            if (value.isEmpty() || (number != null && number > 0 && number <= 999)) {
                                onRecurrenceLimitChanged(number)
                            }
                        },
                        label = { Text("Custom amount") },
                        placeholder = { Text("Enter number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        supportingText = {
                            Text("Maximum 999 occurrences")
                        },
                    )

                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = recurrenceType.getPreviewText(recurrenceDay),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
        }
    }
}
