package de.chittalk.messenger.data.repository

import de.chittalk.messenger.ui.screens.chat.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(): Flow<List<Message>>
    suspend fun sendMessage(message: Message)
    suspend fun deleteMessage(messageId: String)
    suspend fun getMessageById(messageId: String): Message?
}