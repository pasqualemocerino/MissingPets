package com.example.missingpets

import android.media.Image
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import java.util.Date


//import com.example.laboratory1.Model.DataModel

data class Post(
    var post_id: Int,
    var user_id: Int,
    var pet_name: String,
    var pet_type: String,
    var date: String,
    var position: String,
    var address: String,
    var description: String,
)

// Lista di post
object PostsHandler : ViewModel() {            // 'object' e' un Singleton

    private lateinit var postsList: ArrayList<Post>
    private var retrofit = ServerAPI.HelperClass.getInstance()

    // Salvati la data dell'ultima volta che hai preso i post dal server.
    // Cosi' se li hai presi troppo tempo fa, ri-prendili
    private lateinit var lastServerRequestDate : LocalDateTime

    // Ogni quanti minuti aggiornare la lista dei post
    private val minutesBetweenUpdates = 5


    suspend fun getPostsList(): ArrayList<Post> {
        if (this::postsList.isInitialized && this::lastServerRequestDate.isInitialized) {

            // Calcolati quanti minuti sono passati dall'ultima volta che hai preso i post dal server
            val minutes = lastServerRequestDate.until( LocalDateTime.now(), ChronoUnit.MINUTES )

            // Se sono passati piu' di minutesBetweenUpdates minuti, riprendi i dati dal server
            if (minutes >= minutesBetweenUpdates) {
                return getPostsListFromServer()
            }
            // Altrimenti restituisci la lista che gia' ti eri trovato prima
            else {
                return postsList
            }
        }
        else {
            return getPostsListFromServer()
        }
    }


    suspend fun getPostsListFromServer(): ArrayList<Post> {

        Log.d("POST", "requesting data from server")

        // Salvati la data attuale dentro lastServerRequestDate, per segnare
        // quand'e' l'ultima volta che hai preso i dati dal server
        lastServerRequestDate = LocalDateTime.now()

        // Inizializza (o svuota) la lista
        postsList = ArrayList<Post>()

        try {
            val json = retrofit.postsGet()

            // itero su tutti i post
            for (obj in json) {
                val post = obj.asJsonArray
                postsList.add(Post(post[0].asInt, post[1].asInt, post[2].asString, post[3].asString, post[4].asString, post[5].asString, post[6].asString, post[7].asString))
            }
        } catch (e: Exception) {
            // handle exception
            Log.d("ERRORE SERVER", ":(")
            e.printStackTrace()
        }
        return postsList
    }


    suspend fun createPost(user_id:Int, petName:String, pet_type:String, date:String, position:String, description:String, photoPath:String): Int {
        var res = -1

        // Prepara post per l'invio (post_id e address hanno valori qualunque tanto vengono impostati bene dal server)
        val newPost = Post(0, user_id, petName, pet_type, date, position, "", description)
        val postToSend = RequestBody.create("application/json".toMediaTypeOrNull(), Gson().toJson(newPost))

        // Prepara foto per l'invio
        val file = File(photoPath)
        val requestFile = RequestBody.create(MultipartBody.FORM, file)
        val photoToSend = MultipartBody.Part.createFormData("photo", file.name, requestFile)

        try {
            // send POST request
            val serverAnswer = retrofit.postsPost(postToSend, photoToSend)
            Log.d("RISPOSTA", serverAnswer)
            res = serverAnswer.toInt()
        } catch (e: Exception) {
            // handle exception
            Log.d("ERRORE INVIO SERVER", "mo mi sparo")
            e.printStackTrace()
        }
        return res
    }

}
