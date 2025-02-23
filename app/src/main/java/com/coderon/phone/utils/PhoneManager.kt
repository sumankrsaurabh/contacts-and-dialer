package com.coderon.phone.utils

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.core.net.toUri

fun isDefaultDialer(context: Context): Boolean {
    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
    return roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
}

fun getDefaultDialerIntent(context: Context): Intent? {
    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
    return if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(
            RoleManager.ROLE_DIALER
        )
    ) {
        roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
    } else {
        null // Return null if the role is unavailable or already held
    }
}

@SuppressLint("MissingPermission")
fun getHandleToUse(
    context: Context, intent: Intent?, onHandleSelected: (PhoneAccountHandle?) -> Unit
) {
    val telecomManager = context.getSystemService(TelecomManager::class.java)
    val availableAccounts = telecomManager.callCapablePhoneAccounts
    val defaultAccount = telecomManager.getDefaultOutgoingPhoneAccount(PhoneAccount.SCHEME_TEL)

    when {
        // Use the explicit phone account from the intent if provided
        intent?.hasExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE) == true -> {
            onHandleSelected(intent.getParcelableExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE))
        }
        // If there's a system default phone account, use it
        defaultAccount != null -> {
            onHandleSelected(defaultAccount)
        }
        // If only one SIM is available, use it
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

@SuppressLint("QueryPermissionsNeeded")
fun openMessagingApp(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "smsto:$phoneNumber".toUri() // Ensures only SMS apps handle this
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "No messaging app found", Toast.LENGTH_SHORT).show()
    }
}
