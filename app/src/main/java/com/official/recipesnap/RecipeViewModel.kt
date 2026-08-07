package com.official.recipesnap

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    fun getRecipeFromImage(bitmap: Bitmap, apiKey: String) {
        _uiState.value = RecipeUiState.Loading

        val generativeModel = GenerativeModel(
            modelName = "gemini-flash-latest",
            apiKey = "AIzaSyCJjGyZ22n_VwtPDia2A6uQRhClGvAEryg"
        )

        viewModelScope.launch {
            try {
                val prompt = "Analyze the image and provide a detailed recipe based on the food or ingredients visible. Include a title, ingredients list, and step-by-step instructions."
                
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                
                val recipeText = response.text
                if (recipeText != null) {
                    _uiState.value = RecipeUiState.Success(recipeText)
                } else {
                    _uiState.value = RecipeUiState.Error("No recipe could be generated from this image.")
                }
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error(e.localizedMessage ?: "An unexpected error occurred.")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = RecipeUiState.Idle
    }
}

sealed interface RecipeUiState {
    object Idle : RecipeUiState
    object Loading : RecipeUiState
    data class Success(val recipe: String) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}
