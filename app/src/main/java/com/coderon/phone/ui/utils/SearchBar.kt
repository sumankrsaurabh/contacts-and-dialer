package com.coderon.phone.ui.utils

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coderon.phone.ui.Text

@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(query) }

    TextField(
        value = text,
        onValueChange = {
            text = it
            onQueryChange(it)
        },
        placeholder = { Text("Search", color = MaterialTheme.colorScheme.onSurface) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search Icon") },
        modifier = Modifier
            .fillMaxWidth()// Control expansion width
            .animateContentSize()
            .padding(horizontal = 16  .dp),
        shape = RoundedCornerShape(50),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Preview
@Composable
private fun SearchBarPreview() {
    var searchText by rememberSaveable { mutableStateOf("") }
    SearchScreen(
        query = searchText,
        onQueryChange = { searchText = it },
        onSearch = { /* Handle search action */ },
        onDismiss = { /* Handle dismiss action */ }
    )
}
