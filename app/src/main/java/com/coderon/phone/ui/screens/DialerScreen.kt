package com.coderon.phone.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.R
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.utils.ScaffoldScreen
import com.coderon.phone.utils.initiateCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.coderon.phone.data.model.CallLog as CallLogEntry

/* ------------------------------------------------ */
/* -------------------- SCREEN -------------------- */
/* ------------------------------------------------ */

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

    val isDark = isSystemInDarkTheme()
    val background = if (isDark) Color.Black else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val secondaryText = textColor.copy(alpha = 0.6f)

    /* -------- Suggestions (Contacts + Recents) -------- */

    val suggestions = remember(dialedNumber, contacts, callLogs) {
        if (dialedNumber.isBlank()) return@remember emptyList()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = 100.dp)
    ) {


        /* ---------- TYPED NUMBER ---------- */

        Spacer(Modifier.height(24.dp))
        Text(
            text = dialedNumber.ifBlank { " " },
            fontSize = 40.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(12.dp))

        /* ---------- SUGGESTIONS (REMAINING SPACE ONLY) ---------- */

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = suggestions.isNotEmpty()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
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
                                    is CallLogEntry ->
                                        item.contact?.displayName ?: item.phoneNumber

                                    else -> ""
                                },
                                fontSize = 16.sp,
                                color = textColor
                            )
                            Text(
                                text = when (item) {
                                    is Contact -> item.phoneNumbers.first().number
                                    is CallLogEntry -> item.phoneNumber
                                    else -> ""
                                },
                                fontSize = 14.sp,
                                color = secondaryText
                            )
                        }
                    }
                }
            }
        }

        /* ---------- KEYPAD (BOTTOM FIXED) ---------- */

        DialPad(
            playTones = playTones,
            onDigitPress = {
                if (dialedNumber.length < 15) dialedNumber += it
            }
        )

        Spacer(Modifier.height(20.dp))

        /* ---------- CALL + DELETE ---------- */

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(56.dp),
                contentAlignment = Alignment.Center
            ) {}
            Spacer(Modifier.width(24.dp))
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
                        if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                        CircleShape
                    )
                    .clickable { dialedNumber = dialedNumber.dropLast(1) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.Backspace,
                    contentDescription = "Delete",
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
            }

        }
    }
}

/* ------------------------------------------------ */
/* -------------------- DIAL PAD ------------------ */
/* ------------------------------------------------ */

@Composable
private fun DialPad(
    playTones: (Char) -> Unit,
    onDigitPress: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val keyColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val textColor = if (isDark) Color.White else Color.Black
    val letterColor = textColor.copy(alpha = 0.7f)

    val digitLetters = mapOf(
        "1" to "",
        "2" to "ABC", "3" to "DEF",
        "4" to "GHI", "5" to "JKL", "6" to "MNO",
        "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
        "*" to "",
        "0" to "+",
        "#" to ""
    )

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        ).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { digit ->
                    val scale = remember { Animatable(1f) }
                    val scope = rememberCoroutineScope()

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                            }
                            .background(keyColor, CircleShape)
                            .clickable {
                                onDigitPress(digit)
                                playTones(digit.first())
                                scope.launch {
                                    scale.animateTo(0.9f, spring())
                                    scale.animateTo(1f, spring())
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = digit,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            )
                            if (digitLetters[digit]?.isNotEmpty() == true) {
                                Text(
                                    text = digitLetters[digit]!!,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    color = letterColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* -------------------- PREVIEW ------------------- */
/* ------------------------------------------------ */

@Preview(showBackground = true)
@Composable
fun DialerPreviewLight() {
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
