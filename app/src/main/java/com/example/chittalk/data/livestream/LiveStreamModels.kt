package com.example.chittalk.data.livestream

import java.util.UUID

data class LiveStream(
    val id: String = UUID.randomUUID().toString(),
    val hostId: String,
    val temporaryHostId: String? = null,
    val title: String,
    val description: String? = null,
    val isPrivate: Boolean = false, // nur für Kontakte
    val startTime: Long,
    val viewers: Int = 0,
    val likes: Int = 0,
    val participants: List<Participant> = emptyList(),
    val moderators: List<Moderator> = emptyList(),
    val bannedUsers: List<String> = emptyList(), // Liste von User-IDs
    val settings: StreamSettings = StreamSettings(),
    val status: StreamStatus = StreamStatus.LIVE
)

data class Participant(
    val userId: String,
    val role: ParticipantRole = ParticipantRole.VIEWER,
    val isMuted: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val gridPosition: Int? = null // Position im 3x3 Raster (0-8)
)

data class Moderator(
    val userId: String,
    val canManageParticipants: Boolean = true,
    val canManageChat: Boolean = true,
    val canTakeOverStream: Boolean = false
)

data class StreamSettings(
    val maxParticipants: Int = 8,
    val allowGuestRequests: Boolean = true,
    val allowChat: Boolean = true,
    val allowReactions: Boolean = true,
    val allowModeratorTakeover: Boolean = false
)

enum class StreamStatus {
    LIVE,
    PAUSED,
    ENDED
}

enum class ParticipantRole {
    HOST,
    TEMP_HOST,
    MODERATOR,
    GUEST,
    VIEWER
}

data class GuestRequest(
    val userId: String,
    val timestamp: Long,
    val status: RequestStatus = RequestStatus.PENDING
)

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class StreamAction(
    val type: StreamActionType,
    val targetUserId: String,
    val performedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class StreamActionType {
    MUTE_AUDIO,
    UNMUTE_AUDIO,
    DISABLE_VIDEO,
    ENABLE_VIDEO,
    PROMOTE_TO_MODERATOR,
    DEMOTE_FROM_MODERATOR,
    REMOVE_FROM_STREAM,
    BAN_USER,
    UNBAN_USER,
    TRANSFER_HOST,
    TAKE_BACK_HOST
}