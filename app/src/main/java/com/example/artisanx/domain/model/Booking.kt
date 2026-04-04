package com.example.artisanx.domain.model

data class Booking(
    val id: String,
    val jobId: String,
    val customerId: String,
    val artisanId: String,
    val status: String,
    val isPaid: Boolean,
    val startedAt: String,
    val completedAt: String,
    val createdAt: String
)

fun Map<String, Any>.toBooking(id: String, createdAt: String): Booking {
    return Booking(
        id = id,
        jobId = this["jobId"] as? String ?: "",
        customerId = this["customerId"] as? String ?: "",
        artisanId = this["artisanId"] as? String ?: "",
        status = this["status"] as? String ?: "requested",
        isPaid = this["isPaid"] as? Boolean ?: false,
        startedAt = this["startedAt"] as? String ?: "",
        completedAt = this["completedAt"] as? String ?: "",
        createdAt = createdAt
    )
}
