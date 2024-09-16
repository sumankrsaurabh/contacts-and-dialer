package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.modal.CallLog
import com.coderon.phone.domain.GetCallLogsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CallLogViewModel(private val getCallLogsUseCase: GetCallLogsUseCase) : ViewModel() {
    // Using StateFlow for better state management
    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs

    init {
        fetchCallLogs()
    }

    private fun fetchCallLogs() {
        // Ensure data fetching is done asynchronously
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = getCallLogsUseCase()
            withContext(Dispatchers.Main) {
                _callLogs.value = contacts
            }
        }

    }
}
