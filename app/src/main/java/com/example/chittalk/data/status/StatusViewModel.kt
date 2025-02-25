package com.example.chittalk.presentation.status

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chittalk.data.status.LiveStream
import com.example.chittalk.data.status.Status
import com.example.chittalk.data.status.StatusHandler
import com.example.chittalk.data.status.StatusType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val statusHandler: StatusHandler
) : ViewModel() {

    private val _state = MutableStateFlow(StatusScreenState())
    val state: StateFlow<StatusScreenState> = _state

    private var currentLiveStream: LiveStream? = null

    init {
        loadStatuses()
        observeLiveStreams()
    }

    fun onEvent(event: StatusEvent) {
        when (event) {
            is StatusEvent.CreateStatus -> showStatusCreationDialog()
            is StatusEvent.UploadStatus -> uploadStatus(event.uri, event.type, event.caption)
            is StatusEvent.ViewStatus -> viewStatus(event.status)
            is StatusEvent.CloseStatus -> closeStatus()
            is StatusEvent.AddReaction -> addReaction(event.status, event.reaction)
            is StatusEvent.DeleteStatus -> deleteStatus(event.status)
            is StatusEvent.StartLiveStream -> startLiveStream()
            is StatusEvent.StopLiveStream -> stopLiveStream()
            is StatusEvent.WatchLiveStream -> watchLiveStream(event.stream)
        }
    }

    private fun loadStatuses() {
        viewModelScope.launch {
            try {
                statusHandler.getStatusUpdates()
                    .collect { statuses ->
                        val myStatus = statuses.find { it.userId == "current_user_id" }
                        val recentUpdates = statuses.filter { it.userId != "current_user_id" }
                            .sortedByDescending { it.timestamp }

                        _state.update {
                            it.copy(
                                myStatus = myStatus,
                                recentUpdates = recentUpdates,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Fehler beim Laden der Status-Updates",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeLiveStreams() {
        viewModelScope.launch {
            try {
                statusHandler.getLiveStreams()
                    .collect { streams ->
                        _state.update {
                            it.copy(liveStreams = streams)
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Fehler beim Laden der Live-Streams"
                    )
                }
            }
        }
    }

    private fun showStatusCreationDialog() {
        _state.update {
            it.copy(showStatusCreationDialog = true)
        }
    }

    private fun uploadStatus(uri: Uri, type: StatusType, caption: String?) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isUploading = true) }
                
                val status = statusHandler.saveStatus(
                    mediaUri = uri,
                    type = type,
                    caption = caption
                )

                _state.update {
                    it.copy(
                        myStatus = status,
                        isUploading = false,
                        showStatusCreationDialog = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Fehler beim Hochladen des Status",
                        isUploading = false
                    )
                }
            }
        }
    }

    private fun viewStatus(status: Status) {
        _state.update {
            it.copy(selectedStatus = status)
        }

        // Status als gesehen markieren
        viewModelScope.launch {
            try {
                // Implementierung zum Markieren des Status als gesehen
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Fehler beim Markieren des Status als gesehen")
                }
            }
        }
    }

    private fun closeStatus() {
        _state.update {
            it.copy(selectedStatus = null)
        }
    }

    private fun addReaction(status: Status, reaction: String) {
        viewModelScope.launch {
            try {
                val newReaction = com.example.chittalk.data.status.Reaction(
                    userId = "current_user_id",
                    type = reaction,
                    timestamp = System.currentTimeMillis()
                )

                // Status mit neuer Reaktion aktualisieren
                val updatedStatus = status.copy(
                    reactions = status.reactions + newReaction
                )

                // Status in der UI aktualisieren
                _state.update { currentState ->
                    currentState.copy(
                        recentUpdates = currentState.recentUpdates.map { 
                            if (it.id == status.id) updatedStatus else it 
                        }
                    )
                }

                // Reaktion auf dem Server speichern
                // Implementierung zum Speichern der Reaktion
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Fehler beim Hinzufügen der Reaktion")
                }
            }
        }
    }

    private fun deleteStatus(status: Status) {
        viewModelScope.launch {
            try {
                // Status löschen
                // Implementierung zum Löschen des Status

                // UI aktualisieren
                _state.update { currentState ->
                    currentState.copy(
                        myStatus = if (currentState.myStatus?.id == status.id) null else currentState.myStatus,
                        recentUpdates = currentState.recentUpdates.filter { it.id != status.id }
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Fehler beim Löschen des Status")
                }
            }
        }
    }

    private fun startLiveStream() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isStartingLiveStream = true) }
                
                val liveStream = statusHandler.startLiveStream()
                currentLiveStream = liveStream

                _state.update {
                    it.copy(
                        currentLiveStream = liveStream,
                        isStartingLiveStream = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Fehler beim Starten des Live-Streams",
                        isStartingLiveStream = false
                    )
                }
            }
        }
    }

    private fun stopLiveStream() {
        viewModelScope.launch {
            try {
                currentLiveStream?.let { stream ->
                    statusHandler.stopLiveStream(stream.id)
                    currentLiveStream = null
                    
                    _state.update {
                        it.copy(currentLiveStream = null)
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Fehler beim Beenden des Live-Streams")
                }
            }
        }
    }

    private fun watchLiveStream(stream: LiveStream) {
        _state.update {
            it.copy(selectedLiveStream = stream)
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentLiveStream?.let {
            viewModelScope.launch {
                statusHandler.stopLiveStream(it.id)
            }
        }
    }
}

data class StatusScreenState(
    val myStatus: Status? = null,
    val recentUpdates: List<Status> = emptyList(),
    val liveStreams: List<LiveStream> = emptyList(),
    val selectedStatus: Status? = null,
    val selectedLiveStream: LiveStream? = null,
    val currentLiveStream: LiveStream? = null,
    val showStatusCreationDialog: Boolean = false,
    val isLoading: Boolean = true,
    val isUploading: Boolean = false,
    val isStartingLiveStream: Boolean = false,
    val error: String? = null
)

sealed class StatusEvent {
    object CreateStatus : StatusEvent()
    data class UploadStatus(
        val uri: Uri,
        val type: StatusType,
        val caption: String? = null
    ) : StatusEvent()
    data class ViewStatus(val status: Status) : StatusEvent()
    object CloseStatus : StatusEvent()
    data class AddReaction(val status: Status, val reaction: String) : StatusEvent()
    data class DeleteStatus(val status: Status) : StatusEvent()
    object StartLiveStream : StatusEvent()
    object StopLiveStream : StatusEvent()
    data class WatchLiveStream(val stream: LiveStream) : StatusEvent()
}