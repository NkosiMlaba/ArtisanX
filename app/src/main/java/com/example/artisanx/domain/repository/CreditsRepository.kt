package com.example.artisanx.domain.repository

import com.example.artisanx.util.Resource

interface CreditsRepository {
    suspend fun getBalance(artisanId: String): Resource<Int>

    suspend fun initializeCredits(artisanId: String, initialBalance: Int = 5): Resource<Unit>

    suspend fun deductCredits(artisanId: String, amount: Int): Resource<Int>
}
