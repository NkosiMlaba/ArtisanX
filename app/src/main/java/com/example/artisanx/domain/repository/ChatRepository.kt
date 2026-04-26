package com.example.artisanx.domain.repository

import com.example.artisanx.domain.model.ChatMessage
import com.example.artisanx.util.Resource

interface ChatRepository {
    suspend fun sendMessage(bookingId: String, senderId: String, message: String): Resource<ChatMessage>
    suspend fun getMessages(bookingId: String): Resource<List<ChatMessage>>
}
