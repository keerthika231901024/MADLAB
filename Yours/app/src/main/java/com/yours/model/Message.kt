package com.yours.model

data class Message(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageType {
    USER, BOT
}
