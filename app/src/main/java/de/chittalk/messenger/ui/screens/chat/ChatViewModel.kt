package de.chittalk.messenger.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            chatRepository.getMessages()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
                .collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false
                    )
                }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(
                    Message(
                        id = UUID.randomUUID().toString(),
                        text = text,
                        timestamp = System.currentTimeMillis(),
                        senderId = uiState.value.currentUserId
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentUserId: String = "" // Dies sollte aus dem AuthRepository kommen
)

data class Message(
    val id: String,
    val text: String,
    val timestamp: Long,
    val senderId: String
)