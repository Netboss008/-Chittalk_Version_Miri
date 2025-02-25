package de.chittalk.messenger.data.local.dao

import androidx.room.*
import de.chittalk.messenger.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE userId = :userId")
    suspend fun updateUserStatus(userId: String, isOnline: Boolean, lastSeen: Long)

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<UserEntity>>
}