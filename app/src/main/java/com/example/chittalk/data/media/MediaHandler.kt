package com.example.chittalk.data.media

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Singleton
class MediaHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mediaDirectory: File
        get() = File(context.filesDir, "media").apply { mkdirs() }

    private val imageDirectory: File
        get() = File(mediaDirectory, "images").apply { mkdirs() }

    private val videoDirectory: File
        get() = File(mediaDirectory, "videos").apply { mkdirs() }

    private val fileDirectory: File
        get() = File(mediaDirectory, "files").apply { mkdirs() }

    private val thumbnailDirectory: File
        get() = File(mediaDirectory, "thumbnails").apply { mkdirs() }

    suspend fun saveImageToFile(bitmap: Bitmap, quality: Int = 85): MediaFile = withContext(Dispatchers.IO) {
        val fileName = generateFileName("IMG", "jpg")
        val file = File(imageDirectory, fileName)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        
        val thumbnail = createImageThumbnail(bitmap)
        val thumbnailFile = saveThumbnail(thumbnail, fileName)

        MediaFile(
            file = file,
            thumbnailFile = thumbnailFile,
            type = MediaType.IMAGE,
            mimeType = "image/jpeg",
            fileName = fileName
        )
    }

    suspend fun saveVideoToFile(uri: Uri): MediaFile = withContext(Dispatchers.IO) {
        val fileName = generateFileName("VID", "mp4")
        val file = File(videoDirectory, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val thumbnail = createVideoThumbnail(uri)
        val thumbnailFile = thumbnail?.let { saveThumbnail(it, fileName) }

        MediaFile(
            file = file,
            thumbnailFile = thumbnailFile,
            type = MediaType.VIDEO,
            mimeType = context.contentResolver.getType(uri) ?: "video/mp4",
            fileName = fileName
        )
    }

    suspend fun saveFile(uri: Uri): MediaFile = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "dat"
        val fileName = generateFileName("FILE", extension)
        val file = File(fileDirectory, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        MediaFile(
            file = file,
            type = MediaType.FILE,
            mimeType = mimeType ?: "application/octet-stream",
            fileName = fileName
        )
    }

    private suspend fun createImageThumbnail(bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.toFloat()
        
        val targetWidth = 300
        val targetHeight = (targetWidth / aspectRatio).toInt()
        
        Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private suspend fun createVideoThumbnail(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(
                    uri.toFile(),
                    android.util.Size(300, 300),
                    null
                )
            } else {
                MediaMetadataRetriever().use { retriever ->
                    retriever.setDataSource(context, uri)
                    retriever.frameAtTime
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveThumbnail(bitmap: Bitmap, originalFileName: String): File = withContext(Dispatchers.IO) {
        val thumbnailFileName = "thumb_$originalFileName"
        val thumbnailFile = File(thumbnailDirectory, thumbnailFileName)
        
        FileOutputStream(thumbnailFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
        }
        
        thumbnailFile
    }

    private fun generateFileName(prefix: String, extension: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val random = UUID.randomUUID().toString().substring(0, 8)
        return "${prefix}_${timestamp}_${random}.$extension"
    }

    suspend fun deleteMedia(mediaFile: MediaFile) = withContext(Dispatchers.IO) {
        mediaFile.file.delete()
        mediaFile.thumbnailFile?.delete()
    }

    fun getMediaFile(fileName: String): File? {
        return when {
            File(imageDirectory, fileName).exists() -> File(imageDirectory, fileName)
            File(videoDirectory, fileName).exists() -> File(videoDirectory, fileName)
            File(fileDirectory, fileName).exists() -> File(fileDirectory, fileName)
            else -> null
        }
    }

    fun getThumbnailFile(fileName: String): File? {
        val thumbnailFileName = "thumb_$fileName"
        return if (File(thumbnailDirectory, thumbnailFileName).exists()) {
            File(thumbnailDirectory, thumbnailFileName)
        } else null
    }
}

data class MediaFile(
    val file: File,
    val thumbnailFile: File? = null,
    val type: MediaType,
    val mimeType: String,
    val fileName: String
)

enum class MediaType {
    IMAGE, VIDEO, FILE
}

sealed class MediaResult {
    data class Success(val mediaFile: MediaFile) : MediaResult()
    data class Error(val message: String) : MediaResult()
}