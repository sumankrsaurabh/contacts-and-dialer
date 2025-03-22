package com.coderon.phone.utils

import android.media.AudioManager
import android.media.ToneGenerator

private fun getDTMFTone(digit: Char): Int? {
    return when (digit) {
        '1' -> ToneGenerator.TONE_DTMF_1
        '2' -> ToneGenerator.TONE_DTMF_2
        '3' -> ToneGenerator.TONE_DTMF_3
        '4' -> ToneGenerator.TONE_DTMF_4
        '5' -> ToneGenerator.TONE_DTMF_5
        '6' -> ToneGenerator.TONE_DTMF_6
        '7' -> ToneGenerator.TONE_DTMF_7
        '8' -> ToneGenerator.TONE_DTMF_8
        '9' -> ToneGenerator.TONE_DTMF_9
        '0' -> ToneGenerator.TONE_DTMF_0
        '*' -> ToneGenerator.TONE_DTMF_S
        '#' -> ToneGenerator.TONE_DTMF_P
        else -> null
    }
}

fun playTones(char: Char) {
    val toneGenerator = ToneGenerator(AudioManager.STREAM_DTMF, 80)
    val tone = getDTMFTone(char)
    if (tone != null) {
        toneGenerator.startTone(tone)
    }
}