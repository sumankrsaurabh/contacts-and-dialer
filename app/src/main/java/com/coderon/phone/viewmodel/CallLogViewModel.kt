package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.modal.CallLog
import com.coderon.phone.domain.GetCallLogsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CallLogViewModel(private val getCallLogsUseCase: GetCallLogsUseCase) : ViewModel() {
    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs.asStateFlow()


    init {
        fetchCallLogs()
    }

    private fun fetchCallLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val callLogs = getCallLogsUseCase()
                _callLogs.value = callLogs
            } catch (e: Exception) {
                // Handle error, e.g., update state with an error message
            }
        }
    }

    fun filteredCallLogs(searchText: String): Flow<List<CallLog>> = callLogs.map { logs ->
        logs.filter { log ->
            (log.contact?.name?.contains(
                searchText,
                ignoreCase = true
            ) == true) || log.phoneNumber.contains(searchText)
        }
    }
}