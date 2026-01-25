package com.coderon.phone.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.ui.theme.PhoneTheme

/**
 * Premium Hybrid Contact Pill matched with HybridCallLogPill for visual consistency.
 * Combines iOS smoothness, OneUI 8 rounding, and Material 3 adaptive colors.
 */
@Composable
fun HybridContactRow(
    name: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    photoUrl: String? = null,
    onRowClick: () -> Unit = {},
    onInfoClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(32.dp), // OneUI 8 Super Rounding
        color = colorScheme.surfaceContainerLow,
        onClick = onRowClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Avatar (Matched with HybridCallLogPill sizing)
            ProfileAvatar(
                name = name,
                photoUrl = photoUrl,
                size = 50.dp
            )

            Spacer(Modifier.width(16.dp))

            // Name and Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1
                )

                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }

            // Info icon for details (Matched with HybridCallLogPill)
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "Details",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHybridContactRow() {
    PhoneTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            HybridContactRow(
                name = "John Appleseed",
                subtitle = "Mobile • 9876543210",
                onRowClick = {},
                onInfoClick = {}
            )
        }
    }
}
