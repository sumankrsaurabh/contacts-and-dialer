package com.coderon.phone.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MissedVideoCall
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.coderon.phone.R
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.utils.ActionsMenuTop
import com.coderon.phone.utils.initiateCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@Composable
fun DialerScreen(
    navController: NavController,
    filterContact: StateFlow<Map<Char, List<Contact>>>,
    filterCallLog: Flow<List<CallLog>>,
    updateSearchQuery: (String) -> Unit = {},
    playTones: (Char) -> Unit
) {
    var dialedNumber by remember { mutableStateOf("") }
    updateSearchQuery(dialedNumber)
    val maxDialedNumberLength = 15
    val contacts by filterContact.collectAsStateWithLifecycle()
    val callLogs by filterCallLog.collectAsStateWithLifecycle(emptyList())
    val uniqueEntries = remember(callLogs, contacts) {
        val latestCallLogsByNumber = callLogs.groupBy { it.phoneNumber }
            .mapValues { (_, logs) -> logs.maxByOrNull { it.callTime } }.filterValues { it != null }
        val allEntries = linkedMapOf<String, Any>()
        latestCallLogsByNumber.forEach { (number, log) ->
            if (log != null) allEntries[number] = log
        }
        contacts.values.flatten().forEach { contact ->
            allEntries.putIfAbsent(contact.phoneNumber, contact)
        }
        allEntries
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ActionsMenuTop(navController = navController)

        if (dialedNumber.isNotEmpty()) {
            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Display unique call logs and contacts
                items(uniqueEntries.values.toList()) { contactOrCallLog ->
                    when (contactOrCallLog) {
                        is CallLog -> CallLogItem(
                            contactOrCallLog, onCallLogEntryClick = { dialedNumber = it })

                        is Contact -> ContactItem(
                            contactOrCallLog,
                            navController = navController/*, onCallLogEntryClick = { dialedNumber = it }*/
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f))
        }

        Text(
            text = dialedNumber,
            fontSize = 32.sp,
            modifier = Modifier.padding(18.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        DialPad(playTones) { dialPadDigit ->
            if (dialedNumber.length < maxDialedNumberLength) {
                dialedNumber += dialPadDigit
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                modifier = Modifier.size(64.dp),
                onClick = { /* Video Call Logic */ },
                enabled = false,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MissedVideoCall,
                    contentDescription = "Video Call",
                    modifier = Modifier.size(32.dp)
                )
            }

            FilledIconButton(
                onClick = {
                    initiateCall(
                        context = navController.context, phoneNumber = dialedNumber
                    )
                },
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.call),
                    contentDescription = "Call",
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                modifier = Modifier.size(64.dp),
                onClick = {
                    if (dialedNumber.isNotEmpty()) {
                        dialedNumber = dialedNumber.dropLast(1)
                    }
                },
                enabled = dialedNumber.isNotEmpty(),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Backspace,
                    contentDescription = "Delete last digit",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DialPad(playTones: (Char) -> Unit, onDigitPress: (String) -> Unit) {
    val digitLetters = mapOf(
        '1' to "",
        '2' to "ABC",
        '3' to "DEF",
        '4' to "GHI",
        '5' to "JKL",
        '6' to "MNO",
        '7' to "PQRS",
        '8' to "TUV",
        '9' to "WXYZ",
        '*' to "",
        '0' to "+",
        '#' to ""
    )

    Column {
        listOf("123", "456", "789", "*0#").forEach { dialPadDigitRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dialPadDigitRow.forEach { digitInDialPadRow ->
                    val scale = remember { Animatable(1f) }
                    val coroutineScope = rememberCoroutineScope()
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(8.dp)
                            .clickable {
                                onDigitPress(digitInDialPadRow.toString())
                                playTones(digitInDialPadRow)
                                coroutineScope.launch {
                                    scale.animateTo(0.8f, animationSpec = spring())
                                    scale.animateTo(1f, animationSpec = spring())
                                }
                            }
                            .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = digitInDialPadRow.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = digitLetters[digitInDialPadRow] ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}