package de.chittalk.messenger.data.remote.model

data class MessageRequest(
    val text: String,
    val chatId: String? = null
)

data class MessageResponse(
    val id: String,
    val text: String,
    val senderId: String,
    val senderName: String,
    val timestamp: Long,
    val chatId: String? = null
)

data class ChatResponse(
    val id: String,
    val participants: List<String>,
    val lastMessage: MessageResponse?,
    val createdAt: Long
)