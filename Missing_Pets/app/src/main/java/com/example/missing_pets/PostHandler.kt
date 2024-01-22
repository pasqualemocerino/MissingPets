package com.example.missing_pets

import android.util.Log
import androidx.lifecycle.ViewModel
//import com.example.laboratory1.Model.DataModel

data class Post(
    var post_id: Int,
    var user_id: Int,
    var pet_type: Int,
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
                postsList.add(Post(post[0].asInt, post[1].asInt, post[2].asInt, post[3].asString, post[4].asString, post[5].asString))
            }
        } catch (e: Exception) {
            // handle exception
            Log.d("ERRORE SERVER", ":(")
            e.printStackTrace()
        }
        return postsList
    }

    suspend fun createPost(user_id:Int, pet_type:Int, date:String, position:String, description:String): Int {
        var newPost = Post(0, user_id, pet_type, date, position, description)
        var res = -1

        try {
            // send POST request
            res = retrofit.postsPost(newPost).toInt()
        } catch (e: Exception) {
            // handle exception
            Log.d("ERRORE SERVER", ":(")
            e.printStackTrace()
        }
        return res
    }


    /*
    fun createPost(user_id:Int, pet_type:Int, date:String, position:String, description:String): Int {
        var newPost = Post(0, user_id, pet_type, date, position, description)
        var res = -1

        GlobalScope.launch(Dispatchers.IO) {
            try {
                // send POST request
                res = retrofit.postsPost(newPost).toInt()
            } catch (e: Exception) {
                // handle exception
                Log.d("ERRORE SERVER", ":(")
                e.printStackTrace()
            }
        }
        return res
    }
    */
}
