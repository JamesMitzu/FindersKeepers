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
import com.example.finderskeepersapplication.data.local.SyncStatus
import com.example.finderskeepersapplication.data.remote.RemotePost
import com.example.finderskeepersapplication.data.repository.FindersKeepersRepository
import com.example.finderskeepersapplication.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AddItemViewModel(private val repository: FindersKeepersRepository) : ViewModel() {
    val availableRooms = mutableStateListOf(
        "Living Room", "Bedroom", "Kitchen", "Bathroom", "Home Office", "Garage"
    )

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

    var remotePosts by mutableStateOf<Resource<List<RemotePost>>?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var suggestedName by mutableStateOf<String?>(null)
    var suggestedTags by mutableStateOf<List<String>?>(null)
    var isSuggesting by mutableStateOf(false)

    fun loadRemotePosts() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            remotePosts = Resource.Loading
            try {
                remotePosts = repository.fetchRemotePosts()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to load remote posts"
                remotePosts = Resource.Error(errorMessage ?: "Failed to load remote posts")
            } finally {
                isLoading = false
            }
        }
    }

    fun saveItem() {
        val room = selectedRoom ?: return
        val snapshot = ItemEntity(
            id = editingItemId ?: 0,
            itemName = itemName,
            room = room,
            storageLocation = storageLocation,
            imageUri = capturedImageUri?.toString(),
            syncStatus = SyncStatus.PENDING,
            lastUpdated = System.currentTimeMillis()
        )
        viewModelScope.launch {
            isLoading = true
            try {
                repository.saveItem(snapshot)
                lastSavedItem = snapshot
                discardInProgress()
                syncPending()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to save"
            } finally {
                isLoading = false
            }
        }
    }

    fun syncPending() = viewModelScope.launch {
        isLoading = true
        try {
            repository.syncPendingItems()
        } catch (e: Exception) {
            errorMessage = e.localizedMessage
        } finally {
            isLoading = false
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
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun updateItemStatus(item: ItemEntity, status: com.example.finderskeepersapplication.data.local.ItemStatus) {
        viewModelScope.launch {
            repository.updateItemStatus(item, status)
        }
    }

    fun selectRoom(room: String) { selectedRoom = room }
    fun addRoom(room: String) {
        val trimmed = room.trim()
        if (trimmed.isNotBlank() && !availableRooms.contains(trimmed)) {
            availableRooms.add(trimmed)
        }
    }
    fun removeRoom(room: String) {
        availableRooms.remove(room)
        if (selectedRoom == room) {
            selectedRoom = null
        }
    }
    fun onItemNameChange(value: String) { itemName = value }
    fun onStorageLocationChange(value: String) { storageLocation = value }
    fun setCapturedImage(uri: Uri?, context: android.content.Context? = null) {
        capturedImageUri = uri
        if (uri != null && context != null) {
            analyzeCapturedImage(context, uri)
        }
    }

    fun analyzeCapturedImage(context: android.content.Context, uri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            isSuggesting = true
            try {
                val labels = com.example.finderskeepersapplication.util.ImageAnalysisHelper.labelImage(context, uri)
                val ocr = com.example.finderskeepersapplication.util.ImageAnalysisHelper.recognizeText(context, uri)
                suggestedTags = labels.take(5)
                val best = com.example.finderskeepersapplication.util.ImageAnalysisHelper.chooseBestName(labels, ocr)
                suggestedName = best
                if (itemName.isBlank() && !best.isNullOrBlank()) {
                    itemName = best
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSuggesting = false
            }
        }
    }

    fun applySuggestion() {
        suggestedName?.let {
            itemName = it
        }
    }

    fun applyTag(tag: String) {
        if (itemName.isBlank()) {
            itemName = tag
        } else if (!itemName.contains(tag, ignoreCase = true)) {
            itemName = "$itemName ($tag)"
        }
    }

    fun dismissSuggestion() {
        suggestedName = null
        suggestedTags = null
    }

    fun discardInProgress() {
        selectedRoom = null
        itemName = ""
        storageLocation = ""
        capturedImageUri = null
        editingItemId = null
        suggestedName = null
        suggestedTags = null
        isSuggesting = false
    }

    val items: Flow<List<ItemEntity>> = repository.localItems
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