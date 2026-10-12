package com.example.finderskeepersapplication.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://6accf26eb406cb8ec0c6b901.mockapi.io/api/v1/"
    val remotePostsApi: RemotePostsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RemotePostsApi::class.java)
    }
}