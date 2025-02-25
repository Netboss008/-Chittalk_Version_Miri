package com.example.chittalk.data.livestream.chat

import java.util.UUID

data class StreamChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val streamId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String?,
    val content: ChatContent,
    val timestamp: Long = System.currentTimeMillis(),
    val isHighlighted: Boolean = false,
    val isPinned: Boolean = false,
    val userRole: StreamUserRole = StreamUserRole.VIEWER
)

sealed class ChatContent {
    data class Text(val message: String) : ChatContent()
    data class Gift(val giftType: String, val amount: Int) : ChatContent()
    data class Sticker(val stickerId: String) : ChatContent()
    data class SystemMessage(val message: String) : ChatContent()
}

enum class StreamUserRole {
    HOST,
    TEMP_HOST,
    MODERATOR,
    VIP,
    VIEWER
}

data class ChatFilter(
    val blockWords: List<String> = emptyList(),
    val slowMode: Int = 0, // Sekunden zwischen Nachrichten
    val subscriberOnly: Boolean = false,
    val minimumAccountAge: Int = 0 // Mindestalter des Accounts in Tagen
)

data class ChatReaction(
    val messageId: String,
    val userId: String,
    val reaction: String,
    val timestamp: Long = System.currentTimeMillis()
)