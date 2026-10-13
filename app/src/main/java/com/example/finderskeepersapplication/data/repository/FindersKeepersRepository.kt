package com.example.finderskeepersapplication.data.repository

import android.content.Context
import android.net.Uri
import com.example.finderskeepersapplication.data.local.ItemDao
import com.example.finderskeepersapplication.data.local.ItemEntity
import com.example.finderskeepersapplication.data.local.ItemStatus
import com.example.finderskeepersapplication.data.local.SyncStatus
import com.example.finderskeepersapplication.data.remote.CloudinaryUploader
import com.example.finderskeepersapplication.data.remote.RemoteItem
import com.example.finderskeepersapplication.data.remote.RemotePost
import com.example.finderskeepersapplication.data.remote.RemotePostsApi
import com.example.finderskeepersapplication.domain.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class FindersKeepersRepository(
    private val itemDao: ItemDao,
    private val postsApi: RemotePostsApi,
    private val context: Context? = null
) {
    private val syncMutex = Mutex()

    val localItems: Flow<List<ItemEntity>> = itemDao.getAllItems()

    // In FindersKeepersRepository.kt
    suspend fun saveItem(item: ItemEntity): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val toSave = item.copy(
            lastUpdated = now,
            syncStatus = SyncStatus.PENDING
        )
        if (toSave.id == 0) {
            itemDao.insert(toSave) // This now returns the new Long ID
        } else {
            itemDao.update(toSave)
            toSave.id.toLong()     // Return existing ID
        }
    }

    suspend fun deleteItem(item: ItemEntity) = withContext(Dispatchers.IO) {
        itemDao.delete(item)
        if (!item.remoteId.isNullOrBlank()) {
            try {
                postsApi.deleteItem(item.remoteId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun fetchRemotePosts(): Resource<List<RemotePost>> = try {
        Resource.Success(postsApi.getPosts())
    } catch (exception: Exception) {
        Resource.Error(
            exception.localizedMessage ?: "Unable to load sample posts. Check your internet connection."
        )
    }

    suspend fun syncPendingItems() = withContext(Dispatchers.IO) {
        syncMutex.withLock {
            val pending = itemDao.getPendingItems()
            for (local in pending) {
                try {
                    itemDao.update(local.copy(syncStatus = SyncStatus.SYNCING))

                    // 1) Upload image to Cloudinary if local imageUri exists and remote URL is not uploaded yet
                    var uploadedImageUrl = local.imageUrlRemote
                    if (uploadedImageUrl.isNullOrBlank() && !local.imageUri.isNullOrBlank() && context != null) {
                        val uri = Uri.parse(local.imageUri)
                        uploadedImageUrl = CloudinaryUploader.uploadImage(context, uri)
                    }

                    // 2) Sync record to remote API
                    val remoteReq = RemoteItem(
                        id = local.remoteId,
                        itemName = local.itemName,
                        room = local.room,
                        storageLocation = local.storageLocation,
                        imageUrl = uploadedImageUrl,
                        itemStatus = local.itemStatus.name,
                        lastUpdated = local.lastUpdated
                    )

                    val assignedRemoteId: String? = if (local.remoteId.isNullOrBlank()) {
                        try {
                            val created = postsApi.createItem(remoteReq)
                            created.id
                        } catch (e: Exception) {
                            null
                        }
                    } else {
                        try {
                            val updated = postsApi.updateItem(local.remoteId, remoteReq)
                            updated.id ?: local.remoteId
                        } catch (e: Exception) {
                            local.remoteId
                        }
                    }

                    itemDao.update(
                        local.copy(
                            remoteId = assignedRemoteId ?: local.remoteId,
                            imageUrlRemote = uploadedImageUrl,
                            syncStatus = SyncStatus.SYNCED
                        )
                    )
                } catch (e: Exception) {
                    itemDao.update(local.copy(syncStatus = SyncStatus.PENDING))
                }
            }
            fetchRemoteItemsInternal()
        }
    }

    suspend fun updateItemStatus(item: ItemEntity, newStatus: ItemStatus) = withContext(Dispatchers.IO) {
        val updated = item.copy(
            itemStatus = newStatus,
            syncStatus = SyncStatus.PENDING,
            lastUpdated = System.currentTimeMillis()
        )
        itemDao.update(updated)
        syncPendingItems()
    }

    suspend fun fetchRemoteItems() = withContext(Dispatchers.IO) {
        syncMutex.withLock {
            fetchRemoteItemsInternal()
        }
    }

    private suspend fun fetchRemoteItemsInternal() {
        try {
            val remoteList = postsApi.getItems()
            for (remote in remoteList) {
                if (remote.itemName.isNotBlank()) {
                    // 1. Prefer checking DB by remoteId first
                    val existingByRemote = remote.id?.let { itemDao.getItemByRemoteId(it) }

                    // 2. Fallback to matching by fields if remoteId is missing locally
                    val existing = existingByRemote ?: run {
                        val currentLocalList = itemDao.getAllItemsList()
                        currentLocalList.find {
                            it.itemName.trim().equals(remote.itemName.trim(), ignoreCase = true) &&
                                    it.room.trim().equals(remote.room.trim(), ignoreCase = true) &&
                                    it.storageLocation.trim().equals(remote.storageLocation.trim(), ignoreCase = true)
                        }
                    }

                    val parsedStatus = try {
                        ItemStatus.valueOf(remote.itemStatus ?: "STORED")
                    } catch (e: Exception) {
                        ItemStatus.STORED
                    }

                    if (existing == null) {
                        itemDao.insert(
                            ItemEntity(
                                itemName = remote.itemName,
                                room = remote.room.ifBlank { "General" },
                                storageLocation = remote.storageLocation,
                                imageUrlRemote = remote.imageUrl,
                                remoteId = remote.id,
                                syncStatus = SyncStatus.SYNCED,
                                itemStatus = parsedStatus
                            )
                        )
                    } else {
                        // 3. Ensure remoteId and imageUrlRemote / status are up-to-date without making a new duplicate row
                        val updated = existing.copy(
                            remoteId = existing.remoteId ?: remote.id,
                            imageUrlRemote = existing.imageUrlRemote ?: remote.imageUrl,
                            itemStatus = parsedStatus,
                            syncStatus = SyncStatus.SYNCED
                        )
                        itemDao.update(updated)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}