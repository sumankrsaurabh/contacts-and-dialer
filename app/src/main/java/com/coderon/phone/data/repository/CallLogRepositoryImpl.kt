package com.coderon.phone.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.domain.repository.CallLogRepository
import com.coderon.phone.data.model.CallLog as CallLogData

class CallLogRepositoryImpl(
    private val contentResolver: ContentResolver
) : CallLogRepository {

    companion object {
        private const val TAG = "CallLogRepo"
        private const val CACHE_DURATION = 10 * 60 * 1000L // 10 minutes
    }

    private val contactsCache = mutableMapOf<String, Pair<Contact?, Long>>()

    override suspend fun getCallLogs(): List<CallLogData> {
        Log.d(TAG, "Loading call logs from device")

        val callLogs = mutableListOf<CallLogData>()

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE
        )

        val sortOrder = "${CallLog.Calls.DATE} DESC"

        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        if (cursor == null) {
            Log.e(TAG, "Call log query returned null cursor")
            return emptyList()
        }

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(CallLog.Calls._ID)
            val numberIndex = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val typeIndex = it.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val durationIndex = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            val dateIndex = it.getColumnIndexOrThrow(CallLog.Calls.DATE)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val rawNumber = it.getString(numberIndex) ?: continue
                val normalizedNumber = PhoneNumberUtils.normalizeNumber(rawNumber)

                val callType = mapCallType(it.getInt(typeIndex))
                val duration = it.getLong(durationIndex).toString()
                val date = it.getLong(dateIndex)

                val contact = getCachedOrFetchContact(normalizedNumber)

                callLogs.add(
                    CallLogData(
                        id = id,
                        contact = contact,
                        phoneNumber = rawNumber,
                        callType = callType,
                        callDuration = duration,
                        callTime = date
                    )
                )
            }
        }

        Log.d(TAG, "Call logs fetched successfully: ${callLogs.size} entries")
        return callLogs
    }

    override suspend fun addCallLog(callLog: CallLogData) {
        val values = ContentValues().apply {
            put(CallLog.Calls.NUMBER, callLog.phoneNumber)
            put(CallLog.Calls.TYPE, callLog.callType.ordinal)
            put(CallLog.Calls.DURATION, callLog.callDuration)
            put(CallLog.Calls.DATE, System.currentTimeMillis())
        }

        val resultUri = contentResolver.insert(CallLog.Calls.CONTENT_URI, values)
        if (resultUri != null) {
            Log.d(TAG, "Call log inserted successfully: ${callLog.phoneNumber}")
        } else {
            Log.e(TAG, "Failed to insert call log for: ${callLog.phoneNumber}")
        }
    }

    private fun mapCallType(type: Int): CallType {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
            CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
            CallLog.Calls.MISSED_TYPE -> CallType.MISSED
            CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
            else -> CallType.UNKNOWN
        }
    }

    private fun getCachedOrFetchContact(phoneNumber: String): Contact? {
        val currentTime = System.currentTimeMillis()
        val cached = contactsCache[phoneNumber]

        return if (cached != null && currentTime - cached.second < CACHE_DURATION) {
            cached.first
        } else {
            val contact = fetchContactFromContactsContract(phoneNumber)
            contactsCache[phoneNumber] = contact to currentTime
            contact
        }
    }

    private fun fetchContactFromContactsContract(phoneNumber: String): Contact? {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
            .appendPath(phoneNumber)
            .build()

        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI
        )

        return contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
                )
                val photoUri = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI)
                )

                Contact(
                    id = "", // Optional: could extract ID if needed
                    name = name ?: "Unknown",
                    profilePictureUrl = photoUri,
                    phoneNumber = phoneNumber
                )
            } else null
        }
    }
}
