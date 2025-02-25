package com.example.chittalk.presentation.status

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StatusScreen(
    viewModel: StatusViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status") },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(StatusEvent.StartLiveStream) }) {
                        Icon(Icons.Default.LiveTv, "Live Stream starten")
                    }
                    IconButton(onClick = { viewModel.onEvent(StatusEvent.CreateStatus) }) {
                        Icon(Icons.Default.Add, "Status erstellen")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mein Status
            item {
                MyStatusSection(
                    status = state.myStatus,
                    onCreateStatus = { viewModel.onEvent(StatusEvent.CreateStatus) }
                )
            }

            // Aktive Livestreams
            item {
                LiveStreamsSection(
                    streams = state.liveStreams,
                    onStreamClick = { viewModel.onEvent(StatusEvent.WatchLiveStream(it)) }
                )
            }

            // Kürzliche Updates
            item {
                Text(
                    "Kürzliche Updates",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(state.recentUpdates) { status ->
                StatusItem(
                    status = status,
                    onClick = { viewModel.onEvent(StatusEvent.ViewStatus(status)) }
                )
            }
        }
    }

    // Status Viewer
    if (state.selectedStatus != null) {
        StatusViewer(
            status = state.selectedStatus,
            onDismiss = { viewModel.onEvent(StatusEvent.CloseStatus) },
            onReaction = { status, reaction -> 
                viewModel.onEvent(StatusEvent.AddReaction(status, reaction))
            }
        )
    }
}

@Composable
fun MyStatusSection(
    status: Status?,
    onCreateStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .clickable(onClick = onCreateStatus),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (status != null) {
                AsyncImage(
                    model = status.thumbnailUrl ?: status.mediaUrl,
                    contentDescription = "Mein Status",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Status erstellen",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Mein Status",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = status?.let { 
                        SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(Date(status.timestamp))
                    } ?: "Tippe, um Status zu erstellen",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun LiveStreamsSection(
    streams: List<LiveStream>,
    onStreamClick: (LiveStream) -> Unit
) {
    if (streams.isNotEmpty()) {
        Column {
            Text(
                "Live Streams",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(streams) { stream ->
                    LiveStreamItem(
                        stream = stream,
                        onClick = { onStreamClick(stream) }
                    )
                }
            }
        }
    }
}

@Composable
fun LiveStreamItem(
    stream: LiveStream,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LiveTv,
                    contentDescription = "Live",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Live Stream",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${stream.viewers} Zuschauer",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun StatusItem(
    status: Status,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(status.userId) },
        supportingContent = { 
            Text(
                SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Date(status.timestamp))
            )
        },
        leadingContent = {
            AsyncImage(
                model = status.thumbnailUrl ?: status.mediaUrl,
                contentDescription = "Status Vorschau",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatusViewer(
    status: Status,
    onDismiss: () -> Unit,
    onReaction: (Status, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (status.type) {
            StatusType.IMAGE -> {
                AsyncImage(
                    model = status.mediaUrl,
                    contentDescription = "Status Bild",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            StatusType.VIDEO -> {
                // Video Player implementieren
            }
            StatusType.TEXT -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = status.caption ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Reaktionen
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { onReaction(status, "❤️") }
            ) {
                Icon(Icons.Default.Favorite, "Like")
            }
            IconButton(
                onClick = { onReaction(status, "👍") }
            ) {
                Icon(Icons.Default.ThumbUp, "Daumen hoch")
            }
            IconButton(
                onClick = { onReaction(status, "😮") }
            ) {
                Icon(Icons.Default.Star, "Wow")
            }
        }

        // Schließen Button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, "Schließen")
        }
    }
}