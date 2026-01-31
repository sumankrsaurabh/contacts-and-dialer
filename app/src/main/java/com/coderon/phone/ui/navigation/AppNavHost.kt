package com.coderon.phone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.coderon.phone.call.ui.screens.incallui.CallScreen
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.screens.AddContactScreen
import com.coderon.phone.ui.screens.BlockedNumbersScreen
import com.coderon.phone.ui.screens.CallLogScreen
import com.coderon.phone.ui.screens.ContactDetailsScreen
import com.coderon.phone.ui.screens.ContactsScreen
import com.coderon.phone.ui.screens.DialerScreen
import com.coderon.phone.ui.screens.PostCallSummaryScreen
import com.coderon.phone.ui.screens.SearchScreen
import com.coderon.phone.ui.screens.SettingsScreen
import com.coderon.phone.ui.screens.SpeedDialScreen
import com.coderon.phone.ui.screens.VoicemailScreen
import com.coderon.phone.ui.utils.ScaffoldScreen
import com.coderon.phone.utils.normalizePhoneNumber
import com.coderon.phone.utils.playTones
import com.coderon.phone.viewmodel.BlockedNumbersViewModel
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import com.coderon.phone.viewmodel.SettingsViewModel
import com.coderon.phone.viewmodel.VoicemailViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    navigator: Navigator,
    contactViewModel: ContactViewModel = koinViewModel(),
    callLogViewModel: CallLogViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    blockedNumbersViewModel: BlockedNumbersViewModel = koinViewModel(),
    voicemailViewModel: VoicemailViewModel = koinViewModel()
) {
    val groupedContacts by contactViewModel.groupedContacts.collectAsStateWithLifecycle()
    val allContacts by contactViewModel.allContacts.collectAsStateWithLifecycle()
    val filteredContacts by contactViewModel.filteredContacts.collectAsStateWithLifecycle()

    val callLogsByDate by callLogViewModel.callLogsByDate.collectAsStateWithLifecycle()
    val callFilter by callLogViewModel.filter.collectAsStateWithLifecycle()

    val keypadTonesEnabled by settingsViewModel.keypadTonesEnabled.collectAsStateWithLifecycle()
    val ringtoneEnabled by settingsViewModel.ringtoneEnabled.collectAsStateWithLifecycle()
    val ringtoneUri by settingsViewModel.ringtoneUri.collectAsStateWithLifecycle()
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val dynamicColor by settingsViewModel.dynamicColor.collectAsStateWithLifecycle()
    val backgroundUri by settingsViewModel.callScreenBackground.collectAsStateWithLifecycle()
    val vibrateOnAnswer by settingsViewModel.vibrateOnAnswer.collectAsStateWithLifecycle()
    val flashOnCall by settingsViewModel.flashOnCall.collectAsStateWithLifecycle()
    val showContactPhoto by settingsViewModel.showContactPhoto.collectAsStateWithLifecycle()
    val defaultSimId by settingsViewModel.defaultSimId.collectAsStateWithLifecycle()
    val availableSims by settingsViewModel.availableSims.collectAsStateWithLifecycle()
    
    val autoRecordAll by settingsViewModel.autoRecordAll.collectAsStateWithLifecycle()
    val autoRecordUnknown by settingsViewModel.autoRecordUnknown.collectAsStateWithLifecycle()
    val autoRecordContacts by settingsViewModel.autoRecordContacts.collectAsStateWithLifecycle()

    val contactSortOrder by settingsViewModel.contactSortOrder.collectAsStateWithLifecycle()
    val contactDisplayNameFormat by settingsViewModel.contactDisplayNameFormat.collectAsStateWithLifecycle()
    val showPostCallDetails by settingsViewModel.showPostCallDetails.collectAsStateWithLifecycle()
    val vibrationPattern by settingsViewModel.vibrationPattern.collectAsStateWithLifecycle()
    val oneHandedMode by settingsViewModel.oneHandedMode.collectAsStateWithLifecycle()
    val hapticFeedbackEnabled by settingsViewModel.hapticFeedbackEnabled.collectAsStateWithLifecycle()
    val swipeToCallEnabled by settingsViewModel.swipeToCallEnabled.collectAsStateWithLifecycle()
    val speedDials by settingsViewModel.speedDials.collectAsStateWithLifecycle()
    val dialPadSoundTheme by settingsViewModel.dialPadSoundTheme.collectAsStateWithLifecycle()

    val announceCallerName by settingsViewModel.announceCallerName.collectAsStateWithLifecycle()
    val blockUnknownNumbers by settingsViewModel.blockUnknownNumbers.collectAsStateWithLifecycle()
    val spamProtectionEnabled by settingsViewModel.spamProtectionEnabled.collectAsStateWithLifecycle()
    val flipToSilence by settingsViewModel.flipToSilence.collectAsStateWithLifecycle()
    val fullScreenCallerPhoto by settingsViewModel.fullScreenCallerPhoto.collectAsStateWithLifecycle()
    val autoAnswerEnabled by settingsViewModel.autoAnswerEnabled.collectAsStateWithLifecycle()
    val autoAnswerDelay by settingsViewModel.autoAnswerDelay.collectAsStateWithLifecycle()
    val proximitySensorEnabled by settingsViewModel.proximitySensorEnabled.collectAsStateWithLifecycle()

    val blockedNumbers by blockedNumbersViewModel.blockedNumbers.collectAsStateWithLifecycle()
    val voicemails by voicemailViewModel.voicemails.collectAsStateWithLifecycle()

    val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
        fun updateSearchQuery(query: String) {
            contactViewModel.onSearchQueryChanged(query)
            callLogViewModel.onSearchQueryChanged(query)
        }

        /* -------------------- KEYPAD -------------------- */
        entry<Screen.Keypad> {
            ScaffoldScreen(navigator) {
                DialerScreen(
                    navigator = navigator,
                    contactsGrouped = groupedContacts,
                    callLogsGrouped = callLogsByDate,
                    updateSearchQuery = ::updateSearchQuery,
                    playTones = { if (keypadTonesEnabled) playTones(it, dialPadSoundTheme) },
                    defaultSimId = defaultSimId,
                    showContactPhoto = showContactPhoto,
                    oneHandedMode = oneHandedMode,
                    hapticFeedbackEnabled = hapticFeedbackEnabled,
                    onSpeedDial = { digit -> speedDials[digit] }
                )
            }
        }

        /* -------------------- RECENT -------------------- */
        entry<Screen.Recent> {
            ScaffoldScreen(navigator) {
                CallLogScreen(
                    callLogsByDate = callLogsByDate,
                    filter = callFilter,
                    onFilterChanged = { callLogViewModel.onFilterChanged(it) },
                    onDeleteAllLogs = { callLogViewModel.deleteAllLogs() },
                    navigator = navigator,
                    defaultSimId = defaultSimId,
                    showContactPhoto = showContactPhoto,
                    swipeEnabled = swipeToCallEnabled
                )
            }
        }

        /* -------------------- CONTACTS -------------------- */
        entry<Screen.Contacts> {
            ScaffoldScreen(navigator) {
                ContactsScreen(
                    contactsGrouped = groupedContacts,
                    navigator = navigator,
                    defaultSimId = defaultSimId,
                    showContactPhoto = showContactPhoto,
                    displayNameFormat = contactDisplayNameFormat,
                    swipeEnabled = swipeToCallEnabled
                )
            }
        }

        /* -------------------- SEARCH -------------------- */
        entry<Screen.Search> {
            ScaffoldScreen(navigator) {
                SearchScreen(
                    navigator = navigator,
                    contacts = filteredContacts,
                    logs = callLogsByDate.values.flatten().flatMap { it.logs },
                    onBack = { navigator.goBack() },
                )
            }
        }

        /* -------------------- VOICEMAIL -------------------- */
        entry<Screen.Voicemail> {
            ScaffoldScreen(navigator, showBottomBar = false) {
                VoicemailScreen(
                    navigator = navigator,
                    voicemails = voicemails,
                    onDeleteVoicemail = { voicemailViewModel.deleteVoicemail(it) },
                    onPlayVoicemail = { /* Play logic */ }
                )
            }
        }

        /* -------------------- ADD/EDIT CONTACT -------------------- */
        entry<Screen.AddContact> { key ->
            val existingContact = if (key.contactId != null) {
                allContacts.firstOrNull { it.id == key.contactId }
            } else null

            AddContactScreen(
                navigator = navigator,
                initialPhoneNumber = key.number,
                existingContact = existingContact,
                onSaveContact = { contact ->
                    if (key.contactId != null) {
                        contactViewModel.updateContact(
                            contactId = key.contactId,
                            firstName = contact.firstName,
                            lastName = contact.lastName,
                            displayName = contact.displayName,
                            phoneNumbers = contact.phoneNumbers,
                            emailAddresses = contact.emailAddresses,
                            profilePictureUri = contact.profilePictureUrl,
                            isFavorite = contact.isFavorite
                        )
                    } else {
                        contactViewModel.saveContact(
                            firstName = contact.firstName,
                            lastName = contact.lastName,
                            displayName = contact.displayName,
                            phoneNumbers = contact.phoneNumbers,
                            emailAddresses = contact.emailAddresses,
                            profilePictureUri = contact.profilePictureUrl,
                            isFavorite = contact.isFavorite
                        )
                    }
                }
            )
        }

        /* -------------------- CONTACT DETAILS -------------------- */
        entry<Screen.CallDetails> { key ->
            val normalizedRouteNumber = normalizePhoneNumber(key.phoneNumber)

            val callLogsForNumber by callLogViewModel
                .getCallLogsForNumber(normalizedRouteNumber)
                .collectAsStateWithLifecycle(emptyList())

            val matchedContact = allContacts.firstOrNull { contact ->
                contact.phoneNumbers.any { phone ->
                    normalizePhoneNumber(phone.number) == normalizedRouteNumber
                }
            }

            ContactDetailsScreen(
                contact = matchedContact ?: Contact(
                    displayName = normalizedRouteNumber,
                    phoneNumbers = listOf(
                        PhoneNumber(
                            number = normalizedRouteNumber,
                            isPrimary = true
                        )
                    )
                ),
                callLogs = callLogsForNumber,
                navigator = navigator,
                onToggleFavorite = { contactViewModel.toggleFavorite(it) },
                onEditContact = { contact ->
                    navigator.navigate(Screen.AddContact(contactId = contact.id))
                },
                defaultSimId = defaultSimId,
                showContactPhoto = showContactPhoto
            )
        }

        /* -------------------- INCALL UI -------------------- */
        entry<Screen.CallScreen> {
            CallScreen(
                navigator = navigator,
                backgroundUri = backgroundUri,
                showContactPhoto = showContactPhoto,
                fullScreenCallerPhoto = fullScreenCallerPhoto,
                keypadTonesEnabled = keypadTonesEnabled
            )
        }

        /* -------------------- POST CALL SUMMARY -------------------- */
        entry<Screen.PostCallSummary> { key ->
            PostCallSummaryScreen(
                phoneNumber = key.phoneNumber,
                duration = key.duration,
                isIncoming = key.isIncoming,
                timestamp = key.timestamp,
                navigator = navigator
            )
        }

        /* -------------------- SETTINGS -------------------- */
        entry<Screen.Settings> {
            SettingsScreen(
                navigator = navigator,
                ringtoneEnabled = ringtoneEnabled,
                onRingtoneToggled = { settingsViewModel.setRingtoneEnabled(it) },
                ringtoneUri = ringtoneUri,
                onRingtoneUriChanged = { settingsViewModel.setRingtoneUri(it) },
                keypadTonesEnabled = keypadTonesEnabled,
                onKeypadTonesToggled = { settingsViewModel.setKeypadTonesEnabled(it) },
                themeMode = themeMode,
                onThemeModeChanged = { settingsViewModel.setThemeMode(it) },
                dynamicColor = dynamicColor,
                onDynamicColorToggled = { settingsViewModel.setDynamicColor(it) },
                callScreenBackground = backgroundUri,
                onCallScreenBackgroundChanged = { settingsViewModel.setCallScreenBackground(it) },
                vibrateOnAnswer = vibrateOnAnswer,
                onVibrateOnAnswerToggled = { settingsViewModel.setVibrateOnAnswer(it) },
                flashOnCall = flashOnCall,
                onFlashOnCallToggled = { settingsViewModel.setFlashOnCall(it) },
                showContactPhoto = showContactPhoto,
                onShowContactPhotoToggled = { settingsViewModel.setShowContactPhoto(it) },
                defaultSimId = defaultSimId,
                availableSims = availableSims,
                onDefaultSimChanged = { settingsViewModel.setDefaultSimId(it) },
                autoRecordAll = autoRecordAll,
                onAutoRecordAllToggled = { settingsViewModel.setAutoRecordAll(it) },
                autoRecordUnknown = autoRecordUnknown,
                onAutoRecordUnknownToggled = { settingsViewModel.setAutoRecordUnknown(it) },
                autoRecordContacts = autoRecordContacts,
                onAutoRecordContactsToggled = { settingsViewModel.setAutoRecordContacts(it) },
                contactSortOrder = contactSortOrder,
                onContactSortOrderChanged = { settingsViewModel.setContactSortOrder(it) },
                contactDisplayNameFormat = contactDisplayNameFormat,
                onContactDisplayNameFormatChanged = { settingsViewModel.setContactDisplayNameFormat(it) },
                showPostCallDetails = showPostCallDetails,
                onShowPostCallDetailsToggled = { settingsViewModel.setShowPostCallDetails(it) },
                vibrationPattern = vibrationPattern,
                onVibrationPatternChanged = { settingsViewModel.setVibrationPattern(it) },
                announceCallerName = announceCallerName,
                onAnnounceCallerNameToggled = { settingsViewModel.setAnnounceCallerName(it) },
                blockUnknownNumbers = blockUnknownNumbers,
                onBlockUnknownNumbersToggled = { settingsViewModel.setBlockUnknownNumbers(it) },
                spamProtectionEnabled = spamProtectionEnabled,
                onSpamProtectionToggled = { settingsViewModel.setSpamProtectionEnabled(it) },
                flipToSilence = flipToSilence,
                onFlipToSilenceToggled = { settingsViewModel.setFlipToSilence(it) },
                oneHandedMode = oneHandedMode,
                onOneHandedModeChanged = { settingsViewModel.setOneHandedMode(it) },
                dialPadSoundTheme = dialPadSoundTheme,
                onDialPadSoundThemeChanged = { settingsViewModel.setDialPadSoundTheme(it) },
                swipeToCallEnabled = swipeToCallEnabled,
                onSwipeToCallEnabledToggled = { settingsViewModel.setSwipeToCallEnabled(it) },
                hapticFeedbackEnabled = hapticFeedbackEnabled,
                onHapticFeedbackToggled = { settingsViewModel.setHapticFeedbackEnabled(it) },
                fullScreenCallerPhoto = fullScreenCallerPhoto,
                onFullScreenCallerPhotoToggled = { settingsViewModel.setFullScreenCallerPhoto(it) },
                autoAnswerEnabled = autoAnswerEnabled,
                onAutoAnswerEnabledToggled = { settingsViewModel.setAutoAnswerEnabled(it) },
                autoAnswerDelay = autoAnswerDelay,
                onAutoAnswerDelayChanged = { settingsViewModel.setAutoAnswerDelay(it) },
                proximitySensorEnabled = proximitySensorEnabled,
                onProximitySensorEnabledToggled = { settingsViewModel.setProximitySensorEnabled(it) },
                onNavigateToBlockedNumbers = { navigator.navigate(Screen.BlockedNumbers) },
                onNavigateToSpeedDial = { navigator.navigate(Screen.SpeedDial) }
            )
        }

        /* -------------------- BLOCKED NUMBERS -------------------- */
        entry<Screen.BlockedNumbers> {
            BlockedNumbersScreen(
                navigator = navigator,
                blockedNumbers = blockedNumbers,
                onBlockNumber = { blockedNumbersViewModel.blockNumber(it) },
                onUnblockNumber = { blockedNumbersViewModel.unblockNumber(it) }
            )
        }

        /* -------------------- SPEED DIAL -------------------- */
        entry<Screen.SpeedDial> {
            SpeedDialScreen(
                navigator = navigator,
                speedDials = speedDials,
                contacts = allContacts,
                onSetSpeedDial = { digit, number -> settingsViewModel.setSpeedDial(digit, number) }
            )
        }
    }

    NavDisplay(
        entries = navigator.state.toEntries(entryProvider),
        onBack = { navigator.goBack() }
    )
}
