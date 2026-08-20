package com.official.recipesnap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import io.noties.markwon.Markwon
import java.io.File
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.official.recipesnap.ui.theme.RecipeSnapTheme
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restaurant
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipeSnapTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    var showSplash by remember { mutableStateOf(true) }
                    
                    androidx.compose.animation.Crossfade(
                        targetState = showSplash,
                        animationSpec = androidx.compose.animation.core.tween(500),
                        label = "splash_transition"
                    ) { isSplash ->
                        if (isSplash) {
                            com.official.recipesnap.ui.SplashScreen(onContinue = { showSplash = false })
                        } else {
                            RecipeSnapScreen()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSnapScreen(viewModel: RecipeViewModel = viewModel()) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()
    val apiKey = BuildConfig.GEMINI_API_KEY.trim()
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            viewModel.resetState()
        }
    }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempCameraUri
            viewModel.resetState()
        }
    }
    
    var selectedIndex by remember { mutableIntStateOf(0) } // Snap is index 0
    
    var previousImageUri by remember { mutableStateOf<Uri?>(null) }
    var previousRecipes by remember { mutableStateOf<List<RecipeOption>?>(null) }
    
    var selectedExample by remember { mutableStateOf<ExampleRecipe?>(null) }
    
    val backgroundColor = MaterialTheme.colorScheme.background
    val coralColor = MaterialTheme.colorScheme.primary
    val darkBrown = MaterialTheme.colorScheme.onBackground
    val lightPeach = MaterialTheme.colorScheme.primaryContainer

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            com.official.recipesnap.ui.CustomBottomNavBar(
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it }
            )
        },
        content = { padding ->
            if (selectedIndex == 0) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    // Top Section
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Mini Logo
                        val creamWhite = MaterialTheme.colorScheme.background
                        val coralOrange = MaterialTheme.colorScheme.primary
                        val mutedGreen = MaterialTheme.colorScheme.secondary
                        val darkContainer = MaterialTheme.colorScheme.onSecondaryContainer

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(darkContainer)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(mutedGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Restaurant,
                                    contentDescription = "Logo",
                                    tint = creamWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 1.dp, y = (-1).dp)
                                        .background(coralOrange, CircleShape)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "Recipe Snap",
                                color = darkBrown,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Snap any food, get its recipe instantly",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    
                    if (previousImageUri != null && previousRecipes != null) {
                        IconButton(
                            onClick = {
                                imageUri = previousImageUri
                                viewModel.restoreRecipes(previousRecipes!!)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = "Previous Recipe",
                                tint = coralColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                if (imageUri != null && uiState is RecipeUiState.Success) {
                    val state = uiState as RecipeUiState.Success
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = { 
                                previousImageUri = imageUri
                                previousRecipes = state.recipes
                                imageUri = null
                                viewModel.resetState() 
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Retake",
                                tint = coralColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Retake", 
                                color = coralColor, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    RecipeCardStack(recipes = state.recipes)

                } else {
                    // Upload Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(lightPeach)
                            .clickable { launcher.launch("image/*") }
                            .drawBehind {
                                val stroke = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                )
                                drawRoundRect(
                                    color = coralColor,
                                    style = stroke,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Circular soft-peach icon button with camera icon
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CameraAlt,
                                        contentDescription = "Camera",
                                        tint = coralColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Snap a food photo",
                                    color = darkBrown,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "or tap to upload from gallery",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // If image selected, show Get Recipe button
                    if (imageUri != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val isLoading = uiState is RecipeUiState.Loading
                        
                        Button(
                            onClick = {
                                val bitmap = uriToBitmap(context, imageUri!!)
                                if (bitmap != null) {
                                    viewModel.getRecipeFromImage(bitmap, apiKey, imageUri.toString(), context)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = coralColor),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.surface,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Getting Recipe...", 
                                    color = MaterialTheme.colorScheme.surface, 
                                    fontSize = 16.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Get Recipe", 
                                    color = MaterialTheme.colorScheme.surface, 
                                    fontSize = 16.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState is RecipeUiState.Error) {
                            val state = uiState as RecipeUiState.Error
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Divider
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                            Text(
                                text = "or try an example",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Example Grid
                        val examples = listOf(
                            ExampleRecipe(
                                "Spicy Arrabbiata Pasta", 
                                "A fiery and garlic-heavy Italian classic made with tomatoes, dried red chili peppers, and olive oil.",
                                listOf("Italian", "Dinner", "Spicy"),
                                R.drawable.example_pasta
                            ),
                            ExampleRecipe(
                                "Quinoa Salad Bowl", 
                                "A fresh and healthy bowl packed with protein-rich quinoa, leafy greens, and a tangy lemon vinaigrette.",
                                listOf("Healthy", "Lunch", "Vegan"),
                                R.drawable.example_salad
                            ),
                            ExampleRecipe(
                                "Avocado Toast with Egg", 
                                "The ultimate breakfast! Creamy smashed avocado on sourdough, topped with a perfectly poached egg.",
                                listOf("Breakfast", "Quick", "Vegetarian"),
                                R.drawable.example_avocado
                            ),
                            ExampleRecipe(
                                "Fluffy Sweet Pancakes", 
                                "Classic American-style pancakes that are incredibly soft and fluffy, perfect with maple syrup.",
                                listOf("Breakfast", "Sweet", "Comfort Food"),
                                R.drawable.example_pancakes
                            )
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ExampleCard(recipe = examples[0], modifier = Modifier.weight(1f).aspectRatio(1f)) { selectedExample = examples[0] }
                                ExampleCard(recipe = examples[1], modifier = Modifier.weight(1f).aspectRatio(1f)) { selectedExample = examples[1] }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ExampleCard(recipe = examples[2], modifier = Modifier.weight(1f).aspectRatio(1f)) { selectedExample = examples[2] }
                                ExampleCard(recipe = examples[3], modifier = Modifier.weight(1f).aspectRatio(1f)) { selectedExample = examples[3] }
                            }
                        }
                    }
                }
            }
            
            // Example Recipe Dialog
            if (selectedExample != null) {
                val recipe = selectedExample!!
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { selectedExample = null },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            // Recipe Image
                            Image(
                                painter = painterResource(id = recipe.imageRes),
                                contentDescription = recipe.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant), // placeholder background
                                contentScale = ContentScale.Crop
                            )
                            
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = recipe.title, 
                                    fontSize = 22.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = darkBrown
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Tags
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    recipe.tags.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .background(lightPeach, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = tag, 
                                                fontSize = 12.sp, 
                                                color = coralColor, 
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = recipe.description, 
                                    color = MaterialTheme.colorScheme.onSurface, 
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Button(
                                    onClick = {
                                        // Synthesize a Uri for the drawable so the UI transitions smoothly
                                        imageUri = Uri.parse("android.resource://${context.packageName}/${recipe.imageRes}")
                                        // Trigger Get Recipe flow using the example image
                                        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, recipe.imageRes)
                                        viewModel.getRecipeFromImage(bitmap, apiKey, imageUri.toString(), context)
                                        selectedExample = null // Dismiss dialog
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = coralColor)
                                ) {
                                    Text("Get Recipe", color = MaterialTheme.colorScheme.surface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } // closes if (selectedExample != null)
        } else if (selectedIndex == 1) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                BuildRecipeScreen(
                    viewModel = viewModel,
                    apiKey = apiKey,
                    onRecipeGenerated = { }
                )
            }
        } else if (selectedIndex == 2) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                InspirationScreen()
            }
        } else if (selectedIndex == 3) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                MealPlanScreen()
            }
        } else if (selectedIndex == 4) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                GroceryScreen()
            }
        } else {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(), 
                contentAlignment = Alignment.Center
            ) {
                Text("Coming Soon!", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
            }
        }
    )
}

data class ExampleRecipe(
    val title: String,
    val description: String,
    val tags: List<String>,
    val imageRes: Int
)

@Composable
fun ExampleCard(recipe: ExampleRecipe, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        // Image Background
        Image(
            painter = painterResource(id = recipe.imageRes),
            contentDescription = recipe.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Dark gradient overlay at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )
        
        Text(
            text = recipe.title,
            color = MaterialTheme.colorScheme.surface,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }
}

fun createImageUri(context: Context): Uri {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir: File = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File.createTempFile(imageFileName, ".jpg", storageDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

@Preview(showBackground = true)
@Composable
fun RecipeSnapScreenPreview() {
    RecipeSnapTheme {
        RecipeSnapScreen()
    }
}
