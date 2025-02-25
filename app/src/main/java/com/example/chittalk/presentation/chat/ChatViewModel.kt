package com.example.chittalk.presentation.chat

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chittalk.data.media.MediaHandler
import com.example.chittalk.data.media.MediaType
import com.example.chittalk.data.repository.ChatMessage
import com.example.chittalk.data.repository.MatrixRepository
import com.example.chittalk.data.repository.MediaMessage
import com.example.chittalk.data.repository.TextMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val matrixRepository: MatrixRepository,
    private val mediaHandler: MediaHandler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state

    private var currentRoomId: String? = null

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.LoadChat -> loadChat(event.chatId)
            is ChatEvent.SendMessage -> sendTextMessage(event.message)
            is ChatEvent.SendImage -> sendImage(event.uri)
            is ChatEvent.SendVideo -> sendVideo(event.uri)
            is ChatEvent.SendFile -> sendFile(event.uri)
            is ChatEvent.DownloadMedia -> downloadMedia(event.message)
            is ChatEvent.RetryFailedMessage -> retryFailedMessage(event.message)
            is ChatEvent.DeleteMessage -> deleteMessage(event.messageId)
            is ChatEvent.StartRecordingVoice -> startRecordingVoice()
            is ChatEvent.StopRecordingVoice -> stopRecordingVoice()
            is ChatEvent.CancelRecordingVoice -> cancelRecordingVoice()
        }
    }

    private fun loadChat(chatId: String) {
        currentRoomId = chatId
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // Chat Details laden
                matrixRepository.getRoomInfo(chatId)?.let { roomInfo ->
                    _state.update { 
                        it.copy(
                            chatId = chatId,
                            chatName = roomInfo.displayName ?: "Chat",
                            chatAvatar = roomInfo.avatarUrl,
                            memberCount = roomInfo.memberCount,
                            currentUserId = matrixRepository.getCurrentUserId() ?: ""
                        )
                    }
                }

                // Nachrichten beobachten
                matrixRepository.observeRoomMessages(chatId).collect { messages ->
                    _state.update { 
                        it.copy(
                            messages = messages,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Fehler beim Laden des Chats",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun sendTextMessage(message: String) {
        val roomId = currentRoomId ?: return
        viewModelScope.launch {
            try {
                matrixRepository.sendMessage(roomId, message)
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Fehler beim Senden der Nachricht")
                }
            }
        }
    }

    private fun sendImage(uri: Uri) {
        val roomId = currentRoomId ?: return
        viewModelScope.launch {
            try {
                _state.update { it.copy(isMediaUploading = true) }
                val mediaFile = mediaHandler.saveImageToFile(uri)
                matrixRepository.sendMedia(roomId, mediaFile)
                _state.update { it.copy(isMediaUploading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Fehler beim Senden des Bildes",
                        isMediaUploading = false
                    )
                }
            }
        }
    }

    private fun sendVideo(uri: Uri) {
        val roomId = currentRoomId ?: return
        viewModelScope.launch {
            try {
                _state.update { it.copy(isMediaUploading = true) }
                val mediaFile = mediaHandler.saveVideoToFile(uri)
                matrixRepository.sendMedia(roomId, mediaFile)
                _state.update { it.copy(isMediaUploading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Fehler beim Senden des Videos",
                        isMediaUploading = false
                    )
                }
            }
        }
    }

    private fun sendFile(uri: Uri) {
        val roomId = currentRoomId ?: return
        viewModelScope.launch {
            try {
                _state.update { it.copy(isMediaUploading = true) }
                val mediaFile = mediaHandler.saveFile(uri)
                matrixRepository.sendMedia(roomId, mediaFile)
                _state.update { it.copy(isMediaUploading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Fehler beim Senden der Datei",
                        isMediaUploading = false
                    )
                }
            }
        }
    }

    private fun downloadMedia(message: MediaMessage) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isMediaDownloading = true) }
                val file = matrixRepository.downloadMedia(message.url)
                file.onSuccess { downloadedFile ->
                    _state.update { 
                        it.copy(
                            downloadedMedia = DownloadedMedia(
                                file = downloadedFile,
                                type = message.type,
                                mimeType = message.mimeType ?: ""
                            ),
                            isMediaDownloading = false
                        )
                    }
                }.onFailure { exception ->
                    _state.update { 
                        it.copy(
                            error = exception.message ?: "Fehler beim Herunterladen der Mediendatei",
                            isMediaDownloading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Fehler beim Herunterladen der Mediendatei",
                        isMediaDownloading = false
                    )
                }
            }
        }
    }

    private fun retryFailedMessage(message: ChatMessage) {
        when (message) {
            is TextMessage -> sendTextMessage(message.content)
            is MediaMessage -> {
                // Implementierung des erneuten Medienversands
            }
        }
    }

    private fun deleteMessage(messageId: String) {
        val roomId = currentRoomId ?: return
        viewModelScope.launch {
            try {
                // Implementierung des Löschens von Nachrichten
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Fehler beim Löschen der Nachricht")
                }
            }
        }
    }

    private fun startRecordingVoice() {
        // Implementierung der Sprachaufnahme
    }

    private fun stopRecordingVoice() {
        // Implementierung des Beendens der Sprachaufnahme
    }

    private fun cancelRecordingVoice() {
        // Implementierung des Abbrechens der Sprachaufnahme
    }
}

data class ChatState(
    val chatId: String = "",
    val chatName: String = "",
    val chatAvatar: String? = null,
    val memberCount: Int = 0,
    val messages: List<ChatMessage> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val isMediaUploading: Boolean = false,
    val isMediaDownloading: Boolean = false,
    val isRecordingVoice: Boolean = false,
    val downloadedMedia: DownloadedMedia? = null,
    val error: String? = null
)

data class DownloadedMedia(
    val file: File,
    val type: MediaType,
    val mimeType: String
)

sealed class ChatEvent {
    data class LoadChat(val chatId: String) : ChatEvent()
    data class SendMessage(val message: String) : ChatEvent()
    data class SendImage(val uri: Uri) : ChatEvent()
    data class SendVideo(val uri: Uri) : ChatEvent()
    data class SendFile(val uri: Uri) : ChatEvent()
    data class DownloadMedia(val message: MediaMessage) : ChatEvent()
    data class RetryFailedMessage(val message: ChatMessage) : ChatEvent()
    data class DeleteMessage(val messageId: String) : ChatEvent()
    object StartRecordingVoice : ChatEvent()
    object StopRecordingVoice : ChatEvent()
    object CancelRecordingVoice : ChatEvent()
}