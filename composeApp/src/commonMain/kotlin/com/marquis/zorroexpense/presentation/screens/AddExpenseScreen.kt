package com.marquis.zorroexpense.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.MockExpenseData
import com.marquis.zorroexpense.components.CategoryIconCircle
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.SplitMethod
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.presentation.components.bottomsheets.CategorySelectionBottomSheet
import com.marquis.zorroexpense.presentation.components.bottomsheets.PaidBySelectionBottomSheet
import com.marquis.zorroexpense.presentation.components.bottomsheets.SplitMethodBottomSheet
import com.marquis.zorroexpense.presentation.components.bottomsheets.SplitWithSelectionBottomSheet
import com.marquis.zorroexpense.presentation.components.expense.SplitMethodSelectionSection
import com.marquis.zorroexpense.presentation.components.expense.SplitWithSelectionSection
import com.marquis.zorroexpense.presentation.components.expense.UserSelectionSection

// Constants
private const val MIN_EXPENSE_NAME_LENGTH = 2
private const val MIN_EXPENSE_AMOUNT = 0.01

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBackClick: () -> Unit,
    onExpenseSaved: () -> Unit = {}
) {
    // State variables
    var expenseName by remember { mutableStateOf("") }
    var expenseDescription by remember { mutableStateOf("") }
    var expensePrice by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedPaidByUser by remember { mutableStateOf<User?>(null) }
    var selectedSplitWithUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var splitMethod by remember { mutableStateOf(SplitMethod.PERCENTAGE) }
    var percentageSplits by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
    var numberSplits by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
    var showCategoryBottomSheet by remember { mutableStateOf(false) }
    var showPaidByBottomSheet by remember { mutableStateOf(false) }
    var showSplitWithBottomSheet by remember { mutableStateOf(false) }
    var showSplitMethodBottomSheet by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    
    // Validation logic
    val isNameValid = expenseName.length >= MIN_EXPENSE_NAME_LENGTH
    val isPriceValid = expensePrice.isNotBlank() && expensePrice.toDoubleOrNull()?.let { it >= MIN_EXPENSE_AMOUNT } == true
    val isCategoryValid = selectedCategory != null
    val isPaidByValid = selectedPaidByUser != null
    val isFormValid = isNameValid && isPriceValid && isCategoryValid && isPaidByValid
    
    // Bottom sheet states
    val categoryBottomSheetState = rememberModalBottomSheetState()
    val paidByBottomSheetState = rememberModalBottomSheetState()
    val splitWithBottomSheetState = rememberModalBottomSheetState()
    val splitMethodBottomSheetState = rememberModalBottomSheetState()
    
    // Available options from MockData
    val availableCategories = MockExpenseData.allCategories
    val availableUsers = listOf(MockExpenseData.userSarah, MockExpenseData.userAlex)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ajouter une dépense",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Expense Name Field
                        OutlinedTextField(
                            value = expenseName,
                            onValueChange = { expenseName = it },
                            label = { Text("Expense Name") },
                            placeholder = { Text("ex: Croquette, Alfa Longueuil, Café tope") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Expense name"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = expenseName.isNotBlank() && !isNameValid,
                            supportingText = {
                                if (expenseName.isNotBlank() && !isNameValid) {
                                    Text("Please enter a valid expense name")
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        // Amount and Category Row - 45% and 55%
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Expense Price Field (45%)
                            OutlinedTextField(
                                value = expensePrice,
                                onValueChange = { newValue ->
                                    // Only allow numbers and decimal point
                                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        expensePrice = newValue
                                    }
                                },
                                label = { Text("Amount") },
                                placeholder = { Text("0.00") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.AttachMoney,
                                        contentDescription = "Amount"
                                    )
                                },
                                modifier = Modifier.weight(0.45f),
                                singleLine = true,
                                isError = expensePrice.isNotBlank() && !isPriceValid,
                                supportingText = {
                                    if (expensePrice.isNotBlank() && !isPriceValid) {
                                        Text("Please enter a valid amount greater than 0")
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                            )

                            // Category Selection Field (55%)
                            Box(
                                modifier = Modifier
                                    .weight(0.55f)
                                    .clickable { showCategoryBottomSheet = true }
                            ) {
                                OutlinedTextField(
                                    value = selectedCategory?.name ?: "",
                                    onValueChange = { },
                                    label = { Text("Category") },
                                    placeholder = { Text("Select a category") },
                                    leadingIcon = {
                                        if (selectedCategory != null) {
                                            CategoryIconCircle(
                                                category = selectedCategory!!,
                                                size = 24.dp
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.Category,
                                                contentDescription = "Category"
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Select category"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false,
                                    isError = selectedCategory == null,
                                    supportingText = {
                                        if (selectedCategory == null) {
                                            Text("Please select a category")
                                        }
                                    },
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                // Payment & Split Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Split",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Paid By Selection
                        UserSelectionSection(
                            title = "Paid By",
                            selectedUser = selectedPaidByUser,
                            onAddClick = { showPaidByBottomSheet = true },
                            showError = selectedPaidByUser == null,
                            errorMessage = "Please select who paid"
                        )

                        // Split With Selection
                        SplitWithSelectionSection(
                            title = "Split With",
                            selectedUsers = selectedSplitWithUsers,
                            paidByUser = selectedPaidByUser,
                            splitMethod = splitMethod,
                            percentageSplits = percentageSplits,
                            numberSplits = numberSplits,
                            expenseAmount = expensePrice.toFloatOrNull() ?: 0f,
                            onAddClick = { showSplitWithBottomSheet = true },
                            onRemoveUser = { user ->
                                selectedSplitWithUsers = selectedSplitWithUsers.filter { it.userId != user.userId }
                                // Remove from split maps when user is removed
                                percentageSplits = percentageSplits - user.userId
                                numberSplits = numberSplits - user.userId
                                // Recalculate equal splits for remaining users
                                if (selectedSplitWithUsers.isNotEmpty()) {
                                    val remainingUsers = selectedSplitWithUsers.filter { it.userId != user.userId }
                                    val equalPercentage = 100f / remainingUsers.size
                                    percentageSplits = remainingUsers.associate { it.userId to equalPercentage }
                                    
                                    // Also update number splits to match the new percentages
                                    val totalExpense = expensePrice.toFloatOrNull() ?: 0f
                                    if (totalExpense > 0) {
                                        val equalAmount = totalExpense / remainingUsers.size
                                        numberSplits = remainingUsers.associate { it.userId to equalAmount }
                                    }
                                }
                            }
                        )
                        
                        // Split Method Selection
                        if (selectedSplitWithUsers.isNotEmpty()) {
                            SplitMethodSelectionSection(
                                splitMethod = splitMethod,
                                selectedUsers = selectedSplitWithUsers,
                                percentageSplits = percentageSplits,
                                numberSplits = numberSplits,
                                onSplitMethodClick = { showSplitMethodBottomSheet = true }
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = expenseDescription,
                            onValueChange = { expenseDescription = it },
                            label = { Text("Note (Optional)") },
                            placeholder = { Text("Add notes about this expense...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = "Description"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            )
                        )
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            //todo: Implement save logic
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isFormValid && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(if (isLoading) "Saving..." else "Save Expense")
                    }
                }
                
                // Error Message
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // Category Selection Bottom Sheet
        if (showCategoryBottomSheet) {
            CategorySelectionBottomSheet(
                categories = availableCategories,
                onCategorySelected = { category ->
                    selectedCategory = category
                    showCategoryBottomSheet = false
                },
                onDismiss = {
                    showCategoryBottomSheet = false
                },
                bottomSheetState = categoryBottomSheetState
            )
        }
        
        // Paid By Selection Bottom Sheet
        if (showPaidByBottomSheet) {
            PaidBySelectionBottomSheet(
                users = availableUsers,
                onUserSelected = { user ->
                    selectedPaidByUser = user
                    // Automatically add the payer to split with if not already included
                    if (!selectedSplitWithUsers.any { it.userId == user.userId }) {
                        selectedSplitWithUsers = selectedSplitWithUsers + user
                    }
                    // Recalculate equal percentage split
                    val equalPercentage = 100f / selectedSplitWithUsers.size
                    percentageSplits = selectedSplitWithUsers.associate { it.userId to equalPercentage }
                    
                    // Also update number splits to match the new percentages
                    val totalExpense = expensePrice.toFloatOrNull() ?: 0f
                    if (totalExpense > 0) {
                        val equalAmount = totalExpense / selectedSplitWithUsers.size
                        numberSplits = selectedSplitWithUsers.associate { it.userId to equalAmount }
                    }
                    showPaidByBottomSheet = false
                },
                onDismiss = {
                    showPaidByBottomSheet = false
                },
                bottomSheetState = paidByBottomSheetState
            )
        }
        
        // Split With Selection Bottom Sheet
        if (showSplitWithBottomSheet) {
            SplitWithSelectionBottomSheet(
                users = availableUsers,
                selectedUsers = selectedSplitWithUsers,
                paidByUser = selectedPaidByUser,
                onUserToggled = { user ->
                    if (selectedSplitWithUsers.any { it.userId == user.userId }) {
                        // Remove user (only if not the payer)
                        if (user.userId != selectedPaidByUser?.userId) {
                            selectedSplitWithUsers = selectedSplitWithUsers.filter { it.userId != user.userId }
                        }
                    } else {
                        // Add user
                        selectedSplitWithUsers = selectedSplitWithUsers + user
                    }
                    // Recalculate equal splits when user is added or removed
                    val equalPercentage = 100f / selectedSplitWithUsers.size
                    percentageSplits = selectedSplitWithUsers.associate { it.userId to equalPercentage }
                    
                    // Also update number splits to match the new percentages
                    val totalExpense = expensePrice.toFloatOrNull() ?: 0f
                    if (totalExpense > 0) {
                        val equalAmount = totalExpense / selectedSplitWithUsers.size
                        numberSplits = selectedSplitWithUsers.associate { it.userId to equalAmount }
                    }
                },
                onDismiss = {
                    showSplitWithBottomSheet = false
                },
                bottomSheetState = splitWithBottomSheetState
            )
        }
        
        // Split Method Selection Bottom Sheet
        if (showSplitMethodBottomSheet) {
            SplitMethodBottomSheet(
                currentMethod = splitMethod,
                selectedUsers = selectedSplitWithUsers,
                percentageSplits = percentageSplits,
                numberSplits = numberSplits,
                expenseAmount = expensePrice.toFloatOrNull() ?: 0f,
                onMethodChanged = { method -> splitMethod = method },
                onPercentageChanged = { userId, percentage -> 
                    // Update the changed user's percentage
                    val updatedSplits = percentageSplits + (userId to percentage)
                    
                    // Calculate the remaining percentage to distribute among other users
                    val remainingPercentage = 100f - percentage
                    val otherUsers = selectedSplitWithUsers.filter { it.userId != userId }
                    
                    if (otherUsers.isNotEmpty()) {
                        // Distribute remaining percentage equally among other users
                        val equalShare = remainingPercentage / otherUsers.size
                        val balancedSplits = updatedSplits + otherUsers.associate { it.userId to equalShare }
                        percentageSplits = balancedSplits
                        
                        // Update amounts to match the new percentages
                        val totalExpense = expensePrice.toFloatOrNull() ?: 0f
                        if (totalExpense > 0) {
                            val updatedAmounts = balancedSplits.mapValues { (_, percentageValue) ->
                                (percentageValue / 100f) * totalExpense
                            }
                            numberSplits = updatedAmounts
                        }
                    } else {
                        percentageSplits = updatedSplits
                        // Update amount for just this user if total is valid  
                        val totalExpense = expensePrice.toFloatOrNull() ?: 0f
                        if (totalExpense > 0) {
                            val amount = (percentage / 100f) * totalExpense
                            numberSplits = numberSplits + (userId to amount)
                        }
                    }
                },
                onResetToEqual = {
                    if (splitMethod == SplitMethod.PERCENTAGE) {
                        // Reset all users to equal percentage split
                        val equalPercentage = 100f / selectedSplitWithUsers.size
                        percentageSplits = selectedSplitWithUsers.associate { it.userId to equalPercentage }
                    } else {
                        // Reset all users to equal amount split
                        val totalExpense = expensePrice.toFloatOrNull() ?: 0f
                        if (totalExpense > 0) {
                            val equalAmount = totalExpense / selectedSplitWithUsers.size
                            numberSplits = selectedSplitWithUsers.associate { it.userId to equalAmount }
                        }
                    }
                },
                onNumberChanged = { userId, amount -> 
                    // Update the changed user's amount
                    val updatedSplits = numberSplits + (userId to amount)
                    
                    // Calculate the remaining amount to distribute among other users
                    val totalExpense = expensePrice.toFloatOrNull() ?: 0f
                    if (totalExpense > 0) {
                        val remainingAmount = totalExpense - amount
                        val otherUsers = selectedSplitWithUsers.filter { it.userId != userId }
                        
                        if (otherUsers.isNotEmpty() && remainingAmount >= 0) {
                            // Distribute remaining amount equally among other users
                            val equalShare = remainingAmount / otherUsers.size
                            val balancedSplits = updatedSplits + otherUsers.associate { it.userId to equalShare }
                            numberSplits = balancedSplits
                            
                            // Update percentages to match the new amounts
                            val updatedPercentages = balancedSplits.mapValues { (_, amountValue) ->
                                (amountValue / totalExpense) * 100f
                            }
                            percentageSplits = updatedPercentages
                        } else {
                            numberSplits = updatedSplits
                            // Update percentage for just this user if total is valid
                            if (totalExpense > 0) {
                                val percentage = (amount / totalExpense) * 100f
                                percentageSplits = percentageSplits + (userId to percentage)
                            }
                        }
                    } else {
                        numberSplits = updatedSplits
                    }
                },
                onDismiss = { showSplitMethodBottomSheet = false },
                bottomSheetState = splitMethodBottomSheetState
            )
        }
    }
}