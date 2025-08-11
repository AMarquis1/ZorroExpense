package com.marquis.zorroexpense

object AppConfig {
    // Set to true to use mock data during development (saves API costs)
    // Set to false to use real Firestore data
    const val USE_MOCK_DATA = false

    // Debug settings
    const val ENABLE_LOGGING = true
    const val SIMULATE_NETWORK_DELAY = true
}
