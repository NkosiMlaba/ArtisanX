package com.example.artisanx.domain.model

data class Review(
    val id: String,
    val bookingId: String,
    val customerId: String,
    val artisanId: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
)

fun Map<String, Any>.toReview(id: String, createdAt: String): Review {
    val rating = when (val r = this["rating"]) {
        is Int -> r
        is Long -> r.toInt()
        is Double -> r.toInt()
        else -> 0
    }
    return Review(
        id = id,
        bookingId = this["bookingId"] as? String ?: "",
        customerId = this["customerId"] as? String ?: "",
        artisanId = this["artisanId"] as? String ?: "",
        rating = rating,
        comment = this["comment"] as? String ?: "",
        createdAt = createdAt
    )
}
