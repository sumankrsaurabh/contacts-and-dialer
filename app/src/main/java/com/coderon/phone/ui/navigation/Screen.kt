package com.coderon.phone.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable data object Keypad : Screen
    @Serializable data object Recent : Screen
    @Serializable data object Contacts : Screen
    @Serializable data object Search : Screen
    @Serializable data object Voicemail : Screen
    
    @Serializable 
    data class AddContact(
        val number: String? = null, 
        val contactId: String? = null
    ) : Screen

    @Serializable 
    data class CallDetails(val phoneNumber: String) : Screen
    
    @Serializable data object CallScreen : Screen
    @Serializable 
    data class PostCallSummary(
        val phoneNumber: String,
        val duration: Long,
        val isIncoming: Boolean,
        val timestamp: Long
    ) : Screen

    @Serializable data object Settings : Screen
    @Serializable data object BlockedNumbers : Screen
    @Serializable data object SpeedDial : Screen
}
