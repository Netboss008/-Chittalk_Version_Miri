package com.example.chittalk.presentation.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.chittalk.data.repository.ChatMessage
import com.example.chittalk.data.repository.MediaMessage
import com.example.chittalk.data.repository.TextMessage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    chatId: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(ChatEvent.SendImage(it)) }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(ChatEvent.SendVideo(it)) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(ChatEvent.SendFile(it)) }
    }

    LaunchedEffect(chatId) {
        viewModel.onEvent(ChatEvent.LoadChat(chatId))
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                chatName = state.chatName,
                chatAvatar = state.chatAvatar,
                memberCount = state.memberCount,
                onBackClick = { navController.navigateUp() },
                onVideoCallClick = { /* Video Call Implementation */ },
                onVoiceCallClick = { /* Voice Call Implementation */ },
                onMenuClick = { /* Menu Implementation */ }
            )
        },
        bottomBar = {
            ChatBottomBar(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSendClick = {
                    viewModel.onEvent(ChatEvent.SendMessage(messageText))
                    messageText = ""
                },
                onAttachmentClick = { showAttachmentOptions = !showAttachmentOptions },
                isRecording = state.isRecordingVoice,
                onStartRecording = { viewModel.onEvent(ChatEvent.StartRecordingVoice) },
                onStopRecording = { viewModel.onEvent(ChatEvent.StopRecordingVoice) },
                onCancelRecording = { viewModel.onEvent(ChatEvent.CancelRecordingVoice) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true
            ) {
                items(state.messages) { message ->
                    MessageItem(
                        message = message,
                        isOwnMessage = message.senderId == state.currentUserId,
                        onMediaClick = { 
                            if (message is MediaMessage) {
                                viewModel.onEvent(ChatEvent.DownloadMedia(message))
                            }
                        }
                    )
                }
            }

            if (showAttachmentOptions) {
                AttachmentOptionsMenu(
                    onDismiss = { showAttachmentOptions = false },
                    onImagePick = { imagePickerLauncher.launch("image/*") },
                    onVideoPick = { videoPickerLauncher.launch("video/*") },
                    onFilePick = { filePickerLauncher.launch("*/*") }
                )
            }

            if (state.isMediaUploading || state.isMediaDownloading) {
                LoadingOverlay()
            }
        }
    }

    // Error handling
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Show error message
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    chatName: String,
    chatAvatar: String?,
    memberCount: Int,
    onBackClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = chatAvatar,
                    contentDescription = "Chat Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = chatName)
                    Text(
                        text = "$memberCount Teilnehmer",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, "Zurück")
            }
        },
        actions = {
            IconButton(onClick = onVideoCallClick) {
                Icon(Icons.Default.VideoCall, "Video Anruf")
            }
            IconButton(onClick = onVoiceCallClick) {
                Icon(Icons.Default.Call, "Sprach Anruf")
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.MoreVert, "Menü")
            }
        }
    )
}

@Composable
fun ChatBottomBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachmentClick: () -> Unit,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAttachmentClick) {
                Icon(Icons.Default.AttachFile, "Anhang")
            }
            
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nachricht") },
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (messageText.isBlank()) {
                IconButton(
                    onClick = if (isRecording) onStopRecording else onStartRecording
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Aufnahme stoppen" else "Sprachnachricht"
                    )
                }
            } else {
                IconButton(onClick = onSendClick) {
                    Icon(Icons.Default.Send, "Senden")
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: ChatMessage,
    isOwnMessage: Boolean,
    onMediaClick: () -> Unit
) {
    val backgroundColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isOwnMessage) 64.dp else 8.dp,
                end = if (isOwnMessage) 8.dp else 64.dp,
                top = 4.dp,
                bottom = 4.dp
            ),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 16.dp
            ),
            color = backgroundColor
        ) {
            when (message) {
                is TextMessage -> TextMessageContent(message)
                is MediaMessage -> MediaMessageContent(message, onMediaClick)
            }
        }
    }
}

@Composable
fun TextMessageContent(message: TextMessage) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun MediaMessageContent(
    message: MediaMessage,
    onMediaClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onMediaClick)
            .padding(8.dp)
    ) {
        when (message.type) {
            MediaType.IMAGE -> {
                AsyncImage(
                    model = message.thumbnailUrl ?: message.url,
                    contentDescription = "Bild",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            MediaType.VIDEO -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = message.thumbnailUrl,
                        contentDescription = "Video Vorschau",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = "Abspielen",
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                    )
                }
            }
            MediaType.FILE -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.InsertDriveFile, "Datei")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = message.fileName ?: "Datei")
                }
            }
        }
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun AttachmentOptionsMenu(
    onDismiss: () -> Unit,
    onImagePick: () -> Unit,
    onVideoPick: () -> Unit,
    onFilePick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AttachmentOption(
                icon = Icons.Default.Image,
                text = "Bild",
                onClick = {
                    onImagePick()
                    onDismiss()
                }
            )
            AttachmentOption(
                icon = Icons.Default.VideoLibrary,
                text = "Video",
                onClick = {
                    onVideoPick()
                    onDismiss()
                }
            )
            AttachmentOption(
                icon = Icons.Default.AttachFile,
                text = "Datei",
                onClick = {
                    onFilePick()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun AttachmentOption(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text)
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}