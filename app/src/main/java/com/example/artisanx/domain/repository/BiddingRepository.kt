package com.example.artisanx.domain.repository

import com.example.artisanx.domain.model.Bid
import com.example.artisanx.util.Resource

interface BiddingRepository {
    suspend fun submitBid(
        jobId: String,
        artisanId: String,
        priceOffer: Double,
        message: String,
        estimatedHours: Double
    ): Resource<Bid>

    suspend fun getBidsForJob(jobId: String): Resource<List<Bid>>

    suspend fun getBidsByArtisan(artisanId: String): Resource<List<Bid>>

    suspend fun hasArtisanBid(jobId: String, artisanId: String): Boolean

    suspend fun updateBidStatus(bidId: String, status: String): Resource<Bid>

    suspend fun acceptBid(bidId: String, jobId: String, artisanId: String, customerId: String): Resource<Unit>
}
