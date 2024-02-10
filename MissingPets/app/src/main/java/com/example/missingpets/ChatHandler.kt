package com.example.missingpets

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.google.gson.Gson
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class ChatMessage(
    val id: Int,
    val senderId: String,
    val senderUsername: String,
    val receiverId: String,
    val receiverUsername: String,
    val message: String,
    val timestamp: String
)

// List of all chats, unique entry for a specific pair of users
data class Chat(
    val id: Int,
    var lastSenderId: String,
    var lastSenderUsername: String,
    var lastReceiverId: String,
    var lastReceiverUsername: String,
    var lastMessage: String,
    var timestamp: String,
    var unread: Boolean
)


// Singleton object
object ChatHandler : ViewModel() {

    private lateinit var messageList:ArrayList<ChatMessage>
    private lateinit var chatList:ArrayList<Chat>
    private var retrofit = ServerAPI.HelperClass.getInstance()

    // Latest date of server request
    private lateinit var lastServerRequestDate : LocalDateTime

    // Update rate (seconds between updates)
    private const val secondsBetweenUpdates = 1


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getMessageList(userId: String, chatNameId: String): ArrayList<ChatMessage> {
        if (this::messageList.isInitialized && this::lastServerRequestDate.isInitialized) {

            // Compute the seconds elapsed until last request
            val seconds = lastServerRequestDate.until( LocalDateTime.now(), ChronoUnit.SECONDS )

            // Check if more seconds than update rate elapsed
            return if (seconds >= secondsBetweenUpdates) {
                getMessageListFromServer(userId, chatNameId)
            }
            // Otherwise return the previously computed list
            else {
                messageList
            }
        }
        else {
            return getMessageListFromServer(userId, chatNameId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getChatList(userId: String): ArrayList<Chat> {
        if (this::chatList.isInitialized && this::lastServerRequestDate.isInitialized) {

            // Compute the seconds elapsed until last request
            val seconds = lastServerRequestDate.until( LocalDateTime.now(), ChronoUnit.SECONDS )

            // Check if more seconds than update rate elapsed
            return if (seconds >= secondsBetweenUpdates) {
                getChatListFromServer(userId)
            }
            // Otherwise return the previously computed list
            else {
                chatList
            }
        }
        else {
            return getChatListFromServer(userId)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getMessageListFromServer(userId: String, chatNameId: String): ArrayList<ChatMessage> {

        Log.d("getMessageListFromServer", "Requesting messages from server")

        // Record the date of last server request
        lastServerRequestDate = LocalDateTime.now()

        // Initialize the list
        messageList = ArrayList<ChatMessage>()

        try {
            val json = retrofit.messagesGet(userId, chatNameId)

            // Iteration on all messages
            for (obj in json) {
                val message = obj.asJsonArray
                messageList.add(ChatMessage(message[0].asInt, message[1].asString, message[2].asString, message[3].asString, message[4].asString, message[5].asString, message[6].asString))
            }
        } catch (e: Exception) {
            // Handle exception
            Log.d("getMessageListFromServer", "SERVER ERROR")
            e.printStackTrace()
        }
        return messageList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getChatListFromServer(userId: String): ArrayList<Chat> {

        Log.d("getChatListFromServer", "Requesting chats from server")

        // Record the date of last server request
        lastServerRequestDate = LocalDateTime.now()

        // Initialize the list
        chatList = ArrayList<Chat>()

        try {
            val json = retrofit.chatsGet(userId)

            // Iteration on all chats
            for (obj in json) {
                val chat = obj.asJsonArray
                chatList.add(Chat(chat[0].asInt, chat[1].asString, chat[2].asString, chat[3].asString, chat[4].asString, chat[5].asString, chat[6].asString, chat[7].asBoolean))
            }
        } catch (e: Exception) {
            // Handle exception
            Log.d("getChatListFromServer", "SERVER ERROR")
            e.printStackTrace()
        }
        return chatList
    }

    suspend fun createOrUpdateChat(newChat: Chat): Int {
        var ret = -1

        // Create a chat to send
        val chatToSend =
            Gson().toJson(newChat).toRequestBody("application/json".toMediaTypeOrNull())

        try {
            // Send PUT request
            val serverAnswer = retrofit.chatsPut(chatToSend)
            Log.d("createOrUpdateChat answer", serverAnswer)
            ret = serverAnswer.toInt()
        } catch (e: Exception) {
            // Handle exception
            Log.e("createOrUpdateChat", "SERVER PUT ERROR: ${e.message}")
            e.printStackTrace()
        }
        return ret
    }

    suspend fun createMessage(newMessage: ChatMessage): Int {
        var ret = -1

        // Create a message to send
        val messageToSend =
            Gson().toJson(newMessage).toRequestBody("application/json".toMediaTypeOrNull())

        try {
            // Send POST request
            val serverAnswer = retrofit.messagesPost(messageToSend)
            Log.d("createMessage answer", serverAnswer)
            ret = serverAnswer.toInt()
        } catch (e: Exception) {
            // Handle exception
            Log.e("createMessage", "Exception during server request", e)
        }

        return ret
    }

}