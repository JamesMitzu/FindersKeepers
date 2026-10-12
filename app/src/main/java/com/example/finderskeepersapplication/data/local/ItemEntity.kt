package com.example.finderskeepersapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemName: String,
    val room: String,
    val storageLocation: String,
    val imageUri: String? = null,           // local file:// or content:// URI
    val imageUrlRemote: String? = null,     // Cloudinary / remote URL after upload
    val remoteId: String? = null,           // MockAPI id
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val itemStatus: ItemStatus = ItemStatus.STORED,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED
}

enum class ItemStatus {
    STORED,
    MISSING,
    FOUND
}
