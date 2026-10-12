package com.example.finderskeepersapplication.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** Public sample REST API used to demonstrate Retrofit and JSON deserialization. */
interface RemotePostsApi {
    @GET("posts")
    suspend fun getPosts(@Query("_limit") limit: Int = 8): List<RemotePost>
}
