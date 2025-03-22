package com.coderon.phone.ui.screens.incallui

import android.telecom.Call
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun VideoCallUi(call: Call?) {
    var isMicOn by remember { mutableStateOf(true) }
    var isCameraOn by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Other person's video feed
        AndroidView(
            factory = { context ->
                TextureView(context).apply {
                    surfaceTextureListener = object : SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(texture: android.graphics.SurfaceTexture, width: Int, height: Int) {
                            val surface = Surface(texture)
                            call?.videoCall?.setDisplaySurface(surface)
                        }

                        override fun onSurfaceTextureSizeChanged(texture: android.graphics.SurfaceTexture, width: Int, height: Int) {}

                        override fun onSurfaceTextureDestroyed(texture: android.graphics.SurfaceTexture): Boolean = true

                        override fun onSurfaceTextureUpdated(texture: android.graphics.SurfaceTexture) {}
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Small window for self-view
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            AndroidView(
                factory = { context ->
                    TextureView(context).apply {
                        surfaceTextureListener = object : SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(texture: android.graphics.SurfaceTexture, width: Int, height: Int) {
                                val surface = Surface(texture)
                                call?.videoCall?.setPreviewSurface(surface)
                            }

                            override fun onSurfaceTextureSizeChanged(texture: android.graphics.SurfaceTexture, width: Int, height: Int) {}

                            override fun onSurfaceTextureDestroyed(texture: android.graphics.SurfaceTexture): Boolean = true

                            override fun onSurfaceTextureUpdated(texture: android.graphics.SurfaceTexture) {}
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {
                isMicOn = !isMicOn
//                call?.videoCall?.setMuted(!isMicOn)
            }) {
                Icon(
                    imageVector = if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Toggle Microphone"
                )
            }
            IconButton(onClick = {
                isCameraOn = !isCameraOn
                // Call does not support enabling/disabling camera directly
                // Need to handle camera ID switching if required
            }) {
                Icon(
                    imageVector = if (isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    contentDescription = "Toggle Camera"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VideoCallUiPreview() {
    VideoCallUi(null) // Using null since preview can't pass a real Call object
}
