package com.example.chittalk.presentation.livestream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chittalk.data.livestream.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveStreamViewModel @Inject constructor(
    private val liveStreamHandler: LiveStreamHandler,
    private val userRepository: UserRepository // Für Benutzerinformationen
) : ViewModel() {

    private val _state = MutableStateFlow(LiveStreamViewState())
    val state: StateFlow<LiveStreamViewState> = _state.asStateFlow()

    private val currentUserId: String
        get() = userRepository.getCurrentUserId()

    init {
        observeStreams()
        observeGuestRequests()
    }

    fun onEvent(event: LiveStreamEvent) {
        when (event) {
            is LiveStreamEvent.StartStream -> startStream(event.title, event.isPrivate)
            is LiveStreamEvent.EndStream -> endStream()
            is LiveStreamEvent.ChangeStream -> changeStream(event.index)
            is LiveStreamEvent.RequestToJoin -> requestToJoinStream()
            is LiveStreamEvent.HandleGuestRequest -> handleGuestRequest(event.request, event.accepted)
            is LiveStreamEvent.ParticipantAction -> handleParticipantAction(event.participant, event.action)
            is LiveStreamEvent.TransferHostControl -> transferHostControl(event.moderatorId)
            is LiveStreamEvent.ReturnHostControl -> returnHostControl()
            is LiveStreamEvent.ToggleParticipantGrid -> toggleParticipantGrid()
            is LiveStreamEvent.ToggleGuestRequests -> toggleGuestRequests()
            is LiveStreamEvent.ToggleChat -> toggleChat()
            is LiveStreamEvent.LikeStream -> likeStream()
            is LiveStreamEvent.ShareStream -> shareStream()
            is LiveStreamEvent.SendMessage -> sendMessage(event.message)
        }
    }

    private fun observeStreams() {
        viewModelScope.launch {
            combine(
                liveStreamHandler.getForYouPageStreams(),
                liveStreamHandler.getContactStreams(currentUserId)
            ) { forYouStreams, contactStreams ->
                when (state.value.streamFilter) {
                    StreamFilter.FOR_YOU -> forYouStreams
                    StreamFilter.FOLLOWING -> contactStreams
                }
            }.collect { streams ->
                _state.update { it.copy(
                    streams = streams,
                    currentStream = streams.getOrNull(state.value.currentStreamIndex)
                )}
            }
        }
    }

    private fun observeGuestRequests() {
        viewModelScope.launch {
            state.value.currentStream?.let { stream ->
                // Beobachte Gästeanfragen für den aktuellen Stream
                liveStreamHandler.observeGuestRequests(stream.id)
                    .collect { requests ->
                        _state.update { it.copy(guestRequests = requests) }
                    }
            }
        }
    }

    private fun startStream(title: String, isPrivate: Boolean) {
        viewModelScope.launch {
            try {
                val settings = StreamSettings(
                    maxParticipants = 8,
                    allowGuestRequests = true,
                    allowChat = true,
                    allowReactions = true,
                    allowModeratorTakeover = true
                )

                liveStreamHandler.startStream(
                    hostId = currentUserId,
                    title = title,
                    isPrivate = isPrivate,
                    settings = settings
                ).collect { stream ->
                    _state.update { it.copy(
                        currentStream = stream,
                        isCurrentUserHost = true
                    )}
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    private fun changeStream(index: Int) {
        if (index in state.value.streams.indices) {
            _state.update { it.copy(
                currentStreamIndex = index,
                currentStream = state.value.streams[index],
                isCurrentUserHost = state.value.streams[index].hostId == currentUserId,
                isCurrentUserTempHost = state.value.streams[index].temporaryHostId == currentUserId
            )}
            observeGuestRequests()
        }
    }

    private fun requestToJoinStream() {
        viewModelScope.launch {
            state.value.currentStream?.let { stream ->
                liveStreamHandler.requestToJoinStream(stream.id, currentUserId)
                    .onSuccess {
                        _state.update { it.copy(hasRequestedToJoin = true) }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(error = e.message) }
                    }
            }
        }
    }

    private fun handleGuestRequest(request: GuestRequest, accepted: Boolean) {
        viewModelScope.launch {
            state.value.currentStream?.let { stream ->
                val action = if (accepted) {
                    StreamAction(
                        type = StreamActionType.PROMOTE_TO_MODERATOR,
                        targetUserId = request.userId,
                        performedBy = currentUserId
                    )
                } else {
                    StreamAction(
                        type = StreamActionType.REMOVE_FROM_STREAM,
                        targetUserId = request.userId,
                        performedBy = currentUserId
                    )
                }

                liveStreamHandler.handleStreamAction(stream.id, action)
                    .onFailure { e ->
                        _state.update { it.copy(error = e.message) }
                    }
            }
        }
    }

    private fun handleParticipantAction(participant: Participant, action: StreamActionType) {
        viewModelScope.launch {
            state.value.currentStream?.let { stream ->
                val streamAction = StreamAction(
                    type = action,
                    targetUserId = participant.userId,
                    performedBy = currentUserId
                )

                liveStreamHandler.handleStreamAction(stream.id, streamAction)
                    .onFailure { e ->
                        _state.update { it.copy(error = e.message) }
                    }
            }
        }
    }

    private fun transferHostControl(moderatorId: String) {
        viewModelScope.launch {
            state.value.currentStream?.let { stream ->
                val action = StreamAction(
                    type = StreamActionType.TRANSFER_HOST,
                    targetUserId = moderatorId,
                    performedBy = currentUserId
                )

                liveStreamHandler.handleStreamAction(stream.id, action)
                    .onSuccess {
                        _state.update { state ->
                            state.copy(
                                isCurrentUserHost = false,
                                isCurrentUserTempHost = false
                            )
                        }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(error = e.message) }
                    }
            }
        }
    }

    private fun returnHostControl() {
        viewModelScope.launch {
            state.value.currentStream?.let { stream ->
                val action = StreamAction(
                    type = StreamActionType.TAKE_BACK_HOST,
                    targetUserId = stream.hostId,
                    performedBy = currentUserId
                )

                liveStreamHandler.handleStreamAction(stream.id, action)
                    .onSuccess {
                        _state.update { state ->
                            state.copy(
                                isCurrentUserTempHost = false
                            )
                        }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(error = e.message) }
                    }
            }
        }
    }

    private fun toggleParticipantGrid() {
        _state.update { it.copy(showParticipantGrid = !it.showParticipantGrid) }
    }

    private fun toggleGuestRequests() {
        _state.update { it.copy(showGuestRequests = !it.showGuestRequests) }
    }

    private fun toggleChat() {
        _state.update { it.copy(showChat = !it.showChat) }
    }

    private fun likeStream() {
        viewModelScope.launch {
            state.value.currentStream?.let { stream ->
                // Implementiere Like-Funktionalität
            }
        }
    }

    private fun shareStream() {
        viewModelScope.launch {
            state.value.currentStream?.let { stream ->
                // Implementiere Teilen-Funktionalität
            }
        }
    }

    private fun sendMessage(message: String) {
        viewModelScope.launch {
            state.value.currentStream?.let { stream ->
                // Implementiere Chat-Nachricht senden
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            state.value.currentStream?.let { stream ->
                if (stream.hostId == currentUserId) {
                    liveStreamHandler.endStream(stream.id)
                }
            }
        }
    }
}

data class LiveStreamViewState(
    val streams: List<LiveStream> = emptyList(),
    val currentStreamIndex: Int = 0,
    val currentStream: LiveStream? = null,
    val isCurrentUserHost: Boolean = false,
    val isCurrentUserTempHost: Boolean = false,
    val hasRequestedToJoin: Boolean = false,
    val showParticipantGrid: Boolean = false,
    val showGuestRequests: Boolean = false,
    val showChat: Boolean = false,
    val guestRequests: List<GuestRequest> = emptyList(),
    val streamFilter: StreamFilter = StreamFilter.FOR_YOU,
    val error: String? = null
)

enum class StreamFilter {
    FOR_YOU, FOLLOWING
}

sealed class LiveStreamEvent {
    data class StartStream(val title: String, val isPrivate: Boolean) : LiveStreamEvent()
    object EndStream : LiveStreamEvent()
    data class ChangeStream(val index: Int) : LiveStreamEvent()
    object RequestToJoin : LiveStreamEvent()
    data class HandleGuestRequest(val request: GuestRequest, val accepted: Boolean) : LiveStreamEvent()
    data class ParticipantAction(val participant: Participant, val action: StreamActionType) : LiveStreamEvent()
    data class TransferHostControl(val moderatorId: String) : LiveStreamEvent()
    object ReturnHostControl : LiveStreamEvent()
    object ToggleParticipantGrid : LiveStreamEvent()
    object ToggleGuestRequests : LiveStreamEvent()
    object ToggleChat : LiveStreamEvent()
    object LikeStream : LiveStreamEvent()
    object ShareStream : LiveStreamEvent()
    data class SendMessage(val message: String) : LiveStreamEvent()
}