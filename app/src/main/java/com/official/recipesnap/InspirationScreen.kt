package com.official.recipesnap

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspirationScreen(viewModel: InspirationViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    val backgroundColor = Color(0xFFF7F3F0)
    val darkBrown = Color(0xFF3E2723)
    val coralColor = Color(0xFFE8734A)
    
    // Refresh snaps on compose
    LaunchedEffect(Unit) {
        viewModel.loadSnaps()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(top = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "My Snaps",
                        color = darkBrown,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    if (uiState is MySnapsUiState.Success) {
                        val count = (uiState as MySnapsUiState.Success).snaps.size
                        RecipeTag(text = "$count identified", bgColor = Color(0xFFEFEFEF))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Recipes recognised from your photos",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Content
        when (uiState) {
            is MySnapsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = coralColor)
                }
            }
            is MySnapsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = (uiState as MySnapsUiState.Error).message, color = MaterialTheme.colorScheme.error)
                }
            }
            is MySnapsUiState.Success -> {
                val snaps = (uiState as MySnapsUiState.Success).snaps
                if (snaps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No snaps found. Take a snap to get started!", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(snaps, key = { it.id }) { snap ->
                            SavedSnapCard(
                                snap = snap,
                                onFavoriteClick = { viewModel.toggleFavorite(snap) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Bottom nav padding
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedSnapCard(snap: SavedSnap, onFavoriteClick: () -> Unit) {
    val darkBrown = Color(0xFF3E2723)
    val coralColor = Color(0xFFE8734A)
    
    val context = LocalContext.current
    var showShareSheet by remember { mutableStateOf(false) }
    
    // Safely get the first recipe title, or fallback
    val primaryRecipe = snap.recipes.firstOrNull()
    val title = primaryRecipe?.title ?: "Unknown Recipe"
    // Mocking metadata visually as requested
    val matchScore = "94% match" // Hardcoded aesthetic
    val prepTime = "25 min" // Hardcoded aesthetic

    var showRecipeDialog by remember { mutableStateOf(false) }
    
    val heartColor by animateColorAsState(
        targetValue = if (snap.isFavorite) coralColor else Color.LightGray,
        animationSpec = tween(durationMillis = 300),
        label = "heartColor"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                val imageModel = if (snap.imageUri.startsWith("file://")) {
                    java.io.File(snap.imageUri.removePrefix("file://"))
                } else {
                    snap.imageUri
                }
                
                // Food Image
                AsyncImage(
                    model = imageModel,
                    contentDescription = title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkBrown,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Match Badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF0FDF4), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = matchScore, color = Color(0xFF166534), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Time
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = "Time",
                            modifier = Modifier.size(12.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = prepTime, color = Color.Gray, fontSize = 11.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Date
                    val dateString = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.US).format(Date(snap.timestamp))
                    Text(
                        text = dateString,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                
                // Favorite Button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (snap.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = heartColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(4.dp))
            
            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showRecipeDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = "View Recipe",
                        tint = darkBrown,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Recipe", color = darkBrown, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                
                // Vertical divider
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(Color(0xFFF0F0F0))
                )
                
                TextButton(
                    onClick = { showShareSheet = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = darkBrown,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share", color = darkBrown, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
    
    if (showShareSheet) {
        ShareBottomSheet(
            snap = snap,
            onDismiss = { showShareSheet = false }
        )
    }

    if (showRecipeDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showRecipeDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF7F3F0)
            ) {
                Box {
                    Column {
                        RecipeCardStack(recipes = snap.recipes)
                    }
                    IconButton(
                        onClick = { showRecipeDialog = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = darkBrown
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    snap: SavedSnap,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val primaryRecipe = snap.recipes.firstOrNull()
    val title = primaryRecipe?.title ?: "My Recipe"
    val calories = primaryRecipe?.calories?.toInt() ?: 0
    val time = "20 min" // mock time

    var selectedTab by remember { mutableStateOf(0) } // 0=Caption, 1=Hashtags, 2=Story

    val fallbackCaption = "✨ $title 🍝\n\nJust made this amazing dish! \n\n⏱ $time • $calories kcal\n\nFull recipe via link in bio 👋"
    val fallbackHashtags = "#${title.replace(" ", "")} #RecipeSnap #Foodie #DinnerIdeas #HomeCooking"
    val fallbackStory = "🍝 Tonight's dinner!\n\n$title 🌿\n\n$time • $calories kcal\n\nEasy, fresh & delicious!"

    val captionText = primaryRecipe?.caption ?: fallbackCaption
    val hashtagsText = primaryRecipe?.hashtags ?: fallbackHashtags
    val storyText = primaryRecipe?.story ?: fallbackStory

    val currentText = when (selectedTab) {
        0 -> captionText
        1 -> hashtagsText
        else -> storyText
    }

    val coralColor = Color(0xFFE8734A)
    val darkBrown = Color(0xFF3E2723)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFCF9F7),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Share to Instagram", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(title, color = darkBrown, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFEEEEEE), CircleShape)
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = darkBrown, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val imageModel = if (snap.imageUri.startsWith("file://")) {
                java.io.File(snap.imageUri.removePrefix("file://"))
            } else {
                snap.imageUri
            }

            // Recipe Preview Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = title,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = darkBrown, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$time • $calories kcal", color = Color.Gray, fontSize = 13.sp)
                }
                IconButton(onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        val contentUri = try {
                            if (snap.imageUri.startsWith("file://")) {
                                val imageFile = java.io.File(snap.imageUri.removePrefix("file://"))
                                androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    imageFile
                                )
                            } else {
                                android.net.Uri.parse(snap.imageUri)
                            }
                        } catch (e: Exception) {
                            android.net.Uri.parse(snap.imageUri)
                        }
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_TEXT, currentText)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setPackage("com.instagram.android")
                    }
                    try {
                        context.startActivity(shareIntent)
                    } catch (e: Exception) {
                        shareIntent.setPackage(null)
                        context.startActivity(Intent.createChooser(shareIntent, "Share"))
                    }
                }) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = "Instagram", tint = coralColor)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Caption", "Hashtags", "Story").forEachIndexed { index, tabTitle ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .background(if (isSelected) coralColor else Color.White, RoundedCornerShape(20.dp))
                            .clickable { selectedTab = index }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(tabTitle, color = if (isSelected) Color.White else darkBrown, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(currentText, color = darkBrown, fontSize = 14.sp, lineHeight = 20.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Actions
            Button(
                onClick = {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(currentText))
                    val message = when(selectedTab) {
                        0 -> "Caption copied!"
                        1 -> "Hashtags copied!"
                        else -> "Story text copied!"
                    }
                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = coralColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy Text", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Text", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, currentText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Somewhere Else"))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = darkBrown),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Somewhere Else", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RecipeTag(text: String, bgColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF3E2723), // Dark brown
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
