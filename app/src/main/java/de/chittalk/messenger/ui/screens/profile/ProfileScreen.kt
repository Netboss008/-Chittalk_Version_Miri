package de.chittalk.messenger.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.chittalk.messenger.R
import de.chittalk.messenger.ui.components.UserAvatar

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Profile Header
        ProfileHeader(
            profile = profileState.profile,
            onEditProfile = { viewModel.startEditingProfile() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Section
        ProfileStats(stats = profileState.stats)

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Activity
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    items(profileState.recentActivity) { activity ->
                        ActivityItem(activity = activity)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = stringResource(R.string.settings))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = stringResource(R.string.logout))
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    profile: UserProfile,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserAvatar(
            imageUrl = profile.imageUrl,
            username = profile.username,
            size = 120,
            isOnline = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = profile.username,
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = profile.bio ?: "No bio yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(onClick = onEditProfile) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = stringResource(R.string.edit_profile))
        }
    }
}

@Composable
private fun ProfileStats(
    stats: UserStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            value = stats.followers,
            label = "Followers"
        )
        StatItem(
            value = stats.following,
            label = "Following"
        )
        StatItem(
            value = stats.totalStreams,
            label = "Streams"
        )
    }
}

@Composable
private fun StatItem(
    value: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActivityItem(
    activity: UserActivity,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (activity.type) {
                ActivityType.STREAM -> Icons.Default.Videocam
                ActivityType.CHAT -> Icons.Default.Chat
                ActivityType.FOLLOW -> Icons.Default.Person
            },
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp)
        )
        
        Column {
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = activity.timeAgo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}