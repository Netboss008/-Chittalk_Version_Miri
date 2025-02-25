package de.chittalk.messenger.data.remote.api

import de.chittalk.messenger.data.remote.model.*
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    // Chat
    @GET("messages")
    suspend fun getMessages(): List<MessageResponse>

    @POST("messages")
    suspend fun sendMessage(@Body message: MessageRequest): MessageResponse

    // Stream
    @GET("streams")
    suspend fun getStreams(): List<StreamResponse>

    @POST("streams")
    suspend fun createStream(@Body request: CreateStreamRequest): StreamResponse

    @DELETE("streams/{streamId}")
    suspend fun endStream(@Path("streamId") streamId: String)

    // Profile
    @GET("profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ProfileResponse

    @GET("profile/stats")
    suspend fun getProfileStats(): ProfileStatsResponse
}