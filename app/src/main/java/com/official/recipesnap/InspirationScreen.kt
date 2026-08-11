package com.official.recipesnap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspirationScreen(viewModel: InspirationViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    
    val backgroundColor = Color(0xFFF7F3F0)
    val darkBrown = Color(0xFF3E2723)
    val coralColor = Color(0xFFE8734A)
    val darkGreen = Color(0xFF1E3A3A)
    val lightBeige = Color(0xFFEAE5E0)
    
    val categories = listOf("All", "Breakfast", "Dinner", "Snack")

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
                Text(
                    text = "Inspiration",
                    color = darkBrown,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Discover delicious recipes",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(lightBeige, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = darkBrown
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Category Tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) darkGreen else lightBeige,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.selectCategory(category) }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color.White else darkBrown,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Content
        when (uiState) {
            is InspirationUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = coralColor)
                }
            }
            is InspirationUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = (uiState as InspirationUiState.Error).message, color = MaterialTheme.colorScheme.error)
                }
            }
            is InspirationUiState.Success -> {
                val recipes = (uiState as InspirationUiState.Success).recipes
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(recipes) { recipe ->
                        RecipeCard(recipe = recipe)
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Bottom nav padding
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: RecipeDto) {
    val darkBrown = Color(0xFF3E2723)
    val coralColor = Color(0xFFE8734A)
    val lightRed = Color(0xFFFDE4E4)
    val tagBgColor = Color(0xFFEFEFEF)
    
    // Find calories
    val calories = recipe.nutrition?.nutrients?.find { it.name == "Calories" }?.amount?.toInt()

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Image
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                AsyncImage(
                    model = recipe.image,
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                // Title & Heart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = recipe.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkBrown,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(lightRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorite",
                            tint = coralColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (recipe.healthScore != null && recipe.healthScore > 50) {
                        RecipeTag(text = "Healthy", bgColor = tagBgColor)
                    }
                    if (recipe.vegetarian) {
                        RecipeTag(text = "Vegetarian", bgColor = tagBgColor)
                    } else if (recipe.vegan) {
                        RecipeTag(text = "Vegan", bgColor = tagBgColor)
                    }
                    if (recipe.glutenFree) {
                        RecipeTag(text = "Gluten-Free", bgColor = tagBgColor)
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Footer (Time, Calories, Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (recipe.readyInMinutes != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = "Time",
                                    tint = coralColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${recipe.readyInMinutes} min", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                        if (calories != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.LocalFireDepartment,
                                    contentDescription = "Calories",
                                    tint = coralColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$calories kcal", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    }
                    
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = coralColor),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
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
