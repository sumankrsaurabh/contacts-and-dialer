package com.coderon.phone.ui.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SimCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.ui.components.Text

/**
 * Premium SIM Selection Dialog redesigned to match iOS smoothness, 
 * OneUI 8 extreme rounding, and Material 3 adaptive tokens.
 */
@Composable
fun SimSelectionDialog(
    availableAccounts: List<PhoneAccountHandle>,
    onSimSelected: (PhoneAccountHandle) -> Unit,
    onDismiss: () -> Unit,
    context: Context = LocalContext.current,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss, indication = null, interactionSource = null),
        contentAlignment = Alignment.BottomCenter
    ) {
        // iOS Style Blurred Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .blur(15.dp)
        )

        // OneUI 8 Style Rounded Sheet
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(36.dp),
            color = colorScheme.surfaceContainer,
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .size(36.dp, 4.dp)
                        .clip(CircleShape)
                        .background(colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )

                Spacer(Modifier.height(28.dp))

                Text(
                    text = "Select SIM Card",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Choose which SIM to use for this call",
                    fontSize = 15.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(32.dp))

                // Options list as distinct Pills (matched with HybridContactRow/HybridCallLogPill)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    availableAccounts.forEachIndexed { index, account ->
                        SimOptionPill(
                            index = index,
                            label = getSimInfo(context, account),
                            onClick = { onSimSelected(account) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SimOptionPill(
    index: Int,
    label: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colorScheme.surfaceContainerLow,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container (Matched with ProfileAvatar styling)
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = colorScheme.primaryContainer.copy(alpha = 0.8f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.SimCard,
                        contentDescription = null,
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SIM ${index + 1}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = label,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
fun getSimInfo(context: Context, account: PhoneAccountHandle): String {
    val telecomManager = context.getSystemService(TelecomManager::class.java) ?: return "Unknown"
    val phoneAccount = telecomManager.getPhoneAccount(account)
    return phoneAccount?.label?.toString() ?: "Unknown Network"
}

@Preview(showBackground = true)
@PreviewLightDark
@Composable
fun SimSelectionDialogPreview() {
    SimSelectionDialog(
        availableAccounts = emptyList(),
        onSimSelected = {},
        onDismiss = {}
    )
}
