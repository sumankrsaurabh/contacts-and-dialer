package com.coderon.phone.domain

import android.content.ContentResolver
import android.provider.CallLog
import android.util.Log
import com.coderon.phone.data.modal.CallType
import com.coderon.phone.data.modal.Contact
import com.coderon.phone.data.modal.CallLog as CallLogData

class CallLogRepositoryImpl(private val contentResolver: ContentResolver) : CallLogRepository {

    private val TAG = "CallLogRepositoryImpl"

    // Fetch all call logs from the device
    override suspend fun getCallLogs(): List<CallLogData> {
        Log.d(TAG, "Fetching call logs")
        val callLogs = mutableListOf<CallLogData>()

        // Define the columns you want to retrieve from the CallLog
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE
        )

        // Query the CallLog content provider
        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC"  // Order by most recent calls first
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(CallLog.Calls._ID)
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
            val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val phoneNumber = it.getString(numberIndex)
                val callType = when (it.getInt(typeIndex)) {
                    CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                    CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                    CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                    else -> CallType.MISSED
                }
                val callDuration = it.getString(durationIndex)
                val callTime = it.getLong(dateIndex)

                // At this stage, you might want to retrieve the contact associated with the phone number.
                // For simplicity, we'll leave the contact as null, but you can query ContactsContract
                // if needed to match the number with a contact.
                val contact: Contact? = null  // You can implement logic to match this with a Contact

                val callLog = CallLogData(
                    id = id,
                    contact = contact,
                    phoneNumber = phoneNumber,
                    callType = callType,
                    callDuration = callDuration,
                    callTime = callTime
                )

                callLogs.add(callLog)
                Log.d(TAG, "Fetched call log: $callLog")
            }
        } ?: Log.w(TAG, "Cursor returned null when querying call logs")

        Log.d(TAG, "Completed fetching call logs: Total = ${callLogs.size}")
        return callLogs
    }
}
