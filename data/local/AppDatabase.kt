package de.chittalk.messenger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.chittalk.messenger.data.local.dao.MessageDao
import de.chittalk.messenger.data.local.dao.UserDao
import de.chittalk.messenger.data.local.entity.MessageEntity
import de.chittalk.messenger.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "chittalk.db"
    }
}

class Converters {
    // Hier können später bei Bedarf TypeConverter hinzugefügt werden
}