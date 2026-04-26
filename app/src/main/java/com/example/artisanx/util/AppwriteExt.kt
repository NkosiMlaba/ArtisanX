package com.example.artisanx.util

import io.appwrite.exceptions.AppwriteException

fun Throwable.isSessionExpired(): Boolean {
    if (this !is AppwriteException) return false
    val c = code ?: return false
    return c == 401 || c == 1000
}

inline fun <T> appwriteCall(block: () -> T): Resource<T> {
    return try {
        Resource.Success(block())
    } catch (e: Exception) {
        if (e.isSessionExpired()) SessionEventBus.emitExpired()
        Resource.Error(e.message ?: "Unknown error")
    }
}
