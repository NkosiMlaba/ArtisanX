package com.example.artisanx.data.repository

import com.example.artisanx.domain.model.ChatMessage
import com.example.artisanx.domain.model.toChatMessage
import com.example.artisanx.domain.repository.ChatRepository
import com.example.artisanx.util.Constants
import com.example.artisanx.util.Resource
import io.appwrite.ID
import io.appwrite.Permission
import io.appwrite.Query
import io.appwrite.Role
import io.appwrite.services.Databases
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val databases: Databases
) : ChatRepository {

    private fun getCurrentIso8601Date(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    override suspend fun sendMessage(
        bookingId: String,
        senderId: String,
        message: String
    ): Resource<ChatMessage> {
        return try {
            val document = databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_CHAT_MESSAGES,
                documentId = ID.unique(),
                data = mapOf(
                    "bookingId" to bookingId,
                    "senderId" to senderId,
                    "message" to message,
                    "imageFileId" to "",
                    "createdAt" to getCurrentIso8601Date()
                ),
                permissions = listOf(
                    Permission.read(Role.user(senderId)),
                    Permission.read(Role.users())
                )
            )
            val msg = document.data.toChatMessage(document.id, document.createdAt)
            Resource.Success(msg)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to send message")
        }
    }

    override suspend fun sendImageMessage(
        bookingId: String,
        senderId: String,
        imageFileId: String
    ): Resource<ChatMessage> {
        return try {
            val document = databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_CHAT_MESSAGES,
                documentId = ID.unique(),
                data = mapOf(
                    "bookingId" to bookingId,
                    "senderId" to senderId,
                    "message" to "",
                    "imageFileId" to imageFileId,
                    "createdAt" to getCurrentIso8601Date()
                ),
                permissions = listOf(
                    Permission.read(Role.user(senderId)),
                    Permission.read(Role.users())
                )
            )
            val msg = document.data.toChatMessage(document.id, document.createdAt)
            Resource.Success(msg)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to send image")
        }
    }

    override suspend fun getMessages(bookingId: String): Resource<List<ChatMessage>> {
        return try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_CHAT_MESSAGES,
                queries = listOf(
                    Query.equal("bookingId", bookingId),
                    Query.orderAsc("createdAt"),
                    Query.limit(100)
                )
            )
            val messages = response.documents.map { it.data.toChatMessage(it.id, it.createdAt) }
            Resource.Success(messages)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to load messages")
        }
    }
}
