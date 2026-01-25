package com.coderon.phone.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.coderon.phone.data.model.CallLog as CallLogEntry

class CallLogViewModel(
    private val observeCallLogsUseCase: ObserveCallLogsUseCase,
    private val deleteCallLogUseCase: DeleteCallLogUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "CallLogViewModel"
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val allCallLogs: StateFlow<List<CallLogEntry>> =
        observeCallLogsUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

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

    init {
        Log.d(TAG, "CallLogViewModel initialized")
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getCallLogsForNumber(phoneNumber: String): Flow<List<CallLogEntry>> {
        return allCallLogs.map { logs ->
            logs.filter { it.phoneNumber == phoneNumber }
        }
    }

    fun deleteCallLog(callLog: CallLogEntry) {
        viewModelScope.launch {
            try {
                deleteCallLogUseCase(callLog)
                Log.d(TAG, "Deleted call log id=${callLog.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting call log", e)
            }
        }
    }
}
