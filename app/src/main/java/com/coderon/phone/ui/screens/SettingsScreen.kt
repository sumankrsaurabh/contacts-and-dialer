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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Voicemail
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    onNavigateToBlockedNumbers: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

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
        containerColor = colorScheme.background,
        topBar = {
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(24.dp)
                        .background(colorScheme.background.copy(alpha = 0.65f))
                )

                LargeTopAppBar(
                    title = {
                        Text("Settings", fontWeight = FontWeight.Bold, fontSize = 32.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.goBack() }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = colorScheme.surfaceContainer.copy(alpha = 0.9f)
                    )
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .alpha(entryAlpha.value)
                .offset { IntOffset(0, entryOffset.value.roundToInt()) },
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
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

            // General Section
            item {
                SettingsSection(title = "GENERAL") {
                    SettingsToggleRow(
                        icon = Icons.Rounded.Notifications,
                        title = "Ringtone",
                        checked = ringtoneEnabled,
                        onCheckedChange = onRingtoneToggled
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.MusicNote,
                        title = "Select Ringtone",
                        onClick = {
                            val intent = android.content.Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone")
                                ringtoneUri?.let {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(it))
                                }
                            }
                            ringtonePickerLauncher.launch(intent)
                        }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Notifications,
                        title = "Keypad Tones",
                        checked = keypadTonesEnabled,
                        onCheckedChange = onKeypadTonesToggled
                    )
                }
            }

            // Customization Section
            item {
                SettingsSection(title = "CUSTOMIZATION") {
                    SettingsRow(
                        icon = Icons.Rounded.Palette,
                        title = "Theme",
                        subtitle = when(themeMode) { 1 -> "Light"; 2 -> "Dark"; else -> "System" },
                        onClick = { 
                            val nextMode = (themeMode + 1) % 3
                            onThemeModeChanged(nextMode)
                        }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.ColorLens,
                        title = "Dynamic Color",
                        checked = dynamicColor,
                        onCheckedChange = onDynamicColorToggled
                    )
                    SettingsDivider()
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Image, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(16.dp))
                            Text("Call Screen Background", fontSize = 17.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            if (callScreenBackground != null) {
                                IconButton(onClick = { onCallScreenBackgroundChanged(null) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Clear", tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
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
                                    Icon(Icons.Rounded.Image, contentDescription = null, tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
                                    Text("Tap to select", fontSize = 12.sp, color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Image,
                        title = "Show Contact Photos",
                        checked = showContactPhoto,
                        onCheckedChange = onShowContactPhotoToggled
                    )
                }
            }

            // Call Section
            item {
                SettingsSection(title = "CALLS") {
                    val currentSimName = availableSims.find { it.id == defaultSimId }?.getSimName(context) ?: "Ask every time"
                    SettingsRow(
                        icon = Icons.Rounded.Call,
                        title = "Default SIM",
                        subtitle = currentSimName,
                        onClick = { showSimDialog = true }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Vibration,
                        title = "Vibrate on Answer",
                        checked = vibrateOnAnswer,
                        onCheckedChange = onVibrateOnAnswerToggled
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.FlashOn,
                        title = "Flash on Incoming Call",
                        checked = flashOnCall,
                        onCheckedChange = onFlashOnCallToggled
                    )
                }
            }

            // Advanced Section
            item {
                SettingsSection(title = "ADVANCED") {
                    SettingsToggleRow(
                        icon = Icons.Rounded.Security,
                        title = "Spam Protection",
                        checked = true,
                        onCheckedChange = { /* Update State */ }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.Share,
                        title = "Share Feedback",
                        onClick = { /* Action */ }
                    )
                }
            }
        }

        if (showSimDialog) {
            SimSelectionDialog(
                availableAccounts = availableSims,
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
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
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
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
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
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
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
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
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
                uncheckedThumbColor = colorScheme.outline,
                uncheckedTrackColor = colorScheme.surfaceContainerHighest
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp, end = 24.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
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
