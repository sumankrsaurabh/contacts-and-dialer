package com.coderon.phone.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.helpers.formatDate
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.domain.usecase.DeleteCallLogUseCase
import com.coderon.phone.domain.usecase.ObserveCallLogsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CallFilter {
    ALL, MISSED_TODAY
}

data class GroupedCallLog(val logs: List<CallLog>) {
    val log = logs.first()
    val phoneNumber get() = log.phoneNumber
    val callType get() = log.callType
    val callTime get() = log.callTime
    val contact get() = log.contact
    val simSlot get() = log.simSlot
}

class CallLogViewModel(
    private val observeCallLogsUseCase: ObserveCallLogsUseCase,
    private val deleteCallLogUseCase: DeleteCallLogUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "CallLogViewModel"
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(CallFilter.ALL)
    val filter: StateFlow<CallFilter> = _filter.asStateFlow()

    private val allCallLogs: StateFlow<List<CallLog>> =
        observeCallLogsUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /**
     * Pre-processed call logs grouped by date and then by consecutive same number/type.
     * Calculated in background to prevent UI lag.
     */
    val callLogsByDate: StateFlow<Map<String, List<GroupedCallLog>>> = combine(
        allCallLogs,
        _searchQuery,
        _filter
    ) { logs, query, filter ->
        val filtered = logs.filter { log ->
            val matchesQuery = if (query.isBlank()) true 
            else log.contact?.displayName?.contains(query, ignoreCase = true) == true || log.phoneNumber.contains(query)
            
            val matchesFilter = when (filter) {
                CallFilter.ALL -> true
                CallFilter.MISSED_TODAY -> log.callType == CallType.MISSED && log.callTime >= System.currentTimeMillis() - 86_400_000
            }
            
            matchesQuery && matchesFilter
        }

        filtered.sortedByDescending { it.callTime }
            .groupBy { it.callTime.formatDate() }
            .mapValues { entry -> groupConsecutiveLogs(entry.value) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: CallFilter) {
        _filter.value = filter
    }

    fun getCallLogsForNumber(phoneNumber: String): Flow<List<CallLog>> {
        return allCallLogs.map { logs ->
            logs.filter { it.phoneNumber == phoneNumber }
        }
    }

    fun deleteCallLog(callLog: CallLog) {
        viewModelScope.launch {
            try {
                deleteCallLogUseCase(callLog)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting call log", e)
            }
        }
    }

    fun deleteAllLogs() {
        viewModelScope.launch {
            allCallLogs.value.forEach { 
                try {
                    deleteCallLogUseCase(it)
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting all logs", e)
                }
            }
        }
    }

    private fun groupConsecutiveLogs(logs: List<CallLog>): List<GroupedCallLog> {
        if (logs.isEmpty()) return emptyList()
        
        val result = mutableListOf<GroupedCallLog>()
        var currentGroup = mutableListOf<CallLog>()
        
        for (log in logs) {
            if (currentGroup.isEmpty()) {
                currentGroup.add(log)
            } else {
                val lastLog = currentGroup.last()
                // Group if it's the same number and same type (consecutive)
                if (lastLog.phoneNumber == log.phoneNumber && lastLog.callType == log.callType) {
                    currentGroup.add(log)
                } else {
                    result.add(GroupedCallLog(currentGroup))
                    currentGroup = mutableListOf(log)
                }
            }
        }
        
        if (currentGroup.isNotEmpty()) {
            result.add(GroupedCallLog(currentGroup))
        }
        
        return result
    }
}
