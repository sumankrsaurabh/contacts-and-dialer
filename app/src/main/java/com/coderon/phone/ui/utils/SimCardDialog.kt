package com.coderon.phone.ui.utils

import android.Manifest
import android.content.Context
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import com.coderon.phone.ui.Text

@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
@Composable
fun SimSelectionDialog(
    context: Context,
    phoneNumber: String,
    onDismiss: () -> Unit,
    onSimSelected: (PhoneAccountHandle) -> Unit
) {
    val telecomManager = context.getSystemService(TelecomManager::class.java)
    val simHandles = telecomManager.callCapablePhoneAccounts

    if (simHandles.isEmpty()) {
        onDismiss()
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select SIM for $phoneNumber") },
        text = {
            Column {
                simHandles.forEach { sim ->
                    Button(onClick = { onSimSelected(sim) }) {
                        Text(sim.id) // Display SIM name
                    }
                }
            }
        },
        confirmButton = { /* No confirm button needed since SIM selection is direct */ },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
