package com.official.recipesnap

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RecipeOption(
    val title: String,
    val content: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val caption: String? = null,
    val hashtags: String? = null,
    val story: String? = null
)

class RecipeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    fun getRecipeFromImage(bitmap: Bitmap, apiKey: String, originalUriStr: String? = null, context: android.content.Context? = null) {
        _uiState.value = RecipeUiState.Loading

        val generativeModel = GenerativeModel(
            modelName = "gemini-flash-latest",
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        viewModelScope.launch {
            try {
                val prompt = """
                    Analyze this food image. Generate 5 distinct recipe variations based on these ingredients (e.g., standard, keto, spicy, quick, budget).
                    For each recipe, estimate the nutritional macros.
                    Also generate an engaging Instagram caption, a space-separated string of hashtags, and a short Story text.
                    Return a JSON array of objects. Each object MUST strictly have these keys:
                    "title" (string), "content" (string - markdown formatted instructions), "calories" (number), "protein" (number - grams), "carbs" (number - grams), "fat" (number - grams), "caption" (string), "hashtags" (string), "story" (string).
                """.trimIndent()
                
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                
                val jsonString = response.text
                if (jsonString != null) {
                    val recipes = Json { ignoreUnknownKeys = true }.decodeFromString<List<RecipeOption>>(jsonString)
                    _uiState.value = RecipeUiState.Success(recipes)
                    
                    if (originalUriStr != null && context != null) {
                        // Save bitmap to internal storage to ensure it persists
                        val snapId = java.util.UUID.randomUUID().toString()
                        val snapsDir = java.io.File(context.filesDir, "snaps").apply { mkdirs() }
                        val imageFile = java.io.File(snapsDir, "snap_$snapId.jpg")
                        java.io.FileOutputStream(imageFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        
                        val savedImageUri = android.net.Uri.fromFile(imageFile).toString()
                        
                        val repository = SnapRepository(context)
                        repository.saveSnap(
                            SavedSnap(
                                id = snapId,
                                imageUri = savedImageUri,
                                timestamp = System.currentTimeMillis(),
                                recipes = recipes
                            )
                        )
                    }
                } else {
                    _uiState.value = RecipeUiState.Error("No recipes could be generated from this image.")
                }
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error(e.localizedMessage ?: "An unexpected error occurred.")
            }
        }
    }
    
    fun getRecipeFromIngredients(ingredients: List<String>, apiKey: String, context: android.content.Context) {
        _uiState.value = RecipeUiState.Loading

        val generativeModel = GenerativeModel(
            modelName = "gemini-flash-latest",
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        viewModelScope.launch {
            try {
                val ingredientsList = ingredients.joinToString(", ")
                val prompt = """
                    The user wants a practical recipe using these selected ingredients as the primary ingredients: $ingredientsList.
                    Generate a recipe using these ingredients.
                    Estimate the nutritional macros.
                    Also generate an engaging Instagram caption, a space-separated string of hashtags, and a short Story text.
                    Return a JSON array of ONE object. The object MUST strictly have these keys:
                    "title" (string), "content" (string - markdown formatted instructions including a short description, the ingredients with quantities, and numbered cooking steps), "calories" (number), "protein" (number - grams), "carbs" (number - grams), "fat" (number - grams), "caption" (string), "hashtags" (string), "story" (string).
                """.trimIndent()
                
                val response = generativeModel.generateContent(
                    content {
                        text(prompt)
                    }
                )
                
                val jsonString = response.text
                if (jsonString != null) {
                    val recipes = Json { ignoreUnknownKeys = true }.decodeFromString<List<RecipeOption>>(jsonString)
                    _uiState.value = RecipeUiState.Success(recipes)
                    
                    // We generate a single recipe, save it automatically so it goes to "My Snaps"
                    val snapId = java.util.UUID.randomUUID().toString()
                    val placeholderUri = "android.resource://${context.packageName}/drawable/example_pasta"
                    val repository = SnapRepository(context)
                    repository.saveSnap(
                        SavedSnap(
                            id = snapId,
                            imageUri = placeholderUri,
                            timestamp = System.currentTimeMillis(),
                            recipes = recipes
                        )
                    )
                } else {
                    _uiState.value = RecipeUiState.Error("No recipe could be generated from these ingredients.")
                }
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error(e.localizedMessage ?: "An unexpected error occurred.")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = RecipeUiState.Idle
    }

    fun restoreRecipes(recipes: List<RecipeOption>) {
        _uiState.value = RecipeUiState.Success(recipes)
    }
}

sealed interface RecipeUiState {
    object Idle : RecipeUiState
    object Loading : RecipeUiState
    data class Success(val recipes: List<RecipeOption>) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}
