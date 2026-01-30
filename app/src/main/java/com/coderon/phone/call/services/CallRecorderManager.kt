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

    companion object {
        private const val TAG = "CallRecorderManager"
    }

    /**
     * Optimized Call Recording for Default Dialer (Android 14+ compliant).
     * Uses MediaRecorder with specific voice-optimized settings.
     */
    fun startRecording(phoneNumber: String): Boolean {
        if (_isRecording) return false

        val dir = File(context.getExternalFilesDir(null), "CallRecordings").apply { mkdirs() }
        currentFile = File(dir, "Call_${phoneNumber}_${System.currentTimeMillis()}.amr")

        // Prioritized sources for call recording. 
        // VOICE_COMMUNICATION is the standard for VoIP and PSTN calls on modern Android.
        val sources = listOf(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.MIC
        )

        for (source in sources) {
            if (tryStartWithSource(source)) {
                Log.i(TAG, "Recording started successfully with source: $source")
                return true
            }
        }

        Log.e(TAG, "Failed to initialize any audio source for recording.")
        return false
    }

    private fun tryStartWithSource(source: Int): Boolean {
        return try {
            stopAndRelease()

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(source)
                // Using 3GPP/AMR_NB for maximum compatibility with call-audio hardware paths.
                // Many devices route call audio specifically to these legacy encoders.
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                // Note: Do not set sampling rate or bitrate manually for AMR_NB to avoid hardware mismatches.
                setOutputFile(currentFile!!.absolutePath)
                prepare()
                start()
            }
            _isRecording = true
            true
        } catch (e: Exception) {
            Log.w(TAG, "Source $source failed: ${e.message}")
            stopAndRelease()
            false
        }
    }

    fun stopRecording(): String? {
        if (!_isRecording) return null
        val path = currentFile?.absolutePath
        try {
            recorder?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Stop failed", e)
        } finally {
            stopAndRelease()
            _isRecording = false
        }
        return path
    }

    private fun stopAndRelease() {
        try {
            recorder?.apply {
                reset()
                release()
            }
        } catch (_: Exception) {
        } finally {
            recorder = null
        }
    }

    fun isRecording(): Boolean = _isRecording

    fun getRecordingsForNumber(phoneNumber: String): List<File> {
        val dir = File(context.getExternalFilesDir(null), "CallRecordings")
        if (!dir.exists()) return emptyList()

        return dir.listFiles { file ->
            val name = file.name
            name.startsWith("Call_${phoneNumber}_") &&
                (name.endsWith(".amr") || name.endsWith(".3gp") || name.endsWith(".mp4") || name.endsWith(".wav"))
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
