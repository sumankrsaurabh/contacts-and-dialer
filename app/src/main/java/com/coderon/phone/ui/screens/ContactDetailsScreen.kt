@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.coderon.phone.R
import com.coderon.phone.data.helpers.formatDate
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.theme.PhoneTheme

@Composable
fun ContactDetailsScreen(
    contact: Contact,
    callLogs: List<CallLog>,
    navController: NavController,
    onToggleFavorite: (Contact) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Blur Effect (iOS style)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                        .background(colorScheme.background.copy(alpha = 0.7f))
                )

                LargeTopAppBar(
                    title = {
                        Text(
                            text = contact.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Edit */ }) {
                            Text(
                                "Edit",
                                color = colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        navigationIconContentColor = Color.Unspecified,
                        titleContentColor = colorScheme.onBackground,
                        actionIconContentColor = Color.Unspecified
                    )
                )
            }
        },
        containerColor = colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = 48.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            /* ---------------- HEADER / AVATAR (iOS + OneUI 8) ---------------- */
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HybridAvatar(contact)
                    Spacer(Modifier.height(28.dp))
                    HybridQuickActions()
                }
            }

            /* ---------------- INFO SECTION (iOS Grouped look with OneUI 8 Rounding) ---------------- */
            item {
                HybridSection(title = "CONTACT INFO") {
                    contact.phoneNumbers.forEachIndexed { index, phone ->
                        HybridInfoRow(
                            label = phone.type.name.lowercase().capitalize(),
                            value = phone.number,
                            icon = R.drawable.call,
                            showDivider = index < contact.phoneNumbers.size - 1 || contact.emailAddresses.isNotEmpty()
                        )
                    }
                    contact.emailAddresses.forEachIndexed { index, email ->
                        HybridInfoRow(
                            label = "Email",
                            value = email,
                            icon = R.drawable.ic_email,
                            showDivider = index < contact.emailAddresses.size - 1
                        )
                    }
                }
            }

            /* ---------------- RECENT CALLS ---------------- */
            if (callLogs.isNotEmpty()) {
                item {
                    HybridSection(title = "RECENT CALLS") {
                        callLogs.take(5).forEachIndexed { index, log ->
                            HybridCallLogRow(
                                log = log,
                                showDivider = index < 4 && index < callLogs.size - 1
                            )
                        }
                    }
                }
            }

            /* ---------------- FAVORITE TOGGLE ---------------- */
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = colorScheme.surfaceContainerLow,
                    onClick = { onToggleFavorite(contact) }
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            if (contact.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            tint = if (contact.isFavorite) Color.Red else colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (contact.isFavorite) "Remove from Favorites" else "Add to Favorites",
                            fontWeight = FontWeight.Medium,
                            color = if (contact.isFavorite) Color.Red else colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HybridAvatar(contact: Contact) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.size(120.dp),
        shape = CircleShape,
        color = colorScheme.surfaceContainerHigh,
        shadowElevation = 2.dp
    ) {
        if (contact.profilePictureUrl != null) {
            AsyncImage(
                model = contact.profilePictureUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = contact.displayName.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HybridQuickActions() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionPill(R.drawable.call, "call", Color(0xFF34C759))
        ActionPill(R.drawable.ic_message, "message", Color(0xFF007AFF))
        ActionPill(R.drawable.ic_video_call, "video", Color(0xFF5856D6))
    }
}

@Composable
private fun ActionPill(icon: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = { /* Action */ },
            shape = CircleShape,
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.size(72.dp, 44.dp) // Pill-shaped action (OneUI 8 style)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painterResource(icon),
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HybridSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp), // OneUI 8 super rounding
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun HybridInfoRow(
    label: String,
    value: String,
    icon: Int,
    showDivider: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Action */ }
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    fontSize = 12.sp,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(value, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            }
            Icon(
                painterResource(icon),
                contentDescription = null,
                tint = colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                thickness = 0.5.dp,
                color = colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun HybridCallLogRow(log: CallLog, showDivider: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    val icon = when (log.callType) {
        CallType.INCOMING -> R.drawable.ic_call_incoming
        CallType.OUTGOING -> R.drawable.ic_call_outgoing
        CallType.MISSED -> R.drawable.ic_call_missed
        else -> R.drawable.call
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (log.callType == CallType.MISSED) Color.Red else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.callTime.formatDate(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (log.callType == CallType.MISSED) Color.Red else colorScheme.onSurface
                )
                Text(
                    text = log.callTime.formatTime(),
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "SIM ${log.simSlot}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colorScheme.outlineVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                thickness = 0.5.dp,
                color = colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RedesignPreview() {
    PhoneTheme {
        ContactDetailsScreen(
            contact = Contact(
                id = "1",
                displayName = "John Appleseed",
                phoneNumbers = listOf(
                    com.coderon.phone.data.model.PhoneNumber("9876543210", isPrimary = true),
                    com.coderon.phone.data.model.PhoneNumber("9876543211")
                ),
                emailAddresses = listOf(
                    "john.c.calhoun@examplepetstore.com"
                )
            ),
            callLogs = listOf(
                CallLog(phoneNumber = "9876543210", callType = CallType.INCOMING),
                CallLog(phoneNumber = "9876543210", callType = CallType.MISSED)
            ),
            rememberNavController()
        )
    }
}

private fun String.capitalize() = this.replaceFirstChar { it.uppercase() }
