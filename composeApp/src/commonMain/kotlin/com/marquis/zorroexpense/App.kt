package com.marquis.zorroexpense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

fun formatTimestamp(timestamp: String): String {
    return if (timestamp.isBlank()) {
        "No date"
    } else {
        timestamp.substringBefore("T").takeIf { it.isNotBlank() } ?: timestamp
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var firestoreData by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()
        val firestoreService: FirestoreService = remember { FirestoreService() }

        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        firestoreData = null

                        firestoreService.getExpenses()
                            .onSuccess { expenses: List<Expense> ->
                                firestoreData = if (expenses.isEmpty()) {
                                    "No expenses found in collection"
                                } else {
                                    "Found ${expenses.size} expense(s):\n\n" +
                                            expenses.joinToString("\n\n") { expense ->
                                                "💰 ${expense.name}\n" +
                                                        "📝 ${expense.description}\n" +
                                                        "💵 $${expense.price}\n" +
                                                        "🕒 ${formatTimestamp(expense.date)}"
                                            }
                                }
                            }
                            .onFailure { exception: Throwable ->
                                errorMessage = "Error: ${exception.message}"
                            }

                        isLoading = false
                    }
                },
                enabled = !isLoading
            ) {
                Text("Test Firestore Connection")
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            firestoreData?.let { data ->
                Text(
                    text = data,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}