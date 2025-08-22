package com.coderon.phone.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.model.CallLog
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

class CallLogViewModel(
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CallLogViewModel"
    }

    private val _allCallLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val allCallLogs: StateFlow<List<CallLog>> = _allCallLogs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredCallLogs: StateFlow<List<CallLog>> = combine(
        _allCallLogs,
        _searchQuery
    ) { logs, query ->
        if (query.isBlank()) {
            logs
        } else {
            logs.filter { log ->
                log.contact?.name?.contains(query, ignoreCase = true) == true ||
                        log.phoneNumber.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchCallLogs()
    }

    private fun fetchCallLogs() {
        viewModelScope.launch {
            try {
                val logs = callLogRepository.getCallLogs()
                _allCallLogs.value = logs
                Log.d(TAG, "Fetched ${logs.size} call logs")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching call logs", e)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getCallLogsForNumber(phoneNumber: String): Flow<List<CallLog>> {
        return allCallLogs.map { logs ->
            logs.filter { it.phoneNumber == phoneNumber }
        }
    }
}
