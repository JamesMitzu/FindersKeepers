package com.example.finderskeepersapplication.data.remote

import retrofit2.http.*

data class RemoteItem(
    val id: String? = null,
    val itemName: String = "",
    val room: String = "",
    val storageLocation: String = "",
    val imageUrl: String? = null,
    val itemStatus: String? = "STORED",
    val lastUpdated: Long? = null
)

interface RemotePostsApi {
    @GET("posts")
    suspend fun getPosts(): List<RemotePost>

    @GET("items")
    suspend fun getItems(): List<RemoteItem>

    @POST("items")
    suspend fun createItem(@Body item: RemoteItem): RemoteItem

    @PUT("items/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: RemoteItem): RemoteItem

    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") id: String)
}