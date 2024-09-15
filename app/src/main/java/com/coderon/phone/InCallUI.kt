package com.coderon.phone

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R.drawable.logo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CustomCallScreen(
    phoneNumber: String,
    contactName: String?,
    onEndCallClick: () -> Unit,
    onMuteClick: () -> Unit,
    isMuted: Boolean
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val placeholder = painterResource(id = logo)
            val background = remember {
                mutableStateOf(placeholder)
            }
            LaunchedEffect(Unit) {
                scope.launch {
                    val backgroundLocal = getBlurredWallpaper(context)
                    if (backgroundLocal != null) {
                        background.value = backgroundLocal
                    }
                }
            }
            Image(
                painter = background.value,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.blur(200.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(it)
            ) {
                Text(
                    text = contactName ?: phoneNumber,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Calling...",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )
                CallControlButtons(
                    onEndCallClick = onEndCallClick, onMuteClick = onMuteClick, isMuted = isMuted
                )
            }
        }

    }
}

@Composable
fun CallControlButtons(
    onEndCallClick: () -> Unit, onMuteClick: () -> Unit, isMuted: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = onMuteClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(.2f))
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute",
                tint = Color.White
            )
        }

        IconButton(
            onClick = onEndCallClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(.2f))
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardHide,
                contentDescription = "End Call",
                tint = Color.White,
            )
        }
        IconButton(
            onClick = onMuteClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(.2f))
        ) {
            Icon(
                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Mute",
                tint = Color.White
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = onMuteClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(.2f))
        ) {
            Icon(
                imageVector = Icons.Default.Add, contentDescription = "Mute", tint = Color.White
            )
        }

        IconButton(
            onClick = onEndCallClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(.2f))
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = "End Call",
                tint = Color.White,
            )
        }
        IconButton(
            onClick = onMuteClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(.2f))
        ) {
            Icon(
                imageVector = Icons.Default.Contacts,
                contentDescription = "Mute",
                tint = Color.White
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        IconButton(
            onClick = onEndCallClick, modifier = Modifier
                .clip(CircleShape)
                .background(Color.Red)
        ) {
            Icon(
                imageVector = Icons.Rounded.CallEnd,
                contentDescription = "End Call",
                tint = Color.White,
            )
        }
    }
}

suspend fun getBlurredWallpaper(context: Context): Painter? {
    return withContext(Dispatchers.IO) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val wallpaperDrawable = wallpaperManager.drawable as? BitmapDrawable
        val bitmap = wallpaperDrawable?.bitmap

        bitmap?.let {
            val blurredBitmap = blurBitmap(context, it, 25f)
            BitmapPainter(blurredBitmap.asImageBitmap())
        }
    }
}

fun blurBitmap(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
    /*val renderScript = RenderScript.create(context)
    val input = Allocation.createFromBitmap(renderScript, bitmap)
    val output = Allocation.createTyped(renderScript, input.type)
    val script = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    script.setRadius(radius)
    script.setInput(input)
    script.forEach(output)
    output.copyTo(bitmap)
    renderScript.destroy()*/
    return bitmap
}


@Preview
@Composable
fun CallScreenPreview() {
    CustomCallScreen(
        phoneNumber = "123-456-7890",
        contactName = "John Doe",
        onEndCallClick = { /*TODO: Implement End Call Logic*/ },
        onMuteClick = { /*TODO: Implement Mute Logic*/ },
        isMuted = false
    )
}
