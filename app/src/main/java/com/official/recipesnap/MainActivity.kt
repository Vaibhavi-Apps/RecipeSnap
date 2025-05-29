package com.official.recipesnap

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.official.recipesnap.ui.theme.RecipeSnapTheme
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

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
fun RecipeSnapScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var resultText by remember { mutableStateOf("Result will appear here") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("No file chosen") }
    var isLoading by remember { mutableStateOf(false) }
    var generatedRecipeText by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        fileName = uri?.lastPathSegment ?: "No file chosen"
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
                        .size(size)
                        .clickable { launcher.launch("image/*") },
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
                                text = "Tap to select image",
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                fontFamily = FontFamily.Serif
                            )
                            Text(
                                text = "PNG, JPG (max 5MB)",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val recipe = imageUri?.let { uploadImageAndGetRecipe(context, it) }
                            resultText = recipe ?: "No recipe found"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(start = 16.dp, end = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelBlue),
                ) {
                    Text("Get Recipe", color = Color.White, fontSize = 16.sp, fontFamily = FontFamily.Serif)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = PastelBlue)
                } else if (resultText.isNotBlank()) {
                    Text(
                        text = resultText,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    )


}

suspend fun uploadImageAndGetRecipe(context: Context, imageUri: Uri): String {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(imageUri)
    val imageBytes = inputStream?.readBytes() ?: return "Unable to read image"

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
    }

    val response: HttpResponse = client.post("http://192.168.1.14:8080/upload") {
        setBody(
            MultiPartFormDataContent(
                formData {
                    append(
                        "file",
                        imageBytes,
                        Headers.build {
                            append(HttpHeaders.ContentType, "image/jpg")
                            append(HttpHeaders.ContentDisposition, "filename=\"image.jpg\"")
                        }
                    )
                }
            )
        )
    }

    val json = Json { ignoreUnknownKeys = true }

    try {
        val responseString = response.bodyAsText()
        Log.e("Recipe Raw", responseString)

        // First, parse outer JSON
        val outer = json.decodeFromString<RecipeWrapper>(responseString)

        // Now parse the inner recipe string as JSON too
        val innerJson = json.decodeFromString<GeminiResponse>(outer.recipe)

        val text = innerJson.candidates.firstOrNull()
            ?.content?.parts?.firstOrNull()?.text ?: "No recipe found"

        return text
    } catch (e: Exception) {
        e.printStackTrace()
        return "Failed to extract recipe: ${e.message}"
    } finally {
        client.close()
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeSnapScreenPreview() {
    RecipeSnapTheme {
        RecipeSnapScreen()
    }
}
