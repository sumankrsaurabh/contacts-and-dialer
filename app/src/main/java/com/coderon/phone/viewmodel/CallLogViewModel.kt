package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.helpers.formatDate
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.domain.repository.CallLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CallLogViewModel(private val callLogRepository: CallLogRepository) : ViewModel() {

    private val _callLogs = MutableStateFlow<Map<String, List<CallLog>>>(emptyMap())
    val callLogs: StateFlow<Map<String, List<CallLog>>> = _callLogs.asStateFlow()

    init {
        fetchCallLogs()
    }

    private fun fetchCallLogs() {
        viewModelScope.launch {
            try {
                val callLogs = callLogRepository.getCallLogs()
                _callLogs.value = callLogs.groupBy { it.callTime.formatDate() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun filteredCallLogs(searchText: String): Flow<Map<String, List<CallLog>>> {
        return _callLogs.map { logs ->
            if (searchText.isBlank()) logs
            else logs.mapValues { (_, logList) ->
                logList.filter { log ->
                    log.phoneNumber.contains(searchText, ignoreCase = true) ||
                            log.contact?.name?.contains(searchText, ignoreCase = true) == true
                }
            }.filterValues { it.isNotEmpty() }
        }
    }

    fun getCallLogsForNumber(phoneNumber: String): List<CallLog> {
        return _callLogs.value.values.flatten().filter { it.phoneNumber == phoneNumber }
    }
}
