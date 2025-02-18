package com.coderon.phone.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.core.app.ActivityCompat

fun isDefaultDialer(context: Context): Boolean {
    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
    return roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
}

fun requestDefaultDialerRole(activity: Activity) {
    try {
        Toast.makeText(activity, "Requesting default dialer role", Toast.LENGTH_SHORT).show()
        val roleManager = activity.getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(
                RoleManager.ROLE_DIALER
            )
        ) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            ActivityCompat.startActivityForResult(activity, intent, 100, null)
            Toast.makeText(activity, "Request sent", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Already the default dialer", Toast.LENGTH_SHORT).show()
        }
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(activity, "Error: Cannot request default dialer role", Toast.LENGTH_SHORT)
            .show()
    }
}


@SuppressLint("MissingPermission")
fun getHandleToUse(
    context: Context,
    intent: Intent?,
    onHandleSelected: (PhoneAccountHandle?) -> Unit
) {
    val telecomManager = context.getSystemService(TelecomManager::class.java)
    val availableAccounts = telecomManager.callCapablePhoneAccounts

    when {
        // If intent contains an explicit phone account, use it
        intent?.hasExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE) == true -> {
            onHandleSelected(intent.getParcelableExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE))
        }
        // If there's only one SIM, use it
        availableAccounts.size == 1 -> {
            onHandleSelected(availableAccounts.firstOrNull())
        }
        // If multiple SIMs exist, prompt user for selection
        availableAccounts.size > 1 -> {
            onHandleSelected(null) // Let the UI handle SIM selection
        }
        // No SIMs found
        else -> {
            onHandleSelected(null)
        }
    }
}



fun initiateCall(context: Context, phoneNumber: String) {
    getHandleToUse(context, null) { selectedHandle ->
        if (selectedHandle != null) {
            placeCall(context, phoneNumber, selectedHandle)
        } else {
            Toast.makeText(context, "No SIM selected", Toast.LENGTH_SHORT).show()
        }
    }
}

fun placeCall(context: Context, phoneNumber: String, handle: PhoneAccountHandle) {
    val telecomManager = context.getSystemService(TelecomManager::class.java)
    val uri = Uri.fromParts("tel", phoneNumber, null)
    val callIntent = Intent(Intent.ACTION_CALL, uri).apply {
        putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle) // Use the selected SIM
    }
    try {
        context.startActivity(callIntent)
    } catch (e: SecurityException) {
        Toast.makeText(context, "Permission Denied: Cannot make call", Toast.LENGTH_SHORT).show()
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to make call", Toast.LENGTH_SHORT).show()
    }
}
/*

@RequiresPermission(allOf = [
    Manifest.permission.READ_CALL_LOG,
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.PROCESS_OUTGOING_CALLS
])
fun Intent.phoneCallInformation(): CallStateEnum {
    val action = action
    val extras = extras
    if (extras != null) {
        if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            // Incoming Call
            val state = getStringExtra(TelephonyManager.EXTRA_STATE)!!
            if (hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) && state == TelephonyManager.EXTRA_STATE_RINGING) {
                return CallStateEnum.Incoming
            }
        } else if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            return CallStateEnum.Ongoing
        }
    }
    return CallStateEnum.Idle
}*/
