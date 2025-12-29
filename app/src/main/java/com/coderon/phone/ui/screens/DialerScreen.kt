package com.coderon.phone.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.R
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.utils.ActionsMenuTop
import com.coderon.phone.ui.utils.GlassyIconButton
import com.coderon.phone.utils.initiateCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.coderon.phone.data.model.CallLog as CallLogEntry

/* -------------------- SCREEN -------------------- */

@Composable
fun DialerScreen(
    navController: NavController,
    filterContact: StateFlow<Map<Char, List<Contact>>>,
    filterCallLog: Flow<List<CallLogEntry>>,
    updateSearchQuery: (String) -> Unit = {},
    playTones: (Char) -> Unit
) {
    var dialedNumber by remember { mutableStateOf("") }
    updateSearchQuery(dialedNumber)

    val contacts by filterContact.collectAsStateWithLifecycle()
    val callLogs by filterCallLog.collectAsStateWithLifecycle(emptyList())

    /**
     * Build a unique mixed list:
     * - Latest call per phone number
     * - Contacts not present in call log
     */
    val uniqueEntries = remember(callLogs, contacts) {
        val map = linkedMapOf<String, Any>()

        callLogs
            .groupBy { it.phoneNumber }
            .mapNotNull { it.value.maxByOrNull { log -> log.callTime } }
            .forEach { map[it.phoneNumber] = it }

        contacts.values.flatten().forEach { contact ->
            val number = contact.phoneNumbers.firstOrNull()?.number ?: return@forEach
            map.putIfAbsent(number, contact)
        }

        map.values.toList()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(16.dp))
        ActionsMenuTop(navController = navController)

        if (dialedNumber.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uniqueEntries) { item ->
                    when (item) {
                        is CallLogEntry -> CallLogItem(
                            log = item,
                            onCallLogEntryClick = { dialedNumber = it }
                        )

                        is Contact -> ContactItemGlass(
                            contact = item,
                            navController = navController
                        )
                    }
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        /* -------- Dialed Number -------- */

        Row(modifier = Modifier.padding(horizontal = 36.dp)) {
            Text(
                text = dialedNumber,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier
                    .padding(18.dp)
                    .weight(1f)
            )

            AnimatedVisibility(dialedNumber.isNotEmpty()) {
                GlassyIconButton(
                    onClick = { dialedNumber = dialedNumber.dropLast(1) }
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Backspace,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        DialPad(playTones) {
            if (dialedNumber.length < 15) dialedNumber += it
        }

        Spacer(Modifier.height(8.dp))

        DialerActions(
            dialedNumber = dialedNumber,
            onCall = {
                initiateCall(navController.context, dialedNumber)
            }
        )

        Spacer(Modifier.height(16.dp))
    }
}

/* -------------------- DIAL PAD -------------------- */

@Composable
private fun DialPad(
    playTones: (Char) -> Unit,
    onDigitPress: (String) -> Unit
) {
    val digitLetters = mapOf(
        '2' to "ABC", '3' to "DEF",
        '4' to "GHI", '5' to "JKL",
        '6' to "MNO", '7' to "PQRS",
        '8' to "TUV", '9' to "WXYZ",
        '0' to "+"
    )

    Column {
        listOf("123", "456", "789", "*0#").forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { digit ->
                    val scale = remember { Animatable(1f) }
                    val scope = rememberCoroutineScope()

                    Card(
                        modifier = Modifier
                            .size(64.dp)
                            .graphicsLayer(
                                scaleX = scale.value,
                                scaleY = scale.value
                            )
                            .clickable {
                                onDigitPress(digit.toString())
                                playTones(digit)
                                scope.launch {
                                    scale.animateTo(0.85f, spring())
                                    scale.animateTo(1f, spring())
                                }
                            },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.12f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.25f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = digit.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Text(
                                text = digitLetters[digit] ?: "",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/* -------------------- ACTION BUTTONS -------------------- */

@Composable
private fun DialerActions(
    dialedNumber: String,
    onCall: () -> Unit
) {
    FilledIconButton(
        onClick = onCall,
        modifier = Modifier
            .size(64.dp)
            .border(1.dp, Color.Green, CircleShape),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.Green.copy(.5f),
            contentColor = Color.White
        )
    ) {
        Icon(
            painter = painterResource(R.drawable.call),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
    }
}

/* -------------------- PREVIEW -------------------- */

@Preview(showBackground = true)
@PreviewLightDark
@Composable
fun DialerScreenPreview() {
    DialerScreen(
        navController = rememberNavController(),
        filterContact = MutableStateFlow(emptyMap()),
        filterCallLog = MutableStateFlow(emptyList()),
        updateSearchQuery = {},
        playTones = {}
    )
}
