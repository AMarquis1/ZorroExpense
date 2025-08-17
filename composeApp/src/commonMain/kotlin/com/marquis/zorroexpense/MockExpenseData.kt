package com.marquis.zorroexpense

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.SplitDetail
import com.marquis.zorroexpense.domain.model.User

object MockExpenseData {
    // Predefined users
    val userSarah = User(userId = "yblRlB470XiMuiJhbxSZ", name = "Sarah", profileImage = "sarah")
    val userAlex = User(userId = "5KaHBQhJUv6NdU9WuXSm", name = "Alex", profileImage = "alex")

    // User lookup map for easy access
    val usersMap =
        mapOf(
            userSarah.userId to userSarah,
            userAlex.userId to userAlex,
        )

    // Predefined categories (using Material Icon names)
    val categoryLoyer =
        Category(
            documentId = "category_loyer_001",
            name = "Loyer",
            icon = "Home",
            color = "#2196F3",
        ) // Home icon for housing
    val categoryEpicerie =
        Category(
            documentId = "category_epicerie_001",
            name = "Épicerie",
            icon = "ShoppingCart",
            color = "#4CAF50",
        ) // Shopping cart for groceries
    val categoryZorro =
        Category(
            documentId = "category_zorro_001",
            name = "Zorro",
            icon = "Pets",
            color = "#FF9800",
        ) // Pets icon for cat expenses

    // List of all categories for filtering
    val allCategories = listOf(categoryLoyer, categoryEpicerie, categoryZorro)

    val sampleExpenses =
        listOf(
            Expense(
                name = "Loyer Janvier",
                description = "Paiement mensuel du loyer - appartement rue Saint-Denis",
                price = 1250.00,
                date = "2024-01-01T09:00:00Z",
                category = categoryLoyer,
                paidBy = userSarah,
                splitDetails = listOf(
                    SplitDetail(user = userSarah, amount = 625.00),
                    SplitDetail(user = userAlex, amount = 625.00)
                ),
            ),
            Expense(
                name = "Épicerie IGA",
                description = "Courses hebdomadaires - fruits, légumes, viande et produits laitiers",
                price = 89.95,
                date = "2024-01-15T14:30:00Z",
                category = categoryEpicerie,
                paidBy = userSarah,
                splitDetails = listOf(
                    SplitDetail(user = userSarah, amount = 44.98),
                    SplitDetail(user = userAlex, amount = 44.97)
                ),
            ),
            Expense(
                name = "Épicerie Metro",
                description = "Provisions pour la semaine - pain, lait, œufs et fromage",
                price = 32.48,
                date = "2024-01-14T11:15:00Z",
                category = categoryEpicerie,
                paidBy = userAlex,
                splitDetails = listOf(
                    SplitDetail(user = userAlex, amount = 32.48)
                ),
            ),
            Expense(
                name = "Loyer Février",
                description = "Paiement mensuel du loyer - appartement rue Saint-Denis",
                price = 1250.00,
                date = "2024-02-01T09:00:00Z",
                category = categoryLoyer,
                paidBy = userAlex,
                splitDetails = listOf(
                    SplitDetail(user = userSarah, amount = 625.00),
                    SplitDetail(user = userAlex, amount = 625.00)
                ),
            ),
            Expense(
                name = "Épicerie Maxi",
                description = "Achats en vrac - céréales, pâtes et conserves",
                price = 45.75,
                date = "2024-01-13T16:45:00Z",
                category = categoryEpicerie,
                paidBy = userSarah,
                splitDetails = listOf(
                    SplitDetail(user = userSarah, amount = 22.88),
                    SplitDetail(user = userAlex, amount = 22.87)
                ),
            ),
            Expense(
                name = "Épicerie Provigo avec un tres long nom",
                description = "Produits frais - poisson, légumes verts et fruits de saison",
                price = 67.99,
                date = "2024-02-12T10:20:00Z",
                category = categoryEpicerie,
                paidBy = userAlex,
                splitDetails = listOf(
                    SplitDetail(user = userAlex, amount = 34.00),
                    SplitDetail(user = userSarah, amount = 33.99)
                ),
            ),
            Expense(
                name = "Épicerie Costco",
                description = "Achats en gros - viande congelée et produits d'entretien",
                price = 156.30,
                date = "2024-01-11T13:30:00Z",
                category = categoryEpicerie,
                paidBy = userSarah,
                splitDetails = listOf(
                    SplitDetail(user = userSarah, amount = 78.15),
                    SplitDetail(user = userAlex, amount = 78.15)
                ),
            ),
            Expense(
                name = "Loyer Mars",
                description = "Paiement mensuel du loyer - appartement rue Saint-Denis",
                price = 1250.00,
                date = "2024-03-01T09:00:00Z",
                category = categoryLoyer,
                paidBy = userSarah,
                splitDetails = listOf(
                    SplitDetail(user = userSarah, amount = 625.00),
                    SplitDetail(user = userAlex, amount = 625.00)
                ),
            ),
            Expense(
                name = "Épicerie Loblaws",
                description = "Produits bio - légumes organiques et pain artisanal",
                price = 78.50,
                date = "2024-01-07T17:20:00Z",
                category = categoryEpicerie,
                paidBy = userAlex,
                splitDetails = listOf(
                    SplitDetail(user = userAlex, amount = 39.25),
                    SplitDetail(user = userSarah, amount = 39.25)
                ),
            ),
            Expense(
                name = "Épicerie Jean Coutu",
                description = "Produits de première nécessité - lait, beurre et yaourt",
                price = 23.99,
                date = "2024-01-06T14:10:00Z",
                category = categoryEpicerie,
                paidBy = userAlex,
                splitDetails = listOf(
                    SplitDetail(user = userAlex, amount = 23.99)
                ),
            ),
            Expense(
                name = "Nourriture pour Zorro",
                description = "Croquettes premium et pâtée pour chat",
                price = 174.50,
                date = "2024-01-20T16:30:00Z",
                category = categoryZorro,
                paidBy = userSarah,
                splitDetails = listOf(
                    SplitDetail(user = userSarah, amount = 87.25),
                    SplitDetail(user = userAlex, amount = 87.25)
                ),
            ),
            // Future expenses for testing - showing custom split scenario like the user's example
            Expense(
                name = "Loyer Mai",
                description = "Sarah paid 15$ and split it: Sarah 3$ and Alex 12$",
                price = 15.00,
                date = "2025-05-01T09:00:00Z",
                category = categoryLoyer,
                paidBy = userSarah,
                splitDetails = listOf(
                    SplitDetail(user = userSarah, amount = 3.00),
                    SplitDetail(user = userAlex, amount = 12.00)
                ),
                isFromRecurring = true,
            ),
            Expense(
                name = "Épicerie Future",
                description = "Courses prévues pour la semaine prochaine",
                price = 95.50,
                date = "2025-02-15T14:30:00Z",
                category = categoryEpicerie,
                paidBy = userAlex,
                splitDetails = listOf(
                    SplitDetail(user = userSarah, amount = 47.75),
                    SplitDetail(user = userAlex, amount = 47.75)
                ),
            ),
        )

    suspend fun getMockExpenses(): Result<List<Expense>> {
        // Simulate network delay
//        delay(200)
        return Result.success(sampleExpenses)

//        // Simulate occasional errors for testing
//        return if (Random.nextDouble() < 0.95) {
//            Result.success(sampleExpenses)
//        } else {
//            Result.failure(Exception("Simulated network error"))
//        }
    }
}
