package com.coderon.phone.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

suspend fun readRecentCalls(context: Context): List<RecentCall> {
    return withContext(Dispatchers.IO) {
        val recentCalls = mutableListOf<RecentCall>()
        Log.d("Working", "Function is called")

        // Query the CallLog content provider
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.PHONE_ACCOUNT_ID,
                CallLog.Calls.VOICEMAIL_URI
            ),
            null, null, CallLog.Calls.DATE + " DESC"
        )

        cursor?.use { c ->
            val nameIndex = c.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberIndex = c.getColumnIndex(CallLog.Calls.NUMBER)
            val typeIndex = c.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = c.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = c.getColumnIndex(CallLog.Calls.DURATION)
            val simSlotIndex = c.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)
            val voicemailFlagIndex = c.getColumnIndex(CallLog.Calls.VOICEMAIL_URI)

            while (c.moveToNext()) {
                val contactName = c.getString(nameIndex) ?: "Unknown"
                val phoneNumber = c.getString(numberIndex) ?: "Unknown"
                val callType = when (c.getInt(typeIndex)) {
                    CallLog.Calls.INCOMING_TYPE -> RecentCall.CallType.INCOMING
                    CallLog.Calls.OUTGOING_TYPE -> RecentCall.CallType.OUTGOING
                    CallLog.Calls.MISSED_TYPE -> RecentCall.CallType.MISSED
                    else -> RecentCall.CallType.MISSED
                }
                val callTime = c.getString(dateIndex) ?: "Unknown"
                val callDuration = c.getString(durationIndex) ?: "Unknown"

                val isBlocked = c.getInt(typeIndex) == CallLog.Calls.BLOCKED_TYPE
                val isVoicemail = c.getString(voicemailFlagIndex) != null
                val simSlot = c.getInt(simSlotIndex)

                val callLocation: String? = null // Placeholder for location logic
                val isEmergencyCall = phoneNumber == "911" // Example for emergency calls

                val profilePicture = getContactPhoto(context, phoneNumber)

                recentCalls.add(
                    RecentCall(
                        contactName = contactName,
                        phoneNumber = phoneNumber,
                        profilePicture = profilePicture,
                        callTime = callTime,
                        callType = callType,
                        callDuration = callDuration,
                        isVoicemail = isVoicemail,
                        isRead = callType != RecentCall.CallType.MISSED,
                        callLocation = callLocation,
                        isBlocked = isBlocked,
                        isVideoCall = false, // Placeholder for future video call logic
                        simSlot = simSlot,
                        isEmergencyCall = isEmergencyCall
                    )
                )
            }
        }

        recentCalls
    }
}

// Function to retrieve the contact's photo based on phone number
@SuppressLint("Range")
private fun getContactPhoto(context: Context, phoneNumber: String): ImageBitmap? {
    val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI.buildUpon().appendPath(phoneNumber).build()
    val cursor = context.contentResolver.query(
        uri,
        arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID),
        null, null, null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            val contactId = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
            val contactUri = ContactsContract.Contacts.CONTENT_URI.buildUpon().appendPath(contactId).build()
            val inputStream: InputStream? = ContactsContract.Contacts.openContactPhotoInputStream(context.contentResolver, contactUri)

            inputStream?.use { stream ->
                val bitmap: Bitmap = BitmapFactory.decodeStream(stream)
                return bitmap.asImageBitmap()
            }
        }
    }

    return null
}
