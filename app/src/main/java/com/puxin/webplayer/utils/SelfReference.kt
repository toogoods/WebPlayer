package com.puxin.webplayer.utils

class SelfReference<T>(val initializer: SelfReference<T>.() -> T) {
    private val inner = initializer()
    val self: T by lazy {
        inner ?: throw IllegalStateException("Do not use 'self' until initialized.")
    }
}

fun <T> selfReference(initializer: SelfReference<T>.() -> T): T {
    return SelfReference(initializer).self
}