package com.coderon.phone.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.coderon.phone.ui.Text

/* ------------------------------------------------
   ADD CONTACT (iOS STYLE)
------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    navController: NavController? = null,
    onSaveContact: (String, String, String?) -> Unit = { _, _, _ -> }
) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) Color.Black else Color.White
    val primary = if (isDark) Color.White else Color.Black

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            photoUri = it
        }

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }
                },
                title = {
                    Text("New Contact", fontSize = 18.sp)
                },
                actions = {
                    TextButton(
                        onClick = {
                            val displayName =
                                listOf(firstName, lastName).joinToString(" ").trim()
                            onSaveContact(
                                displayName,
                                phoneNumber,
                                photoUri?.toString()
                            )
                            navController?.popBackStack()
                        },
                        enabled = firstName.isNotBlank() && phoneNumber.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Done")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(24.dp))

            /* ---------- AVATAR ---------- */

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(photoUri)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Add Photo",
                        tint = primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            /* ---------- FORM ---------- */

            IOSInputField(
                label = "First name",
                value = firstName,
                onValueChange = { firstName = it }
            )

            IOSInputField(
                label = "Last name",
                value = lastName,
                onValueChange = { lastName = it }
            )

            IOSInputField(
                label = "Phone",
                value = phoneNumber,
                keyboardType = KeyboardType.Phone,
                onValueChange = { phoneNumber = it }
            )

            IOSInputField(
                label = "Email",
                value = email,
                keyboardType = KeyboardType.Email,
                onValueChange = { email = it }
            )
        }
    }
}

/* ------------------------------------------------
   INPUT FIELD (iOS LOOK)
------------------------------------------------ */

@Composable
private fun IOSInputField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(16.dp))
    }
}

/* ------------------------------------------------
   PREVIEW
------------------------------------------------ */

@Preview(showBackground = true)
@Composable
fun PreviewAddContactIOS() {
    AddContactScreen()
}
