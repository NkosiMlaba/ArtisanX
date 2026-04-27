package com.example.artisanx.data.repository

import com.example.artisanx.BuildConfig
import com.example.artisanx.data.remote.AiMessage
import com.example.artisanx.data.remote.AiRequest
import com.example.artisanx.data.remote.GroqApiService
import com.example.artisanx.data.remote.OpenRouterApiService
import com.example.artisanx.domain.repository.AiRepository
import com.example.artisanx.domain.repository.ArtisanMatch
import com.example.artisanx.domain.repository.ArtisanSummary
import com.example.artisanx.domain.repository.BidSuggestion
import com.example.artisanx.util.Constants
import com.example.artisanx.util.Resource
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.appwrite.services.Functions
import javax.inject.Inject
import com.example.artisanx.util.isSessionExpired

class AiRepositoryImpl @Inject constructor(
    private val groqService: GroqApiService,
    private val openRouterService: OpenRouterApiService,
    private val functions: Functions
) : AiRepository {

    private val gson = Gson()

    private suspend fun callProxy(params: Map<String, Any>): String? {
        return try {
            val encoded = java.net.URLEncoder.encode(gson.toJson(params), "UTF-8")
            val execution = functions.createExecution(
                functionId = Constants.FUNCTION_AI_PROXY,
                body = "",
                async = false,
                path = "/?d=$encoded",
                method = io.appwrite.enums.ExecutionMethod.GET
            )
            execution.responseBody.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun generateJobDescription(
        category: String,
        roughDescription: String
    ): Resource<String> {
        return try {
            // Try Appwrite Function proxy first (keeps API keys server-side)
            val proxyResult = callProxy(mapOf(
                "action" to "generateJobDescription",
                "category" to category,
                "roughDescription" to roughDescription
            ))
            if (proxyResult != null) {
                val obj = gson.fromJson(proxyResult, JsonObject::class.java)
                val text = obj.get("result")?.asString
                if (!text.isNullOrBlank()) return Resource.Success(text)
            }

            // Fallback: direct Groq call
            val systemPrompt = """You are a helpful assistant for ArtisansX, a South African artisan services marketplace.
A customer needs help writing a clear job description. Reply with ONLY the improved description — no preamble, no labels.
Write 3-5 clear sentences. Include what needs to be done and relevant details a skilled artisan would need.
Keep it in simple English accessible to South African users. Do not include pricing."""

            val userPrompt = "Category: $category\nRough description: \"$roughDescription\"\n\nWrite the improved job description:"

            val response = groqService.complete(
                authorization = "Bearer ${BuildConfig.GROQ_API_KEY}",
                request = AiRequest(
                    model = "llama-3.3-70b-versatile",
                    messages = listOf(
                        AiMessage("system", systemPrompt),
                        AiMessage("user", userPrompt)
                    ),
                    max_tokens = 300
                )
            )
            val text = response.choices.firstOrNull()?.message?.content?.trim()
                ?: return Resource.Error("No response from AI")
            Resource.Success(text)
        } catch (e: Exception) {
            if (e.isSessionExpired()) com.example.artisanx.util.SessionEventBus.emitExpired()
            e.printStackTrace()
            Resource.Error("AI assistant unavailable right now. You can describe the job manually.")
        }
    }

    override suspend fun getBidSuggestion(
        jobTitle: String,
        jobDescription: String,
        category: String,
        budget: Double,
        artisanSkills: String
    ): Resource<BidSuggestion> {
        return try {
            // Try Appwrite Function proxy first
            val proxyResult = callProxy(mapOf(
                "action" to "getBidSuggestion",
                "jobTitle" to jobTitle,
                "jobDescription" to jobDescription,
                "category" to category,
                "budget" to budget,
                "artisanSkills" to artisanSkills
            ))
            if (proxyResult != null) {
                val outer = gson.fromJson(proxyResult, JsonObject::class.java)
                val raw = outer.get("result")?.asString
                if (!raw.isNullOrBlank()) {
                    val jsonStart = raw.indexOf('{')
                    val jsonEnd = raw.lastIndexOf('}') + 1
                    if (jsonStart != -1 && jsonEnd > jsonStart) {
                        val json = gson.fromJson(raw.substring(jsonStart, jsonEnd), JsonObject::class.java)
                        return Resource.Success(
                            BidSuggestion(
                                minPrice = json.get("minPrice")?.asDouble ?: 0.0,
                                maxPrice = json.get("maxPrice")?.asDouble ?: 0.0,
                                messageTemplate = json.get("messageTemplate")?.asString ?: ""
                            )
                        )
                    }
                }
            }

            // Fallback: direct OpenRouter call
            val budgetText = if (budget > 0) "Customer budget: R$budget" else "No budget specified"
            val systemPrompt = """You are a pricing assistant for ArtisansX, a South African artisan marketplace.
Help artisans price their services fairly and write professional messages.
Respond in this exact JSON format:
{"minPrice": 350, "maxPrice": 500, "messageTemplate": "Your professional message template here"}
Use South African Rand (ZAR). Keep the message template under 150 words, professional and friendly."""

            val userPrompt = """Job: $jobTitle
Category: $category
Description: $jobDescription
$budgetText
Artisan skills: $artisanSkills

Suggest a fair price range and message template:"""

            val response = openRouterService.complete(
                authorization = "Bearer ${BuildConfig.OPENROUTER_API_KEY}",
                request = AiRequest(
                    model = "anthropic/claude-sonnet-4-5",
                    messages = listOf(
                        AiMessage("system", systemPrompt),
                        AiMessage("user", userPrompt)
                    ),
                    max_tokens = 400
                )
            )

            val raw = response.choices.firstOrNull()?.message?.content?.trim()
                ?: return Resource.Error("No response from AI")

            val jsonStart = raw.indexOf('{')
            val jsonEnd = raw.lastIndexOf('}') + 1
            if (jsonStart == -1 || jsonEnd <= jsonStart) return Resource.Error("Could not parse AI response")

            val json = gson.fromJson(raw.substring(jsonStart, jsonEnd), JsonObject::class.java)
            Resource.Success(
                BidSuggestion(
                    minPrice = json.get("minPrice")?.asDouble ?: 0.0,
                    maxPrice = json.get("maxPrice")?.asDouble ?: 0.0,
                    messageTemplate = json.get("messageTemplate")?.asString ?: ""
                )
            )
        } catch (e: Exception) {
            if (e.isSessionExpired()) com.example.artisanx.util.SessionEventBus.emitExpired()
            e.printStackTrace()
            Resource.Error("AI assistant unavailable. Fill in your bid manually.")
        }
    }

    override suspend fun matchArtisans(
        jobTitle: String,
        jobDescription: String,
        category: String,
        artisans: List<ArtisanSummary>
    ): Resource<List<ArtisanMatch>> {
        if (artisans.isEmpty()) return Resource.Success(emptyList())

        return try {
            val artisanList = artisans.joinToString("\n") { a ->
                "- ID:${a.artisanId} | ${a.name} | ${a.trade} | Skills: ${a.skills} | Area: ${a.serviceArea} | Rating: ${a.rating}/5 (${a.reviewCount} reviews) | ${a.badge}"
            }

            // Try Appwrite Function proxy first
            val proxyResult = callProxy(mapOf(
                "action" to "matchArtisans",
                "jobTitle" to jobTitle,
                "jobDescription" to jobDescription,
                "category" to category,
                "artisanList" to artisanList
            ))
            if (proxyResult != null) {
                val outer = gson.fromJson(proxyResult, JsonObject::class.java)
                val raw = outer.get("result")?.asString
                if (!raw.isNullOrBlank()) {
                    val jStart = raw.indexOf('[')
                    val jEnd = raw.lastIndexOf(']') + 1
                    if (jStart != -1 && jEnd > jStart) {
                        val jsonArray = gson.fromJson(raw.substring(jStart, jEnd), com.google.gson.JsonArray::class.java)
                        val artisanMap = artisans.associateBy { it.artisanId }
                        val matches = jsonArray.mapNotNull { element ->
                            val obj = element.asJsonObject
                            val id = obj.get("artisanId")?.asString ?: return@mapNotNull null
                            val summary = artisanMap[id] ?: return@mapNotNull null
                            ArtisanMatch(artisanId = id, name = summary.name, trade = summary.trade,
                                rating = summary.rating, reviewCount = summary.reviewCount,
                                serviceArea = summary.serviceArea, badge = summary.badge,
                                explanation = obj.get("explanation")?.asString ?: "")
                        }
                        return Resource.Success(matches)
                    }
                }
            }

            // Fallback: direct OpenRouter call
            val systemPrompt = """You are a matching assistant for ArtisansX, a South African artisan marketplace.
Rank artisans best suited for the given job. Consider: skills match, rating, experience.
Respond with ONLY a JSON array in this exact format:
[{"artisanId":"id","explanation":"One sentence why they're a good match"}]
Return up to 3 artisans ranked best first. If none match well, return an empty array []."""

            val userPrompt = """Job: $jobTitle
Category: $category
Description: $jobDescription

Available artisans:
$artisanList

Return the ranked JSON array:"""

            val response = openRouterService.complete(
                authorization = "Bearer ${BuildConfig.OPENROUTER_API_KEY}",
                request = AiRequest(
                    model = "anthropic/claude-sonnet-4-5",
                    messages = listOf(
                        AiMessage("system", systemPrompt),
                        AiMessage("user", userPrompt)
                    ),
                    max_tokens = 400
                )
            )

            val raw = response.choices.firstOrNull()?.message?.content?.trim()
                ?: return Resource.Error("No response from AI")

            val jsonStart = raw.indexOf('[')
            val jsonEnd = raw.lastIndexOf(']') + 1
            if (jsonStart == -1 || jsonEnd <= jsonStart) return Resource.Success(emptyList())

            val jsonArray = gson.fromJson(raw.substring(jsonStart, jsonEnd), com.google.gson.JsonArray::class.java)
            val artisanMap = artisans.associateBy { it.artisanId }

            val matches = jsonArray.mapNotNull { element ->
                val obj = element.asJsonObject
                val id = obj.get("artisanId")?.asString ?: return@mapNotNull null
                val summary = artisanMap[id] ?: return@mapNotNull null
                ArtisanMatch(
                    artisanId = id,
                    name = summary.name,
                    trade = summary.trade,
                    rating = summary.rating,
                    reviewCount = summary.reviewCount,
                    serviceArea = summary.serviceArea,
                    badge = summary.badge,
                    explanation = obj.get("explanation")?.asString ?: ""
                )
            }
            Resource.Success(matches)
        } catch (e: Exception) {
            if (e.isSessionExpired()) com.example.artisanx.util.SessionEventBus.emitExpired()
            e.printStackTrace()
            Resource.Error("AI matching unavailable. Artisans sorted by rating.")
        }
    }
}
