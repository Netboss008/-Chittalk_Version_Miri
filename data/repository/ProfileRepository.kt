package de.chittalk.messenger.data.repository

import de.chittalk.messenger.ui.screens.profile.UserActivity
import de.chittalk.messenger.ui.screens.profile.UserProfile
import de.chittalk.messenger.ui.screens.profile.UserStats
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getUserProfile(): UserProfile
    suspend fun getUserStats(): UserStats
    fun getRecentActivity(): Flow<List<UserActivity>>
    suspend fun updateProfile(username: String, bio: String?): UserProfile
    suspend fun updateProfileImage(imageUrl: String): UserProfile
    suspend fun followUser(userId: String)
    suspend fun unfollowUser(userId: String)
    fun getFollowers(): Flow<List<UserProfile>>
    fun getFollowing(): Flow<List<UserProfile>>
}