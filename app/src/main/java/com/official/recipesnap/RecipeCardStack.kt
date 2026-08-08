package com.official.recipesnap

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeCardStack(
    recipes: List<RecipeOption>
) {
    if (recipes.isEmpty()) {
        Text("No recipes available!", modifier = Modifier.padding(16.dp), color = Color.Gray)
        return
    }

    val pagerState = rememberPagerState(pageCount = { recipes.size })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(650.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hint for users to know they can swipe
        Text(
            text = "👈 Swipe to see more options 👉", 
            color = Color.Gray, 
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            RecipePagerCard(recipe = recipes[page])
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Dot Indicators
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color(0xFFFFB541) else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }
    }
}

@Composable
fun RecipePagerCard(
    recipe: RecipeOption
) {
    val context = LocalContext.current
    val markwon = remember { Markwon.create(context) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)) {
            Text(recipe.title, style = MaterialTheme.typography.headlineSmall, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macros with explicit text labels
            Surface(
                color = Color(0xFFF9F9F9),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Nutrition Facts", style = MaterialTheme.typography.titleMedium, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🔥 Calories: ${recipe.calories} kcal", color = Color.DarkGray, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🥩 Protein: ${recipe.protein}g", color = Color.DarkGray, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🍞 Carbs: ${recipe.carbs}g", color = Color.DarkGray, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🥑 Fat: ${recipe.fat}g", color = Color.DarkGray, style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx -> android.widget.TextView(ctx).apply { 
                    setTextColor(android.graphics.Color.DKGRAY)
                    textSize = 16f
                } },
                update = { tv -> markwon.setMarkdown(tv, recipe.content) }
            )
        }
    }
}
