package com.example.fitnessap.network

import com.example.fitnessap.data.model.UserProfile
import com.example.fitnessap.network.model.ChatRequest
import com.example.fitnessap.network.model.ChatResponse
import com.example.fitnessap.network.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OpenAiService {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun estimateCaloriesBurned(
        apiKey: String,
        description: String,
        profile: UserProfile
    ): Int = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are a fitness calorie calculator. Given a physical activity description and user profile,
            estimate the calories burned. Respond with ONLY a single integer number, nothing else.
            User profile: Name=${profile.name}, Age=${profile.age}, Weight=${profile.weightKg}kg,
            Height=${profile.heightCm}cm, Gender=${profile.gender}, Activity Level=${profile.activityLevel}
        """.trimIndent()

        val request = ChatRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                Message("system", systemPrompt),
                Message("user", description)
            ),
            max_tokens = 50
        )

        val response = makeRequest(apiKey, request)
        parseCalorieInteger(response)
    }

    suspend fun estimateCaloriesConsumed(
        apiKey: String,
        description: String
    ): Int = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are a nutrition calorie calculator. Given a food description, estimate the total calories consumed.
            Respond with ONLY a single integer number, nothing else.
        """.trimIndent()

        val request = ChatRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                Message("system", systemPrompt),
                Message("user", description)
            ),
            max_tokens = 50
        )

        val response = makeRequest(apiKey, request)
        parseCalorieInteger(response)
    }

    private fun makeRequest(apiKey: String, chatRequest: ChatRequest): String {
        val body = json.encodeToString(chatRequest).toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response body")
        if (!response.isSuccessful) {
            throw Exception("API error ${response.code}: $responseBody")
        }
        val chatResponse = json.decodeFromString<ChatResponse>(responseBody)
        return chatResponse.choices.firstOrNull()?.message?.content ?: "0"
    }

    private fun parseCalorieInteger(response: String): Int {
        return response.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    }
}
