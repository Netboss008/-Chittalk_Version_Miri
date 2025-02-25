package com.example.chittalk.presentation.chats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    navController: NavController,
    viewModel: ChatsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                actions = {
                    IconButton(onClick = { /* Neuen Chat erstellen */ }) {
                        Icon(Icons.Filled.Add, "Neuer Chat")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Neuen Chat starten */ }) {
                Icon(Icons.Filled.Add, "Neuer Chat")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(state.chats) { chat ->
                ChatItem(
                    chat = chat,
                    onClick = { /* Chat öffnen */ }
                )
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: Chat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Chat Item UI
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = chat.lastMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = chat.timestamp,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

data class Chat(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: String
)