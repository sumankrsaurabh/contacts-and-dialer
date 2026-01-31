package com.coderon.phone.utils

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock.sleep

private const val TONE_DURATION_MS = 100L
private const val TONE_VOLUME_PERCENT = 80

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

/**
 * Plays DTMF tones.
 * @param char The digit pressed.
 * @param theme The sound theme: 0: Default, 1: Piano (Mocked using different tone types), 2: Retro (Mocked)
 */
fun playTones(char: Char, theme: Int = 0) {
    val streamType = when (theme) {
        1 -> AudioManager.STREAM_MUSIC // Piano-like
        2 -> AudioManager.STREAM_SYSTEM // Retro-like
        else -> AudioManager.STREAM_DTMF
    }
    
    val toneGenerator = ToneGenerator(streamType, TONE_VOLUME_PERCENT)
    val tone = getDTMFTone(char)
    if (tone != null) {
        // In a real implementation with specific assets, we would play those .mp3 files here.
        // For now, we vary the stream type or use different tone generators if available.
        toneGenerator.startTone(tone)
        sleep(TONE_DURATION_MS)
        toneGenerator.stopTone()
    }
}
