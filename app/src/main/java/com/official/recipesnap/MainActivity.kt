package com.official.recipesnap

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "App Icon",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recipe Snap", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Snap a photo of your food, and let AI whip up a recipe for you!",
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Upload Your Image",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Take a clear picture of your food items. PNG, JPG, GIF supported (max 5MB).",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { launcher.launch("image/*") }) {
                    Text("Food Image")
                }

                Text(
                    text = fileName,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.placeholder_image),
                        contentDescription = "Placeholder",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                /*Button(
                    onClick = {
                        if (imageUri != null) {
                            coroutineScope.launch {
                                isLoading = true
                                generatedRecipeText = ""
                                val result = generateRecipeFromImage(context, imageUri!!)
                                generatedRecipeText = result
                                isLoading = false
                            }
                        } else {
                            Toast.makeText(context, "Please select an image first.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Get My Recipe!")
                }*/

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val recipe = imageUri?.let { uploadImageAndGetRecipe(context, it) }
                            resultText = recipe ?: "No recipe found"
                            Log.e("Recipe", recipe.toString())
                        }
                    }) {
                    Text("Get Recipe")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = resultText,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else if (generatedRecipeText.isNotBlank()) {
                    Text(
                        text = generatedRecipeText,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
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
