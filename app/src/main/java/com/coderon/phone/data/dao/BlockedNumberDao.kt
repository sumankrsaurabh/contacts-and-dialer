package com.coderon.phone.data.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coderon.phone.data.model.BlockedNumber

@Dao
interface BlockedNumberDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBlockedNumber(blockedNumber: BlockedNumber)

    @Delete
    suspend fun removeBlockedNumber(blockedNumber: BlockedNumber)

    @Query("SELECT * FROM blocked_numbers")
    suspend fun getAllBlockedNumbers(): List<BlockedNumber>

    @Query("SELECT COUNT(*) FROM blocked_numbers WHERE phoneNumber = :phoneNumber")
    suspend fun isNumberBlocked(phoneNumber: String): Int
}
