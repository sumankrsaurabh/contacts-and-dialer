package com.coderon.phone.ui.screens

import android.Manifest
import android.content.Context
import android.telecom.TelecomManager
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.rounded.VideoCall
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.ui.utils.CoderonTopAppBar
import com.coderon.phone.ui.utils.SimSelectionDialog
import com.coderon.phone.utils.placeCall


@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
@Composable
fun DialerScreen(
    context: Context = LocalContext.current
) {
    var dialedNumber by remember { mutableStateOf("") }
    val maxDialedNumberLength = 15
    val showSimSelectDialog = remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val telecomManager = context.getSystemService(TelecomManager::class.java)
    val availableAccounts = telecomManager.callCapablePhoneAccounts
    if (showSimSelectDialog.value) {
        SimSelectionDialog(
            availableAccounts = availableAccounts,
            onDismiss = { showSimSelectDialog.value = false },
            onSimSelected = {
                placeCall(context, dialedNumber, it)
            })
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CoderonTopAppBar(
            showBackArrow = false,
            onSearch = { isSearchExpanded = true },
            onMenu = {},
            title = "Dialpad",
            isSearchExpanded = isSearchExpanded,
            searchText = searchText,
            onSearchTextChanged = { searchText = it },
            onDismissSearch = { isSearchExpanded = false })
        Spacer(Modifier.weight(1f))
        Text(
            text = dialedNumber, fontSize = 32.sp, modifier = Modifier.padding(18.dp)
        )

        DialPad { digit ->
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
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.VideoCall,
                    contentDescription = "Delete last digit",
                    modifier = Modifier.size(32.dp)
                )
            }
            FilledIconButton(
                onClick = {
                    showSimSelectDialog.value = true
                },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.call),
                    contentDescription = "Call",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(onClick = {
                if (dialedNumber.isNotEmpty()) {
                    dialedNumber = dialedNumber.dropLast(1) // Remove last digit
                }
            }) {
                Icon(
                    imageVector = Icons.Default.RemoveCircle,
                    contentDescription = "Delete last digit",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
fun DialPad(onDigitPress: (String) -> Unit) {
    Column {
        // Loop through rows of digits for the dialer
        listOf("123", "456", "789", "*0#").forEach { digitRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Loop through each digit in the row
                digitRow.forEach { dialedDigit ->
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .size(64.dp)
                            .clip(CircleShape)
                            .clickable { onDigitPress(dialedDigit.toString()) }
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dialedDigit.toString(), fontSize = 26.sp)
                    }
                }
            }
        }
    }
}


@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
@Preview(showBackground = true)
@Composable
private fun Preview() {
    DialerScreen()
}