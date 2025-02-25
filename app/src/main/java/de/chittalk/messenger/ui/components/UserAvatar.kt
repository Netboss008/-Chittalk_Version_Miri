package de.chittalk.messenger.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import de.chittalk.messenger.ui.theme.StatusOnline
import de.chittalk.messenger.ui.theme.StatusOffline

@Composable
fun UserAvatar(
    imageUrl: String?,
    username: String,
    isOnline: Boolean = false,
    size: Int = 40,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (imageUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = imageUrl)
                        .build()
                ),
                contentDescription = "Profile picture of $username",
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Online Status Indicator
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size((size / 4).dp)
                    .clip(CircleShape)
                    .background(StatusOnline)
                    .border(1.dp, Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}