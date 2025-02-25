package com.example.chittalk.data.status

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.media.MediaMetadataRetriever
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val statusDirectory: File
        get() = File(context.filesDir, "status").apply { mkdirs() }

    private val liveStreamDirectory: File
        get() = File(context.filesDir, "livestreams").apply { mkdirs() }

    suspend fun saveStatus(
        mediaUri: Uri,
        type: StatusType,
        caption: String? = null,
        duration: Long = 24 * 60 * 60 * 1000 // 24 Stunden in Millisekunden
    ): Status = withContext(Dispatchers.IO) {
        val fileName = "status_${System.currentTimeMillis()}"
        val file = File(statusDirectory, fileName)

        context.contentResolver.openInputStream(mediaUri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val thumbnail = when (type) {
            StatusType.IMAGE -> createImageThumbnail(mediaUri)
            StatusType.VIDEO -> createVideoThumbnail(mediaUri)
            StatusType.TEXT -> null
        }

        Status(
            id = UUID.randomUUID().toString(),
            userId = "current_user_id", // Aus Session holen
            type = type,
            mediaUrl = file.absolutePath,
            thumbnailUrl = thumbnail?.absolutePath,
            caption = caption,
            timestamp = System.currentTimeMillis(),
            duration = duration,
            views = 0,
            reactions = emptyList()
        )
    }

    suspend fun startLiveStream(): LiveStream = withContext(Dispatchers.IO) {
        val streamId = UUID.randomUUID().toString()
        val streamUrl = "rtmp://your-streaming-server/$streamId"

        LiveStream(
            id = streamId,
            userId = "current_user_id", // Aus Session holen
            streamUrl = streamUrl,
            startTime = System.currentTimeMillis(),
            viewers = 0,
            isActive = true
        )
    }

    suspend fun stopLiveStream(streamId: String) {
        // Implementierung zum Beenden des Livestreams
    }

    private suspend fun createImageThumbnail(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val fileName = "thumb_${System.currentTimeMillis()}.jpg"
            val thumbnailFile = File(statusDirectory, fileName)
            // Thumbnail-Erstellung implementieren
            thumbnailFile
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun createVideoThumbnail(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val fileName = "thumb_${System.currentTimeMillis()}.jpg"
            val thumbnailFile = File(statusDirectory, fileName)
            
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, uri)
                val bitmap = retriever.frameAtTime
                // Bitmap in Datei speichern
            }
            
            thumbnailFile
        } catch (e: Exception) {
            null
        }
    }

    fun getStatusUpdates(): Flow<List<Status>> = flow {
        // Implementierung zum Abrufen der Status-Updates
    }

    fun getLiveStreams(): Flow<List<LiveStream>> = flow {
        // Implementierung zum Abrufen aktiver Livestreams
    }
}

enum class StatusType {
    IMAGE, VIDEO, TEXT
}

data class Status(
    val id: String,
    val userId: String,
    val type: StatusType,
    val mediaUrl: String?,
    val thumbnailUrl: String?,
    val caption: String?,
    val timestamp: Long,
    val duration: Long,
    val views: Int,
    val reactions: List<Reaction>
)

data class LiveStream(
    val id: String,
    val userId: String,
    val streamUrl: String,
    val startTime: Long,
    val viewers: Int,
    val isActive: Boolean
)

data class Reaction(
    val userId: String,
    val type: String,
    val timestamp: Long
)