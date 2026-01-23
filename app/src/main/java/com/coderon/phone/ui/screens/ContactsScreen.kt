package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

/* ------------------------------------------------ */
/* ---------------- CONTACTS SCREEN ---------------- */
/* ------------------------------------------------ */

@Composable
fun ContactsScreen(
    contacts: Map<Char, List<Contact>>,
    navController: NavController
) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) Color(0xFF0E0E0E) else Color(0xFFF4F4F4)
    val pill = if (isDark) Color(0xFF1F1F1F) else Color.White
    val primary = if (isDark) Color.White else Color.Black
    val secondary = primary.copy(alpha = 0.6f)

    val allContacts = remember(contacts) { contacts.values.flatten() }

    val grouped = remember(allContacts) {
        allContacts
            .sortedBy { it.displayName.lowercase() }
            .groupBy { it.displayName.firstOrNull()?.uppercaseChar() ?: '#' }
            .toSortedMap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {

        /* ---------- HEADER ---------- */
        Text(
            text = "Contacts",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = primary,
            modifier = Modifier.padding(20.dp)
        )

        /* ---------- LIST ---------- */
        LazyColumn(
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            grouped.forEach { (letter, list) ->

                item {
                    Text(
                        text = letter.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = secondary,
                        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                    )
                }

                items(list, key = { it.id }) { contact ->
                    SamsungContactPill(
                        contact = contact,
                        pillColor = pill,
                        primary = primary,
                        secondary = secondary
                    ) {
                        navController.navigate(
                            Screen.CallDetails.createRoute(
                                contact.phoneNumbers.firstOrNull()?.number.orEmpty()
                            )
                        )
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- SAMSUNG PILL ROW --------------- */
/* ------------------------------------------------ */

@SuppressLint("MissingPermission")
@Composable
private fun SamsungContactPill(
    contact: Contact,
    pillColor: Color,
    primary: Color,
    secondary: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(50))
            .background(pillColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
                    .size(42.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.displayName.first().uppercaseChar().toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = contact.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = primary
            )

            contact.phoneNumbers.firstOrNull()?.let {
                Text(
                    text = it.number,
                    fontSize = 13.sp,
                    color = secondary
                )
            }
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- PREVIEW ----------------------- */
/* ------------------------------------------------ */

@Preview(showBackground = true)
@Composable
private fun PreviewSamsungContacts() {
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
