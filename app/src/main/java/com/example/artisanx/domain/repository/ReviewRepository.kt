package com.example.artisanx.domain.repository

import com.example.artisanx.domain.model.Review
import com.example.artisanx.util.Resource

interface ReviewRepository {
    suspend fun submitReview(bookingId: String, customerId: String, artisanId: String, rating: Int, comment: String): Resource<Review>
    suspend fun getReviewForBooking(bookingId: String): Resource<Review?>
    suspend fun getReviewsForArtisan(artisanId: String): Resource<List<Review>>
}
