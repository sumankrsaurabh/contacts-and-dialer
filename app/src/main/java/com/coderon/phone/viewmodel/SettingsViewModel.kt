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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    val autoRecordAll: StateFlow<Boolean> = repository.autoRecordAll
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoRecordUnknown: StateFlow<Boolean> = repository.autoRecordUnknown
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoRecordContacts: StateFlow<Boolean> = repository.autoRecordContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val contactSortOrder: StateFlow<Int> = repository.contactSortOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val contactDisplayNameFormat: StateFlow<Int> = repository.contactDisplayNameFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val showPostCallDetails: StateFlow<Boolean> = repository.showPostCallDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val vibrationPattern: StateFlow<Int> = repository.vibrationPattern
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val announceCallerName: StateFlow<Boolean> = repository.announceCallerName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val blockUnknownNumbers: StateFlow<Boolean> = repository.blockUnknownNumbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val spamProtectionEnabled: StateFlow<Boolean> = repository.spamProtectionEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val flipToSilence: StateFlow<Boolean> = repository.flipToSilence
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val oneHandedMode: StateFlow<Int> = repository.oneHandedMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dialPadSoundTheme: StateFlow<Int> = repository.dialPadSoundTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val swipeToCallEnabled: StateFlow<Boolean> = repository.swipeToCallEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val hapticFeedbackEnabled: StateFlow<Boolean> = repository.hapticFeedbackEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val fullScreenCallerPhoto: StateFlow<Boolean> = repository.fullScreenCallerPhoto
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoAnswerEnabled: StateFlow<Boolean> = repository.autoAnswerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoAnswerDelay: StateFlow<Int> = repository.autoAnswerDelay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val proximitySensorEnabled: StateFlow<Boolean> = repository.proximitySensorEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _availableSims = MutableStateFlow<List<PhoneAccountHandle>>(emptyList())
    val availableSims: StateFlow<List<PhoneAccountHandle>> = _availableSims.asStateFlow()

    val speedDials: StateFlow<Map<Int, String?>> = combine(
        (2..9).map { digit ->
            repository.getSpeedDial(digit).map { number -> digit to number }
        }
    ) { arrayOfPairs: Array<Pair<Int, String?>> ->
        arrayOfPairs.toMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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

    fun setAutoRecordAll(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoRecordAll(enabled) }
    }

    fun setAutoRecordUnknown(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoRecordUnknown(enabled) }
    }

    fun setAutoRecordContacts(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoRecordContacts(enabled) }
    }

    fun setContactSortOrder(order: Int) {
        viewModelScope.launch { repository.setContactSortOrder(order) }
    }

    fun setContactDisplayNameFormat(format: Int) {
        viewModelScope.launch { repository.setContactDisplayNameFormat(format) }
    }

    fun setShowPostCallDetails(show: Boolean) {
        viewModelScope.launch { repository.setShowPostCallDetails(show) }
    }

    fun setVibrationPattern(pattern: Int) {
        viewModelScope.launch { repository.setVibrationPattern(pattern) }
    }

    fun setAnnounceCallerName(enabled: Boolean) {
        viewModelScope.launch { repository.setAnnounceCallerName(enabled) }
    }

    fun setBlockUnknownNumbers(enabled: Boolean) {
        viewModelScope.launch { repository.setBlockUnknownNumbers(enabled) }
    }

    fun setSpamProtectionEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSpamProtectionEnabled(enabled) }
    }

    fun setFlipToSilence(enabled: Boolean) {
        viewModelScope.launch { repository.setFlipToSilence(enabled) }
    }

    fun setOneHandedMode(mode: Int) {
        viewModelScope.launch { repository.setOneHandedMode(mode) }
    }

    fun setDialPadSoundTheme(theme: Int) {
        viewModelScope.launch { repository.setDialPadSoundTheme(theme) }
    }

    fun setSwipeToCallEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSwipeToCallEnabled(enabled) }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticFeedbackEnabled(enabled) }
    }

    fun setFullScreenCallerPhoto(enabled: Boolean) {
        viewModelScope.launch { repository.setFullScreenCallerPhoto(enabled) }
    }

    fun setAutoAnswerEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoAnswerEnabled(enabled) }
    }

    fun setAutoAnswerDelay(delay: Int) {
        viewModelScope.launch { repository.setAutoAnswerDelay(delay) }
    }

    fun setProximitySensorEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setProximitySensorEnabled(enabled) }
    }

    fun setSpeedDial(digit: Int, number: String?) {
        viewModelScope.launch { repository.setSpeedDial(digit, number) }
    }

    suspend fun getSpeedDialSync(digit: Int): String? {
        return repository.getSpeedDial(digit).first()
    }
}
