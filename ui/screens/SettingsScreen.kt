@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

import android.media.RingtoneManager
import android.net.Uri
import android.telecom.PhoneAccountHandle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.navigation.Navigator
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.navigation.rememberNavigationState
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.SimSelectionDialog
import com.coderon.phone.ui.utils.extentions.getSimName
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    navigator: Navigator,
    ringtoneEnabled: Boolean,
    onRingtoneToggled: (Boolean) -> Unit,
    ringtoneUri: String?,
    onRingtoneUriChanged: (String?) -> Unit,
    keypadTonesEnabled: Boolean,
    onKeypadTonesToggled: (Boolean) -> Unit,
    themeMode: Int,
    onThemeModeChanged: (Int) -> Unit,
    dynamicColor: Boolean,
    onDynamicColorToggled: (Boolean) -> Unit,
    callScreenBackground: String?,
    onCallScreenBackgroundChanged: (String?) -> Unit,
    vibrateOnAnswer: Boolean,
    onVibrateOnAnswerToggled: (Boolean) -> Unit,
    flashOnCall: Boolean,
    onFlashOnCallToggled: (Boolean) -> Unit,
    showContactPhoto: Boolean,
    onShowContactPhotoToggled: (Boolean) -> Unit,
    defaultSimId: String?,
    availableSims: List<PhoneAccountHandle>,
    onDefaultSimChanged: (String?) -> Unit,
    autoRecordAll: Boolean = false,
    onAutoRecordAllToggled: (Boolean) -> Unit = {},
    autoRecordUnknown: Boolean = false,
    onAutoRecordUnknownToggled: (Boolean) -> Unit = {},
    autoRecordContacts: Boolean = false,
    onAutoRecordContactsToggled: (Boolean) -> Unit = {},
    contactSortOrder: Int = 0,
    onContactSortOrderChanged: (Int) -> Unit = {},
    contactDisplayNameFormat: Int = 0,
    onContactDisplayNameFormatChanged: (Int) -> Unit = {},
    showPostCallDetails: Boolean = true,
    onShowPostCallDetailsToggled: (Boolean) -> Unit = {},
    vibrationPattern: Int = 0,
    onVibrationPatternChanged: (Int) -> Unit = {},
    announceCallerName: Boolean = false,
    onAnnounceCallerNameToggled: (Boolean) -> Unit = {},
    blockUnknownNumbers: Boolean = false,
    onBlockUnknownNumbersToggled: (Boolean) -> Unit = {},
    spamProtectionEnabled: Boolean = true,
    onSpamProtectionToggled: (Boolean) -> Unit = {},
    flipToSilence: Boolean = false,
    onFlipToSilenceToggled: (Boolean) -> Unit = {},
    oneHandedMode: Int = 0,
    onOneHandedModeChanged: (Int) -> Unit = {},
    dialPadSoundTheme: Int = 0,
    onDialPadSoundThemeChanged: (Int) -> Unit = {},
    swipeToCallEnabled: Boolean = true,
    onSwipeToCallEnabledToggled: (Boolean) -> Unit = {},
    hapticFeedbackEnabled: Boolean = true,
    onHapticFeedbackToggled: (Boolean) -> Unit = {},
    fullScreenCallerPhoto: Boolean = false,
    onFullScreenCallerPhotoToggled: (Boolean) -> Unit = {},
    autoAnswerEnabled: Boolean = false,
    onAutoAnswerEnabledToggled: (Boolean) -> Unit = {},
    autoAnswerDelay: Int = 5,
    onAutoAnswerDelayChanged: (Int) -> Unit = {},
    proximitySensorEnabled: Boolean = true,
    onProximitySensorEnabledToggled: (Boolean) -> Unit = {},
    onNavigateToBlockedNumbers: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(600, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onCallScreenBackgroundChanged(it.toString()) }
    }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        onRingtoneUriChanged(uri?.toString())
    }

    var showSimDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text("Settings", fontWeight = FontWeight.Bold, fontSize = 34.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.goBack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    scrolledContainerColor = colorScheme.surfaceContainer.copy(alpha = 0.95f)
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .alpha(entryAlpha.value)
                .offset { IntOffset(0, entryOffset.value.roundToInt()) },
            contentPadding = PaddingValues(bottom = 40.dp, top = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Communication Section
            item {
                SettingsSection(title = "COMMUNICATION") {
                    SettingsRow(
                        icon = Icons.Rounded.Voicemail,
                        title = "Voicemail",
                        onClick = { navigator.navigate(Screen.Voicemail) }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.Block,
                        title = "Blocked Numbers",
                        onClick = onNavigateToBlockedNumbers
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Security,
                        title = "Block unknown numbers",
                        checked = blockUnknownNumbers,
                        onCheckedChange = onBlockUnknownNumbersToggled
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.VerifiedUser,
                        title = "Spam Protection",
                        checked = spamProtectionEnabled,
                        onCheckedChange = onSpamProtectionToggled
                    )
                }
            }

            // Contacts Customization
            item {
                SettingsSection(title = "CONTACTS") {
                    SettingsRow(
                        icon = Icons.Rounded.SortByAlpha,
                        title = "Sort order",
                        subtitle = if (contactSortOrder == 0) "First name" else "Last name",
                        onClick = { onContactSortOrderChanged(if (contactSortOrder == 0) 1 else 0) }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.DisplaySettings,
                        title = "Name format",
                        subtitle = if (contactDisplayNameFormat == 0) "First name first" else "Last name first",
                        onClick = { onContactDisplayNameFormatChanged(if (contactDisplayNameFormat == 0) 1 else 0) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Image,
                        title = "Show contact photos",
                        checked = showContactPhoto,
                        onCheckedChange = onShowContactPhotoToggled
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Swipe,
                        title = "Swipe to call or message",
                        checked = swipeToCallEnabled,
                        onCheckedChange = onSwipeToCallEnabledToggled
                    )
                }
            }

            // Call Recording Section
            item {
                SettingsSection(title = "CALL RECORDING") {
                    SettingsToggleRow(
                        icon = Icons.Rounded.Mic,
                        title = "Auto record all calls",
                        checked = autoRecordAll,
                        onCheckedChange = onAutoRecordAllToggled
                    )
                    if (!autoRecordAll) {
                        SettingsDivider()
                        SettingsToggleRow(
                            icon = Icons.Rounded.Mic,
                            title = "Auto record unknown numbers",
                            checked = autoRecordUnknown,
                            onCheckedChange = onAutoRecordUnknownToggled
                        )
                        SettingsDivider()
                        SettingsToggleRow(
                            icon = Icons.Rounded.Mic,
                            title = "Auto record contacts",
                            checked = autoRecordContacts,
                            onCheckedChange = onAutoRecordContactsToggled
                        )
                    }
                }
            }

            // Audio & Feedback Section
            item {
                SettingsSection(title = "AUDIO & FEEDBACK") {
                    SettingsToggleRow(
                        icon = Icons.Rounded.NotificationsActive,
                        title = "Ringtone",
                        checked = ringtoneEnabled,
                        onCheckedChange = onRingtoneToggled
                    )
                    if (ringtoneEnabled) {
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Rounded.MusicNote,
                            title = "Select Ringtone",
                            onClick = {
                                val intent = android.content.Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                                    .apply {
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone")
                                        ringtoneUri?.let { putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(it)) }
                                    }
                                ringtonePickerLauncher.launch(intent)
                            }
                        )
                    }
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Keyboard,
                        title = "Keypad Tones",
                        checked = keypadTonesEnabled,
                        onCheckedChange = onKeypadTonesToggled
                    )
                    if (keypadTonesEnabled) {
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Rounded.Audiotrack,
                            title = "Keypad sound theme",
                            subtitle = when (dialPadSoundTheme) {
                                1 -> "Piano"; 2 -> "Retro"; else -> "Default"
                            },
                            onClick = { onDialPadSoundThemeChanged((dialPadSoundTheme + 1) % 3) }
                        )
                    }
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.RecordVoiceOver,
                        title = "Announce caller name",
                        checked = announceCallerName,
                        onCheckedChange = onAnnounceCallerNameToggled
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Vibration,
                        title = "Haptic feedback",
                        checked = hapticFeedbackEnabled,
                        onCheckedChange = onHapticFeedbackToggled
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.Vibration,
                        title = "Vibration pattern",
                        subtitle = when (vibrationPattern) {
                            1 -> "Heartbeat"; 2 -> "Tick-tock"; else -> "Basic"
                        },
                        onClick = { onVibrationPatternChanged((vibrationPattern + 1) % 3) }
                    )
                }
            }

            // Customization Section
            item {
                SettingsSection(title = "LOOK & FEEL") {
                    SettingsRow(
                        icon = Icons.Rounded.Palette,
                        title = "Theme",
                        subtitle = when (themeMode) {
                            1 -> "Light"; 2 -> "Dark"; else -> "System"
                        },
                        onClick = { onThemeModeChanged((themeMode + 1) % 3) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.ColorLens,
                        title = "Dynamic Color",
                        checked = dynamicColor,
                        onCheckedChange = onDynamicColorToggled
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.AdsClick,
                        title = "One-handed mode",
                        subtitle = when (oneHandedMode) {
                            1 -> "Left side"; 2 -> "Right side"; else -> "Disabled"
                        },
                        onClick = { onOneHandedModeChanged((oneHandedMode + 1) % 3) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.PhotoSizeSelectActual,
                        title = "Full screen caller photo",
                        checked = fullScreenCallerPhoto,
                        onCheckedChange = onFullScreenCallerPhotoToggled
                    )
                    SettingsDivider()
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Wallpaper,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                "Call Screen Background",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            if (callScreenBackground != null) {
                                IconButton(
                                    onClick = { onCallScreenBackgroundChanged(null) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = "Clear",
                                        tint = Color.Red.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colorScheme.surfaceContainerHighest)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (callScreenBackground != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(callScreenBackground),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Rounded.Image,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        "Tap to select",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Call Handling Section
            item {
                SettingsSection(title = "CALL HANDLING") {
                    val currentSimName = availableSims.find { it.id == defaultSimId }?.getSimName(context) ?: "Ask every time"
                    SettingsRow(
                        icon = Icons.Rounded.SimCard,
                        title = "Default SIM for calls",
                        subtitle = currentSimName,
                        onClick = { showSimDialog = true }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Vibration,
                        title = "Vibrate on answer",
                        checked = vibrateOnAnswer,
                        onCheckedChange = onVibrateOnAnswerToggled
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.FlashOn,
                        title = "Flash on incoming call",
                        checked = flashOnCall,
                        onCheckedChange = onFlashOnCallToggled
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.History,
                        title = "Show post-call summary",
                        checked = showPostCallDetails,
                        onCheckedChange = onShowPostCallDetailsToggled
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.PhonelinkRing,
                        title = "Flip to silence",
                        checked = flipToSilence,
                        onCheckedChange = onFlipToSilenceToggled
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.AutoMode,
                        title = "Auto-answer calls",
                        checked = autoAnswerEnabled,
                        onCheckedChange = onAutoAnswerEnabledToggled
                    )
                    if (autoAnswerEnabled) {
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Rounded.Timer,
                            title = "Auto-answer delay",
                            subtitle = "$autoAnswerDelay seconds",
                            onClick = { onAutoAnswerDelayChanged(if (autoAnswerDelay >= 10) 2 else autoAnswerDelay + 2) }
                        )
                    }
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Sensors,
                        title = "Use proximity sensor",
                        checked = proximitySensorEnabled,
                        onCheckedChange = onProximitySensorEnabledToggled
                    )
                }
            }

            // Advanced Section
            item {
                SettingsSection(title = "ABOUT") {
                    SettingsRow(
                        icon = Icons.Rounded.Feedback,
                        title = "Share Feedback",
                        onClick = { /* Action */ }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.Info,
                        title = "App Version",
                        subtitle = "1.0.0 (Coderon Phone)",
                        onClick = {}
                    )
                }
            }
        }

        if (showSimDialog) {
            SimSelectionDialog(
                availableAccounts = availableSims,
                includeAskEveryTime = true,
                onSimSelected = { handle ->
                    onDefaultSimChanged(handle?.id)
                    showSimDialog = false
                },
                onDismiss = { showSimDialog = false }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 28.dp, bottom = 12.dp),
            letterSpacing = 1.sp
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.dp
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colorScheme.surfaceContainerHighest,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 80.dp, end = 24.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettingsScreen() {
    val navState = rememberNavigationState(
        startRoute = Screen.Settings,
        topLevelRoutes = setOf(Screen.Keypad, Screen.Recent, Screen.Contacts, Screen.Search)
    )
    val navigator = remember { Navigator(navState) }

    PhoneTheme {
        SettingsScreen(
            navigator = navigator,
            ringtoneEnabled = true,
            onRingtoneToggled = {},
            ringtoneUri = null,
            onRingtoneUriChanged = {},
            keypadTonesEnabled = true,
            onKeypadTonesToggled = {},
            themeMode = 0,
            onThemeModeChanged = {},
            dynamicColor = true,
            onDynamicColorToggled = {},
            callScreenBackground = null,
            onCallScreenBackgroundChanged = {},
            vibrateOnAnswer = true,
            onVibrateOnAnswerToggled = {},
            flashOnCall = false,
            onFlashOnCallToggled = {},
            showContactPhoto = true,
            onShowContactPhotoToggled = {},
            defaultSimId = null,
            availableSims = emptyList(),
            onDefaultSimChanged = {},
            onNavigateToBlockedNumbers = {}
        )
    }
}
