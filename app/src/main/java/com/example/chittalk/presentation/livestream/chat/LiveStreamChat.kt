package com.example.chittalk.presentation.livestream.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chittalk.data.livestream.chat.*

@Composable
fun LiveStreamChat(
    messages: List<StreamChatMessage>,
    userRole: StreamUserRole,
    onSendMessage: (String) -> Unit,
    onSendGift: (String, Int) -> Unit,
    onSendSticker: (String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onPinMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var chatInput by remember { mutableStateOf("") }
    var showGiftMenu by remember { mutableStateOf(false) }
    var showStickerMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight(0.8f)
            .background(Color.Black.copy(alpha = 0.6f))
    ) {
        // Chat-Nachrichten
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                ChatMessage(
                    message = message,
                    userRole = userRole,
                    onDelete = { onDeleteMessage(message.id) },
                    onPin = { onPinMessage(message.id) }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Eingabebereich
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
        ) {
            // Gepinnte Nachricht
            messages.find { it.isPinned }?.let { pinnedMessage ->
                PinnedMessage(
                    message = pinnedMessage,
                    onUnpin = { onDeleteMessage(pinnedMessage.id) }
                )
            }

            // Eingabezeile
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showStickerMenu = true }) {
                    Icon(Icons.Default.EmojiEmotions, "Sticker")
                }
                IconButton(onClick = { showGiftMenu = true }) {
                    Icon(Icons.Default.CardGiftcard, "Geschenke")
                }
                TextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nachricht schreiben...") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (chatInput.isNotEmpty()) {
                                onSendMessage(chatInput)
                                chatInput = ""
                            }
                        }
                    )
                )
                IconButton(
                    onClick = {
                        if (chatInput.isNotEmpty()) {
                            onSendMessage(chatInput)
                            chatInput = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, "Senden")
                }
            }
        }
    }

    // Gift-Menü
    if (showGiftMenu) {
        GiftMenu(
            onGiftSelected = { gift, amount ->
                onSendGift(gift, amount)
                showGiftMenu = false
            },
            onDismiss = { showGiftMenu = false }
        )
    }

    // Sticker-Menü
    if (showStickerMenu) {
        StickerMenu(
            onStickerSelected = { stickerId ->
                onSendSticker(stickerId)
                showStickerMenu = false
            },
            onDismiss = { showStickerMenu = false }
        )
    }
}

@Composable
fun ChatMessage(
    message: StreamChatMessage,
    userRole: StreamUserRole,
    onDelete: () -> Unit,
    onPin: () -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            AsyncImage(
                model = message.userAvatar,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(8.dp))

            // Benutzername und Rolle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.userName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (message.userRole) {
                        StreamUserRole.HOST -> Color.Red
                        StreamUserRole.MODERATOR -> Color.Blue
                        StreamUserRole.VIP -> Color.Green
                        else -> Color.White
                    }
                )
                if (message.userRole != StreamUserRole.VIEWER) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Badge {
                        Text(message.userRole.name)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Nachrichteninhalt
            when (val content = message.content) {
                is ChatContent.Text -> Text(
                    text = content.message,
                    color = Color.White
                )
                is ChatContent.Gift -> GiftMessage(content)
                is ChatContent.Sticker -> StickerMessage(content)
                is ChatContent.SystemMessage -> SystemMessage(content)
            }

            // Moderations-Optionen
            if (userRole in listOf(StreamUserRole.HOST, StreamUserRole.MODERATOR)) {
                IconButton(onClick = { showOptions = !showOptions }) {
                    Icon(Icons.Default.MoreVert, "Optionen")
                }
                
                DropdownMenu(
                    expanded = showOptions,
                    onDismissRequest = { showOptions = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nachricht löschen") },
                        onClick = {
                            onDelete()
                            showOptions = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Nachricht anpinnen") },
                        onClick = {
                            onPin()
                            showOptions = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GiftMessage(gift: ChatContent.Gift) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CardGiftcard, "Geschenk")
        Spacer(modifier = Modifier.width(4.dp))
        Text("hat ${gift.amount}x ${gift.giftType} geschenkt!")
    }
}

@Composable
fun StickerMessage(sticker: ChatContent.Sticker) {
    AsyncImage(
        model = sticker.stickerId, // Sticker-URL
        contentDescription = "Sticker",
        modifier = Modifier.size(64.dp)
    )
}

@Composable
fun SystemMessage(message: ChatContent.SystemMessage) {
    Text(
        text = message.message,
        color = Color.Yellow,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
fun PinnedMessage(
    message: StreamChatMessage,
    onUnpin: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.PushPin, "Angepinnt")
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message.userName,
                style = MaterialTheme.typography.bodySmall
            )
            when (val content = message.content) {
                is ChatContent.Text -> Text(content.message)
                is ChatContent.Gift -> Text("Hat ein Geschenk gesendet")
                is ChatContent.Sticker -> Text("Hat einen Sticker gesendet")
                is ChatContent.SystemMessage -> Text(content.message)
            }
        }
        IconButton(onClick = onUnpin) {
            Icon(Icons.Default.Close, "Entfernen")
        }
    }
}