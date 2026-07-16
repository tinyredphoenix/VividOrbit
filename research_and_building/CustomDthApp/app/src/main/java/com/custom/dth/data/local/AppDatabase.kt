package com.custom.dth.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.custom.dth.features.history.HistoryDao
import com.custom.dth.features.history.WatchHistoryEntry

@Database(
    entities = [
        ChannelMeta::class,
        ChannelGroup::class,
        ChannelGroupMember::class,
        WatchHistoryEntry::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelMetaDao(): ChannelMetaDao
    abstract fun channelGroupDao(): ChannelGroupDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "channel_meta_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
