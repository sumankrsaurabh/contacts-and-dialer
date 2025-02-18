package com.coderon.phone.ui.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.ui.Text

@SuppressLint("MissingPermission")
@Composable
fun SimSelectionDialog(
    availableAccounts: List<PhoneAccountHandle>,
    onDismiss: () -> Unit,
    onSimSelected: (PhoneAccountHandle) -> Unit,
    context: Context = LocalContext.current,
) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(text = "Select SIM Card") }, text = {
        Column {
            availableAccounts.forEachIndexed { index, account ->
                val carrier = getSimInfo(context, account).first
                val number = getSimInfo(context, account).second
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            onSimSelected(
                                account
                            )
                        })
                        .padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text((index + 1).toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Column {
                        Text(carrier, fontSize = 16.sp, fontWeight = FontWeight.Normal)
                        Text(number, fontSize = 14.sp, fontWeight = FontWeight.Normal)
                    }
                }
            }
        }
    }, confirmButton = {})
}


@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
fun getSimInfo(context: Context, account: PhoneAccountHandle): Pair<String, String> {
    val telecomManager = context.getSystemService(TelecomManager::class.java)
    val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)

    val phoneAccount = telecomManager.getPhoneAccount(account)
    val label = phoneAccount?.label?.toString() ?: "Unknown SIM"

    // Match SIM using subscriptionManager
    val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
    val subscriptionInfo: SubscriptionInfo? = subscriptionInfoList?.find { info ->
        info.carrierName.toString() == label // Matching based on carrier label
    }

    val phoneNumber = subscriptionInfo?.number ?: ""

    return label to phoneNumber
}


