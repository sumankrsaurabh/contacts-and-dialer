package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.model.BlockedNumber
import com.coderon.phone.data.repository.BlockedNumberRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BlockedNumbersViewModel(private val repository: BlockedNumberRepository) : ViewModel() {

    private val _blockedNumbers = MutableStateFlow<List<BlockedNumber>>(emptyList())
    val blockedNumbers: StateFlow<List<BlockedNumber>> = _blockedNumbers

    init {
        loadBlockedNumbers()
    }

    private fun loadBlockedNumbers() {
        viewModelScope.launch {
            _blockedNumbers.value = repository.getAllBlockedNumbers()
        }
    }

    fun blockNumber(number: String) {
        viewModelScope.launch {
            repository.blockNumber(number)
            loadBlockedNumbers()
        }
    }

    fun unblockNumber(number: String) {
        viewModelScope.launch {
            repository.unblockNumber(number)
            loadBlockedNumbers()
        }
    }
}
