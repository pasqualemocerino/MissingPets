package com.example.missingpets

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID


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

    private lateinit var postsList: ArrayList<Post>     // Lista dei post da mostrare nella home
    private var matchingPostsList = ArrayList<Post>()     // Lista dei post da ritornare come risultato di un matching

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


    suspend fun createPost(user_id:Int, petName:String, pet_type:String, date:String, position:String, description:String, photoPath:String): String {
        var res = "ok"

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
        } catch (e: Exception) {
            // handle exception
            Log.d("ERRORE INVIO SERVER", "mo mi sparo")
            e.printStackTrace()
            res = e.message!!
        }
        return res
    }


    fun getLastMatchingResult(): ArrayList<Post> {
        return matchingPostsList
    }

    suspend fun getBestMatchingPosts(user_id:Int, date:String, position:String, photoBitmap: Bitmap, context:Context): Int {

        Log.d("SCAN", "requesting matching posts from server")

        // Svuota la lista
        matchingPostsList = ArrayList<Post>()

        // Prepara campi per l'invio
        val user_idToSend = RequestBody.create("text/plain".toMediaTypeOrNull(), user_id.toString())
        val dateToSend = RequestBody.create("text/plain".toMediaTypeOrNull(), date.toString())
        val positionToSend = RequestBody.create("text/plain".toMediaTypeOrNull(), position.toString())

        // Prepara foto per l'invio
        val file = convertBitmapToFile(photoBitmap, context)
        val requestFile = RequestBody.create(MultipartBody.FORM, file)
        val photoToSend = MultipartBody.Part.createFormData("photo", file.name, requestFile)

        try {
            val json = retrofit.matchPost(user_idToSend, dateToSend, positionToSend, photoToSend)

            // itero su tutti i post
            for (obj in json) {
                val post = obj.asJsonArray
                matchingPostsList.add(Post(post[0].asInt, post[1].asInt, post[2].asString, post[3].asString, post[4].asString, post[5].asString, post[6].asString, post[7].asString))
            }
        } catch (e: Exception) {
            // handle exception
            Log.d("ERRORE SCAN SERVER", ":(")
            e.printStackTrace()
            return -1
        }
        return 0
    }


    // Perche' l'activity Scan ottiene un'immagine Bitmap, ma ci serve un oggetto File da mandare al server
    private fun convertBitmapToFile(imageBitmap: Bitmap, context: Context): File {
        //val wrapper = ContextWrapper(requireContext())
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")
        val stream: OutputStream = FileOutputStream(file)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG,25,stream)
        stream.flush()
        stream.close()
        return file
    }
}
