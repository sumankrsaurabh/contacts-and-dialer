package com.coderon.phone.ui.screens

/*

@Composable
fun BlockedNumbersScreen(viewModel: BlockedNumberViewModel) {
    val blockedNumbers by viewModel.blockedNumbers.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Blocked Numbers", style = MaterialTheme.typography.headlineMedium)

        LazyColumn {
            items(blockedNumbers) { blockedNumber ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(blockedNumber.phoneNumber)
                    IconButton(onClick = { viewModel.unblockNumber(blockedNumber.phoneNumber) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Unblock")
                    }
                }
            }
        }

        var newNumber by remember { mutableStateOf("") }

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            TextField(
                value = newNumber,
                onValueChange = { newNumber = it },
                label = { Text("Enter number to block") }
            )
            Button(onClick = { viewModel.blockNumber(newNumber) }) {
                Text("Block")
            }
        }
    }
}

*/
