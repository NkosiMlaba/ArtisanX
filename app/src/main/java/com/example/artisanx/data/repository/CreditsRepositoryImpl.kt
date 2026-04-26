package com.example.artisanx.data.repository

import com.example.artisanx.domain.repository.CreditsRepository
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

class CreditsRepositoryImpl @Inject constructor(
    private val databases: Databases
) : CreditsRepository {

    private fun getCurrentIso8601Date(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private suspend fun getCreditDocument(artisanId: String): io.appwrite.models.Document<Map<String, Any>>? {
        val response = databases.listDocuments(
            databaseId = Constants.DATABASE_ID,
            collectionId = Constants.COLLECTION_CREDITS,
            queries = listOf(Query.equal("artisanId", artisanId))
        )
        return response.documents.firstOrNull()
    }

    override suspend fun getBalance(artisanId: String): Resource<Int> {
        return try {
            val doc = getCreditDocument(artisanId)
            if (doc != null) {
                val balance = when (val b = doc.data["balance"]) {
                    is Int -> b
                    is Long -> b.toInt()
                    is Double -> b.toInt()
                    else -> 0
                }
                Resource.Success(balance)
            } else {
                // Auto-initialize if no credits record exists
                initializeCredits(artisanId)
                Resource.Success(5)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to get credit balance")
        }
    }

    override suspend fun initializeCredits(artisanId: String, initialBalance: Int): Resource<Unit> {
        return try {
            val existing = getCreditDocument(artisanId)
            if (existing == null) {
                databases.createDocument(
                    databaseId = Constants.DATABASE_ID,
                    collectionId = Constants.COLLECTION_CREDITS,
                    documentId = ID.unique(),
                    data = mapOf(
                        "artisanId" to artisanId,
                        "balance" to initialBalance,
                        "lastUpdated" to getCurrentIso8601Date()
                    ),
                    permissions = listOf(
                        Permission.read(Role.user(artisanId)),
                        Permission.update(Role.user(artisanId))
                    )
                )
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to initialize credits")
        }
    }

    override suspend fun deductCredits(artisanId: String, amount: Int): Resource<Int> {
        return try {
            val doc = getCreditDocument(artisanId)
                ?: return Resource.Error("No credits record found")

            val currentBalance = when (val b = doc.data["balance"]) {
                is Int -> b
                is Long -> b.toInt()
                is Double -> b.toInt()
                else -> 0
            }

            if (currentBalance < amount) {
                return Resource.Error("Insufficient credits")
            }

            val newBalance = currentBalance - amount
            databases.updateDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_CREDITS,
                documentId = doc.id,
                data = mapOf(
                    "balance" to newBalance,
                    "lastUpdated" to getCurrentIso8601Date()
                )
            )
            Resource.Success(newBalance)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to deduct credits")
        }
    }
}
