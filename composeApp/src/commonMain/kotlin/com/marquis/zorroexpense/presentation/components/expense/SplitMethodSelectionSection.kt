package com.marquis.zorroexpense.presentation.components.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.domain.model.SplitMethod
import com.marquis.zorroexpense.domain.model.User

@Composable
fun SplitMethodSelectionSection(
    splitMethod: SplitMethod,
    selectedUsers: List<User>,
    percentageSplits: Map<String, Float>,
    numberSplits: Map<String, Float>,
    onSplitMethodClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Split Method",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { onSplitMethodClick() },
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text =
                            when (splitMethod) {
                                SplitMethod.PERCENTAGE -> "Percentage"
                                SplitMethod.NUMBER -> "By Amount"
                            },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text =
                            when (splitMethod) {
                                SplitMethod.PERCENTAGE -> {
                                    if (percentageSplits.isNotEmpty()) {
                                        // Check if all percentages are equal (default split)
                                        val expectedEqualPercentage = 100f / selectedUsers.size
                                        val isEqualSplit =
                                            percentageSplits.values.all {
                                                kotlin.math.abs(it - expectedEqualPercentage) < 0.1f
                                            }

                                        if (isEqualSplit) {
                                            "Equal percentage split"
                                        } else {
                                            "Custom percentage split"
                                        }
                                    } else {
                                        "Equal percentage split"
                                    }
                                }
                                SplitMethod.NUMBER -> {
                                    if (numberSplits.isNotEmpty()) {
                                        "Custom amounts"
                                    } else {
                                        "Custom amount split"
                                    }
                                }
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }

                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Change split method",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
