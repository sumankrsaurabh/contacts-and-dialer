package com.coderon.phone.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.coderon.phone.services.CallRecordingService
import com.coderon.phone.services.MyConnectionService
import com.coderon.phone.ui.theme.PhoneTheme

class CallActivity : ComponentActivity() {

    private var currentCall: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callId = intent.getStringExtra("CALL_ID")
        currentCall = MyConnectionService.getCallByHandle(callId)
        currentCall?.registerCallback(object : Call.Callback() {
            override fun onStateChanged(call: Call, newState: Int) {
                when (newState) {
                    Call.STATE_ACTIVE -> Log.d("CallActivity", "Call is active")
                    Call.STATE_HOLDING -> Log.d("CallActivity", "Call is on hold")
                    Call.STATE_DISCONNECTED -> {
                        Log.d("CallActivity", "Call ended")
                        finish()
                    }
                }
            }
        })

        setContent {
            PhoneTheme {
                CallScreen(
                    callerName = intent.getStringExtra("CALLER_NAME") ?: "Unknown",
                    phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: "",
                    isIncoming = intent.getBooleanExtra("IS_INCOMING", false),
                    onAnswer = { answerCall() },
                    onDecline = { declineCall() },
                    onMute = { /*toggleMute()*/ },
                    onHold = { toggleHold() },
                    onSpeaker = { /*toggleSpeaker()*/ },
                    onRecord = {}
                )
            }
        }
    }

    private fun answerCall() {
//        currentCall?.answer()
        Log.d("CallActivity", "Call answered")
    }

    private fun declineCall() {
        currentCall?.disconnect()
        finish()
    }

//    private fun toggleMute() {
//        val callAudio = InCallService.getInstance()?.audio
//        callAudio?.let {
//            it.isMuted = !it.isMuted
//            Log.d("CallActivity", "Mute toggled: ${it.isMuted}")
//        }
//    }

    private fun toggleHold() {
        currentCall?.let {
            if (it.state == Call.STATE_HOLDING) {
                it.unhold()
                Log.d("CallActivity", "Call resumed")
            } else {
                it.hold()
                Log.d("CallActivity", "Call put on hold")
            }
        }
    }

//    private fun toggleSpeaker() {
//        val callAudio = InCallService.getInstance()?.audio
//        callAudio?.let {
//            if (it.route == CallAudioState.ROUTE_SPEAKER) {
//                it.route = CallAudioState.ROUTE_EARPIECE
//                Log.d("CallActivity", "Switched to earpiece")
//            } else {
//                it.route = CallAudioState.ROUTE_SPEAKER
//                Log.d("CallActivity", "Switched to speaker")
//            }
//        }
//    }

    private fun startRecording() {
        val intent = Intent(this, CallRecordingService::class.java)
        startService(intent)
        Log.d("CallActivity", "Call recording started")
    }

    private fun stopRecording() {
        val intent = Intent(this, CallRecordingService::class.java)
        stopService(intent)
        Log.d("CallActivity", "Call recording stopped")
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 101
            )
        }
    }

}
