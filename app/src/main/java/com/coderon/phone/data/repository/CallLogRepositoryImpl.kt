package com.coderon.phone.data.repository

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.telephony.SubscriptionManager
import android.util.Log
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.domain.repository.CallLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.System.currentTimeMillis
import com.coderon.phone.data.model.CallLog as CallLogData

class CallLogRepositoryImpl(
    private val contentResolver: ContentResolver,
    private val context: Context
) : CallLogRepository {

    companion object {
        private const val TAG = "CallLogRepo"
        private const val CACHE_DURATION = 10 * 60 * 1000L // 10 min
    }

    private val contactsCache = mutableMapOf<String, Pair<Contact?, Long>>()

    override fun observeCallLogs(): Flow<List<CallLogData>> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                launch {
                    trySend(loadCallLogs())
                }
            }
        }

        try {
            contentResolver.registerContentObserver(
                CallLog.Calls.CONTENT_URI,
                true,
                observer
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error registering observer", e)
        }

        launch {
            trySend(loadCallLogs())
        }

        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getCallLogs(): List<CallLogData> = withContext(Dispatchers.IO) {
        loadCallLogs()
    }

    @SuppressLint("Range")
    private fun loadCallLogs(): List<CallLogData> {
        val callLogs = mutableListOf<CallLogData>()

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
            CallLog.Calls.PHONE_ACCOUNT_ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.CACHED_PHOTO_URI
        )

        val cursor = try {
            contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC LIMIT 500"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error querying call logs", e)
            null
        } ?: return emptyList()

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(CallLog.Calls._ID)
            val numberIndex = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val typeIndex = it.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val durationIndex = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            val dateIndex = it.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val phoneAccountIdIndex = it.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)
            val cachedNameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val cachedPhotoIndex = it.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val rawNumber = it.getString(numberIndex) ?: continue
                val normalized = PhoneNumberUtils.normalizeNumber(rawNumber) ?: rawNumber
                val phoneAccountId =
                    if (phoneAccountIdIndex != -1) it.getString(phoneAccountIdIndex) else null
                val cachedName = if (cachedNameIndex != -1) it.getString(cachedNameIndex) else null
                val cachedPhoto =
                    if (cachedPhotoIndex != -1) it.getString(cachedPhotoIndex) else null

                callLogs.add(
                    CallLogData(
                        id = id,
                        phoneNumber = rawNumber,
                        callType = mapCallType(it.getInt(typeIndex)),
                        callDurationSeconds = it.getInt(durationIndex),
                        callTime = it.getLong(dateIndex),
                        contact = getCachedOrFetchContact(normalized, cachedName, cachedPhoto),
                        simSlot = getSlotFromAccountId(phoneAccountId)
                    )
                )
            }
        }

        return callLogs
    }

    @SuppressLint("MissingPermission")
    private fun getSlotFromAccountId(accountId: String?): Int {
        if (accountId == null) return 1
        val subscriptionManager =
            context.getSystemService(SubscriptionManager::class.java) ?: return 1

        val activeSubscriptions = try {
            subscriptionManager.activeSubscriptionInfoList
        } catch (e: SecurityException) {
            Log.e(TAG, "Error getting active subscriptions", e)
            null
        } ?: return 1

        activeSubscriptions.firstOrNull { it.subscriptionId.toString() == accountId }?.let {
            return it.simSlotIndex + 1
        }

        activeSubscriptions.firstOrNull { it.iccId == accountId }?.let {
            return it.simSlotIndex + 1
        }

        return 1
    }

    override suspend fun addCallLog(callLog: CallLogData) {
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put(CallLog.Calls.NUMBER, callLog.phoneNumber)
                put(CallLog.Calls.TYPE, mapCallTypeToSystem(callLog.callType))
                put(CallLog.Calls.DURATION, callLog.callDurationSeconds)
                put(CallLog.Calls.DATE, callLog.callTime)
            }

            try {
                contentResolver.insert(CallLog.Calls.CONTENT_URI, values)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding call log", e)
            }
        }
    }

    override suspend fun deleteCallLog(callLog: CallLogData) {
        withContext(Dispatchers.IO) {
            try {
                contentResolver.delete(
                    CallLog.Calls.CONTENT_URI,
                    "${CallLog.Calls._ID}=?",
                    arrayOf(callLog.id.toString())
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting call log", e)
            }
        }
    }

    private fun mapCallType(type: Int): CallType = when (type) {
        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
        CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
        CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
        else -> CallType.UNKNOWN
    }

    private fun mapCallTypeToSystem(type: CallType): Int = when (type) {
        CallType.INCOMING -> CallLog.Calls.INCOMING_TYPE
        CallType.OUTGOING -> CallLog.Calls.OUTGOING_TYPE
        CallType.MISSED -> CallLog.Calls.MISSED_TYPE
        CallType.REJECTED -> CallLog.Calls.REJECTED_TYPE
        CallType.BLOCKED -> CallLog.Calls.BLOCKED_TYPE
        else -> CallLog.Calls.OUTGOING_TYPE
    }

    private fun getCachedOrFetchContact(
        phoneNumber: String,
        cachedName: String?,
        cachedPhoto: String?
    ): Contact? {
        val now = currentTimeMillis()
        val cached = contactsCache[phoneNumber]

        if (cached != null && now - cached.second < CACHE_DURATION) {
            return cached.first
        } else {
            val contact = fetchContact(phoneNumber) ?: if (cachedName != null) {
                Contact(
                    displayName = cachedName,
                    profilePictureUrl = cachedPhoto,
                    phoneNumbers = listOf(PhoneNumber(phoneNumber))
                )
            } else null

            contactsCache[phoneNumber] = contact to now
            return contact
        }
    }

    private fun fetchContact(phoneNumber: String): Contact? {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI
            .buildUpon()
            .appendPath(phoneNumber)
            .build()

        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI
        )

        return try {
            contentResolver.query(uri, projection, null, null, null)?.use {
                if (!it.moveToFirst()) return null

                Contact(
                    id = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID)),
                    displayName = it.getString(
                        it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    ) ?: "Unknown",
                    profilePictureUrl = it.getString(
                        it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI)
                    ),
                    phoneNumbers = listOf(
                        PhoneNumber(
                            number = phoneNumber,
                            isPrimary = true
                        )
                    )
                )
            }
        } catch (_: Exception) {
            null
        }
    }
}
