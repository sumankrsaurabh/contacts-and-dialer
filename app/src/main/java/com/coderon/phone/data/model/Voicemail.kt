package com.coderon.phone.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voicemails")
data class Voicemail(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callerNumber: String,
    val filePath: String,
    val timestamp: Long
)
