@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.data.model.PhoneNumberType
import com.coderon.phone.ui.components.HybridAlertDialog
import com.coderon.phone.ui.components.Text

@Composable
fun AddContactScreen(
    navController: NavController? = null,
    initialPhoneNumber: String? = null,
    existingContact: Contact? = null,
    onSaveContact: (Contact) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    var firstName by remember { mutableStateOf(existingContact?.firstName ?: "") }
    var lastName by remember { mutableStateOf(existingContact?.lastName ?: "") }
    
    val phoneNumbers = remember { 
        val list = mutableStateListOf<PhoneNumber>()
        if (existingContact != null) {
            list.addAll(existingContact.phoneNumbers)
        } else {
            list.add(PhoneNumber(initialPhoneNumber ?: "", PhoneNumberType.MOBILE))
        }
        list
    }
    
    val emailAddresses = remember { 
        val list = mutableStateListOf<String>()
        if (existingContact != null) {
            list.addAll(existingContact.emailAddresses)
        } else {
            list.add("")
        }
        list
    }
    
    var photoUri by remember { mutableStateOf<Uri?>(existingContact?.profilePictureUrl?.let { Uri.parse(it) }) }
    var isFavorite by remember { mutableStateOf(existingContact?.isFavorite ?: false) }

    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasChanges = firstName != (existingContact?.firstName ?: "") || 
                     lastName != (existingContact?.lastName ?: "") || 
                     phoneNumbers.toList() != (existingContact?.phoneNumbers ?: listOf(PhoneNumber(initialPhoneNumber ?: "", PhoneNumberType.MOBILE))) ||
                     emailAddresses.toList() != (existingContact?.emailAddresses ?: listOf("")) ||
                     (photoUri?.toString() ?: "") != (existingContact?.profilePictureUrl ?: "") ||
                     isFavorite != (existingContact?.isFavorite ?: false)

    val handleBack = {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            navController?.popBackStack()
        }
    }

    BackHandler(enabled = true) {
        handleBack()
    }

    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        photoUri = it
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                        .background(colorScheme.background.copy(alpha = 0.7f))
                )
                
                LargeTopAppBar(
                    title = {
                        Text(if (existingContact != null) "Edit Contact" else "New Contact", fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    },
                    navigationIcon = {
                        TextButton(
                            onClick = { handleBack() },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Cancel", color = colorScheme.primary, fontSize = 17.sp)
                        }
                    },
                    actions = {
                        val canSave = firstName.isNotBlank() && phoneNumbers.any { it.number.isNotBlank() }
                        TextButton(
                            onClick = {
                                val contact = Contact(
                                    id = existingContact?.id ?: "",
                                    firstName = firstName,
                                    lastName = lastName,
                                    displayName = "$firstName $lastName".trim().ifBlank { phoneNumbers.firstOrNull { it.number.isNotBlank() }?.number ?: "Unknown" },
                                    phoneNumbers = phoneNumbers.filter { it.number.isNotBlank() },
                                    emailAddresses = emailAddresses.filter { it.isNotBlank() },
                                    profilePictureUrl = photoUri?.toString(),
                                    isFavorite = isFavorite
                                )
                                onSaveContact(contact)
                                navController?.popBackStack()
                            },
                            enabled = canSave,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                "Done",
                                color = if (canSave) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = colorScheme.surfaceContainer.copy(alpha = 0.8f)
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceContainerHigh)
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
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Add Photo",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    "Add Photo",
                                    fontSize = 12.sp,
                                    color = colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }

                item {
                    SectionContainer {
                        HybridInputField(
                            label = "First name",
                            value = firstName,
                            onValueChange = { firstName = it }
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = colorScheme.outlineVariant.copy(alpha = 0.3f))
                        HybridInputField(
                            label = "Last name",
                            value = lastName,
                            onValueChange = { lastName = it }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }

                item {
                    Text(
                        "Phone",
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 8.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                }

                itemsIndexed(phoneNumbers) { index, phone ->
                    SectionContainer {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            var expanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.padding(start = 20.dp).clickable { expanded = true }) {
                                Text(phone.type.name.lowercase().replaceFirstChar { it.uppercase() }, color = colorScheme.primary, fontSize = 14.sp)
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    PhoneNumberType.entries.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                            onClick = {
                                                phoneNumbers[index] = phone.copy(type = type)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            HybridInputField(
                                label = "",
                                value = phone.number,
                                keyboardType = KeyboardType.Phone,
                                onValueChange = { phoneNumbers[index] = phone.copy(number = it) },
                                modifier = Modifier.weight(1f)
                            )
                            if (phoneNumbers.size > 1) {
                                Icon(
                                    Icons.Default.RemoveCircle,
                                    contentDescription = "Remove",
                                    tint = Color.Red,
                                    modifier = Modifier.padding(end = 16.dp).clickable { phoneNumbers.removeAt(index) }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { phoneNumbers.add(PhoneNumber("", PhoneNumberType.MOBILE)) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color(0xFF34C759))
                        Spacer(Modifier.width(8.dp))
                        Text("Add phone", color = colorScheme.onSurface, fontSize = 16.sp)
                    }
                }

                item {
                    Text(
                        "Email",
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 8.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                }

                itemsIndexed(emailAddresses) { index, email ->
                    SectionContainer {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HybridInputField(
                                label = "Email",
                                value = email,
                                keyboardType = KeyboardType.Email,
                                onValueChange = { emailAddresses[index] = it },
                                modifier = Modifier.weight(1f)
                            )
                            if (emailAddresses.size > 1) {
                                Icon(
                                    Icons.Default.RemoveCircle,
                                    contentDescription = "Remove",
                                    tint = Color.Red,
                                    modifier = Modifier.padding(end = 16.dp).clickable { emailAddresses.removeAt(index) }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { emailAddresses.add("") },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color(0xFF34C759))
                        Spacer(Modifier.width(8.dp))
                        Text("Add email", color = colorScheme.onSurface, fontSize = 16.sp)
                    }
                }

                item {
                    SectionContainer {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Favorite", fontSize = 16.sp)
                            Switch(checked = isFavorite, onCheckedChange = { isFavorite = it })
                        }
                    }
                    Spacer(Modifier.height(48.dp))
                }
            }

            if (showDiscardDialog) {
                HybridAlertDialog(
                    title = "Discard Changes?",
                    message = "Are you sure you want to discard this contact? Your changes will not be saved.",
                    confirmText = "Discard",
                    confirmColor = Color.Red,
                    onConfirm = {
                        showDiscardDialog = false
                        navController?.popBackStack()
                    },
                    onDismiss = { showDiscardDialog = false }
                )
            }
        }
    }
}

@Composable
private fun SectionContainer(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        content = { Column(content = content) }
    )
}

@Composable
private fun HybridInputField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.width(100.dp)
            )
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(label, color = colorScheme.onSurfaceVariant.copy(alpha = 0.38f), fontSize = 16.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = colorScheme.primary
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddContactComplete() {
    AddContactScreen()
}
