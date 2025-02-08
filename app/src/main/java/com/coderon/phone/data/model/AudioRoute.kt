package com.coderon.phone.data.model

import android.telecom.CallAudioState
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.Headset
import androidx.compose.ui.graphics.vector.ImageVector
import com.coderon.phone.R

enum class AudioRoute(
    val route: Int,
    @StringRes val stringRes: Int,
    val iconRes: ImageVector
) {
    SPEAKER(CallAudioState.ROUTE_SPEAKER, R.string.speaker, Icons.AutoMirrored.Filled.VolumeUp),
    EARPIECE(
        CallAudioState.ROUTE_EARPIECE,
        R.string.audio_route_earpiece,
        Icons.AutoMirrored.Filled.VolumeDown
    ),
    BLUETOOTH(
        CallAudioState.ROUTE_BLUETOOTH,
        R.string.audio_route_bluetooth,
        Icons.Default.BluetoothAudio
    ),
    WIRED_HEADSET(
        CallAudioState.ROUTE_WIRED_HEADSET,
        R.string.audio_route_wired_headset,
        Icons.Default.Headset
    ),
    WIRED_OR_EARPIECE(
        CallAudioState.ROUTE_WIRED_OR_EARPIECE,
        R.string.audio_route_wired_or_earpiece,
        Icons.AutoMirrored.Filled.VolumeDown
    );

    companion object {
        fun fromRoute(route: Int?) = AudioRoute.entries.firstOrNull { it.route == route }
    }
}