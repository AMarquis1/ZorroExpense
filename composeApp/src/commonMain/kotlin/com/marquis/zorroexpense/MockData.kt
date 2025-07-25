package com.marquis.zorroexpense

object MockExpenseData {
    val sampleExpenses = listOf(
        Expense(
            name = "Bouffe de Chat Premium",
            description = "Nourriture sèche Royal Canin pour Pitou - sac de 7kg chez Mondou",
            price = 89.95,
            date = "2024-01-15T14:30:00Z"
        ),
        Expense(
            name = "Conserves de Fancy Feast",
            description = "24 cannes variées pour Ti-Minou - saveurs poisson pis poulet",
            price = 32.48,
            date = "2024-01-14T11:15:00Z"
        ),
        Expense(
            name = "Gâteries pour Chats",
            description = "Temptations pis Greenies pour récompenser Grosse-Toune",
            price = 18.75,
            date = "2024-01-13T16:45:00Z"
        ),
        Expense(
            name = "Litière Agglomérante",
            description = "Arm & Hammer multi-chat 18kg - en spécial chez Canadian Tire",
            price = 24.99,
            date = "2024-02-12T10:20:00Z"
        ),
        Expense(
            name = "Jouets pour Minous",
            description = "Canne à pêche pis souris en plume pour divertir Câlin",
            price = 15.60,
            date = "2024-01-11T13:30:00Z"
        ),
        Expense(
            name = "Vaccins chez le Vét",
            description = "Vaccins annuels pour Mistigri - clinique du Dr Tremblay",
            price = 145.00,
            date = "2024-04-10T09:00:00Z"
        ),
        Expense(
            name = "Pâté Santé pour Chats",
            description = "Hill's Prescription Diet pour les reins de Papatte",
            price = 56.80,
            date = "2024-01-09T15:45:00Z"
        ),
        Expense(
            name = "Griffoir en Sisal",
            description = "Nouveau poteau à griffer pour sauver le divan de Fripouille",
            price = 42.95,
            date = "2024-01-08T12:15:00Z"
        ),
        Expense(
            name = "Suppléments Oméga",
            description = "Huile de poisson pour le pelage brillant de Duchesse",
            price = 28.50,
            date = "2024-01-07T17:20:00Z"
        ),
        Expense(
            name = "Fontaine à Eau",
            description = "Distributeur d'eau fraîche pour encourager Rouquine à boire plus",
            price = 67.99,
            date = "2024-01-06T14:10:00Z"
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