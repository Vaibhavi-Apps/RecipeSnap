package com.official.recipesnap

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import java.util.Date
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel = viewModel(),
    onNavigateToRecipe: ((ExampleRecipe) -> Unit)? = null
) {
    val isSetupCompleted by viewModel.isSetupCompleted.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    val coralColor = MaterialTheme.colorScheme.primary
    val darkBrown = MaterialTheme.colorScheme.onBackground
    val lightPeach = MaterialTheme.colorScheme.primaryContainer
    val darkGreen = MaterialTheme.colorScheme.secondary
    val lightCream = MaterialTheme.colorScheme.background
    
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = This Week, 1 = My Meals

    if (!isSetupCompleted) {
        MealPlanSetupFlow(viewModel, onComplete = {
            selectedTab = 1
        })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightCream)
    ) {
        // Top Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TabButton(
                text = "🇮🇳 This Week",
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "⭐ My Meals",
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
        }

        if (selectedTab == 0) {
            ThisWeekView(viewModel, darkBrown, darkGreen, coralColor, lightPeach, onNavigateToRecipe)
        } else {
            MyMealsView(viewModel, darkBrown, darkGreen, coralColor, lightPeach)
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val darkGreen = MaterialTheme.colorScheme.secondary
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                if (isSelected) darkGreen else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThisWeekView(
    viewModel: MealPlanViewModel,
    darkBrown: Color,
    darkGreen: Color,
    coralColor: Color,
    lightPeach: Color,
    onNavigateToRecipe: ((ExampleRecipe) -> Unit)?
) {
    val currentWeekStart by viewModel.currentWeekStart.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val plannedMeals by viewModel.plannedMeals.collectAsState()
    
    val weekDays = viewModel.getWeekDays()
    val selectedDateStr = viewModel.formatDateForStorage(selectedDate)
    val plannedForDate = plannedMeals.filter { it.date == selectedDateStr }
    
    var showSlotPicker by remember { mutableStateOf<MealCategory?>(null) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Meal Plan", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = darkBrown)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.previousWeek() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous Week")
                    }
                    Text(
                        viewModel.formatDateRange(currentWeekStart), 
                        fontSize = 14.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = { viewModel.nextWeek() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.ChevronRight, contentDescription = "Next Week")
                    }
                }
            }
            
            val totalMealsThisWeek = weekDays.sumOf { date -> 
                val dStr = viewModel.formatDateForStorage(date)
                plannedMeals.count { it.date == dStr }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Outlined.Event, contentDescription = null, tint = darkGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("$totalMealsThisWeek meals", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = darkGreen)
            }
        }
        
        // Days Row
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(weekDays) { date ->
                val cal = Calendar.getInstance().apply { time = date }
                val dayName = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")[cal.get(Calendar.DAY_OF_WEEK) - 1]
                val dayNum = cal.get(Calendar.DAY_OF_MONTH).toString()
                val isSelected = date == selectedDate
                val dStr = viewModel.formatDateForStorage(date)
                val hasMeals = plannedMeals.any { it.date == dStr }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(48.dp)
                        .background(if (isSelected) darkGreen else MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .clickable { viewModel.selectDate(date) }
                        .padding(vertical = 12.dp)
                ) {
                    Text(dayName, fontSize = 12.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha=0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(dayNum, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else darkBrown)
                    if (hasMeals) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.size(4.dp).background(if (isSelected) MaterialTheme.colorScheme.onPrimary else coralColor, CircleShape))
                    }
                }
            }
        }
        
        // Selected Date Details
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val cal = Calendar.getInstance().apply { time = selectedDate }
            val dayName = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")[cal.get(Calendar.DAY_OF_WEEK) - 1]
            val monthName = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[cal.get(Calendar.MONTH)]
            val dayNum = cal.get(Calendar.DAY_OF_MONTH).toString()
            
            Text(
                "$dayName, $monthName $dayNum", 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold, 
                color = darkBrown,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            MealCategory.values().forEach { category ->
                MealSlot(
                    category = category,
                    plannedMeals = plannedForDate.filter { it.mealCategory == category },
                    viewModel = viewModel,
                    darkBrown = darkBrown,
                    coralColor = coralColor,
                    onAddClick = { showSlotPicker = category },
                    onRemoveClick = { planId -> viewModel.removePlannedMeal(planId) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("💡 Recipe ideas for $dayName", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = darkBrown)
            Spacer(modifier = Modifier.height(12.dp))
            
            val examples = listOf(
                ExampleRecipe(
                    "Spicy Arrabbiata Pasta", 
                    "A fiery and garlic-heavy Italian classic.",
                    listOf("Italian", "Dinner", "Spicy"),
                    R.drawable.example_pasta
                ),
                ExampleRecipe(
                    "Quinoa Salad Bowl", 
                    "A fresh and healthy bowl packed with protein.",
                    listOf("Healthy", "Lunch", "Vegan"),
                    R.drawable.example_salad
                ),
                ExampleRecipe(
                    "Avocado Toast with Egg", 
                    "The ultimate breakfast! Creamy smashed avocado on sourdough.",
                    listOf("Breakfast", "Quick", "Vegetarian"),
                    R.drawable.example_avocado
                )
            )
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(examples) { recipe ->
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigateToRecipe?.invoke(recipe) }
                    ) {
                        Column {
                            Image(
                                painter = painterResource(id = recipe.imageRes),
                                contentDescription = recipe.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(recipe.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = darkBrown, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${recipe.tags[1]} · 20 min", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
    
    if (showSlotPicker != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSlotPicker = null },
            sheetState = sheetState
        ) {
            SlotPickerSheet(
                category = showSlotPicker!!,
                viewModel = viewModel,
                onSelectMyMeals = { myMeals ->
                    myMeals.forEach { myMeal ->
                        viewModel.addPlannedMeal(selectedDate, showSlotPicker!!, myMeal)
                    }
                    showSlotPicker = null
                }
            )
        }
    }
}

@Composable
fun MealSlot(
    category: MealCategory,
    plannedMeals: List<PlannedMeal>,
    viewModel: MealPlanViewModel,
    darkBrown: Color,
    coralColor: Color,
    onAddClick: () -> Unit,
    onRemoveClick: (String) -> Unit
) {
    val categoryName = category.name.lowercase().replaceFirstChar { it.uppercase() }
    val lightBg = when(category) {
        MealCategory.BREAKFAST -> Color(0xFFFDECD4)
        MealCategory.LUNCH -> Color(0xFFE3F2E1)
        MealCategory.DINNER -> Color(0xFFE8EAF6)
        MealCategory.SNACK -> Color(0xFFFCE4EC)
    }
    
    val dotColor = when(category) {
        MealCategory.BREAKFAST -> Color(0xFFF57C00)
        MealCategory.LUNCH -> Color(0xFF43A047)
        MealCategory.DINNER -> Color(0xFF3949AB)
        MealCategory.SNACK -> Color(0xFFD81B60)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(dotColor, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(categoryName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = darkBrown)
                if (plannedMeals.isNotEmpty()) {
                    Text(" (${plannedMeals.size})", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            if (plannedMeals.isNotEmpty()) {
                TextButton(onClick = onAddClick, contentPadding = PaddingValues(0.dp)) {
                    Text("+ Add", color = coralColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (plannedMeals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lightBg, RoundedCornerShape(12.dp))
                    .clickable(onClick = onAddClick)
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.AddCircleOutline, contentDescription = null, tint = dotColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Add $categoryName", color = dotColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
        } else {
            plannedMeals.forEachIndexed { index, plan ->
                val myMeal = viewModel.getMyMealById(plan.myMealId)
                if (myMeal != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(lightBg, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (myMeal.imageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(myMeal.imageUri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(myMeal.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = darkBrown)
                            val details = mutableListOf<String>()
                            if (myMeal.cookingTime != null) details.add(myMeal.cookingTime)
                            if (myMeal.calories != null) details.add("${myMeal.calories} kcal")
                            if (details.isNotEmpty()) {
                                Text(details.joinToString(" · "), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        
                        IconButton(onClick = { onRemoveClick(plan.id) }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (index < plannedMeals.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SlotPickerSheet(
    category: MealCategory,
    viewModel: MealPlanViewModel,
    onSelectMyMeals: (List<MyMeal>) -> Unit
) {
    val myMeals by viewModel.myMeals.collectAsState()
    val categoryMeals = myMeals.filter { it.category == category }
    val darkBrown = MaterialTheme.colorScheme.onBackground
    val coralColor = MaterialTheme.colorScheme.primary
    val darkGreen = MaterialTheme.colorScheme.secondary

    var selectedMeals by remember { mutableStateOf(setOf<MyMeal>()) }

    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(16.dp)) {
        val categoryName = category.name.lowercase().replaceFirstChar { it.uppercase() }
        Text("Add to $categoryName", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = darkBrown)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("My Meals", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (categoryMeals.isEmpty()) {
            Text("No meals saved for this category yet.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val pastelColors = listOf(Color(0xFFE8F5E9), Color(0xFFE3F2FD), Color(0xFFFFF3E0), Color(0xFFF3E5F5), Color(0xFFFFF8E1), Color(0xFFFFEBEE))
            val textColors = listOf(Color(0xFF2E7D32), Color(0xFF1565C0), Color(0xFFE65100), Color(0xFF6A1B9A), Color(0xFFF57F17), Color(0xFFC62828))
            
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categoryMeals.forEachIndexed { index, meal ->
                    val isSelected = selectedMeals.contains(meal)
                    val colorIndex = index % pastelColors.size
                    val pastelColor = pastelColors[colorIndex]
                    val textColor = textColors[colorIndex]
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) pastelColor else MaterialTheme.colorScheme.surface)
                            .then(if (isSelected) Modifier else Modifier.border(1.dp, pastelColor, RoundedCornerShape(16.dp)))
                            .clickable {
                                if (isSelected) {
                                    selectedMeals = selectedMeals - meal
                                } else {
                                    selectedMeals = selectedMeals + meal
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(meal.name, color = if (isSelected) textColor else darkBrown, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Recipe Ideas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        
        val examples = listOf(
            MyMeal(name = "Roasted Garlic Pasta", category = category, cookingTime = "25 min"),
            MyMeal(name = "Spiced Chickpea Bowl", category = category, cookingTime = "30 min"),
            MyMeal(name = "Mango Coconut Smoothie", category = category, cookingTime = "10 min")
        )
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(examples) { meal ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                        .clickable { onSelectMyMeals(listOf(meal)) }
                        .padding(12.dp)
                ) {
                    Text(meal.name, color = coralColor, fontWeight = FontWeight.Medium)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (selectedMeals.isNotEmpty()) {
                    onSelectMyMeals(selectedMeals.toList())
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = coralColor),
            shape = RoundedCornerShape(12.dp),
            enabled = selectedMeals.isNotEmpty()
        ) {
            Text("Add Selected", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MyMealsView(
    viewModel: MealPlanViewModel,
    darkBrown: Color,
    darkGreen: Color,
    coralColor: Color,
    lightPeach: Color
) {
    val myMeals by viewModel.myMeals.collectAsState()
    
    var showCustomMealDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("My Meals", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = darkBrown)
                Text("Your go-to meals", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Button(
                onClick = { showCustomMealDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = coralColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Add Meal", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold)
            }
        }
        
        MealCategory.values().forEach { category ->
            val categoryMeals = myMeals.filter { it.category == category }
            if (categoryMeals.isNotEmpty()) {
                val categoryName = category.name.lowercase().replaceFirstChar { it.uppercase() }
                Text(categoryName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = darkBrown, modifier = Modifier.padding(vertical = 8.dp))
                
                categoryMeals.forEach { meal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(meal.name, fontWeight = FontWeight.Medium, color = darkBrown)
                            val details = mutableListOf<String>()
                            if (meal.cookingTime != null) details.add(meal.cookingTime)
                            if (meal.calories != null) details.add("${meal.calories} kcal")
                            if (details.isNotEmpty()) {
                                Text(details.joinToString(" · "), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { viewModel.removeMyMeal(meal.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    if (showCustomMealDialog) {
        CustomMealDialog(
            onDismiss = { showCustomMealDialog = false },
            onSave = { meal -> 
                viewModel.addCustomMeal(meal)
                showCustomMealDialog = false 
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMealDialog(onDismiss: () -> Unit, onSave: (MyMeal) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(MealCategory.LUNCH) }
    var time by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Add Custom Meal", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Meal Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Category", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Cooking Time (e.g. 20 min)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(MyMeal(name = name, category = selectedCategory, cookingTime = time.ifBlank { null }, calories = calories.toIntOrNull(), isCustom = true))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8734A))
                    ) {
                        Text("Add Meal")
                    }
                }
            }
        }
    }
}

@Composable
fun MealPlanSetupFlow(viewModel: MealPlanViewModel, onComplete: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var selectedMeals by remember { mutableStateOf(setOf<MyMeal>()) }
    
    val predefinedMeals = listOf(
        MyMeal(
            name = "Poha", category = MealCategory.BREAKFAST, cookingTime = "15m", calories = 250,
            defaultIngredients = listOf(
                GroceryItem(name = "Flattened Rice", quantity = "2", unit = "cups", category = GroceryCategories.PANTRY),
                GroceryItem(name = "Peanuts", quantity = "1/4", unit = "cup", category = GroceryCategories.PANTRY),
                GroceryItem(name = "Onion", quantity = "1", unit = "medium", category = GroceryCategories.PRODUCE),
                GroceryItem(name = "Curry Leaves", quantity = "1", unit = "sprig", category = GroceryCategories.PRODUCE)
            )
        ),
        MyMeal(
            name = "Dal Rice", category = MealCategory.LUNCH, cookingTime = "30m", calories = 400,
            defaultIngredients = listOf(
                GroceryItem(name = "Rice", quantity = "1", unit = "cup", category = GroceryCategories.PANTRY),
                GroceryItem(name = "Toor Dal", quantity = "1/2", unit = "cup", category = GroceryCategories.PANTRY),
                GroceryItem(name = "Tomato", quantity = "1", unit = "medium", category = GroceryCategories.PRODUCE)
            )
        ),
        MyMeal(name = "Pasta", category = MealCategory.DINNER, cookingTime = "25m", calories = 550,
            defaultIngredients = listOf(
                GroceryItem(name = "Pasta", quantity = "200", unit = "g", category = GroceryCategories.PANTRY),
                GroceryItem(name = "Pasta Sauce", quantity = "1", unit = "jar", category = GroceryCategories.PANTRY),
                GroceryItem(name = "Cheese", quantity = "50", unit = "g", category = GroceryCategories.DAIRY_EGGS)
            )
        ),
        MyMeal(name = "Oatmeal", category = MealCategory.BREAKFAST, cookingTime = "10m", calories = 200,
            defaultIngredients = listOf(
                GroceryItem(name = "Oats", quantity = "1/2", unit = "cup", category = GroceryCategories.PANTRY),
                GroceryItem(name = "Milk", quantity = "1", unit = "cup", category = GroceryCategories.DAIRY_EGGS)
            )
        ),
        MyMeal(name = "Pesto Pasta", category = MealCategory.LUNCH, cookingTime = "20m", calories = 500,
            defaultIngredients = listOf(
                GroceryItem(name = "Pasta", quantity = "200", unit = "g", category = GroceryCategories.PANTRY),
                GroceryItem(name = "Pesto", quantity = "3", unit = "tbsp", category = GroceryCategories.PANTRY),
                GroceryItem(name = "Cherry Tomatoes", quantity = "1", unit = "cup", category = GroceryCategories.PRODUCE)
            )
        ),
        MyMeal(name = "Paneer Tikka", category = MealCategory.DINNER, cookingTime = "45m", calories = 450,
            defaultIngredients = listOf(
                GroceryItem(name = "Paneer", quantity = "250", unit = "g", category = GroceryCategories.DAIRY_EGGS),
                GroceryItem(name = "Yogurt", quantity = "1/2", unit = "cup", category = GroceryCategories.DAIRY_EGGS),
                GroceryItem(name = "Capsicum", quantity = "1", unit = "large", category = GroceryCategories.PRODUCE),
                GroceryItem(name = "Onion", quantity = "1", unit = "large", category = GroceryCategories.PRODUCE)
            )
        ),
        MyMeal(name = "Besan Chilla", category = MealCategory.BREAKFAST, cookingTime = "15m", calories = 220,
            defaultIngredients = listOf(
                GroceryItem(name = "Besan", quantity = "1", unit = "cup", category = GroceryCategories.PANTRY),
                GroceryItem(name = "Onion", quantity = "1", unit = "small", category = GroceryCategories.PRODUCE),
                GroceryItem(name = "Tomato", quantity = "1", unit = "small", category = GroceryCategories.PRODUCE)
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (step == 0) {
            Icon(Icons.Outlined.StarBorder, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFFE8734A))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Build Your Meal List", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add the meals you usually eat so you can quickly plan your week.",
                fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { step = 1 },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E4B31))
            ) {
                Text("Create My Meal List", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("You can edit this anytime.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text("What do you usually eat?", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("Select your go-to meals.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MealCategory.values()) { category ->
                    Column {
                        Text(category.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        predefinedMeals.filter { it.category == category }.forEach { meal ->
                            val isSelected = selectedMeals.contains(meal)
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .background(if (isSelected) Color(0xFF2E4B31) else MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (isSelected) selectedMeals -= meal else selectedMeals += meal
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(meal.name, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { 
                    viewModel.completeSetup(selectedMeals.toList())
                    onComplete()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8734A))
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
