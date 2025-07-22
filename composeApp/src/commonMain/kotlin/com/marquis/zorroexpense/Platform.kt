package com.marquis.zorroexpense

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform