package com.example.artisanx.domain.model

data class Bid(
    val id: String,
    val jobId: String,
    val artisanId: String,
    val priceOffer: Double,
    val message: String,
    val estimatedHours: Double,
    val status: String,
    val createdAt: String
)

fun Map<String, Any>.toBid(id: String, createdAt: String): Bid {
    val price = when (val p = this["priceOffer"]) {
        is Double -> p
        is Int -> p.toDouble()
        is Float -> p.toDouble()
        is Long -> p.toDouble()
        else -> 0.0
    }
    val hours = when (val h = this["estimatedHours"]) {
        is Double -> h
        is Int -> h.toDouble()
        is Float -> h.toDouble()
        is Long -> h.toDouble()
        else -> 0.0
    }
    return Bid(
        id = id,
        jobId = this["jobId"] as? String ?: "",
        artisanId = this["artisanId"] as? String ?: "",
        priceOffer = price,
        message = this["message"] as? String ?: "",
        estimatedHours = hours,
        status = this["status"] as? String ?: "pending",
        createdAt = createdAt
    )
}
