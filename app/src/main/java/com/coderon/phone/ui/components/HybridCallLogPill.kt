package com.coderon.phone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact

@Composable
fun HybridCallLogPill(
    name: String,
    phoneNumber: String,
    callType: CallType,
    callTime: Long,
    simSlot: Int,
    contact: Contact?,
    callCount: Int = 1,
    onRowClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    val icon = when (callType) {
        CallType.INCOMING -> R.drawable.ic_call_incoming
        CallType.OUTGOING -> R.drawable.ic_call_outgoing
        CallType.MISSED -> R.drawable.ic_call_missed
        else -> R.drawable.call
    }
    
    val isMissed = callType == CallType.MISSED

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(32.dp),
        color = colorScheme.surfaceContainerLow,
        onClick = onRowClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                name = name,
                photoUrl = contact?.profilePictureUrl
            )

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isMissed) Color.Red else colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (callCount > 1) {
                        Text(
                            text = " ($callCount)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isMissed) Color.Red else colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = if (isMissed) Color.Red else colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = callTime.formatTime(),
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SIM $simSlot",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = "Details",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
