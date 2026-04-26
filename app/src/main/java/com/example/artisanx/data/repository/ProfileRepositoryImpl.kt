package com.example.artisanx.data.repository

import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.util.Constants
import com.example.artisanx.util.Resource
import io.appwrite.ID
import io.appwrite.Permission
import io.appwrite.Query
import io.appwrite.Role
import io.appwrite.models.Document
import io.appwrite.services.Databases
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(private val databases: Databases) :
        ProfileRepository {

    private fun getCurrentIso8601Date(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    override suspend fun createUserProfile(
            userId: String,
            fullName: String,
            email: String,
            role: String
    ): Resource<Document<Map<String, Any>>> {
        return try {
            val document =
                    databases.createDocument(
                            databaseId = Constants.DATABASE_ID,
                            collectionId = Constants.COLLECTION_USER_PROFILES,
                            documentId = ID.unique(),
                            data =
                                    mapOf(
                                            "userId" to userId,
                                            "fullName" to fullName,
                                            "email" to email,
                                            "phone" to "",
                                            "addresses" to listOf<String>(),
                                            "profileImageId" to "",
                                            "role" to role,
                                            "createdAt" to getCurrentIso8601Date()
                                    ),
                            permissions = listOf(
                                    Permission.read(Role.users()),
                                    Permission.update(Role.user(userId)),
                                    Permission.delete(Role.user(userId))
                            )
                    )
            Resource.Success(document)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to create user profile")
        }
    }

    override suspend fun getUserProfile(userId: String): Resource<Document<Map<String, Any>>> {
        return try {
            val response =
                    databases.listDocuments(
                            databaseId = Constants.DATABASE_ID,
                            collectionId = Constants.COLLECTION_USER_PROFILES,
                            queries = listOf(Query.equal("userId", userId))
                    )
            val document = response.documents.firstOrNull()
            if (document != null) {
                Resource.Success(document)
            } else {
                Resource.Error("Profile not found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to get user profile")
        }
    }

    override suspend fun createArtisanProfile(
            userId: String,
            fullName: String,
            email: String,
            phone: String,
            tradeCategory: String,
            skills: String,
            serviceArea: String,
            isStudent: Boolean
    ): Resource<Document<Map<String, Any>>> {
        return try {
            val document =
                    databases.createDocument(
                            databaseId = Constants.DATABASE_ID,
                            collectionId = Constants.COLLECTION_ARTISAN_PROFILES,
                            documentId = ID.unique(),
                            data =
                                    mapOf(
                                            "userId" to userId,
                                            "fullName" to fullName,
                                            "email" to email,
                                            "phone" to phone,
                                            "isStudent" to isStudent,
                                            "institutionName" to "",
                                            "studentNumber" to "",
                                            "studentCardFileId" to "",
                                            "courseField" to "",
                                            "gradYear" to 0,
                                            "idFileId" to "",
                                            "tradeCategory" to tradeCategory,
                                            "skills" to skills,
                                            "serviceArea" to serviceArea,
                                            "serviceRadiusKm" to 10.0,
                                            "latitude" to 0.0,
                                            "longitude" to 0.0,
                                            "workPhotoIds" to listOf<String>(),
                                            "certifications" to "",
                                            "yearsExperience" to 0,
                                            "verified" to false,
                                            "badge" to
                                                    (if (isStudent) "Student Artisan"
                                                    else "Verified Artisan"),
                                            "avgRating" to 0.0,
                                            "reviewCount" to 0,
                                            "createdAt" to getCurrentIso8601Date()
                                    ),
                            permissions = listOf(
                                    Permission.read(Role.users()),
                                    Permission.update(Role.user(userId)),
                                    Permission.delete(Role.user(userId))
                            )
                    )
            Resource.Success(document)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to create artisan profile")
        }
    }

    override suspend fun getArtisanProfile(userId: String): Resource<Document<Map<String, Any>>> {
        return try {
            val response =
                    databases.listDocuments(
                            databaseId = Constants.DATABASE_ID,
                            collectionId = Constants.COLLECTION_ARTISAN_PROFILES,
                            queries = listOf(Query.equal("userId", userId))
                    )
            val document = response.documents.firstOrNull()
            if (document != null) {
                Resource.Success(document)
            } else {
                Resource.Error("Artisan profile not found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to get artisan profile")
        }
    }

    override suspend fun updateUserProfile(
            userId: String,
            updates: Map<String, Any>
    ): Resource<Document<Map<String, Any>>> {
        return try {
            // First get the document ID
            val profileRes = getUserProfile(userId)
            if (profileRes is Resource.Success) {
                val docId = profileRes.data?.id ?: return Resource.Error("Profile ID not found")
                val document =
                        databases.updateDocument(
                                databaseId = Constants.DATABASE_ID,
                                collectionId = Constants.COLLECTION_USER_PROFILES,
                                documentId = docId,
                                data = updates
                        )
                Resource.Success(document)
            } else {
                Resource.Error("User profile not found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }

    override suspend fun updateArtisanProfile(
            userId: String,
            updates: Map<String, Any>
    ): Resource<Document<Map<String, Any>>> {
        return try {
            // First get the document ID
            val profileRes = getArtisanProfile(userId)
            if (profileRes is Resource.Success) {
                val docId = profileRes.data?.id ?: return Resource.Error("Profile ID not found")
                val document =
                        databases.updateDocument(
                                databaseId = Constants.DATABASE_ID,
                                collectionId = Constants.COLLECTION_ARTISAN_PROFILES,
                                documentId = docId,
                                data = updates
                        )
                Resource.Success(document)
            } else {
                Resource.Error("Artisan profile not found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to update artisan profile")
        }
    }
}
