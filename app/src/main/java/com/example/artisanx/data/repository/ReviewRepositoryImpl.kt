package com.example.artisanx.data.repository

import com.example.artisanx.domain.model.Review
import com.example.artisanx.domain.model.toReview
import com.example.artisanx.domain.repository.RatingStats
import com.example.artisanx.domain.repository.ReviewRepository
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
import com.example.artisanx.util.isSessionExpired

class ReviewRepositoryImpl @Inject constructor(
    private val databases: Databases
) : ReviewRepository {

    private fun getCurrentIso8601Date(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    override suspend fun submitReview(
        bookingId: String,
        customerId: String,
        artisanId: String,
        rating: Int,
        comment: String
    ): Resource<Review> {
        return try {
            val document = databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_REVIEWS,
                documentId = ID.unique(),
                data = mapOf(
                    "bookingId" to bookingId,
                    "customerId" to customerId,
                    "artisanId" to artisanId,
                    "rating" to rating,
                    "comment" to comment,
                    "createdAt" to getCurrentIso8601Date()
                ),
                permissions = listOf(
                    Permission.read(Role.users()),
                    Permission.update(Role.user(customerId))
                )
            )

            Resource.Success(document.data.toReview(document.id, document.createdAt))
        } catch (e: Exception) {
            if (e.isSessionExpired()) com.example.artisanx.util.SessionEventBus.emitExpired()
            Resource.Error(e.message ?: "Failed to submit review")
        }
    }

    override suspend fun getArtisanRatingStats(artisanId: String): Resource<RatingStats> {
        return try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_REVIEWS,
                queries = listOf(
                    Query.equal("artisanId", artisanId),
                    Query.limit(100)
                )
            )
            val ratings = response.documents.mapNotNull {
                when (val r = it.data["rating"]) {
                    is Int -> r
                    is Long -> r.toInt()
                    is Double -> r.toInt()
                    else -> null
                }
            }
            val stats = if (ratings.isEmpty()) RatingStats.EMPTY
                        else RatingStats(avg = ratings.sum().toDouble() / ratings.size, count = ratings.size)
            Resource.Success(stats)
        } catch (e: Exception) {
            if (e.isSessionExpired()) com.example.artisanx.util.SessionEventBus.emitExpired()
            Resource.Error(e.message ?: "Failed to load rating stats")
        }
    }

    override suspend fun getReviewForBooking(bookingId: String): Resource<Review?> {
        return try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_REVIEWS,
                queries = listOf(Query.equal("bookingId", bookingId))
            )
            val review = response.documents.firstOrNull()?.let {
                it.data.toReview(it.id, it.createdAt)
            }
            Resource.Success(review)
        } catch (e: Exception) {
            if (e.isSessionExpired()) com.example.artisanx.util.SessionEventBus.emitExpired()
            Resource.Error(e.message ?: "Failed to load review")
        }
    }

    override suspend fun getReviewsForArtisan(artisanId: String): Resource<List<Review>> {
        return try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_REVIEWS,
                queries = listOf(
                    Query.equal("artisanId", artisanId),
                    Query.orderDesc("createdAt")
                )
            )
            val reviews = response.documents.map { it.data.toReview(it.id, it.createdAt) }
            Resource.Success(reviews)
        } catch (e: Exception) {
            if (e.isSessionExpired()) com.example.artisanx.util.SessionEventBus.emitExpired()
            Resource.Error(e.message ?: "Failed to load reviews")
        }
    }

    override suspend fun getReviewsByCustomer(customerId: String): Resource<List<Review>> {
        return try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_REVIEWS,
                queries = listOf(
                    Query.equal("customerId", customerId),
                    Query.orderDesc("createdAt")
                )
            )
            val reviews = response.documents.map { it.data.toReview(it.id, it.createdAt) }
            Resource.Success(reviews)
        } catch (e: Exception) {
            if (e.isSessionExpired()) com.example.artisanx.util.SessionEventBus.emitExpired()
            Resource.Error(e.message ?: "Failed to load reviews")
        }
    }

    override suspend fun getReviewById(reviewId: String): Resource<Review> {
        return try {
            val doc = databases.getDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_REVIEWS,
                documentId = reviewId
            )
            Resource.Success(doc.data.toReview(doc.id, doc.createdAt))
        } catch (e: Exception) {
            if (e.isSessionExpired()) com.example.artisanx.util.SessionEventBus.emitExpired()
            Resource.Error(e.message ?: "Failed to load review")
        }
    }
}
