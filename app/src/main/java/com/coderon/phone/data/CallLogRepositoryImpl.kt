package com.coderon.phone.data

import android.content.ContentResolver
import android.content.ContentValues
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import com.coderon.phone.data.modal.CallType
import com.coderon.phone.data.modal.Contact
import com.coderon.phone.domain.CallLogRepository
import com.coderon.phone.data.modal.CallLog as CallLogData

class CallLogRepositoryImpl(private val contentResolver: ContentResolver) : CallLogRepository {

    private val TAG = "CallLogRepositoryImpl"
    private val CACHE_DURATION = 10 * 60 * 1000L  // 10 minutes in milliseconds
    private val contactsCache = mutableMapOf<String, Pair<Contact?, Long>>() // Cache with expiration time

    override suspend fun getCallLogs(): List<CallLogData> {
        Log.d(TAG, "Fetching call logs")
        val callLogs = mutableListOf<CallLogData>()

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE
        )

        contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(CallLog.Calls._ID)
            val numberIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val typeIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val durationIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            val dateIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val rawPhoneNumber = cursor.getString(numberIndex)
                val normalizedPhoneNumber = PhoneNumberUtils.normalizeNumber(rawPhoneNumber)

                val callType = when (cursor.getInt(typeIndex)) {
                    CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                    CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                    CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                    CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED // Handle rejected calls
                    else -> CallType.UNKNOWN
                }
                val callDuration = cursor.getLong(durationIndex).toString()
                val callTime = cursor.getLong(dateIndex)

                val contact = getCachedOrFetchContact(normalizedPhoneNumber)

                callLogs.add(
                    CallLogData(
                        id = id,
                        contact = contact,
                        phoneNumber = rawPhoneNumber,
                        callType = callType,
                        callDuration = callDuration,
                        callTime = callTime
                    )
                )
            }
        } ?: Log.w(TAG, "Cursor returned null when querying call logs")

        Log.d(TAG, "Completed fetching call logs: Total = ${callLogs.size}")
        return callLogs
    }

    override suspend fun saveCallLog(phoneNumber: String, callType: CallType, duration: Long) {
        val values = ContentValues().apply {
            put(CallLog.Calls.NUMBER, phoneNumber)
            put(CallLog.Calls.TYPE, callType.ordinal)
            put(CallLog.Calls.DURATION, duration)
            put(CallLog.Calls.DATE, System.currentTimeMillis())
        }

        contentResolver.insert(CallLog.Calls.CONTENT_URI, values)
        Log.d(TAG, "Saved call log for number: $phoneNumber")
    }

    // Cache handling with expiration logic
    private fun getCachedOrFetchContact(phoneNumber: String): Contact? {
        val now = System.currentTimeMillis()
        val cachedContact = contactsCache[phoneNumber]
        return if (cachedContact != null && now - cachedContact.second < CACHE_DURATION) {
            cachedContact.first
        } else {
            val contact = fetchContactInfo(phoneNumber)
            contactsCache[phoneNumber] = contact to now
            contact
        }
    }

    // Function to fetch contact info from ContactsContract
    private fun fetchContactInfo(phoneNumber: String): Contact? {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
            .appendPath(phoneNumber)
            .build()

        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI
        )

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                val photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI))

                return Contact(
                    id = 0L,  // ID can be fetched if needed
                    name = name,
                    profilePictureUrl = photoUri,
                    phoneNumber = phoneNumber
                )
            }
        }
        return null
    }
}
