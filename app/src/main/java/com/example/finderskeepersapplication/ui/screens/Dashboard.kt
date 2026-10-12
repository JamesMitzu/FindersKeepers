package com.example.finderskeepersapplication.ui.screens

import com.example.finderskeepersapplication.R
import com.example.finderskeepersapplication.data.local.AppDatabase
import com.example.finderskeepersapplication.data.local.ItemEntity
import com.example.finderskeepersapplication.data.local.ItemStatus
import com.example.finderskeepersapplication.data.local.SyncStatus
import com.example.finderskeepersapplication.data.remote.RetrofitClient
import com.example.finderskeepersapplication.data.repository.FindersKeepersRepository
import com.example.finderskeepersapplication.domain.model.Resource
import com.example.finderskeepersapplication.ui.additem.AddItemViewModel
import com.example.finderskeepersapplication.ui.additem.AddItemViewModelFactory
import com.example.finderskeepersapplication.ui.additem.VoiceField

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.finderskeepersapplication.ui.theme.Bathroom
import com.example.finderskeepersapplication.ui.theme.Bedroom
import com.example.finderskeepersapplication.ui.theme.FindersGreen
import com.example.finderskeepersapplication.ui.theme.FindersKeepersApplicationTheme
import com.example.finderskeepersapplication.ui.theme.HomeOffice
import com.example.finderskeepersapplication.ui.theme.KeepersOrange
import com.example.finderskeepersapplication.ui.theme.Kitchen
import com.example.finderskeepersapplication.ui.theme.LivingArea
import com.example.finderskeepersapplication.ui.theme.Veranda
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import java.io.File
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Dashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindersKeepersApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        DashboardScreen()
                    }
                }
            }
        }
    }
}

private object DashboardRoutes {
    const val home = "home"
    const val selectRoom = "add_item/select_room"
    const val addItemDetails = "add_item/details"
    const val captureLocation = "add_item/capture_location"
    const val success = "add_item/success"
    const val itemDetail = "item_detail/{itemId}"
}

@Composable
fun DashboardScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val itemDao = remember { AppDatabase.getDatabase(context).itemDao() }
    val repository = remember { FindersKeepersRepository(itemDao, RetrofitClient.remotePostsApi, context.applicationContext) }
    val addItemViewModel: AddItemViewModel = viewModel(factory = AddItemViewModelFactory(repository))

    LaunchedEffect(Unit) {
        com.example.finderskeepersapplication.worker.SyncWorker.schedulePeriodicSync(context.applicationContext)
        addItemViewModel.syncPending()
    }

    NavHost(navController = navController, startDestination = DashboardRoutes.home) {
        composable(DashboardRoutes.home) {
            var searchQuery by remember { mutableStateOf("") }
            var pendingDeleteItem by remember { mutableStateOf<ItemEntity?>(null) }
            val items by addItemViewModel.items.collectAsState(initial = emptyList())

            FindersKeepersHomeScreen(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                items = items,
                onAddClick = {
                    navController.navigate(DashboardRoutes.selectRoom)
                },
                onItemClick = { item ->
                    navController.navigate("item_detail/${item.id}")
                },
                onEditItemClick = { item ->
                    addItemViewModel.startEditing(item)
                    navController.navigate(DashboardRoutes.selectRoom)
                },
                onDeleteItemClick = { item ->
                    pendingDeleteItem = item
                },
                onToggleStatus = { item ->
                    val newStatus = if (item.itemStatus == ItemStatus.MISSING) ItemStatus.STORED else ItemStatus.MISSING
                    addItemViewModel.updateItemStatus(item, newStatus)
                },
                onSyncClick = {
                    addItemViewModel.syncPending()
                },
                isLoading = addItemViewModel.isLoading
            )

            pendingDeleteItem?.let { item ->
                AlertDialog(
                    onDismissRequest = { pendingDeleteItem = null },
                    title = { Text("Delete item?") },
                    text = {
                        Text("This will permanently remove \"${item.itemName.ifBlank { "this item" }}\" from FindersKeepers.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            addItemViewModel.deleteItem(item)
                            pendingDeleteItem = null
                        }) {
                            Text("Delete", color = Color(0xFFC9752F), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingDeleteItem = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
        composable(DashboardRoutes.selectRoom) {
            SelectRoomScreen(
                navController = navController,
                viewModel = addItemViewModel
            )
        }
        composable(DashboardRoutes.captureLocation) {
            CaptureLocationScreen(
                onNextClick = { navController.navigate(DashboardRoutes.addItemDetails) },
                navController = navController,
                viewModel = addItemViewModel
            )
        }
        composable(DashboardRoutes.addItemDetails) {
            AddItemScreen(
                onConfirmSave = {
                    navController.navigate(DashboardRoutes.success) {
                        popUpTo(DashboardRoutes.home)
                    }
                },
                onBackClick = { navController.popBackStack() },
                viewModel = addItemViewModel
            )
        }
        composable(DashboardRoutes.success) {
            SuccessScreen(
                item = addItemViewModel.lastSavedItem,
                onBackToHomeClick = {
                    navController.navigate(DashboardRoutes.home) {
                        popUpTo(DashboardRoutes.home) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = DashboardRoutes.itemDetail,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: -1
            val items by addItemViewModel.items.collectAsState(initial = emptyList())
            val item = items.find { it.id == itemId }

            ItemDetailScreen(
                item = item,
                onFinishClick = { navController.popBackStack() },
                onStatusChange = { newStatus ->
                    item?.let { addItemViewModel.updateItemStatus(it, newStatus) }
                }
            )
        }
    }
}

// Home Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FindersKeepersHomeScreen(
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    items: List<ItemEntity> = emptyList(),
    onAddClick: () -> Unit = {},
    onItemClick: (ItemEntity) -> Unit = {},
    onEditItemClick: (ItemEntity) -> Unit = {},
    onDeleteItemClick: (ItemEntity) -> Unit = {},
    onToggleStatus: (ItemEntity) -> Unit = {},
    onSyncClick: () -> Unit = {},
    isLoading: Boolean = false
) {
    val query = searchQuery.trim()
    val isSearching = query.isNotEmpty()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val missingItems = remember(items) { items.filter { it.itemStatus == ItemStatus.MISSING } }
    val missingCount = missingItems.size

    val visibleItems = remember(items, query, selectedFilter) {
        var result = items
        if (selectedFilter == "MISSING") {
            result = result.filter { it.itemStatus == ItemStatus.MISSING }
        } else if (selectedFilter == "STORED") {
            result = result.filter { it.itemStatus != ItemStatus.MISSING }
        }

        if (query.isNotEmpty()) {
            result = result.filter {
                it.itemName.contains(query, ignoreCase = true) ||
                        it.room.contains(query, ignoreCase = true) ||
                        it.storageLocation.contains(query, ignoreCase = true)
            }
        }
        result
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Log a new item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(R.dimen.screen_padding_horizontal))
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
            AppTitleBar()

            if (missingCount > 0) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "$missingCount item${if (missingCount > 1) "s" else ""} marked as MISSING",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = "Tap to view lost items you are looking for.",
                                fontSize = 11.sp,
                                color = Color(0xFF8D6E63)
                            )
                        }
                        TextButton(onClick = { selectedFilter = "MISSING" }) {
                            Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                    }
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )

            if (isSearching && visibleItems.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("📍", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("FindersKeepers Answer:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text(
                                "\"${visibleItems.first().itemName}\" is in ${visibleItems.first().room} → ${visibleItems.first().storageLocation}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All (${items.size})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedFilter == "MISSING",
                    onClick = { selectedFilter = "MISSING" },
                    label = { Text("Missing ($missingCount)", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedFilter == "STORED",
                    onClick = { selectedFilter = "STORED" },
                    label = { Text("Stored (${items.size - missingCount})", fontSize = 12.sp) }
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onSyncClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isLoading) "Syncing with Cloud…" else "Sync with Cloud (MockAPI & Cloudinary)")
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            SectionHeader(text = if (isSearching) "MATCHES" else if (selectedFilter == "MISSING") "MISSING ITEMS" else "RECENTLY STORED")

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            if (visibleItems.isEmpty()) {
                Text(
                    text = if (items.isEmpty())
                        "Nothing stored yet — tap + to log your first item."
                    else
                        "No items match this filter.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))) {
                    visibleItems.forEach { item ->
                        RecentItemCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            onEditClick = { onEditItemClick(item) },
                            onDeleteClick = { onDeleteItemClick(item) },
                            onToggleStatus = { onToggleStatus(item) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (isSearching) {
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "No more", fontSize = 12.sp, color = Color(0xFFB9B9B9))
                }
            } else {
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
                TapToLogPromptCard()
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
        }
    }
}

@Composable
private fun RecentItemCard(
    item: ItemEntity,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleStatus: (ItemEntity) -> Unit
) {
    val isMissing = item.itemStatus == ItemStatus.MISSING
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            val displayImage = item.imageUri?.ifBlank { null } ?: item.imageUrlRemote?.ifBlank { null }
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
                modifier = Modifier.size(dimensionResource(R.dimen.thumbnail_size))
            ) {
                if (displayImage != null) {
                    AsyncImage(
                        model = displayImage,
                        contentDescription = item.itemName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Filled.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.itemName.ifBlank { "Unnamed item" },
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${item.room} • ${item.storageLocation.ifBlank { "No details" }}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isMissing) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isMissing) "⚠️ MISSING" else "✓ STORED",
                            color = if (isMissing) Color(0xFFC62828) else Color(0xFF2E7D32),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    val syncColor = when (item.syncStatus) {
                        SyncStatus.PENDING -> Color(0xFFFFA726)
                        SyncStatus.SYNCING -> Color(0xFF42A5F5)
                        SyncStatus.SYNCED -> Color(0xFF66BB6A)
                    }

                    Text(
                        text = when (item.syncStatus) {
                            SyncStatus.PENDING -> "Pending"
                            SyncStatus.SYNCING -> "Syncing"
                            SyncStatus.SYNCED -> "Synced"
                        },
                        color = syncColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            TextButton(
                onClick = { onToggleStatus(item) },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
            ) {
                Text(
                    text = if (item.itemStatus == ItemStatus.MISSING) "I Found It!" else "Mark Lost",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.itemStatus == ItemStatus.MISSING) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
            IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit ${item.itemName}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete ${item.itemName}",
                    tint = Color(0xFFC9752F),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AppTitleBar() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Finders",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = FindersGreen
        )
        Text(
            text = "Keepers",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = KeepersOrange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_pill)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 18.dp)
        ) {
            Spacer(Modifier.width(4.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text("Search item", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFF444444))
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color = Color(0xFF8A8A8A)
    )
}

@Composable
private fun TapToLogPromptCard() {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_large)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
                modifier = Modifier.size(dimensionResource(R.dimen.thumbnail_size))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tap the plus to log",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Store an item simply by using your microphone to log.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Shared header used by every step of the Add Item flow
@Composable
private fun AddItemFlowHeader(onBackClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF444444))
        }
        Spacer(Modifier.width(4.dp))
        Row {
            Text(
                text = "Finders",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = FindersGreen
            )
            Text(
                text = "Keepers",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = KeepersOrange
            )
        }
    }
}

// Add Item & Store — Select Room Screen
@Composable
private fun SelectRoomScreen(
    navController: NavHostController,
    viewModel: AddItemViewModel
) {
    val selectedRoom = viewModel.selectedRoom

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.screen_padding_horizontal))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
        AddItemFlowHeader(onBackClick = {
            viewModel.discardInProgress()
            navController.popBackStack()
        })

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Text(
            text = "Add Item & Store",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        SectionHeader(text = "SELECT A ROOM")

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        DynamicRoomPicker(
            rooms = viewModel.availableRooms,
            selectedRoom = selectedRoom,
            onRoomSelected = viewModel::selectRoom,
            onAddRoom = viewModel::addRoom,
            onRemoveRoom = viewModel::removeRoom
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Selected Location:",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(Modifier.width(12.dp))
            SelectedRoomChip(room = selectedRoom)
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))

        StepDots(activeIndex = 0, totalSteps = 3)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        Button(
            onClick = {
                navController.navigate(DashboardRoutes.captureLocation)
            },
            enabled = selectedRoom != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.button_height))
        ) {
            Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
    }
}

@Composable
private fun DynamicRoomPicker(
    rooms: List<String>,
    selectedRoom: String?,
    onRoomSelected: (String) -> Unit,
    onAddRoom: (String) -> Unit,
    onRemoveRoom: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newRoomName by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Your rooms", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Add or remove rooms to match your home.", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${rooms.size}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
        if (rooms.isEmpty()) Text("No rooms yet. Add a room to continue.")
        rooms.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                pair.forEach { room ->
                    val selected = room == selectedRoom
                    Surface(onClick = { onRoomSelected(room) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.weight(1f)) {
                        Row(Modifier.padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Home, contentDescription = null,
                                tint = if (selected) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(room, modifier = Modifier.weight(1f),
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 12.sp)
                            IconButton(onClick = { onRemoveRoom(room) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Remove $room",
                                    tint = if (selected) Color.White else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(17.dp))
                            }
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        OutlinedButton(onClick = { newRoomName = ""; showAddDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.AddCircleOutline, contentDescription = null)
            Spacer(Modifier.width(8.dp)); Text("Add a room")
        }
    }
    if (showAddDialog) AlertDialog(
        onDismissRequest = { showAddDialog = false },
        title = { Text("Add a room") },
        text = { OutlinedTextField(value = newRoomName, onValueChange = { newRoomName = it },
            label = { Text("Room name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { TextButton(onClick = {
            if (newRoomName.isNotBlank() && rooms.none { it.equals(newRoomName.trim(), true) }) {
                onAddRoom(newRoomName); onRoomSelected(newRoomName.trim()); showAddDialog = false
            }
        }) { Text("Add") } },
        dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
    )
}

@Composable
private fun RoomButton(
    label: String,
    color: Color,
    selectedRoom: String?,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = selectedRoom == label
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            onClick = { onClick(label) },
            color = color,
            contentColor = Color.White,
            shape = RoundedCornerShape(40),
            shadowElevation = if (isSelected) 1.dp else 0.dp,
            modifier = modifier.border(
                width = if (isSelected) 1.dp else (-1).dp,
                color = Color(0xFFFFC677),
                shape = RoundedCornerShape(40)
            )
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SelectedRoomChip(room: String?) {
    Surface(
        color = Color(0x33FFE6C2),
        shape = RoundedCornerShape(20),
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color(0xFFFFC677),
                shape = RoundedCornerShape(20)
            )
    ) {
        Text(
            text = room ?: "None selected",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color(0xFFFFC677),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

// ---- Add Item & Store — Item Details Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onConfirmSave: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: AddItemViewModel
) {
    val context = LocalContext.current
    var activeVoiceField by remember { mutableStateOf<VoiceField?>(null) }
    var pendingVoiceField by remember { mutableStateOf<VoiceField?>(null) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        } else {
            null
        }
        when (activeVoiceField) {
            VoiceField.ITEM_NAME -> spokenText?.let(viewModel::onItemNameChange)
            VoiceField.STORAGE_LOCATION -> spokenText?.let(viewModel::onStorageLocationChange)
            null -> {}
        }
        activeVoiceField = null
    }

    fun launchSpeechRecognizer(field: VoiceField) {
        activeVoiceField = field
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now…")
        }
        speechLauncher.launch(intent)
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingVoiceField?.let { launchSpeechRecognizer(it) }
        }
        pendingVoiceField = null
    }

    fun requestMicAndLaunch(field: VoiceField) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchSpeechRecognizer(field)
        } else {
            pendingVoiceField = field
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.screen_padding_horizontal))
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
        AddItemFlowHeader(onBackClick = onBackClick)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Text(
            text = "Add Item & Store",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        SectionHeader(text = "ITEM DETAILS")

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        OutlinedTextField(
            value = viewModel.itemName,
            onValueChange = viewModel::onItemNameChange,
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            trailingIcon = {
                IconButton(onClick = { requestMicAndLaunch(VoiceField.ITEM_NAME) }) {
                    Icon(Icons.Default.Mic, contentDescription = "Speak item name", tint = MaterialTheme.colorScheme.primary)
                }
            }
        )

        if (viewModel.isSuggesting) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("AI analyzing photo…", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        } else if (viewModel.suggestedName != null) {
            Spacer(Modifier.height(6.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI Suggestion: \"${viewModel.suggestedName}\"",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.applySuggestion() },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = { viewModel.dismissSuggestion() },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Dismiss", fontSize = 12.sp)
                    }
                }
            }
        }

        viewModel.suggestedTags?.let { tags ->
            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("AI Tags:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    tags.take(3).forEach { tag ->
                        SuggestionChip(
                            onClick = { viewModel.applyTag(tag) },
                            label = { Text("+ $tag", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        OutlinedTextField(
            value = viewModel.storageLocation,
            onValueChange = viewModel::onStorageLocationChange,
            label = { Text("Storage Location") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            trailingIcon = {
                IconButton(onClick = { requestMicAndLaunch(VoiceField.STORAGE_LOCATION) }) {
                    Icon(Icons.Default.Mic, contentDescription = "Speak storage location", tint = MaterialTheme.colorScheme.primary)
                }
            }
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Tap the mic on either field to speak instead of typing.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.weight(1f))

        StepDots(activeIndex = 2, totalSteps = 3)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        Button(
            onClick = { viewModel.saveItem(); onConfirmSave() },
            enabled = viewModel.itemName.isNotBlank() && viewModel.storageLocation.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.button_height))
        ) {
            Text("Confirm & Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
    }
}

// Add Item & Store — in-app camera preview and capture
private fun createImageFile(context: Context): File {
    val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
    return File.createTempFile("item_photo_", ".jpg", imagesDir)
}

private fun fileUri(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

@Composable
private fun CaptureLocationScreen(
    onNextClick: () -> Unit,
    navController: NavHostController,
    viewModel: AddItemViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val capturedImageUri = viewModel.capturedImageUri
    var cameraPermissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var cameraActive by remember { mutableStateOf(capturedImageUri == null && cameraPermissionGranted) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraPermissionGranted = granted
        cameraActive = granted
        if (!granted) cameraError = "Camera permission is needed to take a location photo."
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    androidx.compose.runtime.LaunchedEffect(cameraActive, previewView) {
        val view = previewView
        if (cameraActive && view != null) {
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener({
                try {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also { it.surfaceProvider = view.surfaceProvider }
                    val capture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                    imageCapture = capture
                    cameraError = null
                } catch (error: Exception) {
                    cameraError = "Unable to start the camera. Please try again."
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.screen_padding_horizontal))
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
        AddItemFlowHeader(onBackClick = { navController.popBackStack() })
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Text("Add Item & Store", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        SectionHeader(text = "CAPTURE STORAGE LOCATION")
        Spacer(Modifier.height(8.dp))
        Text("Take a photo of the shelf, drawer, or spot where you keep this item.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))

        if (cameraActive) {
            AndroidView(
                factory = { ctx -> PreviewView(ctx).also { it.scaleType = PreviewView.ScaleType.FILL_CENTER; previewView = it } },
                modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(18.dp))
            )
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                if (capturedImageUri != null) {
                    AsyncImage(model = capturedImageUri, contentDescription = "Captured storage location",
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Column(Modifier.fillMaxSize().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text("Your camera preview will appear here", fontWeight = FontWeight.SemiBold)
                        Text("Allow camera access to capture the storage location.", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        cameraError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = {
                    if (cameraPermissionGranted) cameraActive = true
                    else permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.weight(1f)
            ) { Text(if (cameraActive) "Camera ready" else if (capturedImageUri == null) "Open camera" else "Retake") }
            Button(
                onClick = {
                    val capture = imageCapture
                    if (capture == null) {
                        cameraError = "Camera is starting. Please try again in a moment."
                    } else {
                        try {
                            val file = createImageFile(context)
                            val options = ImageCapture.OutputFileOptions.Builder(file).build()
                            capture.takePicture(options, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    android.os.Handler(context.mainLooper).post {
                                        viewModel.setCapturedImage(fileUri(context, file), context)
                                        cameraActive = false
                                        cameraError = null
                                    }
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    android.os.Handler(context.mainLooper).post {
                                        cameraError = "Photo could not be saved. Please try again."
                                    }
                                }
                            })
                        } catch (error: Exception) {
                            cameraError = "Photo could not be saved. Please try again."
                        }
                    }
                },
                enabled = cameraActive && imageCapture != null,
                modifier = Modifier.weight(1f)
            ) { Text("Capture photo") }
        }
        if (capturedImageUri != null) {
            Spacer(Modifier.height(8.dp))
            Text("Photo captured ✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.weight(0.2f))
        StepDots(activeIndex = 1, totalSteps = 3)
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        Button(
            onClick = onNextClick,
            enabled = capturedImageUri != null,
            modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.button_height)),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small))
        ) { Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
    }
}

@Composable
private fun ImagePreviewCard(uri: Uri?) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.image_preview_height))
    ) {
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = "Captured storage location photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "No photo yet — tap Take Image below", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun StepDots(activeIndex: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 0 until totalSteps) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (i == activeIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}

// Add Item & Store — Success Screen
@Composable
private fun SuccessScreen(
    item: ItemEntity?,
    onBackToHomeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(R.dimen.screen_padding_horizontal)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Row {
            Text(
                text = "Finders",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = FindersGreen
            )
            Text(
                text = "Keepers",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = KeepersOrange
            )
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFE9DDBF)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        Text(
            text = "Item saved successfully",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))) {
                SuccessDetailRow(label = "Item Name", value = item?.itemName?.ifBlank { "—" } ?: "—")
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
                SuccessDetailRow(label = "Room", value = item?.room ?: "—")
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
                SuccessDetailRow(
                    label = "Storage Location",
                    value = item?.storageLocation?.ifBlank { "—" } ?: "—"
                )
            }
        }

        val displayImage = item?.imageUri?.ifBlank { null } ?: item?.imageUrlRemote?.ifBlank { null }
        if (displayImage != null) {
            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
            Text(
                text = "Attached Image:",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                AsyncImage(
                    model = displayImage,
                    contentDescription = "Attached storage location photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
        Button(
            onClick = onBackToHomeClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = com.example.finderskeepersapplication.ui.theme.Beige,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_pill)),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.button_height))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = "Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
    }
}

@Composable
private fun SuccessDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        Text(text = value, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

// Item Details Screen (reached by tapping a Dashboard row)
@Composable
private fun ItemDetailScreen(
    item: ItemEntity?,
    onFinishClick: () -> Unit,
    onStatusChange: (ItemStatus) -> Unit = {}
) {
    var showFullScreenImage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(R.dimen.screen_padding_horizontal)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Row {
            Text(
                text = "Finders",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = FindersGreen
            )
            Text(
                text = "Keepers",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = KeepersOrange
            )
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
        Text(
            text = "Item Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))) {
                SuccessDetailRow(label = "Item Name", value = item?.itemName?.ifBlank { "—" } ?: "—")
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
                SuccessDetailRow(label = "Room", value = item?.room ?: "—")
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
                SuccessDetailRow(
                    label = "Storage Location",
                    value = item?.storageLocation?.ifBlank { "—" } ?: "—"
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
                SuccessDetailRow(
                    label = "Status",
                    value = if (item?.itemStatus == ItemStatus.MISSING) "⚠️ MISSING / LOST" else "STORED"
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                val newStatus = if (item?.itemStatus == ItemStatus.MISSING) ItemStatus.STORED else ItemStatus.MISSING
                onStatusChange(newStatus)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (item?.itemStatus == ItemStatus.MISSING) "I Found It! (Mark Stored)" else "Mark as Missing / Lost",
                fontWeight = FontWeight.Bold,
                color = if (item?.itemStatus == ItemStatus.MISSING) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }

        val displayImage = item?.imageUri?.ifBlank { null } ?: item?.imageUrlRemote?.ifBlank { null }
        if (displayImage != null) {
            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
            Text(
                text = "Attached Image:",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Box {
                Surface(
                    onClick = { showFullScreenImage = true },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    AsyncImage(
                        model = displayImage,
                        contentDescription = "Attached storage location photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                        .clickable { showFullScreenImage = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.OpenInFull,
                        contentDescription = "View image full-screen",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.weight(1f))
        Button(
            onClick = onFinishClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.button_height))
        ) {
            Text(text = "Finish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
    }

    val displayImage = item?.imageUri?.ifBlank { null } ?: item?.imageUrlRemote?.ifBlank { null }
    if (showFullScreenImage && displayImage != null) {
        FullScreenImageViewer(
            imageModel = displayImage,
            onDismiss = { showFullScreenImage = false }
        )
    }
}

@Composable
private fun FullScreenImageViewer(imageModel: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                )
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = "Attached storage location photo, full screen",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(dimensionResource(R.dimen.spacing_medium))
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}