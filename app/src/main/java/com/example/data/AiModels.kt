package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ThreatAnalysis(
    val threatScore: Int,
    val reason: String,
    val action: String // "ALERT", "MONITOR", "IGNORE"
)

@JsonClass(generateAdapter = true)
data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val response_format: GroqResponseFormat? = null
)

@JsonClass(generateAdapter = true)
data class GroqMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class GroqResponseFormat(
    val type: String
)

@JsonClass(generateAdapter = true)
data class GroqResponse(
    val choices: List<GroqChoice>
)

@JsonClass(generateAdapter = true)
data class GroqChoice(
    val message: GroqMessage
)
