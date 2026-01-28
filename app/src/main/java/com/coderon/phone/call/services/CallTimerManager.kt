package com.coderon.phone.call.services

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

class CallTimerManager(private val scope: CoroutineScope) {
    private var timerJob: Job? = null
    
    fun updateTimerState(hasActiveCall: Boolean, getStartTime: () -> Long?, onTick: (Long) -> Unit) {
        if (hasActiveCall && timerJob == null) {
            startTimer(getStartTime, onTick)
        } else if (!hasActiveCall && timerJob != null) {
            stopTimer(onTick)
        }
    }

    private fun startTimer(getStartTime: () -> Long?, onTick: (Long) -> Unit) {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                getStartTime()?.let { startTime ->
                    val seconds = (System.currentTimeMillis() - startTime) / 1000
                    onTick(seconds)
                }
                delay(1000)
            }
        }
    }

    fun stopTimer(onTick: (Long) -> Unit) {
        timerJob?.cancel()
        timerJob = null
        onTick(0L)
    }
}
