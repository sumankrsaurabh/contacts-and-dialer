package com.coderon.phone.utils

import android.content.Context
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FlashlightManager(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private var flashJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startBlinking() {
        if (cameraId == null || flashJob?.isActive == true) return
        flashJob = scope.launch {
            while (true) {
                setFlashlight(true)
                delay(500)
                setFlashlight(false)
                delay(500)
            }
        }
    }

    fun stopBlinking() {
        flashJob?.cancel()
        setFlashlight(false)
    }

    private fun setFlashlight(enabled: Boolean) {
        cameraId?.let {
            try {
                cameraManager.setTorchMode(it, enabled)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
