package com.marquis.zorroexpense.presentation.components.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.components.ProfileAvatar
import com.marquis.zorroexpense.domain.model.SplitMethod
import com.marquis.zorroexpense.domain.model.User

/**
 * Bottom sheet for configuring split method (percentage vs amount)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitMethodBottomSheet(
    currentMethod: SplitMethod,
    selectedUsers: List<User>,
    percentageSplits: Map<String, Float>,
    numberSplits: Map<String, Float>,
    expenseAmount: Float,
    onMethodChanged: (SplitMethod) -> Unit,
    onPercentageChanged: (String, Float) -> Unit,
    onNumberChanged: (String, Float) -> Unit,
    onResetToEqual: () -> Unit,
    onDismiss: () -> Unit,
    bottomSheetState: androidx.compose.material3.SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Title and Done button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Split Method",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Done button
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = "Done".uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            
            // Segmented control for method selection
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    onClick = { onMethodChanged(SplitMethod.PERCENTAGE) },
                    selected = currentMethod == SplitMethod.PERCENTAGE
                ) {
                    Text("Percentage")
                }
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    onClick = { onMethodChanged(SplitMethod.NUMBER) },
                    selected = currentMethod == SplitMethod.NUMBER
                ) {
                    Text("Amount")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User list with split values
            LazyColumn {
                items(selectedUsers) { user ->
                    val currentPercentage = percentageSplits[user.userId] ?: (100f / selectedUsers.size)
                    val currentAmount = numberSplits[user.userId] ?: run {
                        // Pre-fill amount based on percentage if no custom amount is set
                        if (expenseAmount > 0 && currentPercentage > 0) {
                            (expenseAmount * currentPercentage / 100f)
                        } else {
                            expenseAmount / selectedUsers.size
                        }
                    }
                    
                    SplitUserItem(
                        user = user,
                        splitMethod = currentMethod,
                        percentageValue = currentPercentage,
                        numberValue = currentAmount,
                        expenseAmount = expenseAmount,
                        selectedUsers = selectedUsers,
                        onPercentageFinalized = { value -> onPercentageChanged(user.userId, value) },
                        onNumberFinalized = { value -> onNumberChanged(user.userId, value) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Reset to Equal button (show when splits are not equal)
            val shouldShowResetButton = when (currentMethod) {
                SplitMethod.PERCENTAGE -> {
                    if (percentageSplits.isNotEmpty()) {
                        val expectedEqualPercentage = 100f / selectedUsers.size
                        !percentageSplits.values.all { 
                            kotlin.math.abs(it - expectedEqualPercentage) < 0.1f 
                        }
                    } else false
                }
                SplitMethod.NUMBER -> {
                    if (numberSplits.isNotEmpty() && expenseAmount > 0) {
                        val expectedEqualAmount = expenseAmount / selectedUsers.size
                        !numberSplits.values.all { 
                            kotlin.math.abs(it - expectedEqualAmount) < 0.01f 
                        }
                    } else false
                }
            }
            
            if (shouldShowResetButton) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    OutlinedButton(
                        onClick = onResetToEqual
                    ) {
                        Text("Reset".uppercase())
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Individual user item for split method configuration
 */
@Composable
private fun SplitUserItem(
    user: User,
    splitMethod: SplitMethod,
    percentageValue: Float,
    numberValue: Float,
    @Suppress("UNUSED_PARAMETER") expenseAmount: Float,
    @Suppress("UNUSED_PARAMETER") selectedUsers: List<User>,
    onPercentageFinalized: (Float) -> Unit,
    onNumberFinalized: (Float) -> Unit
) {
    // Local state for the percentage input while editing
    var tempPercentageText by remember(user.userId, percentageValue) { 
        mutableStateOf(if (percentageValue > 0) percentageValue.toInt().toString() else "") 
    }
    
    // Local state for the amount input while editing
    var tempAmountText by remember(user.userId, numberValue) {
        mutableStateOf(
            if (numberValue > 0) {
                // Format to clean decimal display
                val rounded = (numberValue * 100).toInt() / 100.0
                if (rounded == rounded.toInt().toDouble()) {
                    rounded.toInt().toString()
                } else {
                    rounded.toString()
                }
            } else ""
        )
    }
    
    // Update temp states when values change (from auto-balancing)
    LaunchedEffect(numberValue) {
        if (numberValue > 0) {
            val rounded = (numberValue * 100).toInt() / 100.0
            val formattedValue = if (rounded == rounded.toInt().toDouble()) {
                rounded.toInt().toString()
            } else {
                rounded.toString()
            }
            tempAmountText = formattedValue
        } else {
            tempAmountText = ""
        }
    }
    
    LaunchedEffect(percentageValue) {
        tempPercentageText = if (percentageValue > 0) percentageValue.toInt().toString() else ""
    }
    
    val focusManager = LocalFocusManager.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileAvatar(
                name = user.name,
                size = 40.dp,
                userProfile = user.profileImage,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            
            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            when (splitMethod) {
                SplitMethod.PERCENTAGE -> {
                    OutlinedTextField(
                        value = tempPercentageText,
                        onValueChange = { value ->
                            // Update local state immediately for responsive UI
                            tempPercentageText = value
                        },
                        modifier = Modifier
                            .width(80.dp)
                            .onFocusChanged { focusState ->
                                // Finalize percentage when user loses focus
                                if (!focusState.isFocused && tempPercentageText.isNotEmpty()) {
                                    tempPercentageText.toIntOrNull()?.let { percentage ->
                                        if (percentage in 0..100) {
                                            onPercentageFinalized(percentage.toFloat())
                                        }
                                    }
                                }
                            },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                // Finalize percentage when user presses Done
                                tempPercentageText.toIntOrNull()?.let { percentage ->
                                    if (percentage in 0..100) {
                                        onPercentageFinalized(percentage.toFloat())
                                    }
                                }
                                // Clear focus to hide keyboard
                                focusManager.clearFocus()
                            }
                        ),
                        placeholder = { Text("0") },
                        suffix = { Text("%") }
                    )
                }
                SplitMethod.NUMBER -> {
                    OutlinedTextField(
                        value = tempAmountText,
                        onValueChange = { value ->
                            // Update local state immediately for responsive UI
                            tempAmountText = value
                        },
                        modifier = Modifier
                            .width(100.dp)
                            .onFocusChanged { focusState ->
                                // Finalize amount when user loses focus
                                if (!focusState.isFocused && tempAmountText.isNotEmpty()) {
                                    tempAmountText.toFloatOrNull()?.let { amount ->
                                        if (amount >= 0) {
                                            onNumberFinalized(amount)
                                        }
                                    }
                                }
                            },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                // Finalize amount when user presses Done
                                tempAmountText.toFloatOrNull()?.let { amount ->
                                    if (amount >= 0) {
                                        onNumberFinalized(amount)
                                    }
                                }
                                // Clear focus to hide keyboard
                                focusManager.clearFocus()
                            }
                        ),
                        placeholder = { Text("0.00") },
                        prefix = { Text("$") }
                    )
                }
            }
        }
    }
}