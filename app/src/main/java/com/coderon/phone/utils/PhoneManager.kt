package com.coderon.phone.utils

import android.app.Activity
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.annotation.RequiresApi


fun Activity.launchSetDefaultDialerIntent() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = getSystemService(RoleManager::class.java)
        if (roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(
                RoleManager.ROLE_DIALER
            )
        ) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            startActivityForResult(intent,1007)
        }
    } else {
        Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(
            TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName
        ).apply {
            try {
                startActivityForResult(this, 1007)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this@launchSetDefaultDialerIntent, "No activity found", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@launchSetDefaultDialerIntent, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

fun Context.isDefaultDialer(): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        telecomManager.defaultDialerPackage == packageName
    } else {
        val roleManager = getSystemService(RoleManager::class.java)
        roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun Activity.setDefaultCallerIdApp() {
    val roleManager = getSystemService(RoleManager::class.java)
    if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        startActivityForResult(intent, 1008)
    }
}
