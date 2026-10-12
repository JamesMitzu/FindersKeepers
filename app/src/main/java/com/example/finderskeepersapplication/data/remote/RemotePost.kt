package com.example.finderskeepersapplication.data.remote

import com.google.gson.annotations.SerializedName

data class RemotePost(
    val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val description: String
)
