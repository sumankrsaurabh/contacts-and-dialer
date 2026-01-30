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
fun getAvailableSims(context: Context): List<PhoneAccountHandle> {
    val telecomManager = context.getSystemService(TelecomManager::class.java)
    return telecomManager?.callCapablePhoneAccounts ?: emptyList()
}

@SuppressLint("MissingPermission")
fun initiateCall(
    context: Context,
    phoneNumber: String,
    preferredSimId: String? = null,
    onSimSelectionRequired: (List<PhoneAccountHandle>) -> Unit
) {
    val telecomManager = context.getSystemService(TelecomManager::class.java) ?: return
    val availableAccounts = telecomManager.callCapablePhoneAccounts
    
    if (availableAccounts.isEmpty()) {
        Toast.makeText(context, "No SIM available", Toast.LENGTH_SHORT).show()
        return
    }

    // Check if we have a preferred SIM set in app settings
    val preferredAccount = availableAccounts.find { it.id == preferredSimId }
    
    if (preferredAccount != null) {
        placeCall(context, phoneNumber, preferredAccount)
        return
    }

    // If preferred is null (Ask every time) or not found, try system default
    val defaultAccount = telecomManager.getDefaultOutgoingPhoneAccount(PhoneAccount.SCHEME_TEL)

    when {
        defaultAccount != null -> {
            placeCall(context, phoneNumber, defaultAccount)
        }
        availableAccounts.size == 1 -> {
            placeCall(context, phoneNumber, availableAccounts[0])
        }
        availableAccounts.size > 1 -> {
            onSimSelectionRequired(availableAccounts)
        }
        else -> {
            Toast.makeText(context, "No SIM available", Toast.LENGTH_SHORT).show()
        }
    }
}

@SuppressLint("MissingPermission")
fun placeCall(context: Context, phoneNumber: String, handle: PhoneAccountHandle) {
    val uri = Uri.fromParts("tel", phoneNumber, null)
    val callIntent = Intent(Intent.ACTION_CALL, uri).apply {
        putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
