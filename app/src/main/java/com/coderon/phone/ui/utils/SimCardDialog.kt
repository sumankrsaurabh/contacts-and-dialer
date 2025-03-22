package com.coderon.phone.ui.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.ui.Text

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun SimSelectionDialog(
    availableAccounts: List<PhoneAccountHandle> = emptyList(),
    onSimSelected: (PhoneAccountHandle) -> Unit = {},
    context: Context = LocalContext.current,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSystemInDarkTheme()) Color.Black else Color.White,
                    RoundedCornerShape(24.dp)
                ) // Rounded top corners
                .padding(vertical = 16.dp, horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select SIM",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp),
                color = if (isSystemInDarkTheme()) Color.White else Color.Black
            )

            availableAccounts.forEachIndexed { index, account ->
                val carrier = getSimInfo(context, account)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(25))
                        .clickable { onSimSelected(account) }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${index + 1}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black
                    )
                    Text(
                        text = carrier,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black
                    )
                }
                if (index != availableAccounts.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}


@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
fun getSimInfo(context: Context, account: PhoneAccountHandle): String {
    return if (isInPreviewMode()) {
        // Return dummy data for previews
        when (account.id) {
            "1" -> "Carrier A"
            "2" -> "Carrier B"
            else -> "Unknown SIM"
        }
    } else {
        // Actual implementation
        val telecomManager = context.getSystemService(TelecomManager::class.java)
        val phoneAccount = telecomManager.getPhoneAccount(account)
        val label = phoneAccount?.label?.toString() ?: "Unknown SIM"

        label
    }
}

// Helper function to detect preview mode
fun isInPreviewMode(): Boolean {
    return try {
        Class.forName("androidx.compose.ui.tooling.preview.Preview")
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(device = "id:pixel_6_pro", showBackground = true)
@PreviewLightDark
@Composable
fun SimSelectionDialogPreview() {
    val context = LocalContext.current
    val dummyAccounts = listOf(
        PhoneAccountHandle(android.content.ComponentName("com.example", "Sim1"), "1"),
        PhoneAccountHandle(android.content.ComponentName("com.example", "Sim2"), "2")
    )

    SimSelectionDialog(
        availableAccounts = dummyAccounts,
        onSimSelected = { selectedSim -> println("Selected SIM: $selectedSim") },
        context = context
    )
}
