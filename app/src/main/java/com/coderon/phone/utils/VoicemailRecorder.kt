package com.coderon.phone.utils

import android.media.MediaRecorder
import android.os.Environment
import java.io.File

class VoicemailRecorder {

    private var recorder: MediaRecorder? = null
    private var filePath: String? = null

    fun startRecording(phoneNumber: String): String {
        val directory = File(Environment.getExternalStorageDirectory(), "Voicemails")
        if (!directory.exists()) directory.mkdirs()

        filePath = "${directory.absolutePath}/voicemail_${System.currentTimeMillis()}.3gp"

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(filePath)
            prepare()
            start()
        }
        return filePath!!
    }

    fun stopRecording(): String? {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        return filePath
    }
}
