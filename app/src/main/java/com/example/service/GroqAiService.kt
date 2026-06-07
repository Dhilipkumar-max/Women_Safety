package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GroqAiService {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val threatAdapter = moshi.adapter(ThreatAnalysis::class.java)
    private val requestAdapter = moshi.adapter(GroqRequest::class.java)
    private val responseAdapter = moshi.adapter(GroqResponse::class.java)

    private val apiKey = BuildConfig.GROQ_API_KEY

    suspend fun analyzeThreat(
        heartRate: Int,
        motion: String,
        audioTranscript: String
    ): ThreatAnalysis? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "your_groq_api_key_here") {
            Log.w("GroqAiService", "API Key is missing or default")
            return@withContext null
        }

        val prompt = """
            You are the "Neural Emergency Orchestration" (NEO) engine for SafeHer.
            Analyze the following multi-modal sensor data for potential danger:
            - Heart Rate: $heartRate bpm
            - Motion Activity: $motion
            - Ambient Audio Transcript: "$audioTranscript"

            Respond ONLY with a JSON object in this format:
            {
              "threatScore": 0-100,
              "reason": "Brief explanation of the assessment",
              "action": "ALERT" (immediate danger), "MONITOR" (suspicious, stay alert), or "IGNORE" (normal activity)
            }
        """.trimIndent()

        val groqRequest = GroqRequest(
            model = "llama-3-8b-8192",
            messages = listOf(
                GroqMessage(role = "system", content = "You are a critical safety analysis AI. Be precise and fast."),
                GroqMessage(role = "user", content = prompt)
            ),
            response_format = GroqResponseFormat(type = "json_object")
        )

        val bodyJson = requestAdapter.toJson(groqRequest)
        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(bodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GroqAiService", "API Error: ${response.code} - ${response.message}")
                    return@withContext null
                }

                val responseBody = response.body?.string() ?: return@withContext null
                val groqResponse = responseAdapter.fromJson(responseBody)
                val jsonContent = groqResponse?.choices?.firstOrNull()?.message?.content ?: return@withContext null
                
                return@withContext threatAdapter.fromJson(jsonContent)
            }
        } catch (e: Exception) {
            Log.e("GroqAiService", "Exception during threat analysis: ${e.message}")
            null
        }
    }
}
