package com.example.artisanx.data.repository

import com.example.artisanx.domain.model.Booking
import com.example.artisanx.domain.model.toBooking
import com.example.artisanx.domain.repository.BookingRepository
import com.example.artisanx.util.Constants
import com.example.artisanx.util.Resource
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.Databases
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class BookingRepositoryImpl @Inject constructor(
    private val databases: Databases
) : BookingRepository {

    private fun getCurrentIso8601Date(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    override suspend fun createBooking(
        jobId: String,
        customerId: String,
        artisanId: String
    ): Resource<Booking> {
        return try {
            val document = databases.createDocument(
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
            Resource.Success(document.data.toBooking(document.id, document.createdAt))
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to create booking")
        }
    }

    override suspend fun getBookingsForCustomer(customerId: String): Resource<List<Booking>> {
        return try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BOOKINGS,
                queries = listOf(
                    Query.equal("customerId", customerId),
                    Query.orderDesc("\$createdAt")
                )
            )
            val bookings = response.documents.map { it.data.toBooking(it.id, it.createdAt) }
            Resource.Success(bookings)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to load bookings")
        }
    }

    override suspend fun getBookingsForArtisan(artisanId: String): Resource<List<Booking>> {
        return try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BOOKINGS,
                queries = listOf(
                    Query.equal("artisanId", artisanId),
                    Query.orderDesc("\$createdAt")
                )
            )
            val bookings = response.documents.map { it.data.toBooking(it.id, it.createdAt) }
            Resource.Success(bookings)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to load bookings")
        }
    }

    override suspend fun getBookingById(bookingId: String): Resource<Booking> {
        return try {
            val document = databases.getDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BOOKINGS,
                documentId = bookingId
            )
            Resource.Success(document.data.toBooking(document.id, document.createdAt))
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to load booking")
        }
    }

    override suspend fun updateBookingStatus(bookingId: String, status: String): Resource<Booking> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to status)
            if (status == "in_progress") {
                updates["startedAt"] = getCurrentIso8601Date()
            }
            if (status == "completed") {
                updates["completedAt"] = getCurrentIso8601Date()
            }
            val document = databases.updateDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BOOKINGS,
                documentId = bookingId,
                data = updates
            )
            Resource.Success(document.data.toBooking(document.id, document.createdAt))
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to update booking status")
        }
    }

    override suspend fun markAsPaid(bookingId: String): Resource<Booking> {
        return try {
            val document = databases.updateDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_BOOKINGS,
                documentId = bookingId,
                data = mapOf("isPaid" to true)
            )
            Resource.Success(document.data.toBooking(document.id, document.createdAt))
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to mark as paid")
        }
    }
}
