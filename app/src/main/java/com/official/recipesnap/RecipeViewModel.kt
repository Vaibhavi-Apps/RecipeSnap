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
    val fat: Double
)

class RecipeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    fun getRecipeFromImage(bitmap: Bitmap, apiKey: String) {
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
                    Return a JSON array of objects. Each object MUST strictly have these keys:
                    "title" (string), "content" (string - markdown formatted instructions), "calories" (number), "protein" (number - grams), "carbs" (number - grams), "fat" (number - grams).
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
                } else {
                    _uiState.value = RecipeUiState.Error("No recipes could be generated from this image.")
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
