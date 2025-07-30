package com.marquis.zorroexpense

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.User

object MockExpenseData {
    
    // Predefined users
    val userSarah = User(userId = "yblRlB470XiMuiJhbxSZ", name = "Sarah", profileImage = "sarah")
    val userAlex = User(userId = "5KaHBQhJUv6NdU9WuXSm", name = "Alex", profileImage = "alex")
    
    // User lookup map for easy access
    val usersMap = mapOf(
        userSarah.userId to userSarah,
        userAlex.userId to userAlex
    )
    
    // Predefined categories (using Material Icon names)
    val categoryLoyer = Category(name = "Loyer", icon = "Home", color = "#2196F3") // Home icon for housing
    val categoryEpicerie = Category(name = "Épicerie", icon = "ShoppingCart", color = "#4CAF50") // Shopping cart for groceries
    val categoryZorro = Category(name = "Zorro", icon = "Pets", color = "#FF9800") // Pets icon for cat expenses
    
    // List of all categories for filtering
    val allCategories = listOf(categoryLoyer, categoryEpicerie, categoryZorro)
    
    val sampleExpenses = listOf(
        Expense(
            name = "Loyer Janvier",
            description = "Paiement mensuel du loyer - appartement rue Saint-Denis",
            price = 1250.00,
            date = "2024-01-01T09:00:00Z",
            category = categoryLoyer,
            paidBy = userSarah.userId,
            splitWith = listOf(userSarah.userId, userAlex.userId)
        ),
        Expense(
            name = "Épicerie IGA",
            description = "Courses hebdomadaires - fruits, légumes, viande et produits laitiers",
            price = 89.95,
            date = "2024-01-15T14:30:00Z",
            category = categoryEpicerie,
            paidBy = userSarah.userId,
            splitWith = listOf(userSarah.userId, userAlex.userId)
        ),
        Expense(
            name = "Épicerie Metro",
            description = "Provisions pour la semaine - pain, lait, œufs et fromage",
            price = 32.48,
            date = "2024-01-14T11:15:00Z",
            category = categoryEpicerie,
            paidBy = userAlex.userId,
            splitWith = listOf(userAlex.userId)
        ),
        Expense(
            name = "Loyer Février",
            description = "Paiement mensuel du loyer - appartement rue Saint-Denis",
            price = 1250.00,
            date = "2024-02-01T09:00:00Z",
            category = categoryLoyer,
            paidBy = userAlex.userId,
            splitWith = listOf(userSarah.userId, userAlex.userId)
        ),
        Expense(
            name = "Épicerie Maxi",
            description = "Achats en vrac - céréales, pâtes et conserves",
            price = 45.75,
            date = "2024-01-13T16:45:00Z",
            category = categoryEpicerie,
            paidBy = userSarah.userId,
            splitWith = listOf(userSarah.userId, userAlex.userId)
        ),
        Expense(
            name = "Épicerie Provigo avec un tres long nom",
            description = "Produits frais - poisson, légumes verts et fruits de saison",
            price = 67.99,
            date = "2024-02-12T10:20:00Z",
            category = categoryEpicerie,
            paidBy = userAlex.userId,
            splitWith = listOf(userAlex.userId, userSarah.userId)
        ),
        Expense(
            name = "Épicerie Costco",
            description = "Achats en gros - viande congelée et produits d'entretien",
            price = 156.30,
            date = "2024-01-11T13:30:00Z",
            category = categoryEpicerie,
            paidBy = userSarah.userId,
            splitWith = listOf(userSarah.userId, userAlex.userId)
        ),
        Expense(
            name = "Loyer Mars",
            description = "Paiement mensuel du loyer - appartement rue Saint-Denis",
            price = 1250.00,
            date = "2024-03-01T09:00:00Z",
            category = categoryLoyer,
            paidBy = userSarah.userId,
            splitWith = listOf(userSarah.userId, userAlex.userId)
        ),
        Expense(
            name = "Épicerie Loblaws",
            description = "Produits bio - légumes organiques et pain artisanal",
            price = 78.50,
            date = "2024-01-07T17:20:00Z",
            category = categoryEpicerie,
            paidBy = userAlex.userId,
            splitWith = listOf(userAlex.userId, userSarah.userId)
        ),
        Expense(
            name = "Épicerie Jean Coutu",
            description = "Produits de première nécessité - lait, beurre et yaourt",
            price = 23.99,
            date = "2024-01-06T14:10:00Z",
            category = categoryEpicerie,
            paidBy = userAlex.userId,
            splitWith = listOf(userAlex.userId)
        ),
        Expense(
            name = "Nourriture pour Zorro",
            description = "Croquettes premium et pâtée pour chat",
            price = 174.50,
            date = "2024-01-20T16:30:00Z",
            category = categoryZorro,
            paidBy = userSarah.userId,
            splitWith = listOf(userSarah.userId, userAlex.userId)
        )
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