package com.coderon.phone.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HybridContactRow(
    name: String,
    subtitle: String? = null,
    photoUrl: String? = null,
    isMyCard: Boolean = false,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(
            name = name,
            photoUrl = photoUrl,
            size = if (isMyCard) 60.dp else 42.dp
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = name,
                fontSize = if (isMyCard) 18.sp else 17.sp,
                fontWeight = if (isMyCard) FontWeight.SemiBold else FontWeight.Medium,
                color = colorScheme.onSurface
            )

            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
