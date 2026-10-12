package com.example.finderskeepersapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemName: String,
    val room: String,
    val storageLocation: String,
    val imageUri: String?
)