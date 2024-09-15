package com.coderon.phone.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import kotlinx.coroutines.launch

data class Buttons(val number: String, val letter: String)

val buttons = listOf(
    Buttons("1", ""),
    Buttons("2", "ABC"),
    Buttons("3", "DEF"),
    Buttons("4", "GHI"),
    Buttons("5", "JKL"),
    Buttons("6", "MNO"),
    Buttons("7", "PQRS"),
    Buttons("8", "TUV"),
    Buttons("9", "WXYZ"),
    Buttons("*", ""),
    Buttons("0", "+"),
    Buttons("#", "")
)

@Composable
fun KeypadScreen() {
    val numberInput = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = numberInput.value,
                    onValueChange = { numberInput.value = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    readOnly = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 32.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                )

                if (numberInput.value.isNotEmpty()) {
                    IconButton(onClick = {
                        numberInput.value = numberInput.value.dropLast(1)
                    }, modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            coroutineScope.launch {
                                numberInput.value = "" // Clear all text
                            }
                        })
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Backspace,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                items(buttons) { button ->
                    ButtonItem(button = button, onClick = {
                        numberInput.value += button.number
                    })
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()
            ) {
                val context = LocalContext.current
                FilledIconButton(
                    onClick = { //makePhoneCall(context, numberInput.value.toString())
                    },
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.call),
                        contentDescription = "Place call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ButtonItem(button: Buttons, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(56.dp)
            .clickable { onClick() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = button.number, fontSize = 28.sp, color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = button.letter,
            fontSize = 14.sp,
            color = Color.White.copy(.6f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun KeypadScreenPreview() {
    KeypadScreen()
}
