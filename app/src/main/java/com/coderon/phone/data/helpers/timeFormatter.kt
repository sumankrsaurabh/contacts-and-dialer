package com.coderon.phone.data.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Formats the timestamp into a readable time (e.g., "12:30 PM")
fun Long.formatTime(): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(Date(this))
}

// Formats call duration from seconds to "minutes and seconds" format
fun Long.formatDuration(): String {
    val minutes = this / 60
    val remainingSeconds = this % 60

    return when {
        minutes > 0 -> "$minutes m $remainingSeconds s"
        else -> "$remainingSeconds s"
    }
}
