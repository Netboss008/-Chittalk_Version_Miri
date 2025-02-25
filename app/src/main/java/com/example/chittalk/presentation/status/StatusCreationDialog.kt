package com.example.chittalk.presentation.status

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.chittalk.data.status.StatusType
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StatusCreationDialog(
    onDismiss: () -> Unit,
    onStatusCreated: (Uri, StatusType, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf<StatusType?>(null) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(Color.Blue) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            selectedType = StatusType.IMAGE
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            selectedType = StatusType.VIDEO
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Kopfzeile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Schließen")
                    }
                    Text(
                        text = "Status erstellen",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Button(
                        onClick = {
                            selectedUri?.let { uri ->
                                selectedType?.let { type ->
                                    onStatusCreated(uri, type, caption.takeIf { it.isNotEmpty() })
                                }
                            }
                        },
                        enabled = selectedUri != null || (selectedType == StatusType.TEXT && caption.isNotEmpty())
                    ) {
                        Text("Teilen")
                    }
                }

                // Typ-Auswahl
                if (selectedType == null) {
                    StatusTypeSelection(
                        onImageSelected = { imagePickerLauncher.launch("image/*") },
                        onVideoSelected = { videoPickerLauncher.launch("video/*") },
                        onTextSelected = { 
                            selectedType = StatusType.TEXT
                            showColorPicker = true
                        }
                    )
                }

                // Vorschau und Bearbeitung
                when (selectedType) {
                    StatusType.IMAGE -> {
                        selectedUri?.let { uri ->
                            ImagePreview(
                                uri = uri,
                                onRemove = {
                                    selectedUri = null
                                    selectedType = null
                                }
                            )
                        }
                    }
                    StatusType.VIDEO -> {
                        selectedUri?.let { uri ->
                            VideoPreview(
                                uri = uri,
                                onRemove = {
                                    selectedUri = null
                                    selectedType = null
                                }
                            )
                        }
                    }
                    StatusType.TEXT -> {
                        TextStatusEditor(
                            caption = caption,
                            onCaptionChange = { caption = it },
                            backgroundColor = selectedColor,
                            onDone = { keyboardController?.hide() }
                        )
                    }
                    null -> { /* Nichts anzeigen */ }
                }

                // Beschriftungseingabe
                if (selectedType != StatusType.TEXT && selectedUri != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Beschreibung hinzufügen...") },
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        )
                    )
                }
            }
        }

        // Farbauswahl-Dialog
        if (showColorPicker) {
            ColorPickerDialog(
                onColorSelected = { 
                    selectedColor = it
                    showColorPicker = false
                },
                onDismiss = { showColorPicker = false }
            )
        }
    }
}

@Composable
fun StatusTypeSelection(
    onImageSelected: () -> Unit,
    onVideoSelected: () -> Unit,
    onTextSelected: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Wähle einen Status-Typ",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusTypeButton(
                icon = Icons.Default.Image,
                label = "Bild",
                onClick = onImageSelected
            )
            StatusTypeButton(
                icon = Icons.Default.VideoLibrary,
                label = "Video",
                onClick = onVideoSelected
            )
            StatusTypeButton(
                icon = Icons.Default.TextFields,
                label = "Text",
                onClick = onTextSelected
            )
        }
    }
}

@Composable
fun StatusTypeButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label)
    }
}

@Composable
fun ImagePreview(
    uri: Uri,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f/16f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Bildvorschau",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Entfernen",
                tint = Color.White
            )
        }
    }
}

@Composable
fun VideoPreview(
    uri: Uri,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f/16f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Hier könnte eine Video-Vorschau implementiert werden
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Icon(
                Icons.Default.PlayCircle,
                contentDescription = "Video abspielen",
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center),
                tint = Color.White
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Entfernen",
                tint = Color.White
            )
        }
    }
}

@Composable
fun TextStatusEditor(
    caption: String,
    onCaptionChange: (String) -> Unit,
    backgroundColor: Color,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f/16f)
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        TextField(
            value = caption,
            onValueChange = onCaptionChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(0.dp, Color.Transparent),
            textStyle = MaterialTheme.typography.headlineMedium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Text(
                    "Tippe deinen Text...",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() })
        )
    }
}

@Composable
fun ColorPickerDialog(
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        Color.Blue,
        Color.Red,
        Color.Green,
        Color.Yellow,
        Color.Magenta,
        Color.Cyan,
        Color.DarkGray,
        Color.Black
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hintergrundfarbe wählen") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.padding(8.dp)
            ) {
                items(colors) { color ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(48.dp)
                            .background(color, CircleShape)
                            .clickable { onColorSelected(color) }
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}