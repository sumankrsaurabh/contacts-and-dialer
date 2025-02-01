package com.coderon.phone.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.io.IOException

object VoicemailManager {
    private var mediaRecorder: MediaRecorder? = null
    private var voicemailFile: String = ""

    fun startRecordingVoicemail(context: Context, callerId: String) {
        try {
            voicemailFile = "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/voicemail_$callerId.mp3"
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(voicemailFile)
                prepare()
                start()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecordingVoicemail() {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    fun getVoicemailFile(): File? {
        return if (voicemailFile.isNotEmpty()) File(voicemailFile) else null
    }
}
