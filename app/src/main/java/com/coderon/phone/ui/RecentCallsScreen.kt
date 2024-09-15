package com.coderon.phone.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.automirrored.outlined.CallMissed
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.data.RecentCall
import kotlin.random.Random

@Composable
fun DialerScreen(
    recentCalls: List<RecentCall>,
    onRecentCallClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        LazyColumn {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Phone",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.SemiBold)
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Menu")
                    }
                }
            }
            items(items = recentCalls) { recentCall ->
                RecentCallItem(recentCall = recentCall, onRecentCallClick = onRecentCallClick)
            }
        }
    }
}

@Composable
fun RecentCallItem(
    recentCall: RecentCall,
    onRecentCallClick: (String) -> Unit
) {
    val backgroundColor = remember { generateRandomColor() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRecentCallClick(recentCall.phoneNumber) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (recentCall.contactName.isNotEmpty() && recentCall.profilePicture == null) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = recentCall.contactName.first().toString(),
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
        } else {
            Image(
                bitmap = recentCall.profilePicture ?: ImageBitmap.imageResource(id = R.drawable.user),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recentCall.contactName.ifEmpty { recentCall.phoneNumber },
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (recentCall.callType) {
                        RecentCall.CallType.INCOMING -> Icons.AutoMirrored.Outlined.CallReceived
                        RecentCall.CallType.MISSED -> Icons.AutoMirrored.Outlined.CallMissed
                        RecentCall.CallType.OUTGOING -> Icons.AutoMirrored.Outlined.CallMade
                    },
                    contentDescription = "Call Type",
                    modifier = Modifier.size(14.dp),
                    tint = when (recentCall.callType) {
                        RecentCall.CallType.OUTGOING -> Color.Green
                        else -> Color.Red
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${recentCall.phoneNumber} | ${recentCall.callTime}",
                    fontSize = 12.sp
                )
            }
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Outlined.Call, contentDescription = "Make call")
        }
    }
}

fun generateRandomColor(): Color {
    return Color(
        red = Random.nextFloat(),
        green = Random.nextFloat(),
        blue = Random.nextFloat()
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewDialerScreen() {
    DialerScreen(
        recentCalls = listOf(
            RecentCall("Suman Kumar Saurabh", "7808140285", null, "10:00 AM", RecentCall.CallType.OUTGOING, "2 minutes")
        ),
        onRecentCallClick = {}
    )
}
