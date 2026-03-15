package com.example.artisanx.data.repository

import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.util.Resource
import io.appwrite.ID
import io.appwrite.models.Session
import io.appwrite.models.User
import io.appwrite.services.Account
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val account: Account
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
        name: String
    ): Resource<User<Map<String, Any>>> {
        return try {
            val user = account.create(
                userId = ID.unique(),
                email = email,
                password = password,
                name = name
            )
            Resource.Success(user)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "An unknown error occurred during registration.")
        }
    }

    override suspend fun login(email: String, password: String): Resource<Session> {
        return try {
            val session = account.createEmailPasswordSession(email, password)
            Resource.Success(session)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "An unknown error occurred during login.")
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            account.deleteSession("current")
            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "An unknown error occurred during logout.")
        }
    }

    override suspend fun getCurrentUser(): Resource<User<Map<String, Any>>> {
        return try {
            val user = account.get()
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Could not get current user.")
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return try {
            account.getSession("current")
            true
        } catch (e: Exception) {
            false
        }
    }
}
