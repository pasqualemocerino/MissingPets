package com.example.missing_pets

import com.google.gson.JsonArray
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST


class ServerAPI {

    interface ServerAPI {
        @GET("/posts")
        suspend fun postsGet(): JsonArray

        @Headers("Content-Type: application/json")
        @POST("/posts")
        suspend fun postsPost(@Body body: Post): String
    }

    object HelperClass {            // 'object' e' un Singleton
        fun getInstance(): ServerAPI {
            val retrofit =
                Retrofit.Builder().baseUrl("https://maccproject2024.pythonanywhere.com")
                    .addConverterFactory(GsonConverterFactory.create()) // to convert JSON object to Java object
                    .build().create(ServerAPI::class.java)
            return retrofit
        }
    }


}