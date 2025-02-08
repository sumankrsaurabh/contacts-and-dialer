package com.coderon.phone.data.repository

import com.coderon.phone.data.dao.BlockedNumberDao
import com.coderon.phone.data.model.BlockedNumber

class BlockedNumberRepository(private val blockedNumberDao: BlockedNumberDao) {

    suspend fun blockNumber(phoneNumber: String) {
        blockedNumberDao.addBlockedNumber(BlockedNumber(phoneNumber))
    }

    suspend fun unblockNumber(phoneNumber: String) {
        blockedNumberDao.removeBlockedNumber(BlockedNumber(phoneNumber))
    }

    suspend fun isBlocked(phoneNumber: String): Boolean {
        return blockedNumberDao.isNumberBlocked(phoneNumber) > 0
    }

    suspend fun getAllBlockedNumbers(): List<BlockedNumber> {
        return blockedNumberDao.getAllBlockedNumbers()
    }
}
