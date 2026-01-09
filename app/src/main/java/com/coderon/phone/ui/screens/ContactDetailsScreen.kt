@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

import android.os.Build
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.Contact

/* ------------------------------------------------ */
/* ---------------- CONTACT DETAILS ---------------- */
/* ------------------------------------------------ */

@Composable
fun ContactDetailsScreen(
    contact: Contact,
    callLogs: List<CallLog>,
    navController: NavController
) {
    val maxHeroHeight = 420.dp
    val minHeroHeight = 180.dp
    val density = LocalDensity.current

    var heroHeightPx by remember {
        mutableFloatStateOf(with(density) { maxHeroHeight.toPx() })
    }

    val heroHeight by animateDpAsState(
        targetValue = with(density) { heroHeightPx.toDp() },
        label = "heroHeight"
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                heroHeightPx =
                    (heroHeightPx + available.y)
                        .coerceIn(
                            with(density) { minHeroHeight.toPx() },
                            with(density) { maxHeroHeight.toPx() }
                        )
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .background(MaterialTheme.colorScheme.background)
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            /* ---------------- HERO ---------------- */

            item {
                HeroSection(
                    contact = contact,
                    height = heroHeight,
                    navController = navController
                )
            }

            /* ---------------- INFO ---------------- */

            item { Spacer(Modifier.height(16.dp)) }

            item {
                InfoCard("Phone Numbers") {
                    contact.phoneNumbers.forEach {
                        Text(
                            it.number,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            if (contact.emailAddresses.isNotEmpty()) {
                item {
                    InfoCard("Email") {
                        contact.emailAddresses.forEach {
                            Text(it, fontSize = 16.sp)
                        }
                    }
                }
            }

            if (callLogs.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Recent Calls",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                items(callLogs) { log ->
                    CallLogRow(log)
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- HERO SECTION ------------------ */
/* ------------------------------------------------ */

@Composable
private fun HeroSection(
    contact: Contact,
    height: Dp,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
    ) {

        /* ---- HERO IMAGE ---- */
        AsyncImage(
            model = contact.profilePictureUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
            // sharedElement("contact_${contact.id}") // optional
        )

        /* ---- GRADIENT ---- */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    )
                )
        )

        /* ---- BLUR (ANDROID 12+) ---- */
        if (Build.VERSION.SDK_INT >= 31) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
//                        renderEffect =
//                            RenderEffect
//                                .createBlur
                    }
            )
        }

        /* ---- BACK BUTTON ---- */
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        /* ---- NAME + ACTIONS ---- */
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                contact.displayName,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HeroAction(Icons.Rounded.Call)
                HeroAction(Icons.AutoMirrored.Rounded.Message)
                HeroAction(Icons.Rounded.Videocam)
            }
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- HERO ACTION ------------------- */
/* ------------------------------------------------ */

@Composable
private fun HeroAction(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(Color.White.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

/* ------------------------------------------------ */
/* ---------------- INFO CARD --------------------- */
/* ------------------------------------------------ */

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- CALL ROW ---------------------- */
/* ------------------------------------------------ */

@Composable
private fun CallLogRow(log: CallLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(log.phoneNumber)
        Text(
            log.callTime.formatTime(),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun Test() {
    ContactDetailsScreen(
        Contact(
            id = "1",
            displayName = "John Appleseed",
            profilePictureUrl = null,
        ),
        emptyList(),
        NavController(LocalContext.current)
    )
}