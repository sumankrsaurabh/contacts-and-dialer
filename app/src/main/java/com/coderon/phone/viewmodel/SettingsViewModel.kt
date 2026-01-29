package com.coderon.phone.viewmodel

import android.content.Context
import android.telecom.PhoneAccountHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.repository.SettingsRepository
import com.coderon.phone.utils.getAvailableSims
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val context: Context
) : ViewModel() {

    val ringtoneEnabled: StateFlow<Boolean> = repository.ringtoneEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val ringtoneUri: StateFlow<String?> = repository.ringtoneUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val keypadTonesEnabled: StateFlow<Boolean> = repository.keypadTonesEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val callScreenBackground: StateFlow<String?> = repository.callScreenBackground
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themeMode: StateFlow<Int> = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dynamicColor: StateFlow<Boolean> = repository.dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val defaultSimId: StateFlow<String?> = repository.defaultSimId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val vibrateOnAnswer: StateFlow<Boolean> = repository.vibrateOnAnswer
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val flashOnCall: StateFlow<Boolean> = repository.flashOnCall
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showContactPhoto: StateFlow<Boolean> = repository.showContactPhoto
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _availableSims = MutableStateFlow<List<PhoneAccountHandle>>(emptyList())
    val availableSims: StateFlow<List<PhoneAccountHandle>> = _availableSims.asStateFlow()

    init {
        _availableSims.value = getAvailableSims(context)
    }

    fun setRingtoneEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setRingtoneEnabled(enabled) }
    }

    fun setRingtoneUri(uri: String?) {
        viewModelScope.launch { repository.setRingtoneUri(uri) }
    }

    fun setKeypadTonesEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setKeypadTonesEnabled(enabled) }
    }

    fun setCallScreenBackground(uri: String?) {
        viewModelScope.launch { repository.setCallScreenBackground(uri) }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { repository.setDynamicColor(enabled) }
    }

    fun setDefaultSimId(simId: String?) {
        viewModelScope.launch { repository.setDefaultSimId(simId) }
    }

    fun setVibrateOnAnswer(enabled: Boolean) {
        viewModelScope.launch { repository.setVibrateOnAnswer(enabled) }
    }

    fun setFlashOnCall(enabled: Boolean) {
        viewModelScope.launch { repository.setFlashOnCall(enabled) }
    }

    fun setShowContactPhoto(enabled: Boolean) {
        viewModelScope.launch { repository.setShowContactPhoto(enabled) }
    }
}
