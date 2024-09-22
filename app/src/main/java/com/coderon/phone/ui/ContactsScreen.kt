package com.coderon.phone.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.coderon.phone.R
import com.coderon.phone.data.modal.Contact
import com.coderon.phone.viewmodel.ContactViewModel
import kotlinx.coroutines.launch

@Composable
fun ContactsScreen(viewModel: ContactViewModel = viewModel()) {
    var searchText by remember { mutableStateOf("") }
    val contacts = viewModel.contacts.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // State for overlaying letter
    var showLetter by remember { mutableStateOf(false) }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    val overlayAlpha = remember { Animatable(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            SearchBar(searchText) {
                searchText = it
            }
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    // Main Contact List
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp), state = listState
                    ) {
                        val filteredContacts = contacts.value.filter {
                            it.name.contains(
                                searchText, ignoreCase = true
                            ) || it.phoneNumber.contains(searchText)
                        }

                        items(filteredContacts) { contact ->
                            ContactItem(contact)
                        }
                    }
                }

                // Letter Scroll Bar with sliding and animation
                Column(
                    modifier = Modifier.width(16.dp)
                ) {
                    SlidingAlphabetScrollBar(onLetterSelected = { letter ->
                        selectedLetter = letter
                        showLetter = true
                        coroutineScope.launch {
                            overlayAlpha.animateTo(
                                1f, animationSpec = tween(durationMillis = 300)
                            )
                        }
                        coroutineScope.launch {
                            viewModel.scrollToLetter(letter, listState)
                        }
                    }, onDragEnd = {
                        coroutineScope.launch {
                            overlayAlpha.animateTo(
                                0f, animationSpec = tween(durationMillis = 300)
                            )
                            showLetter = false
                        }
                    })
                }
            }
        }

        AnimatedVisibility(
            visible = selectedLetter != null && showLetter,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            selectedLetter?.let {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = it.toString(),
                            fontSize = 64.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = overlayAlpha.value)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SearchBar(searchText: String, onSearchTextChanged: (String) -> Unit) {
    TextField(
        value = searchText,
        onValueChange = onSearchTextChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        placeholder = { Text("Search contacts") },
        singleLine = true,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Transparent,
            unfocusedIndicatorColor = Transparent,
            disabledIndicatorColor = Transparent
        )
    )
}

@Composable
fun ContactItem(contact: Contact) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)
    ) {
        val painterImage = if (contact.profilePictureUrl == null) {
            painterResource(id = R.drawable.user)
        } else {
            rememberAsyncImagePainter(model = contact.profilePictureUrl)
        }
        Image(
            painter = painterImage,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(contact.name, fontSize = 18.sp)
            Text(contact.phoneNumber, fontSize = 14.sp)
        }
    }
}

@Composable
fun SlidingAlphabetScrollBar(
    onLetterSelected: (Char) -> Unit, onDragEnd: () -> Unit
) {
    val letters = ('A'..'Z').toList()
    var draggedLetter by remember { mutableStateOf<Char?>(null) }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(end = 8.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(onDragStart = { offset ->
                    val letterIndex = (offset.y / (size.height / letters.size))
                        .toInt()
                        .coerceIn(0, letters.size - 1)
                    draggedLetter = letters[letterIndex]
                    onLetterSelected(letters[letterIndex])
                }, onVerticalDrag = { change, _ ->
                    if (change.positionChange() != Offset.Zero) change.consume()

                    val letterHeight = size.height / letters.size
                    val draggedIndex = (change.position.y / letterHeight)
                        .toInt()
                        .coerceIn(0, letters.size - 1)
                    draggedLetter = letters[draggedIndex]
                    onLetterSelected(letters[draggedIndex])
                }, onDragEnd = {
                    onDragEnd()
                })
            }, contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
                .width(8.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            letters.forEach { letter ->
                Text(
                    text = letter.toString(),
                    fontSize = 12.sp,
                    color = if (draggedLetter == letter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

suspend fun ContactViewModel.scrollToLetter(letter: Char, listState: LazyListState) {
    val position = contacts.value.indexOfFirst { it.name.startsWith(letter, ignoreCase = true) }
    if (position != -1) {
        listState.scrollToItem(position)
    }
}

@Preview
@Composable
private fun ContactItemUI() {
    ContactItem(
        contact = Contact(
            id = 101, phoneNumber = "7808140285", name = "Suman Kumar Saurabh"
        )
    )
}
