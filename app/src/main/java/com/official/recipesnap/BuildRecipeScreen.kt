package com.official.recipesnap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

val INGREDIENT_CATEGORIES = listOf("Protein", "Vegetables", "Grains", "Spices", "Dairy", "Fruits", "Pantry", "Seafood", "Sauces")

val PREDEFINED_INGREDIENTS = mapOf(
    "Protein" to listOf("Chicken", "Tofu", "Eggs", "Beef", "Tempeh"),
    "Vegetables" to listOf("Tomato", "Onion", "Broccoli", "Spinach", "Carrot", "Bell Pepper", "Mushroom", "Zucchini"),
    "Grains" to listOf("Rice", "Quinoa", "Oats", "Pasta", "Bread"),
    "Spices" to listOf("Garlic", "Ginger", "Turmeric", "Black Pepper", "Cumin", "Chili Powder"),
    "Dairy" to listOf("Milk", "Cheese", "Butter", "Yogurt", "Cream"),
    "Fruits" to listOf("Apple", "Banana", "Lemon", "Lime", "Avocado", "Berries"),
    "Pantry" to listOf("Olive Oil", "Soy Sauce", "Honey", "Flour", "Sugar"),
    "Seafood" to listOf("Salmon", "Shrimp", "Tuna", "Cod"),
    "Sauces" to listOf("Ketchup", "Mustard", "Mayonnaise", "Sriracha", "BBQ Sauce")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildRecipeScreen(viewModel: RecipeViewModel, apiKey: String, onRecipeGenerated: () -> Unit) {
    var selectedCategory by remember { mutableStateOf("Protein") }
    var selectedIngredients by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = uiState is RecipeUiState.Loading
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is RecipeUiState.Success) {
            onRecipeGenerated()
        }
    }

    val coralColor = Color(0xFFE8734A)
    val lightCoral = Color(0xFFFFEFEA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCF9F7))
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 24.dp)
    ) {
        if (uiState is RecipeUiState.Success) {
            val state = uiState as RecipeUiState.Success
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Your Custom Recipe",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )
                TextButton(onClick = { viewModel.resetState() }) {
                    Text("Build Another", color = coralColor, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            RecipeCardStack(recipes = state.recipes)
        } else {
            Text(
                text = "Build a Recipe",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
            )
            Text(
                text = "Pick ingredients, get a custom recipe",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Categories
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(INGREDIENT_CATEGORIES) { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF3E2723) else Color(0xFFEEEEEE))
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else Color.DarkGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search or add custom ingredient") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = coralColor,
                    unfocusedBorderColor = Color.LightGray
                ),
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        TextButton(onClick = { 
                            if (selectedIngredients.size < 4) {
                                selectedIngredients = selectedIngredients + searchQuery.trim()
                                searchQuery = ""
                            }
                        }) {
                            Text("Add", color = coralColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grid
            val currentItems = PREDEFINED_INGREDIENTS[selectedCategory] ?: emptyList()
            val displayItems = if (searchQuery.isNotBlank()) {
                PREDEFINED_INGREDIENTS.values.flatten().filter { it.contains(searchQuery, ignoreCase = true) }.distinct()
            } else {
                currentItems
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(displayItems) { ingredient ->
                    val isSelected = selectedIngredients.contains(ingredient)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) lightCoral else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) coralColor else Color.LightGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (isSelected) {
                                    selectedIngredients = selectedIngredients - ingredient
                                } else if (selectedIngredients.size < 4) {
                                    selectedIngredients = selectedIngredients + ingredient
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = ingredient,
                            color = if (isSelected) coralColor else Color.DarkGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = coralColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected pills
            if (selectedIngredients.isNotEmpty()) {
                Text(
                    text = "${selectedIngredients.size} ingredients selected",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedIngredients.toList()) { ingredient ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(ingredient, fontSize = 12.sp, color = Color.DarkGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { selectedIngredients = selectedIngredients - ingredient },
                                tint = Color.Gray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Text(
                    text = "Select 2–4 ingredients",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp)) // height of selected pills area
            }

            if (uiState is RecipeUiState.Error) {
                val state = uiState as RecipeUiState.Error
                Text(
                    text = "Couldn't create your recipe: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            // Generate Button
            val count = selectedIngredients.size
            val canGenerate = count in 2..4
            Button(
                onClick = {
                    if (canGenerate && !isLoading) {
                        viewModel.getRecipeFromIngredients(selectedIngredients.toList(), apiKey, context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canGenerate) coralColor else Color.LightGray,
                    disabledContainerColor = Color.LightGray
                ),
                enabled = canGenerate && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Creating your recipe...", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else if (!canGenerate) {
                    if (count < 2) {
                        Text("✨ Select ${2 - count} more to continue", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else if (count > 4) {
                        Text("You can select up to 4 ingredients.", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("✨ Generate Recipe", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
