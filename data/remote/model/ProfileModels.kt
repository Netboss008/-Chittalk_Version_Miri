package de.chittalk.messenger.data.remote.model

data class ProfileResponse(
    val userId: String,
    val username: String,
    val email: String,
    val imageUrl: String?,
    val bio: String?,
    val createdAt: Long
)

data class UpdateProfileRequest(
    val username: String?,
    val bio: String?,
    val imageUrl: String?
)

data class ProfileStatsResponse(
    val userId: String,
    val followers: Int,
    val following: Int,
    val totalStreams: Int,
    val totalMessages: Int
)