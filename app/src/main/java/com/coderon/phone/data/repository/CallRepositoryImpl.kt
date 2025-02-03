package com.coderon.phone.data.repository

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri
import com.coderon.phone.domain.repository.CallRepository

class CallRepositoryImpl(private val context: Context) : CallRepository {

    /**
     * Initiates a phone call to the given number using the specified SIM.
     */
    @RequiresPermission(allOf = [Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE])
    override fun makeCall(phoneNumber: String, subscriptionID: Int?) {
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = "tel:$phoneNumber".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (subscriptionID != null) {
            val phoneAccountHandle = getPhoneAccountHandle(subscriptionID)
            phoneAccountHandle?.let {
                callIntent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", it)
            }
        }

        try {
            context.startActivity(callIntent)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * Retrieves the PhoneAccountHandle associated with the given subscription ID.
     */
    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS])
    private fun getPhoneAccountHandle(subscriptionID: Int): PhoneAccountHandle? {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        val subscriptionInfo =
            subscriptionManager.activeSubscriptionInfoList?.find { it.subscriptionId == subscriptionID }
        subscriptionInfo?.let {
            val phoneAccountHandles = telecomManager.callCapablePhoneAccounts
            return phoneAccountHandles.find { handle -> handle.id.contains(it.subscriptionId.toString()) }
        }
        return null
    }

    /**
     * Ends an active call (Requires Telecom API).
     */
    @RequiresPermission(Manifest.permission.ANSWER_PHONE_CALLS)
    override fun endCall() {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isDefaultDialer()) {
            telecomManager.endCall()
        }
    }

    /**
     * Answers an incoming call (Requires Telecom API and Role Default Dialer).
     */
    @RequiresPermission(anyOf = [Manifest.permission.MODIFY_PHONE_STATE, Manifest.permission.ANSWER_PHONE_CALLS])
    override fun answerCall() {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isDefaultDialer()) {
            telecomManager.acceptRingingCall()
        }
    }

    /**
     * Checks if the app is the default phone app (needed for call control features).
     */
    private fun isDefaultDialer(): Boolean {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        return telecomManager.defaultDialerPackage == context.packageName
    }
}
