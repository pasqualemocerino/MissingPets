package com.macc.missingpets

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query


class ServerAPI {

    interface ServerAPI {

        @GET("/messages")
        suspend fun messagesGet(@Query("userId") userId: String, @Query("chatNameId") chatNameId: String): JsonArray

        @POST("/messages")
        suspend fun messagesPost(@Body data: RequestBody): String

        @GET("/chats")
        suspend fun chatsGet(@Query("userId") userId: String): JsonArray

        @PUT("/chats")
        suspend fun chatsPut(@Body data: RequestBody): String

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