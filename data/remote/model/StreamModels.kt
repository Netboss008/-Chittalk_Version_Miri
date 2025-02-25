package de.chittalk.messenger.data.remote.model

data class StreamResponse(
    val id: String,
    val title: String,
    val streamerId: String,
    val streamerName: String,
    val streamerImageUrl: String?,
    val viewerCount: Int,
    val isLive: Boolean,
    val startedAt: Long?
)

data class CreateStreamRequest(
    val title: String
)

data class StreamViewerResponse(
    val streamId: String,
    val viewerCount: Int,
    val viewers: List<String>
)