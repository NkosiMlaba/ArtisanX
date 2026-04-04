package com.example.artisanx.data.repository

import com.example.artisanx.domain.model.Job
import com.example.artisanx.domain.model.toJob
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.util.Constants
import com.example.artisanx.util.Resource
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.Databases
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val databases: Databases
) : JobRepository {

    private fun getCurrentIso8601Date(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    override suspend fun createJob(
        customerId: String,
        title: String,
        description: String,
        category: String,
        address: String,
        budget: Double,
        urgency: String,
        jobType: String
    ): Resource<Job> {
        return try {
            val document = databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_JOBS,
                documentId = ID.unique(),
                data = mapOf(
                    "customerId" to customerId,
                    "title" to title,
                    "category" to category,
                    "description" to description,
                    "photoIds" to listOf<String>(),
                    "latitude" to 0.0,
                    "longitude" to 0.0,
                    "address" to address,
                    "budget" to budget,
                    "urgency" to urgency,
                    "jobType" to jobType,
                    "status" to "open",
                    "assignedArtisanId" to "",
                    "createdAt" to getCurrentIso8601Date()
                )
            )
            Resource.Success(document.data.toJob(document.id, document.createdAt))
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to create job")
        }
    }

    override suspend fun updateJob(jobId: String, updates: Map<String, Any>): Resource<Job> {
        return try {
            val document = databases.updateDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_JOBS,
                documentId = jobId,
                data = updates
            )
            Resource.Success(document.data.toJob(document.id, document.createdAt))
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to update job")
        }
    }

    override suspend fun deleteJob(jobId: String): Resource<Unit> {
        return try {
            databases.deleteDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_JOBS,
                documentId = jobId
            )
            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to delete job")
        }
    }

    override suspend fun getJobById(jobId: String): Resource<Job> {
        return try {
            val document = databases.getDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_JOBS,
                documentId = jobId
            )
            Resource.Success(document.data.toJob(document.id, document.createdAt))
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to get job")
        }
    }

    override suspend fun getJobsByCustomer(customerId: String): Resource<List<Job>> {
        return try {
            val queries = listOf(
                Query.equal("customerId", customerId),
                Query.orderDesc("\$createdAt")
            )
            val documentList = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_JOBS,
                queries = queries
            )
            val jobs = documentList.documents.map { it.data.toJob(it.id, it.createdAt) }
            Resource.Success(jobs)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to list customer jobs")
        }
    }

    override suspend fun getOpenJobs(categoryFilter: String?): Resource<List<Job>> {
        return try {
            val queries = mutableListOf(
                Query.equal("status", "open"),
                Query.orderDesc("\$createdAt")
            )
            if (categoryFilter != null && categoryFilter.isNotEmpty()) {
                queries.add(Query.equal("category", categoryFilter))
            }

            val documentList = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_JOBS,
                queries = queries
            )
            val jobs = documentList.documents.map { it.data.toJob(it.id, it.createdAt) }
            Resource.Success(jobs)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to list open jobs")
        }
    }
}
