package com.coderon.phone.utils

import android.content.ContentValues
import android.content.Context
import android.provider.CallLog
import android.util.Log

object CallLogger {
    fun logCall(context: Context, phoneNumber: String, callType: Int, duration: Long) {
        val values = ContentValues().apply {
            put(CallLog.Calls.NUMBER, phoneNumber)
            put(CallLog.Calls.DATE, System.currentTimeMillis())
            put(CallLog.Calls.DURATION, duration)
            put(CallLog.Calls.TYPE, callType)
            put(CallLog.Calls.NEW, 1)
        }
        context.contentResolver.insert(CallLog.Calls.CONTENT_URI, values)
        Log.d("CallLogger", "Call logged: $phoneNumber")
    }
}
