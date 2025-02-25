package de.chittalk.messenger.data.repository.impl

import de.chittalk.messenger.data.repository.ChatRepository
import de.chittalk.messenger.ui.screens.chat.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    // Hier kommen später die Dependencies wie ApiService, LocalDatabase etc.
) : ChatRepository {
    
    override fun getMessages(): Flow<List<Message>> = flow {
        // TODO: Implementiere Message-Abruf
        emit(emptyList())
    }

    override suspend fun sendMessage(message: Message) {
        // TODO: Implementiere Message-Versand
    }

    override suspend fun deleteMessage(messageId: String) {
        // TODO: Implementiere Message-Löschung
    }

    override suspend fun getMessageById(messageId: String): Message? {
        // TODO: Implementiere einzelne Message-Abfrage
        return null
    }
}