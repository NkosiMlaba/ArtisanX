package com.example.artisanx.domain.model

data class ChatMessage(
    val id: String,
    val bookingId: String,
    val senderId: String,
    val message: String,
    val imageFileId: String,
    val createdAt: String
)

fun Map<String, Any>.toChatMessage(id: String, createdAt: String): ChatMessage {
    return ChatMessage(
        id = id,
        bookingId = this["bookingId"] as? String ?: "",
        senderId = this["senderId"] as? String ?: "",
        message = this["message"] as? String ?: "",
        imageFileId = this["imageFileId"] as? String ?: "",
        createdAt = createdAt
    )
}
