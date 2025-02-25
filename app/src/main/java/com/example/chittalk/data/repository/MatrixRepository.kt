package com.example.chittalk.data.repository

import android.net.Uri
import com.example.chittalk.data.media.MediaFile
import com.example.chittalk.data.media.MediaHandler
import com.example.chittalk.data.media.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.matrix.android.sdk.api.auth.AuthenticationService
import org.matrix.android.sdk.api.auth.data.HomeServerConnectionConfig
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.content.ContentAttachmentData
import org.matrix.android.sdk.api.session.events.model.EventType
import org.matrix.android.sdk.api.session.events.model.toModel
import org.matrix.android.sdk.api.session.room.model.message.MessageContent
import org.matrix.android.sdk.api.session.room.model.message.MessageType
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatrixRepository @Inject constructor(
    private val authenticationService: AuthenticationService,
    private val mediaHandler: MediaHandler
) {
    private var currentSession: Session? = null

    fun login(username: String, password: String): Flow<Result<Session>> = flow {
        try {
            val homeServerConfig = HomeServerConnectionConfig
                .Builder()
                .withHomeServerUri("https://whozapp.de")
                .build()

            val session = authenticationService.directAuthentication(
                homeServerConnectionConfig = homeServerConfig,
                userId = username,
                password = password,
                initialDeviceName = "Chittalk Android"
            )
            currentSession = session
            emit(Result.success(session))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getCurrentSession(): Session? = currentSession
    fun getCurrentUserId(): String? = currentSession?.myUserId

    fun logout() {
        currentSession?.signOut()
        currentSession = null
    }

    fun getRooms(): Flow<List<RoomSummary>> = flow {
        currentSession?.roomService()?.getRoomSummaries()?.let { rooms ->
            emit(rooms)
        } ?: emit(emptyList())
    }

    suspend fun createDirectRoom(userId: String): Result<String> = try {
        val roomId = currentSession?.roomService()?.createDirectRoom(userId)
        if (roomId != null) {
            Result.success(roomId)
        } else {
            Result.failure(Exception("Konnte Raum nicht erstellen"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun leaveRoom(roomId: String): Result<Unit> = try {
        currentSession?.roomService()?.getRoom(roomId)?.leave()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun observeRoomMessages(roomId: String): Flow<List<ChatMessage>> = flow {
        currentSession?.roomService()?.getRoom(roomId)?.timelineService()?.let { timeline ->
            timeline.start()
            timeline.getTimelineEvents().collect { events ->
                val messages = events.mapNotNull { event -> event.toChatMessage() }
                emit(messages)
            }
        }
    }

    private fun TimelineEvent.toChatMessage(): ChatMessage? {
        return when (root.getClearType()) {
            EventType.MESSAGE -> {
                val content = root.getClearContent()?.toModel<MessageContent>()
                when (content?.type) {
                    MessageType.MSGTYPE_TEXT -> TextMessage(
                        id = eventId,
                        senderId = root.senderId ?: "",
                        content = content.body,
                        timestamp = root.originServerTs ?: 0
                    )
                    MessageType.MSGTYPE_IMAGE -> MediaMessage(
                        id = eventId,
                        senderId = root.senderId ?: "",
                        type = MediaType.IMAGE,
                        url = content.url ?: "",
                        thumbnailUrl = content.thumbnailUrl,
                        fileName = content.body,
                        timestamp = root.originServerTs ?: 0
                    )
                    MessageType.MSGTYPE_VIDEO -> MediaMessage(
                        id = eventId,
                        senderId = root.senderId ?: "",
                        type = MediaType.VIDEO,
                        url = content.url ?: "",
                        thumbnailUrl = content.thumbnailUrl,
                        fileName = content.body,
                        timestamp = root.originServerTs ?: 0
                    )
                    MessageType.MSGTYPE_FILE -> MediaMessage(
                        id = eventId,
                        senderId = root.senderId ?: "",
                        type = MediaType.FILE,
                        url = content.url ?: "",
                        fileName = content.body,
                        timestamp = root.originServerTs ?: 0
                    )
                    else -> null
                }
            }
            else -> null
        }
    }

    suspend fun sendMessage(roomId: String, message: String): Result<Unit> = try {
        currentSession?.roomService()?.getRoom(roomId)?.sendService()?.sendTextMessage(message)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun sendMedia(roomId: String, mediaFile: MediaFile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val attachmentData = ContentAttachmentData(
                type = when (mediaFile.type) {
                    MediaType.IMAGE -> MessageType.MSGTYPE_IMAGE
                    MediaType.VIDEO -> MessageType.MSGTYPE_VIDEO
                    MediaType.FILE -> MessageType.MSGTYPE_FILE
                },
                file = mediaFile.file,
                fileName = mediaFile.fileName,
                mimeType = mediaFile.mimeType,
                thumbnailFile = mediaFile.thumbnailFile
            )

            currentSession?.roomService()?.getRoom(roomId)?.sendService()?.sendMedia(attachmentData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadMedia(url: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val file = currentSession?.contentService()?.downloadFile(url)
            if (file != null) {
                Result.success(file)
            } else {
                Result.failure(Exception("Mediendatei nicht gefunden"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoomInfo(roomId: String): RoomInfo? {
        return currentSession?.roomService()?.getRoom(roomId)?.let { room ->
            RoomInfo(
                roomId = roomId,
                displayName = room.roomSummary()?.displayName,
                avatarUrl = room.roomSummary()?.avatarUrl,
                topic = room.roomSummary()?.topic,
                memberCount = room.roomSummary()?.joinedMembersCount ?: 0
            )
        }
    }
}

sealed class ChatMessage {
    abstract val id: String
    abstract val senderId: String
    abstract val timestamp: Long
}

data class TextMessage(
    override val id: String,
    override val senderId: String,
    val content: String,
    override val timestamp: Long
) : ChatMessage()

data class MediaMessage(
    override val id: String,
    override val senderId: String,
    val type: MediaType,
    val url: String,
    val thumbnailUrl: String? = null,
    val fileName: String? = null,
    override val timestamp: Long
) : ChatMessage()

data class RoomInfo(
    val roomId: String,
    val displayName: String?,
    val avatarUrl: String?,
    val topic: String?,
    val memberCount: Int
)

data class RoomSummary(
    val roomId: String,
    val displayName: String?,
    val lastMessage: String?,
    val lastMessageTimestamp: Long,
    val unreadCount: Int
)