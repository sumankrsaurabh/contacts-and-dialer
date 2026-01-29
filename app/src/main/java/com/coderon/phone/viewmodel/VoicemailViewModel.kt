package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.model.Voicemail
import com.coderon.phone.data.repository.VoicemailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoicemailViewModel(private val repository: VoicemailRepository) : ViewModel() {

    private val _voicemails = MutableStateFlow<List<Voicemail>>(emptyList())
    val voicemails: StateFlow<List<Voicemail>> = _voicemails.asStateFlow()

    init {
        loadVoicemails()
    }

    fun loadVoicemails() {
        viewModelScope.launch {
            _voicemails.value = repository.getVoicemails()
        }
    }

    fun deleteVoicemail(voicemail: Voicemail) {
        viewModelScope.launch {
            repository.deleteVoicemail(voicemail)
            loadVoicemails()
        }
    }
}
