package de.chittalk.messenger.data.remote.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: String,
    val username: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val token: String,
    val userId: String,
    val username: String
)