package com.coderon.phone.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.OneUi8DynamicBackground
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun RequestDefaultDialerScreen(
    onRequestDialerRole: () -> Unit
) {
    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(800, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Immersive Dynamic Mesh Background (OneUI 8 Style)
        OneUi8DynamicBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp)
                .alpha(entryAlpha.value)
                .offset { IntOffset(0, entryOffset.value.roundToInt()) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.3f))

            // Brand Graphic with soft glass glow effect
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(140.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.05f)
                ) {}
                Image(
                    painter = painterResource(R.drawable.call_graphics),
                    contentDescription = null,
                    modifier = Modifier.size(180.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Set Default Phone App",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "To unlock the full potential of your device and experience fluid calling, set Coderon Phone as your default dialer.",
                textAlign = TextAlign.Center,
                fontSize = 17.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Glassmorphic Feature Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureItem(Icons.Rounded.Shield, "Real-time Spam Protection")
                FeatureItem(Icons.Rounded.SettingsSuggest, "OneUI 8 Fluid Interface")
                FeatureItem(Icons.Rounded.CheckCircle, "Full Call Management Access")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Primary Action Button (Samsung Style)
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                onClick = onRequestDialerRole,
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Text(
                    text = "Get Started",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your privacy is our priority. Change anytime in Settings.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRequestDefaultDialerScreen() {
    PhoneTheme {
        RequestDefaultDialerScreen {}
    }
}
