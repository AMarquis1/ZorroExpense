package com.marquis.zorroexpense.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.marquis.zorroexpense.components.CategoryIconCircle
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.presentation.components.bottomsheets.CategorySelectionBottomSheet
import com.marquis.zorroexpense.presentation.components.bottomsheets.DatePickerBottomSheet
import com.marquis.zorroexpense.presentation.components.bottomsheets.PaidBySelectionBottomSheet
import com.marquis.zorroexpense.presentation.components.bottomsheets.RecurrenceTypeSelectionBottomSheet
import com.marquis.zorroexpense.presentation.components.bottomsheets.SplitMethodBottomSheet
import com.marquis.zorroexpense.presentation.components.bottomsheets.SplitWithSelectionBottomSheet
import com.marquis.zorroexpense.presentation.components.bottomsheets.formatDateForDisplay
import com.marquis.zorroexpense.presentation.components.expense.RecurringExpensePreview
import com.marquis.zorroexpense.presentation.components.expense.RecurringExpenseSection
import com.marquis.zorroexpense.presentation.components.expense.SplitMethodSelectionSection
import com.marquis.zorroexpense.presentation.components.expense.SplitWithSelectionSection
import com.marquis.zorroexpense.presentation.components.expense.UserSelectionSection
import com.marquis.zorroexpense.presentation.state.AddExpenseUiEvent
import com.marquis.zorroexpense.presentation.state.AddExpenseUiState
import com.marquis.zorroexpense.presentation.viewmodel.AddExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: AddExpenseViewModel,
    onBackClick: () -> Unit,
    onExpenseSaved: (List<Expense>) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val expenseName = formState.expenseName
    val expenseDescription = formState.expenseDescription
    val expensePrice = formState.expensePrice
    val selectedCategory = formState.selectedCategory
    val selectedPaidByUser = formState.selectedPaidByUser
    val selectedSplitWithUsers = formState.selectedSplitWithUsers
    val splitMethod = formState.splitMethod
    val percentageSplits = formState.percentageSplits
    val numberSplits = formState.numberSplits
    val selectedDate = formState.selectedDate
    val isRecurring = formState.isRecurring
    val recurrenceType = formState.recurrenceType
    val recurrenceDay = formState.recurrenceDay
    val futureOccurrences = formState.futureOccurrences
    val recurrenceLimit = formState.recurrenceLimit
    var showCategoryBottomSheet by remember { mutableStateOf(false) }
    var showPaidByBottomSheet by remember { mutableStateOf(false) }
    var showSplitWithBottomSheet by remember { mutableStateOf(false) }
    var showDatePickerBottomSheet by remember { mutableStateOf(false) }
    var showRecurrenceTypeBottomSheet by remember { mutableStateOf(false) }

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddExpenseUiState.Success -> {
                onExpenseSaved(state.savedExpenses)
            }
            is AddExpenseUiState.Error -> {
                // Error message will be shown in the UI
            }
            else -> { /* Handle other states */ }
        }
    }
    var showSplitMethodBottomSheet by remember { mutableStateOf(false) }
    // Loading and error state from ViewModel
    val isLoading = uiState is AddExpenseUiState.Loading
    val errorMessage =
        when (val state = uiState) {
            is AddExpenseUiState.Error -> state.message
            else -> null
        }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Validation from ViewModel
    val isNameValid = formState.isNameValid
    val isPriceValid = formState.isPriceValid
    val isFormValid = formState.isFormValid

    // Bottom sheet states
    val categoryBottomSheetState = rememberModalBottomSheetState()
    val paidByBottomSheetState = rememberModalBottomSheetState()
    val splitWithBottomSheetState = rememberModalBottomSheetState()
    val splitMethodBottomSheetState = rememberModalBottomSheetState()
    val datePickerBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val recurrenceTypeBottomSheetState = rememberModalBottomSheetState()

    // Get categories and users from ViewModel instead of MockData
    val availableCategories by viewModel.categories.collectAsState()
    val availableUsers by viewModel.availableUsers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.isEditMode) "Edit Expense" else "Add Expense",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        focusManager.clearFocus()
                    },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )

                        // Expense Name Field
                        OutlinedTextField(
                            value = expenseName,
                            onValueChange = { viewModel.onEvent(AddExpenseUiEvent.NameChanged(it)) },
                            label = { Text("Expense Name") },
                            placeholder = { Text("ex: Croquette, Alfa Longueuil, Café tope") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Expense name",
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
                            keyboardOptions =
                                KeyboardOptions(
                                    imeAction = ImeAction.Next,
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                ),
                        )

                        // Amount and Category Row - 45% and 55%
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // Expense Price Field (45%)
                            OutlinedTextField(
                                value = expensePrice,
                                onValueChange = { newValue ->
                                    viewModel.onEvent(AddExpenseUiEvent.PriceChanged(newValue))
                                },
                                label = { Text("Amount") },
                                placeholder = { Text("0.00") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.AttachMoney,
                                        contentDescription = "Amount",
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
                                keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done,
                                    ),
                            )

                            // Category Selection Field (55%)
                            Box(
                                modifier =
                                    Modifier
                                        .weight(0.55f)
                                        .clickable {
                                            focusManager.clearFocus()
                                            showCategoryBottomSheet = true
                                        },
                            ) {
                                OutlinedTextField(
                                    value = selectedCategory?.name ?: "",
                                    onValueChange = { },
                                    label = { Text("Category") },
                                    placeholder = { Text("Select a category") },
                                    leadingIcon = {
                                        if (selectedCategory != null) {
                                            CategoryIconCircle(
                                                category = selectedCategory,
                                                size = 24.dp,
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.Category,
                                                contentDescription = "Category",
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Select category",
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
                                    colors =
                                        androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        ),
                                )
                            }
                        }

                        // Date Selection Field
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        focusManager.clearFocus()
                                        showDatePickerBottomSheet = true
                                    },
                        ) {
                            OutlinedTextField(
                                value = formatDateForDisplay(selectedDate),
                                onValueChange = { },
                                label = { Text("Date") },
                                placeholder = { Text("Select date") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = "Date",
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select date",
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                enabled = false,
                                colors =
                                    androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                            )
                        }
                    }
                }

                // Payment & Split Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Split",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        // Paid By Selection
                        UserSelectionSection(
                            title = "Paid By",
                            selectedUser = selectedPaidByUser,
                            onAddClick = {
                                focusManager.clearFocus()
                                showPaidByBottomSheet = true
                            },
                            showError = selectedPaidByUser == null,
                            errorMessage = "Please select who paid",
                        )

                        // Split With Selection
                        SplitWithSelectionSection(
                            title = "Split With",
                            selectedUsers = selectedSplitWithUsers,
                            splitMethod = splitMethod,
                            percentageSplits = percentageSplits,
                            numberSplits = numberSplits,
                            expenseAmount = expensePrice.toFloatOrNull() ?: 0f,
                            onAddClick = {
                                focusManager.clearFocus()
                                showSplitWithBottomSheet = true
                            },
                            onRemoveUser = { user ->
                                viewModel.onEvent(AddExpenseUiEvent.RemoveUserFromSplit(user))
                            },
                        )

                        // Split Method Selection
                        if (selectedSplitWithUsers.isNotEmpty()) {
                            SplitMethodSelectionSection(
                                splitMethod = splitMethod,
                                selectedUsers = selectedSplitWithUsers,
                                percentageSplits = percentageSplits,
                                numberSplits = numberSplits,
                                onSplitMethodClick = {
                                    focusManager.clearFocus()
                                    showSplitMethodBottomSheet = true
                                },
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        OutlinedTextField(
                            value = expenseDescription,
                            onValueChange = { viewModel.onEvent(AddExpenseUiEvent.DescriptionChanged(it)) },
                            label = { Text("Note (Optional)") },
                            placeholder = { Text("Add notes about this expense...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = "Description",
                                )
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                            maxLines = 4,
                            keyboardOptions =
                                KeyboardOptions(
                                    imeAction = ImeAction.Done,
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                    },
                                ),
                        )
                    }
                }

                // Recurring Expense Section - only show for new expenses, not when editing
                if (!viewModel.isEditMode) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        RecurringExpenseSection(
                            isRecurring = isRecurring,
                            recurrenceType = recurrenceType,
                            recurrenceDay = recurrenceDay,
                            recurrenceLimit = recurrenceLimit,
                            onRecurringToggled = { viewModel.onEvent(AddExpenseUiEvent.RecurringToggled(it)) },
                            onRecurrenceTypeClick = { showRecurrenceTypeBottomSheet = true },
                            onRecurrenceLimitChanged = { viewModel.onEvent(AddExpenseUiEvent.RecurrenceLimitChanged(it)) },
                            modifier = Modifier.padding(16.dp),
                        )
                    }

                    if (isRecurring && futureOccurrences.isNotEmpty()) {
                        RecurringExpensePreview(
                            futureOccurrences = futureOccurrences,
                            expenseAmount = expensePrice,
                            expenseName = expenseName.ifBlank { "Recurring Expense" },
                            recurrenceLimit = recurrenceLimit,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            viewModel.onEvent(AddExpenseUiEvent.SaveExpense)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isFormValid && !isLoading,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save",
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(
                            when {
                                isLoading -> "Saving..."
                                viewModel.isEditMode -> "Update Expense"
                                else -> "Save Expense"
                            },
                        )
                    }
                }

                // Error Message
                errorMessage?.let { error ->
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onEvent(AddExpenseUiEvent.DismissError) },
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                text = "Tap to dismiss",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }

        // Category Selection Bottom Sheet
        if (showCategoryBottomSheet) {
            CategorySelectionBottomSheet(
                categories = availableCategories,
                onCategorySelected = { category ->
                    viewModel.onEvent(AddExpenseUiEvent.CategoryChanged(category))
                    showCategoryBottomSheet = false
                },
                onDismiss = {
                    showCategoryBottomSheet = false
                },
                bottomSheetState = categoryBottomSheetState,
            )
        }

        // Paid By Selection Bottom Sheet
        if (showPaidByBottomSheet) {
            PaidBySelectionBottomSheet(
                users = availableUsers,
                onUserSelected = { user ->
                    viewModel.onEvent(AddExpenseUiEvent.PaidByChanged(user))
                    // Automatically add the payer to split with if not already included
                    val updatedSplitWithUsers =
                        if (!selectedSplitWithUsers.any { it.userId == user.userId }) {
                            selectedSplitWithUsers + user
                        } else {
                            selectedSplitWithUsers
                        }
                    viewModel.onEvent(AddExpenseUiEvent.SplitWithChanged(updatedSplitWithUsers))
                    showPaidByBottomSheet = false
                },
                onDismiss = {
                    showPaidByBottomSheet = false
                },
                bottomSheetState = paidByBottomSheetState,
            )
        }

        // Split With Selection Bottom Sheet
        if (showSplitWithBottomSheet) {
            SplitWithSelectionBottomSheet(
                users = availableUsers,
                selectedUsers = selectedSplitWithUsers,
                paidByUser = selectedPaidByUser,
                onUserToggled = { user ->
                    val updatedSplitWithUsers =
                        if (selectedSplitWithUsers.any { it.userId == user.userId }) {
                            selectedSplitWithUsers.filter { it.userId != user.userId }
                        } else {
                            selectedSplitWithUsers + user
                        }
                    viewModel.onEvent(AddExpenseUiEvent.SplitWithChanged(updatedSplitWithUsers))
                },
                onDismiss = {
                    showSplitWithBottomSheet = false
                },
                bottomSheetState = splitWithBottomSheetState,
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
                onMethodChanged = { method -> viewModel.onEvent(AddExpenseUiEvent.SplitMethodChanged(method)) },
                onPercentageChanged = { userId, percentage ->
                    viewModel.onEvent(AddExpenseUiEvent.PercentageChanged(userId, percentage))
                },
                onResetToEqual = {
                    viewModel.onEvent(AddExpenseUiEvent.ResetToEqualSplits)
                },
                onNumberChanged = { userId, amount ->
                    viewModel.onEvent(AddExpenseUiEvent.NumberChanged(userId, amount))
                },
                onDismiss = { showSplitMethodBottomSheet = false },
                bottomSheetState = splitMethodBottomSheetState,
            )
        }

        // Date Picker Bottom Sheet
        if (showDatePickerBottomSheet) {
            DatePickerBottomSheet(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    viewModel.onEvent(AddExpenseUiEvent.DateChanged(date))
                    showDatePickerBottomSheet = false
                },
                onDismiss = {
                    showDatePickerBottomSheet = false
                },
                bottomSheetState = datePickerBottomSheetState,
            )
        }

        // Recurrence Type Selection Bottom Sheet
        if (showRecurrenceTypeBottomSheet) {
            RecurrenceTypeSelectionBottomSheet(
                selectedType = recurrenceType,
                onTypeSelected = { type ->
                    viewModel.onEvent(AddExpenseUiEvent.RecurrenceTypeChanged(type))
                    showRecurrenceTypeBottomSheet = false
                },
                onDismiss = {
                    showRecurrenceTypeBottomSheet = false
                },
                bottomSheetState = recurrenceTypeBottomSheetState,
            )
        }
    }
}
