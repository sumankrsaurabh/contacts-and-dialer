package com.coderon.phone.data

import androidx.compose.ui.graphics.ImageBitmap

data class RecentCall(
    val contactName: String,           // Name of the contact
    val phoneNumber: String,           // Phone number of the call
    val profilePicture: ImageBitmap?,  // Optional profile picture of the contact
    val callTime: String,              // Timestamp of the call
    val callType: CallType,            // Type of the call: Incoming, Outgoing, Missed
    val callDuration: String,          // Duration of the call
    val isVoicemail: Boolean = false,  // If the call was sent to voicemail
    val isRead: Boolean = true,        // Whether the missed call has been seen
    val callLocation: String? = null,  // Location (city, country) of the call
    val isBlocked: Boolean = false,    // If the call was from a blocked number
    val isVideoCall: Boolean = false,  // Indicates if it was a video call
    val simSlot: Int? = null,          // SIM slot used for the call (if dual SIM)
    val isEmergencyCall: Boolean = false // If it was an emergency call
) {
    // Enum for the type of call
    enum class CallType {
        INCOMING, OUTGOING, MISSED
    }
}
