package com.example.artisanx.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// OpenAI-compatible request/response — works for both Groq and OpenRouter
data class AiMessage(val role: String, val content: String)

data class AiRequest(
    val model: String,
    val messages: List<AiMessage>,
    val max_tokens: Int = 1024,
    val temperature: Double = 0.7
)

data class AiChoice(val message: AiMessage)
data class AiResponse(val choices: List<AiChoice>)

interface GroqApiService {
    @POST("openai/v1/chat/completions")
    suspend fun complete(
        @Header("Authorization") authorization: String,
        @Body request: AiRequest
    ): AiResponse
}

interface OpenRouterApiService {
    @POST("api/v1/chat/completions")
    suspend fun complete(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://artisanx.dev",
        @Header("X-Title") title: String = "ArtisansX",
        @Body request: AiRequest
    ): AiResponse
}
