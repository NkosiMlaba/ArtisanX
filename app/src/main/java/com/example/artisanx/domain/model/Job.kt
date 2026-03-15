package com.example.artisanx.domain.model

data class Job(
    val id: String,
    val customerId: String,
    val title: String,
    val description: String,
    val location: List<Double>, // [latitude, longitude]
    val category: String,
    val budget: Double,
    val status: String,
    val createdAt: String
)

fun Map<String, Any>.toJob(id: String, createdAt: String): Job {
    val rawLocation = this["location"] as? List<*> ?: emptyList<Any>()
    val locationList = rawLocation.mapNotNull {
        when (it) {
            is Double -> it
            is Int -> it.toDouble()
            is Float -> it.toDouble()
            else -> 0.0
        }
    }
    
    val budgetValue = when (val b = this["budget"]) {
        is Double -> b
        is Int -> b.toDouble()
        is Float -> b.toDouble()
        else -> 0.0
    }

    return Job(
        id = id,
        customerId = this["customer_id"] as? String ?: "",
        title = this["title"] as? String ?: "",
        description = this["description"] as? String ?: "",
        location = locationList,
        category = this["category"] as? String ?: "",
        budget = budgetValue,
        status = this["status"] as? String ?: "open",
        createdAt = createdAt
    )
}
