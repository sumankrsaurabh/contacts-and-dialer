@file:Suppress("DEPRECATION")

package com.coderon.phone.call.services

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.telecom.VideoProfile
import android.util.Log
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.utils.extentions.AudioRoute
import com.coderon.phone.ui.utils.extentions.State
import com.coderon.phone.ui.utils.extentions.getCallState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
object CallManager {
    private const val TAG = "CallManager"

    var inCallService: InCallService? = null
    private val calls = mutableListOf<Call>()

    /** StateFlow for real-time call updates **/
    private val _phoneState = MutableStateFlow<PhoneState>(NoCall)
    val phoneState: StateFlow<PhoneState> = _phoneState.asStateFlow()

    /** StateFlow for real-time call updates **/
    private val _callState = MutableStateFlow<State>(State.IDLE)
    val callState: StateFlow<State> = _callState.asStateFlow()

    /** StateFlow for current audio state updates **/
    private val _currentAudioRoute = MutableStateFlow(CallAudioState.ROUTE_EARPIECE)
    val currentAudioRoute: StateFlow<Int> = _currentAudioRoute.asStateFlow()


    /** SharedFlow for one-time call events **/
    private val _callEvents = MutableSharedFlow<String>()
    val callEvents = _callEvents.asSharedFlow()

    private val _isMuted = MutableStateFlow(
        inCallService?.callAudioState?.isMuted == true
    )
    val isMuted = _isMuted.asStateFlow()

    fun toggleMute() {
        inCallService?.setMuted(_isMuted.value)
    }

    /** Adds a new call and registers callbacks **/
    fun addCall(call: Call, service: InCallService) {
        Log.d(TAG, "Adding call: ${call.details.handle}, State: ${call.state}")
        calls.add(call)
        inCallService = service
        _callState.update {
            updateCallState(call.getCallState())
        }
        updateState()

        call.registerCallback(object : Call.Callback() {
            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(call: Call, state: Int) {
                Log.d(TAG, "Call state changed: ${call.details.handle}, New State: $state")
                _callState.value = updateCallState(state)

                when (state) {
                    Call.STATE_ACTIVE -> startCallDurationTracking()  // Start duration tracking
                    Call.STATE_DISCONNECTED, Call.STATE_DISCONNECTING -> stopCallDurationTracking() // Stop tracking
                }
            }

            override fun onDetailsChanged(call: Call, details: Call.Details) {
                Log.d(TAG, "Call details changed: ${call.details.handle}")
                updateState()
            }

            override fun onConferenceableCallsChanged(
                call: Call, conferenceableCalls: MutableList<Call>
            ) {
                Log.d(
                    TAG,
                    "Conferenceable calls changed: ${call.details.handle}, Available: ${conferenceableCalls.size}"
                )
                updateState()
            }
        })
    }

    /** Removes a call and updates state **/
    fun removeCall(call: Call) {
        Log.d(TAG, "Removing call: ${call.details.handle}")
        calls.remove(call)
        updateState()
    }

    /** Updates the call audio state **/


    fun updateAudioState(audioState: CallAudioState) {
        Log.d(TAG, "Audio state changed: Route=${audioState.route}, Muted=${audioState.isMuted}")
        _currentAudioRoute.value = audioState.route
        _isMuted.value = audioState.isMuted
    }

    /** Switch between audio routes */
    fun switchAudioRoute(route: AudioRoute) {
        inCallService?.let { service ->
            val availableRoutes = service.callAudioState.supportedRouteMask
            val isEarphoneAvailable = availableRoutes and AudioRoute.WIRED_HEADSET.value != 0
            val isBluetoothAvailable = availableRoutes and AudioRoute.BLUETOOTH.value != 0

            val targetRoute = when (route) {
                AudioRoute.BLUETOOTH -> if (isBluetoothAvailable) AudioRoute.BLUETOOTH.value else _currentAudioRoute.value
                AudioRoute.SPEAKER -> AudioRoute.SPEAKER.value
                AudioRoute.EARPIECE -> if (isEarphoneAvailable) AudioRoute.WIRED_HEADSET.value else AudioRoute.EARPIECE.value
                AudioRoute.WIRED_HEADSET -> if (isEarphoneAvailable) AudioRoute.WIRED_HEADSET.value else _currentAudioRoute.value
            }

            if (availableRoutes and targetRoute != 0) {
                Log.d(TAG, "Switching audio route to: $targetRoute")
                service.setAudioRoute(targetRoute)
                _currentAudioRoute.value = targetRoute
            } else {
                Log.w(TAG, "Requested audio route $targetRoute is not supported")
            }
        } ?: Log.e(TAG, "InCallService is null, cannot switch audio route")
    }


    /** Updates the phone state **/
    private fun updateState() {
        // Clean up disconnected calls
        val activeCalls = calls.filter { it.state != Call.STATE_DISCONNECTED }
        calls.clear()
        calls.addAll(activeCalls)

        // Log the current call count after cleaning up disconnected calls
        Log.d(TAG, "Updated state: Active calls=${calls.size}")

        // Determine the phone state based on the calls left
        _phoneState.value = when {
            calls.isEmpty() -> NoCall  // No active calls
            calls.size == 1 -> SingleCall(calls.first())  // Only one active call
            else -> {
                // Handle multiple calls: find active and on-hold calls
                val activeCall = calls.find { it.state == Call.STATE_ACTIVE }
                val onHoldCall = calls.find { it.state == Call.STATE_HOLDING }

                // Return the state with active and on-hold calls
                TwoCalls(
                    active = activeCall ?: calls.first(), onHold = onHoldCall ?: calls.last()
                )
            }
        }

        // Optionally log the updated phone state
        Log.d(TAG, "Phone state updated: ${_phoneState.value}")
    }


    fun hasNoCalls(): Boolean = calls.isEmpty()

    fun getPrimaryCall(): Call? = calls.firstOrNull()

    /** Accepts an incoming call **/
    suspend fun acceptCall() {
        getPrimaryCall()?.let { call ->
            Log.d(TAG, "Accepting call: ${call.details.handle}")
            call.answer(VideoProfile.STATE_BIDIRECTIONAL)
            _callEvents.emit("Call Accepted")
            updateState()
        }
    }

    /** Rejects or disconnects the call **/
    @SuppressLint("SwitchIntDef")
    suspend fun rejectCall() {
        getPrimaryCall()?.let { call ->
            Log.d(TAG, "Rejecting call: ${call.details.handle}, State: ${call.state}")
            when (call.state) {
                Call.STATE_RINGING -> call.reject(false, null)
                in listOf(
                    Call.STATE_ACTIVE, Call.STATE_HOLDING, Call.STATE_DIALING, Call.STATE_CONNECTING
                ) -> call.disconnect()
            }
            _callEvents.emit("Call Rejected")
        }
        updateState()
    }

    /** Toggles hold on the active call **/
    fun toggleHold(): Boolean {
        val primaryCall = getPrimaryCall()
        return if (primaryCall?.getCallState() == Call.STATE_HOLDING) {
            Log.d(TAG, "Unholding call: ${primaryCall.details.handle}")
            primaryCall.unhold()
            false
        } else {
            Log.d(TAG, "Holding call: ${primaryCall?.details?.handle}")
            primaryCall?.hold()
            true
        }
    }

    /** Swaps between active and held calls **/
    fun swapCalls() {
        calls.find { it.state == Call.STATE_HOLDING }?.let {
            Log.d(TAG, "Swapping calls: Unholding ${it.details.handle}")
            it.unhold()
        }
        updateState()
    }

    /** Merges calls into a conference if possible **/
    fun mergeCalls() {
        getPrimaryCall()?.let { call ->
            val conferenceableCall = call.conferenceableCalls.firstOrNull()
            if (conferenceableCall != null) {
                Log.d(
                    TAG,
                    "Merging calls: ${call.details.handle} + ${conferenceableCall.details.handle}"
                )
                call.conference(conferenceableCall)
            } else if (call.details.hasProperty(Call.Details.PROPERTY_CONFERENCE)) {
                Log.d(TAG, "Merging existing conference call: ${call.details.handle}")
                call.mergeConference()
            }
        }
    }

    /** Fetch contact details by phone number **/
    fun getContactByPhoneNumber(phoneNumber: String, context: Context): Contact? {
        val contentResolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        val selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?"
        val selectionArgs = arrayOf(phoneNumber)

        val cursor: Cursor? = contentResolver.query(uri, projection, selection, selectionArgs, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val id =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val name =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val photoUri =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))

                return Contact(id, name, number, photoUri)
            }
        }
        return null
    }

    fun updateCallState(state: Int): State {
        return when (state) {
            Call.STATE_RINGING -> State.RINGING
            Call.STATE_CONNECTING -> State.CONNECTING
            Call.STATE_ACTIVE -> State.ACTIVE
            Call.STATE_DIALING -> State.DIALING
            Call.STATE_DISCONNECTING -> State.DISCONNECTING
            Call.STATE_DISCONNECTED -> State.ENDED
            Call.STATE_HOLDING -> State.HOLD
            else -> State.IDLE
        }
    }

    private val _callDuration = MutableStateFlow(0L) // Duration in seconds
    val callDuration: StateFlow<Long> = _callDuration.asStateFlow()

    private var callDurationJob: Job? = null

    /** Starts tracking call duration **/
    fun startCallDurationTracking() {
        val call = getPrimaryCall()
        val startTime = call?.details?.connectTimeMillis ?: return

        _callDuration.value = (System.currentTimeMillis() - startTime) / 1000 // Initial duration
        callDurationJob?.cancel() // Cancel any existing job

        callDurationJob = CoroutineScope(Dispatchers.Default).launch {
            while (call.state == Call.STATE_ACTIVE) {  // Only track while the call is active
                delay(1000L) // Wait 1 second
                _callDuration.update { (System.currentTimeMillis() - startTime) / 1000 } // Update duration in seconds
            }
        }
    }


    /** Stops tracking call duration **/
    fun stopCallDurationTracking() {
        callDurationJob?.cancel()
        callDurationJob = null
        _callDuration.value = 0L // Reset duration
    }

    fun sendDtmfTone(digit: Char) {
        getPrimaryCall()?.let { call ->
            if (digit in "0123456789#*") {
                Log.d(TAG, "Sending DTMF tone: $digit")
                call.playDtmfTone(digit)
            } else {
                Log.e(TAG, "Invalid DTMF digit: $digit")
            }
        }
    }

    fun stopDtmfTone() {
        getPrimaryCall()?.let { call ->
            Log.d(TAG, "Stopping DTMF tone")
            call.stopDtmfTone()
        }
    }

}

/** Phone states **/
sealed class PhoneState
object NoCall : PhoneState()
data class SingleCall(val call: Call) : PhoneState()
data class TwoCalls(val active: Call, val onHold: Call) : PhoneState()
