package com.coderon.phone

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.ui.MyApp
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.utils.getDefaultDialerIntent
import com.coderon.phone.utils.isDefaultDialer

class MainActivity : ComponentActivity() {
    private var callType: String? = null
    @SuppressLint("MissingPermission", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContent {
//            HideSystemBars()
            PhoneTheme {
                val isDefaultDialerState = remember { mutableStateOf(isDefaultDialer(this)) }

                val defaultDialerLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == RESULT_OK) {
                        isDefaultDialerState.value = true
                    }
                }
                Box() {
                    if (isDefaultDialerState.value) {
                        MyApp()
                    } else {
                        RequestDefaultDialerScreen {
                            getDefaultDialerIntent(this@MainActivity)?.let { intent ->
                                defaultDialerLauncher.launch(intent)
                            } ?: Toast.makeText(
                                this@MainActivity,
                                "Already the default dialer or unavailable",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        callType = intent?.getStringExtra("CALL_TYPE")
    }
}

@Composable
fun RequestDefaultDialerScreen(onRequestDialerRole: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.call_graphics),
                contentDescription = "icon",
                modifier = Modifier.height(200.dp)
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                "Set default dialer",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "To continue using all calling features seamlessly," + " please set this app as your default dialer.",
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier.fillMaxWidth(), onClick = onRequestDialerRole
            ) {
                Text(
                    "Set as default dialer",
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRequestDefaultDialerScreen() {
    PhoneTheme {
        RequestDefaultDialerScreen {}
    }
}