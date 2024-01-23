package com.example.missing_pets

import android.media.Image
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.*
import java.io.File


//import com.example.laboratory1.Model.DataModel

data class Post(
    var post_id: Int,
    var user_id: Int,
    var pet_type: String,
    var date: String,
    var position: String,
    var description: String,
)
// var str = "user_id=$user_id&pet_type=$pet_type&date=$date&position=$position&description=$description"

// Lista di post
class PostsHandler : ViewModel() {

    private lateinit var postsList : ArrayList<Post>
    private var retrofit = ServerAPI.HelperClass.getInstance()

    suspend fun getAll(): ArrayList<Post> {
        postsList = ArrayList<Post>()

        try {
            val json = retrofit.postsGet()

            // itero su tutti i post
            for (obj in json) {
                val post = obj.asJsonArray
                postsList.add(Post(post[0].asInt, post[1].asInt, post[2].asString, post[3].asString, post[4].asString, post[5].asString))
            }
        } catch (e: Exception) {
            // handle exception
            Log.d("ERRORE SERVER", ":(")
            e.printStackTrace()
        }
        return postsList
    }


    suspend fun createPost(user_id:Int, pet_type:String, date:String, position:String, description:String, photoPath:String): Int {
        var res = -1

        // Prepara post per l'invio
        var newPost = Post(0, user_id, pet_type, date, position, description)
        //val postToSend = RequestBody.create(MediaType.parse("application/json"), Gson().toJson(newPost))
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


    /*
    suspend fun getPhoto(post_id: Int) : Image? {
        try {
            // send POST request
            val image = retrofit.photoGet(post_id)
            return image
        } catch (e: Exception) {
            // handle exception
            Log.d("ERRORE RICEZIONE FOTO", "sigh")
            e.printStackTrace()
        }
        return null
    }
    */

}
