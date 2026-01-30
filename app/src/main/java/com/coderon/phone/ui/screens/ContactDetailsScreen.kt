@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

import android.content.Intent
import android.net.Uri
import android.telecom.PhoneAccountHandle
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.coderon.phone.R
import com.coderon.phone.call.services.CallRecorderManager
import com.coderon.phone.data.helpers.formatDate
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.navigation.Navigator
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.navigation.rememberNavigationState
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.SimSelectionDialog
import com.coderon.phone.utils.getAvailableSims
import com.coderon.phone.utils.initiateCall
import com.coderon.phone.utils.openMessagingApp
import com.coderon.phone.utils.placeCall
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

@Composable
fun ContactDetailsScreen(
    contact: Contact,
    callLogs: List<CallLog>,
    navigator: Navigator,
    onToggleFavorite: (Contact) -> Unit = {},
    onEditContact: (Contact) -> Unit = {},
    defaultSimId: String? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val primaryNumber = contact.phoneNumbers.firstOrNull()?.number ?: ""

    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(600, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

    // Call Recorder Backend
    val recorderManager = remember { CallRecorderManager(context) }
    val recordings = remember(primaryNumber) { 
        if (primaryNumber.isNotBlank()) recorderManager.getRecordingsForNumber(primaryNumber) 
        else emptyList() 
    }

    // SIM Selection State
    var showSimDialog by remember { mutableStateOf(false) }
    var availableSims by remember { mutableStateOf<List<PhoneAccountHandle>>(emptyList()) }
    var numberToCall by remember { mutableStateOf("") }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    fun onCallClick(number: String) {
        numberToCall = number
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

    fun playRecording(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "audio/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot play recording", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                        IconButton(onClick = { navigator.goBack() }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEditContact(contact) }) {
                            Text(
                                "Edit",
                                color = colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
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
        Box(modifier = Modifier
            .fillMaxSize()
            .alpha(entryAlpha.value)
            .offset { IntOffset(0, entryOffset.value.roundToInt()) }
        ) {
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
                /* ---------------- HEADER / AVATAR ---------------- */
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HybridAvatar(contact)
                        Spacer(Modifier.height(28.dp))
                        HybridQuickActions(
                            onCall = {
                                if (primaryNumber.isNotBlank()) {
                                    onCallClick(primaryNumber)
                                }
                            },
                            onMessage = {
                                if (primaryNumber.isNotBlank()) {
                                    openMessagingApp(context, primaryNumber)
                                }
                            },
                            onVideo = {
                                // For now, video call uses same initiateCall flow
                                if (primaryNumber.isNotBlank()) {
                                    onCallClick(primaryNumber)
                                }
                            }
                        )
                    }
                }

                /* ---------------- INFO SECTION ---------------- */
                item {
                    HybridSection(title = "CONTACT INFO") {
                        contact.phoneNumbers.forEachIndexed { index, phone ->
                            HybridInfoRow(
                                label = phone.type.name.lowercase().replaceFirstChar { it.uppercase() },
                                value = phone.number,
                                icon = R.drawable.call,
                                showDivider = index < contact.phoneNumbers.size - 1 || contact.emailAddresses.isNotEmpty(),
                                onClick = { onCallClick(phone.number) }
                            )
                        }
                        contact.emailAddresses.forEachIndexed { index, email ->
                            HybridInfoRow(
                                label = "Email",
                                value = email,
                                icon = R.drawable.ic_email,
                                showDivider = index < contact.emailAddresses.size - 1,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$email")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Handle no email app
                                        e.printStackTrace()
                                    }
                                }
                            )
                        }
                    }
                }

                /* ---------------- RECORDINGS SECTION ---------------- */
                if (recordings.isNotEmpty()) {
                    item {
                        HybridSection(title = "CALL RECORDINGS") {
                            recordings.reversed().forEachIndexed { index, file ->
                                HybridRecordingRow(
                                    file = file,
                                    showDivider = index < recordings.size - 1,
                                    onClick = { playRecording(file) }
                                )
                            }
                        }
                    }
                }

                /* ---------------- RECENT CALLS ---------------- */
                if (callLogs.isNotEmpty()) {
                    item {
                        HybridSection(title = "RECENT CALLS") {
                            callLogs.take(10).forEachIndexed { index, log ->
                                HybridCallLogRow(
                                    log = log,
                                    showDivider = index < 9 && index < callLogs.size - 1,
                                    onClick = { onCallClick(log.phoneNumber) }
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

            if (showSimDialog) {
                SimSelectionDialog(
                    availableAccounts = availableSims,
                    onSimSelected = { handle ->
                        showSimDialog = false
                        handle?.let { placeCall(context, numberToCall, handle)}
                    },
                    onDismiss = { showSimDialog = false }
                )
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
private fun HybridQuickActions(
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onVideo: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionPill(R.drawable.call, "call", Color(0xFF34C759), onCall)
        ActionPill(R.drawable.ic_message, "message", Color(0xFF007AFF), onMessage)
        ActionPill(R.drawable.ic_video_call, "video", Color(0xFF5856D6), onVideo)
    }
}

@Composable
private fun ActionPill(icon: Int, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.size(72.dp, 44.dp)
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
            shape = RoundedCornerShape(32.dp),
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
    showDivider: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
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
private fun HybridRecordingRow(file: File, showDivider: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Mic,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = "${(file.length() / 1024)} KB",
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Rounded.PlayArrow,
                contentDescription = "Play",
                tint = colorScheme.primary,
                modifier = Modifier.size(24.dp)
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
private fun HybridCallLogRow(log: CallLog, showDivider: Boolean, onClick: () -> Unit) {
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
                .clickable { onClick() }
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
    val navState = rememberNavigationState(
        startRoute = Screen.CallDetails("9876543210"),
        topLevelRoutes = setOf(Screen.Keypad, Screen.Recent, Screen.Contacts, Screen.Search)
    )
    val navigator = remember { Navigator(navState) }
    
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
            navigator = navigator
        )
    }
}
