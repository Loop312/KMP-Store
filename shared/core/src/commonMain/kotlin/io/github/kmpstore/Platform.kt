package io.github.kmpstore

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform