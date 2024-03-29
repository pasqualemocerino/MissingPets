package com.macc.missingpets

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Date

@Composable
fun ChatMessageItem(message: ChatMessage, sending: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = if (sending) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp))
                .padding(12.dp)
                .clip(MaterialTheme.shapes.medium)
        ) {
            //Text(text = message.senderUsername, fontWeight = FontWeight.Bold)
            //Spacer(modifier = Modifier.height(4.dp))
            Text(text = message.message,
                color = Color.White
            )
        }
    }
}

@Composable
fun ChatTextInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
                //.clip(RoundedCornerShape(32.dp)),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSendClick()
                }
            ),
            placeholder = {
                Text(text = "Type a message...")
            }
        )

        IconButton(
            onClick = {
                onSendClick()
            },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send"
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(auth: AuthViewModel, chatId: Int, chatNameId: String, chatName: String, navController: NavController) {
    val senderId = auth.userId()
    val senderUsername = auth.currentUser()?.displayName.toString()

    val context = LocalContext.current
    var newMessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }


    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
             CoroutineScope(Dispatchers.IO).launch {
                runBlocking {
                    chatMessages = ChatHandler.getMessageList(senderId, chatNameId)
                    Log.d("Server response", "getMessageList() executed")
                    Log.d("Messages downloaded", chatMessages.toString())
                }
                Log.d("DONE", "Messages fetched from server")
            }
        }
    }

    // chatMessages = getChatMessageList(auth, chatNameId, completeChatMessageList)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = {
                Text(text = chatName)
            },
            modifier = Modifier.background(MaterialTheme.colorScheme.primary),
            navigationIcon = {
                // Add back icon with navigation action
                IconButton(
                    onClick = { navController.navigate(Routes.CHATS) },
                    content = {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                )
            }
        )
        // Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(chatMessages) { message ->
                if (senderId == message.senderId) ChatMessageItem(message = message, sending = true)
                else ChatMessageItem(message = message, sending = false)
            }
        }

        // Text Input Bar
        ChatTextInputBar(
            value = newMessage,
            onValueChange = { newMessage = it },
            onSendClick = {
                if (newMessage.isNotBlank()) {
                    val timestamp = formatDateTime(Date(System.currentTimeMillis()))
                    val chatMessage = ChatMessage(
                        id = 0,         // Set by server
                        senderId = senderId,
                        senderUsername = senderUsername,
                        receiverId = chatNameId,
                        receiverUsername = chatName,
                        message = newMessage,
                        timestamp = timestamp //"2023-06-15"
                    )

                    // For a rapid visualization, to remove
                    //chatMessages += chatMessage

                    val chat = Chat(
                        id = chatId,
                        lastSenderId = senderId,
                        lastSenderUsername = senderUsername,
                        lastReceiverId = chatNameId,
                        lastReceiverUsername = chatName,
                        lastMessage = newMessage,
                        timestamp = timestamp,
                        unread = true
                    )

                    // Add message on server completeChatMessageList += chatMessage
                    //CoroutineScope(Dispatchers.IO).launch {
                    runBlocking {
                        try {
                            val res = ChatHandler.createMessage(chatMessage)
                            Log.d("Server response", res.toString())
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "CREATE MESSAGE ERROR, ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.e("ChatScreen", "Error sending message: ${e.message}")
                        }

                        // Chat creation or update
                        try {
                            val res = ChatHandler.createOrUpdateChat(chat)
                            Log.d("Server response", res.toString())
                        } catch (e: Exception) {
                            Log.e("ChatScreen", "Error creating or updating chat: ${e.message}")
                        }
                    }
                    // }

                    newMessage = ""
                }
            }
        )
    }
}