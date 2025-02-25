package de.chittalk.messenger.data.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Boolean
    suspend fun register(username: String, email: String, password: String): Boolean
    suspend fun logout()
    suspend fun getCurrentUserId(): String?
    suspend fun isLoggedIn(): Boolean
    suspend fun forgotPassword(email: String)
}