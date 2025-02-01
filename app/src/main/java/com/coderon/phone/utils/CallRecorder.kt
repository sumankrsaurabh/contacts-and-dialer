package com.coderon.phone.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.io.IOException

object CallRecorder {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var outputFile: String = ""

    fun startRecording(context: Context, phoneNumber: String) {
        if (isRecording) return
        try {
            outputFile = "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/recording_$phoneNumber.mp3"
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile)
                prepare()
                start()
            }
            isRecording = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        if (isRecording) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        }
    }

    fun getRecordingFile(): File? {
        return if (outputFile.isNotEmpty()) File(outputFile) else null
    }
}
