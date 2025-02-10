package com.coderon.phone.ui.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.coderon.phone.ui.Text

@RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS])
fun getSimInfo(context: Context): List<SubscriptionInfo> {
    val subscriptionManager =
        ContextCompat.getSystemService(context, SubscriptionManager::class.java)
    return subscriptionManager?.activeSubscriptionInfoList ?: emptyList()
}

@SuppressLint("MissingPermission")
@Composable
fun SimSelectionDialog(
    phoneNumber: String,
    makeCallWithSim: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val simList = remember { getSimInfo(context) }

    if (simList.isEmpty()) {
        Toast.makeText(context, "No SIM detected", Toast.LENGTH_SHORT).show()
        onDismiss()
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp)
            ) {
                simList.forEach { simInfo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                makeCallWithSim(phoneNumber, simInfo.subscriptionId)
                                onDismiss()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "SIM ${simInfo.simSlotIndex + 1}", fontSize = 18.sp)
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(text = simInfo.carrierName.toString(), fontSize = 16.sp)
                            Text(text = simInfo.number ?: "Unknown", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
