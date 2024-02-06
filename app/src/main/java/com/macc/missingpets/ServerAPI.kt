package com.macc.missingpets

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Part


class ServerAPI {

    interface ServerAPI {
        /*@GET("/posts")
        suspend fun postsGet(): JsonArray

        @Multipart
        @POST("/posts")
        suspend fun postsPost(@Part("data") data: RequestBody, @Part image: MultipartBody.Part): String

        @GET("/photo")
        suspend fun photoGet(@Query("post_id") post_id: Int): Image
        */
        @GET("/messages")
        suspend fun messagesGet(): JsonArray

        @POST("/messages")
        suspend fun messagesPost(@Part("data") data: RequestBody): String

        @GET("/chats")
        suspend fun chatsGet(): JsonArray

        @POST("/chats")
        suspend fun chatsPost(@Part("data") data: RequestBody): String


    }

    // Singleton object
    object HelperClass {

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