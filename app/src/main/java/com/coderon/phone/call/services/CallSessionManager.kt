package com.coderon.phone.call.services

import com.coderon.phone.call.domain.CallSession

class CallSessionManager {
    private val _sessions = mutableMapOf<Int, CallSession>()
    val sessions: Map<Int, CallSession> get() = _sessions

    private val _sessionStartTimes = mutableMapOf<Int, Long>()
    val sessionStartTimes: Map<Int, Long> get() = _sessionStartTimes

    fun addSession(id: Int, session: CallSession) {
        _sessions[id] = session
    }

    fun removeSession(id: Int) {
        _sessions.remove(id)
        _sessionStartTimes.remove(id)
    }

    fun updateSession(id: Int, session: CallSession) {
        _sessions[id] = session
    }

    fun getSession(id: Int): CallSession? = _sessions[id]

    fun setStartTime(id: Int, time: Long) {
        _sessionStartTimes[id] = time
    }

    fun getStartTime(id: Int): Long? = _sessionStartTimes[id]
    
    fun clear() {
        _sessions.clear()
        _sessionStartTimes.clear()
    }
}
