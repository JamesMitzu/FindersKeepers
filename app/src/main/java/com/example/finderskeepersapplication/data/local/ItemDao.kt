package com.example.finderskeepersapplication.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    // Return the new rowId so repository can use it if needed
    // Changed to IGNORE so we don't accidentally replace/delete existing rows when unique remoteId matches
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)

    @Query("SELECT * FROM items ORDER BY id DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items ORDER BY id DESC")
    suspend fun getAllItemsList(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE syncStatus != 'SYNCED' ORDER BY lastUpdated DESC")
    suspend fun getPendingItems(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Int): ItemEntity?

    // Add this query to check for existing remote items during sync
    @Query("SELECT * FROM items WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getItemByRemoteId(remoteId: String?): ItemEntity?
}