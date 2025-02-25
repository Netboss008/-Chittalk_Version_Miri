package com.example.chittalk.data.livestream.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveStreamChatHandler @Inject constructor() {
    private val chatMessages = MutableStateFlow<Map<String, List<StreamChatMessage>>>(emptyMap())
    private val chatFilters = MutableStateFlow<Map<String, ChatFilter>>(emptyMap())
    private val userLastMessageTime = MutableStateFlow<Map<String, Long>>(emptyMap())

    fun observeChatMessages(streamId: String): Flow<List<StreamChatMessage>> {
        return chatMessages.map { it[streamId].orEmpty() }
    }

    suspend fun sendMessage(
        streamId: String,
        userId: String,
        userName: String,
        userAvatar: String?,
        content: ChatContent,
        userRole: StreamUserRole
    ): Result<StreamChatMessage> {
        return try {
            // Prüfe Chat-Filter
            val filter = chatFilters.value[streamId]
            if (filter != null) {
                checkChatFilters(filter, userId, content)
            }

            val message = StreamChatMessage(
                streamId = streamId,
                userId = userId,
                userName = userName,
                userAvatar = userAvatar,
                content = content,
                userRole = userRole
            )

            val currentMessages = chatMessages.value[streamId].orEmpty()
            chatMessages.value = chatMessages.value + (streamId to (currentMessages + message))
            userLastMessageTime.value = userLastMessageTime.value + (userId to System.currentTimeMillis())

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun checkChatFilters(filter: ChatFilter, userId: String, content: ChatContent) {
        // Prüfe Slow Mode
        val lastMessageTime = userLastMessageTime.value[userId] ?: 0L
        if (System.currentTimeMillis() - lastMessageTime < filter.slowMode * 1000) {
            throw Exception("Bitte warte ${filter.slowMode} Sekunden zwischen Nachrichten")
        }

        // Prüfe blockierte Wörter
        if (content is ChatContent.Text) {
            filter.blockWords.forEach { word ->
                if (content.message.contains(word, ignoreCase = true)) {
                    throw Exception("Deine Nachricht enthält blockierte Wörter")
                }
            }
        }
    }

    suspend fun updateChatFilter(streamId: String, filter: ChatFilter) {
        chatFilters.value = chatFilters.value + (streamId to filter)
    }

    suspend fun pinMessage(messageId: String, streamId: String) {
        val currentMessages = chatMessages.value[streamId].orEmpty()
        val updatedMessages = currentMessages.map { message ->
            if (message.id == messageId) message.copy(isPinned = true) else message
        }
        chatMessages.value = chatMessages.value + (streamId to updatedMessages)
    }

    suspend fun deleteMessage(messageId: String, streamId: String) {
        val currentMessages = chatMessages.value[streamId].orEmpty()
        val updatedMessages = currentMessages.filter { it.id != messageId }
        chatMessages.value = chatMessages.value + (streamId to updatedMessages)
    }

    suspend fun clearChat(streamId: String) {
        chatMessages.value = chatMessages.value + (streamId to emptyList())
    }
}