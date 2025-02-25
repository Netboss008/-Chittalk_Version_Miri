package de.chittalk.messenger.ui.screens.stream

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.chittalk.messenger.R
import de.chittalk.messenger.ui.components.StreamPlayer
import de.chittalk.messenger.ui.components.UserAvatar

@Composable
fun StreamScreen(
    viewModel: StreamViewModel = hiltViewModel(),
    onStartStream: () -> Unit
) {
    val streamState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Active Stream Section
        if (streamState.activeStream != null) {
            StreamPlayer(
                stream = streamState.activeStream,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Stream List
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Live Streams",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(streamState.availableStreams) { stream ->
                        StreamListItem(
                            stream = stream,
                            onClick = { viewModel.joinStream(stream.id) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Start Stream Button
        Button(
            onClick = onStartStream,
            modifier = Modifier.fillMaxWidth(),
            enabled = !streamState.isStreaming
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = stringResource(R.string.start_stream))
        }
    }
}

@Composable
fun StreamListItem(
    stream: Stream,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                imageUrl = stream.streamerImageUrl,
                username = stream.streamerName,
                isOnline = true
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stream.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stream.streamerName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            StreamViewerCount(
                count = stream.viewerCount,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun StreamViewerCount(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}