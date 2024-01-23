package com.example.missing_pets

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
import retrofit2.http.Part
import retrofit2.http.Query


class ServerAPI {

    interface ServerAPI {
        @GET("/posts")
        suspend fun postsGet(): JsonArray

        @Multipart
        @POST("/posts")
        suspend fun postsPost(@Part("data") data: RequestBody, @Part image: MultipartBody.Part): String

        /*
        @GET("/photo")
        suspend fun photoGet(@Query("post_id") post_id: Int): Image
        */
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