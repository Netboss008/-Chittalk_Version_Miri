package de.chittalk.messenger.data.repository

import de.chittalk.messenger.ui.screens.stream.Stream
import kotlinx.coroutines.flow.Flow

interface StreamRepository {
    fun getAvailableStreams(): Flow<List<Stream>>
    suspend fun startStream(title: String): Stream
    suspend fun endStream()
    suspend fun joinStream(streamId: String): Stream
    suspend fun leaveStream(streamId: String)
    suspend fun getStreamById(streamId: String): Stream?
    fun getViewerCount(streamId: String): Flow<Int>
}