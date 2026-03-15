package com.example.artisanx.domain.repository

import com.example.artisanx.domain.model.Job
import com.example.artisanx.util.Resource
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    suspend fun createJob(
        customerId: String,
        title: String,
        description: String,
        location: List<Double>,
        category: String,
        budget: Double
    ): Resource<Job>

    suspend fun updateJob(
        jobId: String,
        updates: Map<String, Any>
    ): Resource<Job>

    suspend fun deleteJob(jobId: String): Resource<Unit>

    suspend fun getJobById(jobId: String): Resource<Job>

    suspend fun getJobsByCustomer(customerId: String): Resource<List<Job>>

    suspend fun getOpenJobs(categoryFilter: String? = null): Resource<List<Job>>
}
