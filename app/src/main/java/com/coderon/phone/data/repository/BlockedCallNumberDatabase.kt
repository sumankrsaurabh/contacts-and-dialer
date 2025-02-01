package com.coderon.phone.data.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BlockedNumberDatabase(context: Context) :
    SQLiteOpenHelper(context, "BlockedNumbers.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE BlockedNumbers (id INTEGER PRIMARY KEY, phoneNumber TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS BlockedNumbers")
        onCreate(db)
    }

    fun addBlockedNumber(phoneNumber: String) {
        writableDatabase.execSQL("INSERT INTO BlockedNumbers (phoneNumber) VALUES ('$phoneNumber')")
    }

    fun removeBlockedNumber(phoneNumber: String) {
        writableDatabase.execSQL("DELETE FROM BlockedNumbers WHERE phoneNumber = '$phoneNumber'")
    }

    fun isBlocked(phoneNumber: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM BlockedNumbers WHERE phoneNumber = '$phoneNumber'", null
        )
        val blocked = cursor.count > 0
        cursor.close()
        return blocked
    }
}
