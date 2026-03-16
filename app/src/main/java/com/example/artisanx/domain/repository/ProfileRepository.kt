package com.example.artisanx.domain.repository

import com.example.artisanx.util.Resource
import io.appwrite.models.Document

interface ProfileRepository {
    suspend fun createUserProfile(
        userId: String,
        fullName: String,
        email: String,
        role: String
    ): Resource<Document<Map<String, Any>>>

    suspend fun getUserProfile(userId: String): Resource<Document<Map<String, Any>>>

    suspend fun createArtisanProfile(
        userId: String,
        fullName: String,
        email: String,
        phone: String,
        tradeCategory: String,
        skills: String,
        serviceArea: String,
        isStudent: Boolean
    ): Resource<Document<Map<String, Any>>>

    suspend fun getArtisanProfile(userId: String): Resource<Document<Map<String, Any>>>
    
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Resource<Document<Map<String, Any>>>
    
    suspend fun updateArtisanProfile(userId: String, updates: Map<String, Any>): Resource<Document<Map<String, Any>>>
}
