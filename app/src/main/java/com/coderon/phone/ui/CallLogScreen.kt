package com.coderon.phone.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coderon.phone.R
import com.coderon.phone.data.helpers.formatDuration
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.modal.CallLog
import com.coderon.phone.data.modal.CallType
import com.coderon.phone.data.modal.Contact
import com.coderon.phone.viewmodel.CallLogViewModel

@Composable
fun CallLogScreen(viewModel: CallLogViewModel = viewModel()) {
    val callLogs = viewModel.callLogs.collectAsState()

    LazyColumn {
        items(callLogs.value) { log ->
            CallLogItem(log)
        }
    }
}

@Composable
fun CallLogItem(log: CallLog) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            painter = painterResource(id = R.drawable.user),
            contentDescription = "Profile image",
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                log.contact?.name ?: log.phoneNumber, fontSize = 18.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    modifier = Modifier.size(14.dp), painter = painterResource(
                        id = when (log.callType) {
                            CallType.INCOMING -> R.drawable.inoming_call
                            CallType.OUTGOING -> R.drawable.outgoing_call
                            CallType.MISSED -> R.drawable.missed_call
                        }
                    ), contentDescription = ""
                )
                Text(log.callDuration.toLong().formatDuration(),fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(log.callTime.formatTime(),fontSize = 14.sp)
            }
        }
        FilledTonalIconButton(
            onClick = { /*TODO*/ },
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "expand"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CallLogUI() {
    CallLogItem(
        log = CallLog(
            id = 0L,
            callDuration = 30.toString(),
            contact = Contact(0L, "Suman Kumar Saurabh", "780840285"),
            callTime = 12,
            callType = CallType.INCOMING,
            phoneNumber = "7808140285"
        )
    )
}