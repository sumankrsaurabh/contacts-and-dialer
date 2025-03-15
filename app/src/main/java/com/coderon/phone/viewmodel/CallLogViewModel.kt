package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.domain.repository.CallLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CallLogViewModel(private val callLogRepository: CallLogRepository) : ViewModel() {

    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs.asStateFlow()

    init {
        fetchCallLogs()
    }

    private fun fetchCallLogs() {
        viewModelScope.launch {
            try {
                val callLogs = callLogRepository.getCallLogs()
                _callLogs.value = callLogs
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun filteredCallLogs(searchText: String): Flow<List<CallLog>> {
        return _callLogs.map { logs ->
            logs.filter { log ->
                log.contact?.name?.contains(searchText, ignoreCase = true) == true ||
                        log.phoneNumber.contains(searchText, ignoreCase = true)
            }
        }
    }

    fun getCallLogsForNumber(phoneNumber: String): Flow<List<CallLog>> {
        return _callLogs.map { logs ->
            logs.filter { it.phoneNumber == phoneNumber }
        }
    }
}
