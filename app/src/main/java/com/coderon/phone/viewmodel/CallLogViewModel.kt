package com.coderon.phone.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.domain.repository.CallLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.coderon.phone.data.model.CallLog as CallLogEntry

class CallLogViewModel(
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CallLogViewModel"
    }

    // -------------------------------
    // SEARCH QUERY
    // -------------------------------
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // -------------------------------
    // AUTO-REFRESHED CALL LOGS
    // -------------------------------
    private val allCallLogs: StateFlow<List<CallLogEntry>> =
        callLogRepository.observeCallLogs()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // -------------------------------
    // FILTERED CALL LOGS
    // -------------------------------
    val filteredCallLogs: StateFlow<List<CallLogEntry>> = combine(
        allCallLogs,
        _searchQuery
    ) { logs, query ->
        if (query.isBlank()) {
            logs
        } else {
            logs.filter { log ->
                log.contact?.displayName
                    ?.contains(query, ignoreCase = true) == true ||
                        log.phoneNumber.contains(query)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // -------------------------------
    // INIT
    // -------------------------------
    init {
        Log.d(TAG, "CallLogViewModel initialized")
    }

    // -------------------------------
    // SEARCH
    // -------------------------------
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // -------------------------------
    // FILTER BY NUMBER
    // -------------------------------
    fun getCallLogsForNumber(phoneNumber: String): Flow<List<CallLogEntry>> {
        return allCallLogs.map { logs ->
            logs.filter { it.phoneNumber == phoneNumber }
        }
    }

    // -------------------------------
    // DELETE CALL LOG
    // -------------------------------
    fun deleteCallLog(callLog: CallLogEntry) {
        viewModelScope.launch {
            try {
                callLogRepository.deleteCallLog(callLog)
                Log.d(TAG, "Deleted call log id=${callLog.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting call log", e)
            }
        }
    }
}
