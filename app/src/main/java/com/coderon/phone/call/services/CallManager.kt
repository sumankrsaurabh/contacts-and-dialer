@file:Suppress("DEPRECATION")

package com.coderon.phone.call.services

import android.annotation.SuppressLint
import android.telecom.Call
import android.telecom.InCallService
import com.coderon.phone.call.domain.CallReducer
import com.coderon.phone.call.domain.CallSession
import com.coderon.phone.call.domain.toDomainState
import com.coderon.phone.call.ui.CallUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

@SuppressLint("StaticFieldLeak")
object CallManager {

    /** Active call sessions (keyed by stable call id) */
    private val sessions = mutableMapOf<String, CallSession>()

    private var inCallService: InCallService? = null

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    /* ------------------------------------------------
       SERVICE
    ------------------------------------------------ */

    fun setService(service: InCallService) {
        inCallService = service
    }

    /* ------------------------------------------------
       CALL LIFECYCLE
    ------------------------------------------------ */

    fun onCallAdded(call: Call) {
        val id = UUID.randomUUID().toString()

        val phoneNumber =
            call.details.handle?.schemeSpecificPart ?: "Unknown"

        val isIncoming = call.state == Call.STATE_RINGING

        sessions[id] = CallSession(
            id = id,
            call = call,
            state = call.state.toDomainState(),
            phoneNumber = phoneNumber,
            displayName = resolveContactName(phoneNumber),
            isIncoming = isIncoming
        )

        recompute()
    }


    fun onCallStateChanged(call: Call, newState: Int) {
        val id = call.stableId()
        val existing = sessions[id] ?: return

        sessions[id] = existing.copy(
            state = newState.toDomainState()
        )
        recompute()
    }

    fun onCallRemoved(call: Call) {
        sessions.remove(call.stableId())
        recompute()
    }

    /* ------------------------------------------------
       REDUCER
    ------------------------------------------------ */

    private fun recompute() {
        _uiState.value = CallReducer.reduce(
            sessions.values.toList(),
            _uiState.value
        )
    }

    /* ------------------------------------------------
       USER ACTIONS
    ------------------------------------------------ */

    fun accept() {
        sessions.values
            .firstOrNull { it.state.isIncoming }
            ?.call
            ?.answer(0)
    }

    fun reject() {
        sessions.values.firstOrNull()?.call?.disconnect()
    }

    fun hold() {
        sessions.values
            .firstOrNull { it.state.isActive }
            ?.call
            ?.hold()
    }

    fun unhold() {
        sessions.values
            .firstOrNull { it.state.isHolding }
            ?.call
            ?.unhold()
    }

    fun swap() {
        val active = sessions.values.firstOrNull { it.state.isActive }?.call
        val holding = sessions.values.firstOrNull { it.state.isHolding }?.call
        active?.hold()
        holding?.unhold()
    }

    fun mergeConference() {
        val primary = sessions.values.firstOrNull()?.call ?: return
        primary.conferenceableCalls.firstOrNull()?.let {
            primary.conference(it)
        }
    }

    fun endAll() {
        sessions.values.forEach { it.call.disconnect() }
    }
}

/* ------------------------------------------------
   EXTENSIONS
------------------------------------------------ */

/**
 * Stable call id for lifetime of Call object
 */
private fun Call.stableId(): String =
    System.identityHashCode(this).toString()

private fun resolveContactName(number: String): String? {
    // Plug your Contacts resolver here (already exists in your project)
    return null
}
