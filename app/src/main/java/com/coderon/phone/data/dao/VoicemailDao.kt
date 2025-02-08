package com.coderon.phone.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coderon.phone.data.model.Voicemail

@Dao
interface VoicemailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoicemail(voicemail: Voicemail)

    @Query("SELECT * FROM voicemails ORDER BY timestamp DESC")
    suspend fun getAllVoicemails(): List<Voicemail>

    @Delete
    suspend fun deleteVoicemail(voicemail: Voicemail)
}
