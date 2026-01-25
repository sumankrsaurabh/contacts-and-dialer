package com.coderon.phone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
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
import com.coderon.phone.ui.components.DialPad
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.utils.ScaffoldScreen
import com.coderon.phone.utils.initiateCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.coderon.phone.data.model.CallLog as CallLogEntry

@Composable
fun DialerScreen(
    navController: NavController,
    filterContact: StateFlow<Map<Char, List<Contact>>>,
    filterCallLog: Flow<List<CallLogEntry>>,
    updateSearchQuery: (String) -> Unit = {},
    playTones: (Char) -> Unit
) {
    var dialedNumber by remember { mutableStateOf("") }

    LaunchedEffect(dialedNumber) {
        updateSearchQuery(dialedNumber)
    }

    val contacts by filterContact.collectAsStateWithLifecycle()
    val callLogs by filterCallLog.collectAsStateWithLifecycle(emptyList())

    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = 100.dp)
    ) {

        Spacer(Modifier.height(24.dp))

        /* ---------- TYPED NUMBER ---------- */
        Text(
            text = dialedNumber.ifBlank { " " },
            fontSize = 40.sp,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onBackground,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(12.dp))

        /* ---------- SUGGESTIONS ---------- */
        val suggestions = remember(dialedNumber, contacts, callLogs) {
            if (dialedNumber.isBlank()) emptyList()
            else {
                val map = linkedMapOf<String, Any>()

                callLogs
                    .groupBy { it.phoneNumber }
                    .mapNotNull { it.value.maxByOrNull { log -> log.callTime } }
                    .filter { it.phoneNumber.contains(dialedNumber) }
                    .forEach { map[it.phoneNumber] = it }

                contacts.values.flatten().forEach { contact ->
                    val number = contact.phoneNumbers.firstOrNull()?.number ?: return@forEach
                    if (number.contains(dialedNumber)) {
                        map.putIfAbsent(number, contact)
                    }
                }

                map.values.toList()
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            items(suggestions) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            dialedNumber = when (item) {
                                is CallLogEntry -> item.phoneNumber
                                is Contact -> item.phoneNumbers.first().number
                                else -> dialedNumber
                            }
                        }
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = when (item) {
                            is Contact -> item.displayName
                            is CallLogEntry -> item.contact?.displayName ?: item.phoneNumber
                            else -> ""
                        },
                        fontSize = 16.sp,
                        color = colorScheme.onSurface
                    )

                    Text(
                        text = when (item) {
                            is Contact -> item.phoneNumbers.first().number
                            is CallLogEntry -> item.phoneNumber
                            else -> ""
                        },
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        /* ---------- KEYPAD ---------- */
        DialPad(
            playTones = playTones,
            onDigitPress = {
                if (dialedNumber.length < 15) {
                    dialedNumber += it
                }
            }
        )

        Spacer(Modifier.height(20.dp))

        /* ---------- CALL + DELETE ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(56.dp))

            FilledIconButton(
                onClick = {
                    if (dialedNumber.isNotBlank()) {
                        initiateCall(navController.context, dialedNumber)
                    }
                },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF34C759),
                    contentColor = Color.White
                ),
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(R.drawable.call),
                    contentDescription = "Call",
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.width(24.dp))

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
                        onClick = {
                            dialedNumber = dialedNumber.dropLast(1)
                        },
                        onLongClick = {
                            dialedNumber = ""
                        }
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

@Preview(showBackground = true)
@PreviewLightDark
@Composable
fun DialerPreview() {
    ScaffoldScreen(rememberNavController()) {
        DialerScreen(
            navController = rememberNavController(),
            filterContact = MutableStateFlow(emptyMap()),
            filterCallLog = MutableStateFlow(emptyList()),
            updateSearchQuery = {},
            playTones = {}
        )
    }
}
