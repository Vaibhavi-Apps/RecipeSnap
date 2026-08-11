package com.official.recipesnap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface InspirationUiState {
    object Loading : InspirationUiState
    data class Success(val recipes: List<RecipeDto>) : InspirationUiState
    data class Error(val message: String) : InspirationUiState
}

class InspirationViewModel : ViewModel() {
    private val api = SpoonacularApi.create()
    
    private val _uiState = MutableStateFlow<InspirationUiState>(InspirationUiState.Loading)
    val uiState: StateFlow<InspirationUiState> = _uiState.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        fetchRecipes("All")
    }

    fun selectCategory(category: String) {
        if (_selectedCategory.value == category) return
        _selectedCategory.value = category
        fetchRecipes(category)
    }

    private fun fetchRecipes(category: String) {
        _uiState.value = InspirationUiState.Loading
        viewModelScope.launch {
            try {
                val type = if (category == "All") null else category.lowercase()
                val response = api.searchRecipes(type = type)
                _uiState.value = InspirationUiState.Success(response.results)
            } catch (e: Exception) {
                _uiState.value = InspirationUiState.Error(e.localizedMessage ?: "Failed to fetch recipes")
            }
        }
    }
}
