package com.example.chittalk.presentation.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chittalk.data.repository.MatrixRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val matrixRepository: MatrixRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatsState())
    val state: StateFlow<ChatsState> = _state

    init {
        loadChats()
    }

    fun onEvent(event: ChatsEvent) {
        when (event) {
            is ChatsEvent.OpenChat -> openChat(event.chatId)
            is ChatsEvent.SendMessage -> sendMessage(event.chatId, event.message)
            is ChatsEvent.CreateNewChat -> createNewChat(event.userId)
            is ChatsEvent.DeleteChat -> deleteChat(event.chatId)
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                matrixRepository.getRooms().collect { rooms ->
                    _state.update { 
                        it.copy(
                            chats = rooms.map { room ->
                                Chat(
                                    id = room.roomId,
                                    title = room.displayName ?: "Unbenannter Chat",
                                    lastMessage = room.latestMessage ?: "",
                                    timestamp = room.lastMessageTimestamp
                                )
                            },
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Fehler beim Laden der Chats",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun openChat(chatId: String) {
        viewModelScope.launch {
            // Navigation zum Chat wird später implementiert
        }
    }

    private fun sendMessage(chatId: String, message: String) {
        viewModelScope.launch {
            try {
                matrixRepository.sendMessage(chatId, message)
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Fehler beim Senden der Nachricht")
                }
            }
        }
    }

    private fun createNewChat(userId: String) {
        viewModelScope.launch {
            try {
                val result = matrixRepository.createDirectRoom(userId)
                result.onSuccess { roomId ->
                    loadChats() // Aktualisiere die Chat-Liste
                }.onFailure { exception ->
                    _state.update { 
                        it.copy(error = exception.message ?: "Fehler beim Erstellen des Chats")
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Fehler beim Erstellen des Chats")
                }
            }
        }
    }

    private fun deleteChat(chatId: String) {
        viewModelScope.launch {
            try {
                matrixRepository.leaveRoom(chatId)
                loadChats() // Aktualisiere die Chat-Liste
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Fehler beim Löschen des Chats")
                }
            }
        }
    }
}

data class ChatsState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ChatsEvent {
    data class OpenChat(val chatId: String) : ChatsEvent()
    data class SendMessage(val chatId: String, val message: String) : ChatsEvent()
    data class CreateNewChat(val userId: String) : ChatsEvent()
    data class DeleteChat(val chatId: String) : ChatsEvent()
}