package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.Screen
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.theme.PhoneTheme
import kotlinx.coroutines.launch

/* ------------------------------------------------ */
/* ---------------- CONTACTS SCREEN ---------------- */
/* ------------------------------------------------ */

@Composable
fun ContactsScreen(
    contacts: Map<Char, List<Contact>>,
    navController: NavController
) {
    val isDark = isSystemInDarkTheme()
    val background = if (isDark) Color.Black else Color.White
    val primaryText = if (isDark) Color.White else Color.Black
    val secondaryText = primaryText.copy(alpha = 0.6f)

    val allContacts = remember(contacts) { contacts.values.flatten() }
    val duplicates = remember(allContacts) { findDuplicateContacts(allContacts) }

    val grouped = remember(allContacts) {
        allContacts
            .sortedBy { it.displayName.lowercase() }
            .groupBy { it.displayName.firstOrNull()?.uppercaseChar() ?: '#' }
            .toSortedMap()
    }

    val listState = rememberLazyListState()
    val letterPositions = remember { mutableStateMapOf<Char, Int>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {

            var currentIndex = 0

            /* -------- TITLE -------- */
            item {
                Text(
                    text = "Contacts",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    modifier = Modifier.padding(16.dp)
                )
                currentIndex++
            }

            /* -------- DUPLICATES -------- */
            if (duplicates.isNotEmpty()) {
                item {
                    SmartDuplicatesRow(
                        count = duplicates.distinctBy { it.displayName }.size
                    )
                }
                currentIndex++
            }

            /* -------- CONTACT SECTIONS -------- */
            grouped.forEach { (letter, list) ->

                item {
                    letterPositions[letter] = currentIndex
                    LetterHeader(letter, secondaryText)
                }
                currentIndex++

                items(list, key = { it.id }) { contact ->
                    ContactRow(
                        contact = contact,
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    ) {
                        navController.navigate(
                            Screen.CallDetails.createRoute(
                                contact.phoneNumbers.firstOrNull()?.number.orEmpty()
                            )
                        )
                    }
                    currentIndex++
                }
            }
        }

        AlphabetIndexBar(
            letters = grouped.keys.toList(),
            listState = listState,
            letterPositions = letterPositions,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

/* ------------------------------------------------ */
/* ---------------- CONTACT ROW ------------------- */
/* ------------------------------------------------ */

@SuppressLint("MissingPermission")
@Composable
private fun ContactRow(
    contact: Contact,
    primaryText: Color,
    secondaryText: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (!contact.profilePictureUrl.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(contact.profilePictureUrl)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(primaryText.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.displayName.first().uppercaseChar().toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryText
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = contact.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = primaryText
            )

            contact.phoneNumbers.firstOrNull()?.let {
                Text(
                    text = it.number,
                    fontSize = 13.sp,
                    color = secondaryText
                )
            }
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- DUPLICATES ROW ---------------- */
/* ------------------------------------------------ */

@Composable
private fun SmartDuplicatesRow(count: Int) {
    Text(
        text = "$count Duplicate${if (count > 1) "s" else ""} Found",
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF0A84FF),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/* ------------------------------------------------ */
/* ---------------- LETTER HEADER ----------------- */
/* ------------------------------------------------ */

@Composable
private fun LetterHeader(letter: Char, color: Color) {
    Text(
        text = letter.toString(),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

/* ------------------------------------------------ */
/* ---------------- ALPHABET INDEX ---------------- */
/* ------------------------------------------------ */

@Composable
private fun AlphabetIndexBar(
    letters: List<Char>,
    listState: LazyListState,
    letterPositions: Map<Char, Int>,
    modifier: Modifier
) {
    var letterHeight by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .padding(end = 4.dp)
            .pointerInput(letters) {
                detectVerticalDragGestures { change, _ ->
                    val index =
                        (change.position.y / letterHeight)
                            .toInt()
                            .coerceIn(0, letters.lastIndex)

                    val letter = letters[index]
                    letterPositions[letter]?.let {
                        scope.launch { listState.scrollToItem(it) }
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        letters.forEach {
            Text(
                text = it.toString(),
                fontSize = 11.sp,
                color = Color(0xFF0A84FF),
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .onGloballyPositioned {
                        letterHeight = it.size.height.toFloat()
                    }
            )
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- DUPLICATE LOGIC ---------------- */
/* ------------------------------------------------ */

private fun findDuplicateContacts(contacts: List<Contact>): List<Contact> =
    contacts
        .groupBy { it.displayName.lowercase() }
        .filter { it.value.size > 1 }
        .flatMap { it.value }

/* ------------------------------------------------ */
/* ---------------- PREVIEW ----------------------- */
/* ------------------------------------------------ */

@Preview(showBackground = true)
@Composable
private fun PreviewContactsIOS() {
    val contacts = listOf(
        Contact("1", "Simple Alpaca", phoneNumbers = listOf(PhoneNumber("123"))),
        Contact("2", "Simple Alpaca", phoneNumbers = listOf(PhoneNumber("456"))),
        Contact("3", "John Doe", phoneNumbers = listOf(PhoneNumber("789")))
    )

    PhoneTheme {
        ContactsScreen(
            contacts = contacts.groupBy {
                it.displayName.first().uppercaseChar()
            },
            navController = rememberNavController()
        )
    }
}
