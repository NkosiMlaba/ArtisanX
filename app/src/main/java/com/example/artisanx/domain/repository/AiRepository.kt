package com.example.artisanx.domain.repository

import com.example.artisanx.util.Resource

data class ArtisanMatch(
    val artisanId: String,
    val name: String,
    val trade: String,
    val rating: Double,
    val reviewCount: Int,
    val serviceArea: String,
    val badge: String,
    val explanation: String
)

interface AiRepository {
    /** Refine a rough job description into a professional one */
    suspend fun generateJobDescription(category: String, roughDescription: String): Resource<String>

    /** Suggest a price range and message template for an artisan bid */
    suspend fun getBidSuggestion(jobTitle: String, jobDescription: String, category: String, budget: Double, artisanSkills: String): Resource<BidSuggestion>

    /** Rank artisans best suited for a job */
    suspend fun matchArtisans(jobTitle: String, jobDescription: String, category: String, artisans: List<ArtisanSummary>): Resource<List<ArtisanMatch>>
}

data class BidSuggestion(
    val minPrice: Double,
    val maxPrice: Double,
    val messageTemplate: String
)

data class ArtisanSummary(
    val artisanId: String,
    val name: String,
    val trade: String,
    val skills: String,
    val serviceArea: String,
    val rating: Double,
    val reviewCount: Int,
    val badge: String
)
