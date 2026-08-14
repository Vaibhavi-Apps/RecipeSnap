package com.official.recipesnap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MySnapsUiState {
    object Loading : MySnapsUiState
    data class Success(val snaps: List<SavedSnap>) : MySnapsUiState
    data class Error(val message: String) : MySnapsUiState
}

class InspirationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SnapRepository(application)
    
    private val _uiState = MutableStateFlow<MySnapsUiState>(MySnapsUiState.Loading)
    val uiState: StateFlow<MySnapsUiState> = _uiState.asStateFlow()

    init {
        loadSnaps()
    }

    fun loadSnaps() {
        _uiState.value = MySnapsUiState.Loading
        viewModelScope.launch {
            try {
                val snaps = repository.getSnaps()
                _uiState.value = MySnapsUiState.Success(snaps)
            } catch (e: Exception) {
                _uiState.value = MySnapsUiState.Error(e.localizedMessage ?: "Failed to load snaps")
            }
        }
    }

    fun toggleFavorite(snap: SavedSnap) {
        viewModelScope.launch {
            try {
                val updatedSnap = snap.copy(isFavorite = !snap.isFavorite)
                repository.updateSnap(updatedSnap)
                
                // Update local state without showing full loading indicator if possible
                val currentState = _uiState.value
                if (currentState is MySnapsUiState.Success) {
                    val updatedSnaps = currentState.snaps.map { 
                        if (it.id == snap.id) updatedSnap else it 
                    }
                    _uiState.value = MySnapsUiState.Success(updatedSnaps)
                } else {
                    loadSnaps() // Fallback to full reload
                }
            } catch (e: Exception) {
                // Ignore or log error
            }
        }
    }
}

