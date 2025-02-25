package com.example.chittalk.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chittalk.data.call.CallHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.call.CallState
import org.webrtc.EglBase
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callHandler: CallHandler
) : ViewModel() {

    private val _state = MutableStateFlow(CallScreenState())
    val state: StateFlow<CallScreenState> = _state

    val eglContext = EglBase.create().eglBaseContext

    init {
        observeCallState()
    }

    private fun observeCallState() {
        viewModelScope.launch {
            callHandler.callState.collect { callState ->
                _state.update { 
                    it.copy(
                        callStatus = when (callState) {
                            CallState.Idle -> "Bereit"
                            CallState.Dialing -> "Wähle..."
                            CallState.Ringing -> "Klingelt..."
                            CallState.Answering -> "Verbinde..."
                            CallState.Connected -> "Verbunden"
                            CallState.Ended -> "Beendet"
                            else -> "Unbekannt"
                        }
                    )
                }
            }
        }

        viewModelScope.launch {
            callHandler.localVideoTrack.collect { track ->
                _state.update { it.copy(localVideoTrack = track) }
            }
        }

        viewModelScope.launch {
            callHandler.remoteVideoTrack.collect { track ->
                _state.update { it.copy(remoteVideoTrack = track) }
            }
        }

        viewModelScope.launch {
            callHandler.isMicrophoneMuted.collect { isMuted ->
                _state.update { it.copy(isMicrophoneMuted = isMuted) }
            }
        }

        viewModelScope.launch {
            callHandler.isSpeakerEnabled.collect { isEnabled ->
                _state.update { it.copy(isSpeakerEnabled = isEnabled) }
            }
        }

        viewModelScope.launch {
            callHandler.isLocalVideoEnabled.collect { isEnabled ->
                _state.update { it.copy(isLocalVideoEnabled = isEnabled) }
            }
        }
    }

    fun onEvent(event: CallEvent) {
        when (event) {
            CallEvent.AcceptCall -> callHandler.acceptCall()
            CallEvent.RejectCall -> callHandler.rejectCall()
            CallEvent.EndCall -> callHandler.endCall()
            CallEvent.ToggleMicrophone -> callHandler.toggleMicrophone()
            CallEvent.ToggleSpeaker -> callHandler.toggleSpeaker()
            CallEvent.ToggleVideo -> callHandler.toggleVideo()
        }
    }
}

data class CallScreenState(
    val remoteName: String = "",
    val callStatus: String = "",
    val isVideoCall: Boolean = false,
    val isIncomingCall: Boolean = false,
    val isMicrophoneMuted: Boolean = false,
    val isSpeakerEnabled: Boolean = false,
    val isLocalVideoEnabled: Boolean = true,
    val localVideoTrack: org.webrtc.VideoTrack? = null,
    val remoteVideoTrack: org.webrtc.VideoTrack? = null
)

sealed class CallEvent {
    object AcceptCall : CallEvent()
    object RejectCall : CallEvent()
    object EndCall : CallEvent()
    object ToggleMicrophone : CallEvent()
    object ToggleSpeaker : CallEvent()
    object ToggleVideo : CallEvent()
}