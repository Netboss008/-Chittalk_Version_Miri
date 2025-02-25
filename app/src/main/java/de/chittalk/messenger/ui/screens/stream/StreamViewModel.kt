package de.chittalk.messenger.ui.screens.stream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StreamViewModel @Inject constructor(
    private val streamRepository: StreamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreamUiState())
    val uiState: StateFlow<StreamUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            streamRepository.getAvailableStreams()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
                .collect { streams ->
                    _uiState.value = _uiState.value.copy(
                        availableStreams = streams,
                        isLoading = false
                    )
                }
        }
    }

    fun joinStream(streamId: String) {
        viewModelScope.launch {
            try {
                val stream = streamRepository.joinStream(streamId)
                _uiState.value = _uiState.value.copy(activeStream = stream)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun startStream(title: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isStreaming = true)
                val stream = streamRepository.startStream(title)
                _uiState.value = _uiState.value.copy(activeStream = stream)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    error = e.message
                )
            }
        }
    }

    fun endStream() {
        viewModelScope.launch {
            try {
                streamRepository.endStream()
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    activeStream = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

data class StreamUiState(
    val availableStreams: List<Stream> = emptyList(),
    val activeStream: Stream? = null,
    val isStreaming: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class Stream(
    val id: String,
    val title: String,
    val streamerName: String,
    val streamerImageUrl: String?,
    val viewerCount: Int,
    val isLive: Boolean
)