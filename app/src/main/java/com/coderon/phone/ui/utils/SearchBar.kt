package com.coderon.phone.ui.utils

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.coderon.phone.ui.Text
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(query) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(300) // Small delay to ensure Compose focuses correctly
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    TextField(
        value = text,
        onValueChange = {
            text = it
            onQueryChange(it)
        },
        placeholder = { Text("Search", color = MaterialTheme.colorScheme.onSurface) },
        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back Icon") },
        trailingIcon = {
            IconButton(onClick = { onDismiss() }) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = "Search Icon"
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
//            .padding(top = 16.dp)
            .focusRequester(focusRequester),
        shape = RectangleShape,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch()
            }
        ),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Preview
@Composable
private fun SearchBarPreview() {
    var searchText by remember { mutableStateOf("") }
    SearchScreen(
        query = searchText,
        onQueryChange = { searchText = it },
        onSearch = { /* Handle search action */ },
        onDismiss = { /* Handle dismiss action */ }
    )
}
