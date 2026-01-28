package com.coderon.phone.call.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.telecom.InCallService
import android.util.Log
import androidx.core.content.ContextCompat

class CallCameraManager(private val context: Context) {
    private var currentFacing: Int = CameraCharacteristics.LENS_FACING_FRONT
    var currentCameraId: String? = null
        private set

    init {
        initCameraId()
    }

    private fun initCameraId() {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        
        // Try to find Front camera first
        val front = getCameraForFacing(manager, CameraCharacteristics.LENS_FACING_FRONT)
        if (front != null) {
            currentCameraId = front
            currentFacing = CameraCharacteristics.LENS_FACING_FRONT
            return
        }

        // Fallback to Back camera
        val back = getCameraForFacing(manager, CameraCharacteristics.LENS_FACING_BACK)
        if (back != null) {
            currentCameraId = back
            currentFacing = CameraCharacteristics.LENS_FACING_BACK
            return
        }

        // Fallback to any available camera
        currentCameraId = manager.cameraIdList.firstOrNull()
        currentFacing = currentCameraId?.let { id ->
            manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING)
        } ?: CameraCharacteristics.LENS_FACING_FRONT
    }

    private fun getCameraForFacing(manager: CameraManager, facing: Int): String? {
        return manager.cameraIdList.firstOrNull { id ->
            manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == facing
        }
    }

    fun isFrontCamera(): Boolean = currentFacing == CameraCharacteristics.LENS_FACING_FRONT

    fun rebindCamera(videoCall: InCallService.VideoCall?) {
        if (!hasCameraPermission()) return
        currentCameraId?.let {
            Log.d("CallCameraManager", "Rebinding camera: $it")
            videoCall?.setCamera(it)
        }
    }

    fun flipCamera(videoCall: InCallService.VideoCall?): Boolean {
        if (!hasCameraPermission()) return false

        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val targetFacing = if (currentFacing == CameraCharacteristics.LENS_FACING_FRONT)
            CameraCharacteristics.LENS_FACING_BACK
        else
            CameraCharacteristics.LENS_FACING_FRONT

        // Determine target ID, falling back to current ID to ensure we always set A camera
        val targetCameraId = getCameraForFacing(manager, targetFacing)
            ?: currentCameraId
            ?: manager.cameraIdList.firstOrNull()
            ?: return false

        return try {
            Log.d("CallCameraManager", "Flipping camera to: $targetCameraId")
            videoCall?.setCamera(targetCameraId)
            
            // Update internal state
            currentCameraId = targetCameraId
            currentFacing = manager.getCameraCharacteristics(targetCameraId)
                .get(CameraCharacteristics.LENS_FACING) ?: currentFacing
            
            true
        } catch (e: Exception) {
            Log.e("CallCameraManager", "Camera flip failed, attempting to ensure a camera is set", e)
            // Ensure we don't leave the video call without a camera if possible
            currentCameraId?.let { videoCall?.setCamera(it) }
            false
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}
