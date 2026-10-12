package com.example.finderskeepersapplication.ui.additem

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.finderskeepersapplication.data.local.ItemEntity
import com.example.finderskeepersapplication.data.remote.RemotePost
import com.example.finderskeepersapplication.data.repository.FindersKeepersRepository
import com.example.finderskeepersapplication.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AddItemViewModel(private val repository: FindersKeepersRepository) : ViewModel() {
    val availableRooms = mutableStateListOf(
        "Living Room", "Bedroom", "Kitchen", "Bathroom", "Home Office", "Garage"
    )

    fun addRoom(room: String) {
        val cleanRoom = room.trim()
        if (cleanRoom.isNotEmpty() && availableRooms.none { it.equals(cleanRoom, true) }) {
            availableRooms.add(cleanRoom)
        }
    }

    fun removeRoom(room: String) {
        availableRooms.remove(room)
        if (selectedRoom == room) selectedRoom = null
    }

    var selectedRoom by mutableStateOf<String?>(null)
        private set
    var itemName by mutableStateOf("")
        private set
    var storageLocation by mutableStateOf("")
        private set
    var capturedImageUri by mutableStateOf<Uri?>(null)
        private set
    var lastSavedItem by mutableStateOf<ItemEntity?>(null)
        private set
    var editingItemId by mutableStateOf<Int?>(null)
        private set

    // Network state is observable by Compose and is always changed on the ViewModel.
    var remotePosts by mutableStateOf<Resource<List<RemotePost>>?>(null)
        private set

    fun loadRemotePosts() {
        viewModelScope.launch {
            remotePosts = Resource.Loading
            remotePosts = repository.fetchRemotePosts()
        }
    }

    fun startEditing(item: ItemEntity) {
        editingItemId = item.id
        selectedRoom = item.room
        itemName = item.itemName
        storageLocation = item.storageLocation
        capturedImageUri = item.imageUri?.let { Uri.parse(it) }
    }

    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch { repository.deleteItem(item) }
    }

    fun selectRoom(room: String) { selectedRoom = room }
    fun onItemNameChange(value: String) { itemName = value }
    fun onStorageLocationChange(value: String) { storageLocation = value }
    fun setCapturedImage(uri: Uri?) { capturedImageUri = uri }

    fun discardInProgress() {
        selectedRoom = null
        itemName = ""
        storageLocation = ""
        capturedImageUri = null
        editingItemId = null
    }

    val items: Flow<List<ItemEntity>> = repository.localItems

    fun saveItem() {
        val room = selectedRoom ?: return
        val snapshot = ItemEntity(
            id = editingItemId ?: 0,
            itemName = itemName,
            room = room,
            storageLocation = storageLocation,
            imageUri = capturedImageUri?.toString()
        )
        viewModelScope.launch {
            repository.saveItem(snapshot)
            lastSavedItem = if (snapshot.id == 0) snapshot else snapshot
            discardInProgress()
        }
    }
}

class AddItemViewModelFactory(
    private val repository: FindersKeepersRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddItemViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

enum class VoiceField { ITEM_NAME, STORAGE_LOCATION }
