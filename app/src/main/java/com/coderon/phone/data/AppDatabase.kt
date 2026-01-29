package com.coderon.phone.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.coderon.phone.data.dao.BlockedNumberDao
import com.coderon.phone.data.dao.VoicemailDao
import com.coderon.phone.data.model.BlockedNumber
import com.coderon.phone.data.model.Voicemail

@Database(entities = [BlockedNumber::class, Voicemail::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun voicemailDao(): VoicemailDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "phone_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
