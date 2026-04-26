package com.example.artisanx.presentation.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.artisanx.domain.model.ChatMessage
import com.example.artisanx.util.AppwriteFileUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val messages = viewModel.messages.value
    val messageInput = viewModel.messageInput.value
    val isSending = viewModel.isSending.value
    val isLoading = viewModel.isLoading.value
    val isImageUploading = viewModel.isImageUploading.value
    val currentUserId = viewModel.currentUserId.value
    val otherPartyName = viewModel.otherPartyName.value
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.sendImageMessage(it) } }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ChatViewModel.UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherPartyName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attach image button
                    IconButton(
                        onClick = { imagePicker.launch("image/*") },
                        enabled = !isImageUploading && !isSending
                    ) {
                        if (isImageUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = "Attach image",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = viewModel::onInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                        shape = RoundedCornerShape(24.dp),
                        supportingText = if (messageInput.length > 1800) {
                            {
                                Text(
                                    "${messageInput.length}/2000",
                                    color = if (messageInput.length >= 2000)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.outline
                                )
                            }
                        } else null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledIconButton(
                        onClick = { viewModel.sendMessage() },
                        enabled = messageInput.isNotBlank() && !isSending && !isImageUploading
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (messages.isEmpty()) {
                Text(
                    text = "No messages yet. Say hello!",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(messages) { msg ->
                        MessageBubble(message = msg, isOwn = msg.senderId == currentUserId)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isOwn: Boolean) {
    val alignment = if (isOwn) Alignment.End else Alignment.Start
    val bubbleColor = if (isOwn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOwn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (isOwn) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (message.imageFileId.isNotBlank()) {
            AsyncImage(
                model = AppwriteFileUtils.fileViewUrl(message.imageFileId),
                contentDescription = "Chat image",
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .clip(shape),
                contentScale = ContentScale.FillWidth
            )
        } else {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(color = bubbleColor, shape = shape)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = message.message, color = textColor, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text(
            text = formatChatTimestamp(message.createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

private fun formatChatTimestamp(isoTimestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        val date = sdf.parse(isoTimestamp) ?: return isoTimestamp
        val saZone = TimeZone.getTimeZone("Africa/Johannesburg")
        val now = Calendar.getInstance(saZone)
        val msgCal = Calendar.getInstance(saZone).apply { time = date }

        val isSameDay = now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = saZone }.format(date)
        } else {
            SimpleDateFormat("MMM d, HH:mm", Locale.US).apply { timeZone = saZone }.format(date)
        }
    } catch (e: Exception) {
        isoTimestamp.take(16).replace("T", " ")
    }
}
