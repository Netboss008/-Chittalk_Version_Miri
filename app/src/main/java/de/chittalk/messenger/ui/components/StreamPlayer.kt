package de.chittalk.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.chittalk.messenger.R
import de.chittalk.messenger.ui.screens.stream.Stream
import de.chittalk.messenger.ui.theme.StreamIndicator

@Composable
fun StreamPlayer(
    stream: Stream,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
    ) {
        // Stream Preview/Video
        AsyncImage(
            model = stream.streamerImageUrl ?: R.drawable.default_stream_thumbnail,
            contentDescription = "Stream preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay with stream info
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            // Live indicator and viewer count
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                if (stream.isLive) {
                    LiveIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                ViewerCount(count = stream.viewerCount)
            }

            // Stream title and streamer info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = stream.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        imageUrl = stream.streamerImageUrl,
                        username = stream.streamerName,
                        size = 32,
                        isOnline = stream.isLive
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = stream.streamerName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = StreamIndicator,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.live),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Composable
private fun ViewerCount(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

@Composable
fun StreamControls(
    isStreaming: Boolean,
    onToggleStream: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onToggleStream,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isStreaming) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(
                    if (isStreaming) R.string.end_stream 
                    else R.string.start_stream
                )
            )
        }
    }
}