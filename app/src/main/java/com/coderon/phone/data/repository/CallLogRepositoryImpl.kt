package com.coderon.phone.data.repository

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.telephony.SubscriptionManager
import android.util.Log
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.domain.repository.CallLogRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(loadCallLogs())
            }
        }

        contentResolver.registerContentObserver(
            CallLog.Calls.CONTENT_URI,
            true,
            observer
        )

        trySend(loadCallLogs())

        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }
    }

    override suspend fun getCallLogs(): List<CallLogData> {
        return loadCallLogs()
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
            CallLog.Calls.PHONE_ACCOUNT_ID
        )

        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        ) ?: return emptyList()

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(CallLog.Calls._ID)
            val numberIndex = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val typeIndex = it.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val durationIndex = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            val dateIndex = it.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val phoneAccountIdIndex = it.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val rawNumber = it.getString(numberIndex) ?: continue
                val normalized = PhoneNumberUtils.normalizeNumber(rawNumber)
                val phoneAccountId = if (phoneAccountIdIndex != -1) it.getString(phoneAccountIdIndex) else null

                callLogs.add(
                    CallLogData(
                        id = id,
                        phoneNumber = rawNumber,
                        callType = mapCallType(it.getInt(typeIndex)),
                        callDurationSeconds = it.getInt(durationIndex),
                        callTime = it.getLong(dateIndex),
                        contact = getCachedOrFetchContact(normalized),
                        simSlot = getSlotFromAccountId(phoneAccountId)
                    )
                )
            }
        }

        Log.d(TAG, "Loaded ${callLogs.size} call logs")
        return callLogs
    }

    @SuppressLint("MissingPermission")
    private fun getSlotFromAccountId(accountId: String?): Int {
        if (accountId == null) return 1
        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java) ?: return 1
        
        // On many devices, PHONE_ACCOUNT_ID in the call log matches the Subscription ID or ICCID
        val activeSubscriptions = subscriptionManager.activeSubscriptionInfoList ?: return 1
        
        // Try matching by subscriptionId string
        activeSubscriptions.firstOrNull { it.subscriptionId.toString() == accountId }?.let {
            return it.simSlotIndex + 1
        }
        
        // Try matching by ICCID (sometimes stored in PHONE_ACCOUNT_ID)
        activeSubscriptions.firstOrNull { it.iccId == accountId }?.let {
            return it.simSlotIndex + 1
        }

        return 1
    }

    override suspend fun addCallLog(callLog: CallLogData) {
        val values = ContentValues().apply {
            put(CallLog.Calls.NUMBER, callLog.phoneNumber)
            put(CallLog.Calls.TYPE, mapCallTypeToSystem(callLog.callType))
            put(CallLog.Calls.DURATION, callLog.callDurationSeconds)
            put(CallLog.Calls.DATE, currentTimeMillis())
            // Note: In a real app, we should also try to set PHONE_ACCOUNT_ID here 
            // if we want the system to know which SIM was used.
        }

        contentResolver.insert(CallLog.Calls.CONTENT_URI, values)
    }

    override suspend fun deleteCallLog(callLog: CallLogData) {
        contentResolver.delete(
            CallLog.Calls.CONTENT_URI,
            "${CallLog.Calls._ID}=?",
            arrayOf(callLog.id.toString())
        )
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

    private fun getCachedOrFetchContact(phoneNumber: String): Contact? {
        val now = currentTimeMillis()
        val cached = contactsCache[phoneNumber]

        return if (cached != null && now - cached.second < CACHE_DURATION) {
            cached.first
        } else {
            val contact = fetchContact(phoneNumber)
            contactsCache[phoneNumber] = contact to now
            contact
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
        } catch (e: Exception) {
            null
        }
    }
}
