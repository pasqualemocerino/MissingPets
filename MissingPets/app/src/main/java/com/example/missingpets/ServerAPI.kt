package com.example.missingpets

import android.media.Image
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query


class ServerAPI {

    interface ServerAPI {
        @GET("/posts")
        suspend fun postsGet(): JsonArray

        @Multipart
        @POST("/posts")
        suspend fun postsPost(@Part("data") data: RequestBody, @Part image: MultipartBody.Part): String

        @Multipart
        @POST("/match")
        suspend fun matchPost(@Part("user_id") user_id: RequestBody, @Part("date") date: RequestBody, @Part("position") position: RequestBody, @Part image: MultipartBody.Part): JsonArray

        /*
        @GET("/photo")
        suspend fun photoGet(@Query("post_id") post_id: Int): Image
        */

        // Per ottenere i post di uno specifico utente
        @GET("/userposts")
        suspend fun userpostsGet(@Query("user_id") user_id: String): JsonArray




        @GET("/messages")
        suspend fun messagesGet(@Query("userId") userId: String, @Query("chatNameId") chatNameId: String): JsonArray

        @POST("/messages")
        suspend fun messagesPost(@Body data: RequestBody): String

        @GET("/chats")
        suspend fun chatsGet(@Query("userId") userId: String): JsonArray

        @PUT("/chats")
        suspend fun chatsPut(@Body data: RequestBody): String
    }

    object HelperClass {            // 'object' e' un Singleton

        var gson = GsonBuilder()
            .setLenient()
            .create()
        fun getInstance(): ServerAPI {
            val retrofit =
                Retrofit.Builder().baseUrl("https://maccproject2024.pythonanywhere.com")
                    .addConverterFactory(GsonConverterFactory.create(gson)) // to convert JSON object to Java object
                    .build().create(ServerAPI::class.java)
            return retrofit
        }
    }
}