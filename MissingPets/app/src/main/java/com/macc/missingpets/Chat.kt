package com.macc.missingpets

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class Chat(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val timestamp: Long,
    val unread: Boolean
    //val unreadCount: Int
)

val chatList = listOf(
    Chat(1, "John", "Hello!", System.currentTimeMillis(), false),
    Chat(2, "Alice", "Hi there!", System.currentTimeMillis(), false)
)

@Composable
fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Chat list functions
@Composable
fun ChatsScreen(auth: AuthViewModel, completeChatMessageList: List<ChatMessage>, navController: NavController) {
    val chatList = getChatList(auth, completeChatMessageList)
    ChatList(chats = chatList, onItemClick = {/* Redirection to a specific chat */ } )
}

@Composable
fun ChatList(chats: List<Chat>, onItemClick: (Chat) -> Unit) {
    LazyColumn {
        itemsIndexed(chats) { index, chat ->
            if (index > 0) {
                // Add a separator line if not the first item
                Divider(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
            }
            ChatItem(chat = chat, onItemClick = onItemClick)
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onItemClick: (Chat) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(chat) }
            .padding(16.dp)
    ) {
        // Profile Image
        Image(
            painter = ColorPainter(Color.Gray), // Image resource
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.width(16.dp))
        // Left side (username and last message)
        Column {
            Text(text = chat.name, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = chat.lastMessage, color = MaterialTheme.colorScheme.onSurface.copy())
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Right side (possible unread messages and timestamp)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.End
        ) {
            if (!chat.unread) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Icon(imageVector = Icons.Default.MailOutline, contentDescription = null)
                    Text(
                        text = formatTime(chat.timestamp),
                        color = MaterialTheme.colorScheme.onSurface.copy()
                    )
                }
            } else {
                // Add unread message icon and highlight icon and time
                Text(text = "Unread messages")

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Icon(imageVector = Icons.Default.MailOutline, contentDescription = null)
                    Text(
                        text = formatTime(chat.timestamp),
                        color = MaterialTheme.colorScheme.onSurface.copy()
                    )
                }
            }
        }
    }
}

// Single chat messages

data class ChatMessage(
    val id: Int,
    val sender: String,
    val receiver: String,
    val message: String,
    val timestamp: Long,
    val unread: Boolean
)

val chatMessageList = listOf(
    ChatMessage(0, "user0", "user1", "Hello", System.currentTimeMillis(), false),
    ChatMessage(1, "user1", "user0", "Hello to you", System.currentTimeMillis() + 1, true)
)

// On a separate thread, to get chat list to display from the entire set of messages
fun getChatList(auth: AuthViewModel, chatMessageList: List<ChatMessage>): List<Chat> {
    val firebaseUser = auth.currentUser()

    if (firebaseUser != null) {
        val user = firebaseUser.displayName
        val chatList = mutableListOf<Chat>()

        val senders = mutableListOf<String>()
        // Scan all messages ordered by timestamps in descending order
        for (chatMessage in chatMessageList) {
            // Select only last messages from each sender with user as receiver
            if (user != null &&
                chatMessage.receiver == user &&
                chatMessage.sender !in senders) {
                chatList.add(Chat(senders.size, chatMessage.sender, chatMessage.message, chatMessage.timestamp, chatMessage.unread))
                senders.add(chatMessage.sender)
            }
        }

        return chatList
    }
    else {
        Log.e("getChatList", " null Firebase user")
        return emptyList()
    }
}

// On a separate thread, to get chat messages list to display from the entire set of messages
fun getChatMessageList(auth: AuthViewModel, chatName: String, completeChatMessageList: List<ChatMessage>): List<ChatMessage> {
    val firebaseUser = auth.currentUser()

    if (firebaseUser != null) {
        val user = firebaseUser.displayName
        val chatMessageList = mutableListOf<ChatMessage>()

        // Scan all messages ordered by timestamps in descending order
        for (chatMessage in completeChatMessageList) {
            // Select only messages between user and chatName user
            if (user != null && (
                        (chatMessage.receiver == user && chatMessage.sender == chatName) ||
                        (chatMessage.receiver == chatName && chatMessage.sender == user)
                    )
                ) {
                    chatMessageList.add(chatMessage)
            }
        }

        return chatMessageList
    }
    else {
        Log.e("getChatMessageList", " null Firebase user")
        return emptyList()
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(16.dp))
                .padding(12.dp)
                .clip(MaterialTheme.shapes.medium)
        ) {
            Text(text = message.sender, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = message.message)
        }
    }
}

@Composable
fun ChatTextInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    var isKeyboardVisible = remember { mutableStateOf(false) }

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

@Composable
fun ChatScreen(auth: AuthViewModel, chatName: String, completeChatMessageList: List<ChatMessage>, navController: NavController) {

    var newMessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    
    chatMessages = getChatMessageList(auth, chatName, completeChatMessageList)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(chatMessages) { message ->
                ChatMessageItem(message = message)
            }
        }

        // Text Input Bar
        ChatTextInputBar(
            value = newMessage,
            onValueChange = { newMessage = it },
            onSendClick = {
                if (newMessage.isNotBlank()) {
                    chatMessages += ChatMessage(
                        id = 0, // Compute the hash
                        sender = "Me",
                        receiver = chatName,
                        message = newMessage,
                        timestamp = System.currentTimeMillis(),
                        unread = true
                    )
                    newMessage = ""
                }
            }
        )
    }
}