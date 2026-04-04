package com.example.artisanx.domain.model

data class Job(
    val id: String,
    val customerId: String,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val budget: Double,
    val urgency: String,
    val jobType: String,
    val status: String,
    val assignedArtisanId: String,
    val createdAt: String
)

private fun Any?.toDoubleOrDefault(default: Double = 0.0): Double {
    return when (this) {
        is Double -> this
        is Int -> this.toDouble()
        is Float -> this.toDouble()
        is Long -> this.toDouble()
        else -> default
    }
}

fun Map<String, Any>.toJob(id: String, createdAt: String): Job {
    return Job(
        id = id,
        customerId = this["customerId"] as? String ?: "",
        title = this["title"] as? String ?: "",
        description = this["description"] as? String ?: "",
        category = this["category"] as? String ?: "",
        latitude = this["latitude"].toDoubleOrDefault(),
        longitude = this["longitude"].toDoubleOrDefault(),
        address = this["address"] as? String ?: "",
        budget = this["budget"].toDoubleOrDefault(),
        urgency = this["urgency"] as? String ?: "standard",
        jobType = this["jobType"] as? String ?: "standard",
        status = this["status"] as? String ?: "open",
        assignedArtisanId = this["assignedArtisanId"] as? String ?: "",
        createdAt = createdAt
    )
}
