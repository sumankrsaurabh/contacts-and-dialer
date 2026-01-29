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
}
