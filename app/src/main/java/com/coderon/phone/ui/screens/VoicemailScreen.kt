package com.coderon.phone.ui.screens

/*

@Composable
fun VoicemailScreen(viewModel: VoicemailViewModel) {
    val voicemails by viewModel.voicemails.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Voicemails", style = MaterialTheme.typography.headlineMedium)

        LazyColumn {
            items(voicemails) { voicemail ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("From: ${voicemail.callerNumber}")
                    IconButton(onClick = { viewModel.deleteVoicemail(voicemail) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    IconButton(onClick = { playVoicemail(voicemail.filePath) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                }
            }
        }
    }
}

fun playVoicemail(filePath: String) {
    val mediaPlayer = MediaPlayer().apply {
        setDataSource(filePath)
        prepare()
        start()
    }
}
*/
