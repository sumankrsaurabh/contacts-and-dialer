package com.coderon.phone.data.repository

import android.content.Context
import android.media.RingtoneManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val RINGTONE_ENABLED = booleanPreferencesKey("ringtone_enabled")
        val RINGTONE_URI = stringPreferencesKey("ringtone_uri")
        val KEYPAD_TONES_ENABLED = booleanPreferencesKey("keypad_tones_enabled")
        val CALL_SCREEN_BACKGROUND = stringPreferencesKey("call_screen_background")
        val THEME_MODE = intPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DEFAULT_SIM = stringPreferencesKey("default_sim_id")
        val VIBRATE_ON_ANSWER = booleanPreferencesKey("vibrate_on_answer")
        val FLASH_ON_CALL = booleanPreferencesKey("flash_on_call")
        val SHOW_CONTACT_PHOTO = booleanPreferencesKey("show_contact_photo")
        val AUTO_RECORD_ALL = booleanPreferencesKey("auto_record_all")
        val AUTO_RECORD_UNKNOWN = booleanPreferencesKey("auto_record_unknown")
        val AUTO_RECORD_CONTACTS = booleanPreferencesKey("auto_record_contacts")
        
        // Customizations
        val CONTACT_SORT_ORDER = intPreferencesKey("contact_sort_order") // 0: First Name, 1: Last Name
        val CONTACT_DISPLAY_NAME_FORMAT = intPreferencesKey("contact_display_name_format") // 0: First Last, 1: Last First
        val FULL_SCREEN_INTENT_ENABLED = booleanPreferencesKey("full_screen_intent_enabled")
        val SHOW_POST_CALL_DETAILS = booleanPreferencesKey("show_post_call_details")
        val VIBRATION_PATTERN = intPreferencesKey("vibration_pattern") // 0: Basic, 1: Heartbeat, 2: Tick-tock
        
        // New Features
        val ANNOUNCE_CALLER_NAME = booleanPreferencesKey("announce_caller_name")
        val BLOCK_UNKNOWN_NUMBERS = booleanPreferencesKey("block_unknown_numbers")
        val SPAM_PROTECTION_ENABLED = booleanPreferencesKey("spam_protection_enabled")
        val FLIP_TO_SILENCE = booleanPreferencesKey("flip_to_silence")
        
        // More Features
        val ONE_HANDED_MODE = intPreferencesKey("one_handed_mode") // 0: Disabled, 1: Left, 2: Right
        val DIAL_PAD_SOUND_THEME = intPreferencesKey("dial_pad_sound_theme") // 0: Default, 1: Piano, 2: Retro
        val SWIPE_TO_CALL_ENABLED = booleanPreferencesKey("swipe_to_call_enabled")
        
        // Even More Features
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val FULL_SCREEN_CALLER_PHOTO = booleanPreferencesKey("full_screen_caller_photo")
        val AUTO_ANSWER_ENABLED = booleanPreferencesKey("auto_answer_enabled")
        val AUTO_ANSWER_DELAY = intPreferencesKey("auto_answer_delay") // Seconds
        val PROXIMITY_SENSOR_ENABLED = booleanPreferencesKey("proximity_sensor_enabled")

        // Speed Dial (Keys 2-9, as 1 is usually Voicemail)
        fun speedDialKey(digit: Int) = stringPreferencesKey("speed_dial_$digit")
    }

    val ringtoneEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RINGTONE_ENABLED] ?: true
    }

    val ringtoneUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RINGTONE_URI] ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString()
    }

    val keypadTonesEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.KEYPAD_TONES_ENABLED] ?: true
    }

    val callScreenBackground: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CALL_SCREEN_BACKGROUND]
    }

    val themeMode: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: 0 // 0: System, 1: Light, 2: Dark
    }

    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
    }

    val defaultSimId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_SIM]
    }

    val vibrateOnAnswer: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIBRATE_ON_ANSWER] ?: true
    }

    val flashOnCall: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FLASH_ON_CALL] ?: false
    }

    val showContactPhoto: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_CONTACT_PHOTO] ?: true
    }

    val autoRecordAll: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_RECORD_ALL] ?: false
    }

    val autoRecordUnknown: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_RECORD_UNKNOWN] ?: false
    }

    val autoRecordContacts: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_RECORD_CONTACTS] ?: false
    }

    val contactSortOrder: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CONTACT_SORT_ORDER] ?: 0
    }

    val contactDisplayNameFormat: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CONTACT_DISPLAY_NAME_FORMAT] ?: 0
    }

    val showPostCallDetails: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_POST_CALL_DETAILS] ?: true
    }

    val vibrationPattern: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIBRATION_PATTERN] ?: 0
    }

    val announceCallerName: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ANNOUNCE_CALLER_NAME] ?: false
    }

    val blockUnknownNumbers: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BLOCK_UNKNOWN_NUMBERS] ?: false
    }

    val spamProtectionEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SPAM_PROTECTION_ENABLED] ?: true
    }

    val flipToSilence: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FLIP_TO_SILENCE] ?: false
    }
    
    val oneHandedMode: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONE_HANDED_MODE] ?: 0
    }

    val dialPadSoundTheme: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DIAL_PAD_SOUND_THEME] ?: 0
    }

    val swipeToCallEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SWIPE_TO_CALL_ENABLED] ?: true
    }

    val hapticFeedbackEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] ?: true
    }

    val fullScreenCallerPhoto: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FULL_SCREEN_CALLER_PHOTO] ?: false
    }

    val autoAnswerEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_ANSWER_ENABLED] ?: false
    }

    val autoAnswerDelay: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_ANSWER_DELAY] ?: 5
    }

    val proximitySensorEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PROXIMITY_SENSOR_ENABLED] ?: true
    }

    fun getSpeedDial(digit: Int): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.speedDialKey(digit)]
    }

    suspend fun setRingtoneEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RINGTONE_ENABLED] = enabled
        }
    }

    suspend fun setRingtoneUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) preferences.remove(PreferencesKeys.RINGTONE_URI)
            else preferences[PreferencesKeys.RINGTONE_URI] = uri
        }
    }

    suspend fun setKeypadTonesEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEYPAD_TONES_ENABLED] = enabled
        }
    }

    suspend fun setCallScreenBackground(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) preferences.remove(PreferencesKeys.CALL_SCREEN_BACKGROUND)
            else preferences[PreferencesKeys.CALL_SCREEN_BACKGROUND] = uri
        }
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setDefaultSimId(simId: String?) {
        context.dataStore.edit { preferences ->
            if (simId == null) preferences.remove(PreferencesKeys.DEFAULT_SIM)
            else preferences[PreferencesKeys.DEFAULT_SIM] = simId
        }
    }

    suspend fun setVibrateOnAnswer(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATE_ON_ANSWER] = enabled
        }
    }

    suspend fun setFlashOnCall(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FLASH_ON_CALL] = enabled
        }
    }

    suspend fun setShowContactPhoto(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_CONTACT_PHOTO] = enabled
        }
    }

    suspend fun setAutoRecordAll(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_RECORD_ALL] = enabled
        }
    }

    suspend fun setAutoRecordUnknown(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_RECORD_UNKNOWN] = enabled
        }
    }

    suspend fun setAutoRecordContacts(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_RECORD_CONTACTS] = enabled
        }
    }

    suspend fun setContactSortOrder(order: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTACT_SORT_ORDER] = order
        }
    }

    suspend fun setContactDisplayNameFormat(format: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTACT_DISPLAY_NAME_FORMAT] = format
        }
    }

    suspend fun setShowPostCallDetails(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_POST_CALL_DETAILS] = show
        }
    }

    suspend fun setVibrationPattern(pattern: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_PATTERN] = pattern
        }
    }

    suspend fun setAnnounceCallerName(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANNOUNCE_CALLER_NAME] = enabled
        }
    }

    suspend fun setBlockUnknownNumbers(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BLOCK_UNKNOWN_NUMBERS] = enabled
        }
    }

    suspend fun setSpamProtectionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SPAM_PROTECTION_ENABLED] = enabled
        }
    }

    suspend fun setFlipToSilence(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FLIP_TO_SILENCE] = enabled
        }
    }
    
    suspend fun setOneHandedMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONE_HANDED_MODE] = mode
        }
    }

    suspend fun setDialPadSoundTheme(theme: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DIAL_PAD_SOUND_THEME] = theme
        }
    }

    suspend fun setSwipeToCallEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SWIPE_TO_CALL_ENABLED] = enabled
        }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    suspend fun setFullScreenCallerPhoto(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_SCREEN_CALLER_PHOTO] = enabled
        }
    }

    suspend fun setAutoAnswerEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_ANSWER_ENABLED] = enabled
        }
    }

    suspend fun setAutoAnswerDelay(delay: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_ANSWER_DELAY] = delay
        }
    }

    suspend fun setProximitySensorEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROXIMITY_SENSOR_ENABLED] = enabled
        }
    }

    suspend fun setSpeedDial(digit: Int, number: String?) {
        context.dataStore.edit { preferences ->
            if (number == null) preferences.remove(PreferencesKeys.speedDialKey(digit))
            else preferences[PreferencesKeys.speedDialKey(digit)] = number
        }
    }
}
