package com.marquis.zorroexpense.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.AppConfig
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.MockExpenseData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBackClick: () -> Unit,
    onExpenseSaved: () -> Unit = {}
) {
    var expenseName by remember { mutableStateOf("") }
    var expenseDescription by remember { mutableStateOf("") }
    var expensePrice by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    
    // Validation states
    val isNameValid = expenseName.isNotBlank()
    val isPriceValid = expensePrice.isNotBlank() && expensePrice.toDoubleOrNull() != null && expensePrice.toDoubleOrNull()!! > 0
    val isFormValid = isNameValid && isPriceValid
    
    val saveExpense = {
        if (isFormValid) {
            isLoading = true
            errorMessage = null
            
            coroutineScope.launch {
                // Simple date string for now - can be enhanced later
                val currentDate = "2024-01-01T12:00:00" // TODO: Use proper date/time
                
                val expense = Expense(
                    name = expenseName.trim(),
                    description = expenseDescription.trim(),
                    price = expensePrice.toDouble(),
                    date = currentDate
                )
                
                if (AppConfig.USE_MOCK_DATA) {
                    // For mock data, we'll just simulate success
                    kotlinx.coroutines.delay(1000) // Simulate network delay
                    isLoading = false
                    snackbarHostState.showSnackbar("Expense saved successfully!")
                    kotlinx.coroutines.delay(1000)
                    onExpenseSaved()
                } else {
                    // TODO: Implement actual saving to Firestore
                    // For now, simulate success
                    kotlinx.coroutines.delay(1000)
                    isLoading = false
                    snackbarHostState.showSnackbar("Expense saved successfully!")
                    kotlinx.coroutines.delay(1000)
                    onExpenseSaved()
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add New Expense",
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💰 Track Your Expense",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
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
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Expense Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Expense Name Field
                        OutlinedTextField(
                            value = expenseName,
                            onValueChange = { expenseName = it },
                            label = { Text("Expense Name") },
                            placeholder = { Text("e.g., Lunch, Gas, Coffee") },
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
                        
                        // Expense Price Field
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
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = expensePrice.isNotBlank() && !isPriceValid,
                            supportingText = {
                                if (expensePrice.isNotBlank() && !isPriceValid) {
                                    Text("Please enter a valid amount greater than 0")
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                        
                        // Expense Description Field
                        OutlinedTextField(
                            value = expenseDescription,
                            onValueChange = { expenseDescription = it },
                            label = { Text("Description (Optional)") },
                            placeholder = { Text("Add notes about this expense...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = "Description"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { 
                                    focusManager.clearFocus()
                                    if (isFormValid) saveExpense()
                                }
                            )
                        )
                        
                        // Date Info
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Date",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Date: Today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
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
                        onClick = saveExpense,
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
    }
}