package com.example.artisanx.data.repository

import android.content.Context
import com.example.artisanx.ArtisansXFirebaseService
import com.example.artisanx.domain.model.Bid
import com.example.artisanx.domain.model.toBid
import com.example.artisanx.domain.repository.BiddingRepository
import com.example.artisanx.util.Constants
import com.example.artisanx.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.Databases
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class BiddingRepositoryImpl @Inject constructor(
    private val databases: Databases,
    @ApplicationContext private val context: Context
) : BiddingRepository {

    private fun getCurrentIso8601Date(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    override suspend fun submitBid(
        jobId: String,
        artisanId: String,
        priceOffer: Double,
        message: String,
        estimatedHours: Double
    ): Resource<Bid> {
        return try {
            val document = databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BIDS,
                documentId = ID.unique(),
                data = mapOf(
                    "jobId" to jobId,
                    "artisanId" to artisanId,
                    "priceOffer" to priceOffer,
                    "message" to message,
                    "estimatedHours" to estimatedHours,
                    "status" to "pending",
                    "createdAt" to getCurrentIso8601Date()
                )
            )
            Resource.Success(document.data.toBid(document.id, document.createdAt))
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to submit bid")
        }
    }

    override suspend fun getBidsForJob(jobId: String): Resource<List<Bid>> {
        return try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BIDS,
                queries = listOf(
                    Query.equal("jobId", jobId),
                    Query.orderAsc("priceOffer")
                )
            )
            val bids = response.documents.map { it.data.toBid(it.id, it.createdAt) }
            Resource.Success(bids)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to load bids")
        }
    }

    override suspend fun getBidsByArtisan(artisanId: String): Resource<List<Bid>> {
        return try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BIDS,
                queries = listOf(
                    Query.equal("artisanId", artisanId),
                    Query.orderDesc("\$createdAt")
                )
            )
            val bids = response.documents.map { it.data.toBid(it.id, it.createdAt) }
            Resource.Success(bids)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to load bids")
        }
    }

    override suspend fun hasArtisanBid(jobId: String, artisanId: String): Boolean {
        return try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BIDS,
                queries = listOf(
                    Query.equal("jobId", jobId),
                    Query.equal("artisanId", artisanId)
                )
            )
            response.documents.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateBidStatus(bidId: String, status: String): Resource<Bid> {
        return try {
            val document = databases.updateDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BIDS,
                documentId = bidId,
                data = mapOf("status" to status)
            )
            Resource.Success(document.data.toBid(document.id, document.createdAt))
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to update bid")
        }
    }

    override suspend fun acceptBid(
        bidId: String,
        jobId: String,
        artisanId: String,
        customerId: String
    ): Resource<Unit> {
        return try {
            // Accept this bid
            databases.updateDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BIDS,
                documentId = bidId,
                data = mapOf("status" to "accepted")
            )

            // Reject all other pending bids for this job
            val otherBids = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BIDS,
                queries = listOf(
                    Query.equal("jobId", jobId),
                    Query.equal("status", "pending")
                )
            )
            for (doc in otherBids.documents) {
                if (doc.id != bidId) {
                    databases.updateDocument(
                        databaseId = Constants.DATABASE_ID,
                        collectionId = Constants.COLLECTION_BIDS,
                        documentId = doc.id,
                        data = mapOf("status" to "rejected")
                    )
                }
            }

            // Update job status to assigned
            databases.updateDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_JOBS,
                documentId = jobId,
                data = mapOf(
                    "status" to "assigned",
                    "assignedArtisanId" to artisanId
                )
            )

            // Create a booking
            databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BOOKINGS,
                documentId = ID.unique(),
                data = mapOf(
                    "jobId" to jobId,
                    "customerId" to customerId,
                    "artisanId" to artisanId,
                    "status" to "requested",
                    "isPaid" to false,
                    "createdAt" to getCurrentIso8601Date()
                )
            )

            // Notify the artisan their bid was accepted
            ArtisansXFirebaseService.showLocalNotification(
                context,
                "Bid Accepted!",
                "Your bid was accepted. Check your bookings to get started."
            )

            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to accept bid")
        }
    }
}
