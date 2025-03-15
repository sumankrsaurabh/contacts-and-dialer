package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.MissedVideoCall
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.R
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.defaultCallLog
import com.coderon.phone.ui.utils.ActionsMenuTop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch


@Composable
fun DialerScreen(
    navController: NavController,
    filterContact: (String) -> Flow<Map<Char, List<Contact>>>,
    filterCallLog: (String) -> Flow<List<CallLog>>,
    toneGenerator: ToneGenerator = remember { ToneGenerator(AudioManager.STREAM_DTMF, 80) }
) {
    var dialedNumber by remember { mutableStateOf("") }
    val maxDialedNumberLength = 15
    val filteredContacts by filterContact(dialedNumber).collectAsStateWithLifecycle(emptyMap())
    val filteredCallLogs by filterCallLog(dialedNumber).collectAsStateWithLifecycle(emptyList())
    val contacts = filteredContacts.values.flatten()
    val uniqueEntries = linkedMapOf<String, Any>()

    // Add the latest call log for each unique phone number
    filteredCallLogs
        .groupBy { it.phoneNumber } // Group by phone number
        .mapValues { it.value.maxByOrNull { log -> log.callTime } } // Keep the most recent log
        .values
        .filterNotNull()
        .forEach { log -> uniqueEntries[log.phoneNumber] = log }

    // Add contacts only if they are not already in call logs
    contacts.forEach { contact ->
        uniqueEntries.putIfAbsent(contact.phoneNumber, contact)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ActionsMenuTop(navController = navController)
        if (dialedNumber.isNotEmpty()) {
            LazyColumn(Modifier.weight(1f)) {

                // Display unique call logs and contacts
                items(uniqueEntries.values.toList()) { entry ->
                    when (entry) {
                        is CallLog -> FilteredCallLogItem(entry, onClick = { dialedNumber = it })
                        is Contact -> FilteredContactsBasedOnDialedDigitsItem(
                            entry,
                            onClick = { dialedNumber = it })
                    }
                }
            }
        }

        Text(
            text = dialedNumber, fontSize = 32.sp, modifier = Modifier.padding(18.dp)
        )

        DialPad(toneGenerator) { digit ->
            if (dialedNumber.length < maxDialedNumberLength) {
                dialedNumber += digit
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                modifier = Modifier.size(64.dp), onClick = { /* Video Call Logic */ },
                enabled = false
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MissedVideoCall,
                    contentDescription = "Video Call",
                    modifier = Modifier.size(32.dp)
                )
            }

            FilledIconButton(
                onClick = { /* Call Logic */ },
                enabled = dialedNumber.isNotEmpty(),
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (dialedNumber.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.call),
                    contentDescription = "Call",
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(modifier = Modifier.size(64.dp), onClick = {
                if (dialedNumber.isNotEmpty()) {
                    dialedNumber = dialedNumber.dropLast(1)
                }
            }, enabled = dialedNumber.isNotEmpty()) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Backspace,
                    contentDescription = "Delete last digit",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun FilteredCallLogItem(
    log: CallLog = defaultCallLog(),
    onClick: (String) -> Unit = { }
) {
    Card(
        modifier = Modifier.padding(horizontal = 0.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        onClick = { onClick(log.phoneNumber) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    when (log.callType) {
                        CallType.INCOMING -> R.drawable.incoming_call
                        CallType.OUTGOING -> R.drawable.outgoing_call
                        CallType.MISSED -> R.drawable.missed_call
                        else -> R.drawable.call
                    }
                ),
                contentDescription = "Call Type",
                tint = if (isSystemInDarkTheme()) Color.Gray else Color.DarkGray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = log.contact?.name?.ifBlank { log.phoneNumber } ?: log.phoneNumber,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = log.phoneNumber,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Normal
                )
            }
            Text(
                text = log.callTime.formatTime().toString(),
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Normal
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun FilteredContactsBasedOnDialedDigitsItem(
    contact: Contact = Contact(name = "Little princes", phoneNumber = "1597534862"),
    onClick: (String) -> Unit = { }

) {
    Card(
        modifier = Modifier.padding(horizontal = 0.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        onClick = { onClick(contact.phoneNumber) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactProfileImage(contact)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = contact.name.ifBlank { contact.phoneNumber },
                    fontSize = 16.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = contact.phoneNumber,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}


@Composable
private fun DialPad(toneGenerator: ToneGenerator, onDigitPress: (String) -> Unit) {
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
        listOf("123", "456", "789", "*0#").forEach { digitRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                digitRow.forEach { dialedDigit ->
                    val scale = remember { Animatable(1f) }
                    val coroutineScope = rememberCoroutineScope()
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .size(64.dp)
                            .clip(CircleShape)
                            .clickable {
                                onDigitPress(dialedDigit.toString())
                                val toneType = getDTMFTone(dialedDigit)
                                if (toneType != null) {
                                    toneGenerator.startTone(toneType, 150)
                                }
                                coroutineScope.launch {
                                    scale.animateTo(0.8f, animationSpec = spring())
                                    scale.animateTo(1f, animationSpec = spring())
                                }
                            }
                            .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dialedDigit.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = digitLetters[dialedDigit] ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

fun getDTMFTone(digit: Char): Int? {
    return when (digit) {
        '1' -> ToneGenerator.TONE_DTMF_1
        '2' -> ToneGenerator.TONE_DTMF_2
        '3' -> ToneGenerator.TONE_DTMF_3
        '4' -> ToneGenerator.TONE_DTMF_4
        '5' -> ToneGenerator.TONE_DTMF_5
        '6' -> ToneGenerator.TONE_DTMF_6
        '7' -> ToneGenerator.TONE_DTMF_7
        '8' -> ToneGenerator.TONE_DTMF_8
        '9' -> ToneGenerator.TONE_DTMF_9
        '0' -> ToneGenerator.TONE_DTMF_0
        '*' -> ToneGenerator.TONE_DTMF_S
        '#' -> ToneGenerator.TONE_DTMF_P
        else -> null
    }
}

@SuppressLint("MissingPermission")
@Preview(showBackground = true)
@Composable
private fun DialerScreenPreview() {
    DialerScreen(
        navController = rememberNavController(),
        filterContact = { emptyFlow() },
        filterCallLog = { emptyFlow() }
    )
}
