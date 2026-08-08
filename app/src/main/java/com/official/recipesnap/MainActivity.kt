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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipeSnapTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    RecipeSnapScreen()
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
    
    android.util.Log.e("RecipeSnap", "API Key used: $apiKey")

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
    
    val PastelBlue = Color(0xFFFFB541)

    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.food_dinner_lunch_knife_fork_svgrepo_com),
                        contentDescription = "App Icon",
                        tint = PastelBlue,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Recipe Snap",
                        color = PastelBlue,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Cursive
                    )
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Snap a photo of your food 🍱",
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.titleLarge,
                            color = PastelBlue,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "Let AI whip up a recipe for you!",
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(top = 4.dp),
                            fontFamily = FontFamily.Serif
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val size = 280.dp

                Box(
                    modifier = Modifier
                        .size(size),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw gray background and dashed border
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color = Color(0xFFF0F0F0), // Gray background
                            style = Fill
                        )

                        drawCircle(
                            color = Color.Gray,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                            )
                        )
                    }

                    // Image or placeholder content
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(size - 12.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .size(size - 24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_camera_enhance_24),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(45.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Choose an image below",
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text("Gallery")
                    }
                    OutlinedButton(
                        onClick = {
                            tempCameraUri = createImageUri(context)
                            cameraLauncher.launch(tempCameraUri!!)
                        },
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text("Camera")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        imageUri?.let { uri ->
                            val bitmap = uriToBitmap(context, uri)
                            if (bitmap != null) {
                                viewModel.getRecipeFromImage(bitmap, apiKey)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(start = 16.dp, end = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelBlue),
                    enabled = imageUri != null && uiState !is RecipeUiState.Loading
                ) {
                    Text("Get Recipe", color = Color.White, fontSize = 16.sp, fontFamily = FontFamily.Serif)
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (val state = uiState) {
                    is RecipeUiState.Idle -> {
                        // Show nothing
                    }
                    is RecipeUiState.Loading -> {
                        CircularProgressIndicator(color = PastelBlue)
                    }
                    is RecipeUiState.Success -> {
                        RecipeCardStack(
                            recipes = state.recipes
                        )
                    }
                    is RecipeUiState.Error -> {
                        Text(
                            text = state.message,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }
    )
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
