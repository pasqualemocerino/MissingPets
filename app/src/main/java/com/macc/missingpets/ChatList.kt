package com.macc.missingpets

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*fun formatTime(mysqlDateTime: String): String {
    // Define the MySQL datetime format
    val mysqlDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Parse the MySQL datetime string into a Date object
    var ret = ""
    val date = mysqlDateTimeFormat.parse(mysqlDateTime)
    if (date != null) ret = sdf.format(date)

    return ret
}
 */

// TODO: Date display format
fun formatTime(mysqlDateTime: String): String {
    return mysqlDateTime
}

fun formatDateTime(timestamp: Date): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(timestamp)
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(auth: AuthViewModel, navController: NavController) {
    // Select only the chats related to the user
    var userChatList by remember { mutableStateOf<List<Chat>>(emptyList()) }
    val userId = auth.currentUser()?.uid.toString()

    // Get all chats from the server
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            CoroutineScope(Dispatchers.IO).launch {
                runBlocking {
                    userChatList = ChatHandler.getChatList(userId)
                    Log.d("Server response", "getChatList() executed")
                }
                // loading.value = false
                Log.d("DONE", "Chats fetched from server")
            }
        }
    }

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
        ChatList(auth = auth, chats = userChatList) { chatId, chatNameId, chatName ->
            // Read all unread messages on chat opening
            val userId = auth.currentUser()?.uid.toString()

            // Register the reading of the chat on the server

            // Chat structure only needed to update the unread parameter of chatId with lastReceiverId equal to userId
            val chat = Chat(
                id = chatId,
                lastSenderId = "",
                lastSenderUsername = "",
                lastReceiverId = userId,
                lastReceiverUsername = "",
                lastMessage = "",
                timestamp = "",
                unread = false
            )

            runBlocking {
                try {
                    val res = ChatHandler.createOrUpdateChat(chat)
                    Log.d("Server response", res.toString())
                } catch (e: Exception) {
                    Log.e("ChatsScreen", "Error reading the chat: ${e.message}")
                }
            }

            // Redirect to a specific chat on click
            navController.navigate(Routes.CHAT + "/$chatId" + "/$chatNameId" + "/$chatName")
        }
    }

}

@Composable
fun ChatList(auth: AuthViewModel, chats: List<Chat>, onItemClick: (Int, String, String) -> Unit) {
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
fun ChatItem(auth:AuthViewModel, chat: Chat, onItemClick: (Int, String, String) -> Unit) {
    val userId = auth.currentUser()?.uid

    val chatId = chat.id
    val chatNameId = if (userId != chat.lastSenderId) { chat.lastSenderId }
    else { chat.lastReceiverId }

    val chatName = if (userId != chat.lastSenderId) { chat.lastSenderUsername }
    else { chat.lastReceiverUsername }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(chatId, chatNameId, chatName) }
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
                        text = "", // formatTime(chat.timestamp),
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
                        text = "", // formatTime(chat.timestamp),
                        color = MaterialTheme.colorScheme.onSurface.copy()
                    )
                }
            }
        }
    }
}