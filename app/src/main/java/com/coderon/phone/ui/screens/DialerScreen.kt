package com.coderon.phone.ui.screens

import android.telecom.PhoneAccountHandle
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.components.DialPad
import com.coderon.phone.ui.components.HybridCallLogPill
import com.coderon.phone.ui.components.HybridContactRow
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.navigation.Navigator
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.navigation.rememberNavigationState
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.LocalBottomNavVisible
import com.coderon.phone.ui.utils.SimSelectionDialog
import com.coderon.phone.utils.getAvailableSims
import com.coderon.phone.utils.initiateCall
import com.coderon.phone.utils.placeCall
import com.coderon.phone.viewmodel.GroupedCallLog
import kotlinx.coroutines.launch
import java.util.SortedMap
import java.util.TreeMap
import kotlin.math.roundToInt

@Composable
fun DialerScreen(
    navigator: Navigator,
    contactsGrouped: SortedMap<Char, List<Contact>>,
    callLogsGrouped: Map<String, List<GroupedCallLog>>,
    updateSearchQuery: (String) -> Unit = {},
    playTones: (Char) -> Unit,
    defaultSimId: String? = null,
    showContactPhoto: Boolean = true,
    oneHandedMode: Int = 0, // 0: Disabled, 1: Left, 2: Right
    hapticFeedbackEnabled: Boolean = true,
    onSpeedDial: (Int) -> String? = { null }
) {
    var dialedNumber by remember { mutableStateOf("") }
    val context = LocalContext.current

    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(600, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

    // SIM Selection State
    var showSimDialog by remember { mutableStateOf(false) }
    var availableSims by remember { mutableStateOf<List<PhoneAccountHandle>>(emptyList()) }
    var phoneNumberToDial by remember { mutableStateOf("") }

    val bottomNavVisible = LocalBottomNavVisible.current
    
    LaunchedEffect(showSimDialog) {
        bottomNavVisible.value = !showSimDialog
    }

    LaunchedEffect(dialedNumber) {
        updateSearchQuery(dialedNumber)
    }

    val colorScheme = MaterialTheme.colorScheme

    fun onCallClick(number: String) {
        phoneNumberToDial = number
        val sims = getAvailableSims(context)
        val defaultSim = sims.find { it.id == defaultSimId }
        
        if (defaultSim != null) {
            placeCall(context, number, defaultSim)
        } else {
            initiateCall(context, number) { available ->
                availableSims = available
                showSimDialog = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .statusBarsPadding()
                .padding(bottom = 104.dp)
                .alpha(entryAlpha.value)
                .offset { IntOffset(0, entryOffset.value.roundToInt()) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navigator.navigate(Screen.Settings) }) {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            /* ---------- DIALED TEXT ---------- */
            Text(
                text = dialedNumber.ifBlank { " " },
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                maxLines = 1
            )

            AnimatedVisibility(
                visible = dialedNumber.isNotBlank(),
                enter = fadeIn() + slideInVertically { -20 },
                exit = fadeOut()
            ) {
                Text(
                    text = "Add to Contacts",
                    color = colorScheme.primary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            navigator.navigate(Screen.AddContact(number = dialedNumber))
                        }
                )
            }
            if (dialedNumber.isBlank()) {
                Spacer(Modifier.height(37.dp))
            }

            Spacer(Modifier.height(8.dp))

            val suggestions = remember(dialedNumber, contactsGrouped, callLogsGrouped) {
                if (dialedNumber.isBlank()) emptyList()
                else {
                    val map = linkedMapOf<String, Any>()

                    callLogsGrouped.values.flatten().forEach { group ->
                        if (group.phoneNumber.contains(dialedNumber)) {
                            map[group.phoneNumber] = group
                        }
                    }

                    contactsGrouped.values.flatten().forEach { contact ->
                        val number = contact.phoneNumbers.firstOrNull()?.number ?: return@forEach
                        if (number.contains(dialedNumber)) {
                            map.putIfAbsent(number, contact)
                        }
                    }

                    map.values.toList()
                }
            }

            /* ---------- SUGGESTIONS ---------- */
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (suggestions.isNotEmpty()) {
                    item {
                        Text(
                            text = "SUGGESTIONS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                            letterSpacing = 0.8.sp
                        )
                    }

                    items(suggestions) { item ->
                        when (item) {
                            is GroupedCallLog -> {
                                HybridCallLogPill(
                                    name = item.contact?.displayName ?: item.phoneNumber,
                                    phoneNumber = item.phoneNumber,
                                    callType = item.callType,
                                    callTime = item.callTime,
                                    simSlot = item.simSlot,
                                    contact = if (showContactPhoto) item.contact else null,
                                    callCount = item.logs.size,
                                    onRowClick = { onCallClick(item.phoneNumber) },
                                    onInfoClick = {
                                        navigator.navigate(Screen.CallDetails(item.phoneNumber))
                                    }
                                )
                            }

                            is Contact -> {
                                val contactNumber = item.phoneNumbers.firstOrNull()?.number ?: ""
                                HybridContactRow(
                                    name = item.displayName,
                                    subtitle = contactNumber,
                                    photoUrl = if (showContactPhoto) item.profilePictureUrl else null,
                                    onRowClick = { onCallClick(contactNumber) },
                                    onInfoClick = {
                                        navigator.navigate(Screen.CallDetails(contactNumber))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // DialPad with One-handed Mode Support
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (oneHandedMode != 0) 24.dp else 0.dp),
                contentAlignment = when (oneHandedMode) {
                    1 -> Alignment.BottomStart
                    2 -> Alignment.BottomEnd
                    else -> Alignment.BottomCenter
                }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(if (oneHandedMode != 0) 0.75f else 1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DialPad(
                        playTones = playTones,
                        onDigitPress = {
                            if (dialedNumber.length < 15) {
                                dialedNumber += it
                            }
                        },
                        onDigitLongPress = { digit ->
                            val d = digit.toIntOrNull()
                            if (d != null && d in 1..9) {
                                val speedDialNumber = onSpeedDial(d)
                                if (!speedDialNumber.isNullOrBlank()) {
                                    onCallClick(speedDialNumber)
                                } else if (d == 1) {
                                    // Default Voicemail number
                                    onCallClick("123") 
                                } else {
                                    Toast.makeText(context, "No speed dial set for $d", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        hapticFeedbackEnabled = hapticFeedbackEnabled
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(56.dp))

                        FilledIconButton(
                            onClick = {
                                if (dialedNumber.isNotBlank()) {
                                    onCallClick(dialedNumber)
                                }
                            },
                            modifier = Modifier.size(72.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            shape = CircleShape
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.call),
                                contentDescription = "Call",
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    colorScheme.surfaceVariant.copy(
                                        alpha = if (dialedNumber.isEmpty()) 0.4f else 1f
                                    ),
                                    CircleShape
                                )
                                .combinedClickable(
                                    enabled = dialedNumber.isNotEmpty(),
                                    onClick = { dialedNumber = dialedNumber.dropLast(1) },
                                    onLongClick = { dialedNumber = "" }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.delete),
                                contentDescription = "Delete",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showSimDialog) {
            SimSelectionDialog(
                availableAccounts = availableSims,
                includeAskEveryTime = false,
                onSimSelected = { handle ->
                    showSimDialog = false
                    handle?.let { placeCall(context, phoneNumberToDial, it) }
                },
                onDismiss = { showSimDialog = false }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DialerPreview() {
    val navState = rememberNavigationState(
        startRoute = Screen.Keypad,
        topLevelRoutes = setOf(Screen.Keypad, Screen.Recent, Screen.Contacts, Screen.Search)
    )
    val navigator = remember { Navigator(navState) }
    
    PhoneTheme {
        DialerScreen(
            navigator = navigator,
            contactsGrouped = TreeMap<Char, List<Contact>>().apply {
                put('A', listOf(Contact(displayName = "Alice", phoneNumbers = listOf(PhoneNumber("123456")))))
            },
            callLogsGrouped = emptyMap(),
            updateSearchQuery = {},
            playTones = {}
        )
    }
}
