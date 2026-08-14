package com.official.recipesnap

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryScreen(viewModel: GroceryViewModel = viewModel()) {
    val context = LocalContext.current
    val groceryItems by viewModel.groceryItems.collectAsState()
    val weekStart by viewModel.currentWeekStart.collectAsState()
    
    val darkBrown = Color(0xFF3E2723)
    val coralColor = Color(0xFFE8734A)
    val darkGreen = Color(0xFF2E4B31)
    val lightCream = Color(0xFFF7F3F0)
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    var showManualAdd by remember { mutableStateOf(false) }

    val activeItems = groceryItems.filter { !it.isSuggested }
    val suggestedItems = groceryItems.filter { it.isSuggested }
    
    val completedCount = activeItems.count { it.isCompleted }
    val totalCount = activeItems.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Scaffold(
        containerColor = lightCream,
        bottomBar = {
            if (activeItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = { viewModel.uncheckAll() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uncheck All", fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = {
                                val text = viewModel.getFormattedClipboardText()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Grocery List", text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Grocery list copied!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy List", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Grocery List", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = darkBrown)
                    Text("$completedCount of $totalCount items collected", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.previousWeek() }) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "Prev Week")
                    }
                    Text(viewModel.formatDateRange(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = darkBrown)
                    IconButton(onClick = { viewModel.nextWeek() }) {
                        Icon(Icons.Outlined.ChevronRight, contentDescription = "Next Week")
                    }
                }
            }
            
            // Progress Bar
            val animatedProgress by animateFloatAsState(targetValue = progress)
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = darkGreen,
                    trackColor = Color(0xFFE8E8E8)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${(progress * 100).toInt()}% done", fontSize = 12.sp, color = Color.Gray)
                    Text("${totalCount - completedCount} remaining", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (groceryItems.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your Grocery List is Empty", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = darkBrown)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Add meals to your Meal Plan and we'll suggest the ingredients you need.",
                        fontSize = 14.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { showManualAdd = true },
                        colors = ButtonDefaults.buttonColors(containerColor = coralColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("+ Add Item", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Manual Add Button
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clickable { showManualAdd = true },
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text("+ Add item", color = coralColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    // Suggested Items
                    if (suggestedItems.isNotEmpty()) {
                        item {
                            Text("Suggested for your meals", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                            
                            suggestedItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.Medium, color = darkBrown)
                                        Text("${item.quantity} ${item.unit} · ${item.sourceMealName}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    TextButton(onClick = { viewModel.acceptSuggestion(item) }) {
                                        Text("+ Add", color = darkGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    // Categorized Active Items
                    val grouped = activeItems.groupBy { it.category }
                    GroceryCategories.ALL.forEach { category ->
                        val items = grouped[category]
                        if (!items.isNullOrEmpty()) {
                            item {
                                GroceryCategoryCard(category, items, viewModel, darkBrown, darkGreen)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // padding for bottom bar
                    }
                }
            }
        }
    }

    if (showManualAdd) {
        ManualAddItemDialog(
            onDismiss = { showManualAdd = false },
            onAdd = { name, quantity, category ->
                viewModel.addManualItem(name, quantity, category)
                showManualAdd = false
            }
        )
    }
}

@Composable
fun GroceryCategoryCard(
    category: String,
    items: List<GroceryItem>,
    viewModel: GroceryViewModel,
    darkBrown: Color,
    darkGreen: Color
) {
    var expanded by remember { mutableStateOf(true) }
    
    val categoryIcon = when(category) {
        GroceryCategories.PRODUCE -> "🥦"
        GroceryCategories.DAIRY_EGGS -> "🥛"
        GroceryCategories.PANTRY -> "🥫"
        GroceryCategories.BAKERY -> "🥖"
        GroceryCategories.PROTEIN -> "🥩"
        GroceryCategories.FROZEN -> "🧊"
        GroceryCategories.BEVERAGES -> "🥤"
        else -> "🛒"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(categoryIcon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(category, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = darkBrown)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("(${items.count { it.isCompleted }}/${items.size})", fontSize = 12.sp, color = Color.Gray)
                }
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
            
            if (expanded) {
                HorizontalDivider(color = Color(0xFFF0F0F0))
                items.forEach { item ->
                    GroceryItemRow(item, viewModel, darkBrown, darkGreen)
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}

@Composable
fun GroceryItemRow(
    item: GroceryItem,
    viewModel: GroceryViewModel,
    darkBrown: Color,
    darkGreen: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.toggleItemCompletion(item) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (item.isCompleted) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = darkGreen, modifier = Modifier.fillMaxSize())
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color.Transparent, CircleShape).padding(2.dp)) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFE8734A),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = item.name,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = if (item.isCompleted) Color.Gray else darkBrown,
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null
            )
            val subText = buildString {
                append(item.quantity)
                if (item.unit.isNotBlank()) append(" ${item.unit}")
                if (item.sourceMealName != null) append(" · ${item.sourceMealName}")
            }
            Text(subText, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddItemDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GroceryCategories.PANTRY) }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Add grocery item", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (e.g. 500g)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Category", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                
                // Simple dropdown alternative using exposed dropdown menu
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        GroceryCategories.ALL.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { 
                                    selectedCategory = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && quantity.isNotBlank()) {
                                onAdd(name, quantity, selectedCategory)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8734A))
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
