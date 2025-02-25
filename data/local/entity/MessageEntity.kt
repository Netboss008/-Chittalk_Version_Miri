package de.chittalk.messenger.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MessageEntity(
    @PrimaryKey
    val messageId: String,
    val text: String,
    val senderId: String,
    val chatId: String?,
    val timestamp: Long,
    val isRead: Boolean,
    val isDelivered: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)