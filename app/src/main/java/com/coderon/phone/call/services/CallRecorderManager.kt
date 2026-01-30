package com.coderon.phone.call.services

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class CallRecorderManager(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var _isRecording = false
    private var currentFile: File? = null

    fun startRecording(phoneNumber: String): Boolean {
        if (_isRecording) return false

        val directory = File(context.getExternalFilesDir(null), "CallRecordings")
        if (!directory.exists()) directory.mkdirs()

        val fileName = "Call_${phoneNumber}_${System.currentTimeMillis()}.mp4"
        currentFile = File(directory, fileName)

        // Try multiple audio sources, starting with the most effective for a Dialer app
        val sources = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listOf(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.VOICE_RECOGNITION
            )
        } else {
            listOf(
                MediaRecorder.AudioSource.VOICE_CALL, // Might work on older versions or if system app
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MediaRecorder.AudioSource.MIC
            )
        }

        for (source in sources) {
            if (tryStartWithSource(source)) {
                Log.d("CallRecorderManager", "Recording started with source: $source")
                return true
            }
        }

        return false
    }

    private fun tryStartWithSource(source: Int): Boolean {
        return try {
            recorder?.release()
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(source)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(currentFile!!.absolutePath)
                prepare()
                start()
            }
            _isRecording = true
            true
        } catch (e: Exception) {
            Log.e("CallRecorderManager", "Failed to start recording with source $source", e)
            recorder?.reset()
            recorder?.release()
            recorder = null
            _isRecording = false
            false
        }
    }

    fun stopRecording(): String? {
        if (!_isRecording) return null

        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e("CallRecorderManager", "stopRecording failed", e)
        } finally {
            recorder = null
            _isRecording = false
        }

        return currentFile?.absolutePath
    }

    fun isRecording() = _isRecording

    fun getRecordingsForNumber(phoneNumber: String): List<File> {
        val directory = File(context.getExternalFilesDir(null), "CallRecordings")
        if (!directory.exists()) return emptyList()
        
        return directory.listFiles { file ->
            val name = file.name
            name.startsWith("Call_${phoneNumber}_") && (name.endsWith(".mp4") || name.endsWith(".amr"))
        }?.toList()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
