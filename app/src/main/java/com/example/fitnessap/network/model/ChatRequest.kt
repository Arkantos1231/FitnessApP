package com.example.fitnessap.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int
)

@Serializable
data class Message(
    val role: String,
    val content: String
)
