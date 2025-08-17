package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.DebtSummary
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.User

/**
 * Use case to calculate simplified debt settlements between users
 *
 * Algorithm:
 * 1. For each expense, calculate how much each person owes to the payer
 * 2. Build a debt matrix between all pairs of users
 * 3. Consolidate debts by calculating net amounts
 * 4. Return only non-zero debts as DebtSummary objects
 */
class CalculateDebtsUseCase {
    operator fun invoke(expenses: List<Expense>): List<DebtSummary> {
        if (expenses.isEmpty()) return emptyList()

        // Step 1: Build debt matrix - Map<Pair<User, User>, Double>
        // Pair(debtor, creditor) -> amount owed
        val debtMatrix = mutableMapOf<Pair<String, String>, Double>()

        expenses.forEach { expense ->
            if (expense.splitDetails.isNotEmpty() && expense.price > 0) {
                val payer = expense.paidBy

                // Each person in splitDetails owes their amount to the payer
                expense.splitDetails.forEach { splitDetail ->
                    if (splitDetail.user.userId != payer.userId) {
                        // This participant owes money to the payer
                        val debtKey = Pair(splitDetail.user.userId, payer.userId)
                        debtMatrix[debtKey] = (debtMatrix[debtKey] ?: 0.0) + splitDetail.amount
                    }
                }
            }
        }

        // Step 2: Get all unique users from expenses
        val allUsers = mutableSetOf<User>()
        expenses.forEach { expense ->
            allUsers.add(expense.paidBy)
            allUsers.addAll(expense.splitDetails.map { it.user })
        }
        val userList = allUsers.toList()

        // Step 3: Consolidate debts by calculating net amounts between each pair
        val netDebts = mutableListOf<DebtSummary>()

        for (i in userList.indices) {
            for (j in i + 1 until userList.size) {
                val userA = userList[i]
                val userB = userList[j]

                // How much A owes B
                val aOwesB = debtMatrix[Pair(userA.userId, userB.userId)] ?: 0.0
                // How much B owes A
                val bOwesA = debtMatrix[Pair(userB.userId, userA.userId)] ?: 0.0

                // Calculate net debt
                val netAmount = aOwesB - bOwesA

                when {
                    netAmount > 0.01 -> { // A owes B (with small tolerance for floating point)
                        netDebts.add(
                            DebtSummary(
                                fromUser = userA,
                                toUser = userB,
                                amount = netAmount,
                            ),
                        )
                    }
                    netAmount < -0.01 -> { // B owes A
                        netDebts.add(
                            DebtSummary(
                                fromUser = userB,
                                toUser = userA,
                                amount = -netAmount,
                            ),
                        )
                    }
                    // If netAmount is between -0.01 and 0.01, consider it settled (ignore)
                }
            }
        }

        // Step 4: Sort debts by amount (largest first) for better visibility
        return netDebts.sortedByDescending { it.amount }
    }
}
