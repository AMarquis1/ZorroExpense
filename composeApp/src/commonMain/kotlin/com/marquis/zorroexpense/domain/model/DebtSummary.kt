package com.marquis.zorroexpense.domain.model

/**
 * Represents a debt between two users after calculating net balances
 *
 * @param fromUser The user who owes money
 * @param toUser The user who is owed money
 * @param amount The amount owed (always positive)
 */
data class DebtSummary(
    val fromUser: User,
    val toUser: User,
    val amount: Double,
) {
    /**
     * Format the debt as a readable string
     * e.g., "Sarah owes $50.00 to Alex"
     */
    fun toDisplayString(): String {
        val formattedAmount =
            if (amount == amount.toInt().toDouble()) {
                "$${amount.toInt()}"
            } else {
                "$${(kotlin.math.round(amount * 100) / 100.0)}"
            }
        return "${fromUser.name} owes $formattedAmount to ${toUser.name}"
    }
}
