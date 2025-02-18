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
import com.coderon.phone.ui.utils.extentions.State
import com.coderon.phone.ui.utils.extentions.getCallState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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


    /** SharedFlow for one-time call events **/
    private val _callEvents = MutableSharedFlow<String>()
    val callEvents = _callEvents.asSharedFlow()

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
            override fun onStateChanged(call: Call, state: Int) {
                Log.d(TAG, "Call state changed: ${call.details.handle}, New State: $state")
                _callState.value = updateCallState(state)
            }

            override fun onDetailsChanged(call: Call, details: Call.Details) {
                Log.d(TAG, "Call details changed: ${call.details.handle}")
                updateState()
            }

            override fun onConferenceableCallsChanged(
                call: Call,
                conferenceableCalls: MutableList<Call>
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
                    active = activeCall ?: calls.first(),
                    onHold = onHoldCall ?: calls.last()
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
            call.answer(VideoProfile.STATE_AUDIO_ONLY)
            _callEvents.emit("Call Accepted")
            updateState()
        }
    }

    /** Rejects or disconnects the call **/
    suspend fun rejectCall() {
        getPrimaryCall()?.let { call ->
            Log.d(TAG, "Rejecting call: ${call.details.handle}, State: ${call.state}")
            when (call.state) {
                Call.STATE_RINGING -> call.reject(false, null)
                in listOf(
                    Call.STATE_ACTIVE,
                    Call.STATE_HOLDING,
                    Call.STATE_DIALING,
                    Call.STATE_CONNECTING
                ) -> call.disconnect()
            }
            _callEvents.emit("Call Rejected")
        }
        updateState()
    }

    /** Toggles hold on the active call **/
    fun toggleHold(): Boolean {
        val primaryCall = getPrimaryCall()
        return if (primaryCall?.state == Call.STATE_HOLDING) {
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
            else -> State.IDLE
        }
    }
}

/** Phone states **/
sealed class PhoneState
object NoCall : PhoneState()
data class SingleCall(val call: Call) : PhoneState()
data class TwoCalls(val active: Call, val onHold: Call) : PhoneState()
