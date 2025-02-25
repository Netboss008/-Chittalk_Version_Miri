package com.example.chittalk.presentation.livestream

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import org.webrtc.SurfaceViewRenderer
import com.example.chittalk.data.livestream.*

@Composable
fun LiveStreamViewer(
    viewModel: LiveStreamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Vertikaler Pager für Stream-Scrolling wie bei TikTok
        VerticalPager(
            streams = state.streams,
            currentStreamIndex = state.currentStreamIndex,
            onStreamChanged = { viewModel.onEvent(LiveStreamEvent.ChangeStream(it)) }
        )

        // Stream-Overlay mit Interaktionselementen
        StreamOverlay(
            stream = state.currentStream,
            isHost = state.isCurrentUserHost,
            onActionSelected = { viewModel.onEvent(it) }
        )

        // Teilnehmer-Raster (wenn aktiv)
        AnimatedVisibility(
            visible = state.showParticipantGrid,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            ParticipantGrid(
                participants = state.currentStream?.participants.orEmpty(),
                moderators = state.currentStream?.moderators.orEmpty(),
                hostId = state.currentStream?.hostId,
                temporaryHostId = state.currentStream?.temporaryHostId,
                onParticipantAction = { participant, action ->
                    viewModel.onEvent(LiveStreamEvent.ParticipantAction(participant, action))
                }
            )
        }

        // Gästeanfragen-Dialog
        if (state.showGuestRequests) {
            GuestRequestsDialog(
                requests = state.guestRequests,
                onRequestAction = { request, accepted ->
                    viewModel.onEvent(LiveStreamEvent.HandleGuestRequest(request, accepted))
                },
                onDismiss = { viewModel.onEvent(LiveStreamEvent.ToggleGuestRequests) }
            )
        }

        // Host-Steuerelemente
        if (state.isCurrentUserHost || state.isCurrentUserTempHost) {
            HostControls(
                isTemporaryHost = state.isCurrentUserTempHost,
                onAction = { viewModel.onEvent(it) }
            )
        }
    }
}

@Composable
fun VerticalPager(
    streams: List<LiveStream>,
    currentStreamIndex: Int,
    onStreamChanged: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = rememberLazyListState(currentStreamIndex),
        flingBehavior = rememberSnapFlingBehavior()
    ) {
        items(streams) { stream ->
            StreamView(
                stream = stream,
                modifier = Modifier
                    .fillParentMaxSize()
                    .aspectRatio(9f/16f)
            )
        }
    }
}

@Composable
fun StreamView(
    stream: LiveStream,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // WebRTC Video-Renderer
        AndroidView(
            factory = { context ->
                SurfaceViewRenderer(context).apply {
                    setMirror(false)
                    setEnableHardwareScaler(true)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun StreamOverlay(
    stream: LiveStream?,
    isHost: Boolean,
    onActionSelected: (LiveStreamEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Stream-Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = "host_avatar_url",
                    contentDescription = "Host",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stream?.title ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "${stream?.viewers ?: 0} Zuschauer",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            if (!isHost) {
                Button(
                    onClick = { onActionSelected(LiveStreamEvent.RequestToJoin) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Mitmachen")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Rechte Aktionsleiste (wie bei TikTok)
        Column(
            modifier = Modifier.align(Alignment.End),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StreamAction(
                icon = Icons.Default.Favorite,
                label = "${stream?.likes ?: 0}",
                onClick = { onActionSelected(LiveStreamEvent.LikeStream) }
            )
            StreamAction(
                icon = Icons.Default.Share,
                label = "Teilen",
                onClick = { onActionSelected(LiveStreamEvent.ShareStream) }
            )
            StreamAction(
                icon = Icons.Default.Comment,
                label = "Chat",
                onClick = { onActionSelected(LiveStreamEvent.ToggleChat) }
            )
        }
    }
}

@Composable
fun ParticipantGrid(
    participants: List<Participant>,
    moderators: List<Moderator>,
    hostId: String?,
    temporaryHostId: String?,
    onParticipantAction: (Participant, StreamActionType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(participants) { participant ->
                ParticipantView(
                    participant = participant,
                    isModerator = moderators.any { it.userId == participant.userId },
                    isHost = participant.userId == hostId,
                    isTempHost = participant.userId == temporaryHostId,
                    onAction = { action -> onParticipantAction(participant, action) }
                )
            }
        }
    }
}

@Composable
fun ParticipantView(
    participant: Participant,
    isModerator: Boolean,
    isHost: Boolean,
    isTempHost: Boolean,
    onAction: (StreamActionType) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(3f/4f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray)
    ) {
        // Video-View oder Platzhalterbild
        if (participant.isVideoEnabled) {
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        setMirror(true)
                        setEnableHardwareScaler(true)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
                tint = Color.White.copy(alpha = 0.5f)
            )
        }

        // Status-Indikatoren
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            if (isHost) {
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text("Host")
                }
            }
            if (isTempHost) {
                Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                    Text("Temp Host")
                }
            }
            if (isModerator) {
                Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                    Text("Mod")
                }
            }
        }

        // Steuerelemente
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp)
        ) {
            IconButton(
                onClick = { onAction(StreamActionType.MUTE_AUDIO) }
            ) {
                Icon(
                    if (participant.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mikrofon",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = { onAction(StreamActionType.DISABLE_VIDEO) }
            ) {
                Icon(
                    if (participant.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    contentDescription = "Video",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun HostControls(
    isTemporaryHost: Boolean,
    onAction: (LiveStreamEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = { onAction(LiveStreamEvent.ToggleGuestRequests) }
        ) {
            Icon(Icons.Default.PersonAdd, "Gästeanfragen")
        }
        
        IconButton(
            onClick = { onAction(LiveStreamEvent.ToggleParticipantGrid) }
        ) {
            Icon(Icons.Default.Grid3x3, "Teilnehmer-Raster")
        }

        if (isTemporaryHost) {
            IconButton(
                onClick = { onAction(LiveStreamEvent.ReturnHostControl) }
            ) {
                Icon(Icons.Default.ExitToApp, "Host-Kontrolle zurückgeben")
            }
        }
    }
}

@Composable
fun GuestRequestsDialog(
    requests: List<GuestRequest>,
    onRequestAction: (GuestRequest, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gästeanfragen") },
        text = {
            LazyColumn {
                items(requests) { request ->
                    GuestRequestItem(
                        request = request,
                        onAccept = { onRequestAction(request, true) },
                        onReject = { onRequestAction(request, false) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}

@Composable
fun GuestRequestItem(
    request: GuestRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = "user_avatar_url", // Hier den tatsächlichen Avatar-URL verwenden
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(request.userId) // Hier den tatsächlichen Benutzernamen verwenden
        }
        
        Row {
            IconButton(onClick = onAccept) {
                Icon(Icons.Default.Check, "Akzeptieren", tint = Color.Green)
            }
            IconButton(onClick = onReject) {
                Icon(Icons.Default.Close, "Ablehnen", tint = Color.Red)
            }
        }
    }
}