package com.coderon.phone.services

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import java.io.File

class CallRecordingService : Service() {

    private var recorder: MediaRecorder? = null
    private var recordingFilePath: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRecording()
        return START_STICKY
    }

    private fun startRecording() {
        val fileName = "Call_${System.currentTimeMillis()}.mp4"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        recordingFilePath = File(storageDir, fileName).absolutePath

        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(recordingFilePath)
                prepare()
                start()
            }
            Log.d("CallRecordingService", "Recording started: $recordingFilePath")
        } catch (e: Exception) {
            Log.e("CallRecordingService", "Recording failed", e)
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        Log.d("CallRecordingService", "Recording stopped: $recordingFilePath")
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
