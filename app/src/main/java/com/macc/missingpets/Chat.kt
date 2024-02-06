package com.macc.missingpets

import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Date
import java.util.Locale


/*
CoroutineScope(Dispatchers.IO).launch {
                        runBlocking {
                            val res = PostsHandler.createPost(user_id, petName, pet_type, date, position, description, getPath(photoURI))
                            Log.d("Server response", res.toString())
                            // TODO: gestire errore server in base al valore di res
                        }
                        finish()
                    }
 */
// On a separate thread, to get chat messages list to display from the entire set of messages
fun getChatMessageList(auth: AuthViewModel, chatNameId: String, completeChatMessageList: List<ChatMessage>): List<ChatMessage> {
    val userId = auth.currentUser()?.uid
    val chatMessageList = mutableListOf<ChatMessage>()

    // Scan all messages ordered by timestamps in descending order
    for (chatMessage in completeChatMessageList) {
        // Select only messages between user and chatName user
        if (userId != null && (
                    (chatMessage.receiverId == userId && chatMessage.senderId == chatNameId) ||
                    (chatMessage.receiverId == chatNameId && chatMessage.senderId == userId)
                )
            ) {
                chatMessageList.add(chatMessage)
        }
    }

    return chatMessageList

}

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

fun updateChat(chatMessage: ChatMessage, chatList: MutableList<Chat>) {
    for (i in chatList.indices) {
        val chat = chatList[i]
        var updateFields = false
        if (chat.lastSenderId == chatMessage.senderId && chat.lastReceiverId == chatMessage.receiverId) { updateFields = true }
        if (chat.lastSenderId == chatMessage.receiverId && chat.lastReceiverId == chatMessage.senderId) {
            val newReceiverId = chatList[i].lastSenderId
            val newReceiverUsername = chatList[i].lastSenderUsername
            chatList[i].lastSenderId = chatList[i].lastReceiverId
            chatList[i].lastSenderUsername = chatList[i].lastReceiverUsername
            chatList[i].lastReceiverId = newReceiverId
            chatList[i].lastReceiverUsername = newReceiverUsername
            updateFields = true
        }
        if (updateFields) {
            chatList[i].lastMessage = chatMessage.message
            chatList[i].timestamp = chatMessage.timestamp
            chatList[i].unread = chatMessage.unread
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(auth: AuthViewModel, chatNameId: String, chatName: String, completeChatMessageList: MutableList<ChatMessage>, chatList: MutableList<Chat>, navController: NavController) {
    val firebaseUser = auth.currentUser()
    val senderId = firebaseUser?.uid
    if (senderId == null) {
        Log.e("ChatScreen", "senderId is null")
        return
    }
    val senderUsername = firebaseUser.displayName
    if (senderUsername == null) {
        Log.e("ChatScreen", "senderUsername is null")
        return
    }

    var newMessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    chatMessages = getChatMessageList(auth, chatNameId, completeChatMessageList)

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
                    val chatMessage = ChatMessage(
                        id = 0, // Locale.getDefault().toString()), // Timestamp-based id
                        senderId = senderId,
                        senderUsername = senderUsername,
                        receiverId = chatNameId,
                        receiverUsername = chatName,
                        message = newMessage,
                        timestamp = Date(System.currentTimeMillis()),
                        unread = true
                    )
                    //chatMessages += chatMessage
                    completeChatMessageList += chatMessage
                    // Chat update
                    updateChat(chatMessage, chatList)
                    newMessage = ""
                }
            }
        )
    }
}