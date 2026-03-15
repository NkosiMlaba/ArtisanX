package com.example.artisanx.domain.repository

import io.appwrite.models.User
import io.appwrite.models.Session
import com.example.artisanx.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(email: String, password: String, name: String): Resource<User<Map<String, Any>>>
    suspend fun login(email: String, password: String): Resource<Session>
    suspend fun logout(): Resource<Unit>
    suspend fun getCurrentUser(): Resource<User<Map<String, Any>>>
    suspend fun isLoggedIn(): Boolean
}
