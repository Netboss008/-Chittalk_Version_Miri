package de.chittalk.messenger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val username: String,
    val email: String,
    val imageUrl: String?,
    val bio: String?,
    val lastSeen: Long,
    val isOnline: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)