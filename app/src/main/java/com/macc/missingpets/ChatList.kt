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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun formatTime(timestamp: Date): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(timestamp)
}

// On a separate thread, to filter chats relative to the user from the entire set of chats
fun getUserChatList(auth: AuthViewModel, chatList: List<Chat>): MutableList<Chat> {
    val firebaseUser = auth.currentUser()
    val userId = firebaseUser?.uid

    val userChatList = mutableListOf<Chat>()

    // Scan all messages ordered by timestamps in descending order
    for (chat in chatList) {
        // Select only the chats relative to the user (as sender or receiver)
        if (chat.lastSenderId == userId ||
            chat.lastReceiverId == userId) {
            userChatList.add(chat)
        }
    }
    return userChatList
}

fun readMessages(senderId: String, receiverId: String, completeMessageList: MutableList<ChatMessage>) {
    // For all unread messages such directed to me, change to read
    for (i in completeMessageList.indices) {
        val message = completeMessageList[i]
        if (message.senderId == senderId && message.receiverId == receiverId && message.unread) {
            completeMessageList[i].unread = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(auth: AuthViewModel, completeMessageList: MutableList<ChatMessage>, completeChatList:MutableList<Chat>, navController: NavController) {
    val userChatList = getUserChatList(auth, completeChatList)
    Column {
        TopAppBar(
            title = {
                Text(text = "Chats")
            },
            navigationIcon = {
                // Add back icon with navigation action
                IconButton(
                    onClick = { navController.navigate(Routes.HOME) },
                    content = {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                )
            }
        )
        ChatList(auth = auth, chats = userChatList) { chatNameId, chatName ->
            // Read all unread messages on chat opening
            val user = auth.currentUser()?.uid
            if (user != null) {
                readMessages(chatNameId, user, completeMessageList)
                // Redirect to a specific chat on click
                navController.navigate(Routes.CHAT + "/$chatNameId" + "/$chatName")
            }
            else Log.e("ChatsScreen", "user is null")
        }
    }

}

@Composable
fun ChatList(auth: AuthViewModel, chats: List<Chat>, onItemClick: (String, String) -> Unit) {
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
            ChatItem(auth = auth, chat = chat, onItemClick = onItemClick)
        }
    }
}

@Composable
fun ChatItem(auth:AuthViewModel, chat: Chat, onItemClick: (String, String) -> Unit) {
    val userId = auth.currentUser()?.uid

    val chatNameId = if (userId != chat.lastSenderId) { chat.lastSenderId }
    else { chat.lastReceiverId }

    val chatName = if (userId != chat.lastSenderId) { chat.lastSenderUsername }
    else { chat.lastReceiverUsername }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(chatNameId, chatName) }
            .padding(16.dp)
    ) {
        // Profile Image
        Image(
            painter = ColorPainter(Color.Black), // Image resource
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.width(16.dp))
        // Left side (username and last message)
        Column {
            Text(text = chatName, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            val prefix = if (chat.lastSenderId != userId) { chat.lastSenderUsername + ": " }
            else { "" }
            Text(text = prefix + chat.lastMessage, color = MaterialTheme.colorScheme.onSurface.copy())
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
            if (userId == chat.lastSenderId || !chat.unread) {
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