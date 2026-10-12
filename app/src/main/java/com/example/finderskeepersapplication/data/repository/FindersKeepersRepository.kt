package com.example.finderskeepersapplication.data.repository

import com.example.finderskeepersapplication.data.local.ItemDao
import com.example.finderskeepersapplication.data.local.ItemEntity
import com.example.finderskeepersapplication.data.remote.RemotePost
import com.example.finderskeepersapplication.data.remote.RemotePostsApi
import com.example.finderskeepersapplication.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/** Single entry point for local inventory data and remote sample-post data. */
class FindersKeepersRepository(
    private val itemDao: ItemDao,
    private val postsApi: RemotePostsApi
) {
    val localItems: Flow<List<ItemEntity>> = itemDao.getAllItems()

    suspend fun saveItem(item: ItemEntity) {
        if (item.id == 0) itemDao.insert(item) else itemDao.update(item)
    }

    suspend fun deleteItem(item: ItemEntity) = itemDao.delete(item)

    suspend fun fetchRemotePosts(): Resource<List<RemotePost>> = try {
        Resource.Success(postsApi.getPosts())
    } catch (exception: Exception) {
        Resource.Error(exception.localizedMessage ?: "Unable to load sample posts. Check your internet connection.")
    }
}
