package com.example.artisanx.domain.repository

import com.example.artisanx.domain.model.Review
import com.example.artisanx.util.Resource

data class RatingStats(val avg: Double, val count: Int) {
    companion object { val EMPTY = RatingStats(0.0, 0) }
}

interface ReviewRepository {
    suspend fun submitReview(bookingId: String, customerId: String, artisanId: String, rating: Int, comment: String): Resource<Review>
    suspend fun getReviewForBooking(bookingId: String): Resource<Review?>
    suspend fun getReviewsForArtisan(artisanId: String): Resource<List<Review>>
    suspend fun getReviewsByCustomer(customerId: String): Resource<List<Review>>
    suspend fun getReviewById(reviewId: String): Resource<Review>
    suspend fun getArtisanRatingStats(artisanId: String): Resource<RatingStats>
}
