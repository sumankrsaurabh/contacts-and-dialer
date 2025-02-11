package com.coderon.phone.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import com.coderon.phone.ui.utils.SimSelectionDialog

fun isDefaultDialer(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
    } else {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        telecomManager.defaultDialerPackage == context.packageName
    }
}

fun requestDefaultDialerRole(activity: Activity) {
    try {
        Toast.makeText(activity, "Requesting default dialer role", Toast.LENGTH_SHORT).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
        } else {
            val intent =
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS) // Open Default Apps settings
            activity.startActivity(intent)
            Toast.makeText(activity, "Request sent", Toast.LENGTH_SHORT).show()
        }
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(activity, "Error: Cannot request default dialer role", Toast.LENGTH_SHORT)
            .show()
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("MissingPermission")
fun getHandleToUse(
    context: Context,
    intent: Intent?,
    phoneNumber: String,
    onHandleSelected: (PhoneAccountHandle?) -> Unit
) {
    val telecomManager = context.getSystemService(TelecomManager::class.java)
    val defaultHandle = telecomManager.getDefaultOutgoingPhoneAccount(PhoneAccount.SCHEME_TEL)

    when {
        intent?.hasExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE) == true -> {
            onHandleSelected(intent.getParcelableExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE))
        }

        defaultHandle != null -> {
            onHandleSelected(defaultHandle)
        }

        else -> {
            onHandleSelected(null) // Let the Composable handle SIM selection
        }
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
fun initiateCall(context: Context, phoneNumber: String) {
    getHandleToUse(context, null, phoneNumber) { selectedHandle ->
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
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun InitiateCallScreen(
    context: Context,
    phoneNumber: String
) {
    var selectedHandle by remember { mutableStateOf<PhoneAccountHandle?>(null) }
    var showSimDialog by remember { mutableStateOf(false) }

    LaunchedEffect(phoneNumber) {
        getHandleToUse(context, null, phoneNumber) { handle ->
            if (handle != null) {
                selectedHandle = handle
            } else {
                showSimDialog = true // Show SIM selection if no default handle
            }
        }
    }

    if (showSimDialog) {
        SimSelectionDialog(
            context = context,
            phoneNumber = phoneNumber,
            onDismiss = { showSimDialog = false },
            onSimSelected = { handle ->
                selectedHandle = handle
                showSimDialog = false
            }
        )
    }

    LaunchedEffect(selectedHandle) {
        selectedHandle?.let {
            placeCall(context, phoneNumber, it)
        }
    }
}
