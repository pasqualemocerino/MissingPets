package com.macc.missingpets
/*
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.missingpets.ServerAPI
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.*
import java.io.File
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
*/
import java.util.Date

data class ChatMessage(
    val id: Int,
    val senderId: String,
    val senderUsername: String,
    val receiverId: String,
    val receiverUsername: String,
    val message: String,
    val timestamp: Date,
    var unread: Boolean
)

// List of all chats, unique entry for a specific pair of users
data class Chat(
    val id: Int,
    var lastSenderId: String,
    var lastSenderUsername: String,
    var lastReceiverId: String,
    var lastReceiverUsername: String,
    var lastMessage: String,
    var timestamp: Date,
    var unread: Boolean
)


/*
// Singleton object
object ChatHandler : ViewModel() {

    /*
    private var chatList:MutableList<Chat> = mutableListOf(
        Chat("0", "ux0P38UTrdNgy6usK2OEZbrG7x32", "Chiara", "4qQUbahchPT9oqT0VY1PRvvwSu92", "Pasquale","Ciao", System.currentTimeMillis() + 1, false)
    )

    private var messageList:MutableList<ChatMessage> = mutableListOf(
        ChatMessage("0", "4qQUbahchPT9oqT0VY1PRvvwSu92", "Pasquale", "ux0P38UTrdNgy6usK2OEZbrG7x32", "Chiara", "Ciao Chiara", System.currentTimeMillis(), false),
        ChatMessage("1", "ux0P38UTrdNgy6usK2OEZbrG7x32", "Chiara", "4qQUbahchPT9oqT0VY1PRvvwSu92", "Pasquale", "Ciao", System.currentTimeMillis() + 1, false)
    )
    */

    private lateinit var  messageList:ArrayList<ChatMessage>
    private lateinit var  chatList:ArrayList<Chat>
    private var retrofit = ServerAPI.HelperClass.getInstance()

    // Latest date of server request
    private lateinit var lastServerRequestDate : LocalDateTime

    // Update rate (seconds between updates)
    private val secondsBetweenUpdates = 5


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getMessageList(): ArrayList<ChatMessage> {
        if (this::messageList.isInitialized && this::lastServerRequestDate.isInitialized) {

            // Compute the seconds elapsed until last request
            val seconds = lastServerRequestDate.until( LocalDateTime.now(), ChronoUnit.SECONDS )

            // Check if more seconds than update rate elapsed
            if (seconds >= secondsBetweenUpdates) {
                return getMessageListFromServer()
            }
            // Otherwise return the previously computed list
            else {
                return messageList
            }
        }
        else {
            return getMessageListFromServer()
        }
    }

    suspend fun getChatList(): ArrayList<Chat> {
        if (this::chatList.isInitialized && this::lastServerRequestDate.isInitialized) {

            // Compute the seconds elapsed until last request
            val seconds = lastServerRequestDate.until( LocalDateTime.now(), ChronoUnit.SECONDS )

            // Check if more seconds than update rate elapsed
            if (seconds >= secondsBetweenUpdates) {
                return getChatListFromServer()
            }
            // Otherwise return the previously computed list
            else {
                return chatList
            }
        }
        else {
            return getChatListFromServer()
        }
    }


    suspend fun getMessageListFromServer(): ArrayList<ChatMessage> {

        Log.d("POST", "Requesting messages from server")

        // Record the date of last server request
        lastServerRequestDate = LocalDateTime.now()

        // Initialize the list
        messageList = ArrayList<ChatMessage>()

        try {
            val json = retrofit.messagesGet()

            // Iteration on all messages
            for (obj in json) {
                val message = obj.asJsonArray
                messageList.add(ChatMessage(message[0].asInt, message[1].asString, message[2].asString, message[3].asString, message[4].asString, message[5].asString, message[6].asString, message[7].asBoolean))
            }
        } catch (e: Exception) {
            // Handle exception
            Log.d("getMessageListFromServer", "SERVER ERROR")
            e.printStackTrace()
        }
        return messageList
    }

    suspend fun getChatListFromServer(): ArrayList<Chat> {

        Log.d("POST", "Requesting chats from server")

        // Record the date of last server request
        lastServerRequestDate = LocalDateTime.now()

        // Initialize the list
        chatList = ArrayList<Chat>()

        try {
            val json = retrofit.chatsGet()

            // Iteration on all chats
            for (obj in json) {
                val chat = obj.asJsonArray
                messageList.add(ChatMessage(chat[0].asInt, chat[1].asString, chat[2].asString, chat[3].asString, chat[4].asString, chat[5].asString, chat[6].asString, chat[7].asBoolean))
            }
        } catch (e: Exception) {
            // Handle exception
            Log.d("getChatListFromServer", "SERVER ERROR")
            e.printStackTrace()
        }
        return chatList
    }

    // When creating a message, it is needed to update also the chat list
    suspend fun createMessage(senderId: String, senderUsername: String, receiverId: String, receiverUsername: String, message: String, timestamp: Date, unread: Boolean): Int {
        var ret = -1

        // Create a message to send (postId will be correctly set by the server)
        val newMessage = ChatMessage(0, senderId, senderUsername, receiverId, receiverUsername, message, timestamp, unread)
        val messageToSend = RequestBody.create("application/json".toMediaTypeOrNull(), Gson().toJson(newMessage))

        try {
            // Send POST request
            val serverAnswer = retrofit.messagesPost(messageToSend)
            Log.d("createMessage answer", serverAnswer)
            ret = serverAnswer.toInt()
        } catch (e: Exception) {
            // Handle exception
            Log.d("createMessage", "SERVER POST ERROR")
            e.printStackTrace()
        }
        return ret
    }

}
 */